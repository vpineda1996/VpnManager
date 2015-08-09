package me.vpineda.vpnmanager.model.test;

import junit.framework.TestCase;

import me.vpineda.vpnmanager.model.VpnManager;

/**
 * Created by vpineda1996 on 2015-08-05.
 */
public class VpnManagerTest extends TestCase {

    public void testEnableVpn() throws Exception {
        VpnManager vpnManager = new VpnManager();
        vpnManager.enableVpn();
        assertTrue(vpnManager.state().enabled);
    }

    public void testDisableVpn() throws Exception {
        VpnManager vpnManager = new VpnManager();
        vpnManager.disableVpn();
        assertFalse(vpnManager.state().enabled);
    }

    public void testState() throws Exception {
        VpnManager vpnManager = new VpnManager();
        vpnManager.enableVpn();
        assertTrue(vpnManager.state().enabled);
        vpnManager.disableVpn();
        assertFalse(vpnManager.state().enabled);
    }

    public void testGetHttpId() throws Exception {
        VpnManager vpnManager = new VpnManager();
        vpnManager.getHttpId();
    }
}