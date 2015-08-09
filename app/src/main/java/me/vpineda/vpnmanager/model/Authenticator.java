package me.vpineda.vpnmanager.model;

import android.util.Base64;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is designed to be the authenticator and holder of the http connection.
 * The VPN manager will ask this class for the connection whenever it needs it and
 * it will give it to him with valid credentials.
 */
public class Authenticator {

    private HttpURLConnection connection;

    /**
     * The main constructor for this class, it requires the url that will be configured with
     * the auth method
     * @param url the url to open the connection with
     * @throws IOException throws it when an the URL can't open a connect, handle it smoothly.
     */
    public Authenticator(URL url) throws IOException{
        connection = (HttpURLConnection) url.openConnection();
        setRequestCredentials(connection);
    }

    private HttpURLConnection setRequestCredentials(HttpURLConnection connection){
        // The authentication that Tomato uses is a Basic one, username and password are
        // encoded in base64 and then sent in the header of the request so lets do that

        // Authentication header by convention is written the following way
        // Authorization: TYPE_OF_AUTH ENCODE_BASE64(USERNAME:PASSWORD)
        connection.setRequestProperty("Authorization", "Basic " + getUsernameAndPassword());

        return connection;
    }

    private String getUsernameAndPassword() {
        String username, password, stringToEncode, stringEncoded;

        username = PreferenceManager.getUsername();
        password = PreferenceManager.getPassword();
        if(username == null || password == null) throw new NullPointerException("Not a valid username or password");
        stringToEncode = username + ":" + password;

        stringEncoded = Base64.encodeToString(stringToEncode.getBytes(), Base64.DEFAULT);
        return stringEncoded;
    }

    public HttpURLConnection getConnection() {
        return connection;
    }
}
