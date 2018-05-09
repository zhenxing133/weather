package com.weather.activity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.weather.R;
import com.weather.adapter.RecyclerViewAdapter;
import com.weather.bean.WeatherBean;
import com.weather.db.City;
import com.weather.db.County;
import com.weather.db.Province;
import com.weather.httpRequest.OkHttpUtils;
import com.weather.util.Constant;
import com.weather.util.ResolverUtils;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Slingge on 2017/2/22 0022.
 */
public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.HolderCilck {

    @BindView(R.id.image_bg)
    ImageView imageBg;
    @BindView(R.id.image_back)
    ImageView image_back;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.tv_temper)
    TextView tvTemper;
    @BindView(R.id.tv_weather)
    TextView tvWeather;
    @BindView(R.id.tv_quality)
    TextView tvQuality;
    @BindView(R.id.tv_pm25)
    TextView tvPm25;
    @BindView(R.id.tv_aqi)
    TextView tvAqi;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.ll_forecast)
    LinearLayout llForecast;
    @BindView(R.id.tv_comfort)
    TextView tvComfort;
    @BindView(R.id.tv_wash)
    TextView tvWash;
    @BindView(R.id.tv_sport)
    TextView tvSport;
    private RecyclerViewAdapter adapter;
    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_COUNTY = 3;

    private ProgressDialog dialog;


    private List<Province> provinces;
    private List<City> citys;
    private List<County> countys;
    //选中的省市
    private Province selectProvince;
    private City selectCity;
    //选中的级别
    private int currentLevel;
    private List<String> datas = new ArrayList<>();
    private ImageView navi_back;
    private TextView nave_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Connector.getDatabase();
        initNavigationView();
        init();
        //默认北京天气
        requestWeather("CN101010100");
        //给预报上背景
        requestImage();
    }
    private void requestImage() {
        OkHttpUtils.get("http://guolin.tech/api/bing_pic", new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                //Log.e("yzx", "000" + response);
                if (response != null) {
                    Glide.with(MainActivity.this).load(response).into(imageBg);
                } else {
                    requestImage();
                }


            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryProvince();
    }

    private void init() {
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

    }

    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navi_back = headerView.findViewById(R.id.image_back);
        nave_text = headerView.findViewById(R.id.navi_text);
        navi_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                }

            }
        });
        RecyclerView recyclerView = (RecyclerView) headerView.findViewById(R.id.recyclerView);
        recyclerView.setFocusable(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (adapter == null) {
            adapter = new RecyclerViewAdapter(this, datas);
            adapter.setHolderCilck(this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void click(int position) {
        if (currentLevel == LEVEL_PROVINCE) {
            selectProvince = provinces.get(position);
            queryCity();
        } else if (currentLevel == LEVEL_CITY) {
            selectCity = citys.get(position);
            queryCounty();
        } else if (currentLevel == LEVEL_COUNTY) {
            initData(countys.get(position).getWeatherId());
            drawerLayout.closeDrawers();
        }
        adapter.notifyDataSetChanged();
    }


    /**
     * 查询省
     */
    private void queryProvince() {
        nave_text.setText("中国");
        navi_back.setVisibility(View.INVISIBLE);
        provinces = DataSupport.findAll(Province.class);
        if (provinces.size() > 0) {
            datas.clear();
            for (Province province : provinces) {
                datas.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_PROVINCE;
        } else {
            //请求网络
            String url = Constant.BASEURL;
            requestServer(url, "province");
        }

    }

    /**
     * 查询市
     */
    private void queryCity() {
        nave_text.setText(selectProvince.getProvinceName());
        navi_back.setVisibility(View.VISIBLE);
        citys = DataSupport.where("provinceid = ?", String.valueOf(selectProvince.getId())).find(City.class);
        if (citys.size() > 0) {
            datas.clear();
            for (City city : citys) {
                datas.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_CITY;
        } else {
            //请求网络
            int provinceCode = selectProvince.getProvinceCode();
            String url = Constant.BASEURL + "/" + provinceCode;
            requestServer(url, "city");
        }

    }

    /**
     * 查询县
     */
    private void queryCounty() {
        nave_text.setText(selectCity.getCityName());
        navi_back.setVisibility(View.VISIBLE);
        countys = DataSupport.where("cityid = ?", String.valueOf(selectCity.getId())).find(County.class);
        if (countys.size() > 0) {
            datas.clear();
            for (County county : countys) {
                datas.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_COUNTY;
        } else {
            //请求网络
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String url = Constant.BASEURL + "/" + provinceCode + "/" + cityCode;
            requestServer(url, "county");
        }

    }

    /**
     * 请求
     *
     * @param url
     * @param type
     */
    private void requestServer(String url, final String type) {
        showProgressDialog();
        OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                boolean result = false;
                switch (type) {
                    case "province":
                        result = ResolverUtils.ResolverProvinces(response);
                        break;
                    case "city":
                        result = ResolverUtils.ResolverCity(response, selectProvince.getId());
                        break;
                    case "county":
                        result = ResolverUtils.ResolverCounty(response, selectCity.getId());
                        break;
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    /**
     * 数据展示
     *
     * @param weatherId
     */
    private void initData(String weatherId) {
        requestWeather(weatherId);
    }

    public void requestWeather(final String weatherId) {
        String url = Constant.weather_url + weatherId + "&key=ddba4129c3694428b2a61c633468dfe2";
        Log.e("yzx", url);
        OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                WeatherBean weatherBean = gson.fromJson(response, WeatherBean.class);
                tvTitle.setText(weatherBean.getHeWeather().get(0).getBasic().getLocation());
                //天气情况
                tvDate.setText(weatherBean.getHeWeather().get(0).getUpdate().getLoc());
                tvWeather.setText(weatherBean.getHeWeather().get(0).getNow().getCond_txt() + "℃");
                tvTemper.setText(weatherBean.getHeWeather().get(0).getNow().getFl());
                //预报
                llForecast.removeAllViews();
                for (int i = 0; i < weatherBean.getHeWeather().get(0).getDaily_forecast().size(); i++) {
                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.forecast_item, llForecast, false);
                    TextView tv_data = view.findViewById(R.id.date_text);
                    TextView infoText = view.findViewById(R.id.info_text);
                    TextView maxText = view.findViewById(R.id.max_text);
                    TextView minText = view.findViewById(R.id.min_text);
                    tv_data.setText(weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getDate());
                    infoText.setText(weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getCond().getTxt_d());
                    maxText.setText("最高：" + weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getTmp().getMax() + "℃");
                    minText.setText("最低：" + weatherBean.getHeWeather().get(0).getDaily_forecast().get(i).getTmp().getMin() + "℃");
                    llForecast.addView(view);
                }
                //空气质量
                tvQuality.setText("空气质量:" + weatherBean.getHeWeather().get(0).getAqi().getCity().getQlty());
                tvPm25.setText(weatherBean.getHeWeather().get(0).getAqi().getCity().getPm25());
                tvAqi.setText(weatherBean.getHeWeather().get(0).getAqi().getCity().getAqi());
                //生活建议
                tvComfort.setText("舒适度:" + "\r\n" + weatherBean.getHeWeather().get(0).getSuggestion().getComf().getTxt());
                tvWash.setText("洗车指数:" + "\r\n" + weatherBean.getHeWeather().get(0).getSuggestion().getCw().getTxt());
                tvSport.setText("运行建议:" + "\r\n" + weatherBean.getHeWeather().get(0).getSuggestion().getSport().getTxt());
                //swipe.setRefreshing(false);
                //SharedPreferencesUtils.clear(WeatherActivity.this, "weather");
            }

            @Override
            public void onFailure(Exception e) {
                //swipe.setRefreshing(false);
            }
        });
    }

    /**
     * 显示等待框
     */
    public void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("加载中...");
            dialog.setCanceledOnTouchOutside(true);
        }
        dialog.show();
    }

    /**
     * 关闭等待框
     */
    public void closeProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
