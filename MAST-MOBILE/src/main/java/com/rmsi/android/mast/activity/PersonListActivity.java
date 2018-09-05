package com.rmsi.android.mast.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rmsi.android.mast.Fragment.PersonListFragment;
import com.rmsi.android.mast.Fragment.PoiListFragment;
import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Gender;
import com.rmsi.android.mast.domain.Person;
import com.rmsi.android.mast.domain.PersonOfInterest;
import com.rmsi.android.mast.domain.Property;
import com.rmsi.android.mast.domain.RelationshipType;
import com.rmsi.android.mast.domain.ShareType;
import com.rmsi.android.mast.util.CommonFunctions;
import com.rmsi.android.mast.util.GuiUtility;

import java.util.List;

public class PersonListActivity extends ActionBarActivity implements PersonListFragment.OnPersonDeletedListener {

    Button addnewPerson, btnNext, btnBack;
    Context context;
    Long featureId = 0L;
    CommonFunctions cf = CommonFunctions.getInstance();
    String msg, warning;
    int position;
    private boolean readOnly = false;
    String warningStr, infoStr, shareTypeStr;
    String saveStr, backStr;
    private Property property;
    private PersonListFragment personsFragment;
    private PoiListFragment poiFragment;
    private TextView lblShareType;
    private LinearLayout pnlPoi;
    private Long rightId = 0L;
    private ShareType shareType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            CommonFunctions.getInstance().Initialize(getApplicationContext());
        } catch (Exception e) {
        }
        cf.loadLocale(getApplicationContext());

        context = this;

        warningStr = getResources().getString(R.string.warning);
        infoStr = getResources().getString(R.string.info);
        shareTypeStr = getResources().getString(R.string.shareType);
        saveStr = getResources().getString(R.string.save);
        backStr = getResources().getString(R.string.back);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            featureId = extras.getLong("featureid");
            rightId = extras.getLong("rightId");
        }

        readOnly = CommonFunctions.isFeatureReadOnly(featureId);
        setContentView(R.layout.activity_list);

        pnlPoi = (LinearLayout) findViewById(R.id.pnlPoi);
        lblShareType = (TextView) findViewById(R.id.tenureType_lbl);
        addnewPerson = (Button) findViewById(R.id.btn_addNewPerson);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnBack = (Button) findViewById(R.id.btnBack);
        personsFragment = (PersonListFragment) getFragmentManager().findFragmentById(R.id.compPersonsList);
        poiFragment = (PoiListFragment) getFragmentManager().findFragmentById(R.id.compPoiList);

        personsFragment.addDeleteListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_person);
        if (toolbar != null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (readOnly) {
            addnewPerson.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
        }

        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!readOnly) {
                    if (property.validatePersonsList(context, true)) {
                        Intent myIntent = new Intent(context, MediaListActivity.class);
                        myIntent.putExtra("featureid", featureId);
                        finish();
                        startActivity(myIntent);
                    }
                }
            }
        });

        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addnewPerson.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(property.getRight().getNaturalPersons() == null || property.getRight().getNaturalPersons().size() < 1) {
                        Intent myIntent = new Intent(context, AddPersonActivity.class);
                        myIntent.putExtra("groupid", 0L);
                        myIntent.putExtra("featureid", featureId);
                        myIntent.putExtra("rightId", property.getRight().getId());
                        startActivity(myIntent);
                    } else {
                        if(shareType != null && shareType.getCode() == ShareType.TYPE_COLLECTIVE){
                            // Add POI
                            DbController db = DbController.getInstance(context);

                            final Dialog dialog = new Dialog(context, R.style.DialogTheme);
                            dialog.setContentView(R.layout.dialog_person_of_interest);
                            dialog.setTitle(getResources().getString(R.string.nextKin));
                            dialog.getWindow().getAttributes().width = LayoutParams.MATCH_PARENT;

                            Button save = (Button) dialog.findViewById(R.id.btn_ok);
                            final EditText firstName = (EditText) dialog.findViewById(R.id.editTextFirstName);
                            final EditText lastName = (EditText) dialog.findViewById(R.id.editTextLastName);
                            final Spinner genderSpinner = (Spinner) dialog.findViewById(R.id.spinnerGender);
                            final TextView txtDob = (TextView) dialog.findViewById(R.id.txtDob);
                            LinearLayout extraFields = (LinearLayout) dialog.findViewById(R.id.extraLayout);
                            extraFields.setVisibility(View.VISIBLE);

                            genderSpinner.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, db.getGenders(true)));
                            ((ArrayAdapter) genderSpinner.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            txtDob.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    GuiUtility.showDatePicker(txtDob, "");
                                }
                            });

                            save.setText(saveStr);

                            save.setOnClickListener(new OnClickListener() {
                                //Run when button is clicked
                                @Override
                                public void onClick(View v) {
                                    String poi_fName = firstName.getText().toString();
                                    String poi_lastName = lastName.getText().toString();
                                    int poi_gender = 0;

                                    if (genderSpinner.getSelectedItem() != null){
                                        poi_gender = ((Gender) genderSpinner.getSelectedItem()).getCode();
                                    }
                                    if (!TextUtils.isEmpty(poi_fName) && !TextUtils.isEmpty(poi_lastName) && poi_gender > 0) {
                                        PersonOfInterest poi = new PersonOfInterest();
                                        poi.setFeatureId(featureId);
                                        if (genderSpinner.getSelectedItem() != null)
                                            poi.setGenderId(poi_gender);
                                        poi.setDob(txtDob.getText().toString());
                                        poi.setFirstName(poi_fName);
                                        poi.setLastName(poi_lastName);

                                        boolean result = DbController.getInstance(context).savePersonOfInterest(poi);
                                        if (result) {
                                            property.getPersonOfInterests().add(poi);
                                            poiFragment.refresh();
                                            msg = getResources().getString(R.string.AddedSuccessfully);
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                        } else {
                                            warning = getResources().getString(R.string.UnableToSave);
                                            Toast.makeText(context, warning, Toast.LENGTH_LONG).show();
                                        }
                                        dialog.dismiss();
                                    } else {
                                        msg = getResources().getString(R.string.enter_poi_details);
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            dialog.show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh(){
        DbController db = DbController.getInstance(context);
        property = db.getProperty(featureId);

        if (property == null || property.getRight() == null) {
            cf.showToast(context, R.string.RightNotFound, Toast.LENGTH_SHORT);
            return;
        }

        if (property.getRight().getShareTypeId() > 0) {
            shareType = db.getShareType(property.getRight().getShareTypeId());
            if (shareType != null) {
                if(shareType.getCode() == ShareType.TYPE_INDIVIDUAL){
                    pnlPoi.setVisibility(View.GONE);
                    addnewPerson.setText(getResources().getString(R.string.AddPerson));

                    if(property.getRight().getNaturalPersons() != null && property.getRight().getNaturalPersons().size() > 0){
                        addnewPerson.setVisibility(View.GONE);
                    } else {
                        addnewPerson.setVisibility(View.VISIBLE);
                    }
                } else {
                    pnlPoi.setVisibility(View.VISIBLE);
                    if(property.getRight().getNaturalPersons() != null && property.getRight().getNaturalPersons().size() > 0){
                        addnewPerson.setText(getResources().getString(R.string.addPOI));
                    } else {
                        addnewPerson.setText(getResources().getString(R.string.AddRepresentative));
                    }
                }
                lblShareType.setText(shareTypeStr + ": " + shareType.toString());
            }
        }

        personsFragment.setPersons(property.getRight().getNaturalPersons(), readOnly);
        poiFragment.setPersons(property.getPersonOfInterests(), readOnly);
    }

    @Override
    public void onPersonDeleted() {
        refresh();
    }
}
