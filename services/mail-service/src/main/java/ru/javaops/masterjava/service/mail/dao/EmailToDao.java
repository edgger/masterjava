package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.dao.AbstractDao;
import ru.javaops.masterjava.service.mail.model.EmailTo;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class EmailToDao implements AbstractDao {

    @SqlUpdate("TRUNCATE email_attempt_tos")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM email_attempt_tos")
    public abstract List<EmailTo> getAll();

    @SqlUpdate("INSERT INTO email_attempt_tos (email_attempt_id, target, name) VALUES (:emailAttemptId, :target, :name)")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean EmailTo to);

    @SqlBatch("INSERT INTO email_attempt_tos (email_attempt_id, target, name) VALUES (:emailAttemptId, :target, :name)")
    public abstract int[] insertBatch(@BindBean List<EmailTo> tos);
}
