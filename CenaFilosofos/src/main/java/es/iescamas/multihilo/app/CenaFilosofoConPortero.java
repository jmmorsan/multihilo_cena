package es.iescamas.multihilo.app;

import java.util.Scanner;

import es.iescamas.multihilo.monitor.MonitorFilosofos;
import es.iescamas.multihilo.monitor.MonitorFilosofoConPortero;
import es.iescamas.multihilo.monitor.hilo.FilosofoConPortero;
import es.iescamas.multihilo.util.Utilidad;

/**
 * Punto de entrada de la aplicación que ejecuta la cena de los filósofos
 * usando la solución del PORTERO (Footman).
 */
public final class CenaFilosofoConPortero {

    /** Número de filósofos en la mesa. */
    private static final int N = 5;

    private CenaFilosofoConPortero() {
        // Evitar instanciación
    }

    public static void main(final String[] args) {
        System.out.println("=== Cena de los Filósofos con PORTERO ===");
        System.out.println("Se crearán " + N + " filósofos.");
        System.out.println("El portero solo permite que se sienten " 
                + (N - 1) + " a la vez.");
        System.out.println("Pulsa ENTER para detener la simulación.\n");

        // Monitor clásico de filósofos
        final MonitorFilosofos monitorBase = new MonitorFilosofos(N);

        // Monitor decorado con la lógica del portero
        final MonitorFilosofoConPortero monitorConPortero =
                new MonitorFilosofoConPortero(monitorBase);

        // Array para poder interrumpir los hilos después
        final Thread[] hilos = new Thread[N];
        
        // Crear utilidad de monitorización

        Thread hiloUtilidad = new Thread(new Utilidad(monitorBase), "Monitor-Utilidad");
        hiloUtilidad.start();

        // Crear y lanzar los hilos de los filósofos
        for (int i = 0; i < N; i++) {
            final FilosofoConPortero filosofo = new FilosofoConPortero(i, monitorConPortero);
            final Thread hilo = new Thread(filosofo, "FilosofoPortero-" + i);
            hilos[i] = hilo;
            hilo.start();
        }

        // Esperar a que el usuario pulse ENTER para detener
        try (Scanner sc = new Scanner(System.in)) {
            sc.nextLine();
        }

        System.out.println("\nDeteniendo simulación...");

        // Interrumpimos todos los hilos
        for (Thread hilo : hilos) {
            if (hilo != null && hilo.isAlive()) {
                hilo.interrupt();
            }
        }

        // Esperamos a que terminen limpiamente
        for (Thread hilo : hilos) {
            if (hilo != null) {
                try {
                    hilo.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // Si nos interrumpen mientras esperamos, salimos
                    break;
                }
            }
        }

        System.out.println("Simulación finalizada.");
    }
}
