import java.util.function.Consumer;

class TransactionTimer {
    private javax.swing.Timer timer;
    private int remaining;

    void start(int seconds, Consumer<Integer> onTick, Runnable onTimeout) {
        stop();
        remaining = seconds;
        onTick.accept(remaining);
        timer = new javax.swing.Timer(1000, e -> {
            remaining--;
            onTick.accept(remaining);
            if (remaining <= 0) {
                stop();
                onTimeout.run();
            }
        });
        timer.start();
    }

    void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}