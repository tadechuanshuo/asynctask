package com.tangqipeng.async;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "ASYNCTASK";

    private Button execute;
    private Button cancel;
    private ProgressBar progressBar;
    private TextView textView;

    private MyTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        execute = (Button)findViewById(R.id.execute);
        cancel = (Button)findViewById(R.id.cancel);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        textView = (TextView)findViewById(R.id.text_view);
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTask = new MyTask();
                mTask.execute("http://www.baidu.com");
                execute.setEnabled(false);
                cancel.setEnabled(true);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTask.cancel(true);
            }
        });
    }

    private class MyTask extends AsyncTask<String, Integer, String>{
        public MyTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textView.setText("loading...");
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "doInBackground(Params... params) called");
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(params[0]);
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    long total = entity.getContentLength();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int count = 0;
                    int length = -1;
                    while ((length = is.read(bytes)) != -1){
                        stream.write(bytes, 0, length);
                        count += length;
                        publishProgress((int)((count /(float)length) * 100));
                        Thread.sleep(500);
                    }
                    return new String(stream.toByteArray(), "gb2312");
                }
            }catch (Exception e){
                Log.i(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i(TAG, "onPostExecute(Result result) called");
            textView.setText(s);

            execute.setEnabled(true);
            cancel.setEnabled(false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i(TAG, "onProgressUpdate(Progress... progresses) called");
            progressBar.setProgress(values[0]);
            textView.setText("loading..." + values[0] + "%");
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            Log.i(TAG, "onCancelled() called:"+s.toString());
            textView.setText("cancelled"+s.toString());
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(TAG, "onCancelled() called");
            textView.setText("cancelled");
            progressBar.setProgress(0);

            execute.setEnabled(true);
            cancel.setEnabled(false);
        }

    }
}
