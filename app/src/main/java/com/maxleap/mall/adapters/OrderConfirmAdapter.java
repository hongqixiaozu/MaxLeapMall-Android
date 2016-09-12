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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.models.OrderProduct;
import com.maxleap.mall.models.ProductData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderConfirmAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<?> productData;

    public OrderConfirmAdapter(Context context, ArrayList<?> productData) {
        this.mContext = context;
        this.productData = productData;
    }

    @Override
    public int getCount() {
        return productData.size();
    }

    @Override
    public Object getItem(int position) {
        return productData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_order_product, parent, false);
            holder = new ViewHolder();
            holder.productIcon = (ImageView) convertView.findViewById(R.id.item_order_product_icon);
            holder.productTitle = (TextView) convertView.findViewById(R.id.item_order_product_title);
            holder.productNo = (TextView) convertView.findViewById(R.id.item_order_product_no);
            holder.productPrice = (TextView) convertView.findViewById(R.id.item_order_product_price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String url;
        String title;
        int count;
        int price;
        Object object = this.productData.get(position);
        if (object instanceof ProductData) {
            ProductData productData = (ProductData) object;
            url = productData.getImageUrl();
            String customInfo = TextUtils.isEmpty(productData.getCustomInfo()) ? "" : productData.getCustomInfo();
            title = productData.getTitle() + " " + customInfo;
            count = productData.getCount();
            price = productData.getPrice() * productData.getCount();
        } else if (object instanceof OrderProduct) {
            OrderProduct orderProduct = (OrderProduct) object;
            url = orderProduct.getProduct().getIcons().get(0);
            String customInfo = TextUtils.isEmpty(orderProduct.getCustomInfo()) ? "" : orderProduct.getCustomInfo();
            title = orderProduct.getProduct().getTitle() + " " + customInfo;
            count = orderProduct.getQuantity();
            price = orderProduct.getPrice();
        } else {
            return null;
        }

        Picasso.with(mContext).load(url).placeholder(R.mipmap.def_item).into(holder.productIcon);
        holder.productTitle.setText(title);
        holder.productNo.setText(String.format(mContext.getString(R.string.activity_my_order_product_no)
                , count));
        holder.productPrice.setVisibility(View.VISIBLE);
        holder.productPrice.setText(String.format(mContext.getString(R.string.activity_my_order_product_price)
                , price / 100f));

        return convertView;

    }


    static class ViewHolder {
        ImageView productIcon;
        TextView productTitle;
        TextView productNo;
        TextView productPrice;
    }
}
