package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.dao.UserGroupDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserGroup;
import ru.javaops.masterjava.persist.model.type.UserFlag;
import ru.javaops.masterjava.upload.PayloadProcessor.FailedEmails;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class UserProcessor {
    private static final int NUMBER_THREADS = 4;

    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);
    private static UserGroupDao userGroupDao = DBIProvider.getDao(UserGroupDao.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    /*
     * return failed users chunks
     */
    public List<FailedEmails> process(final StaxStreamProcessor processor, Map<String, Group> groups, Map<String, City> cities, int chunkSize) throws XMLStreamException, JAXBException {
        log.info("Start processing with chunkSize=" + chunkSize);

        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();  // ordered map (emailRange -> chunk future)

        int id = userDao.getSeqAndSkip(chunkSize);
        List<User> chunkUsers = new ArrayList<>(chunkSize);
        List<UserGroup> chunkUserGroups = new ArrayList<>(chunkSize);
        val unmarshaller = jaxbParser.createUnmarshaller();
        List<FailedEmails> failed = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            String cityRef = processor.getAttribute("city");  // unmarshal doesn't get city ref
            String groupRefsString = processor.getAttribute("groupRefs"); // unmarshal doesn't get group refs
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            boolean groupsSuccess = true;
            List<Group> currentGroups = new ArrayList<>();

            if (groupRefsString != null) {
                String[] xmlUsersGroups = groupRefsString.split(" ");
                currentGroups = Arrays.stream(xmlUsersGroups)
                        .map(groups::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                groupsSuccess = currentGroups.size() == xmlUsersGroups.length;
            }

            if (!groupsSuccess) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "Not all groups are present in DB"));
            } else if (cities.get(cityRef) == null) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "City '" + cityRef + "' is not present in DB"));
            } else {
                final User user = new User(id++, xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()), cityRef);
                chunkUsers.add(user);
                for (Group group : currentGroups) {
                    chunkUserGroups.add(new UserGroup(user.getId(), group.getId()));
                }
                if (chunkUsers.size() == chunkSize) {
                    addChunkFutures(chunkFutures, chunkUsers, chunkUserGroups);
                    chunkUsers = new ArrayList<>(chunkSize);
                    chunkUserGroups = new ArrayList<>(chunkSize);
                    id = userDao.getSeqAndSkip(chunkSize);
                }
            }
        }

        if (!chunkUsers.isEmpty()) {
            addChunkFutures(chunkFutures, chunkUsers, chunkUserGroups);
        }

        List<String> allAlreadyPresents = new ArrayList<>();
        chunkFutures.forEach((emailRange, future) -> {
            try {
                List<String> alreadyPresentsInChunk = future.get();
                log.info("{} successfully executed with already presents: {}", emailRange, alreadyPresentsInChunk);
                allAlreadyPresents.addAll(alreadyPresentsInChunk);
            } catch (InterruptedException | ExecutionException e) {
                log.error(emailRange + " failed", e);
                failed.add(new FailedEmails(emailRange, e.toString()));
            }
        });
        if (!allAlreadyPresents.isEmpty()) {
            failed.add(new FailedEmails(allAlreadyPresents.toString(), "already presents"));
        }
        return failed;
    }

    private void addChunkFutures(Map<String, Future<List<String>>> chunkFutures, List<User> chunkUsers, List<UserGroup> chunkUserGroups) {
        String emailRange = String.format("[%s-%s]", chunkUsers.get(0).getEmail(), chunkUsers.get(chunkUsers.size() - 1).getEmail());
        Future<List<String>> future = executorService.submit(() -> DBIProvider.getDBI()
                .inTransaction((conn, status) -> {
                    List<String> conflictEmails = userDao.insertAndGetConflictEmails(chunkUsers);
                    List<UserGroup> successUserGroups = chunkUsers.stream()
                            .filter(user -> !conflictEmails.contains(user.getEmail()))
                            .flatMap(user -> chunkUserGroups.stream()
                                    .filter(userGroup -> userGroup.getUserId().equals(user.getId())))
                            .collect(Collectors.toList());
                    userGroupDao.insertBatch(successUserGroups);
                    return conflictEmails;
                }));
        chunkFutures.put(emailRange, future);
        log.info("Submit chunk: " + emailRange);
    }
}
