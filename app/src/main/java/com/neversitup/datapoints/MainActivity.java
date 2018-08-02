package com.neversitup.datapoints;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.neversitup.datapoints.dashboard.DashboardActivity;
import com.neversitup.utilities.L;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    private LoginButton loginButton;
    private Button btnLogout;
    private Button btnLogin;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
//            L.d(accessToken.getUserId() + " token : " + accessToken.getToken());
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            initInstance();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initInstance() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        btnLogin = findViewById(R.id.btn_login);
        btnLogout = findViewById(R.id.btn_logout);

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        L.d(response.getRawResponse());
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                L.d("Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                L.d(error.getMessage());
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile",
                        "email", "user_age_range", "user_birthday", "user_friends", "user_gender", "user_hometown", "user_link",
                        "user_location", "user_likes", "user_photos", "user_posts", "user_tagged_places", "user_videos", "read_insights"));
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
            }
        });
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.neversitup.datapoints",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                L.d("KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            L.d("NameNotFoundException : " + e.getLocalizedMessage());
        } catch (NoSuchAlgorithmException e) {
            L.d("NoSuchAlgorithmException : " + e.getLocalizedMessage());
        }
    }
}
