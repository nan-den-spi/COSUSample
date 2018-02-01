// Copyright 2016 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.cosu;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LockedActivity extends Activity {

    private ImageView ivPhoto;
    private Button btnStopLock;
    private String mCurrentPhoto;
    private DevicePolicyManager mDevicePolicyManager;

    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private static final String PHOTO_PATH = "Photo Path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        btnStopLock = (Button) findViewById(R.id.btnStopLock);
        btnStopLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

                if (activityManager.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED) {
                    // normally only allow admins to end lock task mode
                    stopLockTask();
                }
                startActivity(new Intent(LockedActivity.this, MainActivity.class));
            }
        });

        setImageToView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isLockPermitted = mDevicePolicyManager.isLockTaskPermitted(this.getPackageName());
        if (isLockPermitted) {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save the photo being used in SHaredPreferences

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, 0);
        // Get editor object
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // make preference change to save photo filepath
        editor.putString(PHOTO_PATH, mCurrentPhoto);
        editor.apply();
    }

    private void setImageToView() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, 0);
        String savedPhotoPath = sharedPreferences.getString(PHOTO_PATH, null);

        ivPhoto = (ImageView) findViewById(R.id.ivLock);
        String photoPathFromMain = getIntent().getStringExtra(MainActivity.EXTRA_FILEPATH);

        if (photoPathFromMain != null) {
            mCurrentPhoto = photoPathFromMain;
        } else {
            mCurrentPhoto = savedPhotoPath;
        }

        if (mCurrentPhoto != null) {
            int targetH = ivPhoto.getMaxHeight();
            int targetW = ivPhoto.getMaxWidth();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhoto, bmOptions);
            int photoH = bmOptions.outHeight;
            int photoW = bmOptions.outWidth;

            // Determine how much to scale down image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);


            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhoto,
                    bmOptions);
            ivPhoto.setImageBitmap(imageBitmap);
        }
    }
}
