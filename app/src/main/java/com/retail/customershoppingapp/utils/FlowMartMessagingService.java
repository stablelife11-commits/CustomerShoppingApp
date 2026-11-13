package com.retail.customershoppingapp.utils;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FlowMartMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        NotificationHelper.sendFcmTokenToServer(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "FlowMart Order Update";
        String body = "आपके ऑर्डर से जुड़ा नया अपडेट आया है।";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (!remoteMessage.getData().isEmpty()) {
            if (remoteMessage.getData().get("title") != null) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().get("message") != null) {
                body = remoteMessage.getData().get("message");
            }
        }

        NotificationHelper.showInstantPushNotification(getApplicationContext(), title, body);
    }
}