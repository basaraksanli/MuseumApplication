package com.example.museumapplication.utils;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Barrier;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.museumapplication.data.UserLoggedIn;
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
import com.example.museumapplication.R;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.LocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.facebook.FacebookSdk.getApplicationContext;


public class MapUtils {
    static Marker currentPositionMarker;
    static List<Marker> activeMarkers = new ArrayList<>();
    public static List<Site> siteList = new ArrayList<>();
    static boolean isBig = false;

    public MapUtils() {
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
            Drawable drawable = activity.getResources().getDrawable(R.drawable.pin);
            drawable.setBounds(0, 0, dp(62, activity), dp(76, activity));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            Bitmap bitmap = profilePicture;
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = dp(52, activity) / (float) bitmap.getWidth();
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

    public void moveCamera(LatLng latLng, HuaweiMap mMap, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void animateCamera(LatLng latLng, HuaweiMap mMap, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void changeMapSize(LinearLayout mapListLayout) {
        if (!isBig) {
            ValueAnimator va = ValueAnimator.ofFloat(1, 0);
            va.setDuration(100);
            va.addUpdateListener(animation -> {
                ((LinearLayout.LayoutParams) mapListLayout.getLayoutParams()).weight = (Float) animation.getAnimatedValue();
                mapListLayout.requestLayout();
            });
            va.start();
            isBig = true;
        } else {
            ValueAnimator va = ValueAnimator.ofFloat(0, 1);
            va.setDuration(100);
            va.addUpdateListener(animation -> {
                ((LinearLayout.LayoutParams) mapListLayout.getLayoutParams()).weight = (Float) animation.getAnimatedValue();
                mapListLayout.requestLayout();
            });
            va.start();
            isBig = false;
        }
    }

    public void searchMuseums(Context context, Location location, int radius, HuaweiMap hMap, boolean control) {
        SearchService searchService = SearchServiceFactory.create(context, context.getString(R.string.api_key));
        final int[] count = {0};

        NearbySearchRequest request = new NearbySearchRequest();
        request.setLocation(new Coordinate(location.getLatitude(), location.getLongitude()));
        request.setRadius(radius);
        request.setPoiType(LocationType.MUSEUM);
        request.setLanguage("en");
        request.setPageSize(20);
        request.setQuery("Museum");

        for (int i = 1; i < 5; i++) {
            request.setPageIndex(i);

            SearchResultListener<NearbySearchResponse> resultListener = new SearchResultListener<NearbySearchResponse>() {
                @Override
                public void onSearchResult(NearbySearchResponse results) {
                    if (results == null || results.getTotalCount() <= 0) {
                        return;
                    }
                    List<Site> sites = results.getSites();
                    if (sites == null || sites.size() == 0) {

                    }
                    for (Site site : sites) {
                        if (site.getName().contains("Museum") || site.getName().contains("Müzesi")) {
                            if (!checkDuplicateSite(site)) {
                                siteList.add(site);
                                activeMarkers.add(drawMuseumMarkers(context, site, hMap));
                                addBarrierToAwarenessKit(context, site, 5000, 1000L);
                                Log.d("Sites", site.getName());
                            }
                        }
                    }
                    count[0]++;
                    if(control && count[0]== 3){
                        Site siteToFocus = findSiteByName(Objects.requireNonNull(((Activity)context).getIntent().getExtras()).getString("MuseumName"));
                        moveCamera(new LatLng(siteToFocus.getLocation().getLat(), siteToFocus.getLocation().getLng()), hMap, 15f);
                    }

                }

                @Override
                public void onSearchError(@NonNull SearchStatus searchStatus) {
                    Log.i("TAG", "Error : " + searchStatus.getErrorCode() + " " + searchStatus.getErrorMessage());
                }
            };
            searchService.nearbySearch(request, resultListener);
        }
    }

    public Marker drawMuseumMarkers(Context context, Site site, HuaweiMap hMap) {
        Coordinate location = site.getLocation();
        LatLng position = new LatLng(location.getLat(), location.getLng());

        BitmapDrawable bitmapDraw = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.museum_icon);
        assert bitmapDraw != null;
        Bitmap bigMarker = bitmapDraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bigMarker, 190, 220, false);

        MarkerOptions options = new MarkerOptions().position(position)
                .clusterable(true)
                .snippet(site.getName())
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).anchorMarker(0.5f, 0.907f);

        return hMap.addMarker(options);
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
        isBig = false;
        siteList.clear();
    }

    public void addBarrierToAwarenessKit(Context context,Site site, double radius, long duration) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        //AwarenessBarrier timeBarrier = TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT);
        //AwarenessBarrier combinedBarrier = AwarenessBarrier.and(stayBarrier, AwarenessBarrier.not(timeBarrier));

        PendingIntent pendingIntent;
        Intent intent = new Intent(context, AwarenessService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //In Android 8.0 or later, only foreground services can be started when the app is running in the background.
            pendingIntent = PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        addBarrier(context, site.getName(), stayBarrier, pendingIntent);
    }
    private void addBarrier(Context context, String label, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        BarrierUpdateRequest request = new BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build();


        Awareness.getBarrierClient(context.getApplicationContext()).updateBarriers(request)
                .addOnSuccessListener(aVoid -> {
                    Log.i("AddBarrier", "add barrier success");
                    Toast.makeText(getApplicationContext(), "add barrier success", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddBarrier", "add barrier failed", e);
                    Toast.makeText(getApplicationContext(), "add barrier failed" + e.toString(), Toast.LENGTH_SHORT).show();
                });
    }
    public Site findSiteByName(String name){
        for(int i = 0 ; i<siteList.size(); i++){
            if(siteList.get(i).getName().equals(name))
                return siteList.get(i);
        }
        return null;
    }

}
