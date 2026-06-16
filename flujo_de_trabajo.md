# Flujo de Trabajo y Despliegue en Red Real

Este documento describe detalladamente el procedimiento para desplegar y probar el **Sistema de Recomendación de Música Física con Agentes de IA y Arquitectura MVC** utilizando dos computadoras físicas independientes conectadas en red.

---

## 1. Concepto de Arquitectura de Red

El sistema utiliza sockets sobre el protocolo **TCP/IP** para la comunicación bidireccional entre las dos máquinas:

```
+--------------------------+                  +--------------------------+
|    PC 2: Cliente (GUI)   |                  |   PC 1: Servidor (IA)    |
|                          |                  |                          |
|   - ClienteApp           |   JSON (TCP)     |   - ServidorApp          |
|   - ClientController     |------------=====>|   - ServerController     |
|   - VentanaPrincipal     |                  |   - ManejadorCliente     |
|   - config.properties    |                  |   - Agentes & Base Datos |
+--------------------------+                  +--------------------------+
```

* **PC 1 (Servidor):** Corre el hilo de escucha (`ServerSocket`) en el puerto especificado. Acepta y procesa múltiples clientes simultáneamente mediante un pool de hilos dinámico (`ExecutorService`).
* **PC 2 (Cliente):** Consume la IP de red de la PC 1 y se conecta usando un canal de sockets. Dispone de mecanismos de control de errores y reconexión automática en caso de pérdida de señal.

---

## 2. Paso a Paso para la Configuración

Para poner en marcha el sistema con dos computadoras, siga los siguientes pasos:

### Paso 1: Conexión de Red Local (LAN)
Asegúrese de que ambas computadoras (Servidor y Cliente) estén conectadas al mismo router o conmutador de red, ya sea mediante cables Ethernet o a través del mismo SSID de Wi-Fi. Ambas deben poder enviarse paquetes de datos entre sí (hacer `ping`).

### Paso 2: Obtener la Dirección IP de la PC 1 (Servidor)
Necesita saber la dirección de red local de la computadora que actuará como servidor.
* **En Windows:**
  1. Abra la consola de comandos (`cmd`).
  2. Ejecute el comando: `ipconfig`
  3. Localice el adaptador de red activo y anote la **Dirección IPv4** (suele tener el formato `192.168.1.X` o `10.0.0.X`).
* **En Linux / macOS:**
  1. Abra una terminal.
  2. Ejecute: `ip a` o `ifconfig`
  3. Identifique la IP asignada a su tarjeta de red activa.

### Paso 3: Arrancar el Servidor en la PC 1
1. Sitúese en la máquina Servidor.
2. Verifique que la colección base exista en [data/coleccion.json](data/coleccion.json). Esta base se genera con 500 álbumes en español para alimentar las búsquedas y recomendaciones.
3. Si necesita regenerarla, compile y ejecute el importador:
   ```bash
   javac -cp target/classes:lib/gson-2.10.1.jar -d target/classes src/main/java/uaemex/ia/proyecto/herramientas/ImportadorColeccion.java
   java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.herramientas.ImportadorColeccion
   ```
4. Confirme que el archivo contiene 500 registros:
   ```bash
   rg -c '"titulo"' data/coleccion.json
   ```
5. Inicie la aplicación ejecutando la clase [ServidorApp.java](src/main/java/uaemex/ia/proyecto/servidor/ServidorApp.java).
6. Verá en la consola una confirmación de inicio:
   ```text
   [Servidor] Iniciando en puerto 5000...
   [Servidor] Listo. Esperando conexiones...
   ```

### Paso 4: Configurar el Cliente en la PC 2
1. Sitúese en la máquina Cliente.
2. Localice el archivo [config.properties](config.properties) en la raíz del proyecto.
3. Edite la línea `server.ip` colocando la dirección IP de la PC 1 que obtuvo en el Paso 2:
   ```properties
   # Configuracion para despliegue en red real
   server.ip=192.168.1.45
   server.port=5000
   ```
   *(Reemplace `192.168.1.45` por la dirección IPv4 real de su servidor).*

### Paso 5: Iniciar el Cliente en la PC 2
1. Ejecute la clase [ClienteApp.java](src/main/java/uaemex/ia/proyecto/cliente/ClienteApp.java).
2. Se abrirá la interfaz gráfica pulida en Swing.
3. Verifique la barra de estado en la parte inferior: deberá indicar **"Conectado a [IP]:5000"**.
4. Ahora puede proceder a registrar discos, realizar búsquedas difusas u obtener recomendaciones de música en tiempo real.

---

## 3. Base de Datos de 500 Álbumes en Español

La colección persistida en [data/coleccion.json](data/coleccion.json) se genera con [ImportadorColeccion.java](src/main/java/uaemex/ia/proyecto/herramientas/ImportadorColeccion.java). El objetivo es que el servidor tenga una base suficientemente amplia para demostrar búsquedas por título, artista o género, además de recomendaciones menos repetitivas.

La base actual está balanceada así:

*   125 álbumes de `Rock en Espanol`.
*   125 álbumes de `Pop Latino`.
*   125 álbumes de `Salsa`.
*   125 álbumes de `Cumbia`.

El importador usa un catálogo curado interno y no requiere archivos externos. Si se cambia el límite por línea de comandos, la herramienta conserva el orden balanceado por género hasta llegar al número solicitado.

Comandos útiles de verificación:

```bash
rg -c '"titulo"' data/coleccion.json
rg -c '"genero": "Rock en Espanol"' data/coleccion.json
rg -c '"genero": "Pop Latino"' data/coleccion.json
rg -c '"genero": "Salsa"' data/coleccion.json
rg -c '"genero": "Cumbia"' data/coleccion.json
```

Para el estado esperado, estos comandos deben devolver `500`, `125`, `125`, `125` y `125`, respectivamente.

---

## 4. Diagnóstico y Solución de Problemas

Si al intentar conectar el cliente de la PC 2 se muestra un mensaje de error o el estado permanece en **"Sin conexión"**, valide los siguientes puntos:

### A. Bloqueo por Cortafuegos (Firewall)
Es la causa más frecuente de fallos de conexión en redes locales.
* **Problema:** El cortafuegos de la PC 1 (Servidor) detecta la conexión entrante de la PC 2 en el puerto 5000 como una amenaza y la bloquea silenciosamente.
* **Solución:** 
  * Configure una regla de entrada en el Firewall de la PC 1 para permitir conexiones TCP en el puerto **5000**.
  * Alternativamente (sólo para pruebas rápidas), desactive temporalmente el Firewall de la red privada en la PC 1.

### B. Direcciones IP Incorrectas o Fuera de Rango
* **Problema:** Las computadoras no están en la misma subred o se ha configurado una IP incorrecta.
* **Solución:**
  * En la PC 2, abra una terminal y ejecute `ping [IP_DEL_SERVIDOR]`. Si no hay respuesta, valide que ambas computadoras tengan conexión a internet/red y que estén en la misma red local.
  * Valide que no haya errores de digitación en el archivo `config.properties`.

### C. Caída del Servidor o Puerto Ocupado
* **Problema:** El servidor no se está ejecutando o el puerto 5000 está siendo utilizado por otra aplicación de fondo.
* **Solución:**
  * Revise los logs de la PC 1 para confirmar que no ocurrió un fallo al iniciar.
  * Si el puerto 5000 está ocupado, edite tanto la configuración del servidor como el archivo `config.properties` del cliente para utilizar otro puerto (por ejemplo, el `5001`).
