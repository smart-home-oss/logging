package lu.smarthome.logging.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticsearchClient {

    RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("localhost", 9200, "http")));

    public void init() {
        final IndicesClient indices = client.indices();

        System.out.println(indices);
    }
}
