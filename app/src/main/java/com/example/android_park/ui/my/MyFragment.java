package com.example.android_park.ui.my;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.android_park.R;
import com.example.android_park.UdpReceiverThread;


public class MyFragment extends Fragment {

    public static TextView textView_my;
//    private Button button_my;


    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        textView_my = view.findViewById(R.id.my_fragment);
//        button_my = view.findViewById(R.id.start_button);
//        button_my.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
