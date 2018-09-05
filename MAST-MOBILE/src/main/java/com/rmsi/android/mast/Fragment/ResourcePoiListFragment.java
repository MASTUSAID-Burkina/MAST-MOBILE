package com.rmsi.android.mast.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rmsi.android.mast.activity.AddPersonActivity;
import com.rmsi.android.mast.activity.ListActivity;
import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.adapter.PersonListAdapter;
import com.rmsi.android.mast.adapter.PersonOfInterestListAdapter;
import com.rmsi.android.mast.adapter.ResourcePersonOfInterestListAdapter;
import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Gender;

import com.rmsi.android.mast.domain.RelationshipType;
import com.rmsi.android.mast.domain.ResourcePersonOfInterest;

import com.rmsi.android.mast.util.DateUtility;
import com.rmsi.android.mast.util.GuiUtility;


import java.util.List;



/**
 * Created by Ambar.Srivastava on 1/13/2018.
 */

public class ResourcePoiListFragment extends ListFragment implements ListActivity {
    private Context context;
    private ResourcePersonOfInterestListAdapter adapter;
    private List<ResourcePersonOfInterest> persons;
    private boolean readOnly = false;

    public ResourcePoiListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        return view;
    }

    /**
     * Sets persons list
     */
    public void setPersons(List<ResourcePersonOfInterest> persons, boolean readOnly) {
        this.persons = persons;
        this.readOnly = readOnly;
        adapter = new ResourcePersonOfInterestListAdapter(context, this, persons);
        setListAdapter(adapter);
        refresh();
    }

    @Override
    public void showPopup(View v, int position) {
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();

        inflater.inflate(R.menu.attribute_listing_options_for_poi, popup.getMenu());

        final ResourcePersonOfInterest person = persons.get(position);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        edit(person);
                        return true;
                    case R.id.delete_entry:
                        delete(person);
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (!readOnly) {
            popup.show();
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    private void edit(final ResourcePersonOfInterest person) {
        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setContentView(R.layout.dialog_person_of_interest);
        dialog.setTitle(getResources().getString(R.string.nextKin));
        dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;

        Button save = (Button) dialog.findViewById(R.id.btn_ok);
        final EditText firstName = (EditText) dialog.findViewById(R.id.editTextFirstName);
        final EditText lastName = (EditText) dialog.findViewById(R.id.editTextLastName);
        final Spinner genderSpinner = (Spinner) dialog.findViewById(R.id.spinnerGender);
        final TextView txtDob = (TextView) dialog.findViewById(R.id.txtDob);
        LinearLayout extraFields = (LinearLayout) dialog.findViewById(R.id.extraLayout);
        extraFields.setVisibility(View.VISIBLE);

        save.setText(getResources().getString(R.string.save));

        DbController db = DbController.getInstance(context);

        List<RelationshipType> relTypes = db.getRelationshipTypes(true);
        List<Gender> genders = db.getGenders(true);

        genderSpinner.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, genders));
        ((ArrayAdapter) genderSpinner.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        txtDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuiUtility.showDatePicker(txtDob, person.getDob());
            }
        });

        // Init fields
        txtDob.setText(DateUtility.formatDateString(person.getDob()));

        for (int i = 0; i < relTypes.size(); i++) {
            if (relTypes.get(i).getCode() > 0 && relTypes.get(i).getCode() == person.getRelationshipId()) {
                break;
            }
        }

        for (int i = 0; i < genders.size(); i++) {
            if (genders.get(i).getCode() > 0 && genders.get(i).getCode() == person.getGenderId()) {
                genderSpinner.setSelection(i);
                break;
            }
        }

        String[] separated = person.getName().split(" ");

        if (separated.length == 1) {
            String fName = separated[0];
            firstName.setText(fName);
            lastName.setText("");
        } else if (separated.length == 2) {
            String fName = separated[0];
            String mName = separated[1];
            firstName.setText(fName);
            lastName.setText("");
        } else if (separated.length == 3) {
            String fName = separated[0];
            String mName = separated[1];
            String lName = separated[2];
            firstName.setText(fName);
            lastName.setText(lName);
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = firstName.getText().toString() + " " + lastName.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    person.setName(name);
                    if(genderSpinner.getSelectedItem() != null)
                        person.setGenderId(((Gender)genderSpinner.getSelectedItem()).getCode());
                    else
                        person.setGenderId(0);

                    person.setDob(txtDob.getText().toString());

                    if (DbController.getInstance(context).saveResPersonOfInterest(person)) {
                        Toast.makeText(context, "Edited", Toast.LENGTH_LONG).show();
                        refresh();
                    } else {
                        Toast.makeText(context, getResources().getString(R.string.UnableToSave), Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, getResources().getString(R.string.nextOfKin), Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
    }

    private void delete(ResourcePersonOfInterest person)
    {
        if (DbController.getInstance(context).deleteResPersonOfInterest(person.getId())) {
            persons.remove(person);
            Toast.makeText(context,getResources().getString(R.string.deleted), Toast.LENGTH_LONG).show();
            refresh();
        } else {
            Toast.makeText(context, getResources().getString(R.string.unable_delete), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes the list
     */
    public void refresh() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
