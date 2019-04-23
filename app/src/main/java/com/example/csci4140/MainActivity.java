package com.example.csci4140;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity implements AdvancedWebView.Listener{
    //WebView webView;
    AdvancedWebView webView;
    SharedPreferences error;
    SharedPreferences settings;
    Map<String, String> cookies;

    private Map<String, String> getcookie(){
        String cookie = CookieManager.getInstance().getCookie("http://10.0.2.2:8080/");
        Map<String, String> parsed = new HashMap<String, String>();
        if (cookie == null){
            //Log.e("MAIN::getcookie", "nothing");
        }else{
            //Log.e("MAIN::getcookie", cookie);
            String strArray[] = cookie.split(";");
            for(String item : strArray){
                String temp[] = item.split("=");
                //Log.e("MAIN::getcookie", "parsed 0 =" + temp[0].trim() + ", 1=" + temp[1].trim());
                parsed.put(temp[0].trim(), temp[1].trim());
            }
        }
        return parsed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        webView = (AdvancedWebView) findViewById(R.id.mainWeb);
        webView.setListener(this, this);

        cookies = getcookie();
        if(!cookies.containsKey("loggedIn")){
            Intent toLogin = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(toLogin);
        }
        if(cookies.containsKey("url")){
            webView.loadUrl(cookies.get("url"));
        }else{
            webView.loadUrl("http://10.0.2.2:8080/");
        }
        //settings.edit().putString("url", "http://10.0.2.2:8080/").apply();
        CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "url=http://10.0.2.2:8080/");
        CookieManager.getInstance().flush();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_menu) {
            String javaScript = "(function(){" +
                    "var b = document.querySelector('.top-bar-left .top-btn-text');" +
                    "b.click();" +
                    "setTimeout(function(){" +
                    "var d = document.querySelector('li.pop-btn:nth-child(6)');" +
                    "d.style.display = 'none';" +
                    "}, 200);" +
                    "})()";
            webView.evaluateJavascript(javaScript, null);
            return true;
        }else if (id == R.id.menu_upload){
            String javaScript = "(function(){" +
                    "var b = document.querySelector('.top-bar-left .top-btn-text');" +
                    "b.click();" +
                    "setTimeout(function(){" +
                    "var d = document.querySelector('li.pop-btn:nth-child(6)');" +
                    "d.click();" +
                    "}, 500);" +
                    "})()";
            webView.evaluateJavascript(javaScript, null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        webView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        webView.onActivityResult(requestCode, resultCode, intent);
        Log.e("MAINwebView", "onActivityResult");
        Log.e("MAINwebView", "requestCode: " + requestCode + " resultCode: " + resultCode + " intent: " + intent.toString());
    }

    @Override
    public void onBackPressed() {
        if (!webView.onBackPressed()) { return; }
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) { }

    @Override
    public void onPageFinished(String url) {
        cookies = getcookie();
        int needlogin = 0;
        if(cookies.containsKey("needlogin")){
            needlogin = Integer.parseInt(cookies.get("needlogin"));
        }
        //Log.e("MAIN::onPageFinished", cookies.toString());

        if (url.contains("http://10.0.2.2:8080/login")) {
            Log.e("MAINwebView", "failed login");
            // Fail login
            error = getSharedPreferences("ERROR",0);
            error.edit().putString("error", "INCORRECT username/Password!").apply();
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "error=INCORRECT username/Password!");
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "loggedIn=0");
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "needlogin=1");
            CookieManager.getInstance().flush();
            Intent toLogin = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(toLogin);
        }else if (url.contains("http://10.0.2.2:8080/logout")) {
            Log.e("MAINwebView", "trying logout");
            error = getSharedPreferences("ERROR",0);
            error.edit().clear().apply();
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    Log.e("MAINWEBVIEW", "logout");
                }
            });
            Intent toLogin = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(toLogin);
        }else if (needlogin == 1) {
            Log.e("MAINwebView", "try login");
            String javaScript;
            CookieManager.getInstance().setCookie("http://10.0.2.2:8080/", "needlogin=0");
            CookieManager.getInstance().flush();
            settings = getSharedPreferences("SETTING",0);
            String username = settings.getString("username", "");
            String password = settings.getString("password", "");
            javaScript = "(function(){var login = document.querySelector('#top-bar-signin'); login.click(); " +
                    "setTimeout(function(){" +
                    "var a = document.querySelectorAll('.input input'); a[0].value = '" + username + "';" +
                    "a[1].value = '" + password + "'; " +
                    "var submit = document.querySelector('.input button'); submit.click();" +
                    "setTimeout(function(){" +
                    "location.reload(true);" +
                    "}, 1000);" +
                    "}, 100);})()";
            webView.evaluateJavascript(javaScript, null);
        }else{
            Log.e("MAINwebView", "normal usage");
            String javaScript;
            javaScript = "(function(){" +
                    "setTimeout(function(){" +
                    "var b = document.querySelector('.top-bar-left .top-btn-text');" +
                    "b.style.display = 'none';" +
                    "var c = document.querySelector('.top-bar-right');" +
                    "c.style.display = 'none';" +
                    "}, 100);})()";
            webView.evaluateJavascript(javaScript, null);
        }
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        Log.e("MAIN::error", "errorcode: " + errorCode + " description: " + description + " failure: " + failingUrl);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }

}
