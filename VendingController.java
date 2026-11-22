import java.util.ArrayList;
import java.util.List;

class VendingController implements PaymentListener {
    enum State { IDLE, DRINK_SELECTED, PAYING }

    private final List<Drink> drinks = new ArrayList<>();
    private final StockSensor stock = new StockSensor();
    private final LoyaltyScanner loyaltyScanner = new LoyaltyScanner();
    private final ChangeDispenser changer = new ChangeDispenser();
    private final DrinkDispenser dispenser = new DrinkDispenser();
    private final TransactionLogger logger = new TransactionLogger();
    private final PaymentModule payments;
    private final VendingView view;

    private State state = State.IDLE;
    private Drink selected;
    private boolean loyaltyApplied;
    private int paidAmount;

    VendingController(VendingView view) {
        this.view = view;
        addDrink(new Drink("Espresso", 150), 5);
        addDrink(new Drink("Latte", 220), 5);
        addDrink(new Drink("Hot Chocolate", 200), 1);
        addDrink(new Drink("Bottled Water", 100), 5);
        CashReceiver cash = new CashReceiver(new CashValidator(), changer);
        cash.setTickListener(n -> view.showTimer(n));
        payments = new PaymentModule(cash, new CardReader(new BankService()));
    }

    private void addDrink(Drink drink, int quantity) {
        drinks.add(drink);
        stock.addDrink(drink, quantity);
    }

    /* ---- customer actions ---- */

    void selectDrink(Drink drink) {
        if (state == State.PAYING) {
            view.showMessage("Finish or cancel the current payment first.");
            return;
        }
        loyaltyApplied = false;
        if (!stock.isInStock(drink)) {
            selected = null;
            state = State.IDLE;
            logger.log(drink.getName(), "-", 0, "SOLD_OUT", "Out of stock");
            logger.notifyStaff("Empty slot: " + drink.getName());
            view.showMessage(drink.getName() + " is sold out. Please choose another drink.");
        } else {
            selected = drink;
            state = State.DRINK_SELECTED;
            view.showMessage("Selected " + drink.getName() + " (" + Money.format(drink.getPrice())
                    + "). Scan a loyalty card (optional), then choose how to pay.");
        }
        view.refresh();
    }

    void scanLoyaltyCard(String cardId) {
        if (state != State.DRINK_SELECTED) {
            view.showMessage("Select a drink first. The loyalty card must be scanned before payment.");
            return;
        }
        if (loyaltyScanner.isValid(cardId)) {
            loyaltyApplied = true;
            view.showMessage("Loyalty card accepted: 10% discount applied. "
                    + Money.format(getCurrentPrice()) + ".");
        } else {
            loyaltyApplied = false;
            view.showMessage("Loyalty card not valid.");
        }
        view.refresh();
    }

    void payByCash() {
        if (state != State.DRINK_SELECTED) {
            return;
        }
        paidAmount = getCurrentPrice();
        state = State.PAYING;
        payments.startCash(paidAmount, this);
        view.showMessage("Insert coins or notes to pay " + Money.format(paidAmount) + ".");
        view.refresh();
    }

    void payByCard() {
        if (state != State.DRINK_SELECTED) {
            return;
        }
        paidAmount = getCurrentPrice();
        state = State.PAYING;
        payments.startCard(paidAmount, this);
        view.showMessage("Present your card to pay " + Money.format(paidAmount) + ".");
        view.refresh();
    }

    void insertMoney(int value) {
        if (state != State.PAYING || payments.getCurrent() != payments.getCash()) {
            return;
        }
        boolean accepted = payments.getCash().insertMoney(value);
        if (!accepted) {
            view.showMessage("Item rejected by the cash validator and returned to the customer.");
        } else if (state == State.PAYING) {
            view.showMessage("Inserted " + Money.label(value) + ". Still to pay: "
                    + Money.format(paidAmount - payments.getCash().getBalance()) + ".");
        }
        view.refresh();
    }

    void presentCard(String cardNumber) {
        if (state != State.PAYING || payments.getCurrent() != payments.getCard()) {
            return;
        }
        payments.getCard().presentCard(cardNumber);
    }

    void cancel() {
        if (state == State.PAYING) {
            payments.abortPayment();
        } else if (state == State.DRINK_SELECTED) {
            resetTransaction();
            view.showMessage("Selection cancelled.");
            view.refresh();
        }
    }

    /* ---- PaymentListener ---- */

    @Override
    public void onPaymentComplete(String detail) {
        String drinkName = selected.getName();
        String method = payments.getCurrent().getName();
        stock.removeOne(selected);
        String output = dispenser.dispense(selected);
        logger.log(drinkName, method, paidAmount, "SUCCESS", detail);
        resetTransaction();
        view.showDispensed(output);
        view.showMessage("Payment successful. " + detail);
        view.refresh();
    }

    @Override
    public void onPaymentFailed(FailureReason reason, String detail) {
        String drinkName = selected.getName();
        String method = payments.getCurrent().getName();
        logger.log(drinkName, method, paidAmount, "FAILED", reason.name());
        resetTransaction();
        view.showMessage(failureText(reason) + " " + detail);
        view.refresh();
    }

    private String failureText(FailureReason reason) {
        switch (reason) {
            case CARD_DECLINED:
                return "Card declined.";
            case TIMEOUT:
                return "Timeout: not enough money inserted in time.";
            case CANCELLED:
                return "Transaction cancelled.";
            default:
                return "Exact change not available, so the sale was refused.";
        }
    }

    private void resetTransaction() {
        state = State.IDLE;
        selected = null;
        loyaltyApplied = false;
        payments.clear();
        view.showTimer(-1);
    }

    /* ---- staff actions ---- */

    void resetDailyTotal() {
        logger.resetDailyTotal();
        view.refresh();
    }

    void restockAll() {
        stock.restockAll(5);
        view.refresh();
    }

    void setTimeoutSeconds(int seconds) {
        payments.getCash().setTimeoutSeconds(seconds);
    }

    /* ---- getters for the view ---- */

    State getState() {
        return state;
    }

    Drink getSelected() {
        return selected;
    }

    boolean isLoyaltyApplied() {
        return loyaltyApplied;
    }

    int getCurrentPrice() {
        if (selected == null) {
            return 0;
        }
        return loyaltyApplied ? selected.getDiscountedPrice() : selected.getPrice();
    }

    boolean isCashPayment() {
        return state == State.PAYING && payments.getCurrent() == payments.getCash();
    }

    boolean isCardPayment() {
        return state == State.PAYING && payments.getCurrent() == payments.getCard();
    }

    int getBalance() {
        return payments.getCash().getBalance();
    }

    List<Drink> getDrinks() {
        return drinks;
    }

    int getStock(Drink drink) {
        return stock.getStock(drink);
    }

    ChangeDispenser getChanger() {
        return changer;
    }

    TransactionLogger getLogger() {
        return logger;
    }
}
