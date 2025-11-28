package es.iescamas.multihilo.monitor.hilo;
import java.util.Random;

import es.iescamas.multihilo.monitor.MonitorFilosofos;

/**
 * Representa a un filósofo que piensa y come de forma repetida.
 * Cada filósofo es un hilo que coordina su acceso a los tenedores
 * a través del MonitorFilosofos.
 */
public final class Filosofo implements Runnable {

    private final int id;
    private final MonitorFilosofos monitor;
    private final Random random = new Random();

    public Filosofo(final int id, final MonitorFilosofos monitor) {
        this.id = id;
        this.monitor = monitor;
    }

    private void pensar() throws InterruptedException {
        System.out.println("Filósofo " + id + " 🧠 PENSANDO...");
        // Simulamos un tiempo variable de pensamiento (TIMED_WAITING)
        Thread.sleep(random.nextInt(2000) + 500L);
    }

    private void comer() throws InterruptedException {
        System.out.println("Filósofo " + id + " COMIENDO 🍝");
        Thread.sleep(random.nextInt(2000) + 500L);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                pensar();
                System.out.println("Filósofo " + id + " 😋 tiene HAMBRE.");
                // Puede quedar en estado WAITING dentro del monitor
                monitor.tomarTenedores(id);
                comer();
                monitor.dejarTenedores(id);
            }
        } catch (InterruptedException e) {
            // Restablecemos el estado de interrupción y salimos del bucle
            Thread.currentThread().interrupt();
            System.out.println("Filósofo " + getId() + " interrumpido. Termina su ejecución.");
        }
    }

	public Integer getId() {
		return id;
	}
}
