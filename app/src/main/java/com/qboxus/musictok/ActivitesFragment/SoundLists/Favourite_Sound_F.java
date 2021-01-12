package com.qboxus.musictok.ActivitesFragment.SoundLists;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.qboxus.musictok.Adapters.Favourite_Sound_Adapter;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.Models.Sounds_GetSet;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Favourite_Sound_F extends RootFragment implements Player.EventListener {


    Context context;
    View view;
    ArrayList<Sounds_GetSet> datalist;
    Favourite_Sound_Adapter adapter;
    static boolean active = false;
    RecyclerView recyclerView;

    DownloadRequest prDownloader;


    public static String running_sound_id;


    ProgressBar pbar;
    SwipeRefreshLayout swiperefresh;
    RelativeLayout no_data_layout;

    public Favourite_Sound_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_sound_list, container, false);

        context = getContext();

        running_sound_id = "none";


        PRDownloader.initialize(context);

        pbar = view.findViewById(R.id.pbar);
        no_data_layout = view.findViewById(R.id.no_data_layout);

        recyclerView = view.findViewById(R.id.listview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                Call_Api_For_get_allsound();
            }
        });

        Call_Api_For_get_allsound();


        return view;
    }


    public void Set_adapter() {

        adapter = new Favourite_Sound_Adapter(context, datalist, new Favourite_Sound_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Sounds_GetSet item) {

                if (view.getId() == R.id.done) {
                    StopPlaying();
                    Down_load_mp3(item.id, item.sound_name, item.acc_path);
                } else if (view.getId() == R.id.fav_btn) {
                    Call_Api_For_Fav_sound(postion, item.id);
                } else {
                    StopPlaying();
                    playaudio(view, item);
                }

            }
        });

        recyclerView.setAdapter(adapter);


    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if ((view != null && visible)) {
            Call_Api_For_get_allsound();
        } else {
            StopPlaying();
        }
    }


    private void Call_Api_For_get_allsound() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showFavouriteSounds, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                swiperefresh.setRefreshing(false);
                pbar.setVisibility(View.GONE);
                Parse_data(resp);
            }
        });


    }


    public void Parse_data(String responce) {

        datalist = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i).optJSONObject("Sound");

                    Sounds_GetSet item = new Sounds_GetSet();

                    item.id = itemdata.optString("id");


                    String accpath = itemdata.optString("audio");
                    if (accpath != null && accpath.contains("http"))
                        item.acc_path = itemdata.optString("audio");
                    else
                        item.acc_path = ApiLinks.base_url + itemdata.optString("audio");



                    item.sound_name = itemdata.optString("name");
                    item.description = itemdata.optString("description");
                    item.section = itemdata.optString("section");

                    String thum_image = itemdata.optString("thum");

                    if (thum_image != null && thum_image.contains("http"))
                        item.thum = itemdata.optString("thum");
                    else
                        item.thum = ApiLinks.base_url + itemdata.optString("thum");

                    item.duration = itemdata.optString("duration");
                    item.date_created = itemdata.optString("created");

                    datalist.add(item);
                }

                Set_adapter();
                no_data_layout.setVisibility(View.GONE);

            } else {
                no_data_layout.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }


    View previous_view;
    SimpleExoPlayer player;
    String previous_url = "none";

    public void playaudio(View view, final Sounds_GetSet item) {
        previous_view = view;

        if (previous_url.equals(item.acc_path)) {

            previous_url = "none";
            running_sound_id = "none";
        } else {

            previous_url = item.acc_path;
            running_sound_id = item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.acc_path));


            player.prepare(videoSource);
            player.addListener(this);


            player.setPlayWhenReady(true);


        }

    }


    public void StopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;

        running_sound_id = "null";

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    public void Show_Run_State() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }

    }


    public void Show_loading_state() {
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void show_Stop_state() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            previous_view.findViewById(R.id.done).setVisibility(View.GONE);
        }

        running_sound_id = "none";

    }


    public void Down_load_mp3(final String id, final String sound_name, String url) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        prDownloader = PRDownloader.download(url, Variables.app_hided_folder, Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Intent output = new Intent();
                output.putExtra("isSelected", "yes");
                output.putExtra("sound_name", sound_name);
                output.putExtra("sound_id", id);
                getActivity().setResult(RESULT_OK, output);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });

    }


    private void Call_Api_For_Fav_sound(final int pos, String sound_id) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));
            parameters.put("sound_id", sound_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(context, false, false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.addSoundFavourite, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                datalist.remove(pos);
                adapter.notifyItemRemoved(pos);
                adapter.notifyDataSetChanged();

                if (!datalist.isEmpty())
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                else
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);

            }
        });


    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            Show_loading_state();
        } else if (playbackState == Player.STATE_READY) {
            Show_Run_State();
        } else if (playbackState == Player.STATE_ENDED) {
            show_Stop_state();
        }

    }

}
