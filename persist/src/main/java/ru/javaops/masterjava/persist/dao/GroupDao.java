package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao extends AbstractDao<Group> {

    @SqlUpdate("INSERT INTO groups (name, status, project_id) VALUES (:name, CAST(:status AS GROUP_STATUS), :projectId) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean Group group);

    @SqlUpdate("INSERT INTO groups (id, name, status, project_id) VALUES (:id, :name, CAST(:status AS GROUP_STATUS), :projectId) ")
    abstract void insertWitId(@BindBean Group group);

    @SqlQuery("SELECT * FROM groups ORDER BY name LIMIT :it")
    public abstract List<Group> getWithLimit(@Bind int limit);

    @SqlUpdate("TRUNCATE groups CASCADE")
    @Override
    public abstract void clean();
}
