package com.danielkim.soundrecorder.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.MySharedPreferences;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingService;
import com.danielkim.soundrecorder.utils.CallBackUtil;
import com.danielkim.soundrecorder.utils.OkhttpUtil;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.Call;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();

    private int position;

    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;
    private CheckBox syncCheckBox = null;
    private Thread syncThread = null;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;

    private Boolean recording = false;
    private Boolean network = true;
    private Boolean interrupt = false;
    private String machine = null;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button

    private Button mLoadServerButton = null;

    private EditText mExpNameEditText =null;

    private DBHelper mDatabase;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    public RecordFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        syncCheckBox.setChecked(false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        machine = MySharedPreferences.getPrefDevice(getActivity());
        mDatabase = new DBHelper(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = !recording;
                updateRecord(recording);

                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;

            }
        });

        mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseRecord(mPauseRecording);
                mPauseRecording = !mPauseRecording;
            }
        });

        // Sync checkbox
        syncCheckBox = (CheckBox) recordView.findViewById(R.id.cbxSync);
        syncCheckBox.setChecked(false);
        syncCheckBox.setOnCheckedChangeListener(syncRecordingListener);

        // load audio from server
        mLoadServerButton = (Button) recordView.findViewById(R.id.btnLoadServer);
        mLoadServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAudioFromServer();
            }
        });

        // set ExpName
        mExpNameEditText = (EditText) recordView.findViewById(R.id.editTextExpName);

        return recordView;
    }

    // Recording Start/Stop
    //TODO: recording pause
    private void onRecord(boolean start){

        Intent intent = new Intent(getActivity(), RecordingService.class);

        // put exp name to intent
        Log.d(LOG_TAG, mExpNameEditText.getText().toString());
        intent.putExtra("exp_name", mExpNameEditText.getText().toString());

        if (start) {
            // start recording
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            //mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(),R.string.toast_recording_start,Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }

            //start Chronometer
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        mRecordPromptCount = -1;
                    }

                    mRecordPromptCount++;
                }
            });

            //start RecordingService
            getActivity().startService(intent);
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
            mRecordPromptCount++;

        } else {
            //stop recording
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            //mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText(getString(R.string.record_prompt));

            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {
        if (pause) {
            //pause recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_play ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        } else {
            //resume recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_pause ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }

    public void updateRecord(boolean record){
        String host = MySharedPreferences.getPrefHost(getActivity());

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("recording", record? "1": "0");
        parameters.put("machine", machine);
        OkhttpUtil.okHttpPost("http://"+host+"/syncrecording", parameters, new CallBackUtil.CallBackString() {

            @Override
            public void onFailure(Call call, Exception e) {
                network = false;
                Toast.makeText(getActivity(), "Fail to connect Server, Pls set server address on setting", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "fail to updating record");
            }

            @Override
            public void onResponse(String response) {
                Log.e(LOG_TAG, "success to updating record");
            }
        });
    }

    //sync listener
    CompoundButton.OnCheckedChangeListener syncRecordingListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if(isChecked){
                syncThread = new Thread(runnable);
                interrupt = false;
                syncThread.start();
            }else{
                interrupt = true;
            }
        }
    };

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            String host = MySharedPreferences.getPrefHost(getActivity());
            while(network && !interrupt){
                OkhttpUtil.okHttpGet("http://"+host+"/recording", new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        network = false;
                        interrupt = true;
                        Toast.makeText(getActivity(), "Fail to connect Server, Pls set server address on setting", Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "fail to sync record");
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.e(LOG_TAG, "success to sync record：" + response);
                        boolean temp = recording;
                        String commandMachine = null;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String str = jsonObject.getString("recording");
                            commandMachine = jsonObject.getString("machine");
                            temp = str.equals("true")? true: false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(temp != recording && !commandMachine.equals(machine)){

                            Log.e(LOG_TAG, "current play is " + recording + "I will do"+ temp);
                            recording = !recording;
                            onRecord(mStartRecording);
                            mStartRecording = !mStartRecording;
                        }
                    }
                });

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void loadAudioFromServer(){
        final String host = MySharedPreferences.getPrefHost(getActivity());

        OkhttpUtil.okHttpGet("http://"+host+"/audiolist", new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                network = false;
                Toast.makeText(getActivity(), "Fail to connect Server, Pls set server host on setting", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "fail to load audio from server");
            }

            @Override
            public void onResponse(String response) {
                Log.e(LOG_TAG, "success to get audio list");
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject item = jsonArray.getJSONObject(i);
                        String fileName = item.getString("name");
                        long duration = Long.parseLong(item.getString("len"));
                        String path = downloadFile(host, fileName);
                        if(path != null){
                            mDatabase.addRecording(fileName, path,duration,1);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private String downloadFile(String host, String fileName){
        String url = "http://"+host+"/server_audio/" + fileName;
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/";
        String mDestFileDir = mFilePath;
        String mdestFileName = fileName;
        String path = mFilePath + fileName;
        File file = new File(path);
        if(file.exists()){
            return null;
        }

        OkhttpUtil.okHttpDownloadFile(url, new CallBackUtil.CallBackFile(mDestFileDir, mdestFileName) {
            @Override
            public void onFailure(Call call, Exception e) {

            }

            @Override
            public void onResponse(File response) {
                Log.d(LOG_TAG, "Download File ");
            }
        });

        return path;
    }
}