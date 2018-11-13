/*
 * Api.java
 * Class only contains 1 static method, therefore calling a constructor is unnecessary
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class Api {
    /*
        Given a URL for a GET request, the method executes the call,
        converts the data to a string, and returns the string
     */
    public static String fetch(String uri) {
        String apiResponse = null;

        try {
            // call the API
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // 200 = OK
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            // clean the data received from the API by turning it into a String
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            apiResponse = br.lines().collect(Collectors.joining(System.lineSeparator()));

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apiResponse;
    }
}
