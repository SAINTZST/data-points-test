package com.neversitup.datapoints.dashboard;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.neversitup.datapoints.R;
import com.neversitup.utilities.L;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DashboardActivity extends AppCompatActivity {

    private Button btnFeed;
    private Button btnComment;
    private Button btnLikes;
    private Button btnCallRecord;
    private Button btnSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initInstance();
    }

    private void initInstance() {
        btnFeed = findViewById(R.id.btn_feed);
        btnComment = findViewById(R.id.btn_comment);
        btnLikes = findViewById(R.id.btn_likes);
        btnCallRecord = findViewById(R.id.btn_call_record);
        btnSms = findViewById(R.id.btn_sms);

        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("with", "location");
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/feed",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                L.d(response.getRawResponse());
                            }
                        }
                ).executeAsync();
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putBoolean("summary", true);
                params.putString("filter", "stream");
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/1818670214847561_1816163501764899/comments",
                        params, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                L.d(response.getRawResponse());
                            }
                        }
                ).executeAsync();
            }
        });

        btnLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putBoolean("summary", true);
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/1818670214847561_1816163501764899/likes",
                        params, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                L.d(response.getRawResponse());
                            }
                        }
                ).executeAsync();
            }
        });

        btnCallRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DashboardActivityPermissionsDispatcher.getCallDetailsWithPermissionCheck(DashboardActivity.this);
            }
        });

        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DashboardActivityPermissionsDispatcher.getSmsWithPermissionCheck(DashboardActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DashboardActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_CALL_LOG)
    void getCallDetails() {
        StringBuilder sb = new StringBuilder();
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dirCode = Integer.parseInt(callType);
            switch (dirCode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\nPhone Number:--- ").append(phNumber)
                    .append(" \nCall Type:--- ").append(dir)
                    .append(" \nCall Date:--- ").append(callDayTime)
                    .append(" \nCall duration in sec :--- ").append(callDuration);
            sb.append("\n----------------------------------");
        }
        managedCursor.close();
        L.d(sb.toString());
    }

    @NeedsPermission(Manifest.permission.READ_SMS)
    void getSms() {
        Uri mSmsQueryUri = Uri.parse("content://sms/inbox");
        StringBuilder sb = new StringBuilder();
        try (Cursor cursor = getContentResolver().query(mSmsQueryUri, null, null, null, null)) {
            if (cursor == null) {
                return;
            }
            sb.append("SMS Details :");
            JSONArray smsArray = new JSONArray();
            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String read = cursor.getString(cursor.getColumnIndexOrThrow("read"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                JSONObject sms = new JSONObject();
                sms.put("status", read);
                sms.put("message", body);
                sms.put("timestamp", date); //message date
                smsArray.put(sms);
                sb.append("\nSMS timestamp:--- ").append(date)
                        .append("\nSMS Message:--- ").append(body)
                        .append("\nSMS Status:--- ").append(read)
                        .append("\nSMS Address:--- ").append(address);
                sb.append("\n----------------------------------");
            }
            L.d(sb.toString());
        } catch (Exception e) {
            L.d("Exception: " + e.getMessage());
        }
    }

}
