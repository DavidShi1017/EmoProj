package cn.airburg.emo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.io.File;
import java.util.Date;
import cn.airburg.emo.R;

import cn.airburg.emo.listeners.DialogActionCallBack;
import cn.airburg.emo.manager.AppManager;
import cn.airburg.emo.manager.ShareManager;
import cn.airburg.emo.utils.DateUtils;
import cn.airburg.emo.utils.ImageTools;
import cn.airburg.emo.utils.LocationUtils;
import cn.airburg.emo.utils.LogUtils;
import cn.airburg.emo.utils.Utils;

/**
 * Created by shig on 2015/6/3.
 */
public class PhotoActivity extends Activity {
    private static final String KEY_FILE_NAME = "FileName";
    private static final String KEY_HAZE_VALUE = "HazeValue";
    private String fileName;
    private int hazeValue;
    private ImageView ivAddressMark;
    private TextView tvHazeValue;
    private TextView tvHazeDesc;
    private TextView tvDate;
    private TextView tvAddress;
    private ImageView imageView;
    private LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;
    private String address;
    //public final static String TAG = PhotoActivity.class.getSimpleName();

    public static Intent createIntent(Context context, String fileName, int mHazeValue) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra(KEY_FILE_NAME, fileName);
        intent.putExtra(KEY_HAZE_VALUE, mHazeValue);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        AppManager.getAppManager().addActivity(this);
        fileName = getIntent().getStringExtra(KEY_FILE_NAME);
        hazeValue = getIntent().getIntExtra(KEY_HAZE_VALUE, 0);
        initUiFields();



        if(!LocationUtils.isAllow(getApplicationContext())){
            LocationUtils.showLocationDialog(this, new DialogActionCallBack() {
                @Override
                public void onOkAction() {
                    getLocation();
                }

                @Override
                public void onCancelAction() {

                }
            });
        }else{
            getLocation();
        }
    }

    private void getLocation(){
        if(LocationUtils.isAllow(getApplicationContext())){

            mLocationClient = new LocationClient(this.getApplicationContext());
            mMyLocationListener = new MyLocationListener();
            mLocationClient.registerLocationListener(mMyLocationListener);

            LocationClientOption option = new LocationClientOption();
            LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Battery_Saving;
            String tempCoor="gcj02";
            option.setLocationMode(tempMode);//设置定位模式
            option.setCoorType(tempCoor);//返回的定位结果是百度经纬度，默认值gcj02
            //option.setOpenGps(true);
            option.setScanSpan(1000);
            option.setIsNeedAddress(true);
            mLocationClient.setLocOption(option);

            mLocationClient.start();
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            String city = location.getCity();
            String district = location.getDistrict();

            if (location != null){
                if (city != null && !city.isEmpty() && !"null".equals(city))

                    address = city;
                if (district != null && !district.isEmpty() && !"null".equals(district))
                    address += district;
            }
            if (address != null && !address.isEmpty()){
                ivAddressMark.setVisibility(View.VISIBLE);
                tvAddress.setText(address);
                mLocationClient.stop();
                Log.e("BaiduLocationApiDem", address);
            }
        }
    }

    private void initUiFields() {
        imageView = (ImageView) findViewById(R.id.imageView);
        tvHazeValue = (TextView) findViewById(R.id.tv_haze_value);
        tvHazeDesc = (TextView) findViewById(R.id.tv_haze_desc);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvAddress = (TextView) findViewById(R.id.tv_address);

        ivAddressMark = (ImageView) findViewById(R.id.iv_address_mark);

    }

    private void setDisplayedOverview() {
        File path = Utils.getPicturesPath();
        LogUtils.e("-->" + path);
        Bitmap bitmap = ImageTools.rotateBitmap(path + "/image.jpg");
        Bitmap newBitmap = ImageTools.zoomBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);

        bitmap.recycle();

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(newBitmap);
        tvHazeValue.setText(String.valueOf(hazeValue));
        String hazeDesc = Utils.getHazeValueDesc(hazeValue);
        tvHazeDesc.setText(hazeDesc);
        Date date = new Date();
        String datetime = DateUtils.dateToString(getApplicationContext(), date) + " " + DateUtils.timeToString(getApplicationContext(), date);
        tvDate.setText(datetime);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayedOverview();
    }

    public void shareMore(View view) {
        String path = Utils.getAndSaveCurrentScreenshotImage(true, this, imageView.getWidth(), imageView.getHeight());
        ShareManager mShareManager = new ShareManager(this);
        mShareManager.shareScreenshot(path) ;
    }

    public void shareFriend(View view) {

        String path = Utils.getAndSaveCurrentScreenshotImage(true, this, imageView.getWidth(), imageView.getHeight());
        LogUtils.e("image path is::" + path);

        ShareManager mShareManager = new ShareManager(this) ;
        mShareManager.toWeixinFriend(path);
    }

    public void back(View view) {
        setResult(Activity.RESULT_OK);
        finish();
    }


}
