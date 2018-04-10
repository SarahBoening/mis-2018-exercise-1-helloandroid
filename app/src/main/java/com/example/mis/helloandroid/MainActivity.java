package com.example.mis.helloandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
    }

    // https://stackoverflow.com/questions/5474089/how-to-check-currently-internet-connection-is-available-or-not-in-android
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void sendMessage(View view) {

        if (!isNetworkAvailable())
            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
        else {
            String message = editText.getText().toString();
            // To make sure the URL is working
            if (!message.startsWith("http"))
                message = "http://" + message;
            new GetPageTask(imageView, textView).execute(message);

        }
    }

    // inspiration: https://stackoverflow.com/questions/14418021/get-text-from-web-page-to-string
    public class GetPageTask extends AsyncTask<String, Void, String> {
        Bitmap bm;
        String text;
        Exception exception;
        private TextView textview;
        private ImageView imageView;

        public GetPageTask (ImageView iv, TextView tv){
            this.imageView = iv;
            this.textview = tv;
        }

        @Override
        protected String doInBackground(String... urlString) {

            HttpURLConnection http = null;
            BufferedReader rd = null;
            String type = "";
            StringBuffer result = new StringBuffer();

            try {

                URL url = new URL(urlString[0]);
                http = (HttpURLConnection) url.openConnection();
                http.connect();
                String contentType = http.getContentType();
                // Check type of content from server response header to switch representation
                if(contentType.startsWith("image")) {
                    // https://stackoverflow.com/questions/5776851/load-image-from-url
                    bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    type = "image";
                }
                else {
                    String line;
                    rd = new BufferedReader(new InputStreamReader(http.getInputStream()));

                    while ((line = rd.readLine()) != null)
                        result.append(line);
                    text = result.toString();
                    rd.close();
                    http.disconnect();
                    if (contentType.contains("text/html"))
                        type = "html";
                    else
                        type =  "text";
                }
            } catch (MalformedURLException e) {
                Log.e("AsyncTask", e.getMessage(), e);
                exception = e;
            } catch (SocketTimeoutException e){
                Log.e("AsyncTask", e.getMessage(), e);
                exception = e;
            } catch (Exception e) {
                Log.e("AsyncTask", e.getMessage(), e);
                exception = e;
            }

            return type;
        }

        @Override
        protected void onPostExecute( String result) {
            if (exception != null)
                //Show execptions that might have occurred during the http request
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            // representation on screen based on content-type
            switch (result) {
                case "html":
                    // clear imageview https://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
                    imageView.setImageResource(android.R.color.transparent);
                    textview.setText("");
                    //makes text scrollable
                    textview.setMovementMethod( new ScrollingMovementMethod());
                    textview.setText(Html.fromHtml(text));
                    break;
                case "image":
                    imageView.setImageResource(android.R.color.transparent);
                    textview.setText("");
                    imageView.setImageBitmap(bm);
                    break;
                default:
                    imageView.setImageResource(android.R.color.transparent);
                    textview.setText("");
                    textview.setText(text);
                    break;
            }

        }
    }


}
