package com.liuyuan.nyy.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.liuyuan.nyy.R;

import java.util.ArrayList;

/**
 * Created by admin on 2017/3/8.
 * ListView使用的Adapter
 */

public class MyAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater inflater;
    public ArrayList<String> arr;

    public MyAdapter(Context context, ArrayList<String> array) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
        arr = array;
    }

    @Override
    public int getCount() {
        if (arr != null)
            return arr.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int arg0) {
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public void setArray(ArrayList<String> list) {
        this.arr = list;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_group, null);
        }
        final TextView edit = (TextView) view.findViewById(R.id.group_item_content);
        edit.setText(arr.get(arr.size() - position - 1)); // 在重构adapter的时候不至于数据错乱
        return view;
    }
}
