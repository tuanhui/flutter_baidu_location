package com.ectcm.baidu_map_plugin;

import android.app.Activity;
import android.content.Context;

import io.flutter.embedding.android.FlutterTextureView;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class BaiduMapViewFactory extends PlatformViewFactory {
    private final PluginRegistry.Registrar registrar;
    public BaiduMapViewFactory(PluginRegistry.Registrar registrar) {
        super(StandardMessageCodec.INSTANCE);
        this.registrar = registrar;
    }

    @Override
    public PlatformView create(Context context, int id, Object o) {
        return new BaiduMapView(context, registrar, id);
    }
}