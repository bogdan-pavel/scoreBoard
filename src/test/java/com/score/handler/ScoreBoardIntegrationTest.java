package com.score.handler;

import com.score.server.ScoreBoardServer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class ScoreBoardIntegrationTest {

    public static final String LOCALHOST = "localhost";
    public static final int PORT = 8081;

    private ScoreBoardServer scoreBoardServer;

    @Before
    public void createAndStartServer() throws Exception {
        scoreBoardServer = new ScoreBoardServer();
        scoreBoardServer.start();
    }

    @After
    public void stopServer() {
        scoreBoardServer.stop();
    }

    @Test
    public void givenNotLoggedUser_whenUserLogin_thenOk() throws IOException {

        //given
        String testPlainType = "text/plain";
        HttpUriRequest request = new HttpGet("http://" + LOCALHOST + ":" + PORT + "/1/login");

        //when
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        //then
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        String body = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(testPlainType, mimeType);
        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        assertFalse(body.isEmpty());
        assertEquals(8, body.length());
    }

    @Test
    public void givenNoUserScoreForLevel_whenAddUserScoreForLevel_thenOk() throws IOException, URISyntaxException {
        //given
        HttpResponse responseLogin = loginGet();
        HttpPost httpPost = getUserScorePost(EntityUtils.toString(responseLogin.getEntity()));
        CloseableHttpClient client = HttpClients.createDefault();

        //when
        CloseableHttpResponse response = client.execute(httpPost);

        //then
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertTrue(EntityUtils.toString(response.getEntity()).isEmpty());

        client.close();
    }

    @Test
    @Ignore
    public void givenEmptyScoreBoard_whenAddUserScoreWithoutLoginAndGetHighScoreForLevel_thenEmptyBoard() throws IOException, URISyntaxException {
        //given
        HttpPost httpPost = getUserScorePost("invalidSessionKey");
        CloseableHttpResponse clientScorePost = HttpClients.createDefault().execute(httpPost);
        clientScorePost.close();

        //when
        HttpUriRequest httpGetHighScore = new HttpGet("http://" + LOCALHOST + ":" + PORT + "/1/highscorelist");
        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response = client.execute(httpGetHighScore);


        //then
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        String responseBody = EntityUtils.toString(response.getEntity());
        assertTrue(responseBody.isEmpty());

        client.close();
    }

    @Test
    public void givenUserScoreForLevel_whenGetHighScoreForLevel_thenReturnUserScoreCsv() throws IOException, URISyntaxException {
        //given
        HttpResponse responseLogin = loginGet();
        executeUserScorePost(responseLogin);

        HttpUriRequest httpGetHighScore = new HttpGet("http://" + LOCALHOST + ":" + PORT + "/1/highscorelist");
        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response = client.execute(httpGetHighScore);

        //then
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        String responseBody = EntityUtils.toString(response.getEntity());
        assertFalse(responseBody.isEmpty());
        assertEquals("1=15", responseBody);

        client.close();
    }

    @Test
    public void givenWrongUriRequest_whenExecuteRequest_thenBadRequest() throws IOException {
        //given
        HttpUriRequest request = new HttpGet("http://" + LOCALHOST + ":" + PORT + "/1/test");

        //when
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        //then
        int statusCode = response.getStatusLine().getStatusCode();
        assertThat(statusCode, equalTo(HttpStatus.SC_BAD_REQUEST));
    }

    private void executeUserScorePost(HttpResponse responseLogin) throws URISyntaxException, IOException {
        HttpPost httpPost = getUserScorePost(EntityUtils.toString(responseLogin.getEntity()));
        CloseableHttpResponse client = HttpClients.createDefault().execute(httpPost);
        client.close();
    }

    private HttpPost getUserScorePost(String sessionKey) throws URISyntaxException, IOException {
        HttpPost httpPost = new HttpPost("http://" + LOCALHOST + ":" + PORT + "/1/score");
        URI uri = new URIBuilder(httpPost.getURI()).addParameter("sessionkey", sessionKey).build();
        httpPost.setURI(uri);
        httpPost.setEntity(new StringEntity("15"));
        return httpPost;
    }

    private HttpResponse loginGet() throws IOException {
        HttpUriRequest httpGet = new HttpGet("http://" + LOCALHOST + ":" + PORT + "/1/login");
        return HttpClientBuilder.create().build().execute(httpGet);
    }
}