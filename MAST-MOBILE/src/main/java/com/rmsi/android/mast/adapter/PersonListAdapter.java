package com.rmsi.android.mast.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rmsi.android.mast.activity.ListActivity;
import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Person;

import java.util.List;

public class PersonListAdapter extends BaseAdapter {
    private List<Person> persons;
    private LayoutInflater lInflator;
    Context context;
    ListActivity activityObj;

    public PersonListAdapter(Context context, ListActivity activityObj, List<Person> persons) {
        this.context = context;
        this.persons = persons;
        this.lInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activityObj = activityObj;
    }

    public int getCount() {
        return persons.size();
    }

    public Person getItem(int position) {
        return persons.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Person person = getItem(position);
        DbController db = DbController.getInstance(context);
        int isDipsuteValue=db.getDisputpersonTypefromFeature(person.getFeatureId());
        if (isDipsuteValue==0) {

            if (convertView == null) {
                convertView = lInflator.inflate(R.layout.item_list_row_with_attachment, parent, false);
            }

            TextView textViewItem = (TextView) convertView.findViewById(R.id.surveyData);
            ImageButton options = (ImageButton) convertView.findViewById(R.id.optionsButton);
            ImageView imgViewItem = (ImageView) convertView.findViewById(R.id.img_attachment);

            textViewItem.setText(person.getFullName(context));
            if (person.getMedia() != null && person.getMedia().size() > 0) {
                imgViewItem.setVisibility(View.VISIBLE);
            } else {
                imgViewItem.setVisibility(View.INVISIBLE);
            }

            options.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityObj.showPopup(v, position);
                }
            });
        }
        else{
            if (convertView == null) {
                convertView = lInflator.inflate(R.layout.item_list_row_with_attachment, parent, false);
            }

            TextView textViewItem = (TextView) convertView.findViewById(R.id.surveyData);
            ImageButton options = (ImageButton) convertView.findViewById(R.id.optionsButton);
            ImageView imgViewItem = (ImageView) convertView.findViewById(R.id.img_attachment);

            Long  grpID=  person.getAttributes().get(position).getGroupId();
            int disputeType=db.getDisPuteType(grpID);
            if (disputeType==1165 || disputeType==1178) {
                textViewItem.setText(person.getFullName(context) + " (" + "Owner" + ")");
            }
            else if (disputeType==1166 || disputeType==1179){
                textViewItem.setText(person.getFullName(context) + " (" + "Disputed Person" + ")");
            }
            if (person.getMedia() != null && person.getMedia().size() > 0) {
                imgViewItem.setVisibility(View.VISIBLE);
            } else {
                imgViewItem.setVisibility(View.INVISIBLE);
            }

            options.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityObj.showPopup(v, position);
                }
            });
        }



//        else if (isDipsuteValue==1){
//            if (convertView == null) {
//                convertView = lInflator.inflate(R.layout.item_list_row_with_attachment, parent, false);
//            }
//
//            TextView textViewItem = (TextView) convertView.findViewById(R.id.surveyData);
//            ImageButton options = (ImageButton) convertView.findViewById(R.id.optionsButton);
//            ImageView imgViewItem = (ImageView) convertView.findViewById(R.id.img_attachment);
//
//            Long  grpID=  person.getAttributes().get(position).getGroupId();
//            int disputeType=db.getDisPuteType(grpID);
//            if (disputeType==1165) {
//                textViewItem.setText(person.getFullName(context) + " (" + "Owner" + ")");
//            }
//            else if (disputeType==1166){
//                textViewItem.setText(person.getFullName(context) + " (" + "Disputed Person" + ")");
//            }
//            if (person.getMedia() != null && person.getMedia().size() > 0) {
//                imgViewItem.setVisibility(View.VISIBLE);
//            } else {
//                imgViewItem.setVisibility(View.INVISIBLE);
//            }
//
//            options.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    activityObj.showPopup(v, position);
//                }
//            });
//        }
        return convertView;

    }
}
