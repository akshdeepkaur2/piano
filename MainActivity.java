package com.example.Particle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

public class MainActivity extends AppCompatActivity {
    // MARK: Debug info
    private final String TAG="saw";

    // MARK: Particle Account Info
    private final String PARTICLE_USERNAME = "akshdeepkaur235@gmail.com";
    private final String PARTICLE_PASSWORD = "a9463931734";

    // MARK: Particle device-specific info
    private final String DEVICE_ID = "31002e000447363333343435";

    // MARK: Particle Publish / Subscribe variables
    private long subscriptionId;

    // MARK: Particle device
    private ParticleDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize your connection to the Particle API
        ParticleCloudSDK.init(this.getApplicationContext());

        // 2. Setup your device variable
        getDeviceFromCloud();

    }


    /**
     * Custom function to connect to the Particle Cloud and get the device
     */
    public void getDeviceFromCloud() {
        // This function runs in the background
        // It tries to connect to the Particle Cloud and get your device
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logIn(PARTICLE_USERNAME, PARTICLE_PASSWORD);
                mDevice = particleCloud.getDevice(DEVICE_ID);
                return -1;
            }

            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "Successfully got device from Cloud");
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });
    }


    public void changeColorsPressed(View view) {
        Log.d("pirate", "Button pressed;");

        // logic goes here

        // 1. get the r,g,b value from the UI
        EditText red = (EditText) findViewById(R.id.red);
        EditText green = (EditText) findViewById(R.id.green);
        EditText blue = (EditText) findViewById(R.id.blue);

        // Convert these to strings
        String r = red.getText().toString();
        String g = green.getText().toString();
        String b = blue.getText().toString();

        Log.d(TAG, "Red: " + r);
        Log.d(TAG, "Green: " + g);
        Log.d(TAG, "Blue: " + b);


        String commandToSend = r + "," + g + "," + b;
        Log.d(TAG, "Command to send to particle: " + commandToSend);


        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                // 2. build a list and put the r,g,b into the list
                List<String> functionParameters = new ArrayList<String>();
                functionParameters.add(commandToSend);

                // 3. send the command to the particle
                try {
                    mDevice.callFunction("colors", functionParameters);
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }


                return -1;
            }

            @Override
            public void onSuccess(Object o) {

                Log.d(TAG, "Sent colors command to device.");
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });

    }

public void subscribeButton(View view){
    Log.d(TAG, "Subscribe button pressed");


    // check if device is null
    // if null, then show error
    if (mDevice == null) {
        Log.d(TAG, "Cannot find device");
        return;
    }


    Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

        @Override
        public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
            subscriptionId = ParticleCloudSDK.getCloud().subscribeToAllEvents(
                    "broadcastMessage",  // the first argument, "eventNamePrefix", is optional
                    new ParticleEventHandler() {
                        public void onEvent(String eventName, ParticleEvent event) {
                            Log.i(TAG, "Received event with payload: " + event.dataPayload);
                        }

                        public void onEventError(Exception e) {
                            Log.e(TAG, "Event error: ", e);
                        }
                    });


            return -1;
        }

        @Override
        public void onSuccess(Object o) {
            Log.d(TAG, "Successfully got device from Cloud");
        }

        @Override
        public void onFailure(ParticleCloudException exception) {
            Log.d(TAG, exception.getBestMessage());
        }
    });
}

}
