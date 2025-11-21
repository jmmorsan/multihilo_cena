package main;
/**
 * Monitor de sincronización para el problema de la cena de los filósofos.
 * Encapsula el estado de cada filósofo y garantiza que solo puede comer
 * un filósofo cuyos vecinos no estén comiendo.
 */
public final class MonitorFilosofos {

    public static final int PENSANDO  = 0;
    public static final int HAMBRIENTO = 1;
    public static final int COMIENDO  = 2;

    private final int numFilosofos;

    // Estados: 0 = PENSANDO, 1 = HAMBRIENTO, 2 = COMIENDO
    private final int[] estado;

    /**
     * Crea un monitor para N filósofos.
     *
     * @param numFilosofos número de filósofos (N > 1)
     */
    public MonitorFilosofos(final int numFilosofos) {
        if (numFilosofos <= 1) {
            throw new IllegalArgumentException("Debe haber al menos dos filósofos");
        }
        this.numFilosofos = numFilosofos;
        this.estado = new int[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            estado[i] = PENSANDO; // todos empiezan pensando
        }
    }

    public int getNumFilosofos() {
        return numFilosofos;
    }

    private int izquierda(final int i) {
        return (i + numFilosofos - 1) % numFilosofos;
    }

    private int derecha(final int i) {
        return (i + 1) % numFilosofos;
    }

    private void cambiarEstado(final int i, final int nuevo) {
        estado[i] = nuevo;
    }

    private int estadoIzq(final int i) {
        return estado[izquierda(i)];
    }

    private int estadoDer(final int i) {
        return estado[derecha(i)];
    }

    /**
     * Intenta que el filósofo i pase a estado COMIENDO si sus vecinos
     * no están comiendo. Este método debe llamarse siempre con el monitor
     * adquirido (dentro de un método synchronized).
     */
    private void intentarComer(final int i) {
        if (estado[i] == HAMBRIENTO
                && estadoIzq(i) != COMIENDO
                && estadoDer(i) != COMIENDO) {
            cambiarEstado(i, COMIENDO);
            // Despertamos a todos para que los filósofos que estaban
            // esperando puedan reevaluar su condición.
            notifyAll();
        }
    }

    /**
     * Filósofo i se declara hambriento e intenta comer. Si no puede,
     * el hilo queda bloqueado (WAITING) hasta que pueda pasar a COMIENDO.
     *
     * @param i índice del filósofo (0..N-1)
     * @throws InterruptedException si el hilo es interrumpido mientras espera
     */
    public synchronized void tomarTenedores(final int i) throws InterruptedException {
        cambiarEstado(i, HAMBRIENTO);
        intentarComer(i);

        // Si no ha podido empezar a comer, espera hasta que su estado sea COMIENDO
        while (estado[i] != COMIENDO) {
            wait(); // pasa a estado WAITING asociado a este monitor
        }
    }

    /**
     * Filósofo i deja de comer y pasa a pensar. Después de liberar los
     * tenedores, permite que sus vecinos intenten comer.
     *
     * @param i índice del filósofo (0..N-1)
     */
    public synchronized void dejarTenedores(final int i) {
        cambiarEstado(i, PENSANDO);
        // Sus vecinos pueden intentar comer ahora que i ha dejado los tenedores.
        intentarComer(izquierda(i));
        intentarComer(derecha(i));
        notifyAll();
    }

    /**
     * Solo para depuración o para mostrar el estado en una interfaz gráfica.
     *
     * @param i índice del filósofo
     * @return estado del filósofo i
     */
    public synchronized int getEstado(final int i) {
        return estado[i];
    }
}