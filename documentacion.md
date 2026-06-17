# Documentacion tecnica de MarIA

MarIA es un sistema distribuido de recomendacion musical desarrollado en Java. El proyecto gestiona una coleccion fisica de discos y vinilos, expone una interfaz Swing para el usuario y concentra la logica de inteligencia artificial en un servidor TCP que procesa mensajes JSON.

### Organizacion del proyecto

```text
.
├── config.properties
├── data
│   ├── catalogo.json
│   ├── coleccion.json
│   ├── maria.sqlite                # Base de datos SQLite
│   ├── recommendation_learning.json
│   └── recommendation_history.json
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

Los archivos principales de datos están en `data/`. `catalogo.json` funciona como catálogo base global para recomendaciones y `coleccion.json` es el conjunto inicial de discos que se utiliza para poblar la base de datos relacional SQLite (`maria.sqlite`). El aprendizaje y la fatiga del recomendador se guardan en archivos JSON locales.

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
- `AccionesCliente` orquesta la lógica de negocio interactuando con la base SQLite y los agentes.
- `Database` actúa como fachada delegando las peticiones a `SqliteAlbumRepository` para interactuar con SQLite de forma segura.
- Los agentes en `servidor/model/agentes` implementan la lógica del sistema multiagente.

Las clases compartidas viven en `compartido` para asegurar el protocolo de datos común:

- `Disco`: modelo de álbum de música física.
- `MensajeSocket`: petición del cliente que incluye acción, datos opcionales, `pagina` y `tamanoPagina`.
- `RespuestaSocket`: respuesta del servidor con soporte de paginación (`listaDiscos`, `totalDiscos`).
- `TlsConfig`: utilidades comunes para cargar claves y certificados TLS.

## Sistema multiagente

El sistema multiagente se ejecuta en el modelo del servidor. Para el despliegue escolar se usa una sola colección compartida entre la laptop cliente y la laptop servidor.

### AgenteAnalizador

`AgenteAnalizador` calcula estadísticas de consumo musical a partir de la colección. Determina los géneros dominantes, frecuencias de artistas, décadas y porcentajes de afinidad. Su salida es un `PerfilGustos`.

### AgenteBuscador

`AgenteBuscador` realiza búsquedas aproximadas mediante Levenshtein y normalización de clave fonética en español. Permite tolerar errores de tipeado (como `v/b`, acentos omitidos o la `h` muda). Ahora soporta paginación física delegada al repositorio SQLite para consultas de alta velocidad.

### AgenteRecomendador

`AgenteRecomendador` es un singleton que genera sugerencias musicales utilizando aprendizaje por refuerzo (Bandido Multibrazo), afinidades históricas por artista/década y control de fatiga.
* El estado del bandido y el historial de recomendaciones recientes se guardan en `data/recommendation_learning.json` y `data/recommendation_history.json`.
* Las acciones `ACEPTAR_RECOMENDACION` y `RECHAZAR_RECOMENDACION` ajustan esas señales para mejorar las recomendaciones posteriores.

## Protocolo socket JSON

La comunicación usa sockets TCP y Gson. Cada mensaje viaja como una única línea JSON.

Ejemplo de solicitud paginada de colección:

```json
{
  "transaccionId": "t-list-123",
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
`INFORMACIÓN: event=socket_received correlationId=t-list-123 userId=default-user remote=/127.0.0.1:54988 action=LISTAR_DISCOS page=1 size=10`
`INFORMACIÓN: event=socket_response correlationId=t-list-123 userId=default-user action=LISTAR_DISCOS status=OK total=1`

## Base de datos y catalogo

La colección de discos se gestiona de forma persistente en SQLite utilizando la clase SqliteAlbumRepository.java.

* **Bootstrap inicial:** Al iniciar el servidor por primera vez, si `data/maria.sqlite` está vacío, `JsonCollectionBootstrap.java` lee `coleccion.json` y migra sus registros a SQLite.
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
mvn exec:java -Dexec.mainClass="uaemex.ia.proyecto.servidor.ServidorApp"
```

Iniciar cliente:

```bash
mvn exec:java -Dexec.mainClass="uaemex.ia.proyecto.cliente.ClienteApp"
```

### Habilitar seguridad TLS
Para forzar la comunicación encriptada:
1. Genera un almacén PKCS12 local para el servidor:
   ```bash
   keytool -genkeypair -alias maria -keyalg RSA -keysize 2048 -validity 365 \
     -storetype PKCS12 -keystore certs/maria-keystore.p12
   ```
2. Exporta/importa el certificado hacia `certs/maria-truststore.p12`.
3. Cambia `server.tls.enabled=true` y ajusta las contraseñas en `config.properties`.

## Troubleshooting

### Error: `org.sqlite.SQLiteException: [SQLITE_BUSY]`
El archivo SQLite de la base de datos se encuentra bloqueado por otro proceso. Asegúrate de que no haya múltiples instancias del servidor corriendo simultáneamente.

### Error de TLS / SSL Handshake Exception
Se produce cuando hay incompatibilidad de certificados entre cliente y servidor. Si se activa `server.tls.enabled=true`, asegúrate de que ambos extremos carguen los mismos almacenes de llaves y certificados del paquete `compartido/TlsConfig`.

## Pruebas automatizadas

El proyecto incluye 10 pruebas automatizadas divididas en:
1. **Pruebas de red:** Validación de comunicación socket PING/PONG.
2. **Pruebas de idempotencia:** Verificación de no duplicidad de transacciones idénticas en caché LRU.
3. **Pruebas del Buscador:** Testeo de distancias fonéticas y Levenshtein en español.
4. **Pruebas de Persistencia SQLite:** Creación de tablas, inserción, paginación física y restricciones de unicidad de discos.
5. **Pruebas de Aprendizaje:** Validación de convergencia de feedback positivo/negativo e inclinación por políticas épsilon-greedy.

Ejecutar las pruebas:
```bash
mvn test
```
Una suite exitosa finalizará con `BUILD SUCCESS`.
