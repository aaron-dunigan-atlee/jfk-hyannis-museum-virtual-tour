package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitResource;
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
 * Use the {@link MediaPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaPlayerFragment extends Fragment {

    private static final String MEDIA_URL = "media_url";
    private static final String BACKGROUND_URL = "background_url";
    public static final String NO_MEDIA = "no_media";
    public static final String DEFAULT_BACKGROUND = "default_background";
    private static final boolean DEFAULT_AUTOPLAY = false;
    private static final int DEFAULT_WINDOW_INDEX = 0;
    private static final long DEFAULT_PLAYBACK_POSITION = 0;

    private FragmentActivity mHostActivity;
    private String mBackgroundUrl;
    private SimpleExoPlayer mExoPlayer;
    private String mMediaUrl;
    private long exoPlayerPlaybackPosition = DEFAULT_PLAYBACK_POSITION;
    private int exoPlayerWindowIndex = DEFAULT_WINDOW_INDEX;
    private boolean exoPlayerAutoPlay = DEFAULT_AUTOPLAY;
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
            exoPlayerAutoPlay = savedInstanceState.getBoolean(AUTOPLAY, DEFAULT_AUTOPLAY);
            exoPlayerWindowIndex = savedInstanceState.getInt(WINDOW_INDEX, DEFAULT_WINDOW_INDEX);
            exoPlayerPlaybackPosition = savedInstanceState.getLong(PLAYBACK_POSITION, DEFAULT_PLAYBACK_POSITION);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Thanks for this method of getting the host activity from
        // https://stackoverflow.com/a/31302716/10332984
        mHostActivity = (FragmentActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePlayer();
        playMedia(mMediaUrl, mBackgroundUrl, exoPlayerAutoPlay, exoPlayerWindowIndex, exoPlayerPlaybackPosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    // Initialize ExoPlayer.  See https://google.github.io/ExoPlayer/guide.html
    public void initializePlayer() {
        if (mExoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(mHostActivity);
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
            mExoPlayerView.setPlayer(mExoPlayer);
        }
    }

    private void playMedia(String mediaUrl, String backgroundUrl, boolean autoPlay, int windowIndex, long playbackPosition) {
        if (backgroundUrl != null) {
            if (backgroundUrl.equals(DEFAULT_BACKGROUND)) {
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
        Uri mediaUri = Uri.parse(mediaUrl);
        if (!mediaUrl.equals(NO_MEDIA)) {
            //Prepare media source.  See https://google.github.io/ExoPlayer/guide.html#preparing-the-player
            String appName = getString(R.string.app_name);
            String userAgent = Util.getUserAgent(mHostActivity, appName);
            MediaSource mediaSource = new ExtractorMediaSource.Factory(
                    new DefaultDataSourceFactory(mHostActivity, userAgent)).createMediaSource(mediaUri);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(autoPlay);
            mExoPlayer.seekTo(windowIndex, playbackPosition);
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
