import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class TransactionLogger {
    private final List<TransactionRecord> records = new ArrayList<>();
    private final List<String> staffAlerts = new ArrayList<>();
    private int dailyTotal;

    void log(String drink, String method, int amount, String status, String reason) {
        TransactionRecord r = new TransactionRecord(drink, method, amount, status, reason);
        records.add(r);
        if (status.equals("SUCCESS")) {
            dailyTotal += amount;
        }
    }

    void notifyStaff(String message) {
        staffAlerts.add(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " | " + message);
    }

    List<TransactionRecord> getRecords() {
        return records;
    }

    List<String> getStaffAlerts() {
        return staffAlerts;
    }

    int getDailyTotal() {
        return dailyTotal;
    }

    void resetDailyTotal() {
        dailyTotal = 0;
    }
}
