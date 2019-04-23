package com.example.csci4140;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button forgotpw;

    private SharedPreferences settings;
    private SharedPreferences error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        settings = getSharedPreferences("SETTING",0);
    }

    @Override
    protected void onStart(){
        super.onStart();
        error = getSharedPreferences("ERROR",0);
        String error_msg = error.getString("error", "");
        if(!error_msg.isEmpty()){
            Toast.makeText(this, error_msg, Toast.LENGTH_LONG).show();
            error.edit()
                 .putString("error", "")
                 .apply();
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "error=1;Max-Age=0");
            CookieManager.getInstance().flush();
        }

    }

    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if(isEmailValid(email) && isPasswordValid(password)){
            settings.edit()
                    .putInt("loggedIn", 1)
                    .putString("username", email)
                    .putString("password", password)
                    .putInt("needlogin", 1)
                    .apply();
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "loggedIn=1");
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "username=" + email);
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "password=" + password);
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "needlogin=1");
            CookieManager.getInstance().flush();
            Intent toMain = new Intent(this, MainActivity.class);
            finish();
            startActivity(toMain);
        }else{
            Toast.makeText(this, "You leave UserName/Password blank!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isEmailValid(String email) {
        return !email.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        return !password.isEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

