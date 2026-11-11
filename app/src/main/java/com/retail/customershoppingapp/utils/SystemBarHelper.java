package com.retail.customershoppingapp.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.retail.customershoppingapp.R;

public class SystemBarHelper {

    public static void registerGlobalInsets(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                Window window = activity.getWindow();
                View contentView = activity.findViewById(android.R.id.content);
                if (contentView == null) return;

                // 1. ContentView में प्राइमरी डार्क बैकग्राउंड दें ताकि ऊपर स्टेटस बार के पीछे नेवी ब्लू कलर दिखे
                contentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary_dark));

                // 2. ऊपर Status Bar के आइकन्स (Time, Battery, Network) को सफेद (Light) रखें
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
                if (controller != null) {
                    controller.setAppearanceLightStatusBars(false);
                    controller.setAppearanceLightNavigationBars(true);
                }

                // 3. हर फोन के हिसाब से ऊपर (Cutout/Status Bar) और नीचे (Navigation Bar/Keyboard) की सटीक जगह छोड़ें
                ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                    );
                    Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

                    // अगर कीबोर्ड खुला हो तो कीबोर्ड के बराबर, वरना नीचे के नेविगेशन बार के बराबर जगह छोड़ें
                    int bottomInset = Math.max(systemBars.bottom, imeInsets.bottom);

                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset);
                    return WindowInsetsCompat.CONSUMED;
                });
            }

            @Override public void onActivityStarted(@NonNull Activity activity) {}
            @Override public void onActivityResumed(@NonNull Activity activity) {}
            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }
}