package com.retail.customershoppingapp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.retail.customershoppingapp.MainActivity;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.model.BuyerNotificationDto;
import com.retail.customershoppingapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {

    private static final String CHANNEL_ID = "flowmart_order_updates";
    private static final String PREF_NOTIF = "FlowMartNotifPrefs";
    private static final String KEY_LAST_NOTIF_ID = "last_seen_notif_id";

    public static void checkAndShowNotifications(Context context, boolean openDialogOnBellClick) {
        RetrofitClient.getInstance(context).getApi().getMyNotifications()
                .enqueue(new Callback<List<BuyerNotificationDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<BuyerNotificationDto>> call,
                                           @NonNull Response<List<BuyerNotificationDto>> response) {
                        if (context instanceof Activity && ((Activity) context).isFinishing()) {
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            if (openDialogOnBellClick) {
                                Toast.makeText(context, "कोई नया नोटिफिकेशन नहीं है", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        List<BuyerNotificationDto> list = response.body();
                        SharedPreferences prefs = context.getSharedPreferences(PREF_NOTIF, Context.MODE_PRIVATE);
                        long lastSeenId = prefs.getLong(KEY_LAST_NOTIF_ID, 0L);
                        long maxId = lastSeenId;

                        for (BuyerNotificationDto n : list) {
                            if (!n.isRead() && n.getId() != null && n.getId() > lastSeenId) {
                                showSystemTrayNotification(context, n);
                                if (n.getId() > maxId) {
                                    maxId = n.getId();
                                }
                            }
                        }

                        if (maxId > lastSeenId) {
                            prefs.edit().putLong(KEY_LAST_NOTIF_ID, maxId).apply();
                        }

                        if (openDialogOnBellClick) {
                            showNotificationsDialog(context, list);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<BuyerNotificationDto>> call, @NonNull Throwable t) {
                        if (openDialogOnBellClick) {
                            Toast.makeText(context, "नोटिफिकेशन लोड करने में त्रुटि", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static void showSystemTrayNotification(Context context, BuyerNotificationDto notif) {
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

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notif.getId().intValue(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(notif.getTitle())
                .setContentText(notif.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notif.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(notif.getId().intValue(), builder.build());
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

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wholesale Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Order status and admin notifications");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}