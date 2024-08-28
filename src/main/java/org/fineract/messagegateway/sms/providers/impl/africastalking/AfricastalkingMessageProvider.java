package org.fineract.messagegateway.sms.providers.impl.africastalking;

import org.fineract.messagegateway.exception.MessageGatewayException;
import org.fineract.messagegateway.sms.domain.SMSBridge;
import org.fineract.messagegateway.sms.domain.SMSMessage;
import org.fineract.messagegateway.sms.providers.SMSProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.africastalking.AfricasTalking;

import java.util.List;

@Service(value = "Africastalking")
public class AfricastalkingMessageProvider extends SMSProvider {
    private static final Logger logger = LoggerFactory.getLogger(AfricastalkingMessageProvider.class);

    @Override
    public void sendMessage(SMSBridge smsBridgeConfig, SMSMessage message) throws MessageGatewayException {
        try {
            AfricasTalking.initialize(smsBridgeConfig.getConfigValue("username"),
                    smsBridgeConfig.getConfigValue("apiKey"));
            SmsService sms = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
            String mobile = smsBridgeConfig.getCountryCode() + message.getMobileNumber();
            List<Recipient> response = sms.send(message.getMessage(), smsBridgeConfig.getPhoneNo(),
                    new String[] { mobile }, true);
            logger.info("SMS sent successfully. Response: {}", response);
            for (Recipient recipient : response) {
                logger.info("Recipient Number: {}, Recipient Status: {}, Recipient Cost: {}, Recipient Message ID: {}",
                        recipient.number, recipient.status, recipient.cost, recipient.messageId);
            }
        } catch (Exception e) {
            logger.error("An error occurred while sending the SMS: {}", e.getMessage(), e);
            throw new MessageGatewayException("Failed to send SMS. Please check the logs for more details.");
        }
    }

    @Override
    public void updateStatusByMessageId(SMSBridge smsBridgeConfig, String messageId) throws MessageGatewayException {
        throw new UnsupportedOperationException("updateStatusByMessageId is not yet implemented for Africastalking");
    }
}
