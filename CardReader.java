class CardReader extends PaymentMethod {
    private final BankService bank;

    CardReader(BankService bank) {
        this.bank = bank;
    }

    @Override
    String getName() {
        return "Card";
    }

    void presentCard(String cardNumber) {
        if (!active) {
            return;
        }
        active = false;
        if (bank.authorise(cardNumber, amountDue)) {
            listener.onPaymentComplete("Card approved");
        } else {
            listener.onPaymentFailed(FailureReason.CARD_DECLINED, "Card declined");
        }
    }

    @Override
    void cancel() {
        if (!active) {
            return;
        }
        active = false;
        listener.onPaymentFailed(FailureReason.CANCELLED, "Card payment cancelled");
    }
}