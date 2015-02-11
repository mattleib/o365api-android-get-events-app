package com.leibmann.android.myupcommingevents;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by mattleib on 2/9/2015.
 */
public class Office365API {

    public static String getRequestForJSONResponse(String urlRestApi, String accessToken) throws Exception {

        HttpURLConnection conn = null;
        BufferedReader br = null;

        try {
            URL url = new URL(urlRestApi);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json; odata.metadata=none");
            conn.setRequestProperty("User-Agent", "com.leibmann.android.myupcommingevents/1.0");
            conn.setRequestProperty("client-request-id", UUID.randomUUID().toString());
            conn.setRequestProperty("return-client-request-id", "true" );
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String apiOutput = br.readLine();

            return apiOutput;
        }
        catch(Exception e) {

            throw e;
        }
        finally {
            AppHelper.close(br);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        return result;
    }

    public static String sendEmail(String urlRestApi, String subject, String body, String nameOfRecipient, String emailAddress, String accessToken) throws Exception {

        final String eMailTemplate = "{\"Message\":{\"Subject\":\"%s\",\"Importance\":\"High\",\"Body\":{\"ContentType\":\"HTML\",\"Content\":\"%s\"},\"ToRecipients\":[{\"EmailAddress\":{\"Name\":\"%s\",\"Address\":\"%s\"}}]}}";

        String eMailMessage = String.format(eMailTemplate,
                subject, body, nameOfRecipient, emailAddress);

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(urlRestApi);

            StringEntity se = new StringEntity(eMailMessage);
            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json; odata.metadata=none");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("User-Agent", "com.leibmann.android.myupcommingevents/1.0");
            httpPost.setHeader("client-request-id", UUID.randomUUID().toString());
            httpPost.setHeader("return-client-request-id", "true");
            httpPost.setHeader("Authorization", "Bearer " + accessToken);

            HttpResponse httpResponse = httpclient.execute(httpPost);

            String apiOutput = "";
            InputStream inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null) {
                 apiOutput = convertInputStreamToString(inputStream);
            }

            return apiOutput;
        }
        catch(Exception e) {
            throw e;
        }
    }

}
