package com.rmsi.android.mast.util;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.adapter.CheckBoxAdapter;
import com.rmsi.android.mast.adapter.SpinnerAdapter;
import com.rmsi.android.mast.db.DbController;
import com.rmsi.android.mast.domain.Attribute;
import com.rmsi.android.mast.domain.Option;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Contains various GUI functions
 */
public class GuiUtility {

    private static boolean isMandatorty = true;
    private static int isDipute = 0;

    /**
     * Appends LinearLayout with provided attributes
     *
     * @param layout     Layout to append
     * @param attributes List of attributes
     */
    public static void appendLayoutWithAttributes(LinearLayout layout, List<Attribute> attributes, boolean readOnly) {
        isDipute = 0;//for natural
        if (attributes != null && attributes.size() > 0) {
            LayoutInflater inflater = (LayoutInflater) layout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (Attribute attr : attributes) {
                layout.addView(createViewFromAttribute(attr, inflater, true, readOnly));
            }
        }
    }

    public static void appendLayoutWithAttributesByDisputed(LinearLayout layout, List<Attribute> attributes, boolean readOnly) {
        isDipute = 1;//for Disputed claim person
        if (attributes != null && attributes.size() > 0) {
            LayoutInflater inflater = (LayoutInflater) layout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (Attribute attr : attributes) {
                layout.addView(createViewFromAttribute(attr, inflater, true, readOnly));
            }
        }
    }


    /**
     * Create View item based on provided attribute
     *
     * @param attribute Attribute to be used for creating View item
     * @param inflater  Layout inflater to be used for getting appropriate layout
     */
    public static View createViewFromAttribute(Attribute attribute, LayoutInflater inflater, boolean addSeparator, boolean readOnly) {
        View container = null;

        if (attribute.getControlType() == Attribute.CONTROL_TYPE_STIRNG) {
            container = inflater.inflate(R.layout.item_edit_text, null, false);
            attribute.setView(createInputRow(container, attribute, readOnly));
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_DATE) {
            container = inflater.inflate(R.layout.item_date, null, false);
            attribute.setView(createTimePickerRow(container, attribute, readOnly));
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_BOOLEAN) {
            container = inflater.inflate(R.layout.item_spinner, null, false);
            attribute.setView(createSpinnerViewForBoolean(container, attribute, readOnly));
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_NUMBER) {
            container = inflater.inflate(R.layout.item_edittext_numeric, null, false);
            attribute.setView(createInputRow(container, attribute, readOnly));
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_SPINNER) {
            container = inflater.inflate(R.layout.item_spinner, null, false);
            attribute.setView(createSpinnerViewFromArray(container, attribute, readOnly));
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_MULTISELECT) {
            container = inflater.inflate(R.layout.item_button, null, false);
            attribute.setView(createMultiselect(container, attribute, readOnly));
        }

        if (container != null && !addSeparator) {
            View separator = (View) container.findViewById(R.id.separator);
            if (separator != null)
                separator.setVisibility(View.GONE);
        }
        return container;
    }

    private static View createInputRow(View container, final Attribute attribute, boolean readOnly) {
        TextView field = (TextView) container.findViewById(R.id.field);
        field.setText(attribute.getLabelName());
        final EditText fieldValue = (EditText) container.findViewById(R.id.fieldValue);
        fieldValue.setTag(attribute.getId());

        if (readOnly) {
            fieldValue.setEnabled(false);
        }

        if (attribute.getValue() != null) {
            fieldValue.setText(attribute.getValue(), TextView.BufferType.EDITABLE);
        } else {
            fieldValue.setEnabled(true);
            fieldValue.setText("", TextView.BufferType.EDITABLE);
        }

        bindActionOnFieldChange(fieldValue, new Runnable() {
            @Override
            public void run() {
                Long attribId = (Long) fieldValue.getTag();
                if (attribute.getId() == attribId) {
                    attribute.setValue(fieldValue.getText().toString());
                }
            }
        });
        return fieldValue;
    }

    private static View createTimePickerRow(View container, final Attribute attribute, boolean readOnly) {
        TextView field = (TextView) container.findViewById(R.id.field);
        field.setText(attribute.getLabelName());

        final TextView textDatePicker = (TextView) container.findViewById(R.id.textview_datepicker);
        textDatePicker.setTag(attribute.getId());

        if (readOnly) {
            textDatePicker.setEnabled(false);
        }

        if (!StringUtility.isEmpty(attribute.getValue())) {
            textDatePicker.setText(DateUtility.formatDateString(attribute.getValue()));
        }

        textDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(textDatePicker, attribute.getValue());
            }
        });

        bindActionOnLabelChange(textDatePicker, new Runnable() {
            @Override
            public void run() {
                Long attribId = (Long) textDatePicker.getTag();
                if (attribute.getId() == attribId) {
                    attribute.setValue(textDatePicker.getText().toString());
                }
            }
        });

        return textDatePicker;
    }

    /**
     * Binds runnable action to the TextView field event - afterTextChanged
     *
     * @param label  TextView field to bind to
     * @param action Runnable action to execute on field text change event
     */
    public static void bindActionOnLabelChange(TextView label, final Runnable action) {
        label.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    action.run();
                } catch (Exception e) {
                }
            }
        });
    }

    /**
     * Binds runnable action to the EditText field change events, including OnFocusChange and TextChanged
     *
     * @param field  EditText field to bind to
     * @param action Runnable action to execute on field text change and losing focus event
     */
    public static void bindActionOnFieldChange(EditText field, final Runnable action) {
        field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        action.run();
                    } catch (Exception e) {
                    }
                }
            }
        });

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    action.run();
                } catch (Exception e) {
                }
            }
        });
    }

    /**
     * Binds runnable action to the Spinner field change event onItemSelected
     *
     * @param spinner Spinner field to bind to
     * @param action  Runnable action to execute on spinner change
     */
    public static void bindActionOnSpinnerChange(Spinner spinner, final Runnable action) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                action.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    /**
     * Shows custom date picker, attached to the TextView control
     *
     * @param textView   TextView control to attach date picker to
     * @param dateString Initial date string to display
     */
    public static void showDatePicker(final TextView textView, String dateString) {
        final Dialog customTimePicker = new Dialog(textView.getContext(), R.style.DialogTheme);
        customTimePicker.setTitle("Select Date");
        customTimePicker.setContentView(R.layout.dialog_time_picker);
        customTimePicker.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        final DatePicker datepicker = (DatePicker) customTimePicker.findViewById(R.id.datePicker);

        Button btnSet = (Button) customTimePicker.findViewById(R.id.button_set);
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtility.getDate(dateString));

        datepicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
        if (!StringUtility.isEmpty(dateString))
            textView.setText(DateUtility.formatDate(cal.getTime()));

        customTimePicker.show();

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(DateUtility.getStringDateFromDatePicker(datepicker));
                customTimePicker.dismiss();
            }
        });
    }

    private static TextView createMultiselect(final View container, final Attribute attribute, boolean readOnly) {
        TextView attributeLable = (TextView) container.findViewById(R.id.field);
        TextView selectedOptionsText = (TextView) container.findViewById(R.id.selectoptionsText);
        final EditText otherExistingUSe = (EditText) container.findViewById(R.id.editTextOtherExistingUse);

        selectedOptionsText.setEnabled(!readOnly);
        otherExistingUSe.setEnabled(!readOnly);

        attributeLable.setText(attribute.getLabelName());
        List<String> optionTextList = new ArrayList<String>();
        String fieldValue = attribute.getValue();

        if (fieldValue != null && !fieldValue.equals("")) {
            String optionIds[] = fieldValue.split(",");
            for (int i = 0; i < optionIds.length; i++) {
                String optionId = optionIds[i];
                optionTextList.add(DbController.getInstance(CommonFunctions.getApplicationContext()).getOptionText(optionId));
            }

            selectedOptionsText.setText(optionTextList.toString());

            if (fieldValue.contains(Option.ID_OTHER_USE)) {
                otherExistingUSe.setVisibility(View.VISIBLE);
                otherExistingUSe.setText(DbController.getInstance(CommonFunctions.getApplicationContext())
                        .getPropOtherUse(attribute.getFeatureId()));
            } else
                otherExistingUSe.setVisibility(View.GONE);
        }

        bindActionOnFieldChange(otherExistingUSe, new Runnable() {
            @Override
            public void run() {
                attribute.setValue2(otherExistingUSe.getText().toString());
            }
        });

        bindActionOnMultiselect(attribute, selectedOptionsText, otherExistingUSe);
        return selectedOptionsText;
    }

    private static void bindActionOnMultiselect(final Attribute attribute, final TextView selectedOption, final EditText otherExistingUSe) {
        selectedOption.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<String> list = new ArrayList<String>();
                final List<Option> optionsList = attribute.getOptionsList();

                for (int i = 0; i < optionsList.size(); i++) {
                    list.add(optionsList.get(i).toString());
                }

                final Dialog dialog = new Dialog(v.getContext(), R.style.DialogThemeLight);
                dialog.setContentView(R.layout.item_multiselect);
                dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.setCanceledOnTouchOutside(false);
                dialog.setTitle(attribute.getLabelName());

                final ListView mutilist = (ListView) dialog.findViewById(R.id.listviewAnswer);
                final CheckBoxAdapter multiadaptor = new CheckBoxAdapter(list, optionsList, StringUtility.empty(attribute.getValue()));

                mutilist.setAdapter(multiadaptor);
                Button button = (Button) dialog.findViewById(R.id.btn_SubmitAnswer);

                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ArrayList<String> selectedCheckboxText = new ArrayList<String>();
                        String optionSelectedId = "";
                        Boolean dialog_status = true;

                        for (int i = 0; i < optionsList.size(); i++) {
                            LinearLayout view = (LinearLayout) mutilist.getChildAt(i);
                            if (dialog_status == false) {
                                break;
                            }
                            if (view != null) {
                                for (int j = 0; j < optionsList.size(); j++) {
                                    try {
                                        View child = view.getChildAt(j);

                                        if (child instanceof CheckBox) {
                                            CheckBox cb = (CheckBox) child;
                                            if (cb.isChecked()) {
                                                selectedCheckboxText.add(optionsList.get(i).toString());
                                                if(optionSelectedId.equals("")){
                                                    optionSelectedId += optionsList.get(i).getId();
                                                } else {
                                                    optionSelectedId += "," + optionsList.get(i).getId();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.getStackTrace();
                                    }
                                }
                            }
                        }

                        if (selectedCheckboxText.size() < 1) {
                            selectedOption.setText("");
                            otherExistingUSe.setText("");
                            attribute.setValue2(null);
                            attribute.setValue(null);
                        } else {
                            selectedOption.setText(selectedCheckboxText.toString());
                            attribute.setValue(optionSelectedId);
                        }

                        if ((optionSelectedId).contains(Option.ID_OTHER_USE)) {
                            otherExistingUSe.setVisibility(View.VISIBLE);
                            otherExistingUSe.setText("");
                            attribute.setValue2(null);
                        }
                        else
                            otherExistingUSe.setVisibility(View.GONE);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    private static Spinner createSpinnerViewFromArray(View container, final Attribute attribute, boolean readOnly) {
        TextView fieldAlias = (TextView) container.findViewById(R.id.field);
        final Spinner spinner = (Spinner) container.findViewById(R.id.spinner1);
        if (isDipute == 0) {
            if (attribute.getName().equalsIgnoreCase("Disputed PersonType")) {
                container.setVisibility(View.GONE);

            } else {

                fieldAlias.setText(attribute.getLabelName());
                spinner.setPrompt(attribute.getLabelName());
                spinner.setTag(attribute.getId());

                SpinnerAdapter spinnerAdapter = new SpinnerAdapter(
                        container.getContext(),
                        android.R.layout.simple_spinner_item,
                        attribute.getOptionsList());
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                if (readOnly) {
                    spinner.setEnabled(false);
                }

                String fieldValue = attribute.getValue();

                if (!StringUtility.isEmpty(fieldValue) && !fieldValue.equalsIgnoreCase("Select an option")) {
                    int currentValue = Integer.parseInt(fieldValue);
                    spinner.setSelection(spinnerAdapter.getPosition(currentValue));
                }

                bindActionOnSpinnerChange(spinner, new Runnable() {
                    @Override
                    public void run() {
                        Option selecteditem = (Option) spinner.getSelectedItem();
                        attribute.setValue(selecteditem.getId().toString());
//                attribute.setValue(selecteditem.getId().toString());
                    }
                });

            }
        }
//        TextView fieldAlias = (TextView) container.findViewById(R.id.field);
//        final Spinner spinner = (Spinner) container.findViewById(R.id.spinner1);
        else if (isDipute == 1) {


            fieldAlias.setText(attribute.getLabelName());
            spinner.setPrompt(attribute.getLabelName());
            spinner.setTag(attribute.getId());

            SpinnerAdapter spinnerAdapter = new SpinnerAdapter(
                    container.getContext(),
                    android.R.layout.simple_spinner_item,
                    attribute.getOptionsList());
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);

            if (readOnly) {
                spinner.setEnabled(false);
            }

            String fieldValue = attribute.getValue();

            if (!StringUtility.isEmpty(fieldValue) && !fieldValue.equalsIgnoreCase("Select an option")) {
                int currentValue = Integer.parseInt(fieldValue);
                spinner.setSelection(spinnerAdapter.getPosition(currentValue));
            }

            bindActionOnSpinnerChange(spinner, new Runnable() {
                @Override
                public void run() {
                    Option selecteditem = (Option) spinner.getSelectedItem();
                    attribute.setValue(selecteditem.getId().toString());
//                attribute.setValue(selecteditem.getId().toString());
                }
            });

        }
        return spinner;
    }

    private static Spinner createSpinnerViewForBoolean(View container, final Attribute attribute, boolean readOnly) {
        int spinnerPosition = 0;
        TextView fieldAlias = (TextView) container.findViewById(R.id.field);

        final Spinner spinner = (Spinner) container.findViewById(R.id.spinner1);
        fieldAlias.setText(attribute.getLabelName());
        spinner.setPrompt(attribute.getLabelName());

        if (readOnly) {
            spinner.setEnabled(false);
        }

        String[] list = container.getContext().getResources().getStringArray(R.array.booleanControlValues);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinnerPosition = spinnerAdapter.getPosition(attribute.getValue());
        if (attribute.getValue() != null && attribute.getValue() != ""
                && (attribute.getValue() != "Select an option"
                || !attribute.getValue().equalsIgnoreCase("Chagua chaguo"))) {
            if (attribute.getValue().equalsIgnoreCase("yes") || attribute.getValue().equalsIgnoreCase("Ndiyo"))

                spinner.setSelection(spinnerPosition);
            if (attribute.getValue().equalsIgnoreCase("no") || attribute.getValue().equalsIgnoreCase("Hapana"))
                spinner.setSelection(spinnerPosition);
        }

        bindActionOnSpinnerChange(spinner, new Runnable() {
            @Override
            public void run() {
                String selecteditem = (String) spinner.getSelectedItem();
                attribute.setValue(selecteditem);
            }
        });
        return spinner;
    }

    /**
     * Validates provided list of attributes and highlights underlying control in case of missing values in the mandatory fields
     *
     * @param attributeList   Attribute to validateAttributes;
     * @param highlightErrors Indicates whether to highlight fields with errors or not
     */
    public static boolean validateAttributes(List<Attribute> attributeList, boolean highlightErrors) {
        boolean isValid = true;
        if (attributeList == null || attributeList.size() < 1) {
            return isValid;
        }

        for (Attribute attribute : attributeList) {

            if (!validateAttribute(attribute, highlightErrors)) {
                isValid = false;
            }
        }
        return isValid;
    }

//    public static boolean validateAttributesByPerson(List<Attribute> attributeList, boolean highlightErrors,int isDiputeValues) {
//        boolean isValid = true;
//        if (attributeList == null || attributeList.size() < 1) {
//            return isValid;
//        }
//
//        for (Attribute attribute : attributeList) {
//            if (!validateAttributebyPerson(attribute, highlightErrors,isDiputeValues)) {
//                isValid = false;
//            }
//        }
//        return isValid;
//    }

    /**
     * Validates provided attribute and highlights underlying control in case of missing value in the mandatory field
     *
     * @param attribute       Attribute to validateAttributes;
     * @param highlightErrors Indicates whether to highlight fields with errors or not
     */

    public static boolean validateAttribute(Attribute attribute, boolean highlightErrors) {
        boolean isValid = true;
        String value = attribute.getValue();
        String hasValidation = attribute.getValidate();


        if (attribute.getControlType() == Attribute.CONTROL_TYPE_STIRNG) {

            if (attribute.getName().equalsIgnoreCase("Identification No"))//Identification No.
            {
                if (isMandatorty == true) {
                    if (hasValidation.equalsIgnoreCase("true") && StringUtility.isEmpty(value)) {
                        isValid = false;
                    }
                } else {
                    isValid = true;
                }
            } else {
                if (hasValidation.equalsIgnoreCase("true") && StringUtility.isEmpty(value)) {
                    isValid = false;
                }
            }

        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_DATE) {
            if (hasValidation.equalsIgnoreCase("true") && StringUtility.isEmpty(value)) {
                isValid = false;
            }
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_BOOLEAN) {
            if (hasValidation.equalsIgnoreCase("true") &&
                    !StringUtility.empty(value).equalsIgnoreCase("yes") &&
                    !StringUtility.empty(value).equalsIgnoreCase("Ndiyo") &&
                    !StringUtility.empty(value).equalsIgnoreCase("no") &&
                    !StringUtility.empty(value).equalsIgnoreCase("Hapana")
                    ) {
                isValid = false;
            }
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_NUMBER) {
            if (hasValidation.equalsIgnoreCase("true") && StringUtility.isEmpty(value)) {
                isValid = false;
            }
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_SPINNER) {
            if (attribute.getName().equalsIgnoreCase("Identification Type")) {
                if (value.equalsIgnoreCase("1156")) {
                    isMandatorty = false;
                } else if (value.equalsIgnoreCase("0")) {
                    isValid = false;
                } else {
                    isMandatorty = true;
                }
            } else {
                if (isDipute == 0) {
                    if (attribute.getName().equalsIgnoreCase("Disputed PersonType")) {
                        for (int i = 0; i < attribute.getOptionsList().size(); i++) {
                            if (attribute.getOptionsList().get(i).getId() == 1165 || attribute.getOptionsList().get(i).getId() == 1166) {
                                isValid = true;
                            }
                        }
                    } else {
                        if (hasValidation.equalsIgnoreCase("true") && (StringUtility.isEmpty(value) || value.equals("0"))) {
                            isValid = false;
                        }
                    }
                } else {
                    if (hasValidation.equalsIgnoreCase("true") && (StringUtility.isEmpty(value) || value.equals("0"))) {
                        isValid = false;
                    }
                }
            }
        } else if (attribute.getControlType() == Attribute.CONTROL_TYPE_MULTISELECT) {
            if (attribute.getId().intValue() == Attribute.ATTR_EXISTING_USE && StringUtility.empty(value).equals(Option.ID_OTHER_USE)) {
                // Other use
                if (StringUtility.isEmpty(attribute.getValue2())) {
                    isValid = false;
                }
            }
            if (hasValidation.equalsIgnoreCase("true") && StringUtility.isEmpty(value)) {
                isValid = false;
            }
        }


        if (highlightErrors && attribute.getView() != null) {
            if (!isValid) {
                attribute.getView().setBackgroundColor(attribute.getView().getContext().getResources().getColor(R.color.lightred));
            } else {
                if (attribute.getInitialBackground() != null) {
                    attribute.getView().setBackground(attribute.getInitialBackground());
                } else {
                    attribute.getView().setBackgroundColor(attribute.getView().getContext().getResources().getColor(R.color.white));
                }
            }
        }
        return isValid;
    }
}
