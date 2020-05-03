package ru.javaops.masterjava.web.handler;


import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import ru.javaops.masterjava.config.Configs;

import java.util.EnumMap;
import java.util.Map;

/**
 * Refactored from:
 *
 * @see {http://weblogs.java.net/blog/ramapulavarthi/archive/2007/12/extend_your_web.html
 * http://fisheye5.cenqua.com/browse/jax-ws-sources/jaxws-ri/samples/efficient_handler/src/efficient_handler/common/LoggingHandler.java?r=MAIN}
 * <p/>
 * This simple LoggingHandler will log the contents of incoming
 * and outgoing messages. This is implemented as a MessageHandler
 * for better performance over SOAPHandler.
 */
@Slf4j
public abstract class SoapLoggingHandlers extends SoapBaseHandler {

    private final Level loggingLevel;

    protected SoapLoggingHandlers(Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    private static final Map<Level, HANDLER> HANDLER_MAP = new EnumMap<Level, HANDLER>(Level.class) {
        {
            put(Level.TRACE, HANDLER.DEBUG);
            put(Level.DEBUG, HANDLER.DEBUG);
            put(Level.INFO, HANDLER.INFO);
            put(Level.WARN, HANDLER.ERROR);
            put(Level.ERROR, HANDLER.ERROR);
        }
    };

    protected enum HANDLER {
        NONE {
            @Override
            public void handleFault(MessageHandlerContext mhc) {
            }

            @Override
            public void handleMessage(MessageHandlerContext mhc, boolean isRequest) {
            }
        },
        ERROR {
            private static final String REQUEST_MSG = "REQUEST_MSG";

            public void handleFault(MessageHandlerContext context) {
                log.error("Fault SOAP request:\n" + getMessageText(((Message) context.get(REQUEST_MSG))));
            }

            public void handleMessage(MessageHandlerContext context, boolean isRequest) {
                if (isRequest) {
                    context.put(REQUEST_MSG, context.getMessage().copy());
                }
            }
        },
        INFO {
            public void handleFault(MessageHandlerContext context) {
                ERROR.handleFault(context);
            }

            public void handleMessage(MessageHandlerContext context, boolean isRequest) {
                ERROR.handleMessage(context, isRequest);
                log.info((isRequest ? "SOAP request: " : "SOAP response: ") + context.getMessage().getPayloadLocalPart());
            }
        },
        DEBUG {
            public void handleFault(MessageHandlerContext context) {
                log.error("Fault SOAP message:\n" + getMessageText(context.getMessage().copy()));
            }

            public void handleMessage(MessageHandlerContext context, boolean isRequest) {
                log.info((isRequest ? "SOAP request:\n" : "SOAP response:\n") + getMessageText(context.getMessage().copy()));
            }
        };

        public abstract void handleMessage(MessageHandlerContext mhc, boolean isRequest);

        public abstract void handleFault(MessageHandlerContext mhc);
    }

    abstract protected boolean isRequest(boolean isOutbound);

    @Override
    public boolean handleMessage(MessageHandlerContext mhc) {
        HANDLER_MAP.get(loggingLevel).handleMessage(mhc, isRequest(isOutbound(mhc)));
        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext mhc) {
        HANDLER_MAP.get(loggingLevel).handleFault(mhc);
        return true;
    }

    public static class ClientHandler extends SoapLoggingHandlers {
        public ClientHandler() {
            super(Level.valueOf(Configs.getConfig("hosts.conf", "hosts")
                    .getConfig("mail")
                    .getString("debug.client")
                    .toUpperCase()));
        }

        @Override
        protected boolean isRequest(boolean isOutbound) {
            return isOutbound;
        }
    }

    public static class ServerHandler extends SoapLoggingHandlers {

        public ServerHandler() {
            super(Level.valueOf(Configs.getConfig("hosts.conf", "hosts")
                    .getConfig("mail")
                    .getString("debug.server")
                    .toUpperCase()));
        }

        @Override
        protected boolean isRequest(boolean isOutbound) {
            return !isOutbound;
        }
    }
}
