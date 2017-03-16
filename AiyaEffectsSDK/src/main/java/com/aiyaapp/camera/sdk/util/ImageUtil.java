package com.aiyaapp.camera.sdk.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageUtil {

	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
		Matrix matrix = new Matrix();
		matrix.postRotate((float)rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		return rotaBitmap;
	}
}
