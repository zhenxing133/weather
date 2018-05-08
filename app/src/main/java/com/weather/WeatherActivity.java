package com.weather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.weather.bean.WeatherBean;
import com.weather.utils.SharedPreferencesUtils;
import com.weather.utils.StatusBarUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yuan.zhen.xing on 2018-05-07.
 */

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.city_title)
    TextView cityTitle;
    @BindView(R.id.tv_update_time)
    TextView tvUpdateTime;
    @BindView(R.id.ll_forecast)
    LinearLayout ll_forecast;
    @BindView(R.id.tv_aqi)
    TextView tvAqi;
    @BindView(R.id.tv_pm)
    TextView tvPm;
    @BindView(R.id.tv_comfort)
    TextView tvComfort;
    @BindView(R.id.tv_wash)
    TextView tvWash;
    @BindView(R.id.tv_sport)
    TextView tvSport;
    @BindView(R.id.scrollview)
    ScrollView scrollview;
    @BindView(R.id.iv_view)
    ImageView ivView;
    @BindView(R.id.tv_degree)
    TextView tvDegree;
    @BindView(R.id.tv_weather_info)
    TextView tvWeatherInfo;
    @BindView(R.id.tv_quality)
    TextView tvQuality;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.transparencyBar(WeatherActivity.this);
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        //使用缓存
        String weather = (String) SharedPreferencesUtils.getParam(WeatherActivity.this, "weather", "");
        if (TextUtils.isEmpty(weather)) {
            String weatherId = getIntent().getStringExtra("weatherId");
            requestImage();
            requestWeather(weatherId);
        } else {
            Gson gson = new Gson();
            WeatherBean weatherBean = gson.fromJson(weather, WeatherBean.class);
            initData(weatherBean);
            requestImage();
        }

    }

    public void requestWeather(final String weatherId) {
        String url = Constant.weather_url + weatherId + "&key=2d49722a18674405a3aa8b3bf8eb99f3";
        OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.e("yzx", "response=" + response);
                SharedPreferencesUtils.setParam(WeatherActivity.this,"weather",response);
                Gson gson = new Gson();
                WeatherBean weatherBean = gson.fromJson(response, WeatherBean.class);
                initData(weatherBean);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("yzx", "failed=");
            }
        });
    }

    public void requestImage() {
        OkHttpUtils.get("http://guolin.tech/api/bing_pic", new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Glide.with(WeatherActivity.this).load(response).into(ivView);

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void initData(WeatherBean weatherBean) {
        cityTitle.setText(weatherBean.getHeWeather().get(0).getBasic().getCity());
        tvUpdateTime.setText(weatherBean.getHeWeather().get(0).getUpdate().getLoc());
        tvDegree.setText(weatherBean.getHeWeather().get(0).getNow().getCond_txt());
        tvWeatherInfo.setText(weatherBean.getHeWeather().get(0).getDaily_forecast().get(0).getTmp().getMax() + "℃");
        //预报内容
        ll_forecast.removeAllViews();
        for (int i = 0; i < weatherBean.getHeWeather().get(0).getDaily_forecast().size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, ll_forecast, false);
            TextView tv_data = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            tv_data.setText(weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getDate());
            infoText.setText(weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getCond().getTxt_d());
            maxText.setText("最高：" + weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getTmp().getMax() + "℃");
            minText.setText("最低：" + weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getTmp().getMin() + "℃");
            ll_forecast.addView(view);
        }
        //空气质量
        tvQuality.setText("空气质量："+weatherBean.getHeWeather().get(0).getAqi().getCity().getQlty());
        tvAqi.setText(weatherBean.getHeWeather().get(0).getAqi().getCity().getAqi());
        tvPm.setText(weatherBean.getHeWeather().get(0).getAqi().getCity().getPm25());
        //运动指数
        tvComfort.setText("舒适度:"+"\r\n"+weatherBean.getHeWeather().get(0).getSuggestion().getComf().getTxt());
        tvWash.setText("洗车指数:"+"\r\n"+weatherBean.getHeWeather().get(0).getSuggestion().getCw().getTxt());
        tvSport.setText("运行建议:"+"\r\n"+weatherBean.getHeWeather().get(0).getSuggestion().getSport().getTxt());
    }

}
