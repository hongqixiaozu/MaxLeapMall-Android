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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxleap.mall.R;
import com.maxleap.mall.activities.AccountInfoActivity;
import com.maxleap.mall.activities.LoginActivity;
import com.maxleap.mall.activities.MyOrderActivity;
import com.maxleap.mall.activities.ProductsActivity;
import com.maxleap.mall.manage.UserManager;

public class MineFragment extends Fragment implements View.OnClickListener {

    private final String MAXLEAPMOBILE_WEBSITE = "https://maxleap.cn";
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.fragment_mine_title);

        view.findViewById(R.id.mine_frag_account).setOnClickListener(this);
        view.findViewById(R.id.mine_frag_like).setOnClickListener(this);
        view.findViewById(R.id.mine_frag_order).setOnClickListener(this);
        view.findViewById(R.id.mine_frag_help).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mine_frag_account:
                if (UserManager.getInstance().getCurrentUser() == null) {
                    toLogin();
                } else {
                    Intent toAccountIntent = new Intent(mContext, AccountInfoActivity.class);
                    startActivity(toAccountIntent);
                }
                break;
            case R.id.mine_frag_like:
                if (UserManager.getInstance().getCurrentUser() == null) {
                    toLogin();
                } else {
                    Intent likeIntent = new Intent(mContext, ProductsActivity.class);
                    likeIntent.putExtra(ProductsActivity.INTENT_TITLE, mContext.getString(R.string.fragment_mine_like));
                    startActivity(likeIntent);
                }
                break;
            case R.id.mine_frag_order:
                if (UserManager.getInstance().getCurrentUser() == null) {
                    toLogin();
                } else {
                    Intent orderIntent = new Intent(mContext, MyOrderActivity.class);
                    startActivity(orderIntent);
                }
                break;
            case R.id.mine_frag_help:
                Uri uri = Uri.parse(MAXLEAPMOBILE_WEBSITE);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void toLogin() {
        Intent toAccountIntent = new Intent(mContext, LoginActivity.class);
        startActivity(toAccountIntent);
    }

}
