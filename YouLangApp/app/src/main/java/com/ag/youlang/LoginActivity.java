package com.ag.youlang;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class LoginActivity extends AppCompatActivity {

    Dialog dialog;

    String loginURL = "http://youlangmobile-001-site1.gtempurl.com/Home/Login";

    String existenceURL = "http://youlangmobile-001-site1.gtempurl.com/Home/CheckExistence";

    String registreURL = "http://youlangmobile-001-site1.gtempurl.com/Home/RegistreUser";

    String sendLetterURL = "http://youlangmobile-001-site1.gtempurl.com/Home/SendConfirmingLetter";

    int method = 0;

    FrameLayout loginLayout;
    View loginView;

    EditText regEmail;
    EditText regLogin;

    EditText regPass;
    EditText regPassConfirm;

    EditText editTextPass;

    EditText editTextLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //находим экран авторизации и очищаем его
        loginLayout = (FrameLayout) findViewById(R.id.LoginFrameLayout);
        loginLayout.removeAllViews();

        loginView = getLayoutInflater().inflate(R.layout.login_layout, null);
        RelativeLayout loginFormLayout = (RelativeLayout) loginView.findViewById(R.id.LoginFormLayout);
        editTextLogin = (EditText) loginFormLayout.findViewById(R.id.editText);
        editTextPass = (EditText) loginFormLayout.findViewById(R.id.editText2);
        Button buttonLogin = (Button) loginFormLayout.findViewById(R.id.loginBtn);
        Button buttonToRegistration = (Button) loginFormLayout.findViewById(R.id.toRegBtn);

        loginLayout.addView(loginView);

        final View registrationView = getLayoutInflater().inflate(R.layout.reg_layout, null);
        RelativeLayout registrationFormLayout = (RelativeLayout) registrationView.findViewById(R.id.RegistrationFormLayout);
        Button buttonToLogin = (Button) registrationFormLayout.findViewById(R.id.toLoginBtn);
        Button buttonRegistry = (Button) registrationFormLayout.findViewById(R.id.regBtn);
        regEmail = (EditText) registrationFormLayout.findViewById(R.id.editText);
        regLogin = (EditText) registrationFormLayout.findViewById(R.id.editText1);
        regPass = (EditText) registrationFormLayout.findViewById(R.id.editText2);
        regPassConfirm = (EditText) registrationFormLayout.findViewById(R.id.editText3);

        buttonToRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginLayout.removeAllViews();

                loginLayout.addView(registrationView);
            }
        });

        buttonToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginLayout.removeAllViews();

                loginLayout.addView(loginView);
            }
        });

        //вход
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Credentials credentials = new Credentials();
                credentials.Login = editTextLogin.getText().toString();
                credentials.Password = editTextPass.getText().toString();

                Gson gson = new Gson();

                String JSONData = gson.toJson(credentials);

                try {

                    SendLoginRequest sendLoginRequest = new SendLoginRequest();

                    dialog = new Dialog(LoginActivity.this);
                    dialog.setContentView(R.layout.loading_layout);

                    ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
                    imageView.setBackgroundResource(R.drawable.anim_load);

                    AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                    animation.start();

                    dialog.setTitle("Загрузка");
                    dialog.setCancelable(false);
                    dialog.show();

                    sendLoginRequest.execute(loginURL, "credentials", JSONData);
                } catch (Exception ex) {

                }
            }
        });

        //регистрация
        buttonRegistry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (regLogin.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Login cannot be empty", Toast.LENGTH_LONG).show();
                } else if (regPass.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Password cannot be empty", Toast.LENGTH_LONG).show();
                } else if (regEmail.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_LONG).show();
                } else if (regPassConfirm.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Re-enter password", Toast.LENGTH_LONG).show();
                } else if (regLogin.getText().toString().isEmpty() && regPass.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Login and password can not be empty", Toast.LENGTH_LONG).show();
                } else if (containsCyrillic(regEmail.getText().toString()) || containsCyrillic(regLogin.getText().toString()) ||
                        containsCyrillic(regPass.getText().toString()) || containsCyrillic(regPassConfirm.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Login, Email and password should not contain Cyrillic", Toast.LENGTH_LONG).show();
                } else if (!regPass.getText().toString().equals(regPassConfirm.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                } else {
                    try {

                        Gson gson = new Gson();

                        String usersLogin = gson.toJson(regLogin.getText().toString());

                        SendCheckExistenceRequest existenceRequest = new SendCheckExistenceRequest();

                        dialog = new Dialog(LoginActivity.this);
                        dialog.setContentView(R.layout.loading_layout);

                        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
                        imageView.setBackgroundResource(R.drawable.anim_load);

                        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                        animation.start();

                        dialog.setTitle("Loading...");
                        dialog.setCancelable(false);
                        dialog.show();

                        existenceRequest.execute(existenceURL, "login", usersLogin);

                    } catch (Exception ex) {
                        Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean containsCyrillic(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeBlock.of(text.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class SendLoginRequest extends AsyncTask<String, Void, String> {

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

            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {

            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                if (message.toString().equals("ok")) {
                    CurrentUser currentUser = CurrentUser.getInstance();
                    currentUser.Login = editTextLogin.getText().toString();

                    editTextLogin.setText("");

                    editTextPass.setText("");

                    //Toast.makeText(LoginActivity.this, "ok", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {

            }
        }
    }

    private class SendCheckExistenceRequest extends AsyncTask<String, Void, String> {

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

            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {

            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                Type type = new TypeToken<ActionResult>() {
                }.getType();

                ActionResult actionResult = new Gson().fromJson(message, type);

                if (actionResult != null) {
                    switch (actionResult) {
                        case Success:

                            try {

                                FullCredentials credentials = new FullCredentials();
                                credentials.Login = regLogin.getText().toString();
                                credentials.Password = regPass.getText().toString();
                                credentials.Email = regEmail.getText().toString();

                                Gson gson = new Gson();

                                String fCred = gson.toJson(credentials);


                                SendRegistrationRequest registrationRequest = new SendRegistrationRequest();

                                dialog = new Dialog(LoginActivity.this);
                                dialog.setContentView(R.layout.loading_layout);

                                ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
                                imageView.setBackgroundResource(R.drawable.anim_load);

                                AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                                animation.start();

                                dialog.setTitle("Loading...");
                                dialog.setCancelable(false);
                                dialog.show();

                                registrationRequest.execute(registreURL, "fullCredentials", fCred);


                            } catch (Exception ex) {

                            }

                            break;
                        case Failed:

                            Toast.makeText(LoginActivity.this, "Login already exists!", Toast.LENGTH_LONG).show();

                            break;
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

    private class SendRegistrationRequest extends AsyncTask<String, Void, String> {

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

                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
                writer.write(urlParameters);
                writer.close();
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

            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {

            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                Type type = new TypeToken<ActionResult>() {
                }.getType();

                ActionResult actionResult = new Gson().fromJson(message, type);

                if (actionResult != null) {
                    switch (actionResult) {
                        case Success:

                            Gson gson = new Gson();

                            ConfirmingCredentials confirmingCredentials = new ConfirmingCredentials();
                            confirmingCredentials.Login = regLogin.getText().toString();
                            confirmingCredentials.Email = regEmail.getText().toString();

                            String usersLogin = gson.toJson(confirmingCredentials);

                            SendConfirmingLetterRequest letterRequest = new SendConfirmingLetterRequest();

                            dialog = new Dialog(LoginActivity.this);
                            dialog.setContentView(R.layout.loading_layout);

                            ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView2);
                            imageView.setBackgroundResource(R.drawable.anim_load);

                            AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                            animation.start();

                            dialog.setTitle("Loading...");
                            dialog.setCancelable(false);
                            dialog.show();

                            letterRequest.execute(sendLetterURL, "confirmingCredentials", usersLogin);

                            break;
                        case Failed:

                            Toast.makeText(LoginActivity.this, "Error!", Toast.LENGTH_LONG).show();

                            break;
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

    private class SendConfirmingLetterRequest extends AsyncTask<String, Void, String> {

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

            }

            return answer;
        }

        @Override
        protected void onPostExecute(String message) {

            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                Type type = new TypeToken<ActionResult>() {
                }.getType();

                ActionResult actionResult = new Gson().fromJson(message, type);

                if (actionResult != null) {
                    switch (actionResult) {
                        case Success:

                            loginLayout.removeAllViews();

                            loginLayout.addView(loginView);

                            Toast.makeText(LoginActivity.this, "We have sent you a letter. Please check your E-mail. Confirm your account and login in App.", Toast.LENGTH_LONG).show();

                            break;
                        case Failed:

                            Toast.makeText(LoginActivity.this, "Error!", Toast.LENGTH_LONG).show();

                            break;
                    }
                }
            } catch (Exception ex) {

            }
        }
    }
}
