/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.models.Specs;

public class SpecAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class SpecViewholder extends RecyclerView.ViewHolder {
        private TextView key;
        private TextView value;

        public SpecViewholder(View view) {
            super(view);
            key = (TextView) view.findViewById(R.id.key);
            value = (TextView) view.findViewById(R.id.value);
        }
    }

    public static class SpecHeadViewholder extends RecyclerView.ViewHolder {
        private TextView head;

        public SpecHeadViewholder(View view) {
            super(view);
            head = (TextView) view.findViewById(R.id.title);
        }
    }

    private static final int HEAD = 0;
    private static final int CONTENT = 1;
    private Specs mSpecs;

    public SpecAdapter(Specs specs) {
        this.mSpecs = specs;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Specs.SpecItem item = mSpecs.getList().get(position);
        if (holder instanceof SpecHeadViewholder) {
            ((SpecHeadViewholder) holder).head.setText(item.getKey());
        } else {
            ((SpecViewholder) holder).key.setText(item.getKey());
            ((SpecViewholder) holder).value.setText(item.getValue());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == HEAD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spec_head,
                    parent, false);
            SpecHeadViewholder viewholder = new SpecHeadViewholder(view);
            view.setTag(viewholder);
            return viewholder;
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inner_spec,
                    parent, false);
            SpecViewholder viewholder = new SpecViewholder(view);
            view.setTag(viewholder);
            return viewholder;
        }
    }

    @Override
    public int getItemCount() {
        return mSpecs.getList().size();
    }

    @Override
    public int getItemViewType(int position) {
        Specs.SpecItem item = mSpecs.getList().get(position);
        if (TextUtils.isEmpty(item.getValue())) {
            return HEAD;
        } else {
            return CONTENT;
        }
    }
}