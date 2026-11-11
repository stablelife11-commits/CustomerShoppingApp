package com.retail.customershoppingapp;

import android.app.Application;
import com.retail.customershoppingapp.utils.SystemBarHelper;

public class CustomerShoppingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // ऐप की सभी Activities में कटने की समस्या को ग्लोबली ठीक करें
        SystemBarHelper.registerGlobalInsets(this);
    }
}