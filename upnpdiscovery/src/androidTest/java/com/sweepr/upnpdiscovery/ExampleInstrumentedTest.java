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

    @Test
    public void parse_ssdt_device_info_with_success() {
        final String decl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n" +
                "  <URLBase>http://192.168.1.254:80</URLBase>\n" +
                "  <device>\n" +
                "    <deviceType>urn:schemas-upnp-org:device:InternetGatewayDevice:1</deviceType>\n" +
                "    <friendlyName>Ubee EVW3226</friendlyName>\n" +
                "    <serviceList>\n" +
                "      <service>\n" +
                "        <serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType>\n" +
                "        <controlURL>/ctl/L3F</controlURL>\n" +
                "        <eventSubURL>/evt/L3F</eventSubURL>\n" +
                "        <SCPDURL>/L3F.xml</SCPDURL>\n" +
                "      </service>\n" +
                "    </serviceList>\n" +
                "    <deviceList>\n" +
                "      <device>\n" +
                "        <deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>\n" +
                "        <friendlyName>WANDevice</friendlyName>\n" +
                "        <serviceList>\n" +
                "          <service>\n" +
                "            <serviceType>urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1</serviceType>\n" +
                "            <serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>\n" +
                "            <controlURL>/ctl/CmnIfCfg</controlURL>\n" +
                "            <eventSubURL>/evt/CmnIfCfg</eventSubURL>\n" +
                "            <SCPDURL>/WANCfg.xml</SCPDURL>\n" +
                "          </service>\n" +
                "        </serviceList>\n" +
                "        <deviceList>\n" +
                "          <device>\n" +
                "            <deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>\n" +
                "            <friendlyName>WANConnectionDevice</friendlyName>\n" +
                "            <serviceList>\n" +
                "              <service>\n" +
                "                <serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>\n" +
                "                <controlURL>/ctl/IPConn</controlURL>\n" +
                "                <eventSubURL>/evt/IPConn</eventSubURL>\n" +
                "                <SCPDURL>/WANIPCn.xml</SCPDURL>\n" +
                "              </service>\n" +
                "            </serviceList>\n" +
                "          </device>\n" +
                "        </deviceList>\n" +
                "      </device>\n" +
                "    </deviceList>\n" +
                "  </device>\n" +
                "</root>";

        final UPnPDevice device = UPnPDevice.fromXml(decl);

        assertEquals("urn:schemas-upnp-org:device:InternetGatewayDevice:1", device.getDeviceType());
        assertEquals("Ubee EVW3226", device.getFriendlyName());
    }
}
