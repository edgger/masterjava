package ru.javaops.masterjava.service.mail.model;

import lombok.*;
import ru.javaops.masterjava.persist.model.BaseEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmailAttempt extends BaseEntity {

    @NonNull
    private String subject;

    @NonNull
    private String body;

    @NonNull
    private boolean success;
}
