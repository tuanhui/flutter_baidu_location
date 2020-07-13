#import "BaiduMapPlugin.h"
#if __has_include(<baidu_map_plugin/baidu_map_plugin-Swift.h>)
#import <baidu_map_plugin/baidu_map_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "baidu_map_plugin-Swift.h"
#endif

@implementation BaiduMapPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBaiduMapPlugin registerWithRegistrar:registrar];
}
@end
