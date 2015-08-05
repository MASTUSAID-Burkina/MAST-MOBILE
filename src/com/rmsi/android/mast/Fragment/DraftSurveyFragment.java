package com.rmsi.android.mast.Fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rmsi.android.mast.activity.CaptureDataMapActivity;
import com.rmsi.android.mast.activity.DataSummaryActivity;
import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.activity.R.string;
import com.rmsi.android.mast.adapter.SurveyListingAdapter;
import com.rmsi.android.mast.db.DBController;
import com.rmsi.android.mast.domain.Attribute;
import com.rmsi.android.mast.domain.Feature;
import com.rmsi.android.mast.util.CommonFunctions;

/**
 * @author Prashant.Nigam
 * 
 */
public class DraftSurveyFragment extends Fragment {
	ListView listView;
	SurveyListingAdapter adapter;
	Context context;
	List<Feature> features = new ArrayList<Feature>();
	static boolean listChanged = false;
	CommonFunctions cf = CommonFunctions.getInstance();
	String msg, info;

	private void deleteEntry(final Long featureId) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setMessage(R.string.deleteFeatureEntryMsg);
		alertDialogBuilder.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						DBController db = new DBController(context);
						boolean result = db.deleteFeature(featureId);
						db.close();
						if (result) {
							getActivity().recreate();
							// refreshList();
						} else {
							msg = getResources().getString(
									string.Error_deleting_feature);
							Toast.makeText(context, msg, Toast.LENGTH_SHORT)
									.show();
						}

					}
				});

		alertDialogBuilder.setNegativeButton(R.string.btn_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void markFeatureAsComplete(final Long featureId) {
		final DBController db = new DBController(context);
		List<Attribute> tenureList = db.getTenureList(featureId, null);
		List<Attribute> personList = db.getPersonList(featureId);
		boolean propertyFilled = db.IsPropertyAttribValue(featureId);
		if (propertyFilled) {
			if (tenureList.size() != 0 || personList.size() != 0) {
				if (personList.size() == tenureList.size()) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							context);
					alertDialogBuilder.setMessage(R.string.completedFeatureMsg);

					alertDialogBuilder.setPositiveButton(R.string.btn_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									int personCount = db.getPersonList(
											featureId).size();
									int personMediacount = db
											.getPersonMediaCount(featureId);
									if (personCount != personMediacount) {
										msg = getResources()
												.getString(
														R.string.add_photo_of_all_persons);
										info = getResources().getString(
												R.string.info);
										cf.showMessage(context, info, msg);
									}

									else {
										boolean result = db
												.markFeatureAsComplete(featureId);
										db.close();
										if (result) {
											getActivity().recreate();
											// refreshList();
											listChanged = true;
										} else {
											msg = getResources()
													.getString(
															string.Error_updating_feature);
											Toast.makeText(context, msg,
													Toast.LENGTH_SHORT).show();
										}
									}
								}
							});

					alertDialogBuilder.setNegativeButton(R.string.btn_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});

					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				} else {
					msg = getResources().getString(
							string.equal_person_nd_tenure);
					String warning = getResources().getString(string.warning);
					cf.showMessage(context, warning, msg);
				}
			} else {
				msg = getResources().getString(string.atleast_one_person);
				String warning = getResources().getString(string.warning);
				cf.showMessage(context, warning, msg);
			}
		} else {
			msg = getResources().getString(string.fill_property_Details);
			String warning = getResources().getString(string.warning);
			cf.showMessage(context, warning, msg);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			ViewGroup container, final Bundle savedInstanceState) {
		context = getActivity();

		// Initializing context in common functions in case of a crash
		try {
			CommonFunctions.getInstance().Initialize(
					context.getApplicationContext());
		} catch (Exception e) {
		}

		View view = inflater.inflate(R.layout.fragment_survey_review_data_list,
				container, false);
		listView = (ListView) view.findViewById(android.R.id.list);
		TextView emptyText = (TextView) view.findViewById(android.R.id.empty);
		listView.setEmptyView(emptyText);

		adapter = new SurveyListingAdapter(context, this, features,
				"draftsurvey");
		listView.setAdapter(adapter);
		// refreshList();

		return view;
	}

	@Override
	public void onResume() {
		refreshList();
		super.onResume();
	}

	private void refreshList() {
		DBController db = new DBController(context);
		features.clear();
		features.addAll(db.fetchDraftFeatures());
		db.close();
		adapter.notifyDataSetChanged();

	}

	public void showPopupDraft(View v, Object object) {
		PopupMenu popup = new PopupMenu(context, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.survey_listing_options, popup.getMenu());
		int position = (Integer) object;
		final Long featureId = features.get(position).getFeatureid();
		final String server_featureId = features.get(position)
				.getServer_featureid();
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.edit_spatial:
					Intent intent = new Intent(context,
							CaptureDataMapActivity.class);
					intent.putExtra("featureid", featureId);
					startActivity(intent);
					return true;
				case R.id.edit_attributes:
					// Open attributes form to edit --------------
					Intent myIntent = new Intent(context,
							DataSummaryActivity.class);
					myIntent.putExtra("featureid", featureId);
					myIntent.putExtra("Server_featureid", server_featureId);
					// myIntent.putExtra("flag",true);
					startActivity(myIntent);
					return true;
				case R.id.delete_entry:
					deleteEntry(featureId);
					return true;
				case R.id.mark_as_complete:
					markFeatureAsComplete(featureId);
					return true;
				default:
					return false;
				}
			}
		});
		popup.show();
	}
}
