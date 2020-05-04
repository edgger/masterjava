package ru.javaops.masterjava.service.mail.rest;


import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotBlank;
import ru.javaops.masterjava.service.mail.Attachment;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailServiceExecutor;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.masterjava.web.WebStateException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Path("/")
public class MailRS {
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GroupResult send(@NotBlank @FormDataParam("users") String users,
                            @FormDataParam("subject") String subject,
                            @NotBlank @FormDataParam("body") String body,
                            @FormDataParam("attach") FormDataBodyPart attachPart) throws WebStateException {
        List<Attachment> attachments;
        if (attachPart == null) {
            attachments = Collections.emptyList();
        } else {
            BodyPartEntity attachEntity = attachPart.getEntityAs(BodyPartEntity.class);
            String fileName = attachPart.getContentDisposition().getFileName();
            attachments = Collections.singletonList(Attachments.getAttachment(fileName, attachEntity.getInputStream()));
        }
        return MailServiceExecutor.sendBulk(MailWSClient.split(users), subject, body, attachments);
    }
}