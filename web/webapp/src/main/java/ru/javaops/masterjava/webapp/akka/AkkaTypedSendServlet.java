package ru.javaops.masterjava.webapp.akka;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailRemoteService;
import ru.javaops.masterjava.service.mail.util.MailUtils.MailObject;
import ru.javaops.masterjava.webapp.WebUtil;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = "/sendAkkaTyped", loadOnStartup = 1, asyncSupported = true)
@Slf4j
@MultipartConfig
public class AkkaTypedSendServlet extends HttpServlet {

    private MailRemoteService mailService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        mailService = AkkaWebappListener.akkaActivator
                .getTypedRef(MailRemoteService.class, "akka.tcp://MailService@127.0.0.1:2553/user/mail-remote-service");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            log.info("Start asynchronous processing");

            MailObject mailObject = WebUtil.createMailObject(req);

            AsyncContext ac = req.startAsync();

            // https://dzone.com/articles/limited-usefulness
            ac.start(() -> {
                try {
                    String result = sendMail(mailObject);
                    resp.getWriter().write(result);
                    ac.complete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            log.info("Asynchronous processing running ...");
        } catch (Exception e) {
            log.error("Asynchronous processing failed", e);
            String message = e.getMessage();
            String result = (message != null) ? message : e.getClass().getName();
            resp.getWriter().write(result);
        }
    }

    private String sendMail(MailObject mailObject) {
        log.info("Start sending");
        String result;
        try {
            log.info("Start processing");

            scala.concurrent.Future<GroupResult> future = mailService.sendBulk(mailObject);
            log.info("Receive future, await result ...");
            GroupResult groupResult = Await.result(future, Duration.create(10, "seconds"));
            result = groupResult.toString();

            log.info("Processing finished with result: {}", result);
        } catch (Exception e) {
            log.error("Processing failed", e);
            String message = e.getMessage();
            result = (message != null) ? message : e.getClass().getName();
        }
        return result;
    }
}