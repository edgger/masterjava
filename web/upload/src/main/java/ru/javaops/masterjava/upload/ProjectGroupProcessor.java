package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.persist.model.type.GroupType;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class ProjectGroupProcessor {
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);

    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public Map<String, Group> process(StaxStreamProcessor processor) throws XMLStreamException, JAXBException {
        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();

        Map<String, Project> currentProjects = projectDao.getAsMap();

        while (processor.startElement("Project", "Projects")) {
            ru.javaops.masterjava.xml.schema.Project payloadProject = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.Project.class);
            if (!currentProjects.containsKey(payloadProject.getName())) {
                Project project = new Project(payloadProject.getName(), payloadProject.getDescription());
                DBIProvider.getDBI().useTransaction((conn, status) -> {
                    projectDao.insert(project);
                    ArrayList<Group> groups = new ArrayList<>();
                    for (ru.javaops.masterjava.xml.schema.Project.Group payloadGroup : payloadProject.getGroup()) {
                        Group group = new Group(payloadGroup.getName(), GroupType.valueOf(payloadGroup.getType().name()), project.getId());
                        groups.add(group);
                    }
                    groupDao.insertBatch(groups);
                });
            }
        }

        return groupDao.getAsMap();
    }
}
