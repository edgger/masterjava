package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.service.mail.dao.EmailAttemptDao;
import ru.javaops.masterjava.service.mail.dao.EmailCcDao;
import ru.javaops.masterjava.service.mail.dao.EmailToDao;
import ru.javaops.masterjava.service.mail.model.EmailAttempt;
import ru.javaops.masterjava.service.mail.model.EmailCc;
import ru.javaops.masterjava.service.mail.model.EmailTo;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MailSender {

    private static final Config MAIL_CONF = Configs.getConfig("mail.conf", "mail");

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));

        EmailAttempt emailAttempt = new EmailAttempt(subject, body, true);
        try {
            Email email = prepareEmail(to, cc, subject, body);
            email.send();
            log.info("Sent mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));
        } catch (EmailException e) {
            log.error("Failed send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""), e);
            emailAttempt.setSuccess(false);
        }

        saveEmailAttempt(to, cc, emailAttempt);

    }

    private static Email prepareEmail(List<Addressee> to, List<Addressee> cc, String subject, String body) throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName(MAIL_CONF.getString("host"));
        email.setSmtpPort(MAIL_CONF.getInt("port"));
        email.setAuthenticator(new DefaultAuthenticator(MAIL_CONF.getString("username"), MAIL_CONF.getString("password")));
        email.setSSLOnConnect(MAIL_CONF.getBoolean("useSSL"));
        email.setStartTLSEnabled(MAIL_CONF.getBoolean("useTLS"));
        email.setFrom(MAIL_CONF.getString("username"), MAIL_CONF.getString("fromName"));
        email.setDebug(MAIL_CONF.getBoolean("debug"));
        email.setSubject(subject);
        email.setMsg(body);
        for (Addressee addressee : to) {
            email.addTo(addressee.getEmail(), addressee.getName());
        }
        for (Addressee addressee : cc) {
            email.addCc(addressee.getEmail(), addressee.getName());
        }
        return email;
    }

    private static void saveEmailAttempt(List<Addressee> to, List<Addressee> cc, EmailAttempt emailAttempt) {
        EmailAttemptDao emailAttemptDao = DBIProvider.getDao(EmailAttemptDao.class);
        EmailToDao emailToDao = DBIProvider.getDao(EmailToDao.class);
        EmailCcDao emailCcDao = DBIProvider.getDao(EmailCcDao.class);

        DBIProvider.getDBI().useTransaction((conn, status) -> {
            emailAttemptDao.insert(emailAttempt);

            List<EmailTo> emailTos = to.stream()
                    .map(addressee -> new EmailTo(emailAttempt.getId(), addressee.getEmail(), addressee.getName()))
                    .collect(Collectors.toList());
            emailToDao.insertBatch(emailTos);

            List<EmailCc> emailCcs = cc.stream()
                    .map(addressee -> new EmailCc(emailAttempt.getId(), addressee.getEmail(), addressee.getName()))
                    .collect(Collectors.toList());
            emailCcDao.insertBatch(emailCcs);
        });
    }
}
