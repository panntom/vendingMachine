abstract class PaymentMethod {
    protected int amountDue;
    protected PaymentListener listener;
    protected boolean active;

    void start(int amountDue, PaymentListener listener) {
        this.amountDue = amountDue;
        this.listener = listener;
        this.active = true;
        onStart();
    }

    protected void onStart() { }

    abstract String getName();

    abstract void cancel();

    boolean isActive() {
        return active;
    }
}