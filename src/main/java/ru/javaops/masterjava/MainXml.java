package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    public static void main(String[] args) throws IOException, JAXBException, XMLStreamException {
        String projectNameArg = args[0];

//        parseByJaxb(projectName);
        parseByStax(projectNameArg);
    }

    private static void parseByStax(String projectNameArg) throws XMLStreamException, IOException {
        List<Object[]> users = new ArrayList<>(); //laziness
        List<String> groupIds = new ArrayList<>();
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLStreamReader reader = processor.getReader();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("Users".equals(reader.getLocalName())) {

                        readUsers(users, reader);
                    }
                    if ("Projects".equals(reader.getLocalName())) {
                        findProjectGroupIds(projectNameArg, groupIds, reader);
                    }
                }
            }
        }
        users.stream()
                .filter(objects -> {
                    String[] split = objects[2].toString().split("__");
                    for (String groupId : split) {
                        if (groupIds.contains(groupId)){
                            return true;
                        }
                    }
                    return false;
                })
                .forEach(objects -> System.out.println(objects[0] + "/" + objects[1]));
    }

    private static void readUsers(List<Object[]> users, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT) {
                if ("User".equals(reader.getLocalName())) {
                    Object[] user = new Object[3];
                    user[1] = reader.getAttributeValue("", "email");
                    StringJoiner groupsJoiner = new StringJoiner("__");
                    while (reader.hasNext()) {
                        event = reader.next();
                        if (event == XMLEvent.START_ELEMENT) {
                            if ("fullName".equals(reader.getLocalName())) {
                                user[0] = reader.getElementText();
                            }
                            if ("groups".equals(reader.getLocalName())) {

                                while (reader.hasNext()) {
                                    event = reader.next();

                                    if (event == XMLEvent.START_ELEMENT) {
                                        if ("group".equals(reader.getLocalName())) {
                                            groupsJoiner.add(reader.getElementText());
                                        }
                                    }
                                    if (event == XMLEvent.END_ELEMENT && "groups".equals(reader.getLocalName())) {
                                        break; //groups end
                                    }
                                }
                            }
                        }

                        if (event == XMLEvent.END_ELEMENT && "User".equals(reader.getLocalName())) {
                            break; //user end
                        }
                    }
                    user[2] = groupsJoiner.toString();
                    users.add(user);
                }
            }

            if (event == XMLEvent.END_ELEMENT && "Users".equals(reader.getLocalName())) {
                break; //users end
            }
        }
    }

    private static void findProjectGroupIds(String projectNameArg, List<String> groupIds, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT) {
                if ("Project".equals(reader.getLocalName())) {
                    String projectName = reader.getAttributeValue("", "name");
                    if (projectNameArg.equalsIgnoreCase(projectName)) {
                        readGroupIds(groupIds, reader);
                    }
                }
            }

            if (event == XMLEvent.END_ELEMENT && "Projects".equals(reader.getLocalName())) {
                break; //projects end
            }
        }
    }

    private static void readGroupIds(List<String> groupIds, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT) {
                if ("Group".equals(reader.getLocalName())) {
                    String groupId = reader.getAttributeValue("", "id");
                    groupIds.add(groupId);
                }
            }

            if (event == XMLEvent.END_ELEMENT && "Groups".equals(reader.getLocalName())) {
                break; //groups end
            }
        }
    }

    private static void parseByJaxb(String projectName) throws JAXBException, IOException {
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
