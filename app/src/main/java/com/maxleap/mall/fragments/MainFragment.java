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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.maxleap.FindCallback;
import com.maxleap.MLObject;
import com.maxleap.MLQuery;
import com.maxleap.MLQueryManager;
import com.maxleap.mall.R;
import com.maxleap.mall.activities.ProductDetailActivity;
import com.maxleap.mall.activities.ProductsActivity;
import com.maxleap.mall.activities.SearchActivity;
import com.maxleap.mall.adapters.BannerAdapter;
import com.maxleap.mall.adapters.ProductAdapter;
import com.maxleap.mall.adapters.ProductTypeAdapter;
import com.maxleap.mall.models.Banner;
import com.maxleap.mall.models.Comment;
import com.maxleap.mall.models.Product;
import com.maxleap.mall.models.ProductType;
import com.maxleap.mall.utils.FFLog;
import com.maxleap.mall.widget.Indicator;
import com.maxleap.exception.MLException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler mHandler;
    private Context mContext;
    private ArrayList<Banner> mBanners;
    private BannerAdapter mBannerAdapter;
    private ViewPager viewPager;
    private ArrayList<ProductType> mProductTypes;
    private ProductTypeAdapter mProductTypeAdapter;
    private ArrayList<Product> mProducts;
    private ArrayList<Comment> mComments;
    private ProductAdapter mProductAdapter;

    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mHandler = new Handler();
        View view = inflater.inflate(R.layout.frag_main, container, false);
        initToolBar(view);
        initListView(view);
        return view;
    }

    private void initToolBar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.findViewById(R.id.toolbar_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SearchActivity.class));
            }
        });
    }

    private void initListView(View view) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        ListView listview = (ListView) view.findViewById(R.id.frag_main_list_view);
        listview.addHeaderView(getHeadView());
        listview.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.view_footer, null));

        if (mProducts == null) {
            mProducts = new ArrayList<>();
            mComments = new ArrayList<>();
            mHandler.postDelayed(mProgressRunnable, 100);
        }
        if (mProducts.isEmpty()) {
            fetchProductData();
        }
        mProductAdapter = new ProductAdapter(mContext, mProducts, mComments);
        listview.setAdapter(mProductAdapter);
        listview.setOnItemClickListener(this);
    }

    private View getHeadView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.view_head_main, null);
        initBanner(view);
        initProductType(view);
        return view;
    }

    private void initProductType(View view) {
        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        if (mProductTypes == null) {
            mProductTypes = new ArrayList<>();
        }
        if (mProductTypes.isEmpty()) {
            fetchProductTypeData();
        }
        mProductTypeAdapter = new ProductTypeAdapter(mContext, mProductTypes);
        gridView.setAdapter(mProductTypeAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, ProductsActivity.class);
                intent.putExtra(ProductsActivity.INTENT_TITLE, mProductTypes.get(position).getTitle());
                intent.putExtra(ProductsActivity.INTENT_TYPE_ID, mProductTypes.get(position).getId());
                startActivity(intent);
            }
        });
    }

    private void initBanner(final View view) {
        RelativeLayout bannerLayout = (RelativeLayout) view.findViewById(R.id.banner_layout);
        bannerLayout.setVisibility(View.GONE);
        if (mBanners == null) {
            mBanners = new ArrayList<Banner>();
        }
        if (mBanners.isEmpty()) {
            fetchBannerData(bannerLayout);
        } else {
            bannerLayout.setVisibility(View.VISIBLE);
        }
        viewPager = (ViewPager) view.findViewById(R.id.head_view_pager);
        mBannerAdapter = new BannerAdapter(mContext, mBanners);
        viewPager.setAdapter(mBannerAdapter);
        final Indicator indicatorLayout = (Indicator) view.findViewById(R.id.head_indicator_layout);
        indicatorLayout.setCount(mBanners.size());
        indicatorLayout.select(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicatorLayout.select(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mSwipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
        final GestureDetector tapGestureDetector = new GestureDetector(getActivity(), new TapGestureListener());
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                tapGestureDetector.onTouchEvent(event);
                return false;
            }
        });

    }

    private void fetchProductData() {
        FFLog.d("start fetchProductData");
        MLQuery query = new MLQuery("Product");
        MLQueryManager.findAllInBackground(query, new FindCallback<MLObject>() {
            @Override
            public void done(final List<MLObject> list, MLException e) {
                FFLog.d("fetchProductData list: " + list);
                FFLog.d("fetchProductData e: " + e);
                if (e == null) {
                    Set<MLObject> ids = new HashSet<MLObject>();
                    final ArrayList<Product> products = new ArrayList<Product>();
                    for (MLObject object : list) {
                        Product product = new Product(object);
                        products.add(product);
                        ids.add(MLObject.createWithoutData("Product", product.getId()));
                    }
                    if (products.size() > 0) {
                        mProducts.clear();
                        mProducts.addAll(products);

                        MLQuery<MLObject> commentQuery = MLQuery.getQuery("Comment");
                        commentQuery.whereContainedIn("product", ids);
                        commentQuery.include("product");
                        commentQuery.include("user");

                        MLQueryManager.findAllInBackground(commentQuery, new FindCallback<MLObject>() {
                            @Override
                            public void done(List<MLObject> list, MLException e) {
                                mHandler.removeCallbacks(mProgressRunnable);
                                mSwipeRefreshLayout.setRefreshing(false);
                                if (e == null) {
                                    mComments.clear();
                                    for (MLObject object : list) {
                                        mComments.add(new Comment(object));
                                    }
                                }
                                mProductAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } else {
                    mHandler.removeCallbacks(mProgressRunnable);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

        });
    }

    private void fetchProductTypeData() {
        FFLog.d("start fetchProductTypeData");
        MLQuery query = new MLQuery("ProductType");
        MLQueryManager.findAllInBackground(query, new FindCallback<MLObject>() {
            @Override
            public void done(List<MLObject> list, MLException e) {
                FFLog.d("fetchProductTypeData list: " + list);
                FFLog.d("fetchProductTypeData e: " + e);
                if (e == null) {
                    ArrayList<ProductType> types = new ArrayList<ProductType>();
                    for (MLObject object : list) {
                        if (object.getBoolean("recommend")) {
                            types.add(new ProductType(object));
                        }
                    }
                    FFLog.d("fetchProductTypeData types: " + types);
                    if (types.size() > 0) {
                        mProductTypes.clear();
                        mProductTypes.addAll(types);
                        mProductTypeAdapter.notifyDataSetChanged();
                    }
                }
            }

        });
    }

    private void fetchBannerData(final RelativeLayout bannerLayout) {
        FFLog.d("start fetchBannerData");
        MLQuery query = new MLQuery("Banner");
        MLQueryManager.findAllInBackground(query, new FindCallback<MLObject>() {
            @Override
            public void done(List<MLObject> list, MLException e) {
                FFLog.d("fetchBannerData list: " + list);
                FFLog.d("fetchBannerData e: " + e);
                if (e == null) {
                    ArrayList<Banner> banners = new ArrayList<Banner>();
                    for (MLObject object : list) {
                        Banner banner = new Banner(object);
                        if (banner.getStatus() == 1) {
                            banners.add(banner);
                        }
                    }
                    FFLog.d("fetchBannerData banners: " + banners);
                    if (banners.size() > 0) {
                        mBanners.clear();
                        mBanners.addAll(banners);
                        mBannerAdapter.notifyDataSetChanged();
                        bannerLayout.setVisibility(View.VISIBLE);
                        final Indicator indicatorLayout = (Indicator) bannerLayout.findViewById(R.id.head_indicator_layout);
                        indicatorLayout.setCount(mBanners.size());
                        indicatorLayout.select(0);
                    }
                }
            }

        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, ProductDetailActivity.class);
        FFLog.d("onItemClick position : " + position);
        position = position - 1;
        intent.putExtra(ProductDetailActivity.PRODID, mProducts.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        fetchProductData();
    }

    class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            FFLog.d("viewPager onSingleTapConfirmed");
            Intent intent = new Intent(mContext, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.PRODID, mBanners.get(viewPager.getCurrentItem()).getProduct().getId());
            startActivity(intent);
            return true;
        }
    }
}
