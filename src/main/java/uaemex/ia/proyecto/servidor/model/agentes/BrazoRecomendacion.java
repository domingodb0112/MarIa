package uaemex.ia.proyecto.servidor.model.agentes;

/**
 * Brazo del bandido multi-brazo. Cada genero mantiene recompensas promedio con decaimiento.
 */
public class BrazoRecomendacion {
    private int intentos;
    private double recompensaTotal;
    private double pesoTotal;
    private static final double FACTOR_DECAIMIENTO = 0.95;

    public BrazoRecomendacion() {
    }

    public void registrar(double recompensa) {
        intentos++;
        recompensaTotal = (recompensaTotal * FACTOR_DECAIMIENTO) + recompensa;
        pesoTotal = (pesoTotal * FACTOR_DECAIMIENTO) + 1.0;
    }

    public double valorEsperado() {
        if (pesoTotal > 0.0) {
            return recompensaTotal / pesoTotal;
        }
        return intentos == 0 ? 0.0 : recompensaTotal / intentos;
    }
}
