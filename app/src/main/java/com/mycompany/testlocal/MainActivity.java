package com.mycompany.testlocal;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pikkart.ar.geo.GeoElement;
import com.pikkart.ar.geo.GeoFragment;
import com.pikkart.ar.geo.IArGeoListener;
import com.pikkart.ar.recognition.IRecognitionListener;
import com.pikkart.ar.recognition.RecognitionFragment;
import com.pikkart.ar.recognition.RecognitionOptions;
import com.pikkart.ar.recognition.data.CloudRecognitionInfo;
import com.pikkart.ar.recognition.items.Marker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IRecognitionListener, IArGeoListener {
    private int m_permissionCode = 100; // unique permission request code
    ARView m_arView = null;
    private GeoFragment m_geoFragment = null;

    public void doRecognition() {
        m_geoFragment.startRecognition(
                new RecognitionOptions(
                        RecognitionOptions.RecognitionStorage.LOCAL,
                        RecognitionOptions.RecognitionMode.CONTINUOUS_SCAN,
                        new CloudRecognitionInfo(new String[]{})
                ),
                this);
    }

    private void initLayout() {
        setContentView(R.layout.activity_main);

        m_geoFragment = ((GeoFragment) getFragmentManager().findFragmentById(R.id.geo_fragment));
        m_geoFragment.DisableRecognition();
        m_geoFragment.setGeoListener(this);

        Location loc1 = new Location("loc1");
        loc1.setLatitude(44.654894);
        loc1.setLongitude(10.914749);

        Location loc2 = new Location("loc2");
        loc2.setLatitude(44.653505);
        loc2.setLongitude(10.909653);

        Location loc3 = new Location("loc3");
        loc3.setLatitude(44.647315);
        loc3.setLongitude(10.924802);

        List<GeoElement> geoElementList = new ArrayList<GeoElement>();
        geoElementList.add(new GeoElement(loc1, "1", "COOP, Modena"));
        geoElementList.add(new GeoElement(loc2, "2", "Burger King, Modena"));
        geoElementList.add(new GeoElement(loc3, "3", "Piazza Matteotti, Modena"));

        m_geoFragment.setGeoElements(geoElementList);

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.ar_main_layout);
        m_arView  = new ARView(this);
        rl.addView(m_arView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        m_geoFragment.setCameraTextureView(m_arView);

        LinearLayout l = (LinearLayout)findViewById(R.id.ui_layout);
        l.setVisibility(View.VISIBLE);

        TextView t = (TextView)findViewById(R.id.marker_name_text);
        t.setText("Marker Not Found");
        t.setVisibility(View.VISIBLE);

        Button b1 = (Button)findViewById(R.id.geo_enabling_button);
        if(m_geoFragment.IsGeoAREnabled()) {
            b1.setText("Disable GEO");
        }
        else {
            b1.setText("Enable GEO");
        }

        Button b2 = (Button)findViewById(R.id.map_enabling_button);
        if(m_geoFragment.IsGeoMapEnabled()) {
            b2.setText("Disable MAP");
        }
        else {
            b2.setText("Enable MAP");
        }

        Button b3 = (Button)findViewById(R.id.recognition_enabling_button);
        if(m_geoFragment.IsRecognitionEnabled()) {
            b3.setText("Disable REC");
        }
        else {
            b3.setText("Enable REC");
        }

        //doRecognition();
    }

    private void checkPermissions(int code) {
        String[] permissions_required = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        List permissions_not_granted_list = new ArrayList<>();
        for (String permission : permissions_required) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissions_not_granted_list.add(permission);
            }
        }
        if (permissions_not_granted_list.size() > 0) {
            String[] permissions = new String[permissions_not_granted_list.size()];
            permissions_not_granted_list.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, code);
        }
        else {
            initLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode==m_permissionCode) {
            boolean ok = true;
            for(int i=0;i<grantResults.length;++i) {
                ok = ok && (grantResults[i]==PackageManager.PERMISSION_GRANTED);
            }
            if(ok) {
                initLayout();
            }
            else {
                Toast.makeText(this, "Error: required permissions not granted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void OnDisableGeo(View v) {
        Button b1 = (Button)findViewById(R.id.geo_enabling_button);
        if(m_geoFragment.IsGeoAREnabled()) {
            b1.setText("Enable GEO");
            m_geoFragment.DisableGeoAR();
        }
        else {
            b1.setText("Disable GEO");
            m_geoFragment.EnableGeoAR();
        }
    }

    public void OnDisableMap(View v) {
        Button b1 = (Button)findViewById(R.id.map_enabling_button);
        if(m_geoFragment.IsGeoMapEnabled()) {
            b1.setText("Enable MAP");
            m_geoFragment.DisableGeoMap();
        }
        else {
            b1.setText("Disable MAP");
            m_geoFragment.EnableGeoMap();
        }
    }

    public void OnDisableRec(View v) {
        Button b1 = (Button)findViewById(R.id.recognition_enabling_button);
        if(m_geoFragment.IsRecognitionEnabled()) {
            b1.setText("Enable REC");
            m_geoFragment.DisableRecognition();
        }
        else {
            b1.setText("Disable REC");
            doRecognition();
            m_geoFragment.EnableRecognition();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if not Android 6+ run the app
        if (Build.VERSION.SDK_INT < 23) {
            initLayout();
        }
        else {
            checkPermissions(m_permissionCode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(m_arView!=null) m_arView.onResume();
//        RecognitionFragment t_cameraFragment = ((RecognitionFragment) getFragmentManager().findFragmentById(R.id.ar_fragment));
//        if(t_cameraFragment!=null) t_cameraFragment.startRecognition(
//                new RecognitionOptions(
//                        RecognitionOptions.RecognitionStorage.LOCAL,
//                        RecognitionOptions.RecognitionMode.CONTINUOUS_SCAN,
//                        new CloudRecognitionInfo(new String[]{})
//                ), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //pause our renderer and associated videos
        if(m_arView!=null) m_arView.onPause();
    }

    @Override
    public void executingCloudSearch() {

    }

    @Override
    public void cloudMarkerNotFound() {

    }

    @Override
    public void internetConnectionNeeded() {

    }

    @Override
    public void markerFound(Marker marker) {
        final Activity this_Activity = this;
        final String mid = marker.getId();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout l = (LinearLayout)findViewById(R.id.textView_layout);
                    l.bringToFront();
                    TextView t = (TextView)this_Activity.findViewById(R.id.marker_name_text);
                    t.setText("Found marker " + mid);
                    t.bringToFront();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markerNotFound() {

    }

    @Override
    public void markerTrackingLost(String s) {
        //Toast.makeText(this, "PikkartAR: lost tracking of marker " + s, Toast.LENGTH_SHORT).show();
        //m_arView.selectMonkey(0);
        //m_arView.pauseVideo();
    }

    @Override
    public void ARLogoFound(String s, int i) {
        Log.e("ARLogo","code: "+ i);
        /*switch(i){
            case 97703:
                m_arView.selectMonkey(1);
                break;
            case 84895:
                m_arView.selectMonkey(2);
                break;
            case 65266:
                m_arView.selectMonkey(3);
                break;
        }*/
        final Activity this_Activity = this;
        final String mid = s;
        final int c = i;
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout l = (LinearLayout)findViewById(R.id.textView_layout);
                    l.bringToFront();
                    TextView t = (TextView)this_Activity.findViewById(R.id.marker_name_text);
                    t.setText("Logo Found, marker:" + mid + ", code:" + c);
                    t.bringToFront();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markerEngineToUpdate(String s) {

    }

    @Override
    public boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onGeoElementClicked(GeoElement geoElement) {

    }

    @Override
    public void onMapOrCameraClicked() {

    }

    @Override
    public void onGeolocationChanged(Location location) {

    }

    @Override
    public void onGeoBringInterfaceOnTop() {
        LinearLayout l = (LinearLayout)findViewById(R.id.ui_layout);
        l.bringToFront();
    }
}
