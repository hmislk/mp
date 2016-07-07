/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;

import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

/**
 *
 * @author User
 */
@Named(value = "emailController")
@SessionScoped
public class EmailController implements Serializable {

    String host = "smtp.gmail.com";

    /**
     * Creates a new instance of EmailController
     */
    public EmailController() {
    }

    public boolean sendEmail(String to, String from, String subject, String html, String userName, String password) {
        try {
            HtmlEmail hemail = new HtmlEmail();
            hemail.setHostName(host);
            hemail.setAuthenticator(new DefaultAuthenticator(userName, password));
            hemail.setSmtpPort(465);
            hemail.setSSLOnConnect(true);
            hemail.addTo(to);
            hemail.setFrom(from);
            hemail.setSubject(subject);
            hemail.setHtmlMsg(html);
            hemail.setTextMsg("Your email client does not support HTML messages");
            hemail.send();
            return true;
        } catch (Exception mex) {
            System.out.println("mex = " + mex);
            System.out.println("to = " + to);
            System.out.println("from = " + from);
            System.out.println("subject = " + subject);
            System.out.println("html = " + html);
            System.out.println("userName = " + userName);
            System.out.println("password = " + password);
            return false;
        }
    }

}
