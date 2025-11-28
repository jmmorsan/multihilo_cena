package es.iescamas.multihilo.monitor.hilo;

import java.util.Random;
import es.iescamas.multihilo.monitor.MonitorFilosofoConPortero;

/**
 * Filósofo que utiliza la solución del Portero (Footman) para evitar
 * interbloqueos en el problema de la cena de los filósofos.
 */
public final class FilosofoConPortero implements Runnable {

    private final int id;
    private final MonitorFilosofoConPortero monitor;
    private final Random random = new Random();

    public FilosofoConPortero(final int id, final MonitorFilosofoConPortero monitor) {
        this.id = id;
        this.monitor = monitor;
    }

    /**
     * Simula que el filósofo piensa un tiempo aleatorio.
     */
    private void pensar() throws InterruptedException {
        System.out.println("[FilosofoPortero " + id + "] 🧠 PENSANDO...");
        Thread.sleep(random.nextInt(2000) + 500L);
    }

    /**
     * Simula que el filósofo come un tiempo aleatorio.
     */
    private void comer() throws InterruptedException {
        System.out.println("[FilosofoPortero " + id + "] 🍝 COMIENDO");
        Thread.sleep(random.nextInt(2000) + 500L);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                // 1. Pensar
                pensar();

                // 2. Intentar sentarse: portero controla máximo de filósofos
                System.out.println("[FilosofoPortero " + id + "] 😋 tiene HAMBRE.");
                monitor.sentarse(id); // bloquea si el portero está completo

                // 3. Tomar los tenedores y comer
                comer();

                // 4. Levantarse: libera portero y tenedores
                monitor.levantarse(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[FilosofoPortero " + id + "] interrumpido. Termina ejecución.");
        }
    }

    public int getId() {
        return id;
    }
}
