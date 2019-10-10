package com.aiyaapp.aiya;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AYLicenseManager {
    public static void initLicense(Context context, String key, AyCore.OnResultCallback callback) {
        checkAuthFile(context);
        AyCore.InitLicense(context, key, callback);
    }

    private static void checkAuthFile(Context context) {
        File authFile = new File(context.getFilesDir(),"auth.json");
        if (authFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(authFile));
                StringBuilder sb = new StringBuilder();
                String temp;
                while((temp = reader.readLine()) != null) {
                    sb.append(temp);
                }
                String authContent = sb.toString();
                JSONObject jsonObject = new JSONObject(authContent);
                boolean ok = jsonObject.getJSONObject("license").getJSONObject("ret").getBoolean("ok");

                // 如果验证失败直接删除
                if (!ok) {
                    boolean result = authFile.delete();

                } else {
                    long timestamp = jsonObject.getJSONObject("license").getLong("timestamp");
                    long interval = jsonObject.getJSONObject("license").getJSONObject("ret").getJSONObject("data").getLong("interval");
                    int jurisdiction = jsonObject.getJSONObject("license").getJSONObject("ret").getJSONObject("data").getInt("jurisdiction");

                    // 如果超时直接删除
                    if (System.currentTimeMillis() / 1000 > timestamp + interval) {
                        boolean result = authFile.delete();
                    }

                    // 如果SDK没有权限直接删除
                    if (jurisdiction != 255) {
                        boolean result = authFile.delete();
                    }
                }

            } catch (Exception e) {
                // 如果文件错误直接删除
                e.printStackTrace();
                boolean result = authFile.delete();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
