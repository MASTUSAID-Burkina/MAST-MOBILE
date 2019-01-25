package com.rmsi.android.mast.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Classification;
import com.rmsi.android.mast.domain.ClassificationAttribute;
import com.rmsi.android.mast.domain.Property;
import com.rmsi.android.mast.domain.ResourceCustomAttribute;
import com.rmsi.android.mast.domain.SubClassificationAttribute;
import com.rmsi.android.mast.domain.TenureType;
import com.rmsi.android.mast.util.CommonFunctions;
import com.rmsi.android.mast.util.DateUtility;
import com.rmsi.android.mast.util.GuiUtility;
import com.rmsi.android.mast.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ambar.srivastava on 12/21/2017.
 */

public class CaptureResourceAttributes extends ActionBarActivity {
    private final Context context = this;
    private DbController db = DbController.getInstance(context);
    Classification classification=null;
    private List<TenureType> optionsList=new ArrayList<>();
//    Property property=null;

    private Property prop =null;
    Long featureId = 0L;
    CommonFunctions cf = CommonFunctions.getInstance();

    //List<ClassificationAttribute> classificationsList=new ArrayList<>();
    List<SubClassificationAttribute> SubclassificationsList=new ArrayList<>();
    String polytype;
    private Spinner spinnerClass,spinnerSubClass,spinnertenureType;
    private TextView txtValidationDate;
    private CheckBox chartered;
    private LinearLayout charetedFields;
    private EditText txtComment;
    private CheckBox inExploitation;
    private CheckBox validatedByCouncil;
    private boolean saveResult,saveResult1;
    List<Property> propertyList=new ArrayList<>();
    List<Property> subClassificationList=new ArrayList<>();
    List<Property> tenureList=new ArrayList<>();

    private String classi,subClassi,tenureType,tenureID,subID;
    Property property=null;

    ClassificationAttribute classificationData=new ClassificationAttribute();
    ClassificationAttribute subClassificationData=new ClassificationAttribute();
    ClassificationAttribute tenureTypenData=new ClassificationAttribute();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonFunctions.getInstance().Initialize(getApplicationContext());
        cf.loadLocale(getApplicationContext());

        setContentView(R.layout.capture_resource_attribute);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            featureId = extras.getLong("featureid");
        }
        if (featureId > 0) {
            prop = DbController.getInstance(context).getProperty(featureId);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.captureResInfo);

        if (toolbar != null)
            setSupportActionBar(toolbar);

        if (classification == null) {
            classification = new Classification();
        }
        if (prop == null) {
            prop = new Property();
        }


        spinnerClass= (Spinner) findViewById(R.id.classification_spinner);
        spinnerSubClass= (Spinner) findViewById(R.id.sub_classification_spinner);
        spinnertenureType= (Spinner) findViewById(R.id.tenure_spinner);

        txtValidationDate = (TextView)findViewById(R.id.txtValidationDate);
        chartered = (CheckBox) findViewById(R.id.chartered);
        charetedFields = (LinearLayout) findViewById(R.id.charetedFields);
        txtComment = (EditText)findViewById(R.id.txtComment);
        inExploitation = (CheckBox) findViewById(R.id.inExploitation);
        validatedByCouncil = (CheckBox) findViewById(R.id.validatedByCouncil);

        chartered.setChecked(prop.isChartered());
        txtComment.setText(prop.getComment());
        inExploitation.setChecked(prop.isInExploitation());
        validatedByCouncil.setChecked(prop.isValidatedByCouncil());

        if(chartered.isChecked()){
            charetedFields.setVisibility(View.VISIBLE);
        } else {
            charetedFields.setVisibility(View.GONE);
        }

        chartered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    charetedFields.setVisibility(View.VISIBLE);
                } else {
                    charetedFields.setVisibility(View.GONE);
                }
            }
        });

        if (prop != null && !StringUtility.isEmpty(prop.getValidationDate())) {
            txtValidationDate.setText(DateUtility.formatDateString(prop.getValidationDate()));
        }

        txtValidationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuiUtility.showDatePicker(txtValidationDate, txtValidationDate.getText().toString());
            }
        });

        GuiUtility.bindActionOnLabelChange(txtValidationDate, new Runnable() {
            @Override
            public void run() {
                prop.setValidationDate(txtValidationDate.getText().toString());
            }
        });

        List<ClassificationAttribute> classificationsList=  db.getClassification(true);

        if (classificationsList!=null) {
            spinnerClass.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, classificationsList));
            ((ArrayAdapter) spinnerClass.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }


        optionsList=  db.gettenureType(true);


        if (optionsList!=null) {
            spinnertenureType.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, optionsList));
            ((ArrayAdapter) spinnertenureType.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                classificationData=new ClassificationAttribute();
                classificationData.setAttribValue(((ClassificationAttribute) parent.getItemAtPosition(position)).getAttribValue());
                classificationData.setAttribID(((ClassificationAttribute) parent.getItemAtPosition(position)).getAttribID());



                classi=((ClassificationAttribute) parent.getItemAtPosition(position)).getAttribValue();
                prop.setClassificationValue(classi);
                prop.setClassificationId(((ClassificationAttribute) parent.getItemAtPosition(position)).getAttribID());

                getSubClassificationList(classificationData.getAttribID());


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        spinnerSubClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                subClassificationData=new ClassificationAttribute();
                subClassificationData.setAttribValue(((SubClassificationAttribute) parent.getItemAtPosition(position)).getAttribValue());
                subClassificationData.setAttribID(((SubClassificationAttribute) parent.getItemAtPosition(position)).getAttribID());


                subClassi=((SubClassificationAttribute) parent.getItemAtPosition(position)).getAttribValue();
                prop.setSubClassificationValue(subClassi);
                prop.setSubClassificationId(((SubClassificationAttribute) parent.getItemAtPosition(position)).getAttribID());
                subID=((SubClassificationAttribute) parent.getItemAtPosition(position)).getAttribID();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinnertenureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                tenureTypenData=new ClassificationAttribute();

                tenureTypenData.setAttribValue(((TenureType) parent.getItemAtPosition(position)).getAttribValue());
                tenureTypenData.setAttribID(((TenureType) parent.getItemAtPosition(position)).getAttribID().toString());
                prop.setTenureTypeValue(((TenureType) parent.getItemAtPosition(position)).getAttribValue());

                tenureID=((TenureType) parent.getItemAtPosition(position)).getAttribID().toString();
                tenureType=((TenureType) parent.getItemAtPosition(position)).getAttribValue();
                prop.setTenureTypeID(((TenureType) parent.getItemAtPosition(position)).getAttribID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        for (int i = 0; i < classificationsList.size(); i++) {
            if (classificationsList.get(i).getAttribID().equalsIgnoreCase(StringUtility.empty(prop.getClassificationId()))) {
                spinnerClass.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getAttribID().equalsIgnoreCase(StringUtility.empty(prop.getTenureTypeID()))) {
                spinnertenureType.setSelection(i);
                break;
            }
        }
    }

    private void getSubClassificationList(String classificationId) {
        SubclassificationsList=  db.getSubClassification(true,classificationId);

        if (SubclassificationsList!=null) {
            spinnerSubClass.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, SubclassificationsList));
            ((ArrayAdapter) spinnerSubClass.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        }

        for (int i = 0; i < SubclassificationsList.size(); i++) {
            if (SubclassificationsList.get(i).getAttribID().equalsIgnoreCase(StringUtility.empty(prop.getSubClassificationId()))) {
                spinnerSubClass.setSelection(i);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveData();
        }
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        if (validateBasicInfo(context, true)) {
            boolean deleteData=DbController.getInstance(context).deleteResource(featureId);

            prop.setChartered(chartered.isChecked());
            prop.setComment(txtComment.getText().toString());
            if(!prop.isChartered()){
                prop.setValidationDate(null);
                prop.setValidatedByCouncil(false);
                prop.setInExploitation(false);
            } else {
                prop.setValidationDate(txtValidationDate.getText().toString());
                prop.setValidatedByCouncil(validatedByCouncil.isChecked());
                prop.setInExploitation(inExploitation.isChecked());
            }

            boolean saveResult = DbController.getInstance(context).insertResourceAtrrValue(classificationData, featureId);
            boolean saveResultsub = DbController.getInstance(context).insertResourceAtrrValue(subClassificationData, featureId);
            boolean saveResultTenure = DbController.getInstance(context).insertResourceAtrrValue(tenureTypenData, featureId);
            boolean saveResult1 = DbController.getInstance(context).updateResourceBasic(prop);
            boolean saveResult3 = DbController.getInstance(context).insertFeature(featureId);
            boolean saveResult2 = DbController.getInstance(context).updateTenureBasic(prop, featureId);

            subClassificationList.clear();
            tenureList.clear();

            if (saveResult == true) {
                Toast.makeText(context, R.string.data_saved, Toast.LENGTH_SHORT).show();

                //Case to find whether it's an Add event or Edit event
                boolean isAddCase = false;
                isAddCase = cf.IsEditResourceAttribute(featureId, tenureID);
                //------
                if((tenureType.equalsIgnoreCase(TenureType.CODE_COLLECTIVE))||(tenureType.equalsIgnoreCase(TenureType.CODE_COMMUNITY))){
                    DbController db = DbController.getInstance(context);
                    int iGrpID=db.getOwnerCount(featureId);
                    if(iGrpID==0){
                        isAddCase=true;
                    }
                    else{
                        isAddCase=false;
                    }
                }

                //Case for Add Attribute
                if (isAddCase) {

                    if (tenureType.equalsIgnoreCase(TenureType.CODE_OPEN)) {
                        DbController db = DbController.getInstance(context);

                        List<ResourceCustomAttribute> attributesSize = db.getResAttributesSize(tenureID);
                        if (attributesSize.size() > 0) {
                            Intent intent = new Intent(context, CustomAttributeChange.class);
                            intent.putExtra("featureid", featureId);
                            intent.putExtra("classi", classi);
                            intent.putExtra("subclassi", subClassi);
                            intent.putExtra("tenure", tenureType);
                            intent.putExtra("tID", tenureID);
                            intent.putExtra("sID", subID);
                            finish();
                            startActivity(intent);

                        } else {

                            Intent intent = new Intent(context, CollectedResourceDataSummary.class);
                            intent.putExtra("featureid", featureId);
                            intent.putExtra("classi", classi);
                            intent.putExtra("subclassi", subClassi);
                            intent.putExtra("tID", tenureID);
                            intent.putExtra("tenure", tenureType);
                            finish();
                            startActivity(intent);

                        }
                    } else if (!tenureType.equalsIgnoreCase(TenureType.CODE_OPEN)) {

                        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
                        dialog.setContentView(R.layout.dialog_for_info);
                        dialog.setTitle(getResources().getString(R.string.info));
                        dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
                        Button proceed = (Button) dialog.findViewById(R.id.btn_proceed);
                        Button cancel = (Button) dialog.findViewById(R.id.btn_cancel);
                        final TextView txtTenureType = (TextView) dialog.findViewById(R.id.textView_tenure_type);
                        final TextView txtInfoMsg = (TextView) dialog.findViewById(R.id.textView_infoMsg);
                        final TextView cnfrmMsg = (TextView) dialog.findViewById(R.id.textView_cnfrm_msg);
                        cnfrmMsg.setVisibility(View.VISIBLE);
                        txtTenureType.setText(tenureType);
                        txtInfoMsg.setText(GetInfoMessage(tenureID));
                        proceed.setText(getResources().getText(R.string.yes));
                        cancel.setText(getResources().getText(R.string.no));

                        proceed.setOnClickListener(new View.OnClickListener() {
                            //Run when button is clicked
                            @Override
                            public void onClick(View v) {
                                Intent nextScreen = new Intent(context, ResourcePOI.class);

                                nextScreen.putExtra("featureid", featureId);
                                nextScreen.putExtra("classi", classi);
                                nextScreen.putExtra("subclassi", subClassi);
                                nextScreen.putExtra("tenure", tenureType);
                                nextScreen.putExtra("tID", tenureID);
                                nextScreen.putExtra("sID", subID);
                                finish();
                                startActivity(nextScreen);


                                dialog.dismiss();
                            }
                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            //Run when button is clicked
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }

                else {

                    if (tenureType.equalsIgnoreCase(TenureType.CODE_OPEN)) {
                        DbController db = DbController.getInstance(context);

                        List<ResourceCustomAttribute> attributesSize = db.getResAttributesSize(tenureID);
                        if (attributesSize.size() > 0) {
                            Intent intent = new Intent(context, CustomAttributeChange.class);
                            intent.putExtra("featureid", featureId);
                            intent.putExtra("classi", classi);
                            intent.putExtra("subclassi", subClassi);
                            intent.putExtra("tenure", tenureType);
                            intent.putExtra("tID", tenureID);
                            intent.putExtra("sID", subID);
                            finish();
                            startActivity(intent);

                        } else {

                            Intent intent = new Intent(context, CollectedResourceDataSummary.class);
                            intent.putExtra("featureid", featureId);
                            intent.putExtra("classi", classi);
                            intent.putExtra("subclassi", subClassi);
                            intent.putExtra("tID", tenureID);
                            intent.putExtra("tenure", tenureType);
                            finish();
                            startActivity(intent);

                        }
                    } else {
                        Intent nextScreen = new Intent(context, ResourcePOI.class);

                        nextScreen.putExtra("featureid", featureId);
                        nextScreen.putExtra("classi", classi);
                        nextScreen.putExtra("subclassi", subClassi);
                        nextScreen.putExtra("tenure", tenureType);
                        nextScreen.putExtra("tID", tenureID);
                        nextScreen.putExtra("sID", subID);
                        finish();
                        startActivity(nextScreen);
                    }
                }
            } else {
                Toast.makeText(context, R.string.unable_to_save_data, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private String GetInfoMessage(String tenure) {

        String strInfoMessage="Info Message";
        if (tenure.equalsIgnoreCase(TenureType.CODE_PRIVATE_JOINT)) {
            strInfoMessage=context.getResources().getString(R.string.infoMultipleJointStr);
        }
        else if(tenure.equalsIgnoreCase(TenureType.CODE_PRIVATE_INDIVIDUAL)) {
            strInfoMessage=context.getResources().getString(R.string.infoSingleOccupantStr);
        }
        else if(tenure.equalsIgnoreCase(TenureType.CODE_ORGANIZATION_INFORMAL) || tenure.equalsIgnoreCase(TenureType.CODE_ORGANIZATION_FORMAL) ) {
            strInfoMessage=context.getResources().getString(R.string.infoInformalOrganization);
        }
        else if(tenure.equalsIgnoreCase(TenureType.CODE_COMMUNITY) || tenure.equalsIgnoreCase(TenureType.CODE_COLLECTIVE) ) {
            strInfoMessage=context.getResources().getString(R.string.infoCollective);
        }

        else if(tenure.equalsIgnoreCase(TenureType.CODE_PUBLIC)) {
            strInfoMessage=context.getResources().getString(R.string.infoPublic);
        }

        else if(tenure.equalsIgnoreCase(TenureType.CODE_OPEN)) {
            strInfoMessage=context.getResources().getString(R.string.infoOpen);
        }

        return strInfoMessage;
    }


    private boolean validateBasicInfo(Context context, boolean b) {
        boolean result = true;
        String errorMessage = "";

        if (StringUtility.isEmpty(prop.getClassificationValue())) {
            errorMessage = context.getResources().getString(R.string.SelectClassificationType);
        }else if (prop.getClassificationValue().equalsIgnoreCase(context.getResources().getString(R.string.SelectOption))) {
            errorMessage = context.getResources().getString(R.string.SelectClassificationType);
        }
        else if (StringUtility.isEmpty(prop.getSubClassificationValue())) {
            errorMessage = context.getResources().getString(R.string.SelectSubClassificationType);
        }else if (prop.getSubClassificationValue().equalsIgnoreCase(context.getResources().getString(R.string.SelectOption))) {
            errorMessage = context.getResources().getString(R.string.SelectSubClassificationType);
        }else if (prop.getTenureTypeValue().equalsIgnoreCase(context.getResources().getString(R.string.SelectOption))) {
            errorMessage = context.getResources().getString(R.string.SelectTENUREType);
        }
        else if (StringUtility.isEmpty(prop.getTenureTypeValue())) {
            errorMessage = context.getResources().getString(R.string.SelectTENUREType);
        }

        if (!errorMessage.equals("")) {
            result = false;
            if (b)
                CommonFunctions.getInstance().showToast(context, errorMessage, Toast.LENGTH_LONG, Gravity.CENTER);
        }
        return result;
    }

    private void updateCount() {
        try {


                Property tmpProp = db.getProperty(featureId);


        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prop != null && !StringUtility.isEmpty(prop.getClassificationId()))
            spinnerClass.setEnabled(true);

        if (prop != null && !StringUtility.isEmpty(prop.getSubClassificationId()))
            spinnerSubClass.setEnabled(true);

        if (prop != null && !StringUtility.isEmpty(prop.getTenureTypeID()))
            spinnertenureType.setEnabled(true);

        updateCount();
                    // Don't show toolbar for unclaimed parcels

    }

}
