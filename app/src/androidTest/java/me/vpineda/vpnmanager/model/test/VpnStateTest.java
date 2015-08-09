package me.vpineda.vpnmanager.model.test;

import junit.framework.TestCase;

import me.vpineda.vpnmanager.model.VpnState;

/**
 * Created by vpineda1996 on 2015-08-05.
 */
public class VpnStateTest extends TestCase {

    public void testParseLine() throws Exception {
        // Parse date
        String testString = "Updated,Wed Aug  5 06:42:01 2015";
        VpnState vpnState = new VpnState();
        vpnState.parseLine(testString);
    }

    public void testParseResponse() throws Exception {

    }
}