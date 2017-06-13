package com.example.mac.android_hw7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SleepArrayAdapter adapter = null;
    private static final int LIST_SLEEP = 1;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIST_SLEEP: {
                    List<Sleep> sleep = (List<Sleep>)msg.obj;
                    refreshSleepList(sleep);
                    break;
                }
            }
        }
    };
    private void refreshSleepList(List<Sleep> Sleepy) {
        adapter.clear();
        adapter.addAll(Sleepy);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = (ListView)findViewById(R.id.lv_slp);
        adapter = new SleepArrayAdapter(this,new ArrayList<Sleep>());
        list.setAdapter(adapter);
        getSleepFromFirebase();
    }

    class FirebaseThread extends Thread {
        private DataSnapshot dataSnapshot;
        public FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }
        @Override
        public void run() {
            List<Sleep> lsSleep = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                DataSnapshot dsname = ds.child("Name");
                DataSnapshot dsadd = ds.child("Add");

                String DSname = (String)dsname.getValue();
                String DSadd = (String)dsadd.getValue();

                DataSnapshot dsImg = ds.child("Picture2");
                String DSimg = (String) dsImg.getValue();
                Bitmap DSImg = getImgBitmap(DSimg);

                Sleep asleep = new Sleep();
                asleep.setName(DSname);
                asleep.setAdd(DSadd);
                asleep.setImgURL(DSImg);
                lsSleep.add(asleep);
                Log.v("Sleep", DSname + ";" + DSadd);
            }
            Message mgg = new Message();
            mgg.what = LIST_SLEEP;
            mgg.obj = lsSleep;
            handler.sendMessage(mgg);
        }
    }
    private void getSleepFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseThread(dataSnapshot).start();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Sleep", databaseError.getMessage());
            }
        });
    }
    private Bitmap getImgBitmap(String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            Bitmap bm = BitmapFactory.decodeStream(
                    url.openConnection().getInputStream());
            return bm;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    class SleepArrayAdapter extends ArrayAdapter<Sleep> {
        Context context;

        public SleepArrayAdapter(Context context, List<Sleep> items) {
            super(context, 0, items);
            this.context = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout itemlayout = null;
            if (convertView == null) {
                itemlayout = (LinearLayout) inflater.inflate(R.layout.sleepitem, null);
            }
            else{
                itemlayout = (LinearLayout) convertView;
            }
            Sleep item = (Sleep) getItem(position);
            TextView NAME = (TextView) itemlayout.findViewById(R.id.textView2);
            NAME.setText(item.getName());
            TextView ADD = (TextView) itemlayout.findViewById(R.id.textView);
            ADD.setText(item.getAdd());
            ImageView PIC = (ImageView) itemlayout.findViewById(R.id.imageView);
            PIC.setImageBitmap(item.getImgURL());
            return itemlayout;
        }
    }
}