package me.vpineda.vpnmanager.model;

import android.content.Context;
import android.content.SharedPreferences;

import me.vpineda.vpnmanager.R;

/**
 * This class will always have static methods thus you don't need to create the object.
 *
 * Every time you want to use this class, please send in the right context so it can get all of
 * the preferences from {@link android.preference.PreferenceManager}. Failure to do so might result
 * in unexpected behaviour
 */
public class PreferenceManager {

    /**
     * Necessary context
     * FIXME: verify each time that the context is valid and it can be used to retrieve the necessary variables
     */
    public static Context context;

    /**
     * Gets the user from {@link SharedPreferences}
     * @return the username as a string
     */
    public static String getUsername(){
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(
                            context.getResources().getString(R.string.prefs_username_title_key),
                            "admin");
    }

    /**
     * Gets the password from {@link SharedPreferences}
     * @return the password as a string
     * TODO: find a way to encrypt password instead of saving it as plain txt
     */
    public static String getPassword(){
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getResources().getString(R.string.prefs_passwd_title_key),
                        "admin");
    }

    /**
     * Gets host to connect to from {@link SharedPreferences}
     * @return the IP address or host to connect to
     */
    public static String getHost(){
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getResources().getString(R.string.prefs_host_title_key),
                        "192.168.1.1");
    }

    /**
     * Gets the client number from {@link SharedPreferences}
     * @return integer of the client number
     */
    public static int getClientNumber(){
        return Integer.parseInt(android.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getResources().getString(R.string.prefs_client_title_key),
                        "1"));
    }
}
