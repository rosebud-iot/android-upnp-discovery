package com.rosebud.upnpdiscovery_sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rosebud.upnpdiscovery.UPnPDevice;
import com.rosebud.upnpdiscovery.UPnPDiscovery;

import java.util.ArrayList;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context mContext;

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


        UPnPDiscovery.discoveryDevices(this, new UPnPDiscovery.OnDiscoveryListener() {
            @Override
            public void OnStart() {
                Toast.makeText(mContext, "Start discovery", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnFoundNewDevice(UPnPDevice device) {
                Toast.makeText(mContext, "Found new device", Toast.LENGTH_SHORT).show();
                Log.d("App", device.getLocation());
                myDataset.add(device.toString());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void OnFinish(HashSet<UPnPDevice> devices) {
                for (UPnPDevice device : devices) {
                   // To do something
                }
                Toast.makeText(mContext, "Discovery finished", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnError(Exception e) {
                Toast.makeText(mContext, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
