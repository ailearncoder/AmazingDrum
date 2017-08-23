package com.example.panpan.amazingdrum;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BleLink.BleListener {
    private Button scanButton;
    private ListView deviceList;
    private SimpleAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private ArrayMap<String, ArrayMap> address = new ArrayMap<>();
    private ArrayList<ArrayMap<String, String>> listData = new ArrayList<>();
    private Sound sound;
    private Button[] buttons = new Button[4];
    private ArrayMap<String, BleLink> bleLinks = new ArrayMap<>();
    private ArrayList<Integer> bleId = new ArrayList<>();
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String addr = device.getAddress();
            int rssi = result.getRssi();
            if ("AeroBand".equals(name)) {
                if (address.containsKey(addr)) {
                    //listData.set(address.get(addr), "name:" + name + "\naddr:" + addr + "\nrssi:" + rssi + "dBm");
                    ArrayMap<String, String> arrayMap = address.get(addr);
                    arrayMap.put("rssi", rssi + "dBm");
                    adapter.notifyDataSetChanged();
                } else {
                    //listData.add("name:" + name + "\naddr:" + addr + "\nrssi:" + rssi + "dBm");
                    ArrayMap<String, String> arrayMap = new ArrayMap<>();
                    arrayMap.put("name", name);
                    arrayMap.put("addr", addr);
                    arrayMap.put("rssi", rssi + "dBm");
                    arrayMap.put("link", "已扫描到");
                    address.put(addr, arrayMap);
                    listData.add(arrayMap);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            scanButton.setText("ScanDevice");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initBle();
        initView();
    }

    private void initBle() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            finish();
            return;
        }
        mBluetoothAdapter.enable();
    }

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            buttons[0].setScaleX(1);
            buttons[0].setScaleY(1);
        }
    };
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            buttons[1].setScaleX(1);
            buttons[1].setScaleY(1);
        }
    };
    Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            buttons[2].setScaleX(1);
            buttons[2].setScaleY(1);
        }
    };
    Runnable runnable4 = new Runnable() {
        @Override
        public void run() {
            buttons[3].setScaleX(1);
            buttons[3].setScaleY(1);
        }
    };
    Runnable btnRunnable[] = new Runnable[]{runnable1, runnable2, runnable3, runnable4};
    private View.OnTouchListener drumClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int id = v.getId();
                sound.play(id);
                buttons[id].setScaleX(0.95f);
                buttons[id].setScaleY(0.95f);
                handler.removeCallbacks(btnRunnable[id]);
                handler.postDelayed(btnRunnable[id], 100);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.performClick();
            }
            return false;
        }
    };

    @SuppressWarnings("ResourceType")
    private void initView() {
        scanButton = (Button) findViewById(R.id.button_scan);
        deviceList = (ListView) findViewById(R.id.list_device);
        buttons[0] = (Button) findViewById(R.id.drum1);
        buttons[1] = (Button) findViewById(R.id.drum2);
        buttons[2] = (Button) findViewById(R.id.drum3);
        buttons[3] = (Button) findViewById(R.id.drum4);
        buttons[0].setId(0);
        buttons[1].setId(1);
        buttons[2].setId(2);
        buttons[3].setId(3);
        buttons[0].setOnTouchListener(drumClick);
        buttons[1].setOnTouchListener(drumClick);
        buttons[2].setOnTouchListener(drumClick);
        buttons[3].setOnTouchListener(drumClick);

        //adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listData);
        adapter = new SimpleAdapter(this, listData
                , R.layout.list_layout
                , new String[]{"name", "addr", "rssi", "link"}
                , new int[]{R.id.name, R.id.addr, R.id.rssi, R.id.link_info});
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String addr = listData.get(position).get("addr");
                if (!bleLinks.containsKey(addr) && bleId.size() > 0) {
                    BleLink bleLink = new BleLink(MainActivity.this);
                    bleLink.setBleListener(MainActivity.this);
                    bleLink.id = bleId.get(0);
                    bleLink.setAddress(addr);
                    bleId.remove(0);
                    bleLinks.put(addr, bleLink);
                    bleLink.link(mBluetoothAdapter.getRemoteDevice(addr));
                }
            }
        });
        deviceList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String addr = listData.get(position).get("addr");
                if (bleLinks.containsKey(addr)) {
                    int idNum = bleLinks.get(addr).id;
                    bleLinks.get(addr).dislink();
                    bleLinks.remove(addr);
                    addbleId(idNum);
                }
                return true;
            }
        });
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("ScanDevice".equals(scanButton.getText().toString())) {
                    scanButton.setText("StopScan");
                    for (int i = listData.size() - 1; i >= 0; i--) {
                        String addr = listData.get(i).get("addr");
                        if (bleLinks.containsKey(addr)) {
                            BleLink link = bleLinks.get(addr);
                            if (link.getDeviceState() == BleLink.DeviceState.DEVICE_STATE_DISLINK) {
                                address.remove(addr);
                                listData.remove(i);
                                int id=bleLinks.get(addr).id;
                                bleLinks.remove(addr);
                                addbleId(id);
                            }
                        } else {
                            address.remove(addr);
                            listData.remove(i);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!"ScanDevice".equals(scanButton.getText().toString())) {
                                scanButton.setText("ScanDevice");
                                mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                            }
                        }
                    }, 10000);//10s后停止扫描
                } else {
                    scanButton.setText("ScanDevice");
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                }
            }
        });
        sound = new Sound();
        sound.init(this);
        bleId.add(0);
        bleId.add(1);
        bleId.add(2);
        bleId.add(3);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(permissionRunnable, 500);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(permissionRunnable);
        super.onPause();
    }

    Handler handler = new Handler();
    Runnable permissionRunnable = new Runnable() {
        @Override
        public void run() {
            checkPermission();
        }
    };

    private void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "应用权限被拒绝，请打开权限。", Toast.LENGTH_LONG).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        0x88);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "应用权限" + permissions[i] + "被拒绝，请打开权限。", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        sound.release();
        BleLink link;
        for (int i = 0; i < bleLinks.size(); i++) {
            link = bleLinks.valueAt(i);
            link.dislink();
        }
        super.onDestroy();
    }

    private void notifyLinkChanged(BleLink bleLink, BleLink.DeviceState state) {
        MyRunnable myRunnable = new MyRunnable();
        myRunnable.adapter = adapter;
        myRunnable.map = address.get(bleLink.getAddress());
        myRunnable.state = state;
        handler.post(myRunnable);
    }

    private synchronized void addbleId(int id) {
        bleId.add(id);
    }

    @Override
    public void OnDeviceStateChanged(BleLink bleLink, BleLink.DeviceState state) {
        notifyLinkChanged(bleLink, state);
    }
    private int btnId=0;
    private Runnable btnRunnable2=new Runnable() {
        @Override
        public void run() {
            buttons[btnId].setScaleX(0.95f);
            buttons[btnId].setScaleY(0.95f);
            handler.removeCallbacks(btnRunnable[btnId]);
            handler.postDelayed(btnRunnable[btnId], 100);
        }
    };
    @Override
    public void OnDataReceived(BleLink bleLink, byte[] data) {
        Log.d("received", bleLink.id + ":" + data[0]);
        sound.play(bleLink.id);
        btnId=bleLink.id;
        handler.post(btnRunnable2);
    }
}
