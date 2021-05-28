package vn.edu.usth.usthspeechrecord.fragments;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import vn.edu.usth.usthspeechrecord.views.MediaButton;
import vn.edu.usth.usthspeechrecord.R;
import vn.edu.usth.usthspeechrecord.utils.VolleySingleton;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EditFragment extends Fragment {

    RequestQueue mQueue;
    private static final String main_url =  "https://voiceviet.itrithuc.vn/api/v1";
    MediaPlayer mMediaPlayer;

    EditText mEditText;
    MediaButton btnPlay;
    ImageButton btnDone, btnNext;
    ProgressBar circleBar;

    DownloadManager downloadManager;
    public String mToken = "";
    String download_url;
    Uri uri;
    String subpath;
    String pathToFile, mtext, mId;
    Context context;

    public EditFragment() {
        // Required empty public constructor
    }

    public static EditFragment newInstance(String token) {
        EditFragment editFragment = new EditFragment();
        Bundle args = new Bundle();
        args.putString("TOKEN", token);
        editFragment.setArguments(args);
        return editFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_edit, container, false);
        mToken = getArguments().getString("TOKEN");
        context=getActivity();

        btnPlay = view.findViewById(R.id.mediaButton);
        btnDone = view.findViewById(R.id.btn_done);
        btnNext = view.findViewById(R.id.btn_next);
        mEditText = view.findViewById(R.id.text_editor);
        circleBar = view.findViewById(R.id.edit_circle_bar);
        circleBar.setVisibility(View.INVISIBLE);

        disableButton();
        init();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathSave = getFilename();
                Log.d("Local", pathSave);
                MediaButton mb = (MediaButton) v;
                switch (mb.getState()) {
                    case 0:
                        String src = pathSave + "/" + mId + subpath;
                        Log.d("SRC: ", src);
//                            mMediaPlayer.setDataSource(src);
                        mMediaPlayer.start();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {

                                if (mMediaPlayer != null) {
                                    mMediaPlayer = new MediaPlayer();
                                    mMediaPlayer.stop();
                                    mMediaPlayer.release();
                                }
                                Toast.makeText(getActivity(), getString(R.string.has_stopped), Toast.LENGTH_SHORT).show();

                                btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                                mb.changeState();
                            }
                        });
                        btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                        Toast.makeText(getActivity(), getString(R.string.playing_the_audio), Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        if (mMediaPlayer != null) {
                            mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                        Toast.makeText(getActivity(), getString(R.string.has_stopped), Toast.LENGTH_SHORT).show();

                        btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        break;
                }
                mb.changeState();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFilename()!=null) deleteFile();
                getVoiceRef();
                btnPlay.setEnabled(true);
                btnPlay.setBackgroundResource(R.drawable.circle_gradient);
                btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                if (btnPlay.getState()==1){
                    btnPlay.changeState();
                }
                btnDone.setEnabled(true);
                btnDone.setBackgroundResource(R.drawable.play_retry_bg);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                SendEditText(mId, text);
                Toast.makeText(getActivity(), getString(R.string.already_edited), Toast.LENGTH_SHORT).show();

                btnPlay.setBackgroundResource(R.drawable.circle_gradient);
                btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                if (btnPlay.getState()==1){
                    btnPlay.changeState();
                }
                deleteFile();
                getVoiceRef();
            }
        });
        return view;
    }

    private void init() {
        if (getFilename()!=null) deleteFile();
        getVoiceRef();
        btnPlay.setEnabled(true);
        btnPlay.setBackgroundResource(R.drawable.circle_gradient);
        btnDone.setEnabled(true);
        btnDone.setBackgroundResource(R.drawable.play_retry_bg);
    }

    private void getVoiceRef() {
        String url = main_url + "/voice/random";
        mEditText.setText("");
        circleBar.setVisibility(View.VISIBLE);
        mMediaPlayer = new MediaPlayer();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("resp");
                    pathToFile = jsonObject.getString("pathToFile");
                    subpath = pathToFile.substring(pathToFile.length()-4);
                    Log.d("subpath: ", subpath);
                    mtext = jsonObject.getString("text");
                    mId = jsonObject.getString("id");
                    download_url = "https://voiceviet.itrithuc.vn" + pathToFile;
                    Log.e(TAG, "onResponse: "+download_url );
                    mMediaPlayer.setDataSource(context,Uri.parse(download_url));
                    mMediaPlayer.prepare();
                    uri = Uri.parse(download_url);
                    DownloadVoice(uri);
                    mEditText.setText(mtext);
                    circleBar.setVisibility(View.INVISIBLE);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public HashMap<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + mToken);
                return headers;
            }
        };
        mQueue.add(request);
    }

    private void DownloadVoice(Uri uri) {
        downloadManager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setTitle("Download Voice");
        request.setDescription("Download voice mp3 file");
        request.setDestinationInExternalPublicDir("/VoiceDownload", mId + subpath);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"VoiceDownload");

        if(!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath());
    }

    private void deleteFile() {
        File file = new File(getFilename() + mId + subpath);
        file.delete();
    }

    private void SendEditText(String id, String text) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = main_url + "/text/" + id;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String code = response.getString("code");
                    if (code.equals("200")) {
                        Log.d("Edit: ","Edit successful");
                    } else {
                        Log.d("Edit: ", "Edit failed");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public HashMap<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + mToken);
                return headers;
            }
        };
        mQueue.add(request);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void disableButton() {
        btnPlay.setEnabled(false);
        btnPlay.setBackgroundResource(R.drawable.circle_gradient);
        btnDone.setEnabled(false);
        btnDone.setBackgroundResource(R.drawable.play_retry_disable);
    }
}
