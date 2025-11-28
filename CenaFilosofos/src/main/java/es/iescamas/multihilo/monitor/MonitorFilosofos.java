package es.iescamas.multihilo.monitor;

public final class MonitorFilosofos {

    public static final int PENSANDO   = 0;
    public static final int HAMBRIENTO = 1;
    public static final int COMIENDO   = 2;

    private final int n;

    // estado[idFilosofo] = PENSANDO / HAMBRIENTO / COMIENDO
    private final int[] estado;

    // tenedorPoseedor[idTenedor] = idFilosofo que lo tiene, o -1 si está libre
    private final int[] tenedorPoseedor;

    public MonitorFilosofos(final int n) {
        if (n <= 1) {
            throw new IllegalArgumentException("Debe haber al menos dos filósofos");
        }

        this.n = n;
        this.estado = new int[n];
        this.tenedorPoseedor = new int[n];

        for (int id = 0; id < n; id++) {
            estado[id] = PENSANDO;
            tenedorPoseedor[id] = -1;
        }
    }

    // -----------------------------------------------------------
    //  Cálculo de vecinos
    // -----------------------------------------------------------

    private int tenedorIzquierdo(int idFilosofo) {
        return (idFilosofo - 1 + n) % n;
    }

    private int tenedorDerecho(int idFilosofo) {
        return idFilosofo; // por definición del modelo circular
    }

    private int vecinoIzquierdo(int idFilosofo) {
        return (idFilosofo - 1 + n) % n;
    }

    private int vecinoDerecho(int idFilosofo) {
        return (idFilosofo + 1) % n;
    }

    // -----------------------------------------------------------
    //  Lógica principal
    // -----------------------------------------------------------

    public synchronized void tomarTenedores(final int idFilosofo) throws InterruptedException {
        estado[idFilosofo] = HAMBRIENTO;
        intentarComer(idFilosofo);

        while (estado[idFilosofo] != COMIENDO) {
            wait();
        }
    }

    public synchronized void dejarTenedores(final int idFilosofo) {

        // libera sus tenedores primero
        liberarTenedor(tenedorIzquierdo(idFilosofo));
        liberarTenedor(tenedorDerecho(idFilosofo));

        estado[idFilosofo] = PENSANDO;

        // Permite que los vecinos intenten comer ahora
        intentarComer(vecinoIzquierdo(idFilosofo));
        intentarComer(vecinoDerecho(idFilosofo));

        notifyAll();
    }

    private void intentarComer(final int idFilosofo) {

        if (estado[idFilosofo] == HAMBRIENTO &&
            estado[vecinoIzquierdo(idFilosofo)] != COMIENDO &&
            estado[vecinoDerecho(idFilosofo)] != COMIENDO) {

            estado[idFilosofo] = COMIENDO;

            ocuparTenedor(tenedorIzquierdo(idFilosofo), idFilosofo);
            ocuparTenedor(tenedorDerecho(idFilosofo), idFilosofo);
        }
    }

    // -----------------------------------------------------------
    // Gestión de tenedores
    // -----------------------------------------------------------

    private void ocuparTenedor(final int idTenedor, final int idFilosofo) {
        tenedorPoseedor[idTenedor] = idFilosofo;
    }

    private void liberarTenedor(final int idTenedor) {
        tenedorPoseedor[idTenedor] = -1;
    }

    // -----------------------------------------------------------
    // Getters de consulta (todos synchronized)
    // -----------------------------------------------------------

    public synchronized int getEstado(final int idFilosofo) {
        return estado[idFilosofo];
    }

    public synchronized int getPoseedorTenedor(final int idTenedor) {
        return tenedorPoseedor[idTenedor];
    }

    public int getNumFilosofos() {
        return n;
    }
}
