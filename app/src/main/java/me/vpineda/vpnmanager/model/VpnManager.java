package me.vpineda.vpnmanager.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tomato has a service website where all of the vpn request go except the status, this is /service.cgi.
 * Hence the final SERVICEFILE variable that is here.
 *
 * We send the authentication that is handled by the authenticator with a set of data
 * that will be handled here.
 *
 * There are 2 main parts in enabling and disabling the Vpn:
 *  _service=vpnclient1-stop
 *  _http_id=TIDf6a58e72153f44ea
 * Where service will be vpnclient and then the number of the client + the operation that we want to do
 * and the HTTP must be obtained by querying the host and getting the http_id in vpn-client.asp
 */
public class VpnManager {
    private final String SERVICEFILE = "/service.cgi";
    private final String VPNSTATUSFILE = "/vpnstatus.cgi";
    private final String VPNCLIENTFILE = "/vpn-client.asp";

    /**
     * Only modify this variable trough getHttpId
     */
    private String http_id;


    /**
     * Enables the VPN
     * @throws IOException check if there is {@link java.io.FileNotFoundException}, really common here
     */
    public void enableVpn() throws IOException{
        Authenticator conn = getAuthenticator(SERVICEFILE);
        sendData(conn,"start");
    }

    /**
     * Disables the VPN
     * @throws IOException check if there is {@link java.io.FileNotFoundException}, really common here
     */
    public void disableVpn() throws IOException {
        Authenticator conn = getAuthenticator(SERVICEFILE);
        sendData(conn, "stop");

    }

    /**
     * Generic routine for enable and disable VPN.
     */
    private void sendData(Authenticator conn, String op) throws IOException {
        // Setup and send the necessary arguments
        if(!op.equals("start") && !op.equals("stop")) throw new IOException("Not a valid request");

        Map<String,Object> arguments = new LinkedHashMap<>();
        arguments.put("_service", "vpnclient" + String.valueOf(PreferenceManager.getClientNumber()) + "-" + op);
        // The http_id is necessary to send commands thus retrieve it
        if(http_id == null) getHttpId();
        arguments.put("_http_id", http_id);
        // Once we've got the http_id, send the post request to Tomato
        sendPOSTRequest(arguments,conn.getConnection());

        // Send response
        InputStream in = conn.getConnection().getInputStream();
        in.close();
    }

    /**
     * Asks Tomato for the current state of the connection
     * @return {@link VpnState} with all of the information that we got from Tomato
     * @throws IOException check if there is {@link java.io.FileNotFoundException}, really common here
     * @throws ParseException Happens where the received date is in the wrong format
     */
    public VpnState state() throws IOException, ParseException {
        Authenticator conn = getAuthenticator(VPNSTATUSFILE);

        // Set the necessary args
        Map<String,Object> args = new LinkedHashMap<>();
        args.put("client", PreferenceManager.getClientNumber());
        if(http_id == null) getHttpId();
        args.put("_http_id", http_id);
        sendPOSTRequest(args,conn.getConnection());

        // Send response and create a VpnState object from that response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getConnection().getInputStream(), "UTF-8"));
        VpnState vpnState =  new VpnState();
        vpnState.parseResponse(in);
        in.close();
        return vpnState;
    }

    private Authenticator getAuthenticator(String file) throws IOException{
        Authenticator connection;
        try {
            connection = new Authenticator(new URL("http", PreferenceManager.getHost(), file));
        }catch (Exception e){
            // We can try to connect again but if it fails just pass the Exception, hopefully
            // someone knows how to handle it.
            connection = new Authenticator(new URL("http", PreferenceManager.getHost(), file));
        }
        return connection;
    }

    /**
     * Call this method whenever you need to update http_id
     * @throws IOException
     */
    public void getHttpId() throws IOException{
        Authenticator conn = getAuthenticator(VPNCLIENTFILE);
        // Start a buffer where we will read all the information
        InputStream inputStream  = conn.getConnection().getInputStream();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String current;
        while((current = in.readLine()) != null) {
            if(current.contains("http_id:")){
                http_id = current.substring(current.indexOf('\'') + 1,current.indexOf(',') - 1);
                break;
            }
        }
        in.close();
    }

    /**
     * Generic routine to send a request to the server without touching the InputStream
     * @param arguments a map containing all of the arguments to be posted
     * @param conn the opened connection to a URL
     * @throws IOException
     */
    private void sendPOSTRequest(Map<String,Object> arguments, HttpURLConnection conn) throws IOException {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : arguments.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

    }

}
