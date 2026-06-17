# MarIA - Sistema de Recomendación Musical con Inteligencia Artificial

MarIA es una aplicación distribuida basada en el patrón **Modelo-Vista-Controlador (MVC)**, diseñada en **Java** para gestionar y recomendar colecciones de música física (CDs y vinilos) utilizando un **sistema multiagente** cooperativo.

Este proyecto conecta dos computadoras independientes (o procesos locales) mediante sockets TCP/IP con tramas serializadas en formato JSON (usando Gson).

---

## Trabajo Realizado

Hemos llevado el sistema base a un estándar de producción robusto, agregando tolerancia a fallos, inteligencia avanzada y una interfaz enriquecida, todo bajo la restricción estricta de que **ningún archivo fuente `.java` supere las 150 líneas de código** para garantizar modularidad y mantenibilidad.

### 1. Evolución del Sistema Multiagente (IA)
* **Agente Analizador (Perfil de Gustos):** Dibuja un perfil estadístico del usuario basado en sus frecuencias de consumo musical.
* **Agente Buscador (Búsqueda Fonética Avanzada):** Implementa coincidencia aproximada con la distancia Levenshtein y una normalización de clave fonética en español. Tolera errores comunes de ortografía.
* **Agente Recomendador (Aprendizaje por Refuerzo):**
  * Diseñado como **Singleton** para compartir el aprendizaje concurrentemente en ambientes multihilo del servidor.
  * Utiliza un algoritmo de **Bandido Multibrazo** con exploración Épsilon para aprender de los clics (Aceptar/Rechazar) del usuario, persistido en `data/recommendation_learning.json`.
  * Incorpora una heurística avanzada de **similitud de época (décadas)** y **afinidad de artistas** deducidos directamente de la colección.
  * Implementa **control de fatiga y penalización reciente** usando un historial de recomendaciones emitidas persistido en `data/recommendation_history.json`.

### 2. Robustez de Red, Protocolo TLS e Idempotencia
* **Caché de Idempotencia (`RegistroTransacciones`):** Middleware en el servidor que registra las últimas 300 transacciones exitosas. Si el cliente reintenta una petición, el servidor devuelve la respuesta de la caché.
* **Conexión Cifrada SSL/TLS (TlsClientSockets & TlsServerSockets):** Soporte opcional para cifrado seguro SSL/TLS configurable a través de la propiedad `server.tls.enabled` en `config.properties`.
* **Logs Transaccionales Estructurados (TransactionLog):** Registra cada transacción de socket de forma estructurada con `correlationId`, tipo de acción, dirección de origen, páginas de consulta y totales.
* **Validación de Datos en el Servidor:** Asegura la integridad de los datos antes de guardarlos.
* **Latido Periódico (Heartbeat):** Tarea asíncrona en segundo plano del cliente que envía un PING al servidor cada 15 segundos.
* **Reconexión Automática con Backoff Exponencial:** Si el canal se cae, realiza reintentos asíncronos espaciados con tiempos exponenciales para reconectar limpiamente sin congelar la GUI Swing.

### 3. Persistencia Relacional con SQLite (SqliteAlbumRepository)
* **Migración a SQLite:** Reemplazo de la base de datos JSON en disco por un repositorio SQLite relacional en `data/maria.sqlite` que optimiza búsquedas, añade protección contra duplicaciones y soporta paginación física de resultados.
* **Bootstrap inicial:** Carga los discos por defecto desde `data/coleccion.json` si la base de datos SQLite no ha sido inicializada previamente.

### 4. Interfaz Visual Modular (Swing)
* **PanelEstadisticasPerfil:** Un tablero lateral que muestra en tiempo real las estadísticas dinámicas del usuario.
* **PanelBusqueda & PanelRecomendaciones:** Vistas aisladas que reportan acciones al presentador de forma desacoplada y con soporte para búsquedas y listados paginados.

---

## Estructura de Paquetes Modulares (Sub-150 líneas)

El código fuente ha quedado estructurado de la siguiente forma, asegurando que todos los archivos se mantengan sumamente limpios y manejables:

```text
uaemex.ia.proyecto
├── compartido
│   ├── Disco.java                   # Modelo de datos común
│   ├── MensajeSocket.java           # Protocolo de petición (accion, datos, pagina, tamanoPagina)
│   ├── RespuestaSocket.java         # Protocolo de respuesta (paginada)
│   └── TlsConfig.java               # Configuración SSL/TLS compartida
├── herramientas
│   └── ImportadorColeccion.java     # Script de inicialización balanceada
├── cliente
│   ├── ClienteApp.java              # Punto de inicio cliente
│   ├── controller
│   │   ├── ClientController.java    # Control de socket y buffers
│   │   ├── HeartbeatCliente.java    # Latido periódico en hilo daemon
│   │   └── TlsClientSockets.java    # Creación de sockets seguros SSL/TLS
│   └── view
│       ├── VentanaPrincipal.java    # Frame principal de la GUI
│       ├── ClientPresenter.java     # Controlador y puente vista-red
│       ├── PanelEstadisticasPerfil.java # Dashboard de datos
│       ├── PanelBusqueda.java       # Formulario de búsqueda paginada
│       ├── PanelRecomendaciones.java# Feedback de recomendación
│       └── UIStyles.java            # Estilos, colores y fuentes
└── servidor
    ├── ServidorApp.java             # Punto de inicio servidor
    ├── controller
    │   ├── ServerController.java    # Listener de sockets y pool de hilos
    │   ├── ManejadorCliente.java    # Lector de JSON concurrente
    │   ├── AccionesCliente.java     # Ejecución de lógica de negocio
    │   ├── RegistroTransacciones.java# Control de idempotencia (LRU Cache)
    │   ├── ValidadorDiscoServidor.java# Filtros y validaciones
    │   ├── DiscoKeys.java           # Generador de claves
    │   ├── TlsServerSockets.java    # Sockets de servidor SSL/TLS seguros
    │   └── TransactionLog.java      # Logger estructurado de transacciones
    └── model
        ├── Database.java            # Fachada de persistencia delegada
        ├── DbConfig.java            # Configuración de SQLite JDBC
        ├── JsonCollectionBootstrap.java # Inicializador de SQLite desde JSON
        ├── SqliteAlbumRepository.java # Repositorio SQLite (per-user, paginación)
        ├── PerfilGustos.java        # Modelo de perfil analizado
        └── agentes
            ├── AgenteAnalizador.java# IA: Analizador de perfiles
            ├── AgenteBuscador.java  # IA: Buscador aproximado y fonético
            ├── AgenteRecomendador.java# IA: Recomendador Singleton (Multi-tenant)
            ├── PerfilAfinidad.java  # Vector de afinidades por década/artista
            ├── HistorialRecomendacion.java# Modelo de historial persistente
            └── RecomendadorStorage.java# Utilidades de E/S para IA
```

---

## Compilación y Pruebas

Para validar el sistema y correr las pruebas automatizadas (idempotencia, fonética, red PING/PONG, SQLite y el aprendizaje del recomendador), ejecuta en la raíz del proyecto:

```bash
# Compilar y ejecutar pruebas unitarias JUnit (10 pruebas integrales)
mvn clean test

# Regenerar la colección JSON inicial desde el catálogo si se requiere
java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.herramientas.ImportadorColeccion
```

---

*Desarrollado junto al equipo del proyecto.*
