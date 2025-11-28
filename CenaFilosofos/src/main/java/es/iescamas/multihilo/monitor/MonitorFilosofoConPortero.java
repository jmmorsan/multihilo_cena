package es.iescamas.multihilo.monitor;

import java.util.concurrent.Semaphore;

public final class MonitorFilosofoConPortero {

    private final MonitorFilosofos monitor;
    private final Semaphore portero;
    private final int maxSentados;

    // NUEVO: quién está esperando al portero
    private final boolean[] esperandoPortero;

    public MonitorFilosofoConPortero(final MonitorFilosofos monitor) {
        this(monitor, Math.max(1, monitor.getNumFilosofos() - 1));
    }

    public MonitorFilosofoConPortero(final MonitorFilosofos monitor,
                                     final int maxSentados) {
        if (monitor == null) {
            throw new IllegalArgumentException("El monitor de filósofos no puede ser null");
        }
        final int n = monitor.getNumFilosofos();
        if (maxSentados < 1 || maxSentados > n) {
            throw new IllegalArgumentException(
                    "maxSentados debe estar entre 1 y " + n + ", recibido: " + maxSentados);
        }
        this.monitor = monitor;
        this.maxSentados = maxSentados;
        this.portero = new Semaphore(maxSentados, true);
        this.esperandoPortero = new boolean[n];
    }

    public int getMaxSentados() {
        return maxSentados;
    }

    // ========= NUEVOS MÉTODOS DE CONSULTA PARA UTILIDAD =========

    public int getNumFilosofos() {
        return monitor.getNumFilosofos();
    }

    /** true si el filósofo está esperando turno en el portero (footman). */
    public synchronized boolean isEsperandoPortero(final int idFilosofo) {
        return esperandoPortero[idFilosofo];
    }

    // ============================================================

    public void sentarse(final int idFilosofo) throws InterruptedException {
        validarFilosofo(idFilosofo);

        // Marca que está esperando al portero
        setEsperandoPortero(idFilosofo, true);

        portero.acquire();   // bloquea si ya hay maxSentados sentados
        boolean exito = false;
        try {
            monitor.tomarTenedores(idFilosofo);
            exito = true;
        } finally {
            // Pase lo que pase, ya no está esperando al portero
            setEsperandoPortero(idFilosofo, false);

            if (!exito) {
                portero.release();
            }
        }
    }

    public void levantarse(final int idFilosofo) {
        validarFilosofo(idFilosofo);
        monitor.dejarTenedores(idFilosofo);
        portero.release();
    }

    private synchronized void setEsperandoPortero(final int idFilosofo, final boolean valor) {
        esperandoPortero[idFilosofo] = valor;
    }

    private void validarFilosofo(final int idFilosofo) {
        final int n = monitor.getNumFilosofos();
        if (idFilosofo < 0 || idFilosofo >= n) {
            throw new IllegalArgumentException(
                    "Índice de filósofo fuera de rango: " + idFilosofo
                    + " (debe estar entre 0 y " + (n - 1) + ")");
        }
    }
}
