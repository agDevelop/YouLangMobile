package com.ag.youlang;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    CurrentUser currentUser;

    String getVocabularyURL = "http://youlangmobile-001-site1.gtempurl.com/Home/GetVocabulary";

    String getUserVocabularyURL = "http://youlangmobile-001-site1.gtempurl.com/Home/GetUserVocabulary";

    String addWordURL = "http://youlangmobile-001-site1.gtempurl.com/Home/AddWord";

    String getProfileURL = "http://youlangmobile-001-site1.gtempurl.com/Home/GetProfile";

    String getTranslationTestURL = "http://youlangmobile-001-site1.gtempurl.com/Home/GenerateTranslationTest";

    //TranslationTest
    TranslationTest translationTest = null;
    int currentTranslateTaskCounter = 0;
    ArrayList<TranslateTestResult> translateTestResults = null;

    Dialog dialog;

    Dialog startDialog;

    Dialog loadImageDialog;

    int currentPage = 0;

    TabHost tabHost;

    Menu menu;

    ImageView userProfileView;

    ImageView imageView4;
    private static final int REQUEST = 1;

    Uri selectedImage;

    Bitmap bitmapToLoad = null;

    ImageView previewImage;

    Thread myThread;

    ArrayList<String> displayedMessages;

    Map<String, String> userMsgPair;

    public static final int IDM_OPEN = 101;

    View imageSaveSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentUser = CurrentUser.getInstance();

        View header = navigationView.getHeaderView(0);

        TextView loginTextView = (TextView) header.findViewById(R.id.headerTextView);
        loginTextView.setText(currentUser.Login);

        userProfileView = (ImageView) header.findViewById(R.id.imageView);

        registerForContextMenu(userProfileView);

        userProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    final Dialog imageViewDialog = new Dialog(MainActivity.this);
                    imageViewDialog.setContentView(R.layout.upload_image_layout);

                    ImageView imgVw = (ImageView) imageViewDialog.findViewById(R.id.imageView7);
                    imgVw.setImageDrawable(userProfileView.getDrawable());

                    Button button33 = (Button) imageViewDialog.findViewById(R.id.button33);
                    button33.setText("Close");
                    button33.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            imageViewDialog.dismiss();
                        }
                    });

                    Button button34 = (Button) imageViewDialog.findViewById(R.id.button34);
                    button34.setVisibility(View.INVISIBLE);
                    button34.setEnabled(false);

                    Button button35 = (Button) imageViewDialog.findViewById(R.id.button35);
                    button35.setVisibility(View.INVISIBLE);
                    button35.setEnabled(false);

                    imageViewDialog.setCancelable(true);
                    imageViewDialog.show();
                } catch (Exception ex) {

                }
            }
        });


        if (!checkWritePermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        setTitle("Courses");
    }

    public boolean checkWritePermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        imageSaveSender = v;

        menu.add(Menu.NONE, IDM_OPEN, Menu.NONE, "Save");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        CharSequence message;
        switch (item.getItemId()) {
            case IDM_OPEN:

                message = "Saved";

                ImageView sender = (ImageView) imageSaveSender;

                if (sender != null) {
                    Bitmap bitmap = ((BitmapDrawable) sender.getDrawable()).getBitmap();

                    saveToExternalStorage(bitmap);
                }

                break;
            default:
                return super.onContextItemSelected(item);
        }
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        return true;
    }

    private void saveToExternalStorage(Bitmap finalBitmap) {
        try {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File myDir = new File(root + "/YouLang");
            myDir.mkdirs();
            String fname = "Image-" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(myDir, fname);
            if (file.exists())
                file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });
        } catch (Exception ex) {

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_courses) {

        } else if (id == R.id.nav_theory) {

        } else if (id == R.id.nav_tests) {

            setTitle("Test");

            final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

            frameLayout.removeAllViews();

            View cView = getLayoutInflater().inflate(R.layout.translation_test_start, null);

            RelativeLayout relativeLayout = (RelativeLayout) cView.findViewById(R.id.translationStartLayout);

            Button startTestButton = (Button) relativeLayout.findViewById(R.id.startTestButton);

            startTestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Toast.makeText(MainActivity.this, "Surprise!", Toast.LENGTH_SHORT).show();

                    sendTranslationTestRequest();
                }
            });

            frameLayout.addView(cView);

        } else if (id == R.id.nav_vocabulary) {

            sendVocabularyRequest();

        } else if (id == R.id.nav_uservocabulary) {

            sendUserVocabularyRequest();

        } else if (id == R.id.nav_profile) {

            sendProfileRequest();

        } else if (id == R.id.nav_exit) {

            System.exit(-1);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendVocabularyRequest() {
        currentPage = 4;

        try {
            setTitle("Vocabulary");

            final Gson gson = new Gson();

            final String JSONData = gson.toJson(currentUser.Login);

            SendVocabularyRequest sendRequest = new SendVocabularyRequest();

            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.loading_layout);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
            imageView.setBackgroundResource(R.drawable.anim_load);

            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
            animation.start();

            dialog.setTitle("Loading...");
            dialog.setCancelable(false);
            dialog.show();

            sendRequest.execute(getVocabularyURL, "login", JSONData);
        } catch (Exception ex) {

            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SendVocabularyRequest extends AsyncTask<String, Void, String> {

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
                Toast.makeText(MainActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

                Type listType = new TypeToken<ArrayList<VocabularyItem>>() {
                }.getType();

                List<VocabularyItem> yourClassList = new Gson().fromJson(message, listType);

                frameLayout.removeAllViews();

                View cView = getLayoutInflater().inflate(R.layout.vocabulary, null);

                LinearLayout layout = (LinearLayout) cView.findViewById(R.id.PeopleView);

                for (final VocabularyItem vocabularyItem : yourClassList) {
                    final String s = vocabularyItem.WordId;

                    final View view = getLayoutInflater().inflate(R.layout.word_layout, null);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                Intent intent = new Intent(MainActivity.this, WordActivity.class);
                                intent.putExtra("id", s);

                                startActivity(intent);
                            } catch (Exception ex) {

                            }
                        }
                    });

                    final TextView wordTextView = (TextView) view.findViewById(R.id.textView8);
                    wordTextView.setText(vocabularyItem.Word);

                    final TextView translationTextView = (TextView) view.findViewById(R.id.textView3);
                    translationTextView.setText(vocabularyItem.Translate);

                    final Button addToUserVocButton = (Button) view.findViewById(R.id.button6);

                    if (vocabularyItem.IsInUsersVoc)
                        addToUserVocButton.setBackgroundResource(R.drawable.star_on);

                    addToUserVocButton.setOnClickListener(new View.OnClickListener() {

                        boolean pressed = vocabularyItem.IsInUsersVoc;

                        @Override
                        public void onClick(View v) {

                            WordAddingParams params = new WordAddingParams();

                            params.Login = currentUser.Login;
                            params.WordId = vocabularyItem.WordId;

                            if (pressed) {
                                params.Add = false;
                                sendAddWordRequest(params);

                                addToUserVocButton.setBackgroundResource(R.drawable.star_off);

                                pressed = false;
                            } else {

                                params.Add = true;
                                sendAddWordRequest(params);

                                addToUserVocButton.setBackgroundResource(R.drawable.star_on);

                                pressed = true;
                            }
                        }
                    });

                    layout.addView(view);
                }

                frameLayout.addView(cView);
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendAddWordRequest(WordAddingParams params) {

        try {

            final Gson gson = new Gson();

            String JSONData = gson.toJson(params);

            SendAddWordRequest sendRequest = new SendAddWordRequest();

            dialog = new Dialog(MainActivity.this);
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

            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(MainActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
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

                            Toast.makeText(MainActivity.this, "Added in My Vocabulary", Toast.LENGTH_SHORT).show();

                            if (currentPage == 5)
                                sendUserVocabularyRequest();

                            break;
                        case Deleted:

                            Toast.makeText(MainActivity.this, "Deleted from My Vocabulary", Toast.LENGTH_SHORT).show();

                            if (currentPage == 5)
                                sendUserVocabularyRequest();

                            break;
                        case Failed:

                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();

                            break;
                    }
                }
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendUserVocabularyRequest() {
        currentPage = 5;

        try {
            setTitle("My Vocabulary");

            final Gson gson = new Gson();

            final String JSONData = gson.toJson(currentUser.Login);

            SendUserVocabularyRequest sendRequest = new SendUserVocabularyRequest();

            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.loading_layout);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
            imageView.setBackgroundResource(R.drawable.anim_load);

            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
            animation.start();

            dialog.setTitle("Loading...");
            dialog.setCancelable(false);
            dialog.show();

            sendRequest.execute(getUserVocabularyURL, "login", JSONData);
        } catch (Exception ex) {

            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SendUserVocabularyRequest extends AsyncTask<String, Void, String> {

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
                Toast.makeText(MainActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

                Type listType = new TypeToken<ArrayList<VocabularyItem>>() {
                }.getType();

                List<VocabularyItem> yourClassList = new Gson().fromJson(message, listType);

                frameLayout.removeAllViews();

                View cView = getLayoutInflater().inflate(R.layout.my_vocabulary, null);

                LinearLayout layout = (LinearLayout) cView.findViewById(R.id.myVocView);

                for (final VocabularyItem vocabularyItem : yourClassList) {
                    final String s = vocabularyItem.WordId;

                    final View view = getLayoutInflater().inflate(R.layout.word_layout, null);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                Intent intent = new Intent(MainActivity.this, WordActivity.class);
                                intent.putExtra("id", s);

                                startActivity(intent);
                            } catch (Exception ex) {

                            }
                        }
                    });

                    final TextView wordTextView = (TextView) view.findViewById(R.id.textView8);
                    wordTextView.setText(vocabularyItem.Word);

                    final TextView translationTextView = (TextView) view.findViewById(R.id.textView3);
                    translationTextView.setText(vocabularyItem.Translate);

                    final Button addToUserVocButton = (Button) view.findViewById(R.id.button6);

                    if (vocabularyItem.IsInUsersVoc)
                        addToUserVocButton.setBackgroundResource(R.drawable.star_on);

                    addToUserVocButton.setOnClickListener(new View.OnClickListener() {

                        boolean pressed = vocabularyItem.IsInUsersVoc;

                        @Override
                        public void onClick(View v) {

                            WordAddingParams params = new WordAddingParams();

                            params.Login = currentUser.Login;
                            params.WordId = vocabularyItem.WordId;

                            if (pressed) {
                                params.Add = false;
                                sendAddWordRequest(params);

                                addToUserVocButton.setBackgroundResource(R.drawable.star_off);

                                pressed = false;
                            } else {

                                params.Add = true;
                                sendAddWordRequest(params);

                                addToUserVocButton.setBackgroundResource(R.drawable.star_on);

                                pressed = true;
                            }
                        }
                    });

                    layout.addView(view);
                }

                frameLayout.addView(cView);
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendProfileRequest() {
        currentPage = 6;
        try {

            setTitle("Profile");

            final Gson gson = new Gson();

            String JSONData = gson.toJson(currentUser.Login);

            SendProfileRequest sendRequest = new SendProfileRequest();

            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.loading_layout);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
            imageView.setBackgroundResource(R.drawable.anim_load);

            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
            animation.start();

            dialog.setTitle("Loading...");
            dialog.setCancelable(false);
            dialog.show();

            sendRequest.execute(getProfileURL, "login", JSONData);
        } catch (Exception ex) {

            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SendProfileRequest extends AsyncTask<String, Void, String> {

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
                Toast.makeText(MainActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                Type type = new TypeToken<ProfileInfo>() {
                }.getType();

                final ProfileInfo profileInfo = new Gson().fromJson(message, type);

                if (profileInfo != null) {

                    final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

                    frameLayout.removeAllViews();

                    View cView = getLayoutInflater().inflate(R.layout.profile_layout, null);

                    LinearLayout layout = (LinearLayout) cView.findViewById(R.id.profileLayout);

                    TextView loginTV = (TextView) layout.findViewById(R.id.profileLoginTV);
                    TextView emailTV = (TextView) layout.findViewById(R.id.profileEmailTV);
                    TextView wordsInVocTV = (TextView) layout.findViewById(R.id.profileWordsTV);

                    loginTV.setText(profileInfo.Login);
                    emailTV.setText(profileInfo.Email);
                    wordsInVocTV.setText(profileInfo.WordsInVocabulary);

                    frameLayout.addView(cView);
                }
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendTranslationTestRequest() {
        currentPage = 3;
        currentTranslateTaskCounter = 0;

        try {
            setTitle("TranslationTest");

            final Gson gson = new Gson();

            final String JSONData = gson.toJson(currentUser.Login);

            SendTranslationTestRequest sendRequest = new SendTranslationTestRequest();

            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.loading_layout);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
            imageView.setBackgroundResource(R.drawable.anim_load);

            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
            animation.start();

            dialog.setTitle("Loading...");
            dialog.setCancelable(false);
            dialog.show();

            sendRequest.execute(getTranslationTestURL, "login", JSONData);
        } catch (Exception ex) {

            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class SendTranslationTestRequest extends AsyncTask<String, Void, String> {

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
                Toast.makeText(MainActivity.this, "doInBackground" + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

                Type listType = new TypeToken<TranslationTest>() {
                }.getType();

                translationTest = new Gson().fromJson(message, listType);

                frameLayout.removeAllViews();

                View cView = getLayoutInflater().inflate(R.layout.translation_test_task, null);

                final RelativeLayout translationLayout = (RelativeLayout) cView.findViewById(R.id.translationTaskLayout);

                final TextView taskNumberTV = (TextView) translationLayout.findViewById(R.id.textView11);
                final TextView wordTV = (TextView) translationLayout.findViewById(R.id.textView12);

                taskNumberTV.setText("1/" + String.valueOf(translationTest.TaskCount));
                wordTV.setText(translationTest.Tasks.get(0).Word);

                final RadioButton radioButton0 = (RadioButton) translationLayout.findViewById(R.id.radioButton_0);
                final RadioButton radioButton1 = (RadioButton) translationLayout.findViewById(R.id.radioButton_1);

                radioButton0.setText(translationTest.Tasks.get(0).Translates.get(0));
                radioButton1.setText(translationTest.Tasks.get(0).Translates.get(1));

                final Button nextButton = (Button) translationLayout.findViewById(R.id.nextTransTaskButton);

                final RadioGroup radioGroup = (RadioGroup) translationLayout.findViewById(R.id.answerRadioGroup);

                translateTestResults = new ArrayList<TranslateTestResult>();

                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {
                            if (currentTranslateTaskCounter < translationTest.TaskCount - 1) {
                                currentTranslateTaskCounter++;

                                taskNumberTV.setText(String.valueOf(currentTranslateTaskCounter + 1) + "/" + String.valueOf(translationTest.TaskCount));
                                wordTV.setText(translationTest.Tasks.get(currentTranslateTaskCounter).Word);

                                radioButton0.setText(translationTest.Tasks.get(currentTranslateTaskCounter).Translates.get(0));
                                radioButton1.setText(translationTest.Tasks.get(currentTranslateTaskCounter).Translates.get(1));

                                TranslateTestResult testResult = new TranslateTestResult();

                                String rText = ((RadioButton) translationLayout.findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();

                                testResult.Word = translationTest.Tasks.get(currentTranslateTaskCounter).Word;
                                testResult.UserTranslate = rText;

                                int correctTranslate = translationTest.Tasks.get(currentTranslateTaskCounter).RightTranslateIndex;

                                testResult.CorrectTranslate = translationTest.Tasks.get(currentTranslateTaskCounter).Translates.get(correctTranslate);

                                testResult.CorrectTranslateIndex = correctTranslate;

                                if (!testResult.UserTranslate.equals(testResult.CorrectTranslate))
                                    testResult.isCorrectAnswer = false;
                                else
                                    testResult.isCorrectAnswer = true;

                                translateTestResults.add(testResult);

                                radioGroup.clearCheck();

                                if (currentTranslateTaskCounter >= translationTest.TaskCount - 1) {
                                    nextButton.setText("Finish");

                                    nextButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            TranslateTestResult testResult = new TranslateTestResult();

                                            String rText = ((RadioButton) translationLayout.findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();

                                            testResult.Word = translationTest.Tasks.get(currentTranslateTaskCounter).Word;
                                            testResult.UserTranslate = rText;

                                            int correctTranslate = translationTest.Tasks.get(currentTranslateTaskCounter).RightTranslateIndex;

                                            testResult.CorrectTranslate = translationTest.Tasks.get(currentTranslateTaskCounter).Translates.get(correctTranslate);

                                            testResult.CorrectTranslateIndex = correctTranslate;

                                            if (!testResult.UserTranslate.equals(testResult.CorrectTranslate))
                                                testResult.isCorrectAnswer = false;
                                            else
                                                testResult.isCorrectAnswer = true;

                                            translateTestResults.add(testResult);

                                            frameLayout.removeAllViews();

                                            View finishView = getLayoutInflater().inflate(R.layout.translation_test_finish, null);

                                            RelativeLayout translationTestFinishLayout = (RelativeLayout) finishView.findViewById(R.id.translationTestFinishLayout);

                                            int tasksCount = 0;
                                            int rightAnswersCount = 0;

                                            for (TranslateTestResult res : translateTestResults) {

                                                if (res.isCorrectAnswer)
                                                    rightAnswersCount++;

                                                tasksCount++;
                                            }

                                            TextView tasksCountTV = (TextView) translationTestFinishLayout.findViewById(R.id.allAnswersTV);
                                            TextView rightAnswersCountTV = (TextView) translationTestFinishLayout.findViewById(R.id.rightAnswersTV);

                                            tasksCountTV.setText(String.valueOf(tasksCount));

                                            rightAnswersCountTV.setText(String.valueOf(rightAnswersCount));

                                            frameLayout.addView(finishView);
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {

                            Toast.makeText(MainActivity.this, e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                frameLayout.addView(cView);
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "onPostExecute" + e.getMessage() + e.getStackTrace(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
