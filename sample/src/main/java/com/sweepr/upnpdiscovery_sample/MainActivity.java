package com.sweepr.upnpdiscovery_sample;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sweepr.upnpdiscovery.ResultHandler;
import com.sweepr.upnpdiscovery.UPnPDevice;
import com.sweepr.upnpdiscovery.UPnPDiscovery;

import java.util.ArrayList;
import java.util.HashSet;

import io.resourcepool.ssdp.client.SsdpClient;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context mContext;
    HashSet<UPnPDevice> devices = new HashSet<>();
    ArrayList<String> myDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        final SsdpClient client = SsdpClient.create();
        DiscoveryRequest all = SsdpRequest.discoverAll();
        DiscoveryRequest dial = DiscoveryRequest.builder()
                .serviceType("urn:dial-multiscreen-org:service:dial:1")
                .build();

        client.discoverServices(dial, new DiscoveryListener() {
            @Override
            public void onServiceDiscovered(SsdpService service) {
                Log.d("Discovery", "Service " + service.toString());
                UPnPDevice deviceDiscovered = new UPnPDevice(service.getRemoteIp().getHostAddress(), service.getLocation(), service.getSerialNumber(), service.getServiceType(), mContext);
                UPnPDiscovery.getDataFrom(service.getLocation(), deviceDiscovered, mContext, new ResultHandler<UPnPDevice>() {
                    @Override
                    public void onSuccess(UPnPDevice data) {
                        Log.d("mecagoentodo",data.toString());
                        devices.add(data);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d("upnpdevice", e.getLocalizedMessage());
                    }
                });
            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                Log.d("Discovery", announcement.toString());
            }

            @Override
            public void onFailed(Exception ex){
                Log.d("Discovery", ex.getLocalizedMessage());
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Log.d("Final", devices.toString());
                client.stopDiscovery();
            }
        }, 20000);

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<String> mDataset;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            ViewHolder(View view) {
                super(view);
                mTextView = (TextView) view.findViewById(R.id.textView);
            }
        }

        MyAdapter(ArrayList<String> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_row_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mDataset.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }




}
