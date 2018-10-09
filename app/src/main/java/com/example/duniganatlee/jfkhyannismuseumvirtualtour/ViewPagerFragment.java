package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitPiece;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitResource;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.JsonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPagerFragment extends Fragment {

    // Argument keys
    public static final String PIECE_ID = MainActivity.PIECE_ID;

    private int mPieceId;
    private ExhibitPiece mPiece;
    private int mExhibitId;
    private Context mContext;
    private FragmentActivity mHostActivity;

    @BindView(R.id.piece_description_text_view) TextView pieceDescriptionTextView;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ViewPagerFragment.
     */
    public static ViewPagerFragment newInstance(int pieceId) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putInt(PIECE_ID, pieceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (getArguments() != null) {
            mPieceId = getArguments().getInt(PIECE_ID);
            String exhibitsJson = JsonUtils.loadJSONFromAsset(mContext);
            Exhibit[] exhibitsList = JsonUtils.parseExhibitList(exhibitsJson);
            mPiece = Exhibit.getPieceById(exhibitsList, mPieceId);
        } else {
            mPieceId = MainActivity.WELCOME_ID;
        }
        FragmentSharedViewModel model = ViewModelProviders.of(mHostActivity).get(FragmentSharedViewModel.class);
        model.getResource().observe(this, new Observer<ExhibitResource>() {
            @Override
            public void onChanged(@Nullable ExhibitResource resource) {
                if (resource != null) {
                    FragmentManager fragmentManager = getChildFragmentManager();
                    MediaPlayerFragment mediaPlayerFragment = MediaPlayerFragment
                            .newInstance(resource.getResourceURL(), resource.getBackgroundImageURL());
                    fragmentManager.beginTransaction()
                            .replace(R.id.media_player_container, mediaPlayerFragment)
                            .commit();

                }
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_pager, container, false);
        ButterKnife.bind(this, rootView);
        replaceFragments();
        // Swap out the description
        pieceDescriptionTextView.setText(mPiece.getDescription());
        return rootView;
    }

    private void replaceFragments() {
        // Replace the media player fragment and the resource list fragment.
        // By default, load the piece narration and description, which is the first resource.
        // TODO: Create helper functions in ExhibitPiece to get narration and background.
        Log.d("ViewPager", "Replacing fragments.");
        ExhibitResource resource = mPiece.getResources().get(0);
        FragmentManager fragmentManager = getChildFragmentManager();
        MediaPlayerFragment mediaPlayerFragment = MediaPlayerFragment
                .newInstance(resource.getResourceURL(), resource.getBackgroundImageURL());
        ResourceListFragment resourceListFragment = ResourceListFragment.newInstance(mPiece);
        fragmentManager.beginTransaction()
                .replace(R.id.resource_list_container,resourceListFragment)
                .replace(R.id.media_player_container, mediaPlayerFragment)
                .commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHostActivity = (FragmentActivity) context;
    }
}
