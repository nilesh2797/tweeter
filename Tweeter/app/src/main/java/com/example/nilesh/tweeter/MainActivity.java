package com.example.nilesh.tweeter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private connectTask mAuthTask = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        JSONObject jsonParams = null;
        mAuthTask = new connectTask(jsonParams, "SeePosts"){
            @Override
            protected void onPostExecute(final Boolean success) {
                System.out.println(result);
                if (success && result != "") {
                    TextView mTextView = (TextView) findViewById(R.id.sample_text);
                    mTextView.setText(result);
                    mTextView.setMovementMethod(new ScrollingMovementMethod());
                } else {
                    Toast.makeText(MainActivity.this, "Unable to Load Data", Toast.LENGTH_SHORT).show();
                }
            }

        };
        mAuthTask.execute((Void) null);
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

    public boolean check(JSONObject result)
    {
        if(result == null)
            return false;
        String status;
        try {
            status = result.getString("status");
            System.out.println(status);
            return !(status.equals("false") || status.equals(""));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public class connectTask extends AsyncTask<Void, Void, Boolean> {

        private JSONObject jsonParam, userdata = null;
        String result = "", servlet = "";
        connectTask(JSONObject j, String s) {
            servlet = s;
            jsonParam = j;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String http = "http://192.168.1.124:8080/Backend-tweeter/"+servlet;

            try {
                // Simulate network access.
                HttpURLConnection urlConnection = null;
                Log.e("message", "attempting to connect");
                try {
//                    Toast.makeText(LoginActivity.this, "Attempting to connect", Toast.LENGTH_SHORT).show();
                    URL url = new URL(http);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoInput(true);

                    DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                    try {
                        String toPost = getPostDataString(jsonParam);
                        Log.e("toPost", toPost);
                        printout.writeBytes(toPost);
                        System.out.println(toPost);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    printout.flush();
                    printout.close();

                    int HttpResult = urlConnection.getResponseCode();
                    if (HttpResult == HttpURLConnection.HTTP_OK) {
                        System.out.println("Connected");
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream(), "utf-8"));
                        String line = null;
                        String sb = "";
                        while ((line = br.readLine()) != null) {
                            sb += line;
                        }
                        br.close();
                        result = sb.toString();
                        userdata = new JSONObject(result);
                        System.out.println("userdata = " + userdata);


                    } else {
                        System.out.println(urlConnection.getResponseMessage());
                    }
                } catch (IOException e) {
                    System.out.println(result);
                    e.printStackTrace();
                    return false;
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            System.out.println("userdata = " + userdata);
            return check(userdata);
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        if(params == null)
            return "";
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            // Handle the camera action
        } else if (id == R.id.nav_add) {

        } else if (id == R.id.nav_view) {

        } else if (id == R.id.nav_logout) {
            JSONObject jsonParams = null;
            mAuthTask = new connectTask(jsonParams, "Logout"){
                @Override
                protected void onPostExecute(final Boolean success) {

                    if (success) {
                        Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Unable to Logout", Toast.LENGTH_SHORT).show();
                    }
                }

            };
            mAuthTask.execute((Void) null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
