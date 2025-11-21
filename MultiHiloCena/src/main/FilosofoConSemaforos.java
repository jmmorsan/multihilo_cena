package main;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Implementación de un filósofo usando únicamente semáforos:
 * - Un array de Semaphore para los tenedores (uno por posición).
 * - Un Semaphore adicional (portero) con N-1 permisos para evitar deadlock.
 */
public final class FilosofoConSemaforos implements Runnable {

    private final int id;
    private final Semaphore[] tenedores;
    private final Semaphore portero;
    private final Random random = new Random();

    public FilosofoConSemaforos(final int id,
                                final Semaphore[] tenedores,
                                final Semaphore portero) {
        this.id = id;
        this.tenedores = tenedores;
        this.portero = portero;
    }

    private int izquierda() {
        return id;
    }

    private int derecha() {
        return (id + 1) % tenedores.length;
    }

    private void pensar() throws InterruptedException {
        System.out.println("Filósofo " + id + " PENSANDO (semáforos)...");
        Thread.sleep(random.nextInt(2000) + 500L);
    }

    private void comer() throws InterruptedException {
        System.out.println("Filósofo " + id + " COMIENDO (semáforos) 🍝");
        Thread.sleep(random.nextInt(2000) + 500L);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                pensar();

                // El portero limita el número de filósofos que pueden competir
                // por los tenedores simultáneamente. Tiene N-1 permisos.
                portero.acquire();
                try {
                    // Adquirimos primero el tenedor izquierdo y luego el derecho.
                    // Cada tenedor es un semáforo binario (1 permiso).
                    tenedores[izquierda()].acquire();
                    tenedores[derecha()].acquire();
                    try {
                        comer();
                    } finally {
                        // Liberamos los tenedores en orden inverso
                        tenedores[derecha()].release();
                        tenedores[izquierda()].release();
                    }
                } finally {
                    // Dejamos libre un sitio en la mesa
                    portero.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Filósofo " + id + " interrumpido (semáforos).");
        }
    }
}