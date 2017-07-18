package top.wuqinng.bdmapmarkersmanager.entity;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by MD-WuQN on 2017/7/18.
 */

public class LocItem {
    private LatLng latLng;
    private int serial;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }
}
