/**
 * Copyright (c) 2015-present, MaxLeap.
 * All rights reserved.
 * ----
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.maxleap.mall.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maxleap.GetCallback;
import com.maxleap.MLAnalytics;
import com.maxleap.MLObject;
import com.maxleap.MLQuery;
import com.maxleap.MLQueryManager;
import com.maxleap.MLUser;
import com.maxleap.exception.MLException;
import com.maxleap.mall.R;
import com.maxleap.mall.adapters.OrderConfirmAdapter;
import com.maxleap.mall.manage.OperationCallback;
import com.maxleap.mall.manage.UserManager;
import com.maxleap.mall.models.Order;
import com.maxleap.mall.models.OrderProduct;
import com.maxleap.mall.models.ProductData;
import com.maxleap.mall.utils.DialogUtil;
import com.maxleap.mall.utils.FFLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailActivity extends BaseActivity implements View.OnClickListener {

    public static final String INTENT_ORDER_ID_KEY = "intent_order_id_key";
    public static final int REQUEST_COMMENT_CODE = 10;

    private ListView productLV;
    private TextView orderNo;
    private TextView orderState;
    private TextView orderTotal;
    private TextView receiveUser;
    private TextView receiveAddress;
    private TextView createTime;
    private TextView deliverType;
    private TextView remarks;
    private TextView receiptHeading;
    private TextView receiptType;
    private TextView receiptContent;
    private ProgressBar progressBar;
    private FrameLayout confirmArea;
    private Button confirmBtn;
    private Dialog dialog;

    private Order order;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        init();
    }

    private void init() {
        initToolbar();
        initUI();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_order_detail_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != null) {
                    Intent intent = new Intent().putExtra(OrderDetailActivity.INTENT_ORDER_ID_KEY, order.getOrderStatus());
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
    }

    @Override
    public void finish() {
        if (order != null) {
            Intent intent = new Intent().putExtra(MyOrderActivity.INTENT_ORDER_STATE_KEY, order.getOrderStatus());
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    private void initUI() {
        dialog = DialogUtil.createProgressDialog(this);
        progressBar = (ProgressBar) findViewById(R.id.order_detail_progress);
        confirmArea = (FrameLayout) findViewById(R.id.order_detail_confirm_area);
        confirmBtn = (Button) findViewById(R.id.order_detail_confirm);
        confirmBtn.setOnClickListener(this);
        productLV = (ListView) findViewById(R.id.order_detail_products);
        View headView = LayoutInflater.from(this).inflate(R.layout.order_detail_head, null, false);
        orderNo = (TextView) headView.findViewById(R.id.order_detail_no);

        orderState = (TextView) headView.findViewById(R.id.order_detail_state);
        orderState.setOnClickListener(this);

        productLV.addHeaderView(headView);
        View footView = LayoutInflater.from(this).inflate(R.layout.order_detail_foot, null, false);
        orderTotal = (TextView) footView.findViewById(R.id.order_detail_total);

        receiveUser = (TextView) footView.findViewById(R.id.order_detail_receive_user);

        receiveAddress = (TextView) footView.findViewById(R.id.order_detail_receive_address);

        createTime = (TextView) footView.findViewById(R.id.order_detail_time);

        deliverType = (TextView) footView.findViewById(R.id.order_detail_deliver_type);

        remarks = (TextView) footView.findViewById(R.id.order_detail_remarks);

        receiptHeading = (TextView) footView.findViewById(R.id.order_detail_receipt_heading);

        receiptType = (TextView) footView.findViewById(R.id.order_detail_receipt_type);

        receiptContent = (TextView) footView.findViewById(R.id.order_detail_receipt_content);

        productLV.addFooterView(footView);
        fetchOrderData();
    }

    private void initData() {
        orderNo.setText(String.format(getString(R.string.activity_my_order_no), order.getId()));

        switch (order.getOrderStatus()) {
            case 1:
                orderState.setText(R.string.activity_my_order_state_cancel);
                orderState.setTextColor(getResources().getColor(R.color.text_color_orange));
                confirmBtn.setText(R.string.activity_my_order_to_pay);
                confirmArea.setVisibility(View.VISIBLE);
                break;
            case 2:
                orderState.setText(R.string.activity_my_order_state_deliver);
                orderState.setTextColor(getResources().getColor(R.color.text_color_black));
                confirmArea.setVisibility(View.GONE);
                break;
            case 3:
                orderState.setText(R.string.activity_my_order_state_delivered);
                orderState.setTextColor(getResources().getColor(R.color.text_color_black));
                confirmBtn.setText(R.string.activity_my_order_to_confirm);
                confirmArea.setVisibility(View.VISIBLE);
                break;
            case 4:
                orderState.setText(R.string.activity_my_order_state_done);
                orderState.setTextColor(getResources().getColor(R.color.text_color_black));
                confirmBtn.setText(R.string.activity_my_order_to_comment);
                confirmArea.setVisibility(View.VISIBLE);
                break;
            case 5:
                orderState.setText(R.string.activity_my_order_state_commented);
                orderState.setTextColor(getResources().getColor(R.color.text_color_black));
                confirmArea.setVisibility(View.GONE);
                break;
            case 6:
            case 7:
                orderState.setText(R.string.activity_my_order_state_canceled);
                orderState.setTextColor(getResources().getColor(R.color.text_color_black));
                confirmArea.setVisibility(View.GONE);
                break;
        }

        int total = 0;
        for (int i = 0; i < order.getOrderProducts().size(); i++) {
            total = total + order.getOrderProducts().get(i).getQuantity() * order.getOrderProducts().get(i).getPrice();
        }
        orderTotal.setText(String.format(getString(R.string.activity_my_order_total), total / 100f));

        receiveUser.setText(String.format(
                getString(R.string.activity_order_detail_receive_user),
                order.getAddress().getName() + " " + order.getAddress().getTel()));

        receiveAddress.setText(String.format(
                getString(R.string.activity_order_detail_receive_address),
                order.getAddress().getStreet()));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createTime.setText(String.format(
                getString(R.string.activity_order_detail_order_time),
                simpleDateFormat.format(order.getCreateTime())));

        deliverType.setText(String.format(
                getString(R.string.activity_order_detail_deliver_type),
                order.getDelivery()));

        remarks.setText(String.format(
                getString(R.string.activity_order_detail_remarks),
                TextUtils.isEmpty(order.getRemarks()) ?
                        getString(R.string.activity_order_detail_no_string) : order.getRemarks()));

        receiptHeading.setText(String.format(
                getString(R.string.activity_order_detail_receipt_heading),
                TextUtils.isEmpty(order.getReceiptHeading()) ?
                        getString(R.string.activity_order_detail_no_string) : order.getReceiptHeading()));

        receiptType.setText(String.format(
                getString(R.string.activity_order_detail_receipt_type),
                TextUtils.isEmpty(order.getReceiptType()) ?
                        getString(R.string.activity_order_detail_no_string) : order.getReceiptType()));

        receiptContent.setText(String.format(
                getString(R.string.activity_order_detail_receipt_content),
                TextUtils.isEmpty(order.getReceiptContent()) ?
                        getString(R.string.activity_order_detail_no_string) : order.getReceiptContent()));

        OrderConfirmAdapter adapter = new OrderConfirmAdapter(this, order.getOrderProducts());
        productLV.setAdapter(adapter);
    }

    private void fetchOrderData() {
        progressBar.setVisibility(View.VISIBLE);
        confirmArea.setVisibility(View.GONE);
        productLV.setVisibility(View.GONE);
        String id = getIntent().getStringExtra(INTENT_ORDER_ID_KEY);
        MLQuery query = new MLQuery("Order");
        query.include("address");
        query.include("order_products.product");

        MLQueryManager.getInBackground(query, id, new GetCallback<MLObject>() {
            @Override
            public void done(final MLObject object, MLException e) {
                if (e == null) {
                    order = Order.from(object);
                    progressBar.setVisibility(View.GONE);
                    confirmArea.setVisibility(View.VISIBLE);
                    productLV.setVisibility(View.VISIBLE);
                    initData();
                } else {
                    FFLog.toast(OrderDetailActivity.this, e.getMessage());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_detail_confirm:
                String confirmText = confirmBtn.getText().toString();
                if (confirmText.equals(getString(R.string.activity_my_order_to_pay))) {
                    updateState(2);
                } else if (confirmText.equals(getString(R.string.activity_my_order_to_confirm))) {
                    updateState(4);
                } else if (confirmText.equals(getString(R.string.activity_my_order_to_comment))) {
                    ArrayList<ProductData> products = new ArrayList<>();
                    for (OrderProduct orderProduct : order.getOrderProducts()) {
                        ProductData productData = new ProductData();
                        productData.setId(orderProduct.getProduct().getId());
                        productData.setPrice(orderProduct.getPrice());
                        productData.setCount(orderProduct.getQuantity());
                        productData.setTitle(orderProduct.getProduct().getTitle());
                        productData.setCustomInfo(orderProduct.getCustomInfo());
                        productData.setImageUrl(orderProduct.getProduct().getIcons().get(0));
                        products.add(productData);
                    }
                    Intent intent = new Intent(OrderDetailActivity.this, CommentActivity.class);
                    intent.putExtra(CommentActivity.INTENT_ORDER_ID_KEY, order.getId());
                    intent.putExtra(CommentActivity.INTENT_PRODUCT_DATA_KEY, products);
                    startActivityForResult(intent, REQUEST_COMMENT_CODE);
                }
                break;
            case R.id.order_detail_state:
                if (orderState.getText().toString().
                        equals(getString(R.string.activity_my_order_state_cancel))) {
                    updateState(6);
                }
                break;
            default:
                break;
        }
    }

    private void updateState(final int state) {
        final int originalState = order.getOrderStatus();
        order.setOrderStatus(state);
        dialog.show();
        UserManager.getInstance().updateOrder(order, new OperationCallback() {
            @Override
            public void success() {
                dialog.dismiss();
                initData();

                if (state == 6) {
                    Map<String, String> dimensions = new HashMap<>();
                    StringBuilder productIds = new StringBuilder();
                    StringBuilder productNames = new StringBuilder();
                    for (OrderProduct data : order.getOrderProducts()) {
                        productIds.append(data.getId()).append(",");
                        productNames.append(data.getProduct().getTitle()).append(",");
                    }

                    dimensions.put("OrderNo", order.getId());
                    dimensions.put("ProductIds", productIds.toString());
                    dimensions.put("ProductName", productNames.toString());
                    dimensions.put("TotalBuyCount", "" + order.getOrderProducts().size());
                    dimensions.put("Price", "" + order.getTotal());
                    dimensions.put("UserName", MLUser.getCurrentUser().getUserName());

                    MLAnalytics.logEvent("CancelOrder", 1, dimensions);
                }
            }

            @Override
            public void failed(String error) {
                dialog.dismiss();
                order.setOrderStatus(originalState);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_COMMENT_CODE && resultCode == RESULT_OK) {
            order.setOrderStatus(5);
            initData();
        }
    }
}
