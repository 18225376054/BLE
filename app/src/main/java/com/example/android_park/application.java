package com.example.android_park;

import android.app.Application;
import android.content.Context;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class application extends Application {
    public static double LATITUDE_BD = 0;//设备定位百度坐标
    public static double LONGITUDE_BD = 0;
    public static double LATITUDE_GPS = 0;//设备定位GPS坐标
    public static double LONGITUDE_GPS = 0;
    public static int Contain_Mark = 0;//判断定位是否进入场地标识
    public static double Test_Longitude_BD = 106.56364;//试验场地门口百度坐标
    public static double Test_Latitude_BD = 29.750316;

    public static double SCHOOL_Longitude_BD = 106.56;//实验室百度坐标
    public static double SCHOOL_Latitude_BD = 29.74;

    public static double Lon_Contain = 106.5636;//判断定位进入场地的参数坐标
    public static double Lat_Contain = 29.750;
//    public static double Test_Longitude_GPS = 106.55352321383307;//试验场地门口GPS坐标
//    public static double Test_Latitude_GPS = 29.746818742712783;

    public static double Center_Longitude_GPS = (106.5542099+106.554096847341)/2;//停车场中心点
    public static double Center_Latitude_GPS = (29.7466281499999+29.7469336881605)/2;

    public static double start_Longitude_GPS = 106.55352321383307;//入口导航起点GPS坐标
    public static double start_Latitude_GPS = 29.746818742712783;

    public static double show_Longitude_GPS = 106.552941;//展厅中心位置GPS坐标
    public static double show_Latitude_GPS = 29.742340;



    public static final String remote_id ="113.250.60.62";
    public static final int udp_remote_port = 8008;
    public static final int remote_port_arcgis_map = 6080;
   //public static final String mapServer_url = "http://"+remote_id+":"+remote_port_arcgis_map+"/arcgis/rest/services/p_map/MapServer";
    public static final String RouteUrl = "http://"+remote_id+":"+remote_port_arcgis_map+"/arcgis/rest/services/p_map/NAServer/%E8%B7%AF%E5%BE%84";
    //public static final String RouteUrl_Test = "https://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/Route";
    public static double Test_Longitude_GPS = 106.553576;//试验场地门口GPS坐标
    public static double Test_Latitude_GPS = 29.746740;
    public static final String mapServer_url = "http://113.250.60.62:6080/arcgis/rest/services//map615/MapServer";
//    public static final String mapServer_url = "http://113.250.60.62:6080/arcgis/services/carport/MapServer";

    public static String PoiName;
    public static String PoiAddress;
    public static LatLng PoiLocation;
    public static double[] test = {106.552870,29.742240,106.552876,29.742248,106.552882,29.742257,106.552890,29.742260,106.552889,29.742271,106.552899,29.742277,
            106.552904,29.742284,106.552913,29.742288,106.552921,29.742293,106.552930,29.742298,106.552937,29.742303,106.552948,29.742304,106.552953,29.742311,
            106.552964,29.742313,106.552970,29.742320,106.552976,29.742328,106.552988,29.742327,106.552996,29.742330,106.553006,29.742336,106.553012,29.742332,
            106.553022,29.742337,106.553030,29.742341,106.553035,29.742347,106.553043,29.742352,106.553049,29.742361,106.553059,29.742362,106.553067,29.742370,
            106.553076,29.742371,106.553088,29.742373,106.553093,29.742380,106.553102,29.742384,106.553113,29.742385,106.553118,29.742394,};
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.setAgreePrivacy(getApplicationContext(),true);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        LocationClient.setAgreePrivacy(true);
    }
}