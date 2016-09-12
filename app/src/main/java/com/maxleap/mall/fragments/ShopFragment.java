/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.maxleap.MLAnalytics;
import com.maxleap.MLUser;
import com.maxleap.mall.R;
import com.maxleap.mall.activities.LoginActivity;
import com.maxleap.mall.activities.MainActivity;
import com.maxleap.mall.activities.OrderConfirmActivity;
import com.maxleap.mall.activities.ProductDetailActivity;
import com.maxleap.mall.adapters.ShopProductAdapter;
import com.maxleap.mall.manage.UserManager;
import com.maxleap.mall.models.ProductData;
import com.maxleap.mall.utils.CartPreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShopFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Context mContext;
    private ArrayList<ProductData> mProductDatas;
    private ShopProductAdapter mAdapter;
    private TextView mTotalPayView;
    private MainActivity mainActivity;
    private float mTotalPay;
    private Button mPayButton;
    private TextView mEditView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_shop, container, false);
        initUI(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchShopData();
    }

    private void initUI(View view) {
        mContext = getActivity();
        mainActivity = (MainActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mEditView = (TextView) toolbar.findViewById(R.id.edit);
        if (mAdapter != null && mAdapter.isInEditMode()) {
            mEditView.setText(R.string.frag_shop_toolbar_edit_done);
        } else {
            mEditView.setText(R.string.frag_shop_toolbar_edit);
        }
        toolbar.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.isInEditMode()) {
                    mEditView.setText(R.string.frag_shop_toolbar_edit);
                    mAdapter.setInEditMode(false);
                } else {
                    mEditView.setText(R.string.frag_shop_toolbar_edit_done);
                    mAdapter.setInEditMode(true);
                }
            }
        });

        ListView listView = (ListView) view.findViewById(R.id.shop_list);
        View footView = LayoutInflater.from(mContext).inflate(R.layout.view_shop_list_foot, null);
        mTotalPayView = (TextView) footView.findViewById(R.id.total_pay);
        mTotalPayView.setText(String.format(getString(R.string.product_price), mTotalPay));
        mPayButton = (Button) footView.findViewById(R.id.pay_button);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserManager.getInstance().getCurrentUser() == null) {
                    Intent toAccountIntent = new Intent(mContext, LoginActivity.class);
                    startActivity(toAccountIntent);
                } else {
                    StringBuilder productIds = new StringBuilder();
                    StringBuilder productNames = new StringBuilder();
                    for (int i = 0; i < mProductDatas.size(); i++) {
                        productIds.append(mProductDatas.get(i).getId()).append(",");
                        productNames.append(mProductDatas.get(i).getTitle()).append(",");
                    }

                    Map<String, String> dimensions = new HashMap<>();
                    dimensions.put("ProductIds", productIds.toString());
                    dimensions.put("ProductName", productNames.toString());
                    dimensions.put("TotalBuyCount", "" + mProductDatas.size());
                    dimensions.put("Price", "" + mTotalPay);
                    dimensions.put("UserName", MLUser.getCurrentUser().getUserName());

                    MLAnalytics.logEvent("Balance", 1, dimensions);


                    Intent intent = new Intent(mContext, OrderConfirmActivity.class);
                    startActivity(intent);
                }
            }
        });
        view.findViewById(R.id.to_main_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.selectTab(0);
            }
        });
        listView.addFooterView(footView);
        listView.setEmptyView(view.findViewById(R.id.empty));

        if (mProductDatas == null) {
            mProductDatas = new ArrayList<>();
        }

        if (mAdapter == null) {
            mAdapter = new ShopProductAdapter(mContext, mProductDatas, new ShopProductAdapter.CountListener() {
                @Override
                public void onCountChanged() {
                    if (mProductDatas.size() == 0) {
                        mEditView.setVisibility(View.GONE);
                        return;
                    }else{
                        mEditView.setVisibility(View.VISIBLE);
                    }
                    int totalPay = 0;
                    for (ProductData productData : mProductDatas) {
                        totalPay += productData.getPrice() * productData.getCount();
                    }
                    mTotalPay = totalPay / 100f;
                    mTotalPayView.setText(String.format(getString(R.string.product_price), mTotalPay));
                }
            });
        }

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    private void fetchShopData() {
        CartPreferenceUtil sp = CartPreferenceUtil.getComplexPreferences(mContext);
        ArrayList<ProductData> productDatas = (ArrayList<ProductData>) sp.getProductData();
        if (productDatas == null) {
            productDatas = new ArrayList<>();
        }
        mProductDatas.clear();
        mProductDatas.addAll(productDatas);
        int totalPay = 0;
        for (ProductData data : mProductDatas) {
            totalPay += data.getPrice() * data.getCount();
        }
        mTotalPay = totalPay / 100f;
        mTotalPayView.setText(String.format(getString(R.string.product_price), mTotalPay));
        if (mProductDatas.size() == 0) {
            mEditView.setVisibility(View.GONE);
        }else{
            mEditView.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.PRODID, mProductDatas.get(position).getId());
        startActivity(intent);
    }
}
