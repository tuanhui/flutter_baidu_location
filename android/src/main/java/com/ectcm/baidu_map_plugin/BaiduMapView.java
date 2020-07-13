package com.ectcm.baidu_map_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

public class BaiduMapView implements PlatformView, MethodChannel.MethodCallHandler {
    private final TextureMapView mapView;
    private BaiduMap baiduMap;
    private final MethodChannel methodChannel;
    private final Context context;
    private String markIconPath;
    private BitmapDescriptor markIcon = null;
    private final PluginRegistry.Registrar registrar;
    private Overlay overlay = null;
    private GeoCoder geoCoder = null;
    private long time = 0;
    private PoiSearch mPoiSearch = null;
    private BaiduLocation baiduLocation;
    private String mCurrentCity = "深圳";
    private static final String TAG = "BaiduMapView";
    private boolean hasMoveToMyLocation = false;
    private LatLng mLocation = new LatLng(22.5338379860, 114.0312739697);
    private int pageNum = 0;
    private int pageCapacity = 20;
    private MethodChannel mMethodChannel = null;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.UPDATE_MAP_MARK:
                break;
            case Constants.LOCATION_RESULT:
                Log.d(TAG, "LOCATION_RESULT");
                BDLocation location=(BDLocation)msg.obj;
                if (location == null || mapView == null) {
                    return;
                }
                MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius()).direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
                baiduMap.setMyLocationData(locData);
                mCurrentCity = location.getCity();

                if(!hasMoveToMyLocation) {
                    mLocation = new LatLng(location.getLatitude(), location.getLongitude());//坐标点
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLocation);
                    baiduMap.animateMapStatus(u);
                }

                break;
            case Constants.START_LOCATION:

                break;
            }
        }
    };

    BaiduMapView(Context context, PluginRegistry.Registrar registrar, int id) {
        mMethodChannel = new MethodChannel(registrar.messenger(), "com.ectcm.dayi/flutter");
        this.registrar = registrar;
        mapView = new TextureMapView(this.registrar.activity());
        baiduMap = mapView.getMap();
        MapStatusUpdate mapStatus = MapStatusUpdateFactory.zoomTo(19.0f);
        baiduMap.setMapStatus(mapStatus);

        baiduMap.setMyLocationEnabled(true);

        setMarkIcon();

        baiduMap.setOnMapStatusChangeListener(listener);

        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(geoCoderResultListener);

//        mSuggestionSearch = SuggestionSearch.newInstance();
//        mSuggestionSearch.setOnGetSuggestionResultListener(mSuggestionLister);

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);


        this.context = context;
        methodChannel = new MethodChannel(this.registrar.messenger(), "com.ectcm.baidu_map_plugin/baidumapview_" + id);
        methodChannel.setMethodCallHandler(this);

        baiduLocation = new BaiduLocation(this.registrar.activity(), mHandler, 2);
        baiduLocation.start();
    }

//    private OnGetSuggestionResultListener mSuggestionLister = new OnGetSuggestionResultListener() {
//        @Override
//        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
//            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
//                return;
//            }
//            List<String> addressList = new ArrayList<>();
//
//            for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
//                if (info.address != null) {
//                    addressList.add(info.address);
//                    Log.d(TAG, "onGetSuggestionResult ::: address = " + info.address);
//                }
//            }
//            mMethodChannel.invokeMethod("onAddressSearchResultByKeyword", addressList);
//        }
//    };

    private BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {
        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         *
         * @param status 地图状态改变开始时的地图状态
         */
        @Override
        public void onMapStatusChangeStart(MapStatus status) {

        }

        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         *
         * @param status 地图状态改变开始时的地图状态
         *
         * @param reason 地图状态改变的原因
         */

        //用户手势触发导致的地图状态改变,比如双击、拖拽、滑动底图
        //int REASON_GESTURE = 1;
        //SDK导致的地图状态改变, 比如点击缩放控件、指南针图标
        //int REASON_API_ANIMATION = 2;
        //开发者调用,导致的地图状态改变
        //int REASON_DEVELOPER_ANIMATION = 3;
        @Override
        public void onMapStatusChangeStart(MapStatus status, int reason) {
            if(reason == REASON_GESTURE) {
                Log.d(TAG, "onMapStatusChangeStart");
            }
        }

        /**
         * 地图状态变化中
         *
         * @param status 当前地图状态
         */
        @Override
        public void onMapStatusChange(MapStatus status) {
        }

        /**
         * 地图状态改变结束
         *
         * @param status 地图状态改变结束后的地图状态
         */
        @Override
        public void onMapStatusChangeFinish(MapStatus status) {
            if(time == 0 || System.currentTimeMillis() - time > 50) {
                time = System.currentTimeMillis();
                Log.e(TAG, "onMapStatusChangeFinish");
                MapStatusUpdate mapStatus = MapStatusUpdateFactory.newLatLng(status.target);
                baiduMap.setMapStatus(mapStatus);
                // double latitude = status.target.latitude;  // 纬度
                // double longitude = status.target.longitude // 经度
                if(overlay != null && !overlay.isRemoved()) {
                    overlay.remove();
                }
                overlay = addMark(status.target);
                Log.e(TAG, "status.target == lat = " + status.target.latitude + ", lng = "+status.target.longitude);
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(status.target));
            }
        }
    };

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "setGeo":
                setGeo(methodCall, result);
                break;
            case "searchAddress":
                searchAddress(methodCall, result);
                break;
            case "nextPage":
                nextPage(methodCall, result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void searchAddress(MethodCall methodCall, MethodChannel.Result result) {
        String keyword = methodCall.argument("keyword");//维度
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener2);
        mPoiSearch.searchInCity(new PoiCitySearchOption().city(mCurrentCity).keyword(keyword).pageNum(0).pageCapacity(20));
        result.success(null);
    }

    private void nextPage(MethodCall methodCall, MethodChannel.Result result) {
        pageNum+=1;
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        PoiNearbySearchOption option = new PoiNearbySearchOption().keyword("写字楼").location(mLocation).pageCapacity(pageCapacity).pageNum(pageNum).radius(1000);
        mPoiSearch.searchNearby(option);
        result.success(null);
    }

    private void setGeo(MethodCall methodCall, MethodChannel.Result result) {
        double latitude = methodCall.argument("latitude");//维度
        double longitude = methodCall.argument("longitude");//经度
        markIconPath = methodCall.argument("markIcon");

        mLocation = new LatLng(latitude, longitude);//坐标点

        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLocation);
        baiduMap.animateMapStatus(u);

        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLocation));


//        Message obtainMessage = mHandler.obtainMessage();
//        obtainMessage.what = Constants.UPDATE_MAP_MARK;
//        mHandler.sendMessageDelayed(obtainMessage, 3000);
//
//        Message obtainMessage1 = mHandler.obtainMessage();
//        obtainMessage1.what = Constants.START_LOCATION;
//        mHandler.sendMessageDelayed(obtainMessage1, 1000);

        result.success(null);
    }

    private OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            Log.e(TAG, "onGetPoiResult");
            if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                return;
            }
            List<Map<String, String>> addressList = new ArrayList<>();
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                for (PoiInfo p : poiResult.getAllPoi()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("name", p.name);
                    item.put("address", p.address);
                    addressList.add(item);
                    Log.d(TAG, "POI Search ::: address = " + p.address);
                }
            }
            mMethodChannel.invokeMethod("onAddressSearchResult", addressList);
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

    private OnGetPoiSearchResultListener poiSearchResultListener2 = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            Log.e(TAG, "onGetPoiResult");
            if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                return;
            }
            List<Map<String, String>> addressList = new ArrayList<>();
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                for (PoiInfo p : poiResult.getAllPoi()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("name", p.name);
                    item.put("address", p.address);
                    addressList.add(item);
                    Log.d(TAG, "POI Search ::: address = " + p.address);
                }
            }
            mMethodChannel.invokeMethod("onAddressSearchResultByKeyword", addressList);
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

    private OnGetGeoCoderResultListener geoCoderResultListener = new OnGetGeoCoderResultListener() {

        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (null != geoCodeResult && null != geoCodeResult.getLocation()) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                    return;
                } else {
                    double latitude = geoCodeResult.getLocation().latitude;
                    double longitude = geoCodeResult.getLocation().longitude;
                }
            }
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            try{
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.d(TAG, "抱歉,未能找到结果!");
                    return;
                }
                String resultAddress = result.getAddress();
                if(mCurrentCity==null||resultAddress==null){
                    return;
                }
                Log.d(TAG, "address = " + resultAddress);
                pageNum = 0;
                mLocation = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
                mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
                PoiNearbySearchOption option = new PoiNearbySearchOption().keyword("写字楼").keyword("小区").location(mLocation).pageCapacity(pageCapacity).pageNum(pageNum).radius(1000);
                mPoiSearch.searchNearby(option);
            }catch(Exception e){
                Log.d(TAG, "抱歉,未能找到结果!");
                e.printStackTrace();
            }
        }
    };

    private Overlay addMark(LatLng point) {
        if(markIcon == null) {
            markIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location1);
        }
//        //构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions().position(point).icon(markIcon);
//        //在地图上添加Marker，并显示
//        return baiduMap.addOverlay(option);

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point) //必传参数
                .icon(markIcon) //必传参数
                .draggable(true)
                .animateType(null)
                .scaleX(0.5f)
                .scaleY(0.5f)
        //设置平贴地图，在地图中双指下拉查看效果
                .flat(true)
                .alpha(1f);
        //在地图上添加Marker，并显示
       return baiduMap.addOverlay(option);
    }

    private void setMarkIcon() {
        Bitmap markBitmap = null;
        if(markIconPath != null) {
            try {
                AssetManager assetManager = this.context.getAssets();
                String key = this.registrar.lookupKeyForAsset("icons/heart.png");
                AssetFileDescriptor fd = assetManager.openFd(key);
                markBitmap = BitmapFactory.decodeStream(fd.createInputStream());
            } catch (Exception e) {
            }
        }
        //构建Marker图标
        if(markBitmap != null) {
            markIcon = BitmapDescriptorFactory.fromBitmap(markBitmap);
        }
    }

    @Override
    public View getView() {
        return mapView;
    }

    @Override
    public void onFlutterViewAttached(android.view.View flutterView) {

    }

    @Override
    public void onFlutterViewDetached() {

    }

    @Override
    public void dispose() {
        baiduLocation.stop();
//        if(mSuggestionSearch != null) {
//            mSuggestionSearch.destroy();
//        }
        if(mPoiSearch != null) {
            mPoiSearch.destroy();
        }
        if(geoCoder != null) {
            geoCoder.destroy();
        }
        if(markIcon != null) {
            markIcon.recycle();
        }
        if(overlay != null) {
            overlay.remove();
        }
        // 退出时销毁定位
        //  mLocClient.unRegisterLocationListener(myListener);
        //  mLocClient.stop();
        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);
        baiduMap.clear();
        mapView.onDestroy();
    }

    @Override
    public void onInputConnectionLocked() {

    }

    @Override
    public void onInputConnectionUnlocked() {

    }
}