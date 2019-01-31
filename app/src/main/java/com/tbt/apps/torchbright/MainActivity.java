package com.tbt.apps.torchbright;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    static Camera camera;

    ImageButton btnTorch;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btnTorch = (ImageButton) findViewById(R.id.btn_torch_main);
        btnTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processTorch();
            }
        });
        preferences = getSharedPreferences(getString(R.string.key_switch_torch), Context.MODE_PRIVATE);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isTorchOn = preferences.getBoolean(getString(R.string.value_switch_torch), false);
        if (isTorchOn) btnTorch.setImageResource(R.drawable.on);
        else btnTorch.setImageResource(R.drawable.off);
    }

    void processTorch() {
        if (camera != null) btnTorch.setImageResource(R.drawable.off);
        else btnTorch.setImageResource(R.drawable.on);
        Intent intent = new Intent(getString(R.string.action_process_torch));
        sendBroadcast(intent);
    }

    public static class SwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.torch_widget);
            SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.key_switch_torch), Context.MODE_PRIVATE);
            boolean isTorchOn = preferences.getBoolean(context.getString(R.string.value_switch_torch), false);
            if (isTorchOn) {
                turnOff();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                views.setImageViewResource(R.id.btn_torch_widget, R.drawable.off);
            }
            else {
                turnOn();
                showNotification(context);
                showAd(context);
                views.setImageViewResource(R.id.btn_torch_widget, R.drawable.on);
            }
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, TorchWidget.class), views);
            preferences.edit().putBoolean(context.getString(R.string.value_switch_torch), !isTorchOn).apply();
        }
    }

    static void showAd(Context context) {
        final InterstitialAd mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-5499181628523942/6035497111");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    static void showNotification(Context context) {
        Intent intent = new Intent(context.getString(R.string.action_process_torch));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.on)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification))
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    static void turnOn() {
        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    static void turnOff() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

}
