package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class Connection {

    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    // HttpClient is thread-safe and owns a thread pool, so share one instead of creating one per request
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private final String target;

    public Connection(String target)
    {
        this.target = target;
    }


    public Optional<String> sendSimple(String request,String action)
    {

            return post(request,action);

    }

    /** POSTs {@code request} as JSON to {@code action} and parses the response body as {@code responseType}. */
    public <T> T send(Object request, String action, Class<T> responseType)
    {
        return sendSimple(JSONUtil.toJSON(request), action)
                .map(body -> JSONUtil.fromJSON(body, responseType))
                .orElseThrow(() -> new RuntimeException("Request to " + target + action + " failed"));
    }


    private Optional<String> post(String str , String url)
    {

        try {
            HttpRequest httprequest = HttpRequest.newBuilder()
                    .uri(URI.create(target + url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(str))
                    .timeout(Duration.ofMinutes(1))
                    .build();


            HttpResponse<String> response =
                    CLIENT.send(httprequest, HttpResponse.BodyHandlers.ofString());


            LOG.debug("POST {} -> {}", httprequest.uri(), response.statusCode());

           if (response.statusCode()==200)
               return Optional.of(response.body());


        } catch (IOException e) {
            LOG.warn("POST {}{} failed: {}", target, url, e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }

}
