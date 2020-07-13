package com.ectcm.baidu_map_plugin;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** BaiduMapViewPlugin */
public class BaiduMapViewPlugin implements FlutterPlugin, ActivityAware {

  public static void registerWith(Registrar registrar) {
    registrar
      .platformViewRegistry()
      .registerViewFactory(
              "com.ectcm.baidu_map_plugin/baidumapview", new BaiduMapViewFactory(registrar));
  }

  @Override
  public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
    SDKInitializer.initialize(flutterPluginBinding.getApplicationContext());
    // 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
    // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
    SDKInitializer.setCoordType(CoordType.BD09LL);
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding flutterPluginBinding) {

  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }
}
