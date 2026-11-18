/** Implemented by the GUI so the controller never depends on Swing components. */
interface VendingView {
    void showMessage(String text);

    void showDispensed(String text);

    void showTimer(int secondsLeft);

    void refresh();
}
