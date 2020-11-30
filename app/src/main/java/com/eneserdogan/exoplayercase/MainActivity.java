package com.eneserdogan.exoplayercase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory;

public class MainActivity extends AppCompatActivity implements Player.EventListener {


    private PlayerView playerView;
    private ProgressBar progressBar;
    private ImageView btFullScreen, exoPlay, exoPause, exoRew, exoFfwd;
    private boolean flag = false;
    private SimpleExoPlayer simpleExoPlayer;
    private DataSource.Factory factory;
    private Uri subtitleUri, videoUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initilialize
        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.progress_bar);
        btFullScreen = playerView.findViewById(R.id.bt_fullscreen);
        exoFfwd = (ImageView) findViewById(R.id.exo_ffwd);
        exoPlay = (ImageView) findViewById(R.id.exo_play);
        exoPause = (ImageView) findViewById(R.id.exo_pause);
        exoRew = (ImageView) findViewById(R.id.exo_rew);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4
        // https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8


        //Video URL parse ediiyor
        videoUrl = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        subtitleUri = Uri.parse("file:///android_asset/sub.srt");


        //initialize ediliyor
        initilializePlayer();


        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {

                    progressBar.setVisibility(View.VISIBLE);

                } else if (playbackState == Player.STATE_READY) {

                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        // Tam ekran buton tıklaması dinleniyor
        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    btFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.fullscreen_24));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    flag = false;
                } else {
                    btFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.fullscreen_exit_24));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    flag = true;

                }
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    private void initilializePlayer() {
        LoadControl loadControl = new DefaultLoadControl();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));


        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(MainActivity.this, trackSelector, loadControl);
        factory = new DefaultHttpDataSourceFactory("exoplayer_video");

        MediaSource[] mediaSources = new MediaSource[2]; 

        //Video Uri'ını fonksiyona göndererek video türüne göre mediaSource değerini alıyoruz
        MediaSource mediaSourceType = buildMediaSource(videoUrl, null);
        mediaSources[0] = mediaSourceType; // videoUri

        //ALtyazı oluşturuuyor
        SingleSampleMediaSource subtitleSource = new SingleSampleMediaSource(subtitleUri, factory,
                Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en", null),
                C.TIME_UNSET);

        //Dizinin 2.elemanı oalrak altyazı mediaSource ekleniyor
        mediaSources[1] = subtitleSource;
        MediaSource mediaSourceLast = new MergingMediaSource(mediaSources);

        // Playerview'a ExoPlayer set ediliyor
        playerView.setPlayer(simpleExoPlayer);
        playerView.setKeepScreenOn(true);

        // Hangi media kaynağı olduğu ve hazır olduğunda başlatma
        simpleExoPlayer.prepare(mediaSourceType);
        simpleExoPlayer.setPlayWhenReady(true);
    }



    // Gelen media tipine göre o mediaSource değeri döndürülüyor
    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri);
        MediaSource mediaSource = null;
        switch (type) {
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(factory).createMediaSource(uri);
                System.out.println("Dash Type ");
                break;


            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(factory).createMediaSource(uri);
                System.out.println("HLS Type ");
                break;


            case C.TYPE_OTHER:
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                mediaSource = new ExtractorMediaSource(uri, factory, extractorsFactory, null, null);
                System.out.println("Other Type ");
                break;


            default:
                System.out.println("Media Tipi Desteklenmiyor");
        }
        return mediaSource;

    }
}