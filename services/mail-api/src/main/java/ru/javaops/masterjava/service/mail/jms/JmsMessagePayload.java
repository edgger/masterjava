package ru.javaops.masterjava.service.mail.jms;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class JmsMessagePayload implements Serializable {
    private String users;
    private String subject;
    private String body;
}
