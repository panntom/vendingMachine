import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

class CashReceiver extends PaymentMethod {
    private final CashValidator validator;
    private final ChangeDispenser changer;
    private final TransactionTimer timer = new TransactionTimer();
    private final Map<Integer, Integer> inserted = new TreeMap<>(Comparator.reverseOrder());
    private int balance;
    private int timeoutSeconds = 30;
    private Consumer<Integer> tickListener = n -> { };

    CashReceiver(CashValidator validator, ChangeDispenser changer) {
        this.validator = validator;
        this.changer = changer;
    }

    void setTickListener(Consumer<Integer> tickListener) {
        this.tickListener = tickListener;
    }

    void setTimeoutSeconds(int seconds) {
        this.timeoutSeconds = seconds;
    }

    @Override
    String getName() {
        return "Cash";
    }

    @Override
    protected void onStart() {
        balance = 0;
        inserted.clear();
        timer.start(timeoutSeconds, tickListener, this::timeout);
    }

    /** Returns false if the CashValidator rejected the item (it is handed back to the customer). */
    boolean insertMoney(int value) {
        if (!active) {
            return false;
        }
        if (!validator.isValid(value)) {
            return false;
        }
        inserted.merge(value, 1, Integer::sum);
        balance += value;
        if (balance >= amountDue) {
            finish();
        }
        return true;
    }

    private void finish() {
        timer.stop();
        active = false;
        int change = balance - amountDue;
        if (changer.canGiveChange(change)) {
            Map<Integer, Integer> given = changer.dispenseChange(change);
            changer.depositCoins(inserted);
            listener.onPaymentComplete(change > 0
                    ? "Change returned: " + Money.describe(given) : "Exact amount, no change");
        } else {
            listener.onPaymentFailed(FailureReason.NO_EXACT_CHANGE,
                    "Returned: " + Money.describe(changer.refundInserted(inserted)));
        }
    }

    private void timeout() {
        if (!active) {
            return;
        }
        active = false;
        listener.onPaymentFailed(FailureReason.TIMEOUT, "Returned: " + Money.describe(changer.refundInserted(inserted)));
    }

    @Override
    void cancel() {
        if (!active) {
            return;
        }
        timer.stop();
        active = false;
        listener.onPaymentFailed(FailureReason.CANCELLED, "Returned: " + Money.describe(changer.refundInserted(inserted)));
    }

    int getBalance() {
        return balance;
    }
}