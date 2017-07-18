package top.wuqinng.bdmapmarkersmanager.base;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;

import top.wuqinng.bdmapmarkersmanager.service.LocationService;

/**
 * Created by MD-WuQN on 2017/7/18.
 */

public class BaseApplication extends Application {
    public LocationService locationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
    }
}
