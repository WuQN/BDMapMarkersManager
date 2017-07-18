package top.wuqinng.bdmapmarkersmanager.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import top.wuqinng.bdmapmarkersmanager.R;
import top.wuqinng.bdmapmarkersmanager.adapter.ContentAdapter;
import top.wuqinng.bdmapmarkersmanager.base.BaseApplication;
import top.wuqinng.bdmapmarkersmanager.entity.LocItem;
import top.wuqinng.bdmapmarkersmanager.service.LocationService;
import top.wuqinng.bdmapmarkersmanager.utils.PagingScrollHelper;
import top.wuqinng.bdmapmarkersmanager.utils.ScreenUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    // 定位相关
    LocationClient mLocClient;
    boolean isFirstLoc = true; // 是否首次定位
    BaiduMap mBaiduMap;
    MapStatus ms;
    LocationService locationService;
    MapView mapView;
    RecyclerView recyclerView;
    ContentAdapter adapter;
    List<LocItem> list;
    List<LocItem> list1;
    List<LocItem> list2;
    List<Marker> markers;
    PagingScrollHelper scrollHelper;
    RadioGroup rgChange;
    RadioButton radio1;
    RadioButton radio2;
    private MyLocationConfiguration.LocationMode mCurrentMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mapView = (MapView) findViewById(R.id.mapView);
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        rgChange = (RadioGroup) findViewById(R.id.rg_change);
        radio1 = (RadioButton) findViewById(R.id.radio_1);
        radio2 = (RadioButton) findViewById(R.id.radio_2);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION,
                    1231);
        }
        initMap();
        initAdapter();
        initListener();
    }

    private void initListener() {
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int index = marker.getExtraInfo().getInt("position", 1) - 1;
                scrollHelper.setOffsetX(index * ScreenUtils.getScreenWidth(MainActivity.this));
                recyclerView.scrollToPosition(index);
                setClickMarker(list.get(index));
                return false;
            }
        });

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        radio1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    list = list1;
                } else {
                    list = list2;
                }
                adapter.setNewData(list);
                mBaiduMap.hideInfoWindow();
                for (int i = 0; i < markers.size(); i++) {
                    markers.get(i).remove();
                }
                for (int i = 0; i < list.size(); i++) {
                    addSingleMarker(list.get(i));
                }
            }
        });

    }

    private void initAdapter() {
        list = new ArrayList<>();
        list1 = new ArrayList<>();
        list2 = new ArrayList<>();
        adapter = new ContentAdapter(list);
        scrollHelper = new PagingScrollHelper();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        scrollHelper.setUpRecycleView(recyclerView);
        scrollHelper.setOnPageChangeListener(new PagingScrollHelper.onPageChangeListener() {
            @Override
            public void onPageChange(int index) {
                setClickMarker(list.get(index));
                recyclerView.scrollToPosition(index);

            }
        });
    }

    //百度地图配置初始化
    private void initMap() {
        locationService = ((BaseApplication) getApplication()).locationService;
        markers = new ArrayList<>();
        mBaiduMap = mapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(MainActivity.this);
        mLocClient.registerLocationListener(new MyLocationListener());
        mLocClient.setLocOption(locationService.getDefaultLocationClientOption());
        mLocClient.start();
    }

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null || mapView == null) {
                Toast.makeText(MainActivity.this, "未获取到定位信息", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e("GPS监听", location.getAddrStr() + location.getLatitude() + "   " + location.getLongitude());
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            runOnUiThread(new TimerTask() {
                @Override
                public void run() {
                    setView(location.getLatitude(), location.getLongitude());
                }
            });
            mBaiduMap.setMyLocationData(locData);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(18).build()));
            mLocClient.stop();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void setView(double lat, double lng) {
        Random random = new Random(System.currentTimeMillis());
        LocItem item;
        for (int i = 0; i < 20; i++) {
            item = new LocItem();
            double differ1 = (random.nextInt() % 200) / 100000.0;
            double differ2 = (random.nextInt() % 200) / 100000.0;
            double differ3 = (random.nextInt() % 200) / 100000.0;
            double differ4 = (random.nextInt() % 200) / 100000.0;
            item.setLatLng(new LatLng(lat + differ1, lng + differ2));
            item.setSerial(i + 1);
            addSingleMarker(item);
            list.add(item);
            list1.add(item);
            item = new LocItem();
            item.setSerial(i + 1);
            item.setLatLng(new LatLng(lat + differ3, lng + differ4));
            list2.add(item);
        }
        adapter.setNewData(list);
    }

    private void addSingleMarker(LocItem item) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", item.getSerial());
        View markerView = View.inflate(this, R.layout.item_marker, null);
        TextView textView = (TextView) markerView.findViewById(R.id.text_marker_count);
        textView.setText(item.getSerial() + "");
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(markerView);
        OverlayOptions option = new MarkerOptions()
                .position(item.getLatLng())
                .icon(bitmap)
                .animateType(MarkerOptions.MarkerAnimateType.grow)
                .extraInfo(bundle);
        Marker marker = (Marker) mBaiduMap.addOverlay(option);
        markers.add(marker);
    }

    private void setClickMarker(LocItem item) {
        View view = View.inflate(this, R.layout.item_marker_click, null);
        TextView textCount = (TextView) view.findViewById(R.id.text_marker_count);
        textCount.setText(item.getSerial() + "");
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromView(view);
        InfoWindow infoWindow = new InfoWindow(bitmap, item.getLatLng(), 0, null);
        mBaiduMap.showInfoWindow(infoWindow);
        //设置地图新中心点
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(item.getLatLng()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            recyclerView.setVisibility(View.VISIBLE);
            rgChange.setVisibility(View.GONE);
        } else if (id == R.id.nav_gallery) {
            recyclerView.setVisibility(View.GONE);
            rgChange.setVisibility(View.VISIBLE);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //获取定位权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1231: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(MainActivity.this, "无法获取定位权限，定位功能暂无法使用", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
