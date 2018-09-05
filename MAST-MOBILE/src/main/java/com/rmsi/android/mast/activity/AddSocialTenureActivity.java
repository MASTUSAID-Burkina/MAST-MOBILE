package com.rmsi.android.mast.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Attribute;
import com.rmsi.android.mast.domain.ClaimType;
import com.rmsi.android.mast.domain.Person;
import com.rmsi.android.mast.domain.RelationshipType;
import com.rmsi.android.mast.domain.Right;
import com.rmsi.android.mast.domain.ShareType;
import com.rmsi.android.mast.domain.TitleType;
import com.rmsi.android.mast.util.CommonFunctions;
import com.rmsi.android.mast.util.DateUtility;
import com.rmsi.android.mast.util.GuiUtility;

import java.util.List;

public class AddSocialTenureActivity extends ActionBarActivity {

    private final Context context = this;
    private Button btnSave, btnCancel;
    private CommonFunctions cf = CommonFunctions.getInstance();
    private long featureId = 0;
    private boolean readOnly = false;
    private Right right = null;
    private EditText txtCertNumber;
    private TextView txtCertDate;
    private LinearLayout certLayout;
    private Spinner spinnerShareType;
    private Spinner spinnerCertType;
    private Person person = null;
    private ClaimType claimType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            CommonFunctions.getInstance().Initialize(getApplicationContext());
        } catch (Exception e) {
        }
        cf.loadLocale(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            featureId = extras.getLong("featureid");
        }

        final DbController db = DbController.getInstance(context);
        readOnly = CommonFunctions.isFeatureReadOnly(featureId);

        setContentView(R.layout.activity_social_tenure_information);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.add_social_tenure);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        certLayout = (LinearLayout) findViewById(R.id.certLayout);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        spinnerShareType = (Spinner) findViewById(R.id.spinnerShareType);
        txtCertNumber = (EditText) findViewById(R.id.txtCertNumber);
        txtCertDate = (TextView) findViewById(R.id.txtCertDate);
        spinnerCertType = (Spinner) findViewById(R.id.spinnerCertType);

        // Get right
        if (featureId > 0) {
            right = db.getRightByProp(featureId);
        }

        // Populate and setup spinners
        claimType = db.getPropClaimType(featureId);
        List<ShareType> shareTypes = db.getShareTypes(right == null || right.getShareTypeId() < 1);
        final List<TitleType> titleTypes = db.getTitleTypes(right == null || right.getCertTypeId() == null || right.getCertTypeId() < 1);

        if (claimType != null) {
            if (claimType.getCode().equals(ClaimType.TYPE_EXISTING_CLAIM)) {
                certLayout.setVisibility(View.VISIBLE);
            }
        }

        spinnerShareType.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, shareTypes));
        ((ArrayAdapter) spinnerShareType.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCertType.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, titleTypes));
        ((ArrayAdapter) spinnerCertType.getAdapter()).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Setup certificate and area fields
        if (certLayout.getVisibility() != View.GONE) {
            GuiUtility.bindActionOnFieldChange(txtCertNumber, new Runnable() {
                @Override
                public void run() {
                    right.setCertNumber(txtCertNumber.getText().toString());
                }
            });

            txtCertDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GuiUtility.showDatePicker(txtCertDate, right.getCertDate());
                }
            });

            GuiUtility.bindActionOnLabelChange(txtCertDate, new Runnable() {
                @Override
                public void run() {
                    right.setCertDate(txtCertDate.getText().toString());
                }
            });

            GuiUtility.bindActionOnSpinnerChange(spinnerCertType, new Runnable() {
                @Override
                public void run() {
                    int id = ((TitleType) spinnerCertType.getSelectedItem()).getId();
                    if(id == 0){
                        right.setCertTypeId(null);
                    } else {
                        right.setCertTypeId(id);
                    }
                }
            });
        }

        GuiUtility.bindActionOnSpinnerChange(spinnerShareType, new Runnable() {
            @Override
            public void run() {
                int code = ((ShareType) spinnerShareType.getSelectedItem()).getCode();
                right.setShareTypeId(code);
            }
        });

        if (right == null) {
            right = new Right();
            right.setFeatureId(featureId);
         } else {
            // Set fields value
            for (int i = 0; i < shareTypes.size(); i++) {
                if (shareTypes.get(i).getCode() > 0 && shareTypes.get(i).getCode() == right.getShareTypeId()) {
                    spinnerShareType.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < titleTypes.size(); i++) {
                if (titleTypes.get(i).getId() > 0 && titleTypes.get(i).getId() == right.getCertTypeId()) {
                    spinnerCertType.setSelection(i);
                    break;
                }
            }

            txtCertNumber.setText(right.getCertNumber());
            txtCertDate.setText(DateUtility.formatDateString(right.getCertDate()));
        }

        // Populate attributes
        if (right.getAttributes() == null || right.getAttributes().size() < 1) {
            // Pull attributes for social tenure
            right.setAttributes(db.getAttributesByType(Attribute.TYPE_TENURE));
        }

        if (right.getAttributes() != null) {
            GuiUtility.appendLayoutWithAttributes(mainLayout, right.getAttributes(), readOnly);
        }

        // Disable fields and buttons for adjudicator
        if (readOnly) {
            btnSave.setVisibility(View.GONE);
            spinnerShareType.setEnabled(false);
            txtCertDate.setEnabled(false);
            txtCertNumber.setEnabled(false);
            spinnerCertType.setEnabled(false);
        }

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }


    public void saveData() {
        if (readOnly) {
            return;
        }

        if (!right.validate(context, claimType.getCode(), true)) {
            return;
        }

        try {
            if (!claimType.getCode().equals(ClaimType.TYPE_EXISTING_CLAIM)) {
                // Clear certificate fields
                right.setCertTypeId(null);
                right.setCertDate(null);
                right.setCertNumber(null);
            }
            DbController db = DbController.getInstance(context);
            boolean saveResult = db.saveRight(right);

            if (!saveResult) {
                Toast.makeText(context, R.string.unable_to_save_data, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent nextScreen = new Intent(context, PersonListActivity.class);
            nextScreen.putExtra("featureid", featureId);
            nextScreen.putExtra("rightId", right.getId());
            finish();
            startActivity(nextScreen);
        } catch (Exception e) {
            cf.appLog("", e);
            e.printStackTrace();
            Toast.makeText(context, R.string.unable_to_save_data, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Disable share type if there are persons already added
        Right rightTmp = DbController.getInstance(context).getRightByProp(featureId);
        if (rightTmp != null && (rightTmp.getNaturalPersons().size() > 0))
            spinnerShareType.setEnabled(false);
        else if (!readOnly)
            spinnerShareType.setEnabled(true);
    }
}