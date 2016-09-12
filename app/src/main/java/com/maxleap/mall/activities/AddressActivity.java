/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maxleap.FindCallback;
import com.maxleap.MLObject;
import com.maxleap.MLQuery;
import com.maxleap.MLQueryManager;
import com.maxleap.mall.R;
import com.maxleap.mall.adapters.AddressAdapter;
import com.maxleap.mall.manage.OperationCallback;
import com.maxleap.mall.manage.UserManager;
import com.maxleap.mall.models.Address;
import com.maxleap.mall.models.User;
import com.maxleap.mall.utils.FFLog;
import com.maxleap.exception.MLException;

import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends BaseActivity {

    public static final int ADD_ADDRESS_REQUEST = 10;
    public static final int UPDATE_ADDRESS_REQUEST = 11;
    public static final String INTENT_ADDRESS_KEY = "intent_address_key";
    public static final String INTENT_CHOOSE_KEY = "intent_choose_key";

    private ListView listView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private ArrayList<Address> mAddresses;
    private AddressAdapter mAddressAdapter;
    private User mUser;

    private boolean isFromPurchase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        init();
    }

    private void init() {
        initToolbar();
        initUI();
        fetchAddressData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView) toolbar.findViewById(R.id.title)).setText(R.string.activity_address_title);
        toolbar.findViewById(R.id.address_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressActivity.this, AddAddressActivity.class);
                startActivityForResult(intent, ADD_ADDRESS_REQUEST);
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initUI() {
        mUser = UserManager.getInstance().getCurrentUser();
        isFromPurchase = getIntent().getBooleanExtra(INTENT_CHOOSE_KEY, false);
        progressBar = (ProgressBar) findViewById(R.id.address_progress_bar);
        listView = (ListView) findViewById(R.id.address_list);
        emptyView = (TextView) findViewById(R.id.address_empty_view);
        mAddresses = new ArrayList<>();
        mAddressAdapter = new AddressAdapter(this, mAddresses);
        listView.setAdapter(mAddressAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isFromPurchase) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(INTENT_ADDRESS_KEY, mAddresses.get(position));
                    setResult(RESULT_OK, new Intent().putExtras(bundle));
                    finish();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (isFromPurchase) {
                    return false;
                }
                new AlertDialog.Builder(AddressActivity.this)
                        .setMessage(getString(R.string.address_del_dialog_message))
                        .setPositiveButton(getString(R.string.address_del_dialog_confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        progressBar.setVisibility(View.VISIBLE);
                                        UserManager.getInstance().deleteAddress(mAddresses.get(position), new OperationCallback() {
                                            @Override
                                            public void success() {
                                                FFLog.toast(AddressActivity.this,
                                                        getString(R.string.toast_address_del_success));
                                                mAddresses.remove(position);
                                                mAddressAdapter.notifyDataSetChanged();
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void failed(String error) {
                                                FFLog.toast(AddressActivity.this,
                                                        getString(R.string.toast_address_del_failed));
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                })
                        .setNegativeButton(R.string.address_del_dialog_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                return true;
            }
        });
    }

    private void fetchAddressData() {
        FFLog.d("start fetchAddressData");
        MLQuery query = new MLQuery("Address");
        MLObject user = MLObject.createWithoutData("_User", mUser.getId());
        query.whereEqualTo("user", user);
        MLQueryManager.findAllInBackground(query, new FindCallback<MLObject>() {
            @Override
            public void done(List<MLObject> list, MLException e) {
                FFLog.d("fetchAddressData list: " + list);
                FFLog.d("fetchAddressData e: " + e);
                if (e == null) {
                    for (MLObject object : list) {
                        mAddresses.add(Address.from(object));
                    }
                    listView.setEmptyView(emptyView);
                    mAddressAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (e.getCode() == MLException.OBJECT_NOT_FOUND) {
                        listView.setEmptyView(emptyView);
                    } else {
                        FFLog.toast(AddressActivity.this, e.getMessage());
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ADD_ADDRESS_REQUEST:
                if (data != null && data.getExtras() != null) {
                    Address address = (Address) data.getExtras().getSerializable(INTENT_ADDRESS_KEY);
                    mAddresses.add(address);
                    mAddressAdapter.notifyDataSetChanged();
                }
                break;
            case UPDATE_ADDRESS_REQUEST:
                if (data != null && data.getExtras() != null) {
                    Address address = (Address) data.getExtras().getSerializable(INTENT_ADDRESS_KEY);
                    for (int i = 0; i < mAddresses.size(); i++) {
                        if (mAddresses.get(i).getId().equals(address.getId())) {
                            mAddresses.remove(i);
                            mAddresses.add(i, address);
                            break;
                        }
                    }
                    mAddressAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

}
