package com.example.museumapplication.utils.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.museumapplication.R;
import com.example.museumapplication.data.FavoriteMuseum;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.home.map.MapViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.site.api.model.Site;

import java.text.DecimalFormat;
import java.util.List;

public class SiteListAdapter extends RecyclerView.Adapter<SiteListAdapter.SiteViewHolder> {

    static List<Site> dataSet;
    Context mContext;
    LayoutInflater inflater;
    View root;
    MapViewModel viewModel;

    private static final DecimalFormat df2 = new DecimalFormat("#.##");

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.row_item_map, parent, false);
        return new SiteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        Site clickedSite = dataSet.get(position);
        holder.setData(clickedSite , mContext , root, viewModel);
 //       setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(v -> {
            Site data = dataSet.get(position);

            viewModel.getAnimateCameraLatLng().postValue(new LatLng(data.getLocation().getLat(), data.getLocation().getLng()));
            for (Marker marker : viewModel.getActiveMarkers().getValue()) {
                if (marker.getTitle().equals(data.getName()))
                    marker.showInfoWindow();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class SiteViewHolder extends RecyclerView.ViewHolder {


        TextView museumNameText;
        TextView museumDescText;
        TextView distanceText;
        ImageView imageView;


        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);

            museumNameText = itemView.findViewById(R.id.museumName_Row);
            museumDescText = itemView.findViewById(R.id.museumDescription_Row);
            distanceText = itemView.findViewById(R.id.distance_Row);
            imageView = itemView.findViewById(R.id.image_Row);



        }

        @SuppressLint("ResourceAsColor")
        public void setData(Site data, Context context, View root, MapViewModel viewModel) {
            this.museumNameText.setText(data.getName());
            this.museumDescText.setText(data.getFormatAddress());
            this.distanceText.setText(getDistance(data));

            if (data.getPoi().getPhotoUrls() != null) {
                imageView.setImageURI(Uri.parse(data.getPoi().getPhotoUrls()[0]));
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            boolean darkMode = sp.getBoolean("darkMode", true);


            imageView.setOnClickListener(v -> {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(context.getApplicationContext())
                        .inflate(R.layout.layout_bottom_sheet_museum, root.findViewById(R.id.bottom_sheet_container)
                );
                if(!darkMode) {
                    bottomSheetView.findViewById(R.id.bottom_sheet_container).getBackground().setTint(Color.WHITE);
                    ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_address)).setTextColor(Color.BLACK);
                    ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_name)).setTextColor(Color.BLACK);
                    ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_distance)).setTextColor(Color.BLACK);
                    ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_telephone)).setTextColor(Color.BLACK);
                    ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_web_page)).setTextColor(Color.BLACK);
                }
                ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_name)).setText(data.getName());
                ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_address)).setText(data.getFormatAddress());
                ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_distance)).setText(getDistance(data));
                ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_museum_telephone)).setText(data.getPoi().getPhone());
                ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_web_page)).setText(data.getPoi().getWebsiteUrl());



                if(data.getPoi().getPhone()!=null ){
                    ((TextView) bottomSheetView.findViewById(R.id.bottom_sheet_museum_telephone)).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    bottomSheetView.findViewById(R.id.bottom_sheet_museum_telephone).setOnClickListener(v1 -> {
                        Intent intent = new Intent(Intent.ACTION_CALL);

                        intent.setData(Uri.parse("tel:" + data.getPoi().getPhone()));
                        context.startActivity(intent);
                    });
                }

                if(data.getPoi().getWebsiteUrl() != null ){
                    ((TextView)bottomSheetView.findViewById(R.id.bottom_sheet_web_page)).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    bottomSheetView.findViewById(R.id.bottom_sheet_web_page).setOnClickListener(v1 -> {
                            String url;
                        if (!data.getPoi().getWebsiteUrl().startsWith("http://") && !data.getPoi().getWebsiteUrl().startsWith("https://")) {
                            url = "http://" + data.getPoi().getWebsiteUrl();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(browserIntent);
                        }
                        else {
                            url = data.getPoi().getWebsiteUrl();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(browserIntent);
                        }
                    });
                }
                bottomSheetView.findViewById(R.id.navigateButton).setOnClickListener(v12 -> {
                    viewModel.getAnimateCameraLatLng().postValue(new LatLng(data.getLocation().getLat(), data.getLocation().getLng()));
                    bottomSheetDialog.hide();
                });
                bottomSheetView.findViewById(R.id.favorite_button).setOnClickListener(v12 -> {
                    FavoriteMuseum temp=UserLoggedIn.Companion.getInstance().getMuseumFavoriteByName(data.getName());
                    if(temp==null) {
                        UserLoggedIn.Companion.getInstance().getFavoriteMuseumList().add(new FavoriteMuseum(data.getName(), data.getFormatAddress(), data.getPoi().getPhone(), data.getPoi().getWebsiteUrl(), data.getLocation()));
                        UserLoggedIn.Companion.getInstance().saveFavoriteMuseumListToDevice(context);
                        ((ImageView)bottomSheetView.findViewById(R.id.starImage)).setColorFilter(context.getResources().getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN);
                    }
                    else
                    {
                        UserLoggedIn.Companion.getInstance().getFavoriteMuseumList().remove(temp);
                        UserLoggedIn.Companion.getInstance().saveFavoriteMuseumListToDevice(context);
                        ((ImageView)bottomSheetView.findViewById(R.id.starImage)).setColorFilter(context.getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
                    }
                });
                if(UserLoggedIn.Companion.getInstance().getMuseumFavoriteByName(data.getName())!=null)
                    ((ImageView)bottomSheetView.findViewById(R.id.starImage)).setColorFilter(context.getResources().getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN);

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            });
        }

        public String getDistance(Site data) {
            double distance = data.getDistance();
            if (distance > 1000)
                return (df2.format(distance / 1000) + " km");
            else
                return (df2.format(distance) + " m");
        }

    }


    public SiteListAdapter(List<Site> data, @NonNull Context context, View root, MapViewModel viewModel) {
        dataSet = data;
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        this.root = root;
        this.viewModel = viewModel;
    }


    public void calculateAndUpdateDistance(Location currentLocation) {
        for (Site site : dataSet) {
            Location siteLocation = new Location("site");
            siteLocation.setLatitude(site.getLocation().getLat());
            siteLocation.setLongitude(site.getLocation().getLng());
            site.setDistance(currentLocation.distanceTo(siteLocation));
        }
        dataSet.sort((site1, site2) -> (int) (site1.getDistance() - site2.getDistance()));
        notifyDataSetChanged();
    }

}

