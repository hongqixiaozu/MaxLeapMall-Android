/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.maxleap.mall.R;
import com.maxleap.mall.manage.OperationCallback;
import com.maxleap.mall.manage.UserManager;
import com.maxleap.mall.models.Address;
import com.maxleap.mall.utils.FFLog;
import com.maxleap.mall.utils.NoUtilCheck;

public class AddAddressActivity extends BaseActivity {

    private EditText nameView;
    private EditText telView;
    private EditText addressView;
    private Button confirmBtn;
    private RelativeLayout progressRL;

    private Address mAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        init();
    }

    private void init() {
        mAddress = (Address) getIntent().getSerializableExtra(AddressActivity.INTENT_ADDRESS_KEY);
        initToolbar();
        initUI();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mAddress == null) {
            toolbar.setTitle(R.string.activity_add_address_add);
        } else {
            toolbar.setTitle(R.string.activity_add_address_modify);
        }
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initUI() {
        nameView = (EditText) findViewById(R.id.add_address_name);
        telView = (EditText) findViewById(R.id.add_address_tel);
        addressView = (EditText) findViewById(R.id.add_address_street);
        confirmBtn = (Button) findViewById(R.id.add_address_confirm);
        progressRL = (RelativeLayout) findViewById(R.id.add_address_progress_rl);
        if (mAddress != null) {
            nameView.setText(mAddress.getName());
            telView.setText(mAddress.getTel());
            addressView.setText(mAddress.getStreet());
            nameView.setSelection(mAddress.getName().length());
        }
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressRL.setVisibility(View.VISIBLE);
                confirmBtn.setText("");
                confirmBtn.setEnabled(false);
                String name = nameView.getText().toString();
                String tel = telView.getText().toString();
                String street = addressView.getText().toString();
                updateAddress(name, tel, street);
            }
        });
    }

    private void updateAddress(String name, String tel, String street) {
        if (checkError(name, tel, street)) {
            confirmBtn.setText(getString(R.string.activity_add_address_confirm));
            confirmBtn.setEnabled(true);
            progressRL.setVisibility(View.GONE);
            return;
        }
        final Address address = new Address();
        address.setName(name);
        address.setTel(tel);
        address.setStreet(street);
        OperationCallback callback = new OperationCallback() {
            @Override
            public void success() {
                if (mAddress != null) {
                    FFLog.toast(AddAddressActivity.this, R.string.activity_add_address_update_success);
                } else {
                    FFLog.toast(AddAddressActivity.this, R.string.activity_add_address_add_success);
                }
                setResult(RESULT_OK, new Intent().putExtra(AddressActivity.INTENT_ADDRESS_KEY, address));
                finish();
            }

            @Override
            public void failed(String error) {
                FFLog.toast(AddAddressActivity.this, error);
                confirmBtn.setText(R.string.activity_add_address_confirm);
                progressRL.setVisibility(View.GONE);
                confirmBtn.setEnabled(false);
            }
        };
        if (mAddress != null) {
            address.setId(mAddress.getId());
            UserManager.getInstance().updateAddress(address, callback);
        } else {
            UserManager.getInstance().addAddress(address, callback);
        }
    }

    private boolean checkError(String name, String tel, String street) {
        if (TextUtils.isEmpty(name)) {
            FFLog.toast(this, String.format(getString(R.string.activity_update_address_empty),
                    getString(R.string.activity_add_address_name)));
            return true;
        }
        if (TextUtils.isEmpty(tel)) {
            FFLog.toast(this, String.format(getString(R.string.activity_update_address_empty),
                    getString(R.string.activity_add_address_tel)));
            return true;
        }
        if (TextUtils.isEmpty(street)) {
            FFLog.toast(this, String.format(getString(R.string.activity_update_address_empty),
                    getString(R.string.activity_add_address_address)));
            return true;
        }
        if (!NoUtilCheck.isMobileNo(tel)) {
            FFLog.toast(this, R.string.activity_update_address_tel_error);
            return true;
        }

        return false;
    }
}
