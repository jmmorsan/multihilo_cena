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
    
    /**
     * El filósofo solicita los tenedores para poder comer. Si no puede obtenerlos
     * de inmediato, el hilo se bloquea hasta que sea notificado.
     * * <p><strong>Estados del Hilo:</strong></p>
     * <ul>
     * <li><b>RUNNABLE:</b> El hilo entra a la sección crítica (monitor).</li>
     * <li><b>WAITING:</b> Si la condición lógica de {@code intentarComer} no se cumple,
     * el hilo libera el monitor y se suspende invocando {@code wait()}.</li>
     * </ul>
     * * @param idFilosofo El identificador del filósofo.
     * @throws InterruptedException Si el hilo es interrumpido mientras está esperando (WAITING).
     */

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
    
    /**
     * Intenta que el filósofo especificado pase al estado COMIENDO, verificando
     * la disponibilidad de los tenedores.
     * * <p><strong>Condición Lógica:</strong> Se comprueba que el filósofo tenga estado
     * HAMBRIENTO y que sus vecinos inmediatos (izquierda y derecha) NO estén en 
     * estado COMIENDO.</p>
     * * <p><strong>Invariante:</strong> Garantiza la exclusión mutua, asegurando que
     * nunca haya dos filósofos vecinos comiendo simultáneamente.</p>
     * * @param idFilosofo El identificador del filósofo (0..N-1).
     */

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
