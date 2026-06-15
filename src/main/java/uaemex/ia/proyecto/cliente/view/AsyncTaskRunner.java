package uaemex.ia.proyecto.cliente.view;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

final class AsyncTaskRunner {

    private AsyncTaskRunner() {
    }

    static <T> void run(Callable<T> task, Consumer<T> onSuccess,
                        Consumer<Exception> onError, Runnable onFinally) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception {
                return task.call();
            }

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
