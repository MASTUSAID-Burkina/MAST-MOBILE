package com.rmsi.android.mast.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rmsi.android.mast.domain.ResourceCustomAttribute;
import com.rmsi.android.mast.util.CustomGisUtility;
import com.rmsi.android.mast.util.ResGuiUtility;

import java.util.List;

/**
 * Created by Ambar.Srivastava on 1/11/2018.
 */

public class ResourceCustomChangeAdapter extends BaseAdapter {
    private List<ResourceCustomAttribute> attrList;
    private Context context;
    private LayoutInflater inflater;
    private boolean readOnly;

    public ResourceCustomChangeAdapter(Context contextAct, List<ResourceCustomAttribute> attrList, boolean readOnly) {
        this.context = contextAct;
        this.readOnly = readOnly;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.attrList = attrList;
    }

    @Override
    public int getCount() {
        return attrList.size();
    }

    @Override
    public Object getItem(int position) {
        return attrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return CustomGisUtility.createViewFromAttribute((ResourceCustomAttribute) getItem(position), inflater, false, readOnly);
}

}