# Planeación del Proyecto: Sistema de Recomendación de Música Física con Agentes de IA y Arquitectura MVC

Este documento detalla la planeación y arquitectura para el proyecto de Inteligencia Artificial en **Java**. El sistema conecta dos computadoras mediante sockets, implementa una arquitectura **Modelo-Vista-Controlador (MVC)** distribuida y utiliza un sistema multiagente (3 agentes) para gestionar, buscar y recomendar discos físicos (CDs y vinilos).

---

## 1. Arquitectura General y Patrón MVC

Para mantener el código ordenado y escalable, implementamos el patrón **MVC (Model-View-Controller)** tanto en el lado del cliente como en el del servidor. La separación de responsabilidades se distribuye de la siguiente manera:

```mermaid
graph TD
    subgraph Cliente ["Cliente (PC 1 - Interfaz)"]
        View["Vista (GUI Swing/JavaFX o Consola)"] <-->|Interacciones / Actualizaciones| ClientController["Controlador del Cliente"]
        ClientController <-->|Modifica/Consulta| ClientModel["Modelos Locales (Datos temporales / Estado)"]
    end

    subgraph Red ["Red"]
        ClientController <-->|Sockets TCP/IP (JSON)| ServerController["Controlador del Servidor"]
    end

    subgraph Servidor ["Servidor (PC 2 - Motor de IA)"]
        ServerController <-->|Orquesta y Consulta| ServerModel["Modelos del Servidor (Lógica de Negocio y BD)"]
        
        subgraph ServerModel ["Modelos del Servidor"]
            DB[("Almacenamiento (JSON/DB)")]
            
            subgraph SistemaMultiagente ["Sistema Multiagente (IA)"]
                A1["Agente 1: Analizador de Colección"]
                A2["Agente 2: Buscador y Consultor"]
                A3["Agente 3: Recomendador de Música"]
            end
        end
    end

    A1 <--> DB
    A2 <--> DB
    A3 <--> A1
    A3 <--> A2
```

---

## 2. Componentes del Patrón MVC

### Lado del Cliente (PC 1)
*   **Vista (View):** 
    *   Interfaz gráfica (Swing o JavaFX) o de consola donde el usuario registra sus discos, realiza búsquedas y solicita recomendaciones.
    *   No tiene lógica de negocio ni maneja sockets directamente; solo reporta eventos al controlador.
*   **Controlador (ClientController):**
    *   Escucha los eventos de la Vista (ej. clic en "Buscar álbum" o "Registrar Vinilo").
    *   Construye las tramas de datos JSON y las envía a través del socket al servidor.
    *   Recibe las respuestas del servidor y actualiza la Vista.
*   **Modelo (ClientModel):**
    *   Representa los datos locales en memoria (lista actual de álbumes mostrados, estado de la conexión).

### Lado del Servidor (PC 2)
*   **Controlador (ServerController):**
    *   Contiene el hilo principal del Socket (`ServerSocketListener`) que escucha conexiones entrantes.
    *   Recibe las tramas JSON del cliente, determina la acción solicitada (rutas/endpoints del socket) y delega la ejecución al **Modelo del Servidor** (Agentes).
*   **Modelo (ServerModel / Agentes):**
    *   Contiene la lógica de negocio y las capacidades cognitivas del sistema. Los **3 Agentes de IA** forman parte del modelo del servidor:
        *   **Agente 1 (Analizador):** Modela el perfil del usuario analizando los géneros y frecuencias de los discos guardados.
        *   **Agente 2 (Buscador):** Modela la lógica de consulta y coincidencia difusa en la base de datos de música.
        *   **Agente 3 (Recomendador):** Aplica la lógica de filtrado colaborativo o similitud de coseno para generar nuevas recomendaciones.
    *   Administra la lectura/escritura de la base de datos o almacenamiento físico (archivos JSON/texto).

---

## 3. Protocolo de Comunicación (Mensajería JSON)

La comunicación entre el **Controlador del Cliente** y el **Controlador del Servidor** se realiza serializando clases Java a JSON:

```json
{
  "transaccionId": "UUID-12345",
  "accion": "REGISTRAR_DISCO | BUSCAR_ALBUM | OBTENER_RECOMENDACIONES",
  "datos": {
    "titulo": "The Dark Side of the Moon",
    "artista": "Pink Floyd",
    "anio": 1973,
    "genero": "Progressive Rock",
    "formato": "Vinilo"
  }
}
```

---

## 4. Estructura de Paquetes en Java (Propuesta)

```text
uaemex.ia.proyecto
│
├── cliente
│   ├── view
│   │   └── VentanaPrincipal.java (Vista)
│   ├── controller
│   │   └── ClientController.java (Controlador Cliente / Socket)
│   └── model
│       └── DiscoLocal.java (Modelo Cliente)
│
├── servidor
│   ├── controller
│   │   └── ServerController.java (Controlador Servidor / SocketListener)
│   └── model
│       ├── Disco.java (Modelo Datos)
│       ├── PerfilGustos.java (Modelo Datos)
│       ├── Database.java (Gestor de datos/Persistencia)
│       └── agentes
│           ├── AgenteAnalizador.java (Agente 1)
│           ├── AgenteBuscador.java (Agente 2)
│           └── AgenteRecomendador.java (Agente 3)
│
└── compartido (Clases reutilizables por ambos proyectos)
    └── MensajeSocket.java
```

---

## 5. Plan de Desarrollo por Etapas (Roadmap Incremental)

Para evitar construir toda la lógica de golpe, se propone un enfoque incremental de 6 etapas ordenadas de menor a mayor complejidad tecnológica.

### Etapa 1: Estructuras Base y Comunicación Socket Simple
*   **Objetivo:** Establecer la infraestructura de red básica.
*   **Tareas:**
    1.  Crear las clases de datos básicas compartidas (`Disco.java`, `MensajeSocket.java`).
    2.  Implementar un servidor de sockets básico que escuche en un puerto y un cliente básico que se conecte y envíe un mensaje de texto plano.
    3.  Confirmar que la comunicación bidireccional básica funciona.

### Etapa 2: Serialización JSON y Controlador del Servidor
*   **Objetivo:** Lograr el envío e interpretación de datos estructurados.
*   **Tareas:**
    1.  Integrar una librería JSON (como Gson o Jackson) en ambos lados.
    2.  Modificar la comunicación por sockets para enviar y recibir objetos `MensajeSocket` en formato JSON.
    3.  Estructurar el `ServerController` en el servidor para "enrutar" peticiones basándose en el campo `accion` del JSON recibido.

### Etapa 3: Persistencia y Flujo de Registro (Modelo y Vista Básicos)
*   **Objetivo:** Permitir al usuario registrar discos reales y guardarlos en el servidor.
*   **Tareas:**
    1.  Crear una base de datos simulada en formato JSON/texto en el servidor (`Database.java`).
    2.  Implementar una vista preliminar en el cliente (consola o interfaz visual sencilla) para ingresar datos de un disco.
    3.  Conectar el flujo completo: el usuario introduce un vinilo/CD -> el cliente envía la petición -> el servidor la procesa, la guarda en disco y retorna un mensaje de éxito.

### Etapa 4: Implementación del Agente 1 (Analizador) y Agente 2 (Buscador)
*   **Objetivo:** Agregar las dos primeras capacidades de inteligencia artificial y lógica en el servidor.
*   **Tareas:**
    1.  Desarrollar el `AgenteAnalizador`: cada vez que se registre un disco, recalcula el perfil de gustos (porcentajes de géneros favoritos).
    2.  Desarrollar el `AgenteBuscador`: buscar discos utilizando coincidencia aproximada (algoritmo Levenshtein o coincidencia difusa) para tolerar errores ortográficos en la consulta.
    3.  Añadir el motor de búsqueda en la interfaz del cliente.
    4.  Poblar `data/coleccion.json` con una base amplia de prueba usando `ImportadorColeccion`.

### Etapa 5: Implementación del Agente 3 (Recomendador)
*   **Objetivo:** Culminar la lógica de IA con el motor de recomendaciones personalizado.
*   **Tareas:**
    1.  Desarrollar el `AgenteRecomendador`: utiliza el perfil de gustos (del Agente 1) y los registros de búsquedas (del Agente 2) para evaluar y ordenar las recomendaciones utilizando una heurística de similitud.
    2.  Diseñar la sección de recomendaciones en la vista del cliente.
    3.  Probar localmente todo el flujo de agentes de forma cooperativa.

### Etapa 6: Despliegue en Red Real y Pulido
*   **Objetivo:** Validar la conexión entre dos computadoras físicas distintas en la misma red local.
*   **Tareas:**
    1.  Configurar las direcciones IP correctas del servidor en el cliente (considerar usar hostname o archivo de configuración en lugar de IP fija para mayor robustez).
    2.  Asegurar excepciones para fallos de red en el cliente (reconexiones o alertas si el servidor se apaga).
    3.  Pulir la interfaz gráfica para una presentación estética premium.

---

## 6. Base de Datos Musical para Búsqueda y Recomendación

El servidor persiste la colección en [data/coleccion.json](data/coleccion.json). Para que el sistema de búsqueda y los agentes de recomendación tengan suficiente material de prueba, se agregó la herramienta [ImportadorColeccion.java](src/main/java/uaemex/ia/proyecto/herramientas/ImportadorColeccion.java).

La base generada actualmente contiene **500 álbumes icónicos en español**, balanceados para cubrir cuatro familias musicales usadas por el proyecto:

*   **125 álbumes de Rock en Español.**
*   **125 álbumes de Pop Latino.**
*   **125 álbumes de Salsa.**
*   **125 álbumes de Cumbia.**

Cada registro se serializa con la estructura compartida `Disco`: `titulo`, `artista`, `anio`, `genero` y `formato`. El importador asigna `Vinilo` a discos anteriores a 1990 y `CD` a discos de 1990 en adelante, lo que mantiene coherencia histórica sin requerir capturar manualmente el formato para cada una de las 500 entradas.

### 6.1 Flujo del importador

`ImportadorColeccion` genera la colección directamente desde un catálogo curado interno. Esto evita depender de descargas externas o de una importación previa de bases musicales completas.

El catálogo interno se mantiene como líneas delimitadas por tabuladores dentro del código para que sea fácil auditar y ampliar entradas:

```text
titulo<TAB>artista<TAB>anio<TAB>genero
```

Al escribir `data/coleccion.json`, el importador intercala los géneros para evitar que los primeros registros pertenezcan a una sola categoría. Con el límite por defecto de `500`, el resultado queda balanceado en partes iguales.

### 6.2 Impacto en los agentes

*   **Agente Buscador:** dispone de suficientes títulos, artistas y géneros para validar búsquedas exactas y aproximadas.
*   **Agente Analizador:** puede construir perfiles de gustos con distribución más representativa.
*   **Agente Recomendador:** tiene una colección persistida más rica para producir sugerencias y evitar resultados repetitivos.

---

## 7. Observaciones y Mejoras a Considerar

### 7.1 Paquete `compartido` en proyectos separados
Si el cliente y el servidor se estructuran como proyectos Maven/Gradle independientes, el paquete `compartido` (que contiene `MensajeSocket.java` y otras clases reutilizables) debe empaquetarse como un módulo o JAR independiente e incluirse como dependencia en ambos proyectos. Esto evita duplicar clases y mantiene la consistencia del protocolo.

### 7.2 Agente 3 sin acceso directo a la BD
En el diagrama de arquitectura, el `AgenteRecomendador` solo se comunica con el Agente 1 y el Agente 2, sin acceso directo a la BD. Esto es correcto si únicamente consume perfiles ya calculados, pero si en un futuro se requiere guardar historial de recomendaciones o métricas de uso, se deberá añadir una conexión directa del Agente 3 a la capa de persistencia.

### 7.3 Protocolo de respuesta del servidor (estructura JSON)
El protocolo actual solo define la estructura de las **peticiones** del cliente. Se debe definir también la estructura de las **respuestas** del servidor para cubrir casos de éxito y error. Ejemplo propuesto:

```json
{
  "transaccionId": "UUID-12345",
  "status": "OK | ERROR",
  "mensaje": "Disco registrado correctamente.",
  "datos": { }
}
```

### 7.4 Manejo de concurrencia en el servidor
El `ServerController` debe ser capaz de atender múltiples clientes simultáneamente. Se recomienda usar un `ExecutorService` (pool de hilos) para asignar un hilo por conexión entrante, evitando que una solicitud bloquee a las demás. Esta tarea debería incluirse en la **Etapa 1** o **Etapa 2** del roadmap.

```java
// Ejemplo de estructura en ServerController
ExecutorService pool = Executors.newCachedThreadPool();
while (true) {
    Socket clienteSocket = serverSocket.accept();
    pool.execute(new ManejadorCliente(clienteSocket));
}
```
