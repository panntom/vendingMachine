/** Implemented by the VendingController; PaymentMethods report their result through it. */
interface PaymentListener {
    void onPaymentComplete(String detail);

    void onPaymentFailed(FailureReason reason, String detail);
}
