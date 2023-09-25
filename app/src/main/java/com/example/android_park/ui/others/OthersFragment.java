package com.example.android_park.ui.others;

import android.annotation.SuppressLint;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;
import com.example.android_park.R;
import com.example.android_park.UdpReceiverThread;
import com.example.android_park.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.observer.SensorSubject;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.sensor.gyroscope.KalmanGyroscopeSensor;



public class OthersFragment extends Fragment {
    private Button outdoor_button;
    public MapView mArcgisView ;
    ListenableFuture<RouteParameters> listenableFuture;
    private RouteTask mRouteTask;
    private RouteParameters mRouteParams;
    private Route mRoute;
    private SimpleLineSymbol mRouteSymbol;
    private GraphicsOverlay mGraphicsOverlay;
    private Point point;
    private Drawable drawable;
    private PictureMarkerSymbol pictureMarkerSymbol;
    private Graphic graphic;
    private Boolean isnotfirst = false;//用于导航中清除路径的初次判断
    private Boolean isfirst = true;//用于转向角中参考值的初次赋值判断
    private Boolean IsNotFirst = false;//用于定位点更新清除点的初次判断
    private Boolean Switch = true;//用于点击导航后使定位线程进入睡眠态判断
    private Boolean continue_or_not = true;//用于导航线程是否继续的判断
    private Boolean nav_or_cancel = true;//用于区分点击导航是开始导航还是结束导航
    private ArcGISMap map;
    private Double[] revise;//返回路径上离自己最近的一个点的经纬度坐标
    private Object object = new Object();

    private FSensor fsensor;
    private float[] fusedOrientation=new float[3];
    private MeanFilter meanFilter;
    private double degreeX=0;
    private double degreeY=0;
    private double degreeZ=0;//陀螺仪Y轴数据
    private double reference=0;//转角判断参考数据

    private UdpReceiverThread udpReceiverThread;


    private SensorSubject.SensorObserver sensorObserver=new SensorSubject.SensorObserver()
    {
        @Override
        public void onSensorChanged(float[] values) {
            updateValues(values);
        }
    };
    private Thread nav;//导航线程
    private Thread location;//基础定位线程
    private Thread rotation;//旋转角线程




    @SuppressLint("ShowToast")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_others, container, false);
        outdoor_button = view.findViewById(R.id.outdoor);

        //arcgis路径规划初始化
//        mRouteTask = new RouteTask(Objects.requireNonNull(getActivity()).getApplicationContext(), "http://113.250.60.62:6080/arcgis/rest/services/one_floor_show/NAServer/%E8%B7%AF%E5%BE%84");
//        listenableFuture = mRouteTask.createDefaultParametersAsync();

        //获取drawable中的定位图形
        drawable = getResources().getDrawable(R.drawable.location);
        pictureMarkerSymbol = new PictureMarkerSymbol((BitmapDrawable) drawable);


        //初始化arcgis地图
        mArcgisView = view.findViewById(R.id.mArcgisView);
//        ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer(application.mapServer_url);
//        ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer("http://113.250.60.62:6080/arcgis/rest/services//one_feature/MapServer");//实验室一楼http:
        ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer("http://113.250.60.62:6080/arcgis/services/carport/MapServer");//地下停车场
        mArcgisView.setVisibility(View.VISIBLE);
        map = new ArcGISMap(Basemap.Type.STREETS_VECTOR, application.show_Latitude_GPS, application.show_Longitude_GPS, 12);//中心点坐标和缩放级别
        map.getOperationalLayers().add(mapImageLayer);
        mArcgisView.setMap(map);
        map.setInitialViewpoint(new Viewpoint(application.show_Latitude_GPS, application.show_Longitude_GPS,300));

        //初始化门口坐标点
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        mArcgisView.getGraphicsOverlays().add(graphicsOverlay);
        point = new Point(application.test[0], application.test[1], SpatialReferences.getWgs84());
        graphic = new Graphic(point, pictureMarkerSymbol);
        graphicsOverlay.getGraphics().add(graphic);

        //传感器监听器初始化（这里采用的并非android本身提供的方法，而是引用的jar包）
        meanFilter = new MeanFilter();

        //与定位解析程序间做udp通信
        udpReceiverThread = new UdpReceiverThread(8888);
        udpReceiverThread.start();

        //导航按钮监听器
        outdoor_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                if (nav_or_cancel) Toast.makeText(getActivity(),"开始导航",Toast.LENGTH_LONG).show();
                Switch = false;
                continue_or_not = true;
                if (nav_or_cancel) nav_or_cancel = false;
                else if(!nav_or_cancel) nav_or_cancel = true;
                nav = new Thread(new Runnable() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void run() {
                        synchronized (object) {
                            while (continue_or_not) {
                                revise = nav(Double.parseDouble(UdpReceiverThread.xValue), Double.parseDouble(UdpReceiverThread.yValue));
                                graphicsOverlay.getGraphics().clear();
                                point = new Point((Double.parseDouble(UdpReceiverThread.xValue) + revise[0]) / 2, (Double.parseDouble(UdpReceiverThread.yValue) + revise[1]) / 2, SpatialReferences.getWgs84());
                                graphic = new Graphic(point, pictureMarkerSymbol);
                                graphicsOverlay.getGraphics().add(graphic);

                                //当定位进入到目标点或者被再次点击导航关闭时的判断操作
                                if ((UdpReceiverThread.xValue.contains("106.55311") & UdpReceiverThread.yValue.contains("29.74239")) | nav_or_cancel) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!nav_or_cancel)
                                            Toast.makeText(getActivity(), "您已到达目的地", Toast.LENGTH_LONG).show();
                                            else Toast.makeText(getActivity(), "结束导航", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    if (!mGraphicsOverlay.getGraphics().isEmpty()) {
                                        mGraphicsOverlay.getGraphics().clear();
                                    }
                                    if (!graphicsOverlay.getGraphics().isEmpty()) {
                                        graphicsOverlay.getGraphics().clear();
                                    }
                                    Switch = true;
                                    continue_or_not = false;
                                    object.notify();
                                }

                                try {
                                    Thread.sleep(300); // 延迟0.3秒
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                nav.start();
            }
        });


        //基础定位线程
        location = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (object) {
                    while (true) {
                        if (IsNotFirst) {
                            graphicsOverlay.getGraphics().clear();
                        }
                        point = new Point(Double.parseDouble(UdpReceiverThread.xValue), Double.parseDouble(UdpReceiverThread.yValue), SpatialReferences.getWgs84());
                        graphic = new Graphic(point, pictureMarkerSymbol);
                        graphicsOverlay.getGraphics().add(graphic);
                        IsNotFirst = true;

                        if (!Switch){
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            Thread.sleep(300); // 延迟0.3秒
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        location.start();

        //转角线程
        Lock lock = new ReentrantLock();
        lock.lock();
        rotation = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    Viewpoint newViewpoint = new Viewpoint(application.show_Latitude_GPS, application.show_Longitude_GPS,300);
                    // 应用新的视角 默认刷新视角
                    mArcgisView.setViewpointAsync(newViewpoint);
                    if(isfirst & degreeZ != 0){
                        reference = degreeZ;
                        isfirst = false;
                    }
//                    if(degreeZ - reference > -10 & degreeZ - reference < 10){
//                        mArcgisView.setViewpointRotationAsync(reference);
//                    }
//                    else {
//                        mArcgisView.setViewpointRotationAsync(degreeZ);
//                        reference = degreeZ;
//                    }

                    try {
                        Thread.sleep(100); // 延迟0.1秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        rotation.start();
        lock.unlock();

        return view;

    }

    //路径规划
    public Double[] nav(double lon,double lat){
        Double[] change_point = new Double[2];
        listenableFuture.addDoneListener(() -> {
            try {
                if (listenableFuture.isDone()) {

                    try {
                        mRouteParams = listenableFuture.get();
                    } catch (InterruptedException | ExecutionException e ) {
                        Log.e("wcw", "Error listenableFuture.get() " + e.getMessage());
                    }
                    //创建点

                    Stop stop0 = new Stop(new Point(106.553118, 29.742394, SpatialReferences.getWgs84()));//展台
                    Point point_my = new Point(lon, lat, SpatialReferences.getWgs84());
                    Stop stop2 = new Stop(point_my);

                    List<Stop> routeStops = new ArrayList<>();

                    routeStops.add(stop0);
                    routeStops.add(stop2);
                    mRouteParams.setStops(routeStops);
                    mRouteParams.setReturnDirections(true);
                    mRouteParams.setReturnStops(true);
                    mRouteParams.setReturnRoutes(true);
                    //获取路径结果
                    RouteResult result  = null;
                    Polyline parameters;
                    List<Route> routes = null;
                    Point nearestPoint;
                    try {
                        ListenableFuture<RouteResult> routeResultFuture = mRouteTask.solveRouteAsync(mRouteParams);
                        result  = routeResultFuture.get();
                    } catch (InterruptedException | ExecutionException e ) {
                        Log.e("wcw", "Error getting the route result " + e.getMessage());
                    }
                    if(result!=null){
                        routes = result.getRoutes();
                        mRoute = routes.get(0);

                        parameters = mRoute.getRouteGeometry();
                        nearestPoint = GeometryEngine.nearestCoordinate(parameters, point_my).getCoordinate();
                        change_point[0] = nearestPoint.getX();
                        change_point[1] = nearestPoint.getY();


                        Log.i("wcw", "run: num of routes "+ routes.size());
                        //创建路线图层
                        mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.GREEN, 5);
                        Graphic routeGraphic = new Graphic(mRoute.getRouteGeometry(), mRouteSymbol);
                        if(isnotfirst){
                            mGraphicsOverlay.getGraphics().clear();
                        }
                        //添加图层
                        mGraphicsOverlay = new GraphicsOverlay();

                        mGraphicsOverlay.getGraphics().add(routeGraphic);
                        mArcgisView.getGraphicsOverlays().add(mGraphicsOverlay);
                        isnotfirst = true;
                    }else {
                        Log.i("wcw", "run: no routes");
                    }
                }
            } catch (Exception e) {
                Log.e("wcw", e.getMessage());
            }
        });
        return change_point;
    }


    @Override
    public void onPause() {
        super.onPause();
        mArcgisView.pause();
//        sensorManager.unregisterListener(this);


        fsensor.unregister(sensorObserver);
        fsensor.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mArcgisView.resume();

        fsensor=new KalmanGyroscopeSensor(getActivity().getApplicationContext());
        fsensor.register(sensorObserver);
        fsensor.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mArcgisView.dispose();
        // 停止 UDP 接收线程
        if (udpReceiverThread != null) {
            udpReceiverThread.stopReceiver();
        }
    }


    //转角监听器
    private void updateValues(float[] values) {
        fusedOrientation = values;
            fusedOrientation = meanFilter.filter(fusedOrientation);
            degreeX=  ((Math.toDegrees(fusedOrientation[1])+360)%360);
            degreeY=  ((Math.toDegrees(fusedOrientation[2])+360)%360);
            degreeZ=  ((Math.toDegrees(fusedOrientation[0])+360)%360);

        Log.i("degree", "degreeX: "+degreeX);
        Log.i("degree", "degreeY: "+degreeY);
        Log.i("degree", "degreeZ: "+degreeZ);


    }

}
