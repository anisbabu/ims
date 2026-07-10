package com.ims.fee;

/**
 * Plug point for hosted-checkout gateways (Stripe, SSLCommerz, bKash, …).
 * A real provider creates a session with the gateway and returns its hosted
 * checkout URL; completion then arrives via the provider's webhook/IPN instead
 * of the payer-confirm endpoint the mock uses.
 */
public interface PaymentGatewayProvider {

    /** Stored on the OnlinePayment row, e.g. "MOCK", "STRIPE". */
    String name();

    /** Where to send the payer to complete this payment. */
    String checkoutUrl(OnlinePayment payment);
}
