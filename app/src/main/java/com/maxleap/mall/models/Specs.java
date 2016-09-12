/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Specs {

    private List<SpecItem> list;


    public Specs(String json) throws JSONException {
        list = new ArrayList<>();

        JSONObject jsonObject=new JSONObject(json);
        Iterator<String> it=jsonObject.keys();
        while (it.hasNext()) {
            String key = it.next();
            String value = jsonObject.optString(key);
            SpecItem item = new SpecItem(key, value);
            list.add(item);
        }

//        JSONArray jsonArray = new JSONArray(json);
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject jsonObject = jsonArray.optJSONObject(i);
//
//            String key = jsonObject.optString("key");
//            String value = jsonObject.optString("value");
//
//            Iterator<String> it=jsonObject.keys();
//            if(it.hasNext()) {
//                key = jsonObject.keys().next();
//                value = jsonObject.optString(key);
//            }
//
//
//            SpecItem item = new SpecItem(key, value);
//            list.add(item);
//        }

    }

    public List<SpecItem> getList() {
        return list;
    }

    public void setList(List<SpecItem> list) {
        this.list = list;
    }


    public class SpecItem {
        private String key;
        private String value;

        public SpecItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "SpecItem{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}