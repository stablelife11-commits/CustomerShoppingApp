package com.retail.customershoppingapp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.retail.customershoppingapp.MainActivity;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.model.BuyerNotificationDto;
import com.retail.customershoppingapp.network.RetrofitClient;
import com.retail.customershoppingapp.session.SessionManager;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {

    public static final String CHANNEL_ID = "flowmart_order_updates";

    // 1. FCM टोकन निकालकर Spring Boot सर्वर पर सेव करें (0% बैटरी खर्च)
    public static void syncFcmToken(Context context) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();
        createChannelIfNeeded(appContext);

        SessionManager sessionManager = new SessionManager(appContext);
        if (!sessionManager.isLoggedIn()) return;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return;
            }
            sendFcmTokenToServer(appContext, task.getResult());
        });
    }

    public static void sendFcmTokenToServer(Context context, String token) {
        if (context == null || token == null || token.trim().isEmpty()) return;
        Context appContext = context.getApplicationContext();
        SessionManager sessionManager = new SessionManager(appContext);
        if (!sessionManager.isLoggedIn()) return;

        RetrofitClient.getInstance(appContext)
                .getApi()
                .updateFcmToken(Collections.singletonMap("fcmToken", token.trim()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                });
    }

    // 2. FCM से मैसेज आते ही फोन के ऊपर सिस्टम नोटिफिकेशन दिखाएं
    public static void showInstantPushNotification(Context context, String title, String message) {
        createChannelIfNeeded(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_tab", "orders");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int notifId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(notifId, builder.build());
    }

    // 3. HomeFragment से कॉल होने पर: बिना घंटी दबाए सिर्फ FCM टोकन सिंक करें, और घंटी दबाने पर डायलॉग खोलें
    public static void checkAndShowNotifications(Context context, boolean openDialogOnBellClick) {
        syncFcmToken(context);

        if (!openDialogOnBellClick) {
            return;
        }

        RetrofitClient.getInstance(context).getApi().getMyNotifications()
                .enqueue(new Callback<List<BuyerNotificationDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<BuyerNotificationDto>> call,
                                           @NonNull Response<List<BuyerNotificationDto>> response) {
                        if (context instanceof Activity && ((Activity) context).isFinishing()) {
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(context, "कोई नया नोटिफिकेशन नहीं है", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showNotificationsDialog(context, response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<BuyerNotificationDto>> call, @NonNull Throwable t) {
                        if (context instanceof Activity && !((Activity) context).isFinishing()) {
                            Toast.makeText(context, "नोटिफिकेशन लोड करने में त्रुटि", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static void showNotificationsDialog(Context context, List<BuyerNotificationDto> list) {
        if (list.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setTitle("🔔 ऑर्डर अपडेट्स (Notifications)")
                    .setMessage("अभी कोई नोटिफिकेशन नहीं है। जब सेलर आपका ऑर्डर प्रोसेस करेगा, तो उसका अपडेट यहाँ दिखेगा।")
                    .setPositiveButton("ठीक है", null)
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (BuyerNotificationDto n : list) {
            String badge = n.isRead() ? "• " : "🟢 [NEW] ";
            String time = n.getCreatedAt() != null ? n.getCreatedAt().replace("T", " ") : "";
            if (time.length() > 16) time = time.substring(0, 16);

            sb.append(badge).append(n.getTitle()).append("\n")
                    .append(n.getMessage()).append("\n")
                    .append("🕒 ").append(time).append("\n\n─────────────────\n\n");
        }

        new AlertDialog.Builder(context)
                .setTitle("🔔 आपके ऑर्डर अपडेट्स")
                .setMessage(sb.toString().trim())
                .setPositiveButton("सब पढ़ लिया (Mark Read)", (d, w) -> {
                    RetrofitClient.getInstance(context).getApi().markAllNotificationsRead()
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}
                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                            });
                })
                .setNegativeButton("बंद करें", null)
                .show();
    }

    public static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wholesale Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Order status and admin push notifications");
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}