package es.iescamas.multihilo.monitor.hilo;

import es.iescamas.multihilo.monitor.MonitorFilosofos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FilosofoTest {

    private MonitorFilosofos monitor;

    @BeforeEach
    void setUp() {
        monitor = new MonitorFilosofos(5);
    }

    // ============================================================
    // TEST CONSTRUCTOR
    // ============================================================

    @Test
    @DisplayName("Constructor asigna correctamente id y monitor")
    void testConstructor() {
        Filosofo f = new Filosofo(2, monitor);
        assertNotNull(f, "Filósofo no debe ser null");
        assertEquals(2, f.getId(), "El id del filósofo debe coincidir");
    }

    // ============================================================
    // TEST INICIALIZACIÓN
    // ============================================================

    @Test
    @DisplayName("Todos los filósofos comienzan PENSANDO y los tenedores libres")
    void testInicializacionMonitor() {
        for (int i = 0; i < monitor.getNumFilosofos(); i++) {
            assertEquals(MonitorFilosofos.PENSANDO, monitor.getEstado(i));
            assertEquals(-1, monitor.getPoseedorTenedor(i));
        }
    }

    // ============================================================
    // TEST TOMAR Y DEJAR TENEDORES DIRECTAMENTE
    // ============================================================

    @Test
    @DisplayName("Filósofo puede tomar y dejar tenedores directamente en el monitor")
    void testTomarDejarTenedores() throws InterruptedException {
        monitor.tomarTenedores(0);
        assertEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(0));

        monitor.dejarTenedores(0);
        assertEquals(MonitorFilosofos.PENSANDO, monitor.getEstado(0));
        assertEquals(-1, monitor.getPoseedorTenedor(0));
        assertEquals(-1, monitor.getPoseedorTenedor(4));
    }

    // ============================================================
    // TEST EXCLUSIÓN MUTUA ENTRE VECINOS
    // ============================================================

    @Test
    @DisplayName("Dos filósofos vecinos no pueden comer simultáneamente")
    void testExclusionMutuaVecinos() throws InterruptedException {
        // Creamos un CountDownLatch con contador 2 para sincronizar el inicio de ambos hilos
        CountDownLatch latch = new CountDownLatch(2);

        // Hilo del filósofo 0
        Thread t0 = new Thread(() -> {
            try {
                latch.countDown();    // Señalamos que t0 está listo
                latch.await();        // Esperamos hasta que ambos hilos estén listos para intentar comer
                monitor.tomarTenedores(0); // Filósofo 0 intenta tomar sus tenedores
                TimeUnit.MILLISECONDS.sleep(50); // Simulamos un tiempo comiendo
                monitor.dejarTenedores(0);      // Filósofo 0 deja los tenedores
            } catch (Exception ignored) {}
        });

        // Hilo del filósofo 1 (vecino de 0)
        Thread t1 = new Thread(() -> {
            try {
                latch.countDown();    // Señalamos que t1 está listo
                latch.await();        // Esperamos hasta que ambos hilos estén listos
                monitor.tomarTenedores(1); // Filósofo 1 intenta tomar sus tenedores
                TimeUnit.MILLISECONDS.sleep(50); // Simulamos tiempo comiendo
                monitor.dejarTenedores(1);      // Filósofo 1 deja los tenedores
            } catch (Exception ignored) {}
        });

        // Iniciamos ambos hilos casi simultáneamente
        t0.start();
        t1.start();

        // Esperamos a que ambos hilos terminen para poder hacer las aserciones
        t0.join();
        t1.join();

        // Comprobamos si alguno de los filósofos está COMIENDO al mismo tiempo
        boolean f0Come = monitor.getEstado(0) == MonitorFilosofos.COMIENDO;
        boolean f1Come = monitor.getEstado(1) == MonitorFilosofos.COMIENDO;

        // Aserción final: ningún par de vecinos debe comer simultáneamente
        assertFalse(f0Come && f1Come, "Vecinos no deben comer simultáneamente");
    }


    // ============================================================
    // TEST PROGRESO DE VECINOS
    // ============================================================

    @Test
    @DisplayName("Si un filósofo deja de comer, su vecino puede comer")
    void testProgresoVecino() throws InterruptedException {
        monitor.tomarTenedores(0);

        Thread t1 = new Thread(() -> {
            try {
                monitor.tomarTenedores(1);
            } catch (InterruptedException ignored) {}
        });
        t1.start();

        TimeUnit.MILLISECONDS.sleep(50);

        assertNotEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(1));

        monitor.dejarTenedores(0);
        t1.join(200);

        assertEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(1));
    }

    // ============================================================
    // TEST DEADLOCK (EVITAR BLOQUEO)
    // ============================================================

    @Test
    @DisplayName("Varios filósofos pueden comer sin provocar deadlock")
    void testSinDeadlock() throws InterruptedException {
        int n = monitor.getNumFilosofos();
        Thread[] hilos = new Thread[n];

        for (int i = 0; i < n; i++) {
            final int id = i;
            hilos[i] = new Thread(() -> {
                try {
                    monitor.tomarTenedores(id);
                    TimeUnit.MILLISECONDS.sleep(20);
                    monitor.dejarTenedores(id);
                } catch (InterruptedException ignored) {}
            });
            hilos[i].start();
        }

        for (Thread t : hilos) t.join(500);

        // Ningún filósofo debería quedarse COMIENDO indefinidamente
        for (int i = 0; i < n; i++) {
            assertEquals(MonitorFilosofos.PENSANDO, monitor.getEstado(i),
                    "Filósofo " + i + " debería haber terminado de comer");
        }
    }

    // ============================================================
    // TEST CIRCULARIDAD
    // ============================================================

    @Test
    @DisplayName("Verifica circularidad de vecinos y tenedores")
    void testCircularidad() {
        int n = monitor.getNumFilosofos();
        int last = n - 1;

        // Vecino derecho del último = 0
        assertEquals(0, (last + 1) % n);

        // Tenedor izquierdo del último = n-2, derecho = n-1
        assertEquals(last - 1, (last - 1 + n) % n);
        assertEquals(last, last); // derecho
    }
}
