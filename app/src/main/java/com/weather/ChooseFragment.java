package com.weather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.weather.sql.City;
import com.weather.sql.County;
import com.weather.sql.Province;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yuan.zhen.xing on 2018-05-04.
 */

public class ChooseFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_COUNTY = 3;

    private ProgressDialog dialog;
    private TextView tv_title;
    private ImageView iv_back;
    private RecyclerView rc_view;

    private List<Province> provinces;
    private List<City> citys;
    private List<County> countys;
    //选中的省市
    private Province selectProvince;
    private City selectCity;
    //选中的级别
    private int currentLevel;
    private MyAdapter adapter;
    private List<String> datas = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_choose, null);
        Connector.getDatabase();
        tv_title = view.findViewById(R.id.tv_title);
        iv_back = view.findViewById(R.id.iv_back);
        rc_view = view.findViewById(R.id.rc_view);
        rc_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyAdapter(getActivity(),datas);
        rc_view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setOnClickItemListener(new MyAdapter.OnClickItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectProvince = provinces.get(position);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    selectCity = citys.get(position);
                    queryCounty();
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                }
            }
        });
        queryProvince();


    }
    /**
     * 查询省
     */
    private void queryProvince() {
        tv_title.setText("中国");
        iv_back.setVisibility(View.INVISIBLE);
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
            String url = Constant.BASEURL ;
            requestServer(url,"province");
        }

    }
    /**
     * 查询市
     */
    private void queryCity() {
        tv_title.setText(selectProvince.getProvinceName());
        iv_back.setVisibility(View.VISIBLE);
        citys = DataSupport.where("provinceid = ?", String.valueOf(selectProvince.getId())).find(City.class);
        if (citys.size() > 0) {
            datas.clear();
            for (City city : citys) {
                datas.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_CITY ;
        } else {
            //请求网络
            int provinceCode = selectProvince.getProvinceCode();
            String url = Constant.BASEURL + "/"+provinceCode;
            requestServer(url,"city");
        }

    }
    /**
     * 查询县
     */
    private void queryCounty() {
        tv_title.setText(selectCity.getCityName());
        iv_back.setVisibility(View.VISIBLE);
        countys = DataSupport.where("cityid = ?", String.valueOf(selectCity.getId())).find(County.class);
        if (countys.size() > 0) {
            datas.clear();
            for (County county : countys) {
                datas.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_COUNTY ;
        } else {
            //请求网络
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String url = Constant.BASEURL+"/"+provinceCode+"/"+cityCode;
            requestServer(url,"county");
        }

    }

    /**
     * 请求
     * @param url
     * @param type
     */
    private void requestServer(String url, final String type) {
        showProgressDialog();
        OkHttpUtils.get(url, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                boolean result = false ;
                switch (type) {
                    case "province":
                        result = ResolverUtils.ResolverProvinces(response);
                        break;
                    case "city":
                        result = ResolverUtils.ResolverCity(response,selectProvince.getId());
                        break;
                    case "county":
                        result = ResolverUtils.ResolverCounty(response,selectCity.getId());
                        break;
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
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
     * 显示等待框
     */
    public void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
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
