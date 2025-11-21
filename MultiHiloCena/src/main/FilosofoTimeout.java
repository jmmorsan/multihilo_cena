package main;

import java.util.Random;

public final class FilosofoTimeout implements Runnable {

    private final int id;
    private final MonitorFilosofos monitor;
    private final Random random = new Random();

    public FilosofoTimeout(final int id, final MonitorFilosofos monitor) {
        this.id = id;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Pensar
                Thread.sleep(1000L);

                long inicio = System.currentTimeMillis();
                long timeout = 2000L + random.nextInt(2000);

                monitor.tomarTenedores(id);

                // Si no llega a COMER en cierto tiempo, suelta los tenedores
                while (monitor.getEstado(id) != MonitorFilosofos.COMIENDO) {
                    if (System.currentTimeMillis() - inicio > timeout) {
                        System.out.println("Filósofo " + id
                                + " se rinde y vuelve a pensar (timeout).");
                        monitor.dejarTenedores(id);
                        break;
                    }
                    Thread.sleep(50L);
                }

                if (monitor.getEstado(id) == MonitorFilosofos.COMIENDO) {
                    // Comer normalmente
                    Thread.sleep(1000L);
                    monitor.dejarTenedores(id);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}