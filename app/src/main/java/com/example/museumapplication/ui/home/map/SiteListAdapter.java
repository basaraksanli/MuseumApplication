/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.museumapplication.ui.home.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.museumapplication.R;
import com.example.museumapplication.utils.map.MapUtils;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.site.api.model.Site;

import java.text.DecimalFormat;
import java.util.List;

public class SiteListAdapter extends RecyclerView.Adapter<SiteListAdapter.SiteViewHolder> {

    Fragment fragment;
    List<Site> dataSet;
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        Site clickedSite = dataSet.get(position);
        holder.setData(clickedSite , mContext , root, viewModel, fragment);


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
        public void setData(Site data, Context context, View root, MapViewModel viewModel, Fragment fragment) {
            this.museumNameText.setText(data.getName());
            this.museumDescText.setText(data.getFormatAddress());
            this.distanceText.setText(getDistance(data));

            if (data.getPoi().getPhotoUrls() != null) {
                imageView.setImageURI(Uri.parse(data.getPoi().getPhotoUrls()[0]));
            }
            imageView.setOnClickListener(v -> {
                MapUtils.Companion.showMuseumInfo(data, root, fragment);
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


    public SiteListAdapter(List<Site> data, @NonNull Context context, View root, MapViewModel viewModel , Fragment fragment) {
        this.fragment = fragment;
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

