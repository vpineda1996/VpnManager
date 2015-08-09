package me.vpineda.vpnmanager.model.test;

import android.util.Log;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import me.vpineda.vpnmanager.model.Authenticator;

/**
 * Created by vpineda1996 on 2015-08-04.
 */
public class AuthenticatorTest extends TestCase {

    public void testSetRequestCredentials() throws Exception {
        URL url = new URL("http", "192.168.1.1","/");
        Authenticator authenticator = new Authenticator(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(authenticator.getConnection().getInputStream()));
        String urlString = "";
        String current;
        while((current = in.readLine()) != null)
        {
            urlString += current;
        }
        assertEquals(authenticator.getConnection().getResponseCode(), 200);

        in.close();
    }
}