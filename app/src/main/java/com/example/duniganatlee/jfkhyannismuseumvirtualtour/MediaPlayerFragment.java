package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MediaPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaPlayerFragment extends Fragment {

    private static final String MEDIA_URL = "media_url";
    private static final String BACKGROUND_URL = "background_url";
    public static final String NO_MEDIA = "no_media";
    public static final String DEFAULT_BACKGROUND = "default_background";

    private String mBackgroundUrl;

    private OnFragmentInteractionListener mListener;
    private SimpleExoPlayer mExoPlayer;
    private String mMediaUrl;
    private long exoPlayerPlaybackPosition = 0;
    private int exoPlayerWindowIndex = 0;
    private boolean exoPlayerAutoPlay = false;
    private static final String PLAYBACK_POSITION = "playback_position";
    private static final String WINDOW_INDEX = "window_index";
    private static final String AUTOPLAY = "autoplay";

    // Layout view variables to be bound using ButterKnife.
    @BindView(R.id.player_view) PlayerView mExoPlayerView;

    public MediaPlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mediaUrl URL of media to be played.
     * @param backgroundURL URL of background image to display.
     * @return A new instance of fragment MediaPlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaPlayerFragment newInstance(String mediaUrl, String backgroundURL) {
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        Bundle args = new Bundle();
        args.putString(MEDIA_URL, mediaUrl);
        args.putString(BACKGROUND_URL, backgroundURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMediaUrl = getArguments().getString(MEDIA_URL);
            mBackgroundUrl = getArguments().getString(BACKGROUND_URL);
        } else {
            mMediaUrl = NO_MEDIA;
            mBackgroundUrl = DEFAULT_BACKGROUND;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_media_player, container, false);
        ButterKnife.bind(this, rootView);

        // Get the media to be played, and load previous state of player if applicable.
        if (savedInstanceState != null) {
            exoPlayerAutoPlay = savedInstanceState.getBoolean(AUTOPLAY, false);
            exoPlayerWindowIndex = savedInstanceState.getInt(WINDOW_INDEX, 0);
            exoPlayerPlaybackPosition = savedInstanceState.getLong(PLAYBACK_POSITION, 0);
        }

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // TODO: Probably remove this interface. No need for interaction with this fragment, right?
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePlayer(mMediaUrl);
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    // Initialize ExoPlayer.  See https://google.github.io/ExoPlayer/guide.html
    public void initializePlayer(String mediaUrl) {
        Context context = getContext();
        Uri mediaUri = Uri.parse(mediaUrl);
        if (mExoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
            if (mBackgroundUrl != null) {
                if (mBackgroundUrl.equals(DEFAULT_BACKGROUND)) {
                    Log.d("intitalizePlayer","Setting default artwork.");
                    // TODO: Choose default artwork.
                    mExoPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                            (getResources(), R.drawable.jfklogo_bluebg_mobile));
                } else {
                    // TODO: Load background URL image and set it.
                    // might have to build a Picasso Target.  See https://square.github.io/picasso/2.x/picasso/com/squareup/picasso/Target.html
                    // https://www.codexpedia.com/android/android-download-and-save-image-through-picasso/

                }
            }
            mExoPlayerView.setPlayer(mExoPlayer);
            if (!mMediaUrl.equals(NO_MEDIA)) {
                //Prepare media source.  See https://google.github.io/ExoPlayer/guide.html#preparing-the-player
                String appName = getString(R.string.app_name);
                String userAgent = Util.getUserAgent(context, appName);
                MediaSource mediaSource = new ExtractorMediaSource.Factory(
                        new DefaultDataSourceFactory(context, userAgent)).createMediaSource(mediaUri);
                mExoPlayer.prepare(mediaSource);
                mExoPlayer.setPlayWhenReady(exoPlayerAutoPlay);
                mExoPlayer.seekTo(exoPlayerWindowIndex, exoPlayerPlaybackPosition);
            }
        }
    }

    // Release ExoPlayer
    private void releasePlayer() {
        // Get current state of player before releasing, in case we need to
        // restore the player state/position later (e.g. on screen rotation).
        exoPlayerPlaybackPosition = mExoPlayer.getCurrentPosition();
        exoPlayerWindowIndex = mExoPlayer.getCurrentWindowIndex();
        // getPlayWhenReady() will be true if player is currently playing; false otherwise.
        // Therefore, this tells us whether to continue playing when the activity resumes, or just
        // queue up the track in paused state.
        // See https://github.com/google/ExoPlayer/issues/3570
        exoPlayerAutoPlay = mExoPlayer.getPlayWhenReady();
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }
}
