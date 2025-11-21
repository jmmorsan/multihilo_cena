package main;

public final class FilosofoCiclico implements Runnable {

    private final int id;
    private final MonitorFilosofos monitor;

    public FilosofoCiclico(final int id, final MonitorFilosofos monitor) {
        this.id = id;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Pensar exactamente 1 segundo
                Thread.sleep(1000L);

                monitor.tomarTenedores(id);

                // Comer exactamente 2 segundos
                Thread.sleep(2000L);

                monitor.dejarTenedores(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}