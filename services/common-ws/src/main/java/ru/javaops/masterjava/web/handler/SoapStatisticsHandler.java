package ru.javaops.masterjava.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.web.Statistics;

import javax.xml.ws.handler.MessageContext;
import java.time.Instant;

@Slf4j
public class SoapStatisticsHandler extends SoapBaseHandler {

    private static final String REQUEST_STAT = "REQUEST_STAT";

    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        if (!isOutbound(context)) {
            RequestStat requestStat = new RequestStat(Instant.now().toEpochMilli(), getMessageText(context.getMessage().copy()));
            context.put(REQUEST_STAT, requestStat);
        } else {
            RequestStat requestStat = (RequestStat) context.get(REQUEST_STAT);
            if (requestStat != null) {
                int responseCode = Integer.parseInt(context.get(MessageContext.HTTP_RESPONSE_CODE).toString());

                Statistics.RESULT result = responseCode >= 200 && responseCode < 300
                        ? Statistics.RESULT.SUCCESS
                        : Statistics.RESULT.FAIL;

                Statistics.count(requestStat.getBody(), requestStat.getStartTime(), result);
            }
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext context) {
        RequestStat requestStat = (RequestStat) context.get(REQUEST_STAT);
        if (requestStat != null) {
            Statistics.count(requestStat.getBody(), requestStat.getStartTime(), Statistics.RESULT.FAIL);
        }
        return true;
    }

    @Value
    private static class RequestStat {
        private final long startTime;
        private final String body;
    }
}
