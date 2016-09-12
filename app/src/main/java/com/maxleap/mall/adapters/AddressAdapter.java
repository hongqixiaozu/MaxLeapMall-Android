/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.activities.AddAddressActivity;
import com.maxleap.mall.activities.AddressActivity;
import com.maxleap.mall.models.Address;

import java.util.List;

public class AddressAdapter extends BaseAdapter {

    private Context mContext;
    private List<Address> addresses;

    public AddressAdapter(Context mContext, List<Address> addresses) {
        this.mContext = mContext;
        this.addresses = addresses;
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public Object getItem(int position) {
        return addresses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_address, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.address_name);
            holder.tel = (TextView) convertView.findViewById(R.id.address_tel);
            holder.address = (TextView) convertView.findViewById(R.id.address_street);
            holder.editView = (ImageView) convertView.findViewById(R.id.address_edit);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Address address = addresses.get(position);

        holder.name.setText(address.getName());
        holder.tel.setText(address.getTel());
        holder.address.setText(address.getStreet());
        holder.editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddAddressActivity.class);
                intent.putExtra(AddressActivity.INTENT_ADDRESS_KEY, addresses.get(position));
                ((Activity) mContext).startActivityForResult(intent,
                        AddressActivity.UPDATE_ADDRESS_REQUEST);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView tel;
        TextView address;
        ImageView editView;
    }
}
