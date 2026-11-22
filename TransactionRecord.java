import java.text.SimpleDateFormat;
import java.util.Date;

class TransactionRecord {
    private final String time;
    private final String drink;
    private final String method;
    private final int amount;
    private final String status;
    private final String reason;

    TransactionRecord(String drink, String method, int amount, String status, String reason) {
        this.time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        this.drink = drink;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
    }

    String getStatus() {
        return status;
    }

    int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return time + " | " + drink + " | " + method + " | " + Money.format(amount)
                + " | " + status + " | " + reason;
    }
}
