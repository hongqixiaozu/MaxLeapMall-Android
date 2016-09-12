/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.utils;

import java.util.regex.Pattern;

public class NoUtilCheck {

    private static Pattern mobilePattern;
    /**
     * 移动：134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188
     * 联通：130、131、132、145、155、156、175、176、185、186
     * 电信：133、153、177、180、181、189
     * 全球星：1349
     * 虚拟运营商：170
     *
     * @param mobileNo
     * @return
     */
    public static boolean isMobileNo(String mobileNo) {
        if (mobilePattern == null) {
            mobilePattern = Pattern.compile("^((13[0-9])|(14[5,7])|(15[0-3,5-8])|(17[0,5-8])|(18[0-9])|(147))\\d{8}$");
        }
        return mobilePattern.matcher(mobileNo).matches();
    }

}
