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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.maxleap.mall.R;
import com.maxleap.mall.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        private RatingBar mRatingBar;
        private TextView mUsername;
        private TextView mContent;
        private TextView mTime;

        public ReviewHolder(View view) {
            super(view);
            mRatingBar = (RatingBar) view.findViewById(R.id.rating);
            mUsername = (TextView) view.findViewById(R.id.username);
            mContent = (TextView) view.findViewById(R.id.content);
            mTime = (TextView) view.findViewById(R.id.review_time);
        }
    }

    private List<Comment> mCommentList;

    public ReviewAdapter(List<Comment> list) {
        this.mCommentList = list;
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent,
                false);
        ReviewHolder holder = new ReviewHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        Comment comment = mCommentList.get(position);
        holder.mRatingBar.setRating(comment.getScore());
        holder.mUsername.setText(comment.getUser().getUsername());
        holder.mContent.setText(comment.getContent());
        Date date = comment.getUpdateAt();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        holder.mTime.setText(simpleDateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }
}