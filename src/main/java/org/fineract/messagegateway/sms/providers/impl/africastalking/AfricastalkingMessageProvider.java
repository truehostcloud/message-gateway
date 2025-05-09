package org.fineract.messagegateway.sms.providers.impl.africastalking;

import okhttp3.*;
import org.fineract.messagegateway.exception.MessageGatewayException;
import org.fineract.messagegateway.sms.domain.SMSBridge;
import org.fineract.messagegateway.sms.domain.SMSMessage;
import org.fineract.messagegateway.sms.providers.SMSProvider;
import org.fineract.messagegateway.sms.util.SmsMessageStatusType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service(value = "Africastalking")
public class AfricastalkingMessageProvider extends SMSProvider {
    private static final Logger logger = LoggerFactory.getLogger(AfricastalkingMessageProvider.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SANDBOX_API_URL = "https://api.sandbox.africastalking.com/version1/messaging";
    private static final String LIVE_API_URL = "https://api.africastalking.com/version1/messaging/bulk";

    @Override
    public void sendMessage(SMSBridge smsBridgeConfig, SMSMessage message) throws MessageGatewayException {
        try {
            OkHttpClient client = new OkHttpClient();
            String username = smsBridgeConfig.getConfigValue("username");
            String mobile = smsBridgeConfig.getCountryCode() + message.getMobileNumber();
            String apiKey = smsBridgeConfig.getConfigValue("apiKey");

            if (username.equals("sandbox")) {
                sendMessageSandbox(client, smsBridgeConfig, message, mobile, apiKey);
            } else {
                sendMessageLive(client, smsBridgeConfig, message, mobile, apiKey);
            }
        } catch (IOException e) {
            logger.error("An error occurred while sending the SMS: {}", e.getMessage(), e);
            throw new MessageGatewayException("Failed to send SMS. Please check the logs for more details.");
        }
    }

    private void sendMessageSandbox(OkHttpClient client, SMSBridge smsBridgeConfig, SMSMessage message, String mobile, String apiKey) throws IOException, MessageGatewayException {
        String from = smsBridgeConfig.getPhoneNo();
        logger.info("Sending SMS via sandbox from: {}", from);
        if (from.equals("AFRICASTKNG")) {
            from = null;
        }

        FormBody.Builder requestBodyBuilder = new FormBody.Builder()
                .add("username", smsBridgeConfig.getConfigValue("username"))
                .add("to", mobile)
                .add("message", message.getMessage())
                .add("bulkSMSMode", "1")
                .add("enqueue", "0");

        if (from != null) {
            requestBodyBuilder.add("from", from);
        }

        FormBody requestBody = requestBodyBuilder.build();

        // Log request details
        logRequestDetails(SANDBOX_API_URL, "application/x-www-form-urlencoded",
                String.format("username=%s, to=%s, message=%s, bulkSMSMode=1, enqueue=0%s",
                        smsBridgeConfig.getConfigValue("username"), mobile, message.getMessage(),
                        from != null ? ", from=" + from : ""));

        // Build and execute request
        Request request = buildRequest(SANDBOX_API_URL, apiKey, "application/x-www-form-urlencoded", requestBody);
        executeRequest(client, request, message);
    }

    private void sendMessageLive(OkHttpClient client, SMSBridge smsBridgeConfig, SMSMessage message, String mobile, String apiKey) throws IOException, MessageGatewayException {
        String senderId = smsBridgeConfig.getConfigValue("senderId");
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new MessageGatewayException("senderId configuration is required for live environment");
        }

        logger.info("Sending SMS via live API with senderId: {}", senderId);

        JSONObject jsonBody = new JSONObject()
                .put("username", smsBridgeConfig.getConfigValue("username"))
                .put("phoneNumbers", Collections.singletonList(mobile))
                .put("message", message.getMessage())
                .put("senderId", senderId);

        RequestBody requestBody = RequestBody.create(JSON, jsonBody.toString());

        // Log request details
        logRequestDetails(LIVE_API_URL, "application/json", jsonBody.toString());

        // Build and execute request
        Request request = buildRequest(LIVE_API_URL, apiKey, "application/json", requestBody);
        executeRequest(client, request, message);
    }

    /**
     * Builds the HTTP request with appropriate headers
     */
    private Request buildRequest(String url, String apiKey, String contentType, RequestBody requestBody) {
        return new Request.Builder()
                .url(url)
                .addHeader("apiKey", apiKey)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build();
    }

    /**
     * Logs request details for debugging
     */
    private void logRequestDetails(String url, String contentType, String payload) {
        logger.info("Request URL: {}", url);
        logger.info("Request Headers: Content-Type: {}, Accept: application/json", contentType);
        logger.info("Request Payload: {}", payload);
    }

    /**
     * Executes the request and processes the response
     */
    private void executeRequest(OkHttpClient client, Request request, SMSMessage message) throws IOException, MessageGatewayException {
        Response response = client.newCall(request).execute();
        processResponse(response, message);
    }

    private void processResponse(Response response, SMSMessage message) throws IOException, MessageGatewayException {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new MessageGatewayException("Empty response received from Africa's Talking API");
        }

        String responseString = responseBody.string();
        if (response.isSuccessful()) {
            logger.info("SMS sent successfully. Response: {}", responseString);

            JSONObject responseJson = new JSONObject(responseString);
            JSONObject smsMessageData = responseJson.getJSONObject("SMSMessageData");

            if (smsMessageData.has("Recipients") && !smsMessageData.isNull("Recipients")) {
                JSONArray recipients = smsMessageData.getJSONArray("Recipients");

                if (!recipients.isEmpty()) {
                    JSONObject recipient = recipients.getJSONObject(0);

                    String messageId = recipient.getString("messageId");
                    int statusCode = recipient.getInt("statusCode");
                    logger.info("Africa's Talking API response - MessageId: {}, StatusCode: {}", messageId, statusCode);
                    SmsMessageStatusType deliveryStatus = AfricastalkingStatus.smsStatus(statusCode);
                    logger.info("Mapped delivery status: {}", deliveryStatus);

                    message.setExternalId(messageId);
                    message.setDeliveryStatus(deliveryStatus.getValue());
                } else {
                    String errorMessage = smsMessageData.getString("Message");
                    logger.error("Failed to send SMS. Empty recipients array. Error message: {}", errorMessage);
                    throw new MessageGatewayException("Failed to send SMS. Empty recipients array. Error message: " + errorMessage);
                }
            } else {
                String errorMessage = smsMessageData.has("Message") ? smsMessageData.getString("Message") : "No Recipients array in response";
                logger.error("Failed to send SMS. Missing Recipients data. Error message: {}", errorMessage);
                throw new MessageGatewayException("Failed to send SMS. Missing Recipients data. Error message: " + errorMessage);
            }
        } else {
            logger.error("Failed to send SMS. Response code: {}, Response body: {}", response.code(), responseString);
            throw new MessageGatewayException("Failed to send SMS. Response code: " + response.code() + ", Response body: " + responseString);
        }
    }

    @Override
    public void updateStatusByMessageId(SMSBridge smsBridgeConfig, String messageId) throws MessageGatewayException {
    }
}
