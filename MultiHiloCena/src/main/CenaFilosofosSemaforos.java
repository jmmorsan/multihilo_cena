package main;
	
import java.util.concurrent.Semaphore;

/**
 * Variante de la cena de los filósofos implementada con semáforos.
 */
public final class CenaFilosofosSemaforos {

    private CenaFilosofosSemaforos() {
        // Evitamos instanciación
        }

    public static void main(String[] args) {
        final int N = 5;

        // Un semáforo binario por tenedor (1 permiso)
        final Semaphore[] tenedores = new Semaphore[N];
        for (int i = 0; i < N; i++) {
            tenedores[i] = new Semaphore(1, true); // fairness=true para reducir starvation
        }

        // Semáforo "portero" con N-1 permisos
        final Semaphore portero = new Semaphore(N - 1, true);

        for (int i = 0; i < N; i++) {
            final FilosofoConSemaforos f =
                    new FilosofoConSemaforos(i, tenedores, portero);
            final Thread t = new Thread(f, "FilosofoSem-" + i);
            t.start();
        }
    }
}