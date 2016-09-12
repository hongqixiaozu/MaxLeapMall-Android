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
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.models.ProductData;
import com.maxleap.mall.utils.CartPreferenceUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopProductAdapter extends BaseAdapter implements View.OnClickListener {
    private ArrayList<ProductData> mProductDatas;
    private Context mContext;
    private boolean inEditMode;
    private CountListener mListener;

    public ShopProductAdapter(Context context, ArrayList<ProductData> productDatas, CountListener listener) {
        mContext = context;
        mProductDatas = productDatas;
        inEditMode = false;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mProductDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mProductDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shop_product, parent, false);
            holder = new ViewHolder();
            holder.deleteBtn = (ImageButton) convertView.findViewById(R.id.delete_btn);
            holder.imageView = (ImageView) convertView.findViewById(R.id.product_image);
            holder.titleView = (TextView) convertView.findViewById(R.id.product_title);
            holder.priceView = (TextView) convertView.findViewById(R.id.product_price);
            holder.minusBtn = (ImageButton) convertView.findViewById(R.id.minus_btn);
            holder.countView = (TextView) convertView.findViewById(R.id.product_count);
            holder.addBtn = (ImageButton) convertView.findViewById(R.id.add_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isInEditMode()) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        ProductData productData = mProductDatas.get(position);
        Picasso.with(mContext).load(productData.getImageUrl()).placeholder(R.mipmap.def_item).into(holder.imageView);
        holder.titleView.setText(productData.getTitle()+" "+productData.getCustomInfo());
        holder.priceView.setText(String.format(mContext.getString(R.string.product_price), productData.getPrice() / 100f));
        holder.countView.setText(String.valueOf(productData.getCount()));

        holder.deleteBtn.setTag(position);
        holder.minusBtn.setTag(position);
        holder.addBtn.setTag(position);
        holder.deleteBtn.setOnClickListener(this);
        holder.minusBtn.setOnClickListener(this);
        holder.addBtn.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        final int position = (int) v.getTag();

        switch (v.getId()) {
            case R.id.delete_btn:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
                builder.setMessage(R.string.dialog_shop_delete_confirm_notice);
                builder.setPositiveButton(R.string.dialog_shop_delete_confirm_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CartPreferenceUtil.getComplexPreferences(mContext).delete(mProductDatas.get(position));
                        mProductDatas.remove(position);
                        if (mListener != null) {
                            mListener.onCountChanged();
                        }
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.dialog_shop_delete_confirm_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            case R.id.minus_btn:
                ProductData item = mProductDatas.get(position);
                int count = item.getCount();
                count--;
                if (count < 1) count = 1;
                item.setCount(count);
                CartPreferenceUtil.getComplexPreferences(mContext).update(mProductDatas.get(position));
                if (mListener != null) {
                    mListener.onCountChanged();
                }
                notifyDataSetChanged();
                break;
            case R.id.add_btn:
                ProductData item2 = mProductDatas.get(position);
                int count2 = item2.getCount();
                count2++;
                item2.setCount(count2);
                CartPreferenceUtil.getComplexPreferences(mContext).update(mProductDatas.get(position));
                if (mListener != null) {
                    mListener.onCountChanged();
                }
                notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    static class ViewHolder {
        ImageButton deleteBtn;
        ImageView imageView;
        TextView titleView;
        TextView priceView;
        ImageButton minusBtn;
        TextView countView;
        ImageButton addBtn;
    }

    public boolean isInEditMode() {
        return inEditMode;
    }

    public void setInEditMode(boolean inEditMode) {
        this.inEditMode = inEditMode;
        notifyDataSetChanged();
    }

    public interface CountListener {
        void onCountChanged();
    }
}
