package ru.javaops.masterjava.service.mail.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.typesafe.config.Config;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.web.AuthUtil;
import ru.javaops.masterjava.web.handler.SoapBaseHandler;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

public class SoapServerSecurityHandler extends SoapBaseHandler {

    private static String AUTH_HEADER;

    static {
        Config config = Configs.getConfig("hosts.conf", "hosts").getConfig("mail");
        AUTH_HEADER = AuthUtil.encodeBasicAuthHeader(config.getString("user"), config.getString("password"));
    }

    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);

//        HttpServletRequest request = (HttpServletRequest) mCtx.get(MessageContext.SERVLET_REQUEST);
//        HttpServletResponse response = (HttpServletResponse) mCtx.get(MessageContext.SERVLET_RESPONSE);

        int code = AuthUtil.checkBasicAuth(headers, AUTH_HEADER);
        if (code != 0) {
            context.put(MessageContext.HTTP_RESPONSE_CODE, code);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }
}
