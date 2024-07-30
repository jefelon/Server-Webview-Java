package com.lyra.redirection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * Example controller that handles a redirection payment that will be displayed inside a Webview.<p></p>
 *
 * This controller just extracts parameters from Request and sends them to the service component. This should
 * return a prepared URL to browse into the WebView<p></p>
 *
 * For readability purposes in this example:
 * <li>We do not use logs</li>
 * <li>The JSON content is converted into a basic map structure. Use an appropriate DTO class hierarchy instead
 * if you want to provide a more scalable and robust code</li>
 *
 * @autor Lyra Network
 */
public class RedirectionController {

    private static final Gson gson = new Gson();

    public static Route createPayment = (request, response) -> {
        response.type("application/json; charset=utf-8"); // Always return a JSON response

        // Retrieve and validate parameters from request payload
        PaymentRequest paymentRequest = null;
        try {
            paymentRequest = gson.fromJson(request.body(), PaymentRequest.class);
            validatePaymentRequest(paymentRequest);
        } catch (JsonSyntaxException jse) {
            response.status(400);
            return toJSONError("Bad Request: Invalid JSON");
        } catch (IllegalArgumentException iae) {
            response.status(400);
            return toJSONError("Bad Request: " + iae.getMessage());
        }

        // Retrieve payment URL
        String redirectionUrl = null;
        try {
            redirectionUrl = new RedirectionService().initPayment(
                    paymentRequest.getEmail(),
                    paymentRequest.getAmount(),
                    paymentRequest.getCurrency(),
                    paymentRequest.getMode(),
                    paymentRequest.getLanguage(),
                    paymentRequest.getCardType(),
                    paymentRequest.getOrderId(),
                    paymentRequest.getConfigurationSet(),
                    "REGISTER_PAY_SUBSCRIBE".equals(paymentRequest.getPageAction())
            );
        } catch (Exception e) {
            response.status(500);
            return toJSONError("Internal Server Error: " + e.getMessage());
        }

        // Return response data with the URL in JSON format
        return toJSONOk(redirectionUrl);
    };

    /*
     * Validate payment request data
     */
    private static void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest.getEmail() == null || paymentRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (paymentRequest.getCurrency() == null || paymentRequest.getCurrency().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (paymentRequest.getMode() == null || paymentRequest.getMode().isEmpty()) {
            throw new IllegalArgumentException("Mode is required");
        }
        if (paymentRequest.getLanguage() == null || paymentRequest.getLanguage().isEmpty()) {
            throw new IllegalArgumentException("Language is required");
        }
       /* if (paymentRequest.getCardType() == null || paymentRequest.getCardType().isEmpty()) {
            throw new IllegalArgumentException("Card Type is required");
        }*/
        if (paymentRequest.getOrderId() == null || paymentRequest.getOrderId().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
    }

    /*
     * Static example methods to build a JSON response for the client
     */
    private static String toJSONOk(String redirectionUrl) {
        return responseToJSON("OK", "", redirectionUrl);
    }

    private static String toJSONError(String message) {
        return responseToJSON("ERROR", message, "");
    }

    private static String responseToJSON(String status, String errorMessage, String redirectionUrl) {
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", status);
        responseObject.addProperty("errorMessage", errorMessage);
        responseObject.addProperty("redirectionUrl", redirectionUrl);

        return responseObject.toString();
    }

    /**
     * DTO for payment request data
     */
    private static class PaymentRequest {
        private String email;
        private String amount;
        private String currency;
        private String mode;
        private String language;
        private String cardType;
        private String orderId;
        private String configurationSet;
        private String pageAction;

        // Getters
        public String getEmail() { return email; }
        public String getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getMode() { return mode; }
        public String getLanguage() { return language; }
        public String getCardType() { return cardType; }
        public String getOrderId() { return orderId; }
        public String getConfigurationSet() { return configurationSet; }
        public String getPageAction() { return pageAction; }
    }
}