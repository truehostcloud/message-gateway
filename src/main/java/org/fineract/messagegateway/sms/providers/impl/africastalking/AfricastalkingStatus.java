package org.fineract.messagegateway.sms.providers.impl.africastalking;

import org.fineract.messagegateway.sms.util.SmsMessageStatusType;

public class AfricastalkingStatus {
    public static SmsMessageStatusType smsStatus(final Integer AfricastalkingStatus) {
        SmsMessageStatusType smsStatus = SmsMessageStatusType.PENDING;
        switch (AfricastalkingStatus) {
            case 100:
                smsStatus = SmsMessageStatusType.PENDING;
                break;
            case 101:
                smsStatus = SmsMessageStatusType.SENT;
                break;
            case 102:
                smsStatus = SmsMessageStatusType.WAITING_FOR_REPORT;
                break;
            case 401:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 402:
                smsStatus = SmsMessageStatusType.INVALID;
                break;
            case 403:
                smsStatus = SmsMessageStatusType.INVALID;
                break;
            case 404:
                smsStatus = SmsMessageStatusType.INVALID;
                break;
            case 405:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 406:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 407:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 409:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 500:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 501:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            case 502:
                smsStatus = SmsMessageStatusType.FAILED;
                break;
            default:
                smsStatus = SmsMessageStatusType.INVALID;
                break;
        }
        return smsStatus;
    }
}
