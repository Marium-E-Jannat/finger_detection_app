package com.ubcohci.fingerdetection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ubcohci.fingerdetection.R;

public class ImageSlidePageFragment extends Fragment {

    public final String imageURL;

    public ImageSlidePageFragment(@NonNull String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =  inflater.inflate(
                R.layout.browsed_image_page,
                container,
                false
        );

        // Load image
        Glide.with(this)
                .load(this.imageURL)
                .placeholder(R.drawable.image_place_holder)
                .into((ImageView) view.findViewById(R.id.browsedImage));
        return view;
    }
}
