package com.sweepr.upnpdiscovery;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class UPnPDiscovery extends AsyncTask {

    private static final String TAG = UPnPDiscovery.class.getSimpleName();

    private static int DISCOVER_TIMEOUT = 1500;
    private static final String LINE_END = "\r\n";
    private static final String DEFAULT_QUERY = "M-SEARCH * HTTP/1.1" + LINE_END +
            "HOST: 239.255.255.250:1900" + LINE_END +
            "MAN: \"ssdp:discover\"" + LINE_END +
            "MX: 1" + LINE_END +
            //"ST: urn:schemas-upnp-org:service:AVTransport:1" + LINE_END + // Use for Sonos
            //"ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1" + LINE_END + // Use for Routes
            "ST: ssdp:all" + LINE_END + // Use this for all UPnP Devices
            LINE_END;
    private static int DEFAULT_PORT = 1900;
    private static final String DEFAULT_ADDRESS = "239.255.255.250";

    private HashSet<UPnPDevice> devices = new HashSet<>();
    private Context mContext;
    private Handler mHandler;
    private OnDiscoveryListener mListener;
    private int mThreadsCount = 0;
    private String mCustomQuery;
    private String mInetAddress;
    private int mPort;

    public interface OnDiscoveryListener {
        void onDiscoveryStart();

        void onDiscoveryFoundNewDevice(UPnPDevice device);

        void onDiscoveryFinish(HashSet<UPnPDevice> devices);

        void onDiscoveryError(Exception e);
    }

    private UPnPDiscovery(@NonNull Context context, @Nullable Handler handler, OnDiscoveryListener listener) {
        mContext = context.getApplicationContext();
        mHandler = handler != null ? handler : new Handler(Looper.getMainLooper());
        mListener = listener;
        mThreadsCount = 0;
        mCustomQuery = DEFAULT_QUERY;
        mInetAddress = DEFAULT_ADDRESS;
        mPort = DEFAULT_PORT;
    }

    private UPnPDiscovery(@NonNull Context context, @Nullable Handler handler, OnDiscoveryListener listener, String customQuery, String address, int port) {
        mContext = context.getApplicationContext();
        mHandler = handler != null ? handler : new Handler(Looper.getMainLooper());
        mListener = listener;
        mThreadsCount = 0;
        mCustomQuery = customQuery;
        mInetAddress = address;
        mPort = port;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Log.e("DoBackground", "We enter");
        Log.d("DoBackground", "Enter in background " + mThreadsCount);
        mHandler.post(new Runnable() {
            public void run() {
                mListener.onDiscoveryStart();
            }
        });
        WifiManager wifi = null;
        if (wifi != null) {
            Log.d("DoBackground", "Lock wifi " + mThreadsCount);
            WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
            if(!lock.isHeld()) {
                lock.acquire();
            }
            DatagramSocket socket = null;
            try {
                Log.d("DoBackground", "Try " + mThreadsCount);
                InetAddress group = InetAddress.getByName(mInetAddress);
                int port = mPort;
                String query = mCustomQuery;
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                socket.bind(new InetSocketAddress(port));

                DatagramPacket datagramPacketRequest = new DatagramPacket(query.getBytes(), query.length(), group, port);
                socket.send(datagramPacketRequest);

                long time = System.currentTimeMillis();
                long curTime = System.currentTimeMillis();
                long rest = curTime - time;
                while (curTime - time < 1000) {

                    DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(datagramPacket);

                    String response = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                    if (response.substring(0, 12).toUpperCase().equals("HTTP/1.1 200")) {
                        UPnPDevice device = new UPnPDevice(datagramPacket.getAddress().getHostAddress(), response, mContext);
                        mThreadsCount++;

                        getData(device.getLocation(), device);
                    }
                    curTime = System.currentTimeMillis();
                }

            } catch (final IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    public void run() {
                        mListener.onDiscoveryError(e);
                    }
                });
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
            lock.release();
        }
        return null;
    }

    private void getData(final String url, final UPnPDevice device) {
        if (url != null && !url.equals("")) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            device.update(response);
                            mListener.onDiscoveryFoundNewDevice(device);
                            devices.add(device);
                            mThreadsCount--;
                            if (mThreadsCount == 0) {
                                mHandler.post(new Runnable() {
                                    public void run() {
                                        mListener.onDiscoveryFinish(devices);
                                    }
                                });
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mThreadsCount--;
                    Log.d(TAG, "URL: " + url + " get content error!");
                }
            });
            stringRequest.setTag(TAG + "SSDP description request");
            Volley.newRequestQueue(mContext).add(stringRequest);
        }
    }

    public static void getDataFrom(final String url, final UPnPDevice device, Context context, final ResultHandler<UPnPDevice> result) {
        if (url != null && !url.equals("")) {
            Log.d("URL", "GetDataFrom " + url);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            device.update(response);
                            result.onSuccess(device);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    result.onFailure(error);
                    Log.d(TAG, "URL: " + url + " get content error!");
                }
            });
            stringRequest.setTag("URL" + "SSDP description request");
            Volley.newRequestQueue(context).add(stringRequest);
        }
    }

    public static boolean discoveryDevices(@NonNull Context context, @Nullable Handler handler, OnDiscoveryListener listener) {
        UPnPDiscovery discover = new UPnPDiscovery(context, handler, listener);
        discover.execute();
        try {
            Thread.sleep(DISCOVER_TIMEOUT);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static boolean discoveryDevices(@NonNull Context context, @Nullable Handler handler, OnDiscoveryListener listener, String customQuery, String address, int port) {
        UPnPDiscovery discover = new UPnPDiscovery(context, handler, listener, customQuery, address, port);
        discover.execute();
        try {
            Thread.sleep(DISCOVER_TIMEOUT);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

}
