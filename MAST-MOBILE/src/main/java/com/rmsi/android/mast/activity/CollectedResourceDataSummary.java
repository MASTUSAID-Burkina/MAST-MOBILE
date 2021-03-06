package com.rmsi.android.mast.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rmsi.android.mast.adapter.SummaryAdapater;
import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Attribute;
import com.rmsi.android.mast.domain.Classification;
import com.rmsi.android.mast.domain.ProjectSpatialDataDto;
import com.rmsi.android.mast.domain.Property;
import com.rmsi.android.mast.domain.Summary;
import com.rmsi.android.mast.domain.TenureInformation;
import com.rmsi.android.mast.util.CommonFunctions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class CollectedResourceDataSummary  extends ActionBarActivity {
    private final Context context = this;
    private DbController db = DbController.getInstance(context);
    Long featureId = 0L;
    private String classi,subClassi,tenureType;
    private TextView textViewType,textViewSubType,textViewResTenureType,textViewFirstName,textViewMiddleName,textViewLastName,textViewVillege,textViewRegion,textViewPOICount,
    textViewCountry,textViewGeoType;
    CommonFunctions cf = CommonFunctions.getInstance();
    Classification classification=null;
    private Property property = null;
    private Button buttonClose;
    private SummaryAdapater summaryAdapater,summaryAdapaterCustom;
    String geoType,coordinates;
    private List<Summary> attributes;
    private List<Summary> attributesCustom;
    private ListView listView,listviewcustom;
    TextView textViewCustom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonFunctions.getInstance().Initialize(getApplicationContext());


        setContentView(R.layout.collecte_data_summary);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            featureId = extras.getLong("featureid");
            classi=extras.getString("classi");
            subClassi=extras.getString("subclassi");
            tenureType=extras.getString("tenure");
            property = db.getProperty(featureId);
            attributes = db.getResourceTenureInfo(featureId);

            attributesCustom = db.getResourceCustomInfo(featureId);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(context.getResources().getString(R.string.title_activity_data_summary));

        if (toolbar != null)
            setSupportActionBar(toolbar);

        if (classification == null) {
            classification = new Classification();
        }
        textViewType= (TextView) findViewById(R.id.Type);
        textViewCustom= (TextView) findViewById(R.id.cutom);
        textViewSubType=(TextView) findViewById(R.id.subType);
        textViewResTenureType=(TextView) findViewById(R.id.resourceTenureType);
        textViewPOICount= (TextView) findViewById(R.id.count_poi);

        listView= (ListView) findViewById(R.id.tenureInfo);
        listviewcustom=(ListView) findViewById(R.id.customInfo);
//        textViewMiddleName=(TextView) findViewById(R.id.middleName);
//        textViewLastName=(TextView) findViewById(R.id.lastName);

        buttonClose= (Button) findViewById(R.id.btnClose);
        Button buttonedit= (Button) findViewById(R.id.edit_attributes);



        buttonedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, CaptureResourceAttributes.class);
                myIntent.putExtra("featureid", featureId);
                finish();
                startActivity(myIntent);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        if (attributes.size()!=0){
            summaryAdapater=new SummaryAdapater(CollectedResourceDataSummary.this,attributes);
            listView.setAdapter(summaryAdapater);
        }

        if (attributesCustom.size()!=0){
            summaryAdapaterCustom=new SummaryAdapater(CollectedResourceDataSummary.this,attributesCustom);
            listviewcustom.setAdapter(summaryAdapaterCustom);
        }else{
            textViewCustom.setVisibility(View.GONE);
            listviewcustom.setVisibility(View.GONE);
        }

        textViewPOICount.setText(context.getResources().getString(R.string.numOfPois) + ": " +""+cf.getResourcePoiCount(featureId));


        getResourcesAttributes();
    }

    private void getResourcesAttributes() {

        try {


           Property property=db.getClassi(featureId);
            textViewType.setText(property.getClassificationValue());
            textViewSubType.setText(property.getSubClassificationValue());
            textViewResTenureType.setText(property.getTenureTypeValue());

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            coordinates=db.getGeoCordinates(featureId);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void saveData() {
        //Intent intent = new Intent(CollectedResourceDataSummary.this, CaptureDataMapActivity.class);
        Intent intent = new Intent(CollectedResourceDataSummary.this, LandingPageActivity.class);
        intent.putExtra("GEOTYPE",geoType);
        intent.putExtra("featID",featureId);

        intent.putExtra("CORD",coordinates);
        Toast.makeText(context,R.string.data_saved,Toast.LENGTH_SHORT).show();
        finish();
        startActivity(intent);
    }
}
