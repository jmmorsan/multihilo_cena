package es.iescamas.multihilo.util;

import es.iescamas.multihilo.monitor.MonitorFilosofos;
import es.iescamas.multihilo.monitor.MonitorFilosofoConPortero;

/**
 * Clase de utilidad que muestra el estado de los filósofos y tenedores.
 * Puede monitorizar un monitor clásico o un monitor con portero.
 */
public final class Utilidad implements Runnable {

    private final MonitorFilosofos monitor;
  //  private final MonitorFilosofoConPortero monitorPortero; // puede ser null
    private volatile boolean ejecutando = true;
    private volatile boolean pausado = false;

    public Utilidad(final MonitorFilosofos monitor) {
       this.monitor = monitor;
    }

  /*  public Utilidad(final MonitorFilosofos monitor,
                    final MonitorFilosofoConPortero monitorPortero) {
        this.monitor = monitor;
        this.monitorPortero = monitorPortero;
    }*/

    public void detener() {
        ejecutando = false;
    }

    public void pausar() {
        pausado = true;
    }

    public void reanudar() {
        pausado = false;
    }

    @Override
    public void run() {
        try {
            while (ejecutando && !Thread.currentThread().isInterrupted()) {
                if (!pausado) {
                    System.out.println(crearTabla(monitor));
                }
                Thread.sleep(500L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // =============================================================
    // CREACIÓN DE LA TABLA
    // =============================================================

    public static String crearTabla(final MonitorFilosofos monitor) {
        final int n = monitor.getNumFilosofos();
        final StringBuilder sb = new StringBuilder();

        final String sep =
                "+" + repetir('-', 10) +
                "+" + repetir('-', 22) +
                "+" + repetir('-', 10) +
                "+" + repetir('-', 10) +
                "+";

        sb.append("\n========== ESTADO DE LA MESA ==========\n");
        sb.append("Leyenda estados: 🧠 PENSANDO | 😋 HAMBRIENTO | 🍝 COMIENDO\n");
        sb.append("Leyenda tenedores: [🍴] = en mano | [   ] = no lo tiene\n");
        sb.append(sep).append("\n");
        sb.append(String.format("| %8s | %-20s | %-8s | %-8s |\n",
                "Filósofo", "Estado", "Tizq", "Tder"));
        sb.append(sep).append("\n");

        for (int idFilosofo = 0; idFilosofo < n; idFilosofo++) {
            final int estado = monitor.getEstado(idFilosofo);
            final String estadoTxt = textoEstado(estado);

            final int tenedorIzq = (idFilosofo - 1 + n) % n;
            final int tenedorDer = idFilosofo;

            final String reprIzq = representarTenedor(monitor, tenedorIzq, idFilosofo);
            final String reprDer = representarTenedor(monitor, tenedorDer, idFilosofo);

            sb.append(String.format("| %8d | %-20s | %-8s | %-8s |\n",
                    idFilosofo, estadoTxt, reprIzq, reprDer));
        }

        sb.append(sep).append("\n");

        // Mostrar quién está fuera del portero
      /*  if (monitorPortero != null) {
            sb.append(crearLineaFueraPortero(monitorPortero));
        }*/

        return sb.toString();
    }

    private static String crearLineaFueraPortero(final MonitorFilosofoConPortero monitorPortero) {
        final int n = monitorPortero.getNumFilosofos();
        final StringBuilder sb = new StringBuilder();
        sb.append("Filósofos esperando FUERA del portero: ");

        boolean hayAlguno = false;
        for (int id = 0; id < n; id++) {
            if (monitorPortero.isEsperandoPortero(id)) {
                if (hayAlguno) sb.append(", ");
                sb.append("F").append(id);
                hayAlguno = true;
            }
        }

        if (!hayAlguno) {
            sb.append("ninguno (todos dentro o pensando lejos)");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =============================================================
    // MÉTODOS AUXILIARES
    // =============================================================

    private static String repetir(final char c, final int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    private static String textoEstado(final int estado) {
        return switch (estado) {
            case MonitorFilosofos.PENSANDO   -> "🧠 PENSANDO";
            case MonitorFilosofos.HAMBRIENTO -> "😋 HAMBRIENTO";
            case MonitorFilosofos.COMIENDO   -> "🍝 COMIENDO";
            default                          -> "???";
        };
    }

    private static String representarTenedor(final MonitorFilosofos monitor,
                                             final int idTenedor,
                                             final int idFilosofo) {
        return monitor.getPoseedorTenedor(idTenedor) == idFilosofo ? "[🍴]" : "[   ]";
    }
}
