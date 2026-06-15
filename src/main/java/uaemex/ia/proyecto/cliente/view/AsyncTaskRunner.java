package uaemex.ia.proyecto.cliente.view;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Utilidad interna para ejecutar tareas lentas fuera del hilo de interfaz Swing.
 */
final class AsyncTaskRunner {

    /**
     * Evita instanciar la clase porque solo agrupa metodos estaticos.
     */
    private AsyncTaskRunner() {
    }

    /**
     * Ejecuta una tarea en segundo plano y notifica el resultado en el Event Dispatch Thread.
     *
     * @param task trabajo que puede bloquear, por ejemplo una llamada de red.
     * @param onSuccess consumidor invocado si la tarea termina correctamente.
     * @param onError consumidor invocado cuando la tarea falla.
     * @param onFinally accion que siempre se ejecuta al finalizar.
     * @param <T> tipo de dato devuelto por la tarea.
     */
    static <T> void run(Callable<T> task, Consumer<T> onSuccess,
                        Consumer<Exception> onError, Runnable onFinally) {
        new SwingWorker<T, Void>() {
            /** Ejecuta el trabajo lejos de la interfaz para no congelar la ventana. */
            @Override protected T doInBackground() throws Exception {
                return task.call();
            }

            /** Recupera el resultado y actualiza la vista desde el hilo seguro de Swing. */
            @Override protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception ex) {
                    onError.accept(ex);
                } finally {
                    onFinally.run();
                }
            }
        }.execute();
    }
}
