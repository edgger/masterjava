package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupStatus;

import java.util.List;

public class GroupTestData {
    public static Group TJ06;
    public static Group TJ07;
    public static Group TJ08;
    public static Group MJ01;
    public static List<Group> GROUPS;

    public static void init() {
        TJ06 = new Group("topjava06", GroupStatus.FINISHED, null);
        TJ07 = new Group("topjava07", GroupStatus.FINISHED, null);
        TJ08 = new Group("topjava08", GroupStatus.CURRENT, null);
        MJ01 = new Group("masterjava01", GroupStatus.CURRENT, null);
        GROUPS = ImmutableList.of(MJ01, TJ06, TJ07);
    }

    public static void setUp() {
        GroupDao dao = DBIProvider.getDao(GroupDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            GROUPS.forEach(dao::insert);
            dao.insert(TJ08);
        });
    }
}
