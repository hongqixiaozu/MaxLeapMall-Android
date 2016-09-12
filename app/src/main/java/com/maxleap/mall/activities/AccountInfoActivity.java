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
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxleap.CountCallback;
import com.maxleap.MLFile;
import com.maxleap.MLFileManager;
import com.maxleap.MLObject;
import com.maxleap.MLQuery;
import com.maxleap.MLQueryManager;
import com.maxleap.ProgressCallback;
import com.maxleap.SaveCallback;
import com.maxleap.mall.R;
import com.maxleap.mall.manage.OperationCallback;
import com.maxleap.mall.manage.UserManager;
import com.maxleap.mall.models.User;
import com.maxleap.mall.utils.DialogUtil;
import com.maxleap.mall.utils.FFLog;
import com.maxleap.mall.utils.UnitUtil;
import com.maxleap.exception.MLException;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AccountInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SELECT_PICTURE = 2;

    private ImageView mPhotoView;
    private TextView mNameView;
    private TextView mAddrCountView;

    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        init();
    }

    private void init() {
        initToolbar();
        initUI();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_account_info_title);
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
        findViewById(R.id.account_info_name_area).setOnClickListener(this);
        findViewById(R.id.account_info_pic_area).setOnClickListener(this);
        findViewById(R.id.account_info_address_area).setOnClickListener(this);
        mUser = UserManager.getInstance().getCurrentUser();
        mPhotoView = (ImageView) findViewById(R.id.account_info_pic);
        if (mUser.getIcon() != null) {
            Picasso.with(this).load("http://" + mUser.getIcon()).
                    placeholder(R.mipmap.def_portrait).into(mPhotoView);
        }
        mNameView = (TextView) findViewById(R.id.account_info_name);
        mNameView.setText(mUser.getNickname());
        mAddrCountView = (TextView) findViewById(R.id.account_info_address);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAddressCount();
    }

    private void fetchAddressCount() {
        FFLog.d("start fetchAddressCount");
        MLQuery query = new MLQuery("Address");
        MLObject user = MLObject.createWithoutData("_User", mUser.getId());
        query.whereEqualTo("user", user);
        MLQueryManager.countInBackground(query, new CountCallback() {
            @Override
            public void done(int count, MLException e) {
                FFLog.d("fetchAddressCount e: " + e);
                if (e == null) {
                    mAddrCountView.setText(String.format(
                            getString(R.string.activity_account_info_address_unit), count));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_info_name_area:
                clickUsername();
                break;
            case R.id.account_info_pic_area:
                clickPhoto();
                break;
            case R.id.account_info_address_area:
                Intent intent = new Intent(this, AddressActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void clickUsername() {
        DialogUtil.showInputInfoDialog(this, getString(R.string.activity_account_info_name),
                getString(R.string.dialog_username_hint), mUser.getNickname(), new DialogUtil.Listener() {
                    @Override
                    public void onOk(String content) {
                        if (!TextUtils.isEmpty(content)) {
                            mUser.setNickname(content);
                            mNameView.setText(content);
                            updateUser();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });

    }

    private void updateUser() {
        FFLog.d("start updateUser");
        UserManager.getInstance().updateUserBasicInfo(mUser, new OperationCallback() {
            @Override
            public void success() {
                FFLog.toast(AccountInfoActivity.this, R.string.toast_update_user_success);
            }

            @Override
            public void failed(String error) {
                FFLog.toast(AccountInfoActivity.this, error);
            }
        });
    }

    private void clickPhoto() {
        DialogUtil.showUploadPhotoDialog(this, new DialogUtil.UploadPhotoListener() {
            @Override
            public void onTakePic() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

            @Override
            public void onPickPic() {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_SELECT_PICTURE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPhotoView.setImageBitmap(imageBitmap);
            uploadFile(getBytes(imageBitmap));
        } else if (requestCode == REQUEST_SELECT_PICTURE) {
            final Uri selectedImageUri = data.getData();
            mPhotoView.setImageURI(selectedImageUri);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(
                                AccountInfoActivity.this.getContentResolver(), selectedImageUri);
                        int size = UnitUtil.dpToPx(AccountInfoActivity.this, 32);
                        bmp = ThumbnailUtils.extractThumbnail(bmp, size, size);
                        uploadFile(getBytes(bmp));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private byte[] getBytes(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void uploadFile(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        FFLog.d("start uploadFile");
        final MLFile file = new MLFile("photo.png", data);
        MLFileManager.saveInBackground(file, new SaveCallback() {
            @Override
            public void done(MLException e) {
                if (e == null) {
                    FFLog.d("uploadFile url : " + file.getUrl());
                    mUser.setIcon(file.getUrl());
                    updateUser();
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(int percentDone) {
            }
        });

    }

}
