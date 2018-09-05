package com.rmsi.android.mast.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;

import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.domain.Option;
import com.rmsi.android.mast.util.CommonFunctions;

import java.util.ArrayList;
import java.util.List;

public class CheckBoxAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    ArrayList<String> checkedStatus = new ArrayList<String>();
    List<Option> optionsList;
    Context ctx;
    String checkedOptionIds = "";

    public CheckBoxAdapter(
            ArrayList<String> list,
            List<Option> optionsList,
            String checkedOptionIds) {
        Context ctx = CommonFunctions.getApplicationContext();
        this.checkedStatus = list;
        this.optionsList = optionsList;
        this.checkedOptionIds = checkedOptionIds;
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // mStrings = list;
        this.ctx = ctx;
    }

    public int getCount() {
        return checkedStatus.size();
    }

    public String getItem(int position) {
        return checkedStatus.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.multiselect_content, null, false);
            }
            final CheckBox checkOption = (CheckBox) convertView.findViewById(R.id.checkoption);
            checkOption.setText(checkedStatus.get(position));
            if (checkedOptionIds.contains(optionsList.get(position).getId().toString())) {
                int index = checkedOptionIds.indexOf(optionsList.get(position).getId().toString());
                if (index > -1) {
                    checkOption.setChecked(true);
                }
            } else {
                checkOption.setChecked(false);
            }

        } catch (Exception e) {
            Log.e("CheckBoxAdapter", e.getMessage());
        }

        return convertView;
    }
}
