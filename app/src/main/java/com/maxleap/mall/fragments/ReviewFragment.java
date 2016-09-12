/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.adapters.ReviewAdapter;
import com.maxleap.mall.models.Comment;
import com.maxleap.mall.widget.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ReviewFragment extends Fragment {

    public static final String DATALIST = "list";

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private ReviewAdapter mReviewAdapter;
    private List<Comment> mCommentList;

    public static ReviewFragment newInstance(ArrayList<Comment> list) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DATALIST, list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_review, container, false);
        initViews(view);
        mCommentList = (List<Comment>) getArguments().getSerializable(DATALIST);
        if (mCommentList == null || mCommentList.size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mReviewAdapter = new ReviewAdapter(mCommentList);
            mRecyclerView.setAdapter(mReviewAdapter);
        }
        return view;
    }

    private void initViews(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.review_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(R.color.bg_main)
                        .size(1)
                        .marginResId(R.dimen.item_margin,
                                R.dimen.item_margin).build()
        );

        mEmptyView = (TextView) view.findViewById(R.id.empty);
        mEmptyView.setVisibility(View.GONE);
    }
}