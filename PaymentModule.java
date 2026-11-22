class PaymentModule {
    private final CashReceiver cash;
    private final CardReader card;
    private PaymentMethod current;

    PaymentModule(CashReceiver cash, CardReader card) {
        this.cash = cash;
        this.card = card;
    }

    void startCash(int amount, PaymentListener listener) {
        current = cash;
        cash.start(amount, listener);
    }

    void startCard(int amount, PaymentListener listener) {
        current = card;
        card.start(amount, listener);
    }

    void abortPayment() {
        if (current != null) {
            current.cancel();
        }
    }

    CashReceiver getCash() {
        return cash;
    }

    CardReader getCard() {
        return card;
    }

    PaymentMethod getCurrent() {
        return current;
    }

    void clear() {
        current = null;
    }
}