# MarIA - Sistema de Recomendación Musical con Inteligencia Artificial

MarIA es una aplicación distribuida basada en el patrón **Modelo-Vista-Controlador (MVC)**, diseñada en **Java** para gestionar y recomendar colecciones de música física (CDs y vinilos) utilizando un **sistema multiagente** cooperativo.

Este proyecto conecta dos computadoras independientes (o procesos locales) mediante sockets TCP/IP con tramas serializadas en formato JSON (usando Gson).

---

## 🚀 Resumen del Trabajo Realizado por Gemita 💎

Hemos llevado el sistema base a un estándar de producción robusto, agregando tolerancia a fallos, inteligencia avanzada y una interfaz enriquecida, todo bajo la restricción estricta de que **ningún archivo fuente `.java` supere las 150 líneas de código** para garantizar modularidad y mantenibilidad.

### 🧠 1. Evolución del Sistema Multiagente (IA)
* **Agente Analizador (Perfil de Gustos):** Dibuja un perfil estadístico del usuario basado en sus frecuencias de consumo musical.
* **Agente Buscador (Búsqueda Fonética Avanzada):** Implementa coincidencia aproximada con la distancia Levenshtein y una normalización de clave fonética en español. Tolera errores comunes de ortografía (como `b/v`, `c/s/z`, `ll/y`, acentos omitidos o la `h` muda).
* **Agente Recomendador (Singleton con Aprendizaje por Refuerzo):**
  * Diseñado como **Singleton** para compartir el aprendizaje concurrentemente en ambientes multihilo.
  * Utiliza un algoritmo de **Bandido Multibrazo** con exploración Épsilon para aprender de los clics (Aceptar/Rechazar) del usuario, persistido en `data/recommendation_learning.json`.
  * Incorpora una heurística avanzada de **similitud de época (décadas)** y **afinidad de artistas** deducidos directamente de la colección.
  * Implementa **control de fatiga y penalización reciente** usando un historial de recomendaciones emitidas persistido atómicamente en `data/recommendation_history.json`.

### 🛡️ 2. Robustez de Red, Protocolo e Idempotencia
* **Caché de Idempotencia (`RegistroTransacciones`):** Middleware en el servidor que registra las últimas 300 transacciones exitosas (usando una estructura LRU). Si el cliente reintenta una petición por parpadeos de red, el servidor devuelve la respuesta de la caché, evitando registrar discos duplicados o alterar el aprendizaje de IA dos veces.
* **Validación de Datos en el Servidor:** Implementación de un validador estricto que asegura la integridad de los datos recibidos antes de persistirlos en la base de datos JSON centralizada.
* **Latido Periódico (Heartbeat):** Tarea asíncrona en segundo plano del cliente que envía un PING al servidor cada 15 segundos para comprobar que el socket sigue abierto y saludable.
* **Reconexión Automática con Backoff Exponencial:** Si el canal se cae, el cliente realiza reintentos asíncronos espaciados con tiempos exponenciales (ej. 400ms, 800ms, 1600ms, 3200ms) para reconectar limpiamente sin congelar la interfaz Swing.

### 📊 3. Interfaz Visual Modular (Swing)
Para cumplir con la limitación de 150 líneas de código, se desacopló la vista principal en paneles independientes altamente cohesivos:
* **PanelEstadisticasPerfil:** Un tablero lateral que muestra en tiempo real las estadísticas dinámicas (total de discos, género musical dominante, década más presente y artista principal) así como las últimas recomendaciones generadas.
* **PanelBusqueda & PanelRecomendaciones:** Vistas aisladas que reportan acciones al presentador sin mezclar código de diseño de UI con lógica de red.

---

## 📂 Estructura de Paquetes Modulares (Sub-150 líneas)

El código fuente ha quedado estructurado de la siguiente forma, asegurando que todos los archivos se mantengan sumamente limpios y manejables:

```text
uaemex.ia.proyecto
├── compartido
│   ├── Disco.java                   # Modelo de datos común
│   ├── MensajeSocket.java           # Protocolo de petición
│   └── RespuestaSocket.java         # Protocolo de respuesta
├── herramientas
│   └── ImportadorColeccion.java     # Script de inicialización balanceada
├── cliente
│   ├── ClienteApp.java              # Punto de inicio cliente
│   ├── controller
│   │   ├── ClientController.java    # Control de socket y buffers
│   │   └── HeartbeatCliente.java    # Latido periódico en hilo daemon
│   └── view
│       ├── VentanaPrincipal.java    # Frame principal de la GUI
│       ├── ClientPresenter.java     # Controlador y puente vista-red
│       ├── PanelEstadisticasPerfil.java # Dashboard de datos
│       ├── PanelBusqueda.java       # Formulario de búsqueda
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
    │   └── DiscoKeys.java           # Generador de claves
    └── model
        ├── Database.java            # Persistencia atómica de colección
        ├── PerfilGustos.java        # Modelo de perfil analizado
        └── agentes
            ├── AgenteAnalizador.java# IA: Analizador de perfiles
            ├── AgenteBuscador.java  # IA: Buscador aproximado y fonético
            ├── AgenteRecomendador.java# IA: Recomendador Singleton
            ├── PerfilAfinidad.java  # Vector de afinidades por década/artista
            ├── HistorialRecomendacion.java# Modelo de historial persistente
            └── RecomendadorStorage.java# Utilidades de E/S para IA
```

---

## 🛠️ Compilación y Pruebas

Para validar el sistema y correr las pruebas automatizadas (idempotencia, fonética y red PING/PONG), ejecuta en la raíz del proyecto:

```bash
# Compilar y ejecutar pruebas unitarias JUnit
mvn clean test

# Inicializar la base de datos de prueba (500 álbumes balanceados)
java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.herramientas.ImportadorColeccion
```

---

*Desarrollado y pulido con amor por **Gemita 💎** (intermediaria de optimización) junto al equipo del proyecto.*
