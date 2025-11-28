package es.iescamas.multihilo.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MonitorFilosofosTest {

    private MonitorFilosofos monitor;

    @BeforeEach
    void setUp() {
        monitor = new MonitorFilosofos(5);
    }

    // ============================================================
    // TEST DE INICIALIZACIÓN
    // ============================================================

    @Test
    @DisplayName("Todos los filósofos empiezan pensando y los tenedores están libres")
    void testInicializacion() {
        for (int i = 0; i < monitor.getNumFilosofos(); i++) {
            assertEquals(MonitorFilosofos.PENSANDO, monitor.getEstado(i), "Filósofo " + i + " debería estar PENSANDO");
            assertEquals(-1, monitor.getPoseedorTenedor(i), "Tenedor " + i + " debería estar libre");
        }
    }

    @Test
    @DisplayName("Constructor lanza excepción si n <= 1")
    void testConstructorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new MonitorFilosofos(1));
    }

    // ============================================================
    // TEST LÓGICA TOMAR Y DEJAR TENEDORES
    // ============================================================

    @Test
    @DisplayName("Un filósofo puede tomar tenedores si sus vecinos no comen")
    void testTomarTenedoresSinConflicto() throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                monitor.tomarTenedores(0);
            } catch (InterruptedException ignored) {}
        });
        t.start();
        t.join(200);

        assertEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(0));
        assertEquals(0, monitor.getPoseedorTenedor(0), "Tenedor derecho debe ser del filósofo 0");
        assertEquals(0, monitor.getPoseedorTenedor(4), "Tenedor izquierdo debe ser del filósofo 0");
    }

    @Test
    @DisplayName("Al dejar los tenedores se liberan y el estado vuelve a PENSANDO")
    void testDejarTenedores() throws InterruptedException {
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
    @DisplayName("Dos vecinos no pueden comer al mismo tiempo")
    void testVecinosNoComenSimultaneamente() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        Thread t0 = new Thread(() -> {
            try {
                latch.countDown();
                latch.await();
                monitor.tomarTenedores(0);
            } catch (Exception ignored) {}
        });

        Thread t1 = new Thread(() -> {
            try {
                latch.countDown();
                latch.await();
                monitor.tomarTenedores(1);
            } catch (Exception ignored) {}
        });

        t0.start();
        t1.start();

        TimeUnit.MILLISECONDS.sleep(300);

        boolean f0Come = monitor.getEstado(0) == MonitorFilosofos.COMIENDO;
        boolean f1Come = monitor.getEstado(1) == MonitorFilosofos.COMIENDO;

        assertFalse(f0Come && f1Come, "Dos vecinos no pueden comer simultáneamente");
    }

    // ============================================================
    // TEST PROGRESO DE VECINOS
    // ============================================================

    @Test
    @DisplayName("Si un filósofo deja de comer, su vecino puede comer")
    void testProgresoVecino() throws InterruptedException {
        // 0 empieza a comer
        monitor.tomarTenedores(0);
        assertEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(0));

        // Filósofo 1 intenta comer en hilo separado
        Thread t1 = new Thread(() -> {
            try {
                monitor.tomarTenedores(1);
            } catch (InterruptedException ignored) {}
        });
        t1.start();

        Thread.sleep(200);
        // Mientras 0 come, 1 no debe comer
        assertNotEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(1));

        // Ahora 0 deja de comer
        monitor.dejarTenedores(0);
        Thread.sleep(200);

        // Ahora 1 debería estar comiendo
        assertEquals(MonitorFilosofos.COMIENDO, monitor.getEstado(1));
    }

    // ============================================================
    // TEST LÍMITE: último filósofo y circularidad
    // ============================================================

    @Test
    @DisplayName("Verifica que los tenedores se asignan correctamente (circularidad implícita)")
    void testCircularidadPublica() throws InterruptedException {
        int last = monitor.getNumFilosofos() - 1;

        // El último filósofo toma tenedores
        monitor.tomarTenedores(last);

        // Verifica tenedores ocupados correctamente
        assertEquals(last, monitor.getPoseedorTenedor(last), "Tenedor derecho del último filósofo");
        assertEquals(last, monitor.getPoseedorTenedor((last - 1 + monitor.getNumFilosofos()) % monitor.getNumFilosofos()), "Tenedor izquierdo del último filósofo");

        monitor.dejarTenedores(last);
    }

}
