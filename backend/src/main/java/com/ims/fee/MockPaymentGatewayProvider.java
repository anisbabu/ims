package com.ims.fee;

import org.springframework.stereotype.Component;

/**
 * Dev/demo gateway: "hosted checkout" is the frontend /pay/{id} page and the
 * payer confirming there plays the role of the gateway webhook.
 */
@Component
public class MockPaymentGatewayProvider implements PaymentGatewayProvider {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public String checkoutUrl(OnlinePayment payment) {
        return "/pay/" + payment.getId();
    }
}
