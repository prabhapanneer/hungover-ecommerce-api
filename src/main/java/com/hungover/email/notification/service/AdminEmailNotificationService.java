package com.hungover.email.notification.service;

import com.hungover.common.constant.ApplicationConstants;
import com.hungover.core.domain.notification.Notification;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Date;

/**
 * Service class for sending email notifications related to customer measurement updates by an admin.
 */
@Service
public class AdminEmailNotificationService {

    private EmailNotificationService emailNotificationService;

    private VelocityEngine velocityEngine;

    @Value("${from.email}")
    private String fromEmail;

    public AdminEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    /**
     * Sends an email notification to the customer when their measurement is updated by an admin.
     *
     * @param customerName  The name of the customer.
     * @param customerEmail The email address of the customer.
     */
    public void sendUpdatedCustomerMeasurementToCustomer(String customerName, String customerEmail) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(ApplicationConstants.Email.CUSTOMER_NAME, customerName);
        Template templateObj = velocityEngine.getTemplate("emailtemplate/customermeasurementaddedbyadmin.vm");
        StringWriter stringWriter = new StringWriter();
        templateObj.merge(velocityContext, stringWriter);
        Notification emailNotification = new Notification(fromEmail, customerEmail,null
                , stringWriter.toString());
        emailNotificationService.sendLoginIssueMailForAdmin(emailNotification);
    }

    /**
     * Sends an email notification to the admin when a customer's measurement is updated.
     *
     * @param customerName  The name of the customer whose measurement was updated.
     * @param adminName     The name of the admin.
     * @param createdDate   The date when the customer's measurement was updated.
     */
    public void sendUpdatedCustomerMeasurementToAdmin(String customerName, String adminName, Date createdDate) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(ApplicationConstants.Email.CUSTOMER_NAME, customerName);
        velocityContext.put(ApplicationConstants.Email.ADMIN_NAME, adminName);
        velocityContext.put(ApplicationConstants.Email.CREATED_DATE, createdDate);
        Template templateObj = velocityEngine.getTemplate("emailtemplate/customermeasurementaddedbyadmin.vm");
        StringWriter stringWriter = new StringWriter();
        templateObj.merge(velocityContext, stringWriter);
        Notification emailNotification = new Notification(fromEmail, fromEmail,
                ApplicationConstants.EmailSubject.CUSTOMER_MEASUREMENT_UPDATES, stringWriter.toString());
        emailNotificationService.sendLoginIssueMailForAdmin(emailNotification);
    }
}
