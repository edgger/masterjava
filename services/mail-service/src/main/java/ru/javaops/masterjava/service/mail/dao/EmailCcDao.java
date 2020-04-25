package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.dao.AbstractDao;
import ru.javaops.masterjava.service.mail.model.EmailCc;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class EmailCcDao implements AbstractDao {

    @SqlUpdate("TRUNCATE email_attempt_ccs")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM email_attempt_ccs")
    public abstract List<EmailCc> getAll();

    @SqlUpdate("INSERT INTO email_attempt_ccs (email_attempt_id, target, name) VALUES (:emailAttemptId, :target, :name)")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean EmailCc to);

    @SqlBatch("INSERT INTO email_attempt_ccs (email_attempt_id, target, name) VALUES (:emailAttemptId, :target, :name)")
    public abstract int[] insertBatch(@BindBean List<EmailCc> tos);
}
