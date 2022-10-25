package ru.algocode.ejudge;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Perveev Mike
 */
public class EjudgeSession implements AutoCloseable {
    private static final Properties properties = new Properties();
    private static final String newClientUrl;
    private static final int langId;

    static {
        try {
            properties.load(EjudgeSession.class.getClassLoader().getResourceAsStream("ejudge.properties"));
        } catch (IOException e) {
            throw new EjudgeSessionException("Cannot find ejudge.properties file");
        }

        newClientUrl = properties.getProperty("ejudge.client-url");
        langId = Integer.parseInt(properties.getProperty("ejudge.cpp-lang-id"));
    }

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .setRedirectStrategy(new LaxRedirectStrategy())
            .build();

    private final String login;
    private final String password;

    private final int contestId;

    private String sid;

    public EjudgeSession(final String login, final String password, final int contestId) {
        this.login = login;
        this.password = password;
        this.contestId = contestId;
    }

    public void authenticate() {
        HttpClientContext context = HttpClientContext.create();
        List<NameValuePair> parameters = List.of(
                new BasicNameValuePair("action_2", "Войти"),
                new BasicNameValuePair("contest_id", String.valueOf(contestId)),
                new BasicNameValuePair("locale_id", "1"),
                new BasicNameValuePair("login", login),
                new BasicNameValuePair("password", password),
                new BasicNameValuePair("prob_name", ""),
                new BasicNameValuePair("role", "0")
        );

        HttpResponse response = sendPost(newClientUrl, parameters, context);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new EjudgeSessionException("Couldn't authenticate, probably login or password are incorrect, code = "
                    + response.getStatusLine().getStatusCode());
        }

        URI uri = context.getRedirectLocations().get(0);
        List<NameValuePair> responseParameters = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        for (NameValuePair parameter : responseParameters) {
            if (parameter.getName().equals("SID")) {
                this.sid = parameter.getValue();
                return;
            }
        }

        throw new EjudgeSessionException("SID wasn't found in response parameters");
    }

    public void submit(final String textProblemId, final String source) {
        if (textProblemId == null || textProblemId.length() != 1) {
            throw new EjudgeSessionException("Invalid problem character: " + textProblemId);
        }

        List<NameValuePair> parameters = List.of(
                new BasicNameValuePair("SID", sid),
                new BasicNameValuePair("action_40", "Отправить!"),
                new BasicNameValuePair("file", ""),
                new BasicNameValuePair("lang_id", String.valueOf(langId)),
                new BasicNameValuePair("prob_id", String.valueOf(textProblemId.charAt(0) - 'A' + 1)),
                new BasicNameValuePair("text_form", source)
        );

        HttpResponse response = sendPost(newClientUrl, parameters, null);
//        try {
//            System.out.println(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
//        } catch (Exception e) {
//            return;
//        }


        if (response.getStatusLine().getStatusCode() != 200) {
            throw new EjudgeSessionException("Couldn't submit problem, probably not authenticated, code = "
                    + response.getStatusLine().getStatusCode());
        }
    }

    private HttpResponse sendPost(final String url,
                                  final List<NameValuePair> parameters,
                                  final HttpClientContext context) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
        HttpResponse response;
        try {
            if (context == null) {
                response = client.execute(request);
            } else {
                response = client.execute(request, context);
            }
        } catch (IOException e) {
            throw new EjudgeSessionException("Error happened while sending POST request: " + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
