package com.hungover.email.notification.service;

import com.hungover.common.apiresponse.SingleDataResponse;
import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.domain.notification.Notification;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.SendGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for sending email notifications using the SendGrid API.
 */
@Service
public class EmailNotificationService {
    private static final Logger emailNotificationServiceLogger = LoggerFactory.
            getLogger(EmailNotificationService.class);
    private static final String Text_Html = "text/html";
    private static final String MailSentSuccessfully = "Mail Sent Successfully";
    private static final String ResponseMessage = "responseMessage";
    @Value("${sendgrid.apikey}")
    private String sendgridApiKey;

    /**
     * Send login issue mail to admin.
     *
     * @param emailNotification The email notification object containing the necessary details.
     * @return SingleDataResponse containing the status of the mail sent.
     */
    public SingleDataResponse sendLoginIssueMailForAdmin(Notification emailNotification) {
        SingleDataResponse singleDataResponse = new SingleDataResponse();
        Notification notification = new Notification(emailNotification.getFrom(), emailNotification.getTo(),
                emailNotification.getSubject(), emailNotification.getMessage());
        Email fromEmail = new Email(notification.getFrom());
        fromEmail.setName("Hungover");
        String subject = notification.getSubject();
        Content content = new Content(Text_Html, notification.getMessage());
        Email toEmail = new Email(notification.getTo());
        Mail mail = new Mail(fromEmail, subject, toEmail, content);
        com.sendgrid.Response mailResponse = sendMail(mail, sendgridApiKey);
        emailNotificationServiceLogger
                .info("Email Notification Status Code:::::::::::::::::" + mailResponse.getStatusCode());
        Map<String, String> object = new HashMap<>();
        if ((mailResponse.getStatusCode() == 200) || (mailResponse.getStatusCode() == 202)) {
            object.put(ResponseMessage, MailSentSuccessfully);
            singleDataResponse.setResponse(ApplicationConstants.Status.SUCCESS,
                    "Mail Sent Successfully", null);
        } else {
            emailNotificationServiceLogger.info("Exception while sending email:::::::::::::::::::::::::::::::::::");
        }
        return singleDataResponse;
    }
    private com.sendgrid.Response sendMail(Mail mail, String apiKey) {
        com.sendgrid.Response response = null;
        try {
            long currentTime = System.currentTimeMillis();
            emailNotificationServiceLogger.info("" + currentTime);
            long sentMailAtTime = currentTime;
            long unixTime = sentMailAtTime / 1000L;
            emailNotificationServiceLogger.info("" + unixTime);
            com.sendgrid.Request request = new com.sendgrid.Request();
            mail.setSendAt(unixTime);
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            SendGrid sg = new SendGrid(apiKey);
            response = sg.api(request);
            return response;
        } catch (IOException ex) {
            return new com.sendgrid.Response(500, "Error sending email", null);
        }
    }
}
