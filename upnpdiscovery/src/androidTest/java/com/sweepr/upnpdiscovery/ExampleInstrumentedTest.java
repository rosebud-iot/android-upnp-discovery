package com.sweepr.upnpdiscovery;

import android.content.Context;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.sweepr.upnpdiscovery.androidTest", appContext.getPackageName());
    }

    @Test
    public void run_discovery_for_10sec() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        UPnPDiscovery.discoveryDevices(appContext, executor, 10000, null, new UPnPDiscovery.OnDiscoveryListener() {
            @Override
            public void onDiscoveryStart() {
                Log.d(TAG, "Discovery started");
            }

            @Override
            public void onDiscoveryFoundNewDevice(@NonNull UPnPDevice device) {
                Log.d(TAG, "Device found: " + device.getFriendlyName());
            }

            @Override
            public void onDiscoveryFinish(@NonNull Set<UPnPDevice> devices) {
                Log.d(TAG, "Discovery finished with: " + devices.size() +  " devices");
            }

            @Override
            public void onDiscoveryError(@NonNull Exception e) {
                Log.e(TAG, "Error: " + e.getLocalizedMessage());
            }
        });
    }
}
