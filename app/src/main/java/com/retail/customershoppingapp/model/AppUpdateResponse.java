package com.retail.customershoppingapp.model;

import com.google.gson.annotations.SerializedName;

public class AppUpdateResponse {

    @SerializedName("appType")
    private String appType;

    @SerializedName("latestVersionCode")
    private int latestVersionCode;

    @SerializedName("versionName")
    private String versionName;

    @SerializedName("apkUrl")
    private String apkUrl;

    @SerializedName("releaseNotes")
    private String releaseNotes;

    @SerializedName("forceUpdate")
    private boolean forceUpdate;

    public String getAppType() { return appType; }
    public int getLatestVersionCode() { return latestVersionCode; }
    public String getVersionName() { return versionName; }
    public String getApkUrl() { return apkUrl; }
    public String getReleaseNotes() { return releaseNotes; }
    public boolean isForceUpdate() { return forceUpdate; }
}