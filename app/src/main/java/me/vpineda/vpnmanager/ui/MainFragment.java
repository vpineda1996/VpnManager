package me.vpineda.vpnmanager.ui;

import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;

import me.vpineda.vpnmanager.R;
import me.vpineda.vpnmanager.model.VpnManager;
import me.vpineda.vpnmanager.model.VpnState;

/**
 * All of the real magic lives within this class
 */
public class MainFragment extends Fragment implements View.OnClickListener{
    private ButtonFragment buttonFragment;
    private DataFragment dataFragment;
    private VpnManager vpnManager;
    private boolean queryCurrentlyActive = false;

    public MainFragment() {
        // Required empty public constructor
    }

    //
    //
    // OVERWRITES
    //
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        // Assign all of the fragments to each and every one of the global variables for this class
        Fragment f = getChildFragmentManager().findFragmentById(R.id.button_fragment);
        if(f instanceof ButtonFragment){
            buttonFragment = (ButtonFragment) f;
        }else throw new RuntimeException("Could not find ButtonFragment");

        f = getChildFragmentManager().findFragmentById(R.id.data_fragment);
        if(f instanceof DataFragment){
            dataFragment = (DataFragment) f;
        }else throw new RuntimeException("Could not find DataFragment");
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buttonFragment.button.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Every time Android resumes the app, it will delete the previous VpnManager and create
        // a new one this is a trick so whenever a preference is changed, VpnManager will read
        // all of the preferences again
        me.vpineda.vpnmanager.model.PreferenceManager.context = getActivity();
        vpnManager = new VpnManager();
        checkStatus();
    }


    /**
     * OnClickListener implementation for the cool button
     * @param v
     */
    @Override
    public void onClick(View v) {
        changeStatus();
    }

    //
    //
    // METHODS
    //
    //

    /**
     * Call this method if you want to turn on or off the VPN, if the vpn is turned on it will
     * turn it off and vice versa
     */
    private void changeStatus(){
        if(queryCurrentlyActive) return;
        queryCurrentlyActive = true;
        new ChangeStatusAsyncTask().execute(vpnManager);
        updateView(5);
    }

    /**
     * This method will only check if the VPN is active, it will not modify its state
     */
    public void checkStatus(){
        updateView(5);
        new CheckStatusAsyncTask().execute();
    }

    /**
     * It will update the view depending on the integer that is passed
     * @param result 0 if everything went smoothly and it changed to disabled
     *               1 if everything went smoothly and it changed to enabled
     *               2 if a authentication error
     *               3 if we cannot find a route to host
     *               4 could not parse date but active
     *               5 change view to currently refreshing
     *               -1 Unknown error
     */
    private void updateView(int result){
        switch (result){
            // Changed to enabled
            case 1:
                new CheckStatusAsyncTask().execute();
                return;
            // Auth error
            case 2:
                Toast.makeText(getActivity(),getResources().getText(R.string.error_authentication),Toast.LENGTH_LONG)
                        .show();
                result = 0;
                break;
            // Route to host error
            case 3:
                Toast.makeText(getActivity(),getResources().getText(R.string.error_route_host),Toast.LENGTH_LONG)
                        .show();
                result = 0;
                break;
            // Parse date error
            case 4:
                Toast.makeText(getActivity(),getResources().getText(R.string.error_date_parsing),Toast.LENGTH_LONG)
                        .show();
                result = 0;
                break;
            // Unknown error
            case -1:
                Toast.makeText(getActivity(),getResources().getText(R.string.error_unknown_error),Toast.LENGTH_LONG)
                        .show();
                result = 0;
                break;
        }
        // Once you've displayed the error message or simply turned off, return to off button
        buttonFragment.changeLayout(result);
        dataFragment.changeLayout(result);

    }

    /**
     * This method is used mainly when we have a result 1 since this method reduces the second request
     * to find out all of the data
     * @param result integer describing the final result
     * @param vpnState {@link VpnState} that has all the info regarding the state of the VPN
     */
    private void updateView(int result, VpnState vpnState) {
        if(result == 1 && vpnState != null){
            buttonFragment.changeLayout(result);
            dataFragment.changeLayout(vpnState);
        }else updateView(result);
    }

    //
    //
    // FRAGMENTS
    //
    //

    public static class ButtonFragment extends Fragment{
        public ImageButton button;
        public RelativeLayout layout;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_button, container, false);
            View b = v.findViewById(R.id.power_button);
            if (b instanceof ImageButton){
                button = (ImageButton) b;
            }else throw new RuntimeException("Cant find button");

            b = v.findViewById(R.id.button_fragment_rel_layout);
            if (b instanceof RelativeLayout){
                layout = (RelativeLayout) b;
            }else throw new RuntimeException("Cant find button's relative layout");
            return v;
        }

        /**
         *
         * @param on 1 means that is currently on
         *           0 means that is currently off
         *           5 means that is currently refreshing
         */
        public void changeLayout(int on){
            switch(on) {
                case 0:
                    button.setClickable(true);
                    button.setImageResource(R.drawable.off_button);
                    layout.setBackgroundColor(getResources().getColor(R.color.button_red_background_color));
                    break;
                case 1:
                    button.setClickable(true);
                    button.setImageResource(R.drawable.on_button);
                    layout.setBackgroundColor(getResources().getColor(R.color.button_green_background_color));
                    break;
                case 5:
                    button.setClickable(false);
                    button.setImageResource(R.drawable.refresh_button);
                    layout.setBackgroundColor(getResources().getColor(R.color.button_blue_background_color));
            }
        }
    }

    public static class DataFragment extends Fragment implements ValueAnimator.AnimatorUpdateListener{
        private boolean visible;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_data, container, false);
        }

        /**
         * OnAnimatorUpdate implementation, makes code cleaner in changeLayout
         * @param animation the animated value is a float object
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            View view = getView();
            if(view == null) throw new RuntimeException("Couldn't get view");
            view.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    (float) animation.getAnimatedValue()
            ));
            view.requestLayout();
        }

        /**
         * Shows the values that we get from Tomato
         * @param vpnState the values that will be set in the view
         * TODO: verify that we can find the TextView and not create a NullPointerException
         */
        private void showValues(VpnState vpnState) {
            ((TextView) getView().findViewById(R.id.data_fragment_TCP_read))
                    .setText(String.valueOf(vpnState.tcpReadBytes));
            ((TextView) getView().findViewById(R.id.data_fragment_TCP_write))
                    .setText(String.valueOf(vpnState.tcpWriteBytes));
            ((TextView) getView().findViewById(R.id.data_fragment_TUN_read))
                    .setText(String.valueOf(vpnState.tunReadBytes));
            ((TextView) getView().findViewById(R.id.data_fragment_TUN_write))
                    .setText(String.valueOf(vpnState.tunWriteBytes));
        }

        /**
         * Same as ButtonFragment
         * @param on 1 means that is currently on
         *           0 means that is currently off
         *           5 means that is currently refreshing
         */
        private void changeLayout(int on){
            final View view = getView();
            if(view != null) {
                if(on == 1 && !visible) {
                    ValueAnimator valueAnimator = new ValueAnimator();
                    valueAnimator.setFloatValues(0f, 1f);
                    valueAnimator.addUpdateListener(this);
                    valueAnimator.start();
                    visible = true;
                }else if (on == 5) {
                    if (visible) {
                        ValueAnimator valueAnimator = new ValueAnimator();
                        valueAnimator.setFloatValues(1f, 0f);
                        valueAnimator.addUpdateListener(this);
                        valueAnimator.start();
                        visible = false;
                    }
                }else if(visible && on == 0) {
                    ValueAnimator valueAnimator = new ValueAnimator();
                    valueAnimator.setFloatValues(1f, 0f);
                    valueAnimator.addUpdateListener(this);
                    valueAnimator.start();
                    visible = false;
                }
                view.requestLayout();
            }

        }

        public void changeLayout(VpnState vpnState){
            showValues(vpnState);
            changeLayout(vpnState.enabled ? 1 : 0);
        }
    }

    //
    //
    // ASYNCTASKS
    //
    //

    private class ChangeStatusAsyncTask extends AsyncTask<VpnManager,Void,ChangeStatusAsyncTask.Result>{

        /**
         * Once more, this is why structs should exist in Java
         */
        public class Result {
            int result = -1 ;
            VpnState vpnState = null;
        }
        /**
         * Sends data to server
         * @param params the vpnmanager that will be used
         * @return 1 if everything went smoothly and it changed to enabled
         *         0 if everything went smoothly and it changed to disabled
         *         2 if a authentication error
         *         3 if we cannot find a route to host
         *         4 could not parse date but active
         */
        @Override
        protected Result doInBackground(VpnManager... params) {
            VpnManager vpnMngr = params[0];
            Result res = new Result();
            boolean enabled;
            try {
                enabled = vpnMngr.state().enabled;
                if(!enabled){
                    vpnMngr.enableVpn();
                }else {
                    vpnMngr.disableVpn();
                }
                res.vpnState = vpnMngr.state();
                res.result = res.vpnState.enabled ? 1 : 0;
                return res;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                res.result = 2;
                return res;
            } catch (ConnectException e) {
                e.printStackTrace();
                res.result = 3;
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
                res.result = 4;
                return res;
            }
            return res;
        }

        /**
         * Calls updateView so we can update all of the fragments with the respective result numer
         * @param result the result of the background process
         */
        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            queryCurrentlyActive = false;
            if(isVisible()) updateView(result.result,result.vpnState);
        }
    }

    private class CheckStatusAsyncTask extends AsyncTask<Void,Void,CheckStatusAsyncTask.Result>{

        /**
         * Once more, this is why structs should exist in Java
         */
        public class Result {
            int result = -1 ;
            VpnState vpnState = null;
        }

        /**
         * This guy will only query and update the view depending on the status that we get from
         * Tomato
         * @return the vpnState that will update the view
         */
        @Override
        protected Result doInBackground(Void... params) {
            Result result = new Result();
            try {
                result.vpnState = vpnManager.state();
                result.result = result.vpnState.enabled ? 1 : 0;
                return result;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result.result = 2;
                return result;
            } catch (ConnectException e) {
                e.printStackTrace();
                result.result = 3;
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
                result.result = 4;
                return result;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result vpnState) {
            super.onPostExecute(vpnState);
            if(vpnState.vpnState == null)
                updateView(vpnState.result);
            else if(isVisible()){
                buttonFragment.changeLayout(vpnState.result);
                dataFragment.changeLayout(vpnState.vpnState);
            }

        }
    }

}
