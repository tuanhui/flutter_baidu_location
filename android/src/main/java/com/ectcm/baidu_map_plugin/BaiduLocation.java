package com.ectcm.baidu_map_plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BaiduLocation {

	private LocationClient mLocationClient;
	private MyLocationListener mMyLocationListener;
	private Handler handler;
	private int model=0;
	private Context ctx;
	
	
	public BaiduLocation(Context ctx, Handler handler, int model){
		this.ctx=ctx;
		mLocationClient = new LocationClient(ctx.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		this.handler=handler;
		this.model=model;
		InitLocation();
	}
	
	public void start(){
		mLocationClient.start();
	}
	
	public void stop(){
		mLocationClient.stop();
	}
	
	private void InitLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式 LocationMode.Battery_Saving  LocationMode.Device_Sensors
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02国测局加密经纬度坐标  "bd09ll"百度加密经纬度坐标  "bd09"百度加密墨卡托坐标
		option.setOpenGps(true);
		int span=1000;
//		try {
//			span = Integer.valueOf(frequence.getText().toString());
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		option.setScanSpan(span);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);//是否反地理编码
		mLocationClient.setLocOption(option);
	}
	
	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();
			mLocationClient.stop();
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(latitude);
			sb.append(longitude);
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				//运营商信息
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}

			String locstr=null;//arg0.getProvince()获取省份+ arg0.getExtras().getString("desc")获取详细地址
			if(model==1){
				locstr=location.getAddrStr();
			}else if(model==0){
				locstr=new StringBuilder(location.getCity()).append("").append(location.getDistrict()).toString();
			}
			
			if((model==0||model==1)&&locstr==null){
				mLocationClient.start();
				return;
			}
			if(handler!=null){
				Message msg = handler.obtainMessage(Constants.LOCATION_RESULT);
				if(model==0||model==1){
					msg.obj = (locstr==null) ? "获取地址失败" : locstr;
				}else if(model==2){
					msg.obj=location;
				}
				handler.sendMessage(msg);
			}
			
			Log.i("BaiduLocationApiDem", sb.toString());
		}
	}
}