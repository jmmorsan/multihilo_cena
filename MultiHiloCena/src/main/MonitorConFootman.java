package main;

import java.util.concurrent.Semaphore;

/**
 * Envuelve al MonitorFilosofos añadiendo un "portero" (footman)
 * que limita el número de filósofos que se sientan simultáneamente.
 */
public final class MonitorConFootman {

    private static final int MAX_SENTADOS = 4;

    private final MonitorFilosofos monitor;
    private final Semaphore footman = new Semaphore(MAX_SENTADOS, true); // fairness=true

    public MonitorConFootman(final MonitorFilosofos monitor) {
        this.monitor = monitor;
    }

    public void sentarse(final int i) throws InterruptedException {
        // El portero limita el número de filósofos concurrentemente sentados
        footman.acquire();   // espera si ya hay MAX_SENTADOS filósofos sentados
        monitor.tomarTenedores(i);
    }

    public void levantarse(final int i) {
        monitor.dejarTenedores(i);
        footman.release();   // libera un sitio en la mesa
    }
}