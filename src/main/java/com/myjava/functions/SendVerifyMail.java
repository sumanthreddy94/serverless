package com.myjava.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.CloudEventsFunction;
import com.myjava.functions.model.MailNotification;
import com.sendgrid.*;
import io.cloudevents.CloudEvent;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.Properties;

public class SendVerifyMail implements CloudEventsFunction {

    private SessionFactory sessionFactory =null;
    @Override
    public void accept(CloudEvent cloudEvent) {
        String data = new String(cloudEvent.getData().toBytes());
        JSONObject message = new JSONObject(data);
        JSONObject messageData = message.getJSONObject("message");
        String encodedData = messageData.getString("data");
        String json = new String(Base64.getDecoder().decode(encodedData));
        ObjectMapper mapper = new ObjectMapper();
        try {
            MailNotification mailNotification = mapper.readValue(json, MailNotification.class);
            System.out.println("Username: "+ mailNotification.getUsername());
            int statusCode = sendEmail(mailNotification);
            if(statusCode == 202) {
                if(sessionFactory==null) {
                    System.out.println("CREATE SESSION FACTORY");
                    sessionFactory = getSessionFactory();
                }
                saveRecord(sessionFactory, mailNotification);
                System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            } else {
                throw new RuntimeException("Unable to send email");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private SessionFactory getSessionFactory() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        Properties properties =   configuration.getProperties();
        String user = System.getenv("MYSQL_APP_USER");
        String password = System.getenv("MYSQL_APP_PASSWORD");
        String host = System.getenv("MYSQL_APP_HOST");
        properties.setProperty("hibernate.connection.url", host);
        properties.setProperty("hibernate.connection.username", user);
        properties.setProperty("hibernate.connection.password", password);
        return configuration.buildSessionFactory();
    }

    private int sendEmail(MailNotification mailNotification) {
        String dns = System.getenv("DNS_NAME");
        String sendGridAPIKey= "RS";
        String verifyLink = "https://"+dns.substring(0, dns.length() - 1)+"/v1/users/verifyEmail/"+mailNotification.getUsername();
        String htmlContent = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<title>Please Verify your Webapp Account</title>"
                + "</head>"
                + "<body>"
                + "<p>Please send verification request in browser: "+ verifyLink+" </p>"
                + "</body>"
                + "</html>";
        mailNotification.setMailVerifyLink(verifyLink);
        Email from = new Email("noreply@northeasternboston.org");
        String subject = "Webapp User verification";
        Email to = new Email(mailNotification.getUsername());
        Content content = new Content("text/html",htmlContent);
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendGridAPIKey);
        Request request = new Request();
        int statusCode = 1;
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            statusCode = response.getStatusCode();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to Send Mail: "+ex.getMessage());
        }
        return statusCode;
    }

    private void saveRecord(SessionFactory sessionFactory, MailNotification mailNotification) {
        mailNotification.setMailSent(true);
        try (Session session = sessionFactory.openSession();) {
            session.beginTransaction();
            Criteria criteria = session.createCriteria(MailNotification.class);
            criteria.add(Restrictions.eq("username", mailNotification.getUsername()));
            criteria.setMaxResults(1);
            MailNotification savedMailNotification = (MailNotification) criteria.uniqueResult();
            savedMailNotification.setMailSent(true);
            savedMailNotification.setMailVerifyLink(mailNotification.getMailVerifyLink());
            savedMailNotification.setMailVerified(false);

            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            savedMailNotification.setMailSentDate(currentTimestamp);
            long twoMinutesInMillis = 2 * 60 * 1000; // 2 minutes in milliseconds
            Timestamp twoMinutesAhead = new Timestamp(currentTimestamp.getTime() + twoMinutesInMillis + 3000);
            savedMailNotification.setMailExpireDate(twoMinutesAhead);
            session.update(savedMailNotification);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("unable to save record: "+e.getMessage());
        }
    }
}