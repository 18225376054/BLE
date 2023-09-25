package com.example.android_park.ui.navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.fonts.FontVariationAxis;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;

import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.android_park.DrivingRouteOverlay;
import com.example.android_park.PoiOverlay;
import com.example.android_park.R;
import com.example.android_park.application;
import com.example.android_park.ui.others.OthersFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


public class DisplayBaiduMapFragment extends Fragment {
    private String TAG ="DisplayBaiduMapFragment";
    private EditText search_box;
    private Button search_button;
//    private Button indoor_button;
    private double[] local_gps;

    private LocationClient mLocationClient;
    private MapView mMapView;
    private BaiduMap mBaiduMap ;
    private PoiSearch mPoiSearch;
    private MyPoiOverlay poiOverlay;
    private MyDrivingRouteOverlay overlay;
    private RoutePlanSearch mSearch;
    private ReentrantLock lock = new ReentrantLock();
    private Handler route_handler = new Handler();
    private Runnable route_runnable;
    private MyLocationConfiguration.LocationMode lm;
    private BitmapDescriptor bd;
    private boolean useDefaultIcon = false;
    private boolean isFirstLocate = true;
    private boolean interface_flag = false;


    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_displaybaidumap, container, false);
        mSearch = RoutePlanSearch.newInstance();//创建路径规划实例
        mPoiSearch = PoiSearch.newInstance();//创建poi搜索实例
        mMapView = view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        search_button = view.findViewById(R.id.search);
//        indoor_button = view.findViewById(R.id.indoor);
        search_box = view.findViewById(R.id.search_box);

        try {
            mLocationClient = new LocationClient(Objects.requireNonNull(getActivity()).getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        mLocationClient.registerLocationListener(new MyLocationListener());
        mSearch.setOnGetRoutePlanResultListener(routeListener);

        //设置定位模式
        MyLocationConfiguration config = new MyLocationConfiguration(lm, true, bd);
        mBaiduMap.setMyLocationConfiguration(config);//设置自定义样式
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
                search_box.setText(search_box.getHint().toString().substring(11));
                Log.d(TAG, "onClick: "+search_box.getText());
                if(search_box.getText().toString().equals("重邮仙桃数据谷停车场")){
//                    mBaiduMap.clear();
                    MyLocationConfiguration.LocationMode CurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                    MyLocationConfiguration config = new MyLocationConfiguration(lm, true, bd);
                    mBaiduMap.setMyLocationConfiguration(config);//设置自定义样式
//                    lock.lock();
//                    route_runnable = new Runnable() {
//                        @Override
//                        public void run() {
                            PlanNode stNode = PlanNode.withLocation(new LatLng(application.LATITUDE_BD, application.LONGITUDE_BD));
                            PlanNode enNode = PlanNode.withLocation(new LatLng(application.Test_Latitude_BD, application.Test_Longitude_BD));
                            mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
//                            route_handler.postDelayed(this, 2000);
//                        }
//                    };
//                    route_handler.postDelayed(route_runnable, 2000);
//                    lock.unlock();
                }else {
                    mBaiduMap.clear();
                    mPoiSearch.searchNearby(new PoiNearbySearchOption().location(new LatLng(application.LATITUDE_BD, application.LONGITUDE_BD))
                            .keyword(search_box.getText().toString()).radius(5000).pageNum(0));
                    MyLocationConfiguration config = new MyLocationConfiguration(lm, true, bd);
                    mBaiduMap.setMyLocationConfiguration(config);//设置自定义样式
                }
                interface_flag = true;
            }
        });


        List<String> permissionList = new ArrayList<String>();
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
//        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(),permissions,1);
        }else {
            requestLocation();
        }//该if判断用于检测以上三个权限是否被通过，如果未被通过则无法继续（动态申请权限）

        return view;

    }

    //路径规划
    OnGetRoutePlanResultListener routeListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }
        //获取驾车线路规划结果
        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            if (drivingRouteResult == null || drivingRouteResult.error !=   SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                drivingRouteResult.getSuggestAddrInfo();
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                if (drivingRouteResult.getRouteLines().size() >= 1) {
                    overlay = new MyDrivingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(drivingRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                } else {
                    Log.d(TAG,"route result"+"结果数<0");
                    return;
                }
            }

        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };

    //poi点搜索
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity(), "未搜索到POI数据", Toast.LENGTH_SHORT).show();
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
                mBaiduMap.clear();
                poiOverlay = new MyPoiOverlay(mBaiduMap);
                poiOverlay.setData(result);// 设置POI数据
                mBaiduMap.setOnMarkerClickListener(poiOverlay);
                poiOverlay.addToMap();// 将所有的overlay添加到地图上
                poiOverlay.zoomToSpan();
                //
                int totalPage = result.getTotalPageNum();// 获取总分页数
                Toast.makeText(
                        getActivity(),
                        "总共查到" + result.getTotalPoiNum() + "个兴趣点, 分为"
                                + totalPage + "页", Toast.LENGTH_SHORT).show();
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
                // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                String strInfo = "在";
                for (CityInfo cityInfo : result.getSuggestCityList()) {
                    strInfo += cityInfo.city;
                    strInfo += ",";
                }
                strInfo += "找到结果";
                Toast.makeText(getActivity(), strInfo, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    //关于poi点的操作（点击poi点直接规划路径）
    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap arg0) {
            super(arg0);
        }
        @Override
        public boolean onPoiClick(int arg0) {//重写poi点击事件
            super.onPoiClick(arg0);
            application.PoiName = getPoiResult().getAllPoi().get(arg0).name;
            application.PoiLocation = getPoiResult().getAllPoi().get(arg0).location;
            application.PoiAddress = getPoiResult().getAllPoi().get(arg0).address;
            Toast.makeText(getActivity(), application.PoiName + ": " + application.PoiAddress, Toast.LENGTH_LONG).show();
            mBaiduMap.clear();
            PlanNode stNode = PlanNode.withLocation(new LatLng(application.LATITUDE_BD, application.LONGITUDE_BD));
            PlanNode enNode = PlanNode.withLocation(application.PoiLocation);
            mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
            //MyLocationConfiguration.LocationMode CurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
            MyLocationConfiguration config = new MyLocationConfiguration(lm, true, bd);
            mBaiduMap.setMyLocationConfiguration(config);//设置自定义样式
            return true;
        }
    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public BitmapDescriptor getStartMarker() {
//            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.direction);
//            }
            return null;
        }
        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.location);
            }
            return null;
        }
    }
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    //百度地图定位的初始化
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//定位模式选择，有高精度、省电和仅设备
        option.setCoorType("bd09ll");//定位坐标系选择，有bd09ll,bd09,gcj02
        option.setScanSpan(1000);//定位时间间隔
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    //定位监听
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if(isFirstLocate) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(17f);
                mBaiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }
            mBaiduMap.setMyLocationEnabled(true);

            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.longitude(location.getLongitude());
            locationBuilder.latitude(location.getLatitude());
            MyLocationData locationData = locationBuilder.build();
            mBaiduMap.setMyLocationData(locationData);

            application.LATITUDE_BD = location.getLatitude();
            application.LONGITUDE_BD = location.getLongitude();
            Log.d(TAG, "onReceiveLocation: LATITUDE_BD "+application.LATITUDE_BD);
            Log.d(TAG, "onReceiveLocation: LONGITUDE_BD "+application.LONGITUDE_BD);


        }
    }


    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();

    }
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
//在Fragment执行onPause时执行mMapView. onPause ()，实现地图生命周期管理


    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        mPoiSearch.destroy();
        mSearch.destroy();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        route_handler.removeCallbacks(route_runnable);
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}