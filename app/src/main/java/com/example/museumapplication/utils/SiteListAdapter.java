package com.example.museumapplication.utils;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.museumapplication.R;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.site.api.model.Site;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class SiteListAdapter extends RecyclerView.Adapter<SiteListAdapter.SiteViewHolder>  {

    static List<Site> dataSet;
    Context mContext;
    LayoutInflater inflater;
    private int lastPosition = -1;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.row_item, parent, false);
        return new SiteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        Site clickedSite = dataSet.get(position);
        holder.setData(clickedSite);
        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(v -> {
            Site data = dataSet.get(position);

            MapUtils.animateCamera(new LatLng(data.getLocation().getLat(), data.getLocation().getLng()), 15f);
            for (Marker marker : MapUtils.activeMarkers) {
                if (marker.getSnippet().equals(data.getName()))
                    marker.showInfoWindow();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class SiteViewHolder extends RecyclerView.ViewHolder  {


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
        public void setData(Site data){
            this.museumNameText.setText(data.getName());
            this.museumDescText.setText(data.getFormatAddress());
            this.distanceText.setText(getDistance(data));

            if (data.getPoi().getPhotoUrls() != null) {
                imageView.setImageURI(Uri.parse(data.getPoi().getPhotoUrls()[0]));
            }

        }
        public String getDistance(Site data){
            double distance = data.getDistance();
            if (distance > 1000)
                return (df2.format(distance / 1000) + " km");
            else
                return (df2.format(distance) + " m");
        }

        /*public void bind( post) {
            post.setValue(post);
        }*/
    }


    public SiteListAdapter(List<Site> data, @NonNull Context context) {
        dataSet = data;
        this.mContext = context;
        inflater = LayoutInflater.from(context);

    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.up_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
        else
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.down_from_top);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
    public void calculateAndUpdateDistance(Location currentLocation){
        for(Site site : dataSet)
        {
            Location siteLocation = new Location("site");
            siteLocation.setLatitude(site.getLocation().getLat());
            siteLocation.setLongitude(site.getLocation().getLng());
            site.setDistance(currentLocation.distanceTo(siteLocation));
        }
        Collections.sort(dataSet, (site1, site2) -> (int) (site1.getDistance() - site2.getDistance()));
        notifyDataSetChanged();
    }

}

