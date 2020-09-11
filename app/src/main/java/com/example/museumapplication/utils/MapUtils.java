package com.example.museumapplication.utils;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.museumapplication.R;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.utils.Services.AwarenessService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.LocationBarrier;
import com.huawei.hms.kit.awareness.barrier.TimeBarrier;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.LocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;


public class MapUtils {
    static Marker currentPositionMarker;
    static List<Marker> activeMarkers = new ArrayList<>();
    public static List<Site> siteList = new ArrayList<>();
    private static SiteListAdapter adapter;
    static boolean isBig = true;
    private View mapFragmentView;
    private LinearLayout mapListLayout;
    private FloatingActionButton sizeButton;
    private static HuaweiMap mMap;
    private Context mapContext;
    private RecyclerView siteListView;


    public MapUtils(View mapFragmentView, Context mapContext,RecyclerView siteListView , LinearLayout mapListLayout, FloatingActionButton sizeButton, HuaweiMap mMap) {
        this.mapFragmentView = mapFragmentView;
        this.mapListLayout = mapListLayout;
        this.sizeButton = sizeButton;
        this.mapContext = mapContext;
        this.siteListView = siteListView;
    }

    public static void setMap(HuaweiMap mMap) {
        MapUtils.mMap = mMap;
    }

    private int dp(float value, Activity fragment) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(fragment.getResources().getDisplayMetrics().density * value);
    }

    public void drawUserMarker(Location location, Bitmap profilePicture, Activity activity, HuaweiMap mMap) {

        MarkerOptions options = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);
        Bitmap bitmap = createUserBitmap(activity, profilePicture);
        if (profilePicture != null) {
            options.title(UserLoggedIn.getInstance().getName());
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            options.anchorMarker(0.5f, 0.907f);
            if (currentPositionMarker != null)
                currentPositionMarker.remove();
            currentPositionMarker = mMap.addMarker(options);
        }
    }

    public Bitmap createUserBitmap(Activity activity, Bitmap profilePicture) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(dp(62, activity), dp(76, activity), Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = activity.getResources().getDrawable(R.drawable.pin);
            drawable.setBounds(0, 0, dp(62, activity), dp(76, activity));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (profilePicture != null) {
                BitmapShader shader = new BitmapShader(profilePicture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = dp(52, activity) / (float) profilePicture.getWidth();
                matrix.postTranslate(dp(5, activity), dp(5, activity));
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);
                bitmapRect.set(dp(5, activity), dp(5, activity), dp(52 + 5, activity), dp(52 + 5, activity));
                canvas.drawRoundRect(bitmapRect, dp(26, activity), dp(26, activity), roundPaint);
            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception ignored) {
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public static void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public static void animateCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeMapSize() {
        if (!isBig) {

            ValueAnimator va = ValueAnimator.ofFloat(1, 0);
            va.setDuration(100);
            va.addUpdateListener(animation -> {
                ((LinearLayout.LayoutParams) mapListLayout.getLayoutParams()).weight = (Float) animation.getAnimatedValue();
                mapListLayout.requestLayout();
            });
            va.start();
            sizeButton.setImageDrawable(getApplicationContext().getDrawable(R.drawable.uparrow));
            isBig = true;

        } else {
            if (siteList.size() != 0) {
                ValueAnimator va = ValueAnimator.ofFloat(0, 1);
                va.setDuration(100);
                va.addUpdateListener(animation -> {
                    ((LinearLayout.LayoutParams) mapListLayout.getLayoutParams()).weight = (Float) animation.getAnimatedValue();
                    mapListLayout.requestLayout();
                });
                va.start();
                sizeButton.setImageDrawable(getApplicationContext().getDrawable(R.drawable.downarrow));
                isBig = false;
            } else {
                Snackbar.make(mapFragmentView, "Please scan for Museums first", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void searchMuseums(Location location, int radius) {

        activeMarkers.clear();
        siteList.clear();

        SearchService searchService = SearchServiceFactory.create(mapContext, mapContext.getString(R.string.api_key));
        final int[] count = {0};

        NearbySearchRequest request = new NearbySearchRequest();
        request.setLocation(new Coordinate(location.getLatitude(), location.getLongitude()));
        request.setRadius(radius);
        request.setQuery("Museum");
        request.setPoiType(LocationType.MUSEUM);
        request.setLanguage("en");
        request.setPageSize(20);
        request.setQuery("Museum");

        for (int i = 1; i < 20; i++) {
            request.setPageIndex(i);

            SearchResultListener<NearbySearchResponse> resultListener = new SearchResultListener<NearbySearchResponse>() {
                @Override
                public void onSearchResult(NearbySearchResponse results) {
                    if (results == null || results.getTotalCount() <= 0) {
                        return;
                    }
                    List<Site> sites = results.getSites();
                    if (sites == null || sites.size() == 0) {
                        return;
                    }
                    deleteAllBarriers(mapContext);
                    for (Site site : sites) {
                        if (site.getName().contains("Museum") || site.getName().contains("Müzesi")) {
                            if (!checkDuplicateSite(site)) {
                                siteList.add(site);
                                activeMarkers.add(drawMuseumMarkers(site));
                                addBarrierToAwarenessKit(site, 5000, 1000L);
                                Log.d("Sites", site.getName());
                            }
                        }
                    }
                    count[0]++;
                    if (count[0] == 19) {
                        initializeRecycleView();
                        changeMapSize();
                        saveSiteListToDevice();

                    }
                }

                @Override
                public void onSearchError(@NonNull SearchStatus searchStatus) {
                    count[0]++;
                    Log.i("TAG", "Error : " + searchStatus.getErrorCode() + " " + searchStatus.getErrorMessage());

                    if (count[0] == 19) {
                        initializeRecycleView();
                        changeMapSize();
                        saveSiteListToDevice();
                    }
                }
            };
            searchService.nearbySearch(request, resultListener);
        }
    }

    public void saveSiteListToDevice() {
        SharedPreferences mPrefs = mapContext.getSharedPreferences("SiteData", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();

        prefsEditor.clear();

        Gson gson = new Gson();
        String siteListJson = gson.toJson(siteList);

        prefsEditor.putString("siteList", siteListJson);

        prefsEditor.apply();
    }

    public void retrieveSiteList() {
        SharedPreferences mPrefs = mapContext.getSharedPreferences("SiteData", MODE_PRIVATE);

        String siteListJson = mPrefs.getString("siteList", "");

        Type typeSite = new TypeToken<List<Site>>() {}.getType();

        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson gson = gsonBuilder.create();
        if (!siteListJson.isEmpty()) {
            siteList = gson.fromJson(siteListJson, typeSite);
            initializeRecycleView();
        }

        for(Site site : siteList)
            activeMarkers.add(drawMuseumMarkers(site));


    }
    public void initializeRecycleView(){

        Collections.sort(siteList, (site1, site2) -> (int) (site1.getDistance() - site2.getDistance()));
        adapter = new SiteListAdapter(siteList, mapContext);
        siteListView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mapContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        siteListView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(siteListView.getContext(),
                linearLayoutManager.getOrientation());
        siteListView.addItemDecoration(dividerItemDecoration);
    }


    public Bitmap getMuseumMarkerBitmap() {
        BitmapDrawable bitmapDraw = (BitmapDrawable) ContextCompat.getDrawable(mapContext, R.drawable.museum_icon);
        assert bitmapDraw != null;
        Bitmap bigMarker = bitmapDraw.getBitmap();

        return Bitmap.createScaledBitmap(bigMarker, 190, 190, false);
    }

    public Marker drawMuseumMarkers(Site site) {
        Coordinate location = site.getLocation();
        LatLng position = new LatLng(location.getLat(), location.getLng());

        MarkerOptions options = new MarkerOptions().position(position)
                .clusterable(true)
                .snippet(site.getName())
                .icon(BitmapDescriptorFactory.fromBitmap(getMuseumMarkerBitmap()))
                .anchorMarker(0.5f, 0.907f);

        return mMap.addMarker(options);
    }
    public void updateRecycleView(Location currentLocation){
        adapter.calculateAndUpdateDistance(currentLocation);
    }

    public boolean checkDuplicateSite(Site toCompare) {
        for (Site site : siteList) {
            if (site.getName().equals(toCompare.getName()))
                return true;
        }
        return false;
    }

    public void resetInfo() {
        activeMarkers.clear();
        isBig = true;
        siteList.clear();
    }

    public void addBarrierToAwarenessKit(Site site, double radius, long duration) {
        if (ActivityCompat.checkSelfPermission(mapContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AwarenessBarrier stayBarrier = LocationBarrier.stay(site.getLocation().getLat(), site.getLocation().getLng(), radius, duration);
        AwarenessBarrier timeBarrier = TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT);
        AwarenessBarrier combinedBarrier = AwarenessBarrier.and(stayBarrier, AwarenessBarrier.not(timeBarrier));

        PendingIntent pendingIntent;
        Intent intent = new Intent(mapContext, AwarenessService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //In Android 8.0 or later, only foreground services can be started when the app is running in the background.
            pendingIntent = PendingIntent.getForegroundService(mapContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getService(mapContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        addBarrier(site.getName(), combinedBarrier, pendingIntent);
    }

    private void addBarrier(String label, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        BarrierUpdateRequest request = new BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build();

        Awareness.getBarrierClient(mapContext.getApplicationContext()).updateBarriers(request)
                .addOnSuccessListener(aVoid -> {
                    Log.i("AddBarrier", "add barrier success");
                    Toast.makeText(getApplicationContext(), "add barrier success", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddBarrier", "add barrier failed", e);
                    Toast.makeText(getApplicationContext(), "add barrier failed" + e.toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAllBarriers(Context context) {
        BarrierUpdateRequest request = new BarrierUpdateRequest.Builder()
                .deleteAll()
                .build();

        Awareness.getBarrierClient(context.getApplicationContext()).updateBarriers(request)
                .addOnSuccessListener(aVoid -> {
                    Log.i("DeleteAllBarriers", "delete all barriers success");
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteAllBarriers", "delete all barriers failed ", e);
                });
    }

    public Site findSiteByName(String name) {
        for (int i = 0; i < siteList.size(); i++) {
            if (siteList.get(i).getName().equals(name))
                return siteList.get(i);
        }
        return null;
    }

}
