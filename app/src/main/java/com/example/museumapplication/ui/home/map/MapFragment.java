package com.example.museumapplication.ui.home.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.museumapplication.R;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.utils.MapUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MapStyleOptions;
import com.huawei.hms.site.api.model.Site;

import java.util.Objects;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapViewDemoActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private HuaweiMap hMap;
    private MapUtils mapUtils;
    private Location currentLocation;
    private boolean firstTime = true;
    private MapView mMapView;
    private boolean mLocationPermissionGranted = false;
    private Bundle mSavedInstnceState;
    private ProgressBar progressBar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSavedInstnceState = savedInstanceState;

        mapUtils = new MapUtils();
        mapUtils.resetInfo();
        FloatingActionButton fabLocation = root.findViewById(R.id.fabLocation);
        fabLocation.setOnClickListener(view -> mapUtils.animateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), hMap, 15f));

        Button searchForMuseumButton = root.findViewById(R.id.searchForMuseumButton);
        searchForMuseumButton.setOnClickListener(v ->
                mapUtils.searchMuseums(getContext(), currentLocation, 50000, hMap, false));



        // get mapView by layout view
        mMapView = root.findViewById(R.id.mapView);
        LinearLayout mapListLayout = root.findViewById(R.id.mapListLayout);

        FloatingActionButton fabScreenSize = root.findViewById(R.id.fabScreenSize);
        fabScreenSize.setOnClickListener(view -> mapUtils.changeMapSize(mapListLayout));

        progressBar = root.findViewById(R.id.mapProgressBar);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();

        getLocationPermissions();
        if (mLocationPermissionGranted) {
            initMap(savedInstanceState);
            Log.d("Location Permission:", "Permission granted");
        }
        return root;
    }


    @Override
    public void onMapReady(HuaweiMap map) {
        Log.d(TAG, "onMapReady: ");


        hMap = map;
        hMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle_dark));

        hMap.setMaxZoomPreference(20.0f);
        hMap.setMinZoomPreference(6.0f);

        hMap.setIndoorEnabled(true);

        getDeviceLocation();

    }

    public void getDeviceLocation() {


        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                //Toast.makeText(getContext(), currentLocation.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Current Location", currentLocation.toString());
                if (firstTime) {
                    progressBar.setVisibility(View.GONE);

                    if(requireActivity().getIntent().getExtras()==null )
                        mapUtils.moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), hMap, 15f);

                    firstTime = false;

                }
                if (UserLoggedIn.getInstance().getProfilePicture() == null)
                    mapUtils.drawUserMarker(location, BitmapFactory.decodeResource(getResources(), R.drawable.avatar), getActivity(), hMap);
                else
                    mapUtils.drawUserMarker(location, UserLoggedIn.getInstance().getProfilePicture(), getActivity(), hMap);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }


    private void getLocationPermissions() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q");
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||  ActivityCompat.checkSelfPermission(requireContext(), "com.huawei.hms.permission.ACTIVITY_RECOGNITION") !=PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, "com.huawei.hms.permission.ACTIVITY_RECOGNITION"};
                requestPermissions(strings, 1);
            } else
                mLocationPermissionGranted = true;
        } else {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(),
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), "com.huawei.hms.permission.ACTIVITY_RECOGNITION") !=PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION",
                        "com.huawei.hms.permission.ACTIVITY_RECOGNITION"};
                requestPermissions(strings, 2);
            } else
                mLocationPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initMap(mSavedInstnceState);
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
                mLocationPermissionGranted = true;
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION  failed");
                progressBar.setVisibility(View.GONE);
            }
        }

        if (requestCode == 2) {
            if (grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initMap(mSavedInstnceState);
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful");
                mLocationPermissionGranted = true;
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed");
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }
}