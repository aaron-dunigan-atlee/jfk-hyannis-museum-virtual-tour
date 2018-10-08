package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
public class ViewPagerFragment extends Fragment
        implements MediaPlayerFragment.OnFragmentInteractionListener,
                    ResourceListFragment.OnListFragmentInteractionListener{

    // Argument keys
    public static final String PIECE_ID = MainActivity.PIECE_ID;

    private int mPieceId;
    private ExhibitPiece mPiece;
    private int mExhibitId;
    private Context mContext;

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
    public void onFragmentInteraction(Uri uri) {
        // TODO: Remove this interface if no interaction is needed.
    }

    @Override
    public void onListFragmentInteraction(ExhibitResource resource) {
        // Change the media player to the resource that was clicked.
        MediaPlayerFragment mediaPlayerFragment = MediaPlayerFragment
                .newInstance(resource.getResourceURL(), resource.getBackgroundImageURL());
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_player_container, mediaPlayerFragment)
                .commit();
    }
}
