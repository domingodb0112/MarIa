# Documentacion tecnica de MarIA

MarIA es un sistema distribuido de recomendacion musical desarrollado en Java. El proyecto gestiona una coleccion fisica de discos y vinilos, expone una interfaz Swing para el usuario y concentra la logica de inteligencia artificial en un servidor TCP que procesa mensajes JSON.

### Organizacion del proyecto

```text
.
├── config.properties
├── data
│   ├── catalogo.json
│   ├── coleccion.json
│   ├── coleccion.db                # Base de datos SQLite
│   └── users                       # Almacenamiento aislado por usuario
│       └── <userId>
│           ├── recommendation_learning.json
│           └── recommendation_history.json
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

Los archivos principales de datos están en `data/`. `catalogo.json` funciona como catálogo base global para recomendaciones y `coleccion.json` es el conjunto inicial de discos que se utiliza para poblar la base de datos relacional SQLite (`coleccion.db`). Los datos de aprendizaje y fatiga de cada usuario se almacenan de manera aislada en `data/users/<userId>/`.

`config.properties` centraliza la configuración de red y seguridad:

```properties
server.ip=localhost
server.port=5000
server.tls.enabled=false
```

El cliente usa `server.ip` y `server.port`; el servidor usa `server.port`. Si `server.tls.enabled` es `true`, la comunicación se cifrará utilizando sockets SSL/TLS.

## Arquitectura MVC

El proyecto aplica MVC de forma distribuida. La vista vive en el cliente, el controlador de red existe en ambos lados y el modelo principal vive en el servidor.

En el cliente:

- `ClienteApp` lee la configuración e inicia la interfaz visual Swing.
- `VentanaPrincipal` organiza el panel principal de la interfaz de usuario.
- Los paneles de `cliente/view` desacoplan los distintos módulos (búsqueda paginada, formulario, estadísticas y recomendaciones).
- `ClientPresenter` media entre las interacciones del usuario y la lógica de red.
- `ClientController` administra la conexión por socket TCP (estándar o SSL/TLS vía `TlsClientSockets`), serializa peticiones JSON y maneja la lógica de reconexión asíncrona.
- `HeartbeatCliente` mantiene un PING periódico para validar la salud de la red.

En el servidor:

- `ServidorApp` inicia el servidor en el puerto e indica si se utilizará TLS.
- `ServerController` abre el `ServerSocket` (estándar o SSL/TLS vía `TlsServerSockets`) y delega las conexiones de clientes a un `ExecutorService`.
- `ManejadorCliente` atiende un cliente por hilo, delega a `TransactionLog` el logueo estructurado de cada mensaje entrante/saliente, y envía las solicitudes a `AccionesCliente`.
- `AccionesCliente` orquesta la lógica de negocio multi-inquilino interactuando con los agentes correspondientes de acuerdo al `userId`.
- `Database` actúa como fachada delegando las peticiones a `SqliteAlbumRepository` para interactuar con SQLite de forma segura.
- Los agentes en `servidor/model/agentes` implementan la lógica del sistema multiagente.

Las clases compartidas viven en `compartido` para asegurar el protocolo de datos común:

- `Disco`: modelo de álbum de música física.
- `MensajeSocket`: petición del cliente que incluye `userId`, `pagina` y `tamanoPagina`.
- `RespuestaSocket`: respuesta del servidor con soporte de paginación (`listaDiscos`, `totalDiscos`).
- `TlsConfig`: utilidades comunes para cargar claves y certificados TLS.

## Sistema multiagente

El sistema multiagente se ejecuta en el modelo del servidor y procesa las peticiones aislando la identidad de cada usuario a través de su `userId`.

### AgenteAnalizador

`AgenteAnalizador` calcula estadísticas de consumo musical a partir de la colección de un usuario específico. Determina los géneros dominantes, frecuencias de artistas, décadas y calcula los porcentajes de afinidad del usuario. Su salida es un `PerfilGustos`.

### AgenteBuscador

`AgenteBuscador` realiza búsquedas aproximadas mediante Levenshtein y normalización de clave fonética en español. Permite tolerar errores de tipeado (como `v/b`, acentos omitidos o la `h` muda). Ahora soporta paginación física delegada al repositorio SQLite para consultas de alta velocidad.

### AgenteRecomendador

`AgenteRecomendador` es un singleton que genera sugerencias musicales utilizando aprendizaje por refuerzo (Bandido Multibrazo), afinidades históricas por artista/década y control de fatiga.
* El estado del bandido y el historial de recomendaciones recientes se guardan de forma aislada por usuario en `data/users/<userId>/recommendation_learning.json` y `data/users/<userId>/recommendation_history.json`.
* Las acciones `ACEPTAR_RECOMENDACION` y `RECHAZAR_RECOMENDACION` se rigen bajo este mismo aislamiento, permitiendo que las decisiones de un usuario no afecten las recomendaciones de otros inquilinos.

## Protocolo socket JSON

La comunicación usa sockets TCP y Gson. Cada mensaje viaja como una única línea JSON.

Ejemplo de solicitud paginada de colección:

```json
{
  "transaccionId": "t-list-123",
  "userId": "usuario-maria-9",
  "accion": "LISTAR_DISCOS",
  "pagina": 1,
  "tamanoPagina": 10
}
```

Respuesta del servidor con paginación y log estructurado:

```json
{
  "transaccionId": "t-list-123",
  "status": "OK",
  "mensaje": "Mostrando pagina 1 de 1",
  "listaDiscos": [
    {
      "titulo": "Bocanada",
      "artista": "Gustavo Cerati",
      "anio": 1999,
      "genero": "Rock",
      "formato": "CD"
    }
  ],
  "totalDiscos": 1
}
```

Cada petición en el socket es capturada por `TransactionLog` y logueada con el siguiente formato estructurado:
`INFORMACIÓN: event=socket_received correlationId=t-list-123 userId=usuario-maria-9 remote=/127.0.0.1:54988 action=LISTAR_DISCOS page=1 size=10`
`INFORMACIÓN: event=socket_response correlationId=t-list-123 userId=usuario-maria-9 action=LISTAR_DISCOS status=OK total=1`

## Base de datos y catalogo

La colección de discos se gestiona de forma persistente en SQLite utilizando la clase SqliteAlbumRepository.java.

* **Bootstrap inicial:** Al iniciar el servidor por primera vez, si el archivo `data/coleccion.db` no existe, se ejecuta JsonCollectionBootstrap.java, el cual lee coleccion.json y migra todos los registros a la base de datos relacional SQLite bajo el usuario por defecto `default-user`.
* **Esquema:** La tabla `discos` cuenta con columnas `id`, `user_id`, `titulo`, `artista`, `anio`, `genero` y `formato`, garantizando restricciones de unicidad por combinación de `user_id`, `titulo` y `artista`.
* **Paginación:** Todas las operaciones de listado y búsqueda incluyen cláusulas `LIMIT` y `OFFSET` en SQL para garantizar un rendimiento óptimo de la memoria del servidor.

El catálogo base para recomendaciones sigue leyéndose desde catalogo.json.

## Configuracion y ejecucion

Compilar y probar:

```bash
mvn test
```

Iniciar servidor (el puerto se obtiene de `config.properties`, por defecto `5000`):

```bash
java -cp target/classes:lib/gson-2.10.1.jar:lib/sqlite-jdbc-3.45.1.0.jar uaemex.ia.proyecto.servidor.ServidorApp
```

Iniciar cliente:

```bash
java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.cliente.ClienteApp
```

### Habilitar seguridad TLS
Para forzar la comunicación encriptada:
1. Cambia `server.tls.enabled=true` en `config.properties`.
2. El sistema creará sockets utilizando los almacenes de llaves internos definidos por `TlsConfig`.

## Troubleshooting

### Error: `org.sqlite.SQLiteException: [SQLITE_BUSY]`
El archivo SQLite de la base de datos se encuentra bloqueado por otro proceso. Asegúrate de que no haya múltiples instancias del servidor corriendo simultáneamente.

### Error de TLS / SSL Handshake Exception
Se produce cuando hay incompatibilidad de certificados entre cliente y servidor. Si se activa `server.tls.enabled=true`, asegúrate de que ambos extremos carguen los mismos almacenes de llaves y certificados del paquete `compartido/TlsConfig`.

### Las recomendaciones de un usuario se mezclan con las de otro
Asegúrate de que el cliente esté enviando el parámetro `userId` único en la cabecera de la trama `MensajeSocket`. De lo contrario, se utilizará el identificador por defecto `default-user`.

## Pruebas automatizadas

El proyecto incluye 10 pruebas automatizadas divididas en:
1. **Pruebas de red:** Validación de comunicación socket PING/PONG.
2. **Pruebas de idempotencia:** Verificación de no duplicidad de transacciones idénticas en caché LRU.
3. **Pruebas del Buscador:** Testeo de distancias fonéticas y Levenshtein en español.
4. **Pruebas de Persistencia SQLite:** Creación de tablas, inserción, paginación física y restricciones de unicidad de discos.
5. **Pruebas de Aprendizaje Multi-Usuario:** Validación de aislamiento de gustos entre usuarios, convergencia de feedback positivo/negativo e inclinación por políticas épsilon-greedy.

Ejecutar las pruebas:
```bash
mvn test
```
Una suite exitosa finalizará con `BUILD SUCCESS`.
