import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef void BaiduMapViewCreatedCallback(BaiduMapViewController controller);

class BaiduMapView extends StatefulWidget {
  const BaiduMapView({
    Key key,
    this.onTextViewCreated,
  }) : super(key: key);

  final BaiduMapViewCreatedCallback onTextViewCreated;

  @override
  State<StatefulWidget> createState() => _BaiduMapViewState();
}

class _BaiduMapViewState extends State<BaiduMapView> {
  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'com.ectcm.baidu_map_plugin/baidumapview',
        onPlatformViewCreated: _onPlatformViewCreated,
      );
    }
    return Text(
        '$defaultTargetPlatform is not yet supported by the text_view plugin');
  }

  void _onPlatformViewCreated(int id) {
    if (widget.onTextViewCreated == null) {
      return;
    }
    widget.onTextViewCreated(new BaiduMapViewController._(id));
  }
}

class BaiduMapViewController {
  BaiduMapViewController._(int id)
      : _channel = new MethodChannel('com.ectcm.baidu_map_plugin/baidumapview_$id');

  final MethodChannel _channel;

  Future<void> setGeo(double latitude, double longitude) async {
    assert(latitude != null);
    assert(longitude != null);
    return _channel.invokeMethod('setGeo', {'latitude': latitude, 'longitude': longitude});
  }

  Future<void> searchAddress(String keyword) async {
    assert(keyword != null);
    return _channel.invokeMethod('searchAddress', {'keyword': keyword});
  }
}