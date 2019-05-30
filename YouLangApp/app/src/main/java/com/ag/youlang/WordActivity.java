package com.ag.youlang;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

public class WordActivity extends AppCompatActivity {

    int id;

    String getDetailedWordURL = "http://youlangmobile-001-site1.gtempurl.com/Home/GetDetailedWord";

    String addWordURL = "http://youlangmobile-001-site1.gtempurl.com/Home/AddWord";

    CurrentUser currentUser;

    ImageButton sendButton;

    Dialog dialog;

    TextView wordTV;
    TextView translateTV;
    TextView transcriptionTV;
    TextView partOfSpeechTV;
    TextView descriptionTV;

    Button addWordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        currentUser = CurrentUser.getInstance();

        Intent intent = getIntent();

        id = Integer.parseInt(intent.getStringExtra("id"));

        wordTV = (TextView) findViewById(R.id.wordTV);
        translateTV = (TextView) findViewById(R.id.translateTV);
        transcriptionTV = (TextView) findViewById(R.id.transcriptionTV);
        partOfSpeechTV = (TextView) findViewById(R.id.partOfSpeechTV);
        descriptionTV = (TextView) findViewById(R.id.descrTV);

        addWordButton = (Button) findViewById(R.id.addWordButton);

        DetailedWordRequest wordRequest = new DetailedWordRequest();
        wordRequest.Login = currentUser.Login;
        wordRequest.WordId = String.valueOf(id);

        sendDetailedWordRequest(wordRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendDetailedWordRequest(DetailedWordRequest wordRequest) {

        try {

            final Gson gson = new Gson();

            String JSONData = gson.toJson(wordRequest);

            SendDetailedWordRequest sendRequest = new SendDetailedWordRequest();

            dialog = new Dialog(WordActivity.this);
            dialog.setContentView(R.layout.loading_layout);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
            imageView.setBackgroundResource(R.drawable.anim_load);

            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
            animation.start();

            dialog.setTitle("Loading...");
            dialog.setCancelable(false);
            dialog.show();

            sendRequest.execute(getDetailedWordURL, "wordRequest", JSONData);
        } catch (Exception ex) {

            Toast.makeText(WordActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SendDetailedWordRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {

            String answer = null;

            try {

                String url = params[0];
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                String urlParameters = params[1] + "=" + params[2];

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                answer = response.toString();
            } catch (Exception ex) {
                Toast.makeText(WordActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                Type type = new TypeToken<DetailedWord>() {
                }.getType();

                final DetailedWord word = new Gson().fromJson(message, type);

                if (word != null) {

                    wordTV.setText(word.Word);

                    translateTV.setText(word.Translate);

                    transcriptionTV.setText(word.Transcription);

                    partOfSpeechTV.setText(word.PartOfSpeech);

                    descriptionTV.setText(word.Description);

                    setTitle(word.Word);

                    if (word.IsInUsersVoc)
                        addWordButton.setBackgroundResource(R.drawable.star_on);

                    addWordButton.setOnClickListener(new View.OnClickListener() {

                        boolean pressed = word.IsInUsersVoc;
                        @Override
                        public void onClick(View v) {

                            WordAddingParams params = new WordAddingParams();

                            params.Login = currentUser.Login;
                            params.WordId = String.valueOf(id);

                            if (pressed) {
                                params.Add = false;
                                sendAddWordRequest(params);

                                addWordButton.setBackgroundResource(R.drawable.star_off);

                                pressed = false;
                            }
                            else {

                                params.Add = true;
                                sendAddWordRequest(params);

                                addWordButton.setBackgroundResource(R.drawable.star_on);

                                pressed = true;
                            }
                        }
                    });
                }
            } catch (Exception e) {

                Toast.makeText(WordActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendAddWordRequest(WordAddingParams params) {

        try {

            final Gson gson = new Gson();

            String JSONData = gson.toJson(params);

            SendAddWordRequest sendRequest = new SendAddWordRequest();

            dialog = new Dialog(WordActivity.this);
            dialog.setContentView(R.layout.loading_layout);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
            imageView.setBackgroundResource(R.drawable.anim_load);

            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
            animation.start();

            dialog.setTitle("Loading...");
            dialog.setCancelable(false);
            dialog.show();

            sendRequest.execute(addWordURL, "addingParams", JSONData);
        } catch (Exception ex) {

            Toast.makeText(WordActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SendAddWordRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {

            String answer = null;

            try {

                String url = params[0];
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                String urlParameters = params[1] + "=" + params[2];

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                answer = response.toString();
            } catch (Exception ex) {
                Toast.makeText(WordActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                Type type = new TypeToken<AddWordActionResult>() {
                }.getType();

                AddWordActionResult actionResult = new Gson().fromJson(message, type);

                if (actionResult != null) {
                    switch (actionResult) {
                        case Added:

                            Toast.makeText(WordActivity.this, "Added in My Vocabulary", Toast.LENGTH_SHORT).show();

                            break;
                        case Deleted:

                            Toast.makeText(WordActivity.this, "Deleted from My Vocabulary", Toast.LENGTH_SHORT).show();

                            break;
                        case Failed:

                            Toast.makeText(WordActivity.this, "Error", Toast.LENGTH_LONG).show();

                            break;
                    }
                }
            } catch (Exception e) {

                Toast.makeText(WordActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
