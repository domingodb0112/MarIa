# Documentacion tecnica de MarIA

MarIA es un sistema distribuido de recomendacion musical desarrollado en Java. El proyecto gestiona una coleccion fisica de discos y vinilos, expone una interfaz Swing para el usuario y concentra la logica de inteligencia artificial en un servidor TCP que procesa mensajes JSON.

## Organizacion del proyecto

```text
.
├── config.properties
├── data
│   ├── catalogo.json
│   └── coleccion.json
├── lib
│   └── gson-2.10.1.jar
├── pom.xml
├── README.md
├── documentacion.md
└── src
    ├── main/java/uaemex/ia/proyecto
    │   ├── cliente
    │   ├── compartido
    │   ├── herramientas
    │   └── servidor
    └── test/java
```

Los archivos principales de datos estan en `data/`. `catalogo.json` funciona como catalogo base para recomendaciones y `coleccion.json` es la coleccion persistida que carga el servidor. Ambos usan objetos con los campos `titulo`, `artista`, `anio`, `genero` y `formato`.

`config.properties` centraliza la configuracion de red:

```properties
server.ip=localhost
server.port=5000
```

El cliente usa `server.ip` y `server.port`; el servidor usa `server.port` y cae al puerto `5000` si el archivo no existe o el valor es invalido.

## Arquitectura MVC

El proyecto aplica MVC de forma distribuida. La vista vive en el cliente, el controlador de red existe en ambos lados y el modelo principal vive en el servidor.

En el cliente:

- `ClienteApp` lee `config.properties` y abre la interfaz Swing.
- `VentanaPrincipal` organiza la ventana principal.
- Los paneles de `cliente/view` separan formulario, busqueda, consultas, recomendaciones, estado y estadisticas.
- `ClientPresenter` conecta eventos de la interfaz con peticiones al servidor.
- `ClientController` administra el socket TCP, serializa solicitudes JSON, lee respuestas y ejecuta reconexion con backoff.
- `HeartbeatCliente` envia `PING` periodicos para validar que la conexion siga viva.

En el servidor:

- `ServidorApp` lee el puerto configurado e inicia el servidor.
- `ServerController` abre el `ServerSocket` y delega cada cliente a un `ExecutorService`.
- `ManejadorCliente` atiende un cliente por hilo, lee una linea JSON por solicitud y responde con una linea JSON.
- `AccionesCliente` orquesta la logica de negocio: registro, listado, busqueda, recomendaciones y retroalimentacion.
- `Database` mantiene la coleccion en memoria y la persiste en JSON.
- Los agentes en `servidor/model/agentes` implementan el sistema multiagente.

Las clases compartidas viven en `compartido` para que cliente y servidor usen el mismo contrato de datos:

- `Disco`: modelo de album fisico.
- `MensajeSocket`: solicitud enviada por el cliente.
- `RespuestaSocket`: respuesta enviada por el servidor.

## Sistema multiagente

El sistema multiagente se ejecuta en el modelo del servidor. Cada agente tiene una responsabilidad acotada y `AccionesCliente` los coordina segun la accion recibida por socket.

### AgenteAnalizador

`AgenteAnalizador` calcula el perfil de gustos a partir de la coleccion actual. Normaliza generos, cuenta frecuencias, calcula porcentajes y determina el genero favorito. Su resultado es un `PerfilGustos`, usado por el servidor para mostrar estadisticas y alimentar el recomendador.

Flujo principal:

1. Recibe la lista de discos desde `Database.getInstance().obtenerTodos()`.
2. Normaliza el genero de cada disco.
3. Ordena las frecuencias de mayor a menor.
4. Devuelve total de discos, genero favorito, conteos y porcentajes.

### AgenteBuscador

`AgenteBuscador` realiza busquedas aproximadas por titulo, artista o genero. Usa `SimilarityUtils` para normalizar texto, calcular distancia Levenshtein y obtener una clave fonetica pensada para errores comunes en espanol.

La busqueda:

1. Normaliza la consulta.
2. Compara contra titulo, artista y genero de cada disco.
3. Acepta candidatos dentro de un umbral dependiente de la longitud de la consulta.
4. Ordena por mejor coincidencia y devuelve hasta 10 resultados.

Esto permite tolerar acentos omitidos, diferencias de mayusculas, errores pequenos y coincidencias foneticas.

### AgenteRecomendador

`AgenteRecomendador` es un singleton compartido por los hilos del servidor. Genera hasta 8 recomendaciones evitando discos que ya existen en la coleccion del usuario.

Sus senales principales son:

- Perfil de gustos calculado por `AgenteAnalizador`.
- Afinidad historica de artistas y decadas mediante `PerfilAfinidad`.
- Aprendizaje por retroalimentacion con `BrazoRecomendacion`.
- Exploracion epsilon para no repetir siempre la misma ruta.
- Penalizacion de recomendaciones recientes con `HistorialRecomendacion`.

El aprendizaje se guarda con `RecomendadorStorage` en archivos JSON auxiliares dentro de `data/`, como `recommendation_learning.json` y `recommendation_history.json` cuando el sistema los genera.

La retroalimentacion entra por las acciones `ACEPTAR_RECOMENDACION` y `RECHAZAR_RECOMENDACION`. Cada respuesta actualiza senales por genero, artista y decada.

## Protocolo socket JSON

La comunicacion usa sockets TCP y Gson. Cada mensaje viaja como una linea JSON completa; por eso `ManejadorCliente` usa `BufferedReader.readLine()` y el cliente escribe con `PrintWriter.println()`.

Solicitud:

```json
{
  "transaccionId": "uuid",
  "accion": "BUSCAR_ALBUM",
  "datos": {
    "titulo": "Kind of Blue",
    "artista": "Miles Davis",
    "anio": 1959,
    "genero": "Jazz",
    "formato": "Vinilo"
  }
}
```

Respuesta con disco:

```json
{
  "transaccionId": "uuid",
  "status": "OK",
  "mensaje": "Disco registrado y guardado correctamente.",
  "datos": {
    "titulo": "Kind of Blue",
    "artista": "Miles Davis",
    "anio": 1959,
    "genero": "Jazz",
    "formato": "Vinilo"
  }
}
```

Respuesta con lista:

```json
{
  "transaccionId": "uuid",
  "status": "OK",
  "mensaje": "8 recomendacion(es) generada(s) segun tu perfil: Salsa",
  "listaDiscos": []
}
```

Respuesta de error:

```json
{
  "transaccionId": "uuid",
  "status": "ERROR",
  "mensaje": "Accion no reconocida: ACCION_INVALIDA"
}
```

Acciones soportadas por `ManejadorCliente`:

- `PING`: responde `PONG`.
- `REGISTRAR_DISCO`: valida, evita duplicados y persiste un disco.
- `LISTAR_DISCOS`: devuelve toda la coleccion.
- `BUSCAR_ALBUM`: busca por titulo, artista o genero.
- `OBTENER_RECOMENDACIONES`: genera recomendaciones personalizadas.
- `ACEPTAR_RECOMENDACION`: registra retroalimentacion positiva.
- `RECHAZAR_RECOMENDACION`: registra retroalimentacion negativa.

`RegistroTransacciones` evita reprocesar solicitudes idempotentes cuando el cliente reintenta por fallos de red.

## Base de datos y catalogo

`Database` es un singleton del servidor. Al iniciar, lee `data/coleccion.json` si existe; si no existe, crea una coleccion vacia en memoria. Cuando se registra un disco, lo agrega a la lista y llama a `persistir()`.

La persistencia usa una escritura defensiva:

1. Crea el directorio `data` si hace falta.
2. Escribe primero `data/coleccion.json.tmp`.
3. Reemplaza `data/coleccion.json` con `Files.move`.
4. Intenta `ATOMIC_MOVE`; si el sistema de archivos no lo soporta, usa reemplazo normal.
5. Borra el temporal si queda presente.

El catalogo base para recomendaciones vive en `data/catalogo.json`. La herramienta `ImportadorColeccion` puede leer un catalogo y generar una salida balanceada:

```bash
javac -cp target/classes:lib/gson-2.10.1.jar -d target/classes src/main/java/uaemex/ia/proyecto/herramientas/ImportadorColeccion.java
java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.herramientas.ImportadorColeccion
```

Para validar la distribucion actual:

```bash
jq 'length' data/catalogo.json
jq 'group_by(.genero) | map({genero: .[0].genero, total: length}) | sort_by(.genero)' data/catalogo.json
```

## Configuracion y ejecucion

Compilar y probar:

```bash
mvn test
```

Iniciar servidor:

```bash
mvn -q exec:java -Dexec.mainClass=uaemex.ia.proyecto.servidor.ServidorApp
```

Si no se usa el plugin de Maven, se puede ejecutar desde clases compiladas:

```bash
mvn compile
java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.servidor.ServidorApp
```

Iniciar cliente:

```bash
java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.cliente.ClienteApp
```

Para despliegue en dos computadoras:

1. Conectar ambas maquinas a la misma red.
2. En la maquina servidor, obtener la IPv4 local.
3. Editar `config.properties` en la maquina cliente:

```properties
server.ip=192.168.1.45
server.port=5000
```

4. Ejecutar primero `ServidorApp`.
5. Ejecutar despues `ClienteApp`.

## Troubleshooting

### El cliente muestra que no puede conectar

Revisar que el servidor este ejecutandose y que `server.ip` apunte a la IP correcta. En pruebas locales debe bastar con `server.ip=localhost`.

Tambien validar conectividad:

```bash
ping 192.168.1.45
```

### El puerto esta ocupado

Cambiar `server.port` en `config.properties` y reiniciar tanto cliente como servidor. El puerto debe estar entre `1` y `65535`.

### El firewall bloquea la conexion

Permitir conexiones TCP entrantes hacia el puerto configurado en la maquina servidor. En Windows, agregar una regla de entrada para Java o para el puerto `5000`. En Linux, revisar reglas de `ufw` o del firewall activo.

### El servidor no responde o el cliente se desconecta

`ClientController` usa timeout de conexion, timeout de lectura y reconexion con backoff. Si todos los reintentos fallan, revisar logs del servidor, direccion IP, firewall y que no haya otra aplicacion usando el puerto.

### Aparece `JSON malformado`

Cada solicitud debe enviarse como una sola linea JSON completa. No enviar fragmentos multilnea por el socket. Validar que la estructura coincida con `MensajeSocket`.

### Las recomendaciones salen vacias

Verificar que `data/catalogo.json` tenga discos y que no todos esten ya presentes en `data/coleccion.json`. El recomendador filtra los discos existentes por titulo y artista normalizados.

### La coleccion no se guarda

Revisar permisos de escritura sobre el directorio `data`. El servidor necesita poder crear `data/coleccion.json.tmp` y reemplazar `data/coleccion.json`.

### Busquedas sin resultados esperados

La busqueda usa titulo, artista o genero. Si el formulario envia campos vacios, el servidor responde `Se requiere titulo, artista o genero.`. Probar con una palabra corta del titulo o el genero exacto para aislar el problema.

## Pruebas automatizadas

El proyecto incluye pruebas para componentes de red, busqueda y transacciones. Ejecutar:

```bash
mvn test
```

Una ejecucion correcta debe terminar con `BUILD SUCCESS`.
