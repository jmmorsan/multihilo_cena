package main;
/**
 * Punto de entrada de la aplicación. Crea N filósofos y un monitor
 * compartido para coordinar el acceso a los recursos (tenedores).
 */
public final class CenaFilosofos {

    private CenaFilosofos() {
        // Evitamos instanciación
    }

    public static void main(String[] args) {
        final int N = 5;
        final MonitorFilosofos monitor = new MonitorFilosofos(N);

        for (int i = 0; i < N; i++) {
            final Filosofo f = new Filosofo(i, monitor);
            final Thread t = new Thread(f, "Filosofo-" + i);

            // Opcional: prioridad para trabajar el criterio g)
            // t.setPriority(Thread.NORM_PRIORITY + (i % 2));

            t.start(); // el hilo pasa de NEW a RUNNABLE
        }
    }
}