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
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.maxleap.FindCallback;
import com.maxleap.GetCallback;
import com.maxleap.MLAnalytics;
import com.maxleap.MLObject;
import com.maxleap.MLQuery;
import com.maxleap.MLQueryManager;
import com.maxleap.MLRelation;
import com.maxleap.MLUser;
import com.maxleap.mall.R;
import com.maxleap.mall.adapters.ProductGalleryAdapter;
import com.maxleap.mall.databinding.ActivityProductDetailBinding;
import com.maxleap.mall.manage.OperationCallback;
import com.maxleap.mall.manage.UserManager;
import com.maxleap.mall.models.Product;
import com.maxleap.mall.models.ProductData;
import com.maxleap.mall.utils.CartPreferenceUtil;
import com.maxleap.mall.utils.FFLog;
import com.maxleap.exception.MLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends BaseActivity implements View.OnClickListener {

    public final static String PRODID = "id";

    private ActivityProductDetailBinding mBinding;
    private ProductGalleryAdapter mAdapter;
    private List<String> mInfo1;
    private List<String> mInfo2;
    private List<String> mInfo3;

    private String mObjectId;
    private Product mProduct;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_product_detail);
        mBinding.progressbar.setVisibility(View.VISIBLE);
        mBinding.content.setVisibility(View.GONE);
        mBinding.bottom.setVisibility(View.GONE);
        mObjectId = getIntent().getStringExtra(PRODID);
        initViews();
        String id = getIntent().getStringExtra(PRODID);
        fetchProductData(id);
    }

    private void initViews() {
        Toolbar toolbar = mBinding.toolbar;
        toolbar.setTitle(R.string.activity_product_detail_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tv = mBinding.originPrice;
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        mBinding.review.setOnClickListener(this);
        mBinding.spec.setOnClickListener(this);
        mBinding.detail.setOnClickListener(this);
        mBinding.increaseQuantity.setOnClickListener(this);
        mBinding.decreaseQuantity.setOnClickListener(this);
        mBinding.fav.setOnClickListener(this);
        mBinding.cart.setOnClickListener(this);
        mBinding.addToCart.setOnClickListener(this);
        mBinding.info1Layout.setOnClickListener(this);
        mBinding.info2Layout.setOnClickListener(this);
        mBinding.info3Layout.setOnClickListener(this);

        checkFav();

        initViewPager();

        mBinding.cartNum.setVisibility(View.GONE);
        CartPreferenceUtil cartPreferenceUtil = CartPreferenceUtil.getComplexPreferences(
                getApplicationContext());
        // CartList cartList = cartPreferenceUtil.getObject(CartPreferenceUtil.KEY, CartList.class);
        List<ProductData> dataList = cartPreferenceUtil.getProductData();
        if (dataList != null && dataList.size() > 0) {
            mBinding.cartNum.setVisibility(View.VISIBLE);
            mBinding.cartNum.setText(dataList.size() + "");
        }

    }

    private void initViewPager() {

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBinding.indicator.setIndex(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void fetchProductData(String id) {
        MLQuery<MLObject> query = MLQuery.getQuery("Product");
        query.whereEqualTo("objectId", id);
        MLQueryManager.getFirstInBackground(query, new GetCallback<MLObject>() {
            @Override
            public void done(MLObject object, MLException e) {
                if (e == null) {
                    mBinding.progressbar.setVisibility(View.GONE);
                    mBinding.content.setVisibility(View.VISIBLE);
                    mBinding.bottom.setVisibility(View.VISIBLE);

                    mProduct = new Product(object);
                    initProductInfo();
                    checkParams(mProduct);
                    mBinding.setProduct(mProduct);

                    Map<String, String> dimensions = new HashMap<>();
                    dimensions.put("ProductId", mProduct.getId());
                    dimensions.put("ProductName", mProduct.getTitle());
                    dimensions.put("Price", "" + mProduct.getPrice());
                    dimensions.put("UserName", MLUser.getCurrentUser().getUserName());

                    MLAnalytics.logEvent("ViewProduct", 1, dimensions);
                }
            }
        });
    }

    private void initProductInfo() {
        mBinding.price.setText(String.format(getApplicationContext().getString(
                R.string.activity_product_detail_price), (float) (mProduct.getPrice() / 100)
        ));
        mBinding.originPrice.setText(String.format(getApplicationContext().getString(
                R.string.activity_product_detail_unit_origin), (float) (mProduct.getOriginalPrice() / 100)
        ));

        initPicList(mProduct);
        try {
            initCustomInfo(mProduct);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkParams(Product product) {
        if (TextUtils.isEmpty(product.getIntro())) {
            mBinding.intro.setVisibility(View.GONE);
        }

        if (product.getServices() == null) {
            mBinding.service.setVisibility(View.GONE);
            mBinding.serviceLabel.setVisibility(View.GONE);
        }
    }


    private void initPicList(Product product) {
        List<String> pics = product.getIcons();
        mAdapter = new ProductGalleryAdapter(getApplicationContext(), pics);
        mBinding.viewPager.setAdapter(mAdapter);
        mBinding.indicator.setItemCount(pics.size());
        mBinding.indicator.setIndex(1);
    }

    private void initCustomInfo(Product product) throws JSONException {
        mInfo1 = new ArrayList<>();
        mInfo2 = new ArrayList<>();
        mInfo3 = new ArrayList<>();

        if (!TextUtils.isEmpty(product.getCustomInfo1())) {
            JSONObject customInfo1 = new JSONObject(product.getCustomInfo1());
            mBinding.info1Label.setText(customInfo1.optString("key"));
            JSONArray array = customInfo1.optJSONArray("value");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = new JSONObject(array.getString(i));
                    mInfo1.add(jsonObject.getString("value"));
                }
                mBinding.info1.setText(mInfo1.get(0));
            }
        } else {
            mBinding.info1Layout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(product.getCustomInfo2())) {
            JSONObject customInfo2 = new JSONObject(product.getCustomInfo2());
            mBinding.info2Label.setText(customInfo2.optString("key"));
            JSONArray array = customInfo2.optJSONArray("value");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = new JSONObject(array.getString(i));
                    mInfo2.add(jsonObject.getString("value"));
                }
                mBinding.info2.setText(mInfo2.get(0));
            }
        } else {
            mBinding.info2Layout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(product.getCustomInfo3())) {
            JSONObject customInfo3 = new JSONObject(product.getCustomInfo3());
            mBinding.info3Label.setText(customInfo3.optString("key"));
            JSONArray array = customInfo3.optJSONArray("value");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = new JSONObject(array.getString(i));
                    mInfo3.add(jsonObject.getString("value"));
                }
                mBinding.info3.setText(mInfo3.get(0));
            }
        } else {
            mBinding.info3Layout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info1_layout:
                onClickInfo(mInfo1, mBinding.info1);
                break;
            case R.id.info2_layout:
                onClickInfo(mInfo2, mBinding.info2);
                break;
            case R.id.info3_layout:
                onClickInfo(mInfo3, mBinding.info3);
                break;
            case R.id.increase_quantity:
                increase();
                break;
            case R.id.decrease_quantity:
                decrease();
                break;
            case R.id.review:
                Intent reviewIntent = new Intent(this, ReviewActivity.class);
                reviewIntent.putExtra(PRODID, mObjectId);
                startActivity(reviewIntent);
                break;
            case R.id.spec:
                Intent specIntent = new Intent(this, SpecActivity.class);
                specIntent.putExtra(SpecActivity.SPEC, mProduct.getInfo());
                startActivity(specIntent);
                break;
            case R.id.detail:
                break;
            case R.id.fav:
                fav();
                break;
            case R.id.cart:
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.putExtra(MainActivity.INTENT_TAB_INDEX, 2);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                break;
            case R.id.add_to_cart:
                addToCart();
                break;
            default:
                break;
        }
    }

    private void decrease() {
        int quantity = Integer.parseInt(mBinding.quantity.getText().toString());
        if (quantity == 1) {
            return;
        } else {
            mBinding.quantity.setText(--quantity + "");
        }
    }

    private void increase() {
        int quantity = Integer.parseInt(mBinding.quantity.getText().toString());
        // check store quantity
        mBinding.quantity.setText(++quantity + "");
    }

    private void fav() {
        if (UserManager.getInstance().getCurrentUser() == null) {
            FFLog.toast(this, "Please login first");
            return;
        }
        boolean isFav = mBinding.fav.isSelected();
        mBinding.fav.setSelected(!isFav);
        if (isFav) {
            UserManager.getInstance().removeFavorite(mProduct, new OperationCallback() {
                @Override
                public void success() {

                }

                @Override
                public void failed(String error) {

                }
            });
        } else {
            UserManager.getInstance().addFavorite(mProduct, new OperationCallback() {
                @Override
                public void success() {
                    Map<String, String> dimensions = new HashMap<>();
                    dimensions.put("ProductId", mProduct.getId());
                    dimensions.put("ProductName", mProduct.getTitle());
                    dimensions.put("Price", "" + mProduct.getPrice());
                    dimensions.put("UserName", MLUser.getCurrentUser().getUserName());

                    MLAnalytics.logEvent("FavoriteProduct", 1, dimensions);
                }

                @Override
                public void failed(String error) {

                }
            });
        }
    }

    private void onClickInfo(List<String> list, final TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        final CharSequence[] array = list.toArray(new CharSequence[list.size()]);
        builder.setTitle(getString(R.string.activity_product_detail_choice_title))
                .setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textView.setText(array[which]);
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void checkFav() {
        if (UserManager.getInstance().getCurrentUser() == null) {
            mBinding.fav.setSelected(false);
            return;
        }

        MLRelation favoritesRelation = UserManager.getInstance().getCurrentUser().getFavorites();
        if (favoritesRelation != null) {
            MLQuery<MLObject> query = favoritesRelation.getQuery();
            MLQueryManager.findAllInBackground(query, new FindCallback<MLObject>() {
                @Override
                public void done(List<MLObject> list, MLException e) {
                    if (e == null) {
                        for (MLObject object : list) {
                            if (object.getObjectId().equals(mObjectId)) {
                                mBinding.fav.setSelected(true);
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    private void addToCart() {
        if (mProduct == null) {
            return;
        }
        ProductData productData = new ProductData();
        productData.setPrice(mProduct.getPrice());
        productData.setCount(Integer.parseInt(mBinding.quantity.getText().toString()));
        productData.setId(mProduct.getId());
        productData.setTitle(mProduct.getTitle());
        productData.setImageUrl(mProduct.getIcons().get(0));
        String customInfo = buildCustomInfo();
        if (!TextUtils.isEmpty(customInfo.toString().trim())) {
            productData.setCustomInfo(customInfo);
        }

        CartPreferenceUtil cartPreferenceUtil = CartPreferenceUtil.getComplexPreferences(
                getApplicationContext());
        if (!cartPreferenceUtil.add(productData)) {
            FFLog.toast(getApplicationContext(),
                    getString(R.string.activity_product_detail_already_in_cart));
        }

        List<ProductData> list = cartPreferenceUtil.getProductData();
        mBinding.cartNum.setVisibility(View.VISIBLE);
        mBinding.cartNum.setText(list.size() + "");

        Map<String, String> dimensions = new HashMap<>();

        dimensions.put("ProductId", mProduct.getId());
        dimensions.put("ProductName", mProduct.getTitle());
        dimensions.put("Price", "" + mProduct.getPrice());
        dimensions.put("UserName", MLUser.getCurrentUser().getUserName());
        dimensions.put("BuyCount", "" + productData.getCount());

        MLAnalytics.logEvent("AddShoppingCart", 1, dimensions);
    }

    @NonNull
    private String buildCustomInfo() {
        StringBuilder customInfo = new StringBuilder();
        if (mBinding.info1Layout.getVisibility() != View.GONE &&
                !TextUtils.isEmpty(mBinding.info1.getText().toString())) {
            customInfo.append(mBinding.info1.getText().toString() + " ");
        }
        if (mBinding.info2Layout.getVisibility() != View.GONE &&
                !TextUtils.isEmpty(mBinding.info2.getText().toString())) {
            customInfo.append(mBinding.info2.getText().toString() + " ");
        }
        if (mBinding.info3Layout.getVisibility() != View.GONE &&
                !TextUtils.isEmpty(mBinding.info3.getText().toString())) {
            customInfo.append(mBinding.info3.getText().toString() + " ");
        }
        return customInfo.toString();
    }


}