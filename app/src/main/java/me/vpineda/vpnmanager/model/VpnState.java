package me.vpineda.vpnmanager.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class to represent all of the information that tomato send us
 *
 * Sometimes I really want Java to support structs, its faster than creating a whole
 * class
 */
public class VpnState {
    /* Common response
            OpenVPN STATISTICS
            Updated,Wed Aug  5 06:42:01 2015
            TUN/TAP read bytes,265
            TUN/TAP write bytes,0
            TCP/UDP read bytes,3233
            TCP/UDP write bytes,1752
            Auth read bytes,0
            pre-compress bytes,105
            post-compress bytes,110
            pre-decompress bytes,0
            post-decompress bytes,0
            END
     */

    public Date updateDate;
    public long tunReadBytes, tunWriteBytes, tcpReadBytes, tcpWriteBytes, authReadBytes, preCompressBytes,
        postCompressBytes, preDecompressBytes, postDecompressBytes;
    public boolean enabled = false;

    /**
     * Parse each line of the response individually.
     * @param i line to be parsed
     * @throws ParseException
     * FIXME: Maybe raise an exception when the response is corrupted or doesn't follow the right order
     */
    public void parseLine(String i) throws ParseException {
        // Be careful awesome switches ahead!!!
        switch (i.charAt(0)){
            case 'U':
                DateFormat dateFormat = new SimpleDateFormat("EEE MMM  d HH:mm:ss yyyy", Locale.ENGLISH);
                updateDate = dateFormat.parse(i.substring(i.indexOf(",")+ 1));
                enabled = true;
                break;
            case 'T':
                switch (i.charAt(1)){
                    case 'U':
                        if(i.contains("read")) tunReadBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                        else tunWriteBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                        break;
                    case 'C':
                        if(i.contains("read")) tcpReadBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                        else tcpWriteBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                        break;
                }
                break;
            case 'A':
                authReadBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                break;
            case 'p':
                if(!i.contains("decompress")){
                    switch (i.charAt(1)){
                        case 'r':
                            preCompressBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                            break;
                        case 'o':
                            postCompressBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                            break;
                    }
                }else {
                    switch (i.charAt(1)){
                        case 'r':
                            preDecompressBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                            break;
                        case 'o':
                            postDecompressBytes = Long.parseLong(i.substring(i.indexOf(',') + 1));
                            break;
                    }
                }
                break;
        }
    }

    /**
     *
     * @param in the response similar to the one at the beginning of the class
     * @throws IOException
     * @throws ParseException
     */
    public void parseResponse(BufferedReader in) throws IOException, ParseException {
        String i;
        // When the BufferReader is empty, that means that the VPN was offline but if we receive something
        // we can read a line thus if i != null we can get that line
        while ((i = in.readLine()) != null){
            if(enabled) {
                parseLine(i);
                continue;
            }
            if(i.contains("OpenVPN")) enabled = true;
        }
    }

    @Override
    public String toString() {
        return "VpnState{" +
                "updateDate=" + updateDate +
                ", tunReadBytes=" + tunReadBytes +
                ", tunWriteBytes=" + tunWriteBytes +
                ", tcpReadBytes=" + tcpReadBytes +
                ", tcpWriteBytes=" + tcpWriteBytes +
                ", authReadBytes=" + authReadBytes +
                ", preCompressBytes=" + preCompressBytes +
                ", postCompressBytes=" + postCompressBytes +
                ", preDecompressBytes=" + preDecompressBytes +
                ", postDecompressBytes=" + postDecompressBytes +
                ", enabled=" + enabled +
                '}';
    }
}
