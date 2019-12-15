package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    public static void main(String[] args) throws IOException, JAXBException {
        String projectName = args[0];

        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());

        Project project = payload.getProjects().getProject().stream()
                .filter(prt -> projectName.equalsIgnoreCase(prt.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(projectName + " not found"));
        List<Group> projectGroups = project.getGroups().getGroup();

        List<User> users = new ArrayList<>();
        for (User user : payload.getUsers().getUser()) {
            user.getGroups().getGroup().stream()
                    .map(JAXBElement::getValue)
                    .filter(Group.class::isInstance)
                    .map(Group.class::cast)
                    .filter(projectGroups::contains)
                    .findFirst()
                    .ifPresent(group -> users.add(user));
        }
        users.forEach(user -> System.out.println(user.getFullName()));
    }
}
