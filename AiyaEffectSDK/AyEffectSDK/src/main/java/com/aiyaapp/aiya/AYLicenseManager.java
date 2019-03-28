package com.aiyaapp.aiya;

import android.content.Context;

public class AYLicenseManager {
    public static void initLicense(Context context, String key, AyCore.OnResultCallback callback) {
        AyCore.InitLicense(context, key, callback);
    }
}
