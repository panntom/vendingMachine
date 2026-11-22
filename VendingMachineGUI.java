import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendingMachineGUI extends JFrame implements VendingView {
    private static final int[] MONEY = {10, 20, 50, 100, 200, 500, 1000};
    private static final String[] TEST_CARDS = {
        "4111111111111111 (approved)", "4000000000000002 (declined)"};

    private final VendingController controller = new VendingController(this);

    private final List<JButton> drinkButtons = new ArrayList<>();
    private final List<JButton> moneyButtons = new ArrayList<>();
    private final JTextArea messageArea = new JTextArea(5, 28);
    private final JLabel selectionLabel = new JLabel("Selected: -");
    private final JLabel priceLabel = new JLabel("Price: -");
    private final JLabel balanceLabel = new JLabel("Inserted: -");
    private final JLabel timerLabel = new JLabel("Timer: -");
    private final JLabel dispensedLabel = new JLabel("Dispenser tray: empty");
    private final JTextField loyaltyField = new JTextField(10);
    private final JButton scanButton = new JButton("Scan loyalty card");
    private final JButton cashButton = new JButton("Pay by cash");
    private final JButton cardButton = new JButton("Pay by card");
    private final JButton foreignCoinButton = new JButton("Insert unrecognised coin");
    private final JComboBox<String> cardBox = new JComboBox<>(TEST_CARDS);
    private final JButton tapCardButton = new JButton("Present card");
    private final JButton cancelButton = new JButton("Cancel");
    private final JCheckBox demoBox = new JCheckBox("Demo mode (10 s timeout)");

    private final JTextArea cashArea = new JTextArea(8, 30);
    private final JTextArea historyArea = new JTextArea(12, 50);
    private final JTextArea alertsArea = new JTextArea(4, 50);
    private final JLabel totalLabel = new JLabel();

    public VendingMachineGUI() {
        super("Cafe Vending Machine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customer", buildCustomerPanel());
        tabs.addTab("Staff", buildStaffPanel());
        add(tabs);

        pack();
        setLocationRelativeTo(null);
        refresh();
        showMessage("Welcome. Please select a drink.");
    }

    private JPanel buildCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: drinks
        JPanel drinks = new JPanel(new GridLayout(0, 1, 5, 5));
        drinks.setBorder(BorderFactory.createTitledBorder("1. Select a drink"));
        for (Drink d : controller.getDrinks()) {
            JButton b = new JButton();
            b.addActionListener(e -> controller.selectDrink(d));
            drinkButtons.add(b);
            drinks.add(b);
        }
        panel.add(drinks, BorderLayout.WEST);

        // Centre: display
        JPanel display = new JPanel();
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
        display.setBorder(BorderFactory.createTitledBorder("Display"));
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        display.add(new JScrollPane(messageArea));
        display.add(Box.createVerticalStrut(8));
        display.add(selectionLabel);
        display.add(priceLabel);
        display.add(balanceLabel);
        display.add(timerLabel);
        display.add(Box.createVerticalStrut(8));
        display.add(dispensedLabel);
        panel.add(display, BorderLayout.CENTER);

        // Right: loyalty, payment, cancel
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JPanel loyalty = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loyalty.setBorder(BorderFactory.createTitledBorder("2. Loyalty card (optional, e.g. LOYAL001)"));
        loyalty.add(loyaltyField);
        loyalty.add(scanButton);
        scanButton.addActionListener(e -> controller.scanLoyaltyCard(loyaltyField.getText()));
        right.add(loyalty);

        JPanel method = new JPanel(new FlowLayout(FlowLayout.LEFT));
        method.setBorder(BorderFactory.createTitledBorder("3. Choose payment method"));
        method.add(cashButton);
        method.add(cardButton);
        cashButton.addActionListener(e -> controller.payByCash());
        cardButton.addActionListener(e -> controller.payByCard());
        right.add(method);

        JPanel cash = new JPanel(new GridLayout(0, 4, 5, 5));
        cash.setBorder(BorderFactory.createTitledBorder("Cash slot"));
        for (int value : MONEY) {
            JButton b = new JButton(Money.label(value));
            b.addActionListener(e -> controller.insertMoney(value));
            moneyButtons.add(b);
            cash.add(b);
        }
        right.add(cash);

        JPanel foreign = new JPanel(new FlowLayout(FlowLayout.LEFT));
        foreign.add(foreignCoinButton);
        foreignCoinButton.addActionListener(e -> controller.insertMoney(5));
        right.add(foreign);

        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT));
        card.setBorder(BorderFactory.createTitledBorder("Card reader (test cards)"));
        card.add(cardBox);
        card.add(tapCardButton);
        tapCardButton.addActionListener(e ->
                controller.presentCard(((String) cardBox.getSelectedItem()).split(" ")[0]));
        right.add(card);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(cancelButton);
        bottom.add(demoBox);
        cancelButton.addActionListener(e -> controller.cancel());
        demoBox.addActionListener(e -> controller.setTimeoutSeconds(demoBox.isSelected() ? 10 : 30));
        right.add(bottom);

        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cashArea.setEditable(false);
        historyArea.setEditable(false);
        alertsArea.setEditable(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel cashPanel = new JPanel(new BorderLayout());
        cashPanel.setBorder(BorderFactory.createTitledBorder("Cash levels (change float)"));
        cashPanel.add(new JScrollPane(cashArea));
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("Staff alerts"));
        alertPanel.add(new JScrollPane(alertsArea));
        top.add(cashPanel);
        top.add(alertPanel);
        panel.add(top, BorderLayout.NORTH);

        JPanel history = new JPanel(new BorderLayout());
        history.setBorder(BorderFactory.createTitledBorder(
                "Transaction history (time | drink | method | amount | status | reason)"));
        history.add(new JScrollPane(historyArea));
        panel.add(history, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reset = new JButton("Reset daily total");
        JButton restock = new JButton("Restock all drinks (5 each)");
        reset.addActionListener(e -> controller.resetDailyTotal());
        restock.addActionListener(e -> controller.restockAll());
        buttons.add(totalLabel);
        buttons.add(reset);
        buttons.add(restock);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    /* ---- VendingView ---- */

    @Override
    public void showMessage(String text) {
        messageArea.setText(text);
    }

    @Override
    public void showDispensed(String text) {
        dispensedLabel.setText("Dispenser tray: " + text);
    }

    @Override
    public void showTimer(int secondsLeft) {
        timerLabel.setText(secondsLeft < 0 ? "Timer: -" : "Timer: " + secondsLeft + " s left");
    }

    @Override
    public void refresh() {
        VendingController.State state = controller.getState();

        for (int i = 0; i < drinkButtons.size(); i++) {
            Drink d = controller.getDrinks().get(i);
            int qty = controller.getStock(d);
            drinkButtons.get(i).setText(d.getName() + "  " + Money.format(d.getPrice())
                    + (qty == 0 ? "  [SOLD OUT]" : "  (" + qty + " left)"));
            drinkButtons.get(i).setEnabled(state != VendingController.State.PAYING);
        }

        Drink sel = controller.getSelected();
        selectionLabel.setText("Selected: " + (sel == null ? "-" : sel.getName()));
        priceLabel.setText("Price: " + (sel == null ? "-" : Money.format(controller.getCurrentPrice())
                + (controller.isLoyaltyApplied() ? " (10% loyalty discount)" : "")));
        balanceLabel.setText("Inserted: " + (controller.isCashPayment()
                ? Money.format(controller.getBalance()) : "-"));

        scanButton.setEnabled(state == VendingController.State.DRINK_SELECTED);
        cashButton.setEnabled(state == VendingController.State.DRINK_SELECTED);
        cardButton.setEnabled(state == VendingController.State.DRINK_SELECTED);
        for (JButton b : moneyButtons) {
            b.setEnabled(controller.isCashPayment());
        }
        foreignCoinButton.setEnabled(controller.isCashPayment());
        tapCardButton.setEnabled(controller.isCardPayment());
        cardBox.setEnabled(controller.isCardPayment());
        cancelButton.setEnabled(state != VendingController.State.IDLE);
        demoBox.setEnabled(state != VendingController.State.PAYING);

        // Staff tab
        StringBuilder cash = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : controller.getChanger().getCoinLevels().entrySet()) {
            cash.append(Money.label(e.getKey())).append(": ").append(e.getValue()).append(" coins\n");
        }
        cash.append("Total float: ").append(Money.format(controller.getChanger().getTotal()));
        cashArea.setText(cash.toString());

        StringBuilder history = new StringBuilder();
        for (TransactionRecord r : controller.getLogger().getRecords()) {
            history.append(r).append('\n');
        }
        historyArea.setText(history.toString());

        StringBuilder alerts = new StringBuilder();
        for (String a : controller.getLogger().getStaffAlerts()) {
            alerts.append(a).append('\n');
        }
        alertsArea.setText(alerts.toString());
        totalLabel.setText("Daily total: " + Money.format(controller.getLogger().getDailyTotal()) + "   ");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VendingMachineGUI().setVisible(true));
    }
}