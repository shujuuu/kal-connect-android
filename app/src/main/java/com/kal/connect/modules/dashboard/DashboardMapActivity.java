package com.kal.connect.modules.dashboard;

import static com.kal.connect.customLibs.fcm.MyFirebaseMessagingService.CALL_DECLINE;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.places.Place;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.kal.connect.R;
import com.kal.connect.customLibs.Callbacks.ItemHomeScreen;
import com.kal.connect.customLibs.HTTP.GetPost.APICallback;
import com.kal.connect.customLibs.HTTP.GetPost.SoapAPIManager;
import com.kal.connect.customLibs.Maps.Manager.CustomMapActivity;
import com.kal.connect.modules.dashboard.AccountDetails.AccountFragment;
import com.kal.connect.modules.dashboard.AppointmentsDetails.AppointmentsFragment;
import com.kal.connect.modules.dashboard.BookAppointment.HomeFragmentS1;
import com.kal.connect.modules.dashboard.BuyMedicine.MedicineActivity;
import com.kal.connect.modules.dashboard.Home.HomeScreenFragment;
import com.kal.connect.utilities.AppPreferences;
import com.kal.connect.utilities.Config;
import com.kal.connect.utilities.GlobValues;
import com.kal.connect.utilities.Utilities;
import com.kal.connect.utilities.UtilitiesInterfaces;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class DashboardMapActivity extends CustomMapActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    private static final int MY_REQUEST_CODE = 111;
    // MARK : Properties
    BottomNavigationView bottomTab = null;

    HomeFragmentS1 homeFragmentS1;
    AppointmentsFragment appointmentFragment;
    private HomeScreenFragment homeScreenFragment;
    private ItemHomeScreen itemHomeScreen;


    // MARK : Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        buildUI();

        try{
            checkForUpdate();

        }catch (Exception e){
            Log.e(TAG, "onCreate: Update failed" );
        }
    }
    private void checkForUpdate() {
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "checkForUpdate: "+e );
                    e.printStackTrace();
                }
            }else{
                Log.e(TAG, "checkForUpdate: No update available" );
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    // MARK : UIActions
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }


    // MARK : Instance Methods
    private void buildUI() {

        if (getIntent()!=null && getIntent().getIntExtra(CALL_DECLINE, 0)==1)
        {
            Utilities.showAlert(DashboardMapActivity.this, "Doctor declined call...\nPlease wait for some time.", false);
        }
        buildBottomTabs();
        getStateCityList();
        //if (AppPreferences.getInstance().getCountryCode()==null || AppPreferences.getInstance().getCountryCode().equals("")){
            //getCountryCodeFromServer();
        //}

        try {
            if (GlobValues.getAddAppointmentParams() != null)
                GlobValues.getAddAppointmentParams().clear();
        } catch (Exception e) {

        }

        itemHomeScreen = new ItemHomeScreen() {
            @Override
            public void onSelected(int id) {
                Fragment pageToShow = null;

                switch (id) {

                    case 0:
                        if (homeScreenFragment == null) {
                            homeScreenFragment = new HomeScreenFragment(itemHomeScreen);
                        }
                        pageToShow = homeScreenFragment;
                        bottomTab.setSelectedItemId(R.id.tab_home);

                        break;

                    case 2:
                        if (homeFragmentS1 == null) {
                            homeFragmentS1 = new HomeFragmentS1();
                        }
                        pageToShow = homeFragmentS1;
                        bottomTab.setSelectedItemId(R.id.tab_consultation);

                        break;

                    case 3:
                        if (appointmentFragment == null) {
                            appointmentFragment = new AppointmentsFragment();
                        }
                        pageToShow = appointmentFragment;
                        bottomTab.setSelectedItemId(R.id.tab_appointments);

                        break;

                    case 1:
//                        pageToShow = new Medicine();
                        Intent mIntent = new Intent(DashboardMapActivity.this, MedicineActivity.class);
                        startActivity(mIntent);
                        break;

                    case 4:
                        pageToShow = new AccountFragment();
                        bottomTab.setSelectedItemId(R.id.tab_account);

                        break;

                }
                if (pageToShow != null) {
                    loadFragment(pageToShow);
                }

            }
        };

        if (getIntent().hasExtra("FromNotification") && getIntent().getBooleanExtra("FromNotification", false)) {
            loadFragment(new AppointmentsFragment());
            bottomTab.setSelectedItemId(R.id.tab_appointments);

        } else {
            if (homeScreenFragment == null) {
                homeScreenFragment = new HomeScreenFragment(itemHomeScreen);
            }
            loadFragment(homeScreenFragment);
        }


        ImageButton sos = findViewById(R.id.sos);
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dexter.withActivity(DashboardMapActivity.this)
                        .withPermissions(
                                Manifest.permission.CALL_PHONE

                        ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "7575005555"));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();

            }
        });





//        try{
//            JSONObject userInfo = AppPreferences.getInstance().getUserInfo();
//            if(userInfo.has("Lattitude") && userInfo.get("Lattitude") != null && !userInfo.getString("Lattitude").isEmpty()  && !userInfo.getString("Lattitude").equals("null") &&
//                    userInfo.has("Longitude") && userInfo.get("Longitude") != null && !userInfo.getString("Longitude").isEmpty()  && !userInfo.getString("Longitude").equals("null")){
//
//            }else{
//                if(AppPreferences.getInstance().isLocationRequested())
//                    return;
//                Utilities.showAlertDialogWithOptions(this, getResources().getString(R.string.alert_map_options), new String[]{getResources().getString(R.string.btn_yes), getResources().getString(R.string.cancel)}, new UtilitiesInterfaces.AlertCallback() {
//                    @Override
//                    public void onOptionClick(DialogInterface dialog, int buttonIndex) {
//                        if(buttonIndex == 0)
//                        {
//                            launchPlacePicker();
//                        }
//                        if(buttonIndex == 1)
//                        {
//
//                        }
//                    }
//                });
//
//            }
//        }catch (Exception e){
//
//        }


    }

    // MARK : Bottom Tab bar
    private void buildBottomTabs() {

        // Build Tab bar with bottom navigation view
        bottomTab = findViewById(R.id.tab_bar);

        bottomTab.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment pageToShow = null;

                // Replace fragment for the selected tab
                switch (item.getItemId()) {

                    case R.id.tab_home:
                        if (homeScreenFragment == null) {
                            homeScreenFragment = new HomeScreenFragment(itemHomeScreen);
                        }
                        pageToShow = homeScreenFragment;
                        break;

                    case R.id.tab_consultation:
                        if (homeFragmentS1 == null) {
                            homeFragmentS1 = new HomeFragmentS1();
                        }
                        pageToShow = homeFragmentS1;
                        break;

                    case R.id.tab_appointments:
                        if (appointmentFragment == null) {
                            appointmentFragment = new AppointmentsFragment();
                        }
                        pageToShow = appointmentFragment;
                        break;

                    case R.id.tab_medicine:
//                        pageToShow = new Medicine();
                        Intent mIntent = new Intent(DashboardMapActivity.this, MedicineActivity.class);
                        startActivity(mIntent);
                        break;

                    case R.id.tab_account:
                        pageToShow = new AccountFragment();
                        break;

                }

                // load selected page
                if (pageToShow != null) {
                    loadFragment(pageToShow);
                }

                return pageToShow != null;

            }
        });

    }

    // Bottom Tab bar - Show selected Fragment
    public void loadFragment(Fragment pageToShow) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, pageToShow).commit();
    }

    // Add Badge with item id
    public static void showBadge(Context context, BottomNavigationView bottomNavigationView, int itemId, String value) {

        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        View badge = LayoutInflater.from(context).inflate(R.layout.tab_badge, bottomNavigationView, false);

        TextView txtBadge = badge.findViewById(R.id.badge_text_view);
        txtBadge.setText(value);
        itemView.addView(badge);

    }

    // Remove Badge with item id
    public static void removeBadge(BottomNavigationView bottomNavigationView, int itemId) {

        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }

    }

    @Override
    public void onBackPressed() {
        Utilities.showAlertDialogWithOptions(DashboardMapActivity.this, getResources().getString(R.string.alert_exit), new String[]{getResources().getString(R.string.btn_yes), getResources().getString(R.string.cancel)}, new UtilitiesInterfaces.AlertCallback() {
            @Override
            public void onOptionClick(DialogInterface dialog, int buttonIndex) {
                if (buttonIndex == 0) {
                    finish();
                }
            }
        });
    }

    // MARK : Choose your location via location picker
    public void launchPlacePicker() {

        showPlacePicker(new CustomMapActivity.PlacePickCallback() {
            @Override
            public void receiveSelectedPlace(Boolean status, Place selectedPlace) {

                String addressInfo = "";
                if (status) {

                    if (selectedPlace != null) {

                        // Name
                        addressInfo = (selectedPlace.getName() != null && selectedPlace.getName().length() > 0) ? selectedPlace.getName().toString() : addressInfo;

                        // Address
                        addressInfo = (selectedPlace.getAddress() != null && selectedPlace.getAddress().length() > 0) ? addressInfo + ", " + selectedPlace.getAddress().toString() : addressInfo;

                        // Phone
//                        addressInfo = (selectedPlace.getPhoneNumber() != null && selectedPlace.getPhoneNumber().length() > 0)? addressInfo + ", Phone: " + selectedPlace.getPhoneNumber().toString() : addressInfo;

                        // Location
                        addressInfo = (selectedPlace.getLatLng() != null) ? addressInfo + ", " + selectedPlace.getLatLng() : addressInfo;
                        HashMap<String, Object> inputParams = AppPreferences.getInstance().sendingInputParam();

                        inputParams.put("Lattitude", "" + selectedPlace.getLatLng().latitude);
                        inputParams.put("Longitude", "" + selectedPlace.getLatLng().longitude);
                        inputParams.put("LocationAddress", addressInfo);
                        Utilities.updateLocation(DashboardMapActivity.this, inputParams);

                    }

                }

                // Set the address details
                if (addressInfo.length() > 0) {

//                    addressTxtVw.setVisibility(View.VISIBLE);
//                    addressTxtVw.setText(addressInfo);
//                    techRequired.setSelected(true);

                }
            }
        });
    }
    void getCountryCodeFromServer() {
        HashMap<String, Object> inputParams = new HashMap<String, Object>();
        try {
            inputParams.put("PatientID", AppPreferences.getInstance().getUserInfo().getString("PatientID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        SoapAPIManager apiManager = new SoapAPIManager(this, inputParams, new APICallback() {
            @Override
            public void responseCallback(Context context, String response) throws JSONException {
                Log.e(TAG, response);

                try {
                    JSONArray responseAry = new JSONArray(response);
                    if (responseAry.length() > 0) {
                        JSONObject commonDataInfo = responseAry.getJSONObject(0);
                        if (commonDataInfo.has("APIStatus") && Integer.parseInt(commonDataInfo.getString("APIStatus")) == -1) {
                            if (commonDataInfo.has("APIStatus") && !commonDataInfo.getString("Message").isEmpty()) {
                                Utilities.showAlert(DashboardMapActivity.this, commonDataInfo.getString("Message"), false);
                            } else {
                                Utilities.showAlert(DashboardMapActivity.this, "Please check again!", false);
                            }

                        }else{
                            try{
                                String cc = commonDataInfo.getString("countryCode");
                                AppPreferences.getInstance().setCountryCode(cc);

                            }catch (Exception e){
                                Log.e(TAG, "responseCallback: not able to save country code" );
                            }
                        }



                    }
                } catch (Exception e) {

                }
            }
        }, true);
        String[] url = {Config.WEB_Services1, Config.GET_PATIENT_DATA, "GET"};

        if (Utilities.isNetworkAvailable(DashboardMapActivity.this)) {
            apiManager.execute(url);
        } else {
            Utilities.showAlert(DashboardMapActivity.this, "No Internet!", false);

        }
    }

    void getStateCityList() {
        HashMap<String, Object> inputParams = new HashMap<String, Object>();


        SoapAPIManager apiManager = new SoapAPIManager(this, inputParams, new APICallback() {
            @Override
            public void responseCallback(Context context, String response) throws JSONException {
                Log.e("***response***", response);

                try {
                    JSONArray responseAry = new JSONArray(response);
                    if (responseAry.length() > 0) {
                        JSONObject commonDataInfo = responseAry.getJSONObject(0);
                        if (commonDataInfo.has("APIStatus") && Integer.parseInt(commonDataInfo.getString("APIStatus")) == -1) {
                            if (commonDataInfo.has("APIStatus") && !commonDataInfo.getString("Message").isEmpty()) {
                                Utilities.showAlert(DashboardMapActivity.this, commonDataInfo.getString("Message"), false);
                            } else {
                                Utilities.showAlert(DashboardMapActivity.this, "Please check again!", false);
                            }
                            return;

                        }
                        JSONArray cityAry = commonDataInfo.getJSONArray("City");
                        JSONArray stateAry = commonDataInfo.getJSONArray("State");

                        GlobValues.setCityAry(cityAry);
                        GlobValues.setStateAry(stateAry);


                    }
                } catch (Exception e) {

                }
            }
        }, true);
        String[] url = {Config.WEB_Services1, Config.GET_APP_COMMON_DATA, "POST"};

        if (Utilities.isNetworkAvailable(DashboardMapActivity.this)) {
            apiManager.execute(url);
        } else {
            Utilities.showAlert(DashboardMapActivity.this, "No Internet!", false);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(false){
//            PlayStoreUpdateView.versionCheck1(DashboardMapActivity.this);
//        }
    }

}

