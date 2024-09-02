package org.fineract.messagegateway.sms.providers.impl.africastalking;

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

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Service(value = "Africastalking")
public class AfricastalkingMessageProvider extends SMSProvider {
    private static final Logger logger = LoggerFactory.getLogger(AfricastalkingMessageProvider.class);

    @Override
    public void sendMessage(SMSBridge smsBridgeConfig, SMSMessage message) throws MessageGatewayException {
        try {
            OkHttpClient client = new OkHttpClient();
            String from = smsBridgeConfig.getPhoneNo();
            logger.info("Sending SMS from: {}", from);
            if (from.equals("AFRICASTKNG")) {
                from = null;
            }
            String username = smsBridgeConfig.getConfigValue("username");
            String url = "https://api.africastalking.com/version1/messaging";
            if (username.equals("sandbox")) {
                url = "https://api.sandbox.africastalking.com/version1/messaging";
            }
            String mobile = smsBridgeConfig.getCountryCode() + message.getMobileNumber();
            FormBody.Builder requestBodyBuilder = new FormBody.Builder()
                    .add("username", username)
                    .add("to", mobile)
                    .add("message", message.getMessage())
                    .add("bulkSMSMode", "1")
                    .add("enqueue", "0");

            if (from != null) {
                requestBodyBuilder.add("from", from);
            }

            FormBody requestBody = requestBodyBuilder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apiKey", smsBridgeConfig.getConfigValue("apiKey"))
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "application/json")
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                logger.info("SMS sent successfully. Response: {}", responseBody);

                // Parse the response body
                JSONObject responseJson = new JSONObject(responseBody);
                JSONObject smsMessageData = responseJson.getJSONObject("SMSMessageData");
                JSONArray recipients = smsMessageData.getJSONArray("Recipients");

                if (recipients.length() > 0) {
                    JSONObject recipient = recipients.getJSONObject(0);

                    // Update the message with external ID and delivery status
                    String messageId = recipient.getString("messageId");
                    int statusCode = recipient.getInt("statusCode");
                    SmsMessageStatusType deliveryStatus = AfricastalkingStatus.smsStatus(statusCode);

                    message.setExternalId(messageId);
                    message.setDeliveryStatus(deliveryStatus.getValue());
                } else {
                    String errorMessage = smsMessageData.getString("Message");
                    logger.error("Failed to send SMS. Error message: {}", errorMessage);
                    throw new MessageGatewayException("Failed to send SMS. Error message: " + errorMessage);
                }
            } else {
                logger.error("Failed to send SMS. Response code: {}, Response body: {}", response.code(),
                        response.body().string());
                throw new MessageGatewayException("Failed to send SMS. Please check the logs for more details.");
            }
        } catch (IOException e) {
            logger.error("An error occurred while sending the SMS: {}", e.getMessage(), e);
            throw new MessageGatewayException("Failed to send SMS. Please check the logs for more details.");
        }
    }

    @Override
    public void updateStatusByMessageId(SMSBridge smsBridgeConfig, String messageId) throws MessageGatewayException {
    }
}
