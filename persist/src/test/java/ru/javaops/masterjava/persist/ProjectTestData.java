package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

public class ProjectTestData {
    public static Project MASTER;
    public static Project TOP;
    public static List<Project> PROJECTS;

    public static void init() {
        MASTER = new Project("masterjava", "Masterjava");
        TOP = new Project("topjava", "Topjava");
        PROJECTS = ImmutableList.of(MASTER, TOP);
    }

    public static void setUp() {
        ProjectDao dao = DBIProvider.getDao(ProjectDao.class);
        dao.clean();
        dao.insert(MASTER);
        dao.insert(TOP);
    }
}
