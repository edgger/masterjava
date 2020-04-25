package ru.javaops.masterjava.service.mail.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;
import ru.javaops.masterjava.persist.model.BaseEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmailCc extends BaseEntity {

    @NonNull
    @Column("email_attempt_id")
    private Integer emailAttemptId;

    @NonNull
    private String target;

    private String name;
}
