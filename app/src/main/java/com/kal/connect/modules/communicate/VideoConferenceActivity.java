package com.kal.connect.modules.communicate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
//import androidx.annotation.NonNull;
//import androidx.constraintlayout.widget.ConstraintLayout ;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.kal.connect.R;
import com.kal.connect.appconstants.OpenTokConfigConstants;
import com.kal.connect.customLibs.HTTP.GetPost.APICallback;
import com.kal.connect.customLibs.HTTP.GetPost.SoapAPIManager;
import com.kal.connect.utilities.AppPreferences;
import com.kal.connect.utilities.Config;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.kal.connect.modules.dashboard.DashboardMapActivity;
import com.kal.connect.utilities.GlobValues;
import com.kal.connect.utilities.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.kal.connect.appconstants.OpenTokConfigConstants.TYPE_ERROR_FOR_DOCTOR;
import static com.kal.connect.utilities.Config.IMAGE_URL_FOR_SPEED;

public class VideoConferenceActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
        Publisher.PublisherListener,
        SubscriberKit.SubscriberListener,
        Session.SessionListener, SubscriberKit.VideoListener, Session.SignalListener, Session.ReconnectionListener {

    private static final String TAG = "VideoConfrnceAct";
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    boolean callConnected = false;

    private Session mSession;
    private Publisher mPublisher;

    Boolean isEndCall = false;

    String docId = "";

    private ArrayList<Subscriber> mSubscribers = new ArrayList<Subscriber>();
    private HashMap<Stream, Subscriber> mSubscriberStreams = new HashMap<Stream, Subscriber>();

    private ConstraintLayout mContainer;
    CountDownTimer countDownTimer;

    @BindView(R.id.initial)
    TextView initialText;

    @BindView(R.id.doctorName)
    TextView doctorName;

    @BindView(R.id.timer)
    TextView timer;

    @BindView(R.id.dialerVw)
    RelativeLayout dialerView;

    @BindView(R.id.videoCallLayout)
    RelativeLayout videoCallLayout;

    @BindView(R.id.temp_publisher_id)
    FrameLayout mPublisherViewContainer;

    @BindView(R.id.temp_subscriber_id)
    FrameLayout mSubscriberViewContainer;

    @BindView(R.id.temp_subscriber_id_additional)
    FrameLayout mSubscriberViewContainerAdditional;

    @BindView(R.id.video_disabled_video)
    RelativeLayout videoDisabledView;

    @BindView(R.id.video_disabled_additional)
    RelativeLayout videoDisabledViewAdditional;

    @BindView(R.id.ll_temp_subscriper)
    LinearLayout mLlVideoSubscriberRoot;

    @BindView(R.id.ll_subscriber_id_additional)
    LinearLayout mLlVideoSubscriberRootAdditional;


    private Subscriber mSubscriber, mSubscriberAdditional;

    boolean isMovingToHome = false;
    private String message="";
    private CountDownTimer patientPresentTimer;
    private boolean isDoctorPresent = false;
    private TextView tv_patientPresent;

    long startTime;
    long endTime;
    long fileSize;
    OkHttpClient client = new OkHttpClient();

    // bandwidth in kbps
    private int POOR_BANDWIDTH = 350;
    private int AVERAGE_BANDWIDTH = 550;
    private int GOOD_BANDWIDTH = 2000;
    @OnClick(R.id.disconnect)
    void disconnectCall() {
        if (Config.isDisconnect) {
            disconnectSession();
            moveToHome();
        } else {
            getEndCall();
        }
    }

    @OnClick(R.id.switch_camera)
    void switchCamera() {
        try {
            if (mPublisher != null)
                mPublisher.cycleCamera();
//            Request request = new Request.Builder()
//                    .url(IMAGE_URL_FOR_SPEED)
//                    //.url("Image")
//                    .build();
//
//            startTime = System.currentTimeMillis();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//                    Headers responseHeaders = response.headers();
//                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                        Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }
//
//                    InputStream input = response.body().byteStream();
//
//                    try {
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        byte[] buffer = new byte[1024];
//
//                        while (input.read(buffer) != -1) {
//                            bos.write(buffer);
//                        }
//                        byte[] docBuffer = bos.toByteArray();
//                        fileSize = bos.size();
//
//                    } finally {
//                        input.close();
//                    }
//
//                    endTime = System.currentTimeMillis();
//
//
//                    // calculate how long it took by subtracting endtime from starttime
//
//                    double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
//                    double timeTakenInSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
//                    final int kilobytePerSec = (int) Math.round(1024 / timeTakenInSecs);
//
//
//                    Log.e(TAG, "KBPS: " + kilobytePerSec);
//                    mSession.sendSignal(TYPE_ERROR_FOR_DOCTOR, "This is a test message!\nSpeed:- "+kilobytePerSec+" KBPS");
//
//
//                }
//
//                @Override
//                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    e.printStackTrace();
//
//                }
//
//            });

        } catch (Exception e) {
            //Utilities.showAlert(VideoConferenceActivity.this, "Signal not sent", false);
        }

    }

    @OnClick(R.id.dialer_disconnect)
    void dialerDisconnectCall() {
        getEndCall();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_call_communication);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        docId = getIntent().getStringExtra("docId");

        Config.mActivity = this;
        ButterKnife.bind(this);
        tv_patientPresent = findViewById(R.id.tv_patient_present);

        mLlVideoSubscriberRootAdditional.setVisibility(View.GONE);

        message = "Thank You for using our service. You can check details in Appointment Section. Stay Healthy!!";

        dialHandler(getIntent().getExtras().getInt("CALL_TYPE") == 2);

        requestPermissions();
    }


    private void checkInternetSpeed() {

        Request request = new Request.Builder()
                .url(IMAGE_URL_FOR_SPEED)
                //.url("Image")
                .build();

        startTime = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                InputStream input = response.body().byteStream();

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    while (input.read(buffer) != -1) {
                        bos.write(buffer);
                    }
                    byte[] docBuffer = bos.toByteArray();
                    fileSize = bos.size();

                } finally {
                    input.close();
                }

                endTime = System.currentTimeMillis();


                // calculate how long it took by subtracting endtime from starttime

                double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
                double timeTakenInSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
                final int kilobytePerSec = (int) Math.round(1024 / timeTakenInSecs);

//                if (kilobytePerSec <= POOR_BANDWIDTH) {
//                    // slow connection
//                    Utilities.showAlert(VideoConferenceActivity.this, "Slow Internet Detected", false);
//                    try{
//                        mSession.sendSignal(TYPE_ERROR_FOR_DOCTOR, "Patient having slow internet!");
//                    }catch (Exception e){}
//                }

                Log.e(TAG, "KBPS: " + kilobytePerSec);


            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();

            }

        });


    }
    void dialHandler(boolean shouldShow) {
        if (shouldShow) {
            dialerView.setVisibility(View.VISIBLE);
            videoCallLayout.setVisibility(View.GONE);
            try {
                if (!getIntent().getExtras().getString("CALER_NAME").isEmpty()) {
                    String docName = getIntent().getExtras().getString("CALER_NAME");

                    doctorName.setText(docName);
                    initialText.setText(docName.charAt(0));
                }
            } catch (Exception e) {
            }

            countDownTimer = new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    timer.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                }

                public void onFinish() {
                    try{
                        if (!callConnected) {
                            message = "Doctor is not able to pick up your call right now. Please wait for some time.";
                            dialerDisconnectCall();
                        }
                    }catch (Exception e){
                        Log.e(TAG, "onFinish: "+e );
                    }


                }
            };
            countDownTimer.start();

        } else {
            if (countDownTimer != null)
                countDownTimer.cancel();
            dialerView.setVisibility(View.GONE);
            videoCallLayout.setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");

        super.onResume();

        if (mSession == null) {
            return;
        }
        mSession.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");

        super.onPause();

        if (mSession == null) {
            return;
        }
        mSession.onPause();

        if (isFinishing()) {
            disconnectSession();
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");

        disconnectSession();
        try{
            if (countDownTimer!=null){
                countDownTimer.cancel();
            }

        }catch (Exception e){}

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.e(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.e(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.settings))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }


    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
        if (EasyPermissions.hasPermissions(this, perms)) {

            Log.e(TAG, OpenTokConfigConstants.API_KEY+"\n\n"+ OpenTokConfigConstants.SESSION_ID+"\n\n"+ OpenTokConfigConstants.TOKEN);

            mSession = new Session.Builder(this, OpenTokConfigConstants.API_KEY, OpenTokConfigConstants.SESSION_ID).sessionOptions(new Session.SessionOptions() {
                @Override
                public boolean useTextureViews() {
                    return true;
                }
            }).build();
            mSession.setSessionListener(this);
            mSession.setSignalListener(this);
            mSession.setReconnectionListener(this);
            mSession.connect(OpenTokConfigConstants.TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onConnected(Session session) {

        Log.e(TAG, "onConnected: Connected to session " + session.getSessionId());
        // initialize Publisher and set this object to listen to Publisher events

        if (getIntent().getExtras().getInt("CALL_TYPE") == 1) {
            setPublisherView();
        }

    }


    @Override
    public void onDisconnected(Session session) {
        Log.e(TAG, "onDisconnected: disconnected from session " + session.getSessionId());

        try{
            checkInternetSpeed();
        }catch (Exception e){}
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());

            //mSession.sendSignal(TYPE_ERROR_FOR_DOCTOR, "Error Occurred at Patient's end");
            moveToHome();


    }


    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Config.isDisconnect = true;
        callConnected = true;
        tv_patientPresent.setVisibility(View.GONE);
        isDoctorPresent = true;
        Log.e(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());


        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriber.setSubscriberListener(this);
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
            mSubscriber.setVideoListener(this);

            if (getIntent().getExtras().getInt("CALL_TYPE") == 2) {
                setPublisherView();
            }

            dialHandler(false);
            return;
        }

        if (mSubscriberAdditional == null) {
            mSubscriberAdditional = new Subscriber.Builder(this, stream).build();
            mSubscriberAdditional.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriberAdditional.setSubscriberListener(this);
            mSession.subscribe(mSubscriberAdditional);
            mSubscriberViewContainerAdditional.addView(mSubscriberAdditional.getView());
            mSubscriberAdditional.setVideoListener(this);
            mLlVideoSubscriberRootAdditional.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.e(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());
        Config.isDisconnect = false;
        tv_patientPresent.setVisibility(View.VISIBLE);

        if (mSubscriberAdditional != null && mSubscriberAdditional.getStream() != null && mSubscriberAdditional.getStream().getStreamId().equalsIgnoreCase(stream.getStreamId())) {
            mSubscriberAdditional = null;
            mLlVideoSubscriberRootAdditional.setVisibility(View.GONE);
        }

        if (mSubscriber != null && mSubscriber.getStream() != null && mSubscriber.getStream().getStreamId().equalsIgnoreCase(stream.getStreamId())) {
            mSubscriber = null;
            mLlVideoSubscriberRoot.setVisibility(View.GONE);
        }

        if (mSubscriber == null && mSubscriberAdditional == null) {
            moveToHome();
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.e(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");
        patientPresentTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                tv_patientPresent.setText("Connecting... \n" + String.format("%02d",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))+" Sec");
            }

            public void onFinish() {
                try{
                    tv_patientPresent.setText("Reconnecting... ");
                }catch (Exception e){}
                if(!isDoctorPresent){
                    try{
                        message = "Apologies! Doctor is not able to connect to the session.";
                        disconnectCall();

                    }catch (Exception e){}
                }

            }
        };
        patientPresentTimer.start();
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.e(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
        Config.isDisconnect = false;
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");
        Config.isDisconnect = false;
        //Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        try{
            checkInternetSpeed();
            mSession.sendSignal(TYPE_ERROR_FOR_DOCTOR, "Error Occurred at Patient's end");
            moveToHome();

        }catch (Exception e){}
    }

    public void setPublisherView() {
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        // set publisher video style to fill view
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }


        mSession.publish(mPublisher);
    }

    public void moveToHome() {
        if (isMovingToHome) {
            finish();
            return;
        }

        isMovingToHome = true;
        if (GlobValues.getAddAppointmentParams() != null)
            GlobValues.getAddAppointmentParams().clear();
        Intent homeScreen = new Intent(getApplicationContext(), CallEndedActivity.class);
        homeScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeScreen.putExtra("message", message);
        startActivity(homeScreen);
        finish();
//        if (isMovingToHome) {
//            return;
//        }
//
//        isMovingToHome = true;
//
//        ConfirmDialog confirmDialog = new ConfirmDialog(VideoConferenceActivity.this, false, message, new ConfirmDialog.DialogListener() {
//            @Override
//            public void onYes() {
//                Intent homeScreen = new Intent(getApplicationContext(), DashboardMapActivity.class);
//                homeScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(homeScreen);
//                finish();
//            }
//
//            @Override
//            public void onNO() {
//
//            }
//        }, new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//
//            }
//        });
//        if (getApplicationContext() != null) {
//            Objects.requireNonNull(confirmDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            confirmDialog.show();
//        }
    }


    public void moveToHomeWithoutConfirmationDialog() {
        if (GlobValues.getAddAppointmentParams() != null)
            GlobValues.getAddAppointmentParams().clear();

        isMovingToHome = true;

        Intent homeScreen = new Intent(getApplicationContext(), DashboardMapActivity.class);
        homeScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeScreen);
        finish();
    }


    private void disconnectSession() {

        try {

            if (mSubscriber != null) {
                mSession.unsubscribe(mSubscriber);
                mSubscriber.destroy();
            }

            if (mSubscriberAdditional != null) {
                mSession.unsubscribe(mSubscriberAdditional);
                mSubscriberAdditional.destroy();
            }

            if (mPublisher != null) {
                mSession.unpublish(mPublisher);
                mPublisher.destroy();
                mPublisher = null;
            }

            if (mSession != null) {
                mSession.disconnect();

            }

        } catch (Exception e) {
            Log.e(TAG, "disconnectSession: "+e );
        }


    }


    @Override
    public void onSignalReceived(Session session, String s, String s1, Connection connection) {
        Log.e(TAG, "onSignalReceived: "+s+"\n\n"+s1 );

        if (!s.equals(TYPE_ERROR_FOR_DOCTOR)){
            Utilities.showAlert(VideoConferenceActivity.this, s1, false);
        }

    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {

    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        Log.e(TAG, "&&&&&&&&&&  onVideoDisabled  &&&&&&&&&");
        if (subscriberKit != null && mSubscriber != null && subscriberKit.getStream() != null && subscriberKit.getStream().getStreamId().equalsIgnoreCase(mSubscriber.getStream().getStreamId())) {
            videoDisabledView.setVisibility(View.VISIBLE);
            mSubscriberViewContainer.setVisibility(View.GONE);
            mLlVideoSubscriberRoot.setVisibility(View.VISIBLE);
        }

        if (subscriberKit != null && mSubscriberAdditional != null && subscriberKit.getStream() != null && subscriberKit.getStream().getStreamId().equalsIgnoreCase(mSubscriberAdditional.getStream().getStreamId())) {
            mLlVideoSubscriberRootAdditional.setVisibility(View.VISIBLE);
            mSubscriberViewContainerAdditional.setVisibility(View.GONE);
            videoDisabledViewAdditional.setVisibility(View.VISIBLE);

        }


    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        Log.e(TAG, "&&&&&&&&&&  onVideoEnabled  &&&&&&&&&");

        if (subscriberKit.getStream().getStreamId().equalsIgnoreCase(mSubscriber.getStream().getStreamId())) {
            videoDisabledView.setVisibility(View.GONE);
            mSubscriberViewContainer.setVisibility(View.VISIBLE);
            mLlVideoSubscriberRoot.setVisibility(View.VISIBLE);
        }
        if (subscriberKit != null && mSubscriberAdditional != null && subscriberKit.getStream() != null && subscriberKit.getStream().getStreamId().equalsIgnoreCase(mSubscriberAdditional.getStream().getStreamId())) {
            mLlVideoSubscriberRootAdditional.setVisibility(View.VISIBLE);
            mSubscriberViewContainerAdditional.setVisibility(View.VISIBLE);
            videoDisabledViewAdditional.setVisibility(View.GONE);

        }

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {
        Log.e(TAG, "&&&&&&&&&&  onVideoDisableWarning  &&&&&&&&&");
        try{
            checkInternetSpeed();
        }catch (Exception e){}

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {
        Log.e(TAG, "&&&&&&&&&&  onVideoDisableWarningLifted  &&&&&&&&&");
        try{
            checkInternetSpeed();
        }catch (Exception e){}
    }


    public void getEndCall() {

        if (countDownTimer != null)
            countDownTimer.cancel();
        dialerView.setVisibility(View.GONE);
        //videoCallLayout.setVisibility(View.VISIBLE);

        Log.e(TAG, "getEndCall: "+docId);
        HashMap<String, Object> endCallParams = new HashMap<>();
        endCallParams.put("User_id", docId);
        endCallParams.put("SpecialistID", docId);

        JSONObject accInfo = AppPreferences.getInstance().getUserInfo();

        try {
            endCallParams.put("PatientID", accInfo.getString("PatientID"));
            endCallParams.put("SpecialistID", accInfo.getString("PatientID"));

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getEndCall: "+e);
        }
        SoapAPIManager apiManager = new SoapAPIManager(VideoConferenceActivity.this, endCallParams, new APICallback() {
            @Override
            public void responseCallback(Context context, String response) throws JSONException {
                Log.e(TAG, "responseCallback: "+response );

                JSONArray mJsonArray = new JSONArray(response);
                JSONObject mJsonObject = mJsonArray.getJSONObject(0);
                if (mJsonObject.has("APIStatus")) {
                    if (mJsonObject.getString("APIStatus").equalsIgnoreCase("1")) {
                        moveToHome();
                    }else{
                        Utilities.showAlert(VideoConferenceActivity.this, "Something went wrong...", false);

                    }
                }

            }
        }, true);
        String[] url = {Config.WEB_Services4, Config.END_CALL, "POST"};

        if (Utilities.isNetworkAvailable(VideoConferenceActivity.this)) {
            apiManager.execute(url);
        } else {
            Utilities.showAlert(VideoConferenceActivity.this, "No Internet", false);

        }
    }

    @Override
    public void onReconnecting(Session session) {
        mSession.sendSignal(TYPE_ERROR_FOR_DOCTOR, "Patient Reconnecting...");
    }

    @Override
    public void onReconnected(Session session) {
        mSession.sendSignal(TYPE_ERROR_FOR_DOCTOR, "Patient Reconnected!");

    }
}
