package com.retail.customershoppingapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.retail.customershoppingapp.BuildConfig;
import com.retail.customershoppingapp.model.AppUpdateResponse;
import com.retail.customershoppingapp.network.RetrofitClient;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateHelper {

    public static void checkForUpdate(Context context, boolean showUpToDateToast) {
        RetrofitClient.getInstance(context).getApi().checkAppUpdate("BUYER")
                .enqueue(new Callback<AppUpdateResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AppUpdateResponse> call, @NonNull Response<AppUpdateResponse> response) {
                        if (context instanceof Activity && ((Activity) context).isFinishing()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            AppUpdateResponse updateInfo = response.body();
                            int currentVersion = BuildConfig.VERSION_CODE;

                            if (updateInfo.getLatestVersionCode() > currentVersion) {
                                showUpdateDialog(context, updateInfo);
                            } else if (showUpToDateToast) {
                                Toast.makeText(context,
                                        "Retail Flow App v" + BuildConfig.VERSION_NAME + " (Up to date ✓)",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (showUpToDateToast) {
                            Toast.makeText(context, "Unable to check for updates", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AppUpdateResponse> call, @NonNull Throwable t) {
                        if (showUpToDateToast) {
                            Toast.makeText(context, "Update check failed: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static void showUpdateDialog(Context context, AppUpdateResponse updateInfo) {
        String notes = (updateInfo.getReleaseNotes() != null && !updateInfo.getReleaseNotes().trim().isEmpty())
                ? updateInfo.getReleaseNotes().trim()
                : "• Performance improvements and bug fixes.";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("New Update Available (v" + updateInfo.getVersionName() + ") 🚀");
        builder.setMessage("What's new in this version:\n\n" + notes);
        builder.setCancelable(!updateInfo.isForceUpdate());

        builder.setPositiveButton("Update Now", (dialog, which) -> {
            downloadAndInstallApk(context, updateInfo.getApkUrl());
        });

        if (!updateInfo.isForceUpdate()) {
            builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());
        }

        builder.show();
    }

    private static void downloadAndInstallApk(Context context, String apkUrl) {
        if (apkUrl == null || !apkUrl.startsWith("http")) {
            Toast.makeText(context, "Invalid APK download link", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Downloading update in background...", Toast.LENGTH_LONG).show();

        String fileName = "FlowMart-Buyer-Update.apk";
        File apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (apkFile.exists()) {
            apkFile.delete();
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("Retail Flow Update");
        request.setDescription("Downloading new version...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) return;
        long downloadId = manager.enqueue(request);

        Context appContext = context.getApplicationContext();
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    installApk(appContext, apkFile);
                    try {
                        appContext.unregisterReceiver(this);
                    } catch (Exception ignored) {
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(onComplete, filter, Context.RECEIVER_EXPORTED);
        } else {
            appContext.registerReceiver(onComplete, filter);
        }
    }

    private static void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) return;

        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                apkFile
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}