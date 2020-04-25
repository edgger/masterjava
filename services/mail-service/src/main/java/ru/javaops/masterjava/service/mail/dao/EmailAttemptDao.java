package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.dao.AbstractDao;
import ru.javaops.masterjava.service.mail.model.EmailAttempt;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class EmailAttemptDao implements AbstractDao {

    @SqlUpdate("TRUNCATE email_attempts CASCADE ")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM email_attempts")
    public abstract List<EmailAttempt> getAll();

    @SqlUpdate("INSERT INTO email_attempts (subject, body, success)  VALUES (:subject, :body, :success)")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean EmailAttempt emailAttempt);

    public void insert(EmailAttempt emailAttempt) {
        int id = insertGeneratedId(emailAttempt);
        emailAttempt.setId(id);
    }
}
