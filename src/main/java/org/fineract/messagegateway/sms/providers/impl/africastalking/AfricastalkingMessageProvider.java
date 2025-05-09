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

    @Override
    public void sendMessage(SMSBridge smsBridgeConfig, SMSMessage message) throws MessageGatewayException {
        try {
            OkHttpClient client = new OkHttpClient();
            String username = smsBridgeConfig.getConfigValue("username");
            String mobile = smsBridgeConfig.getCountryCode() + message.getMobileNumber();

            if (username.equals("sandbox")) {
                sendMessageSandbox(client, smsBridgeConfig, message, mobile);
            } else {
                sendMessageLive(client, smsBridgeConfig, message, mobile);
            }
        } catch (IOException e) {
            logger.error("An error occurred while sending the SMS: {}", e.getMessage(), e);
            throw new MessageGatewayException("Failed to send SMS. Please check the logs for more details.");
        }
    }

    private void sendMessageSandbox(OkHttpClient client, SMSBridge smsBridgeConfig, SMSMessage message, String mobile) throws IOException, MessageGatewayException {
        String from = smsBridgeConfig.getPhoneNo();
        logger.info("Sending SMS via sandbox from: {}", from);
        if (from.equals("AFRICASTKNG")) {
            from = null;
        }

        String url = "https://api.sandbox.africastalking.com/version1/messaging";

        FormBody.Builder requestBodyBuilder = new FormBody.Builder().add("username", smsBridgeConfig.getConfigValue("username")).add("to", mobile).add("message", message.getMessage()).add("bulkSMSMode", "1").add("enqueue", "0");

        if (from != null) {
            requestBodyBuilder.add("from", from);
        }

        FormBody requestBody = requestBodyBuilder.build();

        Request request = new Request.Builder().url(url).addHeader("apiKey", smsBridgeConfig.getConfigValue("apiKey")).addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("Accept", "application/json").post(requestBody).build();

        logger.info("Request URL: {}", url);
        logger.info("Request Headers: Content-Type: application/x-www-form-urlencoded, Accept: application/json");
        logger.info("Request Payload: username={}, to={}, message={}, bulkSMSMode=1, enqueue=0{}", 
            smsBridgeConfig.getConfigValue("username"), mobile, message.getMessage(), 
            from != null ? ", from=" + from : "");

        processResponse(client.newCall(request).execute(), message);
    }

    private void sendMessageLive(OkHttpClient client, SMSBridge smsBridgeConfig, SMSMessage message, String mobile) throws IOException, MessageGatewayException {
        String senderId = smsBridgeConfig.getConfigValue("senderId");
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new MessageGatewayException("senderId configuration is required for live environment");
        }

        logger.info("Sending SMS via live API with senderId: {}", senderId);

        String url = "https://api.africastalking.com/version1/messaging/bulk";

        JSONObject requestBody = new JSONObject().put("username", smsBridgeConfig.getConfigValue("username")).put("phoneNumbers", Collections.singletonList(mobile)).put("message", message.getMessage()).put("senderId", senderId);

        RequestBody body = RequestBody.create(JSON, requestBody.toString());

        Request request = new Request.Builder().url(url).addHeader("apiKey", smsBridgeConfig.getConfigValue("apiKey")).addHeader("Content-Type", "application/json").addHeader("Accept", "application/json").post(body).build();
        processResponse(client.newCall(request).execute(), message);
    }

    private void processResponse(Response response, SMSMessage message) throws IOException, MessageGatewayException {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new MessageGatewayException("Empty response received from Africa's Talking API");
        }

        String responseString = responseBody.string();
        if (response.isSuccessful()) {
            logger.info("SMS sent successfully. Response: {}", responseString);

            // Parse the response body
            JSONObject responseJson = new JSONObject(responseString);
            JSONObject smsMessageData = responseJson.getJSONObject("SMSMessageData");
            JSONArray recipients = smsMessageData.getJSONArray("Recipients");

            if (!recipients.isEmpty()) {
                JSONObject recipient = recipients.getJSONObject(0);

                // Update the message with external ID and delivery status
                String messageId = recipient.getString("messageId");
                int statusCode = recipient.getInt("statusCode");
                logger.info("Africa's Talking API response - MessageId: {}, StatusCode: {}", messageId, statusCode);
                SmsMessageStatusType deliveryStatus = AfricastalkingStatus.smsStatus(statusCode);
                logger.info("Mapped delivery status: {}", deliveryStatus);

                message.setExternalId(messageId);
                message.setDeliveryStatus(deliveryStatus.getValue());
            } else {
                String errorMessage = smsMessageData.getString("Message");
                logger.error("Failed to send SMS. Error message: {}", errorMessage);
                throw new MessageGatewayException("Failed to send SMS. Error message: " + errorMessage);
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
