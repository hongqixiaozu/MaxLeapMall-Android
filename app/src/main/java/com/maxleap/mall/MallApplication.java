/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall;

import android.app.Application;

import com.maxleap.MaxLeap;

public class MallApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MaxLeap.initialize(this);
        MaxLeap.setLogLevel(MaxLeap.LOG_LEVEL_ERROR);
    }
}
