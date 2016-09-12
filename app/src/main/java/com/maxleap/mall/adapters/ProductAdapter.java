/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.models.Comment;
import com.maxleap.mall.models.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter extends BaseAdapter {
    private ArrayList<Product> mProducts;
    private ArrayList<Comment> mComments;
    private Context mContext;

    public ProductAdapter(Context context, ArrayList<Product> products, ArrayList<Comment> comments) {
        mContext = context;
        mProducts = products;
        mComments = comments;
    }

    @Override
    public int getCount() {
        return mProducts.size();
    }

    @Override
    public Object getItem(int position) {
        return mProducts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_product, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.product_image);
            holder.titleView = (TextView) convertView.findViewById(R.id.product_title);
            holder.priceView = (TextView) convertView.findViewById(R.id.product_price);
            holder.commentCountView = (TextView) convertView.findViewById(R.id.product_comment_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product item = mProducts.get(position);
        if(item.getIcons() != null && !item.getIcons().isEmpty()){
            Picasso.with(mContext).load(item.getIcons().get(0)).placeholder(R.mipmap.def_item).into(holder.imageView);
        }else{
            holder.imageView.setImageResource(R.mipmap.def_item);
        }

        holder.titleView.setText(item.getTitle());
        holder.priceView.setText(String.format(mContext.getString(R.string.product_price), item.getPrice() / 100f));

        int count = 0;
        for (Comment comment : mComments) {
            if (item.getId().equals(comment.getProduct().getId())) {
                count++;
            }
        }
        holder.commentCountView.setText(String.format(mContext.getString(R.string.product_comment_count), count));

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView priceView;
        TextView commentCountView;
    }
}
