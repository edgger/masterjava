package ru.javaops.masterjava.persist.dao;

import ru.javaops.masterjava.persist.model.BaseEntity;

public abstract class AbstractDao<T extends BaseEntity> implements IDao {

    public T insert(T entity) {
        if (entity.isNew()) {
            int id = insertGeneratedId(entity);
            entity.setId(id);
        } else {
            insertWitId(entity);
        }
        return entity;
    }

    abstract int insertGeneratedId(T entity);

    abstract void insertWitId(T entity);

}
