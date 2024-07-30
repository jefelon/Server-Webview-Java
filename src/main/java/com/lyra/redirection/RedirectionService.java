package com.lyra.redirection;

import com.google.gson.Gson;
import com.lyra.ServerConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class initializes a payment in the payment platform and returns the generated URL.<p></p>
 *
 * @author Lyra Network
 */
public class RedirectionService {

    public String initPayment(String email, String amount, String currency, String mode, String language, String cardType, String orderId, String configurationSet, boolean isSubscription) {
        String redirectionUrl = null;
        
        // Read configuration
        Map<String, String> configuration = ServerConfiguration.getConfiguration(mode);

        // Create HTTP client
        CloseableHttpClient client = createHttpClient(configuration);
        
        HttpPost post = new HttpPost(String.format("%s/vads-payment/entry.silentInit.a", configuration.get("paymentPlatformUrl")));

        // Create form to send to payment platform
        Integer transactionId = TransactionIdGenerator.generateNewTransactionId();
        List<NameValuePair> formParameters = createFormParameters(transactionId, email, amount, currency, mode, language, cardType, orderId, configuration, isSubscription);

        try {
            post.setEntity(new UrlEncodedFormEntity(formParameters, "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Error in payment initialization", uee);
        }

        // Perform the HTTP call to payment platform
        try (CloseableHttpResponse httpResponse = client.execute(post)) {
            HttpEntity entity = httpResponse.getEntity();
            int httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            Map<String, String> responseData = new Gson().fromJson(EntityUtils.toString(entity, "UTF-8"), Map.class);
            
            if (httpResponseCode == 200) {
                if ("INITIALIZED".equals(responseData.get("status"))) {
                    redirectionUrl = responseData.get("redirect_url");
                } else {
                    throw new RuntimeException("Error in payment initialization. Returned error: " + responseData.get("error"));
                }
            } else {
                throw new RuntimeException("Error in payment initialization. HTTP errorCode: " + httpResponseCode);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in payment initialization", ex);
        }

        return redirectionUrl;
    }

    private static CloseableHttpClient createHttpClient(Map<String, String> configuration) {
        int timeout = Integer.valueOf(configuration.get("httpPostTimeout"));
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        CookieStore httpCookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCookieStore(httpCookieStore)
                .setDefaultRequestConfig(config)
                .build();

        return client;
    }

    private List<NameValuePair> createFormParameters(Integer transactionId, String email, String amount, String currency, String mode, String language, String cardType, String orderId, Map<String, String> configuration, boolean isSubscription) {    
        String merchantSiteId = configuration.get("merchantSiteId");
        String usedMerchantKey = configuration.get("usedMerchantKey");

        List<NameValuePair> formParameters = new ArrayList<>();

        formParameters.add(new BasicNameValuePair("vads_action_mode", "INTERACTIVE"));
        formParameters.add(new BasicNameValuePair("vads_amount", amount));
        formParameters.add(new BasicNameValuePair("vads_ctx_mode", mode));
        formParameters.add(new BasicNameValuePair("vads_currency", currency));
        formParameters.add(new BasicNameValuePair("vads_cust_email", email));
        formParameters.add(new BasicNameValuePair("vads_language", language));
        formParameters.add(new BasicNameValuePair("vads_order_id", orderId));
        if (StringUtils.isNotEmpty(cardType)) {
            formParameters.add(new BasicNameValuePair("vads_payment_cards", cardType.toUpperCase()));
        }
        formParameters.add(new BasicNameValuePair("vads_page_action", isSubscription ? "REGISTER_PAY_SUBSCRIBE" : "PAYMENT"));
        formParameters.add(new BasicNameValuePair("vads_payment_config", "SINGLE"));
        formParameters.add(new BasicNameValuePair("vads_redirect_success_timeout", "5"));
        formParameters.add(new BasicNameValuePair("vads_return_mode", "GET"));
        formParameters.add(new BasicNameValuePair("vads_site_id", merchantSiteId));

        if (isSubscription) {
            formParameters.add(new BasicNameValuePair("vads_sub_amount", amount));
            formParameters.add(new BasicNameValuePair("vads_sub_currency", currency));
            formParameters.add(new BasicNameValuePair("vads_sub_effect_date", calculateDateFormatInUTC("yyyyMMdd")));
            formParameters.add(new BasicNameValuePair("vads_sub_desc", "RRULE:FREQ=WEEKLY;INTERVAL=2"));
        }

        formParameters.add(new BasicNameValuePair("vads_trans_date", calculateDateFormatInUTC("yyyyMMddHHmmss")));
        formParameters.add(new BasicNameValuePair("vads_trans_id", String.format("%06d", transactionId)));

        formParameters.add(new BasicNameValuePair("vads_url_cancel", "https://webview_" + merchantSiteId + ".cancel"));
        formParameters.add(new BasicNameValuePair("vads_url_error", "https://webview_" + merchantSiteId + ".error"));
        formParameters.add(new BasicNameValuePair("vads_url_refused", "https://webview_" + merchantSiteId + ".refused"));
        formParameters.add(new BasicNameValuePair("vads_url_return", "https://webview_" + merchantSiteId + ".return"));
        formParameters.add(new BasicNameValuePair("vads_url_success", "http://webview_" + merchantSiteId + ".success"));

        formParameters.add(new BasicNameValuePair("vads_version", "V2"));

        String concatenateMapParams = "";
        for (NameValuePair pair : formParameters) {
            concatenateMapParams += pair.getValue() + "+";
        }
        concatenateMapParams += usedMerchantKey;

        formParameters.add(new BasicNameValuePair("signature", hmacSha256(concatenateMapParams, usedMerchantKey)));

        return formParameters;
    }

    private static String calculateDateFormatInUTC(String format) {
        return calculateDateFormatInUTC(format, 0);
    }
    
    private static String calculateDateFormatInUTC(String format, int monthsToAdd) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.MONTH, monthsToAdd);
        Date futureDate = calendar.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormatter.format(futureDate);
    }    

    private static String hmacSha256(String input, String key) {
        Mac hmacSha256;
        byte[] inputBytes;
        try {
            inputBytes = input.getBytes("UTF-8");
            try {
                hmacSha256 = Mac.getInstance("HmacSHA256");
            } catch (NoSuchAlgorithmException nsae) {
                hmacSha256 = Mac.getInstance("HMAC-SHA-256");
            }
            SecretKeySpec macKey = new SecretKeySpec(key.getBytes("UTF-8"), "RAW");
            hmacSha256.init(macKey);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(hmacSha256.doFinal(inputBytes));
    }
}

class TransactionIdGenerator {
    private static int counter = new Random().nextInt(500000);
    private static int maxCounter = 999999;

    public static synchronized int generateNewTransactionId() {
        if (counter == (maxCounter - 1))
            counter = 0;

        return counter++;
    }
}