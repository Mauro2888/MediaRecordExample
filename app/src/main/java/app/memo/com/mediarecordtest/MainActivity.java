package app.memo.com.mediarecordtest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button mPlay,mStop,mRec;
    private MediaRecorder mRecorder;
    private File audioFile = null;
    Uri newUri;

    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlay = (Button) findViewById(R.id.btnPlay);
        mRec = (Button) findViewById(R.id.btnRec);
        mStop = (Button) findViewById(R.id.btnStop);

        //Get Permission
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }

        mRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRec.setEnabled(false);
                mStop.setEnabled(true);

                File recorderDir = Environment.getExternalStorageDirectory();
                try {
                    audioFile = File.createTempFile("sound", ".3gp", recorderDir);
                } catch (IOException e) {
                    Log.e("TAG", "error record");
                }

                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile(audioFile.getAbsolutePath());
                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRecorder.start();
                Toast.makeText(MainActivity.this, "Record Start", Toast.LENGTH_SHORT).show();

            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRec.setEnabled(true);
                mStop.setEnabled(false);
                mRecorder.stop();
                mRecorder.release();
                addRecordToContentProvider();
                Toast.makeText(MainActivity.this, "Record Stop", Toast.LENGTH_SHORT).show();
            }

            private void addRecordToContentProvider() {
                ContentValues content = new ContentValues(4);
                content.put(MediaStore.Audio.Media.TITLE, "audio" + audioFile.getName());
                content.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
                content.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());

                ContentResolver contentResolver = getContentResolver();
                Uri UriBase = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                newUri = contentResolver.insert(UriBase, content);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
                Toast.makeText(MainActivity.this, "added" + newUri, Toast.LENGTH_SHORT).show();
            }
        });
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayer play = new MediaPlayer();
                try {
                    play.setDataSource(MainActivity.this,newUri);
                    play.prepare();
                    play.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    //Result Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 200:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToWriteAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) MainActivity.super.finish();
        if (!permissionToWriteAccepted ) MainActivity.super.finish();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
