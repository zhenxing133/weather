package com.weather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yuan.zhen.xing on 2018-05-04.
 */

public class MyAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private LayoutInflater inflater;
    private Context mContext;
    private List<String> datas ;
    public MyAdapter(Context context,List<String> datas) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.datas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adapter_choosefrg, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tv_content.setOnClickListener(this);
        viewHolder.tv_content.setTag(position);
        viewHolder.tv_content.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onClick(View view) {
        if (this.onClickItemListener != null) {
            onClickItemListener.onItemClick(view, (int) view.getTag());
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_content ;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_content = itemView.findViewById(R.id.tv_content);
        }
    }

    private OnClickItemListener onClickItemListener;

    public interface OnClickItemListener {
        void onItemClick(View v, int position);
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }


}
