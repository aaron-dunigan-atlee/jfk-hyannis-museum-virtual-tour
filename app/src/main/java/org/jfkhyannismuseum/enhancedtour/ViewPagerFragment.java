package org.jfkhyannismuseum.enhancedtour;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jfkhyannismuseum.enhancedtour.model.Exhibit;
import org.jfkhyannismuseum.enhancedtour.model.ExhibitPiece;
import org.jfkhyannismuseum.enhancedtour.model.ExhibitResource;
import org.jfkhyannismuseum.enhancedtour.utils.JsonUtils;

import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPagerFragment extends Fragment {

    // Argument keys
    private static final String PIECE_ID = MainActivity.PIECE_ID;

    private int mPieceId;
    private ExhibitPiece mPiece;
    private Context mContext;
    private FragmentActivity mHostActivity;
    private FragmentSharedViewModel fragmentSharedViewModel;
    private Fragment mMediaFragment;

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

        fragmentSharedViewModel.getResources().observe(this, new Observer<Hashtable<Integer,ExhibitResource>>() {
            @Override
            public void onChanged(@Nullable Hashtable<Integer,ExhibitResource> resourceTable) {
                if (resourceTable != null) {
                    setResource(resourceTable.get(mPieceId));
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
        // Setting the resource in the sharedViewModel will trigger setResource() to load
        // the media fragment.
        fragmentSharedViewModel.setResource(mPieceId, resource);
        FragmentManager fragmentManager = getChildFragmentManager();
        ResourceListFragment resourceListFragment = ResourceListFragment.newInstance(mPiece);
        fragmentManager.beginTransaction()
                .replace(R.id.resource_list_container,resourceListFragment)
                .commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Get the calling Activity as a FragmentActivity so we can reference its ViewModelProviders.
        mHostActivity = (FragmentActivity) context;
        fragmentSharedViewModel = ViewModelProviders.of(mHostActivity).get(FragmentSharedViewModel.class);
    }

    private void setResource(ExhibitResource resource) {
        String text = resource.getText();
        Log.d("ViewPagerFragment","Text: " + text);
        pieceDescriptionTextView.setText(text);
        FragmentManager fragmentManager = getChildFragmentManager();
        if (resource.getType().equals(ExhibitResource.IMAGE)) {
            mMediaFragment = ImageFragment
                    .newInstance(resource.getResourceURL());
        } else {
            mMediaFragment = MediaPlayerFragment
                    .newInstance(resource.getResourceURL(), resource.getBackgroundImageURL());
        }
        fragmentManager.beginTransaction()
                .replace(R.id.media_player_container, mMediaFragment)
                .commit();
    }

    public void pauseMedia() {
        if (mMediaFragment instanceof MediaPlayerFragment) {
            ((MediaPlayerFragment) mMediaFragment).pausePlayer();
        }
    }

    public void resumeMedia() {
        if (mMediaFragment instanceof MediaPlayerFragment) {
            ((MediaPlayerFragment) mMediaFragment).resumePlayer();
        }
    }

}
