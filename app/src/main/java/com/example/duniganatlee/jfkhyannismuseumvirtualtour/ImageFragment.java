package com.example.duniganatlee.jfkhyannismuseumvirtualtour;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment {
    private static final String IMAGE_URL = "image_URL";
    private static final String IMAGE_TITLE = "image_title";
    private static final String DEFAULT_IMAGE = "default_image";
    private static final int DEFAULT_IMAGE_ID = R.drawable.jfklogo_bluebg_mobile;
    private String mImageUrl;
    private String mImageTitle;
    @BindView(R.id.media_image_view) ImageView mediaImageView;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageUrl URL of the image to be loaded.
     * @param imageTitle Content description of the image.
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String imageUrl, String imageTitle) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageUrl = getArguments().getString(IMAGE_URL, DEFAULT_IMAGE);
            mImageTitle = getArguments().getString(IMAGE_TITLE, getString(R.string.default_image_description));
        } else {
            mImageUrl = DEFAULT_IMAGE;
            mImageTitle = getString(R.string.default_image_description);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, rootView);
        if (mImageUrl.equals(DEFAULT_IMAGE)) {
            mediaImageView.setImageResource(DEFAULT_IMAGE_ID);
        } else {
            Picasso.get()
                    .load(mImageUrl)
                    .placeholder(DEFAULT_IMAGE_ID)
                    .error(DEFAULT_IMAGE_ID)
                    .into(mediaImageView);
            mediaImageView.setContentDescription(mImageTitle);
        }
        return rootView;
    }

}
