package io.flutter.plugins;

import io.flutter.plugin.common.PluginRegistry;
import com.ectcm.baidu_map_plugin.BaiduMapViewPlugin;

/**
 * Generated file. Do not edit.
 */
public final class GeneratedPluginRegistrant {
  public static void registerWith(PluginRegistry registry) {
    if (alreadyRegisteredWith(registry)) {
      return;
    }
    BaiduMapViewPlugin.registerWith(registry.registrarFor("com.ectcm.baidu_map_plugin.BaiduMapViewPlugin"));
  }

  private static boolean alreadyRegisteredWith(PluginRegistry registry) {
    final String key = GeneratedPluginRegistrant.class.getCanonicalName();
    if (registry.hasPlugin(key)) {
      return true;
    }
    registry.registrarFor(key);
    return false;
  }
}
