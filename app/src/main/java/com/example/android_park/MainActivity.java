package com.example.android_park;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;

import android.widget.Toast;


import com.baidu.mapapi.PermissionUtils;
import com.baidu.mapsdkplatform.comapi.util.PermissionCheck;
import com.example.android_park.ui.my.MyFragment;

import com.example.android_park.ui.navigation.DisplayBaiduMapFragment;
//import com.example.android_park.ui.navigation.NavigationFragment;
import com.example.android_park.ui.others.OthersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;

public class MainActivity extends AppCompatActivity {
    private String TAG  = "MainActivity";
    private FragmentManager fm;
    private BottomNavigationView bottomNavigationView;
//    private NavigationFragment navigationFragment = new NavigationFragment();
    private DisplayBaiduMapFragment baiduMapFragment = new DisplayBaiduMapFragment();
    private OthersFragment othersFragment = new OthersFragment();
    private MyFragment myFragment = new MyFragment();
    private Fragment[] fragments;
    private Messenger messenger;
    private int lastfragment = 0;
    private static final int REQUEST_CODE=1;
    private Handler my_handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
        }
    };
    private Messenger mOutMessenger = new Messenger(my_handler);

    private static final String[] PERMISSIONS=new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        setContentView(R.layout.activity_main);

        @SuppressLint("ResourceType")
        ColorStateList csl = getResources().getColorStateList(R.drawable.nvigation_colors);
        bottomNavigationView = findViewById(R.id.nav_display);
        bottomNavigationView.setItemTextColor(csl);
        fragments = new Fragment[]{baiduMapFragment, othersFragment, myFragment};

        // 动态查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
        else Log.i("wcw","写权限已有");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
        else Log.i("wcw","读权限已有");


        //首先获取FragmentManager
        fm =  getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.replace_fragment,baiduMapFragment).show(baiduMapFragment).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnItemSelectedListener);


        //室外定位判断跳转室内（因为用到了switchfragment及toast，我将其设在mainactivity中而非baidufragment）
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (String.valueOf(application.LONGITUDE_BD).contains("106.5628") & String.valueOf(application.LONGITUDE_BD).contains("106.5629")
                            & String.valueOf(application.LATITUDE_BD).contains("29.7457")) {
                        if (lastfragment != 1) {
                            switchFragment(lastfragment, 1);
                            Toast.makeText(getApplicationContext(), "转为室内定位", Toast.LENGTH_SHORT).show();
                            lastfragment = 1;
                        }
                    }
                    try {
                        Thread.sleep(1000); // 延迟1秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //下方三个fragment切换按钮的监听器
    private BottomNavigationView.OnNavigationItemSelectedListener mOnItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_navigation:
                    //这里因为需要对3个fragment进行切换
                    //start
                    if (lastfragment != 0) {
                        switchFragment(lastfragment, 0);
                        lastfragment = 0;
                    }
                    //basicinformationselect();
                    //end
                    //如果只是想测试按钮点击，不管fragment的切换，可以把start到end里面的内容去掉
                    return true;
                case R.id.menu_others:
                    if (lastfragment != 1) {
                        switchFragment(lastfragment, 1);
                        Toast.makeText(getApplicationContext(), "转为室内定位", Toast.LENGTH_SHORT).show();
                        lastfragment = 1;
                    }
                    return true;
                case R.id.menu_my:
                    if (lastfragment != 2) {
                        switchFragment(lastfragment, 2);
                        lastfragment = 2;
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }

    };
    private void switchFragment(int lastfragment, int index) {
        FragmentTransaction transaction = fm.beginTransaction();
        //隐藏上个Fragment
        transaction.hide(fragments[lastfragment]);
        if (fragments[index].isAdded() == false) {
            transaction.add(R.id.replace_fragment, fragments[index]);
        }
        transaction.show(fragments[index]).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}