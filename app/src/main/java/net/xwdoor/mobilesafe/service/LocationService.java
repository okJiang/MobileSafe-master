package net.xwdoor.mobilesafe.service;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import net.xwdoor.mobilesafe.base.BaseActivity;
import net.xwdoor.mobilesafe.utils.PrefUtils;

import java.util.List;

public class LocationService extends Service {

    private LocationManager mLM;
    private MyLocationListener mListener;
    private Geocoder geocoder;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLM = (LocationManager) getSystemService(LOCATION_SERVICE);
        mListener = new MyLocationListener();
        geocoder = new Geocoder(this);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//获取良好精度
        criteria.setCostAllowed(true);//允许流量消耗（花费、花钱）

        String bestProvider = mLM.getBestProvider(criteria, true);// 获取当前最好的位置提供者
        mLM.requestLocationUpdates(bestProvider,0,0, mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLM.removeUpdates(mListener);
    }

    class MyLocationListener implements LocationListener{

        // 位置发生变化
        @Override
        public void onLocationChanged(Location location) {
            String jLongitude = "j: "+location.getLongitude();//经度
            String wLatitude = "w: "+location.getLatitude();//纬度
            List places = null;



            try {
                places = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String placename = "";

            if(places != null && places.size() > 0) {
                for (int i = 0; i < places.size(); i ++) {
                    Address address = (Address)places.get(i);
                    placename += address.getAddressLine(0) + address.getAddressLine(1) + address.getAddressLine(2) + "/n";
                }
            }

            String phone = PrefUtils.getString(BaseActivity.PREF_PHONE_NUMBER,"",getApplicationContext());//获取安全号码
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone,null,"Location-->"+jLongitude+";"+wLatitude + "/n" + placename,null,null);

            stopSelf();//停止服务（service自杀的方法）
        }

        // 位置提供者状态发生变化
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // 定位开关开启
        @Override
        public void onProviderEnabled(String provider) {

        }

        // 定位开关关闭
        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
