package es.iescamas.multihilo.util;

import es.iescamas.multihilo.monitor.MonitorFilosofos;
import es.iescamas.multihilo.monitor.MonitorFilosofoConPortero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilidadTest {

    private MonitorFilosofos monitor;
    private MonitorFilosofoConPortero monitorPortero;

    @BeforeEach
    void setUp() {
        monitor = new MonitorFilosofos(5);
        monitorPortero = new MonitorFilosofoConPortero(monitor);
    }

    // =============================================================
    // TEST CREACIÓN DE TABLA CLÁSICA
    // =============================================================
    @Test
    @DisplayName("Crear tabla de monitor clásico")
    void testCrearTablaMonitorClasico() throws InterruptedException {
        // Simulamos que el filósofo 0 está comiendo
        monitor.tomarTenedores(0);

        String tabla = Utilidad.crearTabla(monitor, null);

        assertTrue(tabla.contains("F0"), "Tabla debe contener al filósofo 0");
        assertTrue(tabla.contains("🍝 COMIENDO"), "Filósofo 0 debe aparecer comiendo");
        assertTrue(tabla.contains("🧠 PENSANDO"), "Otros filósofos deben aparecer pensando o hambrientos");
    }

    // =============================================================
    // TEST CREACIÓN DE TABLA CON PORTERO
    // =============================================================
    @Test
    @DisplayName("Crear tabla de monitor con portero mostrando quien espera fuera")
    void testCrearTablaMonitorPortero() throws InterruptedException {
        // Simulamos que el filósofo 0 y 1 intentan sentarse, pero solo uno puede
        monitorPortero.sentarse(0);
        monitorPortero.sentarse(1);

        String tabla = Utilidad.crearTabla(monitor, monitorPortero);

        assertTrue(tabla.contains("F0") || tabla.contains("F1"), "Tabla debe contener filósofos dentro");
        assertTrue(tabla.contains("esperando FUERA"), "Debe mostrar filósofos esperando fuera del portero");
    }

    // =============================================================
    // TEST REPRESENTACIÓN DE TENEDORES
    // =============================================================
    @Test
    @DisplayName("Representar tenedores correctamente")
    void testRepresentarTenedores() throws InterruptedException {
        monitor.tomarTenedores(2);

        String tabla = Utilidad.crearTabla(monitor, null);

        assertTrue(tabla.contains("[🍴]"), "Tenedores deben marcarse como en mano cuando los tiene un filósofo");
        assertTrue(tabla.contains("[   ]"), "Tenedores libres deben mostrarse vacíos");
    }

    // =============================================================
    // TEST COMPLETO DE FILÓSOFO FUERA DEL PORTERO
    // =============================================================
    @Test
    @DisplayName("Filósofo esperando fuera del portero aparece en la tabla")
    void testFilosofoFueraPortero() throws InterruptedException {
        // Simulamos que todos los filósofos intentan sentarse
        for (int i = 0; i < 5; i++) {
            monitorPortero.sentarse(i);
        }

        String tabla = Utilidad.crearTabla(monitor, monitorPortero);

        assertTrue(tabla.contains("esperando FUERA"), "Debe mostrar filósofos esperando fuera del portero");
        assertTrue(tabla.contains("F"), "Debe listar al menos un filósofo fuera");
    }
}
