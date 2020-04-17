package lu.smarthome.logging.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
public class ElasticsearchLoggingAppender extends AppenderBase<ILoggingEvent> {

    private RestHighLevelClient client;

    private boolean allGood = true;
    private String packageNameRoot;

    public static final String INDEX_NAME = "external-sensors";

    @Override
    public void start() {
        super.start();
        log.info("Elasticsearch appender is starting, index: " + INDEX_NAME);

        packageNameRoot = getClass().getCanonicalName().replace(".elasticsearch" + getClass().getName(), "");

        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));

        final IndicesClient indices = client.indices();

        try {
            indices.get(new GetIndexRequest(INDEX_NAME), RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException e) {
            try {
                CreateIndexRequest newIndex = new CreateIndexRequest(INDEX_NAME);

                final CreateIndexResponse response = indices.create(newIndex, RequestOptions.DEFAULT);
                if(response.index() == null) {
                    log.info("Create index for logs, index: " + INDEX_NAME);
                } else {
                    log.error("Failed to create index for logs, index: {}", INDEX_NAME);
                }
            } catch (IOException e2) {
                log.error("Failed to create index for logs, index: {}, , e: {}", INDEX_NAME, e2.getMessage());
            }
        } catch (IOException e) {
            log.error("Failed to create index for logs, index: {}, , e: {}", INDEX_NAME, e.getMessage());
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        request.id(UUID.randomUUID().toString());

        final String stackTraceLite;
        final String messageDetails;

        if(event.getThrowableProxy() != null) {
            stackTraceLite = parseStackTraceLite(event);
            messageDetails = (", details: ").concat(event.getThrowableProxy().getMessage());
        } else {
            stackTraceLite = "-";
            messageDetails = "";
        }

        String jsonString = buildJsonString(event, stackTraceLite, messageDetails);
        request.source(jsonString, XContentType.JSON);

        client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                if(!allGood) {
                    Logger.getGlobal().severe("Elasticsearch appender got back on track.");
                }

                allGood = true;
            }

            @Override
            public void onFailure(Exception e) {
                if(allGood) {
                    Logger.getGlobal().severe("Elasticsearch appender is failing, e: " + e.getMessage());
                }

                allGood = false;

                log.error(event.getFormattedMessage().concat("details: {}").concat(", stack: {}"), messageDetails, stackTraceLite);
            }
        });
    }

    private String buildJsonString(ILoggingEvent event, String stackTraceLite, String messageDetails) {
        return "{" +
                    "\"message\":\"" + event.getFormattedMessage() + messageDetails + "\"," +
                    "\"timestamp\":\"" + event.getTimeStamp() + "\"," +
                    "\"level\":\"" + event.getLevel() + "\"," +
                    "\"thread\":\"" + event.getThreadName() + "\"," +
                    "\"stackTraceLite\":\"" + stackTraceLite + "\"," +
                    "\"logger\":\"" + event.getLoggerName() + "\"" +
                    "}";
    }

    private String parseStackTraceLite(ILoggingEvent event) {
        StringBuilder result = new StringBuilder("[stack trace lite]");

        final StackTraceElementProxy[] stackTraceElementProxyArray = event.getThrowableProxy().getStackTraceElementProxyArray();

        for (StackTraceElementProxy callerDatum : stackTraceElementProxyArray) {
            if(callerDatum.getStackTraceElement().getClassName().startsWith(packageNameRoot)) {
                result.append(" -->   ");
                result.append(callerDatum);
            }
        }

        return result.toString();
    }
}
