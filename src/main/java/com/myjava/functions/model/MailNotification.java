package com.myjava.functions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "mail_notification", schema = "webapp")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "mail_verify_link")
    private String mailVerifyLink;

    @Column(name = "is_mail_sent")
    private boolean isMailSent;

    @Column(name = "is_mail_verified")
    private boolean isMailVerified;

    @Column(name = "mail_sent_date")
    private Timestamp mailSentDate;

    @Column(name = "mail_sent_expire_date")
    private Timestamp mailExpireDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMailVerifyLink() {
        return mailVerifyLink;
    }

    public void setMailVerifyLink(String mailVerifyLink) {
        this.mailVerifyLink = mailVerifyLink;
    }

    public boolean isMailSent() {
        return isMailSent;
    }

    public void setMailSent(boolean mailSent) {
        isMailSent = mailSent;
    }

    public Timestamp getMailSentDate() {
        return mailSentDate;
    }

    public void setMailSentDate(Timestamp mailSentDate) {
        this.mailSentDate = mailSentDate;
    }

    public boolean isMailVerified() {
        return isMailVerified;
    }

    public void setMailVerified(boolean mailVerified) {
        isMailVerified = mailVerified;
    }

    public Timestamp getMailExpireDate() {
        return mailExpireDate;
    }

    public void setMailExpireDate(Timestamp mailExpireDate) {
        this.mailExpireDate = mailExpireDate;
    }
}
