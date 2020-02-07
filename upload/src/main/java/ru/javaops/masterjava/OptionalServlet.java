package ru.javaops.masterjava;

import one.util.streamex.StreamEx;
import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/try", loadOnStartup = 1)
@MultipartConfig
public class OptionalServlet extends HttpServlet {

    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        WebContext ctx = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        ThymeleafUtil.getTemplateEngine().process("upload", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<UserData> topjavaUsers = Collections.emptyList();
        try {
            topjavaUsers = parseByJaxb("topjava", req.getParts().iterator().next().getInputStream()).stream()
                    .map(user ->
                            new UserData(
                                    user.getValue(),
                                    user.getEmail(),
                                    Boolean.parseBoolean(user.getFlag().value())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        resp.setContentType("text/html;charset=UTF-8");
        WebContext ctx = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        ctx.setVariable("users", topjavaUsers);
        ThymeleafUtil.getTemplateEngine().process("users", ctx, resp.getWriter());
    }

    private static Set<User> parseByJaxb(String projectName, InputStream is) throws Exception {
        JaxbParser parser = new JaxbParser(ObjectFactory.class);
        parser.setSchema(Schemas.ofClasspath("payload.xsd"));
        Payload payload;

        payload = parser.unmarshal(is);

        Project project = StreamEx.of(payload.getProjects().getProject())
                .filter(p -> p.getName().equals(projectName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Invalid project name '" + projectName + '\''));

        final Set<Project.Group> groups = new HashSet<>(project.getGroup());  // identity compare
        return StreamEx.of(payload.getUsers().getUser())
                .filter(u -> !Collections.disjoint(groups, u.getGroupRefs()))
                .collect(
                        Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR))
                );
    }
}
