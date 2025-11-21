package main;

public final class FilosofoFootman implements Runnable {

    private final int id;
    private final MonitorConFootman monitor;

    public FilosofoFootman(final int id, final MonitorConFootman monitor) {
        this.id = id;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // pensar...
                System.out.println("Filósofo " + id + " PENSANDO (footman)...");
                Thread.sleep(1000L);

                monitor.sentarse(id);
                System.out.println("Filósofo " + id + " COMIENDO (footman) 🍝");
                Thread.sleep(1000L);
                monitor.levantarse(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}