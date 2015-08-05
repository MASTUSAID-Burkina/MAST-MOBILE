package com.rmsi.android.mast.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.rmsi.android.mast.activity.R;
import com.rmsi.android.mast.domain.Attribute;
import com.rmsi.android.mast.domain.Bookmark;
import com.rmsi.android.mast.domain.Feature;
import com.rmsi.android.mast.domain.Media;
import com.rmsi.android.mast.domain.Option;
import com.rmsi.android.mast.domain.ProjectSpatialDataDto;
import com.rmsi.android.mast.domain.User;
import com.rmsi.android.mast.util.CommonFunctions;

/**
 * @author Prashant.Nigam
 * 
 */
public class DBController extends SQLiteOpenHelper {
	Context contxt;
	SharedPreferences sharedpreferences;
	SQLiteDatabase myDataBase;

	static String DBPATH = "/" + CommonFunctions.parentFolderName + "/"
			+ CommonFunctions.dbFolderName + "/MAST_mobile.db";
	static String DB_FULL_PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + DBPATH;
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a",
			Locale.ENGLISH);

	private static String STATUS_DRAFT = "draft";
	private static String STATUS_COMPLETE = "complete";
	private static String STATUS_SYNCED = "synced";
	private static String STATUS_VERIFIED = "verified";
	private static String STATUS_VERIFIED_SYNCED = "verified&synced";
	private static String STATUS_REJECTED = "rejected";
	private static String STATUS_FINAL = "final";
	private static String CAT_General = "1";
	private static String CAT_NaturalPerson = "2";
	private static String CAT_Multimedia = "3";
	private static String CAT_Tenure = "4";
	private static String CAT_NonNaturalPerson = "5";
	private static String CAT_Custom = "6";
	private static String CAT_General_Property = "7";
	private static String NonNaturalPerson = "Non-Natural";
	CommonFunctions cf = null;

	public DBController(Context applicationcontext) {
		super(applicationcontext, DB_FULL_PATH, null, 2);
		this.contxt = applicationcontext;
		cf = CommonFunctions.getInstance();
		// Initializing context in common functions in case of a crash
		try {
			CommonFunctions.getInstance().Initialize(
					applicationcontext.getApplicationContext());
		} catch (Exception e) {
		}
	}

	public boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = getReadableDatabase();
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	public boolean checkPendingDraftAndCompletedRecordsToSync() {
		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		String spatialFeatureSql = "SELECT * FROM SPATIAL_FEATURES where status = '"
				+ STATUS_DRAFT
				+ "' OR status = '"
				+ STATUS_COMPLETE
				+ "' and SERVER_FEATURE_ID IS NULL OR SERVER_FEATURE_ID = ''";
		Cursor cursor = database.rawQuery(spatialFeatureSql, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	private void cleandb(SQLiteDatabase db) {
		db.delete("FORM_VALUES", null, null);
		db.delete("PERSON", null, null);
		db.delete("MEDIA", null, null);
		db.delete("SPATIAL_FEATURES", null, null);
		db.delete("PERSON_MEDIA", null, null);
		db.delete("SOCIAL_TENURE", null, null);
		db.delete("PROJECT_SPATIAL_DATA", null, null);
		db.delete("OPTIONS", null, null);
		db.delete("ATTRIBUTE_MASTER", null, null);
	}

	public boolean deleteFeature(Long featureid) {

		String whereClause = "FEATURE_ID =" + featureid;
		try {
			myDataBase = getReadableDatabase();
			myDataBase.delete("SPATIAL_FEATURES", whereClause, null);
			myDataBase.delete("FORM_VALUES", whereClause, null);
			myDataBase.delete("PERSON", whereClause, null);
			myDataBase.delete("PERSON_MEDIA", whereClause, null);
			myDataBase.delete("SOCIAL_TENURE", whereClause, null);
			myDataBase.delete("MEDIA", whereClause, null);
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean deletePersonPhoto(int groupId) {
		SQLiteDatabase db = getWritableDatabase();
		// "CREATE TABLE FORM_VALUES(ATTRIB_ID INTEGER,ATTRIB_VALUE TEXT,OPTION_ID TEXT,OPTION_VALUE TEXT,FEATURE_ID)";
		try {
			String delteWherePersonId = "PERSON_ID=" + groupId;
			int rows = db.delete("PERSON_MEDIA", delteWherePersonId, null);
			if (rows == 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteRecord(int groupId, String keyword) {
		SQLiteDatabase db = getWritableDatabase();
		// "CREATE TABLE FORM_VALUES(ATTRIB_ID INTEGER,ATTRIB_VALUE TEXT,OPTION_ID TEXT,OPTION_VALUE TEXT,FEATURE_ID)";

		try {
			String deleteWhereSql = "GROUP_ID=" + groupId;
			String delteWhere = "ID=" + groupId;
			String delteWherePersonId = "PERSON_ID=" + groupId;

			String delteWhereMediaId = "MEDIA_ID=" + groupId;
			// delete data frm master table(FORM_VALUES)
			db.delete("FORM_VALUES", deleteWhereSql, null);

			// Delete data from respective table

			if (keyword.equalsIgnoreCase("person")) {
				db.delete("PERSON", delteWhere, null);
				db.delete("PERSON_MEDIA", delteWherePersonId, null);
			}

			else if (keyword.equalsIgnoreCase("tenure")) {
				db.delete("SOCIAL_TENURE", delteWhere, null);
			} else if (keyword.equalsIgnoreCase("media")) {
				db.delete("MEDIA", delteWhereMediaId, null);
			}

			return true;
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
	}

	public void dropTable(SQLiteDatabase database, String tableName) {
		String query;
		query = "DROP TABLE IF EXISTS " + tableName;
		database.execSQL(query);
	}

	public Object[] fetchAllBookmarks() {
		String q = "SELECT * FROM BOOKMARKS";

		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		List<String> bookmarksStr = new ArrayList<String>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Bookmark bookmark = new Bookmark();
					bookmark.setName(cursor.getString(1));
					bookmark.setLatitude(cursor.getDouble(2));
					bookmark.setLongitude(cursor.getDouble(3));
					bookmark.setZoomlevel(cursor.getFloat(4));
					bookmarksStr.add(bookmark.getName());
					bookmarks.add(bookmark);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return new Object[] { bookmarks, bookmarksStr };
	}

	public List<Feature> fetchCompletedFeatures() {
		String q = "SELECT * FROM SPATIAL_FEATURES where status = '"
				+ STATUS_COMPLETE
				+ "' and (SERVER_FEATURE_ID = '' or SERVER_FEATURE_ID is null)";

		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();

					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Feature> fetchDraftFeatures() {
		String q = "SELECT * FROM SPATIAL_FEATURES where status = '"
				+ STATUS_DRAFT + "'";

		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();

					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
			close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public Feature fetchFeaturebyID(Long featureId) {
		String q = "SELECT * FROM SPATIAL_FEATURES where FEATURE_ID = "
				+ featureId;

		Feature feature = null;
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					feature = new Feature();
					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));
				} while (cursor.moveToNext());
			}
			cursor.close();
			close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return feature;
	}

	public List<Feature> fetchFeatures() {
		String q = "SELECT * FROM SPATIAL_FEATURES";

		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();
					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
			close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Feature> fetchFeaturesByGeomtype(String geomtype) {
		String q = "SELECT * FROM SPATIAL_FEATURES WHERE GEOMTYPE = '"
				+ geomtype + "'";

		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();

					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
			close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Feature> fetchFinalFeatures() {
		String syncedFeatureSql = "SELECT * FROM SPATIAL_FEATURES where STATUS='"
				+ STATUS_FINAL
				+ "' and (SERVER_FEATURE_ID IS not NULL OR SERVER_FEATURE_ID != '')";
		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(syncedFeatureSql, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();

					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Feature> fetchRejectedFeatures() {
		String q = "SELECT * FROM SPATIAL_FEATURES where status = '"
				+ STATUS_REJECTED + "'";

		List<Feature> features = new ArrayList<Feature>();
		try {
			SQLiteDatabase myDataBase1 = getReadableDatabase();
			Cursor cursor = myDataBase1.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();
					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));
					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Feature> fetchSyncededFeatures() {
		String syncedFeatureSql = "SELECT * FROM SPATIAL_FEATURES where STATUS='"
				+ STATUS_COMPLETE
				+ "' and (SERVER_FEATURE_ID IS not NULL OR SERVER_FEATURE_ID != '')";
		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(syncedFeatureSql, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();

					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Feature> fetchVerifiedFeatures() {
		String q = "SELECT * FROM SPATIAL_FEATURES where status in ('"
				+ STATUS_VERIFIED + "','" + STATUS_VERIFIED_SYNCED + "')";

		List<Feature> features = new ArrayList<Feature>();
		try {
			myDataBase = getReadableDatabase();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					Feature feature = new Feature();

					feature.setFeatureid(cursor.getLong(0));
					feature.setServer_featureid(cursor.getString(1));
					feature.setCoordinates(cursor.getString(2));
					feature.setGeomtype(cursor.getString(3));
					feature.setStatus(cursor.getString(5));

					features.add(feature);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return features;
	}

	public List<Attribute> getAssociatedPersonWithtenure(int groupId) {
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryQues = "SELECT * from SOCIAL_TENURE where PERSON_ID ="
				+ groupId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			do {
				Attribute attrib = new Attribute();
				attrib.setGroupId(cursor.getInt(0));
				String value1, value2;
				if (TextUtils.isEmpty(cursor.getString(2)))
					value2 = "";
				else
					value2 = cursor.getString(2);

				if (TextUtils.isEmpty(cursor.getString(1)))
					value1 = "";
				else
					value1 = cursor.getString(1);
				String fieldValue = value1 + " " + value2;
				attrib.setFieldValue(fieldValue);
				attrib.setPeronId(cursor.getInt(3));

				attribList.add(attrib);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public int getCount(String status) {
		int count = 0;
		SQLiteDatabase database = getReadableDatabase();
		String selectQueryQues = null;

		if (status.equalsIgnoreCase(STATUS_DRAFT)) {

			selectQueryQues = "SELECT * FROM SPATIAL_FEATURES where status = '"
					+ STATUS_DRAFT + "'";
		} else if (status.equalsIgnoreCase(STATUS_COMPLETE)) {
			selectQueryQues = "SELECT * FROM SPATIAL_FEATURES where status = '"
					+ STATUS_COMPLETE
					+ "' and (SERVER_FEATURE_ID = '' or SERVER_FEATURE_ID is null)";
		} else if (status.equalsIgnoreCase(STATUS_SYNCED)) {
			selectQueryQues = "SELECT * FROM SPATIAL_FEATURES where STATUS='"
					+ STATUS_COMPLETE
					+ "' and (SERVER_FEATURE_ID IS not NULL OR SERVER_FEATURE_ID != '')";
		} else if (status.equalsIgnoreCase(STATUS_REJECTED)) {
			selectQueryQues = "SELECT * FROM SPATIAL_FEATURES where status = '"
					+ STATUS_REJECTED + "'";
		}

		Cursor cursor = database.rawQuery(selectQueryQues, null);

		if (cursor.moveToFirst()) {
			do {
				count++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return count;
	}

	public List<Attribute> getFeatureGenaralInfo(Long featureId,
			String keyword, String lang) {
		String attributeType = null;
		SQLiteDatabase database = getReadableDatabase();
		/*
		 * String sql =
		 * "SELECT  AM.*,FM.GROUP_ID,FM.ATTRIB_VALUE FROM ATTRIBUTE_MASTER AS AM "
		 * +
		 * " LEFT OUTER JOIN FORM_VALUES AS FM ON AM.ATTRIB_ID =  FM.ATTRIB_ID and FM.FEATURE_ID = "
		 * +featureId +
		 * " LEFT JOIN SPATIAL_FEATURES AS SF ON FM.FEATURE_ID = SF.FEATURE_ID"
		 * + " where AM.ATTRIBUTE_TYPE = 'General'";
		 */
		if (keyword.equalsIgnoreCase("general")) {
			attributeType = CAT_General;
		} else if (keyword.equalsIgnoreCase("NonNatural")) {
			attributeType = CAT_NonNaturalPerson;
		} else if (keyword.equalsIgnoreCase("custom")) {
			attributeType = CAT_Custom;
		} else if (keyword.equalsIgnoreCase("Property")) {
			attributeType = CAT_General_Property;
		}

		String sql = "SELECT  AM.*,FM.GROUP_ID,FM.ATTRIB_VALUE,SF.PERSON_TYPE FROM ATTRIBUTE_MASTER AS AM"
				+ " LEFT OUTER JOIN FORM_VALUES AS FM ON AM.ATTRIB_ID =  FM.ATTRIB_ID and FM.FEATURE_ID = "
				+ featureId
				+ " LEFT JOIN SPATIAL_FEATURES AS SF ON FM.FEATURE_ID = SF.FEATURE_ID"
				+ " where AM.ATTRIBUTE_TYPE = '" + attributeType + "'";

		List<Attribute> attribList = new ArrayList<Attribute>();
		Cursor cursor = database.rawQuery(sql, null);

		if (cursor.moveToFirst()) {
			do {
				Attribute attrib = new Attribute();
				attrib.setAttributeid(cursor.getInt(1));
				attrib.setControlType(cursor.getInt(3));
				if (lang.equalsIgnoreCase("sw")
						&& !TextUtils.isEmpty(cursor.getString(6))) {
					attrib.setAttributeName(cursor.getString(6));
				} else {
					attrib.setAttributeName(cursor.getString(4));
				}

				attrib.setValidation(cursor.getString(7));
				attrib.setGroupId(cursor.getInt(8));
				attrib.setFieldValue(cursor.getString(9));
				attrib.setPersonType(cursor.getString(10));

				if (attrib.getControlType() == 5) {

					Option option = null;
					List<Option> optionList = new ArrayList<Option>();
					option = new Option();
					option.setOptionId(0L);
					option.setOptionName(contxt.getResources().getString(
							R.string.select));
					optionList.add(option);

					String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
							+ attrib.getAttributeid();
					Cursor cursor2 = database
							.rawQuery(selectQueryOptions, null);

					if (cursor2.moveToFirst()) {
						do {
							option = new Option();
							option.setOptionId(cursor2.getLong(0));
							StringBuffer multiLnagText = new StringBuffer();
							if (lang.equalsIgnoreCase("sw")
									&& !TextUtils.isEmpty(cursor2.getString(3))) {
								option.setOptionName(cursor2.getString(3));
							} else {
								option.setOptionName(cursor2.getString(2));
							}
							if (!TextUtils.isEmpty(cursor2.getString(3))) {
								multiLnagText.append(cursor2.getString(3));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							} else {
								multiLnagText.append(cursor2.getString(2));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							}
							optionList.add(option);
						} while (cursor2.moveToNext());
					}
					cursor2.close();
					attrib.setOptionsList(optionList);
				}
				attribList.add(attrib);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public List<Attribute> getFormDataByGroupId(int GroupId, String lang) {
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryQues = "SELECT  AM.*,FM.GROUP_ID,FM.ATTRIB_VALUE FROM ATTRIBUTE_MASTER AS AM LEFT JOIN form_VALUES AS FM ON AM.ATTRIB_ID = FM.ATTRIB_ID where FM.GROUP_ID="
				+ GroupId;// +" ORDER BY AM.ATTRIB_ID";

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			try {
				do {

					Attribute attrib = new Attribute();
					attrib.setAttributeid(cursor.getInt(1));
					if (lang.equalsIgnoreCase("sw")
							&& !TextUtils.isEmpty(cursor.getString(6))) {
						attrib.setAttributeName(cursor.getString(6));
					} else {
						attrib.setAttributeName(cursor.getString(4));
					}
					attrib.setControlType(cursor.getInt(3));
					attrib.setValidation(cursor.getString(7));
					attrib.setFieldValue(cursor.getString(9));
					attrib.setListing(cursor.getInt(5));
					if (attrib.getControlType() == 5) {
						List<Option> optionList = new ArrayList<Option>();
						Option option = new Option();
						option.setOptionId(0L);
						option.setOptionName(contxt.getResources().getString(
								R.string.select));
						optionList.add(option);
						String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
								+ attrib.getAttributeid();
						Cursor cursor2 = database.rawQuery(selectQueryOptions,
								null);

						if (cursor2.moveToFirst()) {
							do {
								option = new Option();
								option.setOptionId(cursor2.getLong(0));
								StringBuffer multiLnagText = new StringBuffer();
								if (lang.equalsIgnoreCase("sw")
										&& !TextUtils.isEmpty(cursor2
												.getString(3))) {
									option.setOptionName(cursor2.getString(3));
								} else {
									option.setOptionName(cursor2.getString(2));
								}
								if (!TextUtils.isEmpty(cursor2.getString(3))) {
									multiLnagText.append(cursor2.getString(3));
									multiLnagText.append("&#&"
											+ cursor2.getString(2));
									option.setOptionMultiLang(multiLnagText
											.toString());
								} else {
									multiLnagText.append(cursor2.getString(2));
									multiLnagText.append("&#&"
											+ cursor2.getString(2));
									option.setOptionMultiLang(multiLnagText
											.toString());
								}
								optionList.add(option);
							} while (cursor2.moveToNext());
						}
						cursor2.close();
						attrib.setOptionsList(optionList);
					}
					attribList.add(attrib);
				} while (cursor.moveToNext());
			} catch (Exception e) {
				cf.appLog("", e);
				e.printStackTrace();
			}
		}
		cursor.close();
		return attribList;
	}

	public boolean getFormValues(Long featureId) {

		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryQues = "SELECT GROUP_ID from fORM_VALUES where FEATURE_ID ="
				+ featureId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	public List<Attribute> getGeneralAttribute(String lang) {
		SQLiteDatabase db = getWritableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryValues = "SELECT  * FROM ATTRIBUTE_MASTER where ATTRIBUTE_TYPE ='"
				+ CAT_General + "'";

		Cursor cursor = db.rawQuery(selectQueryValues, null);
		// id ATTRIB_ID INTEGER ATTRIBUTE_TYPE STRING ATTRIBUTE_CONTROLTYPE
		// INTEGER ATTRIBUTE_NAME TEXT)";
		// 0 1 2 3
		Attribute attrib;
		if (cursor.moveToFirst()) {
			do {
				attrib = new Attribute();
				attrib.setAttributeid(cursor.getInt(1));
				attrib.setAttributeType(cursor.getString(2));
				attrib.setControlType(cursor.getInt(3));
				if (lang.equalsIgnoreCase("sw")
						&& !TextUtils.isEmpty(cursor.getString(6))) {
					attrib.setAttributeName(cursor.getString(6));
				} else {
					attrib.setAttributeName(cursor.getString(4));
				}

				if (attrib.getControlType() == 5) {
					List<Option> optionList = new ArrayList<Option>();
					Option option = null;

					option = new Option();
					option.setOptionId(0L);
					option.setOptionName(contxt.getResources().getString(
							R.string.select));
					optionList.add(option);

					String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
							+ attrib.getAttributeid();
					Cursor cursor2 = db.rawQuery(selectQueryOptions, null);

					if (cursor2.moveToFirst()) {
						do {
							option = new Option();
							option.setOptionId(cursor2.getLong(0));
							StringBuffer multiLnagText = new StringBuffer();
							if (lang.equalsIgnoreCase("sw")
									&& !TextUtils.isEmpty(cursor2.getString(3))) {
								option.setOptionName(cursor2.getString(3));
							} else {
								option.setOptionName(cursor2.getString(2));
							}
							if (!TextUtils.isEmpty(cursor2.getString(3))) {
								multiLnagText.append(cursor2.getString(3));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							} else {
								multiLnagText.append(cursor2.getString(2));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							}
							optionList.add(option);
						} while (cursor2.moveToNext());
					}
					cursor2.close();
					attrib.setOptionsList(optionList);
				}
				attribList.add(attrib);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public User getLoggedUser() {
		SQLiteDatabase database = getReadableDatabase();
		User user = null;
		String selectSQLUser = "SELECT * FROM USER ";
		Cursor cursor = database.rawQuery(selectSQLUser, null);

		if (cursor.moveToFirst()) {
			user = new User();
			user.setUserId(cursor.getLong(0));
			user.setUserName(cursor.getString(1));
			user.setPassword(cursor.getString(2));
			user.setRoleName(cursor.getString(4));

		}
		cursor.close();

		// close();
		return user;
	}

	public int getMediaCount(int groupId) {
		int count = 0;
		SQLiteDatabase database = getReadableDatabase();

		String selectQueryQues = "SELECT * from PERSON_MEDIA where PERSON_ID ="
				+ groupId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);

		if (cursor.moveToFirst()) {
			do {
				count++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return count;
	}

	public Media getMediaFile(int groupid) {
		SQLiteDatabase database = getReadableDatabase();
		Media media = new Media();
		String selectQueryQues = "SELECT * from MEDIA where MEDIA_ID ="
				+ groupid;

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			media.setMediaId(cursor.getInt(0));
			media.setMediaPath(cursor.getString(3));
			media.setMediaType(cursor.getString(2));
		}
		cursor.close();
		return media;
	}

	public List<Attribute> getMediaList(Long featureId) {
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryQues = "SELECT * from MEDIA where FEATURE_ID ="
				+ featureId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			do {
				Attribute attrib = new Attribute();
				attrib.setGroupId(cursor.getInt(0));
				String value1, value2;
				if (TextUtils.isEmpty(cursor.getString(5)))
					value2 = "";
				else
					value2 = cursor.getString(5);

				if (TextUtils.isEmpty(cursor.getString(4)))
					value1 = "";
				else
					value1 = cursor.getString(4);
				String fieldValue = value1 + " " + value2;
				attrib.setFieldValue(fieldValue);
				attribList.add(attrib);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public List<Media> getMediaPathByGroupId(int GroupId) {

		SQLiteDatabase database = getReadableDatabase();
		List<Media> mediaList = new ArrayList<Media>();
		String selectQueryQues = "SELECT PATH FROM PERSON_MEDIA where PERSON_ID="
				+ GroupId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			try {
				do {
					Media media = new Media();
					media.setMediaPath(cursor.getString(0));
					mediaList.add(media);
				} while (cursor.moveToNext());
			} catch (Exception e) {
				cf.appLog("", e);
				e.printStackTrace();
			}
		}
		cursor.close();
		return mediaList;

	}

	public List<Attribute> getMultimediaFormDataByGroupId(int GroupId,
			String lang) {
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryQues = "SELECT  AM.*,FM.GROUP_ID,FM.ATTRIB_VALUE FROM ATTRIBUTE_MASTER AS AM LEFT JOIN form_VALUES AS FM "
				+ "ON AM.ATTRIB_ID = FM.ATTRIB_ID and FM.GROUP_ID="
				+ GroupId
				+ " where attribute_type = '" + CAT_Multimedia + "'";

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			do {

				Attribute attrib = new Attribute();
				attrib.setAttributeid(cursor.getInt(1));
				if (lang.equalsIgnoreCase("sw")
						&& !TextUtils.isEmpty(cursor.getString(6))) {
					attrib.setAttributeName(cursor.getString(6));
				} else {
					attrib.setAttributeName(cursor.getString(4));
				}
				attrib.setControlType(cursor.getInt(3));
				attrib.setValidation(cursor.getString(7));
				attrib.setFieldValue(cursor.getString(9));
				attrib.setListing(cursor.getInt(5));
				if (attrib.getControlType() == 5) {
					List<Option> optionList = new ArrayList<Option>();
					Option option = null;

					option = new Option();
					option.setOptionId(0L);
					option.setOptionName(contxt.getResources().getString(
							R.string.select));
					optionList.add(option);

					String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
							+ attrib.getAttributeid();
					Cursor cursor2 = database
							.rawQuery(selectQueryOptions, null);

					if (cursor2.moveToFirst()) {
						do {
							option = new Option();
							option.setOptionId(cursor2.getLong(0));
							StringBuffer multiLnagText = new StringBuffer();
							if (lang.equalsIgnoreCase("sw")
									&& !TextUtils.isEmpty(cursor2.getString(3))) {
								option.setOptionName(cursor2.getString(3));
							} else {
								option.setOptionName(cursor2.getString(2));
							}
							if (!TextUtils.isEmpty(cursor2.getString(3))) {
								multiLnagText.append(cursor2.getString(3));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							} else {
								multiLnagText.append(cursor2.getString(2));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							}
							optionList.add(option);
						} while (cursor2.moveToNext());
					}
					cursor2.close();
					attrib.setOptionsList(optionList);
				}

				attribList.add(attrib);

			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;

	}

	public JSONArray getMultimediaforUpload() {
		JSONArray mediaAttribsObj = new JSONArray();
		try {
			int mediaId = 0;
			boolean featureMediaAvailable = false;
			myDataBase = getReadableDatabase();

			String fetchMediaAttributeSql = "SELECT  AM.attrib_id,FM.GROUP_ID,FM.ATTRIB_VALUE FROM ATTRIBUTE_MASTER AS AM "
					+ " LEFT OUTER JOIN FORM_VALUES AS FM ON AM.ATTRIB_ID =  FM.ATTRIB_ID and FM.FEATURE_ID "
					+ "where AM.ATTRIBUTE_TYPE = '"
					+ CAT_Multimedia
					+ "' and GROUP_ID = <group_id> and (FM.ATTRIB_VALUE != '' OR FM.ATTRIB_VALUE != NULL) order by group_id";

			String fetchFromMediaSql = "SELECT MV.MEDIA_ID,MV.FEATURE_ID,MV.PATH,SF.SERVER_FEATURE_ID,MV.TYPE FROM MEDIA AS MV "
					+ "LEFT OUTER JOIN SPATIAL_FEATURES AS SF ON MV.FEATURE_ID =  SF.FEATURE_ID "
					+ "where SF.STATUS = '"
					+ STATUS_COMPLETE
					+ "' and SYNCED=0 Limit 1";

			String fetchFromPersonMediaSql = "SELECT PM.ID,PM.PERSON_ID,PM.FEATURE_ID,PM.PATH,SF.SERVER_FEATURE_ID,PM.TYPE FROM PERSON_MEDIA AS PM "
					+ "LEFT OUTER JOIN SPATIAL_FEATURES AS SF ON PM.FEATURE_ID =  SF.FEATURE_ID "
					+ "where SF.STATUS = '"
					+ STATUS_COMPLETE
					+ "' and SYNCED = 0 Limit 1";

			JSONArray mediaAttribsArr = new JSONArray();
			JSONArray mediaValuesArr = new JSONArray();
			JSONArray atribsArrList = new JSONArray();

			// Fetching media for spatial unit
			Cursor cursor = myDataBase.rawQuery(fetchFromMediaSql, null);
			if (cursor.moveToFirst()) {
				featureMediaAvailable = true;
				mediaId = cursor.getInt(0);
				mediaAttribsArr.put(0, cursor.getLong(3)); // usin
				mediaAttribsArr.put(1, "");// person id -- Empty for feature
											// media
				mediaAttribsArr.put(2, mediaId);// media id
				mediaAttribsArr.put(3, cursor.getString(2));// media path
				mediaAttribsArr.put(4, cursor.getString(4));// media type
			}
			cursor.close();

			if (!featureMediaAvailable) // Fetching media for person
			{
				cursor = myDataBase.rawQuery(fetchFromPersonMediaSql, null);
				if (cursor.moveToFirst()) {
					mediaId = cursor.getInt(0);
					mediaAttribsArr.put(0, cursor.getLong(4)); // usin
					mediaAttribsArr.put(1, cursor.getLong(1));
					mediaAttribsArr.put(2, mediaId);// media id
					mediaAttribsArr.put(3, cursor.getString(3));// media path
					mediaAttribsArr.put(4, cursor.getString(5));// media type
				}
				cursor.close();
			}

			if (mediaId != 0) {
				if (featureMediaAvailable) {
					String final_sql = fetchMediaAttributeSql.replace(
							"<group_id>", mediaId + "");
					Cursor cursor_attrib = myDataBase.rawQuery(final_sql, null);
					if (cursor_attrib.moveToFirst()) {
						do {
							mediaValuesArr = new JSONArray();
							mediaValuesArr.put(0, cursor_attrib.getString(0));// attribID
							mediaValuesArr.put(1, cursor_attrib.getString(2));// attribvalue

							atribsArrList.put(mediaValuesArr);
						} while (cursor_attrib.moveToNext());
					}
					cursor_attrib.close();
				}
				mediaAttribsObj.put(0, mediaAttribsArr);
				mediaAttribsObj.put(1, atribsArrList);
			}
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mediaAttribsObj;
	}

	private Long getNewFeatureId() // for fetching new value from features
	{
		String sql = "select seq from sqlite_sequence where name='SPATIAL_FEATURES'";
		Long featureid = null;
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.rawQuery(sql, null);

		if (cursor.moveToFirst()) {
			do {
				featureid = cursor.getLong(0);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return featureid;
	}

	public int getNewGroupId() // for fetching new value from group ids
	{
		String sql = "SELECT * FROM GROUPID_SEQ";
		int groupid = 0;
		SQLiteDatabase database = getWritableDatabase();
		Cursor cursor = database.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			groupid = cursor.getInt(0);
		}
		cursor.close();

		if (groupid == 0) {
			groupid++;
			ContentValues value = new ContentValues();
			value.put("value", groupid);
			database.insert("GROUPID_SEQ", null, value);
		} else {
			groupid++;
			ContentValues value = new ContentValues();
			value.put("value", groupid);
			database.update("GROUPID_SEQ", value, null, null);
		}
		return groupid;
	}

	public List<Attribute> getNonNaturalPersonAttribute(
			SQLiteDatabase database, String lang) {

		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryValues = "SELECT  * FROM ATTRIBUTE_MASTER where ATTRIBUTE_TYPE ='"
				+ CAT_NonNaturalPerson + "'";

		Cursor cursor = database.rawQuery(selectQueryValues, null);
		// id ATTRIB_ID INTEGER ATTRIBUTE_TYPE STRING ATTRIBUTE_CONTROLTYPE
		// INTEGER ATTRIBUTE_NAME TEXT)";
		// 0 1 2 3 4
		Attribute attrib;
		if (cursor.moveToFirst()) {
			do {
				attrib = new Attribute();
				attrib.setAttributeid(cursor.getInt(1));
				attrib.setAttributeType(cursor.getString(2));
				attrib.setControlType(cursor.getInt(3));
				if (lang.equalsIgnoreCase("sw")
						&& !TextUtils.isEmpty(cursor.getString(6))) {
					attrib.setAttributeName(cursor.getString(6));
				} else {
					attrib.setAttributeName(cursor.getString(4));
				}
				attrib.setListing(cursor.getInt(5));

				if (attrib.getControlType() == 5) {
					List<Option> optionList = new ArrayList<Option>();
					Option option = null;

					option = new Option();
					option.setOptionId(0L);
					option.setOptionName(contxt.getResources().getString(
							R.string.select));
					optionList.add(option);
					String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
							+ attrib.getAttributeid();
					Cursor cursor2 = database
							.rawQuery(selectQueryOptions, null);

					if (cursor2.moveToFirst()) {
						do {
							option = new Option();
							option.setOptionId(cursor2.getLong(0));
							StringBuffer multiLnagText = new StringBuffer();
							if (lang.equalsIgnoreCase("sw")
									&& !TextUtils.isEmpty(cursor2.getString(3))) {
								option.setOptionName(cursor2.getString(3));
							} else {
								option.setOptionName(cursor2.getString(2));
							}
							if (!TextUtils.isEmpty(cursor2.getString(3))) {
								multiLnagText.append(cursor2.getString(3));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							} else {
								multiLnagText.append(cursor2.getString(2));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							}
							optionList.add(option);
						} while (cursor2.moveToNext());
					}
					cursor2.close();
					attrib.setOptionsList(optionList);
				}
				attribList.add(attrib);

			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public String getOptionText(String optionID) {
		SQLiteDatabase db = getWritableDatabase();
		StringBuffer multiLnagText = new StringBuffer();
		String selectQueryOptions = "SELECT  OPTION_NAME,OPTION_NAME_OTHER FROM OPTIONS where OPTION_ID ="
				+ optionID;
		Cursor cursor = db.rawQuery(selectQueryOptions, null);
		if (cursor.moveToFirst()) {
			if (!TextUtils.isEmpty(cursor.getString(1))) {
				multiLnagText.append(cursor.getString(1));
				multiLnagText.append("&#&" + cursor.getString(0));
			} else {
				multiLnagText.append(cursor.getString(0));
				multiLnagText.append("&#&" + cursor.getString(0));
			}
		}
		return multiLnagText.toString();
	}

	public List<Attribute> getPersonAttribute(SQLiteDatabase database,
			String lang) {

		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryValues = "SELECT  * FROM ATTRIBUTE_MASTER where ATTRIBUTE_TYPE ='"
				+ CAT_NaturalPerson + "'";// ORDER BY ATTRIB_ID";

		Cursor cursor = database.rawQuery(selectQueryValues, null);
		// id ATTRIB_ID INTEGER ATTRIBUTE_TYPE STRING ATTRIBUTE_CONTROLTYPE
		// INTEGER ATTRIBUTE_NAME TEXT)";
		// 0 1 2 3 4
		Attribute attrib;
		if (cursor.moveToFirst()) {
			do {
				attrib = new Attribute();
				attrib.setAttributeid(cursor.getInt(1));
				attrib.setAttributeType(cursor.getString(2));
				attrib.setControlType(cursor.getInt(3));

				if (lang.equalsIgnoreCase("sw")
						&& !TextUtils.isEmpty(cursor.getString(6))) {
					attrib.setAttributeName(cursor.getString(6));
				} else {
					attrib.setAttributeName(cursor.getString(4));
				}
				attrib.setListing(cursor.getInt(5));

				if (attrib.getControlType() == 5) {
					List<Option> optionList = new ArrayList<Option>();
					Option option = null;

					option = new Option();
					option.setOptionId(0L);
					option.setOptionName(contxt.getResources().getString(
							R.string.select));
					optionList.add(option);

					String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
							+ attrib.getAttributeid();
					Cursor cursor2 = database
							.rawQuery(selectQueryOptions, null);

					if (cursor2.moveToFirst()) {
						do {
							option = new Option();
							option.setOptionId(cursor2.getLong(0));
							StringBuffer multiLnagText = new StringBuffer();
							if (lang.equalsIgnoreCase("sw")
									&& !TextUtils.isEmpty(cursor2.getString(3))) {
								option.setOptionName(cursor2.getString(3));
							} else {
								option.setOptionName(cursor2.getString(2));
							}
							if (!TextUtils.isEmpty(cursor2.getString(3))) {
								multiLnagText.append(cursor2.getString(3));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							} else {
								multiLnagText.append(cursor2.getString(2));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							}
							optionList.add(option);
						} while (cursor2.moveToNext());
					}
					cursor2.close();
					attrib.setOptionsList(optionList);
				}

				attrib.setValidation(cursor.getString(7));
				attribList.add(attrib);

			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public List<Option> getPersonForTenure(Long featureId) {
		SQLiteDatabase database = getReadableDatabase();
		List<Option> optionList = new ArrayList<Option>();
		try {
			String selectQueryQues = "select ID,ATTRIB_1 from PERSON AS P WHERE P.ID not in (SELECT PERSON_ID FROM SOCIAL_TENURE) and FEATURE_ID ="
					+ featureId;

			Cursor cursor = database.rawQuery(selectQueryQues, null);
			if (cursor.moveToFirst()) {
				do {
					Option option = new Option();

					option.setOptionId(cursor.getLong(0));
					option.setOptionName(cursor.getString(1));

					optionList.add(option);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return optionList;

	}

	public List<Attribute> getPersonList(Long featureId) {
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryQues = "SELECT * from PERSON where FEATURE_ID ="
				+ featureId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			do {
				Attribute attrib = new Attribute();
				attrib.setGroupId(cursor.getInt(0));
				String value1, value2;
				if (TextUtils.isEmpty(cursor.getString(2)))
					value2 = "";
				else
					value2 = cursor.getString(2);

				if (TextUtils.isEmpty(cursor.getString(1)))
					value1 = "";
				else
					value1 = cursor.getString(1);
				String fieldValue = value1 + " " + value2;
				attrib.setFieldValue(fieldValue);
				attribList.add(attrib);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;

	}

	public int getPersonMediaCount(long featureId) {
		int count = 0;
		SQLiteDatabase database = getReadableDatabase();

		String selectQueryQues = "SELECT * from PERSON_MEDIA where FEATURE_ID ="
				+ featureId;

		Cursor cursor = database.rawQuery(selectQueryQues, null);

		if (cursor.moveToFirst()) {
			do {
				count++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		close();
		return count;
	}

	public String getProjectDataForUpload() {
		String syncData = null;
		try {
			int featureId = 0;
			myDataBase = getReadableDatabase();
			String attributeFeatchSql = "SELECT  AM.attrib_id,FM.GROUP_ID,FM.ATTRIB_VALUE FROM ATTRIBUTE_MASTER AS AM "
					+ " LEFT OUTER JOIN FORM_VALUES AS FM ON AM.ATTRIB_ID =  FM.ATTRIB_ID and FM.FEATURE_ID = <feat_id>"
					+ " where AM.ATTRIBUTE_TYPE IN (<category>) and FM.ATTRIB_VALUE is not null order by group_id";

			// fetching Spatial feature DATA
			// FEATURE_ID,SERVER_FEATURE_ID,COORDINATES,GEOMTYPE,CREATEDTIME,STATUS,COMPLETEDTIME,SERVER_PK,IMEI,PERSON_TYPE
			// 0 1 2 3 4 5 6 7 8 9
			JSONArray spatialFeatures, attribsArr, atribsArrList;
			JSONArray attribValuesArr = new JSONArray();
			JSONObject featureAttribsObj = new JSONObject();
			JSONArray featureAttribsArr = new JSONArray();
			String projName = getProjectname();
			User user = getLoggedUser();
			String spatialFeatureSql = "SELECT * FROM SPATIAL_FEATURES where status = '"
					+ STATUS_COMPLETE
					+ "' and SERVER_FEATURE_ID IS NULL OR SERVER_FEATURE_ID = ''";
			Cursor cursor = myDataBase.rawQuery(spatialFeatureSql, null);
			if (cursor.moveToFirst()) {
				do {
					spatialFeatures = new JSONArray();
					attribValuesArr = new JSONArray();
					featureId = cursor.getInt(0);
					spatialFeatures.put(0, cursor.getInt(0));// featureid
					spatialFeatures.put(1, cursor.getString(3));// Geomtype
					spatialFeatures.put(2, cursor.getString(2));// Coordinates
					spatialFeatures.put(3, cursor.getString(4));// createdTime
					spatialFeatures.put(4, cursor.getString(6));// completedtime
					spatialFeatures.put(5, cursor.getString(8));// imei
					spatialFeatures.put(6, cursor.getString(9));// PersonType
					spatialFeatures.put(7, projName);// ProjectName
					spatialFeatures.put(8, user.getUserId());// user Name

					// Fetching GENERAL FEATURES
					atribsArrList = new JSONArray();
					String final_sql = attributeFeatchSql.replace("<feat_id>",
							featureId + "").replace("<category>",
							CAT_General + "," + CAT_General_Property);
					Cursor cursor_attrib = myDataBase.rawQuery(final_sql, null);
					if (cursor_attrib.moveToFirst()) {
						do {
							attribsArr = new JSONArray();
							attribsArr.put(0, featureId);// featureid
							attribsArr.put(1, cursor_attrib.getString(1));// groupid
							attribsArr.put(2, cursor_attrib.getString(0));// attribID
							attribsArr.put(3, cursor_attrib.getString(2));// attribvalue

							atribsArrList.put(attribsArr);
						} while (cursor_attrib.moveToNext());
						cursor_attrib.close();
					}
					attribValuesArr.put(0, atribsArrList); // putting general at
															// 0

					// Fetching NATURAL FEATURES
					atribsArrList = new JSONArray();
					final_sql = attributeFeatchSql.replace("<feat_id>",
							featureId + "").replace("<category>",
							CAT_NaturalPerson);
					cursor_attrib = myDataBase.rawQuery(final_sql, null);
					if (cursor_attrib.moveToFirst()) {
						do {
							attribsArr = new JSONArray();
							attribsArr.put(0, featureId);// featureid
							attribsArr.put(1, cursor_attrib.getString(1));// groupid
							attribsArr.put(2, cursor_attrib.getString(0));// attribID

							// ////////// Hardcode for boolean control type
							// ///////////
							if (cursor_attrib.getString(2).equalsIgnoreCase(
									"Ndiyo")) {
								attribsArr.put(3, "yes");// attribvalue
							} else if (cursor_attrib.getString(2)
									.equalsIgnoreCase("Hapana")) {
								attribsArr.put(3, "no");// attribvalue
							} else {
								attribsArr.put(3, cursor_attrib.getString(2));// attribvalue
							}

							atribsArrList.put(attribsArr);
						} while (cursor_attrib.moveToNext());
						cursor_attrib.close();
					}
					attribValuesArr.put(1, atribsArrList); // putting natural at
															// 1

					// Fetching NON NATURAL FEATURES
					atribsArrList = new JSONArray();
					final_sql = attributeFeatchSql.replace("<feat_id>",
							featureId + "").replace("<category>",
							CAT_NonNaturalPerson);
					cursor_attrib = myDataBase.rawQuery(final_sql, null);
					if (cursor_attrib.moveToFirst()) {
						do {
							attribsArr = new JSONArray();
							attribsArr.put(0, featureId);// featureid
							attribsArr.put(1, cursor_attrib.getString(1));// groupid
							attribsArr.put(2, cursor_attrib.getString(0));// attribID
							attribsArr.put(3, cursor_attrib.getString(2));// attribvalue

							atribsArrList.put(attribsArr);
						} while (cursor_attrib.moveToNext());
						cursor_attrib.close();
					}
					attribValuesArr.put(2, atribsArrList); // putting
															// non-natural at 2

					// Fetching TENURE FEATURES
					atribsArrList = new JSONArray();
					final_sql = attributeFeatchSql.replace("<feat_id>",
							featureId + "").replace("<category>", CAT_Tenure);
					cursor_attrib = myDataBase.rawQuery(final_sql, null);
					if (cursor_attrib.moveToFirst()) {
						do {
							attribsArr = new JSONArray();
							attribsArr.put(0, featureId);// featureid
							attribsArr.put(1, cursor_attrib.getString(1));// groupid
							attribsArr.put(2, cursor_attrib.getString(0));// attribID
							attribsArr.put(3, cursor_attrib.getString(2));// attribvalue

							atribsArrList.put(attribsArr);
						} while (cursor_attrib.moveToNext());
						cursor_attrib.close();
					}
					attribValuesArr.put(3, atribsArrList); // putting tenure at
															// 3

					// Fetching CUSTOM FEATURES
					atribsArrList = new JSONArray();
					final_sql = attributeFeatchSql.replace("<feat_id>",
							featureId + "").replace("<category>", CAT_Custom);
					cursor_attrib = myDataBase.rawQuery(final_sql, null);
					if (cursor_attrib.moveToFirst()) {
						do {
							attribsArr = new JSONArray();
							attribsArr.put(0, featureId);// featureid
							attribsArr.put(1, cursor_attrib.getString(1));// groupid
							attribsArr.put(2, cursor_attrib.getString(0));// attribID
							attribsArr.put(3, cursor_attrib.getString(2));// attribvalue

							atribsArrList.put(attribsArr);
						} while (cursor_attrib.moveToNext());
						cursor_attrib.close();
					}
					attribValuesArr.put(4, atribsArrList); // putting custom at
															// 4

					featureAttribsObj = new JSONObject();
					featureAttribsObj.put("SpatialFeatures", spatialFeatures);
					featureAttribsObj.put("AttributeValue", attribValuesArr);
					featureAttribsArr.put(featureAttribsObj);
				} while (cursor.moveToNext());
				cursor.close();
				close();
			}
			if (featureAttribsArr.length() != 0) {
				syncData = featureAttribsArr.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return syncData;
	}

	public String getProjectname() {
		String projectName = "";
		myDataBase = getReadableDatabase();
		String q = "select project_name from PROJECT_SPATIAL_DATA LIMIT 1";
		Cursor cursor = myDataBase.rawQuery(q, null);

		if (cursor.moveToFirst()) {
			projectName = cursor.getString(0);
			cursor.close();
		}
		return projectName;
	}

	public List<ProjectSpatialDataDto> getProjectSpatialData() {
		SQLiteDatabase database = getReadableDatabase();
		List<ProjectSpatialDataDto> projectSpatialList = new ArrayList<ProjectSpatialDataDto>();
		String selectQueryQues = "SELECT * from PROJECT_SPATIAL_DATA order by SERVER_PK";
		Cursor cursor = database.rawQuery(selectQueryQues, null);
		if (cursor.moveToFirst()) {
			try {
				do {
					ProjectSpatialDataDto projectSpatialData = new ProjectSpatialDataDto();
					projectSpatialData.setServer_Pk(cursor.getInt(0));
					projectSpatialData.setProject_Name(cursor.getString(1));
					projectSpatialData.setFile_Name(cursor.getString(2));
					projectSpatialData.setFile_Ext(cursor.getString(3));
					projectSpatialData.setAlias(cursor.getString(4));
					projectSpatialList.add(projectSpatialData);
				} while (cursor.moveToNext());
			} catch (Exception e) {
				cf.appLog("", e);
				e.printStackTrace();
			}
		}
		cursor.close();
		close();
		return projectSpatialList;
	}

	public List<Attribute> getTenureAttribute(SQLiteDatabase database,
			String lang) {

		List<Attribute> attribList = new ArrayList<Attribute>();
		String selectQueryValues = "SELECT  * FROM ATTRIBUTE_MASTER where ATTRIBUTE_TYPE ='"
				+ CAT_Tenure + "'";

		Cursor cursor = database.rawQuery(selectQueryValues, null);
		// id ATTRIB_ID INTEGER ATTRIBUTE_TYPE STRING ATTRIBUTE_CONTROLTYPE
		// INTEGER ATTRIBUTE_NAME TEXT)";
		// 0 1 2 3
		Attribute attrib;
		if (cursor.moveToFirst()) {
			do {
				attrib = new Attribute();
				attrib.setAttributeid(cursor.getInt(1));
				attrib.setAttributeType(cursor.getString(2));
				attrib.setControlType(cursor.getInt(3));
				if (lang.equalsIgnoreCase("sw")
						&& !TextUtils.isEmpty(cursor.getString(6))) {
					attrib.setAttributeName(cursor.getString(6));
				} else {
					attrib.setAttributeName(cursor.getString(4));
				}
				attrib.setListing(cursor.getInt(5));
				if (attrib.getControlType() == 5) {
					List<Option> optionList = new ArrayList<Option>();
					Option option = null;

					option = new Option();
					option.setOptionId(0L);
					option.setOptionName(contxt.getResources().getString(
							R.string.select));
					optionList.add(option);

					String selectQueryOptions = "SELECT  * FROM OPTIONS where ATTRIB_ID ="
							+ attrib.getAttributeid();
					Cursor cursor2 = database
							.rawQuery(selectQueryOptions, null);
					if (cursor2.moveToFirst()) {
						do {
							option = new Option();
							option.setOptionId(cursor2.getLong(0));
							StringBuffer multiLnagText = new StringBuffer();
							if (lang.equalsIgnoreCase("sw")
									&& !TextUtils.isEmpty(cursor2.getString(3))) {
								option.setOptionName(cursor2.getString(3));
							} else {
								option.setOptionName(cursor2.getString(2));
							}
							if (!TextUtils.isEmpty(cursor2.getString(3))) {
								multiLnagText.append(cursor2.getString(3));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							} else {
								multiLnagText.append(cursor2.getString(2));
								multiLnagText.append("&#&"
										+ cursor2.getString(2));
								option.setOptionMultiLang(multiLnagText
										.toString());
							}
							optionList.add(option);
						} while (cursor2.moveToNext());
					}
					cursor2.close();
					attrib.setOptionsList(optionList);
				}
				attrib.setValidation(cursor.getString(7));
				attribList.add(attrib);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return attribList;
	}

	public List<Attribute> getTenureList(Long featureId, String lang) {
		SQLiteDatabase database = getReadableDatabase();
		List<Attribute> attribList = new ArrayList<Attribute>();
		boolean isNonNatural = IsNonNaturalPerson(featureId);

		if (isNonNatural) {

			/*
			 * //String selectQueryQues ="SELECT * from SOCIAL_TENURE AS ST" +
			 * " left join peRSON AS P ON ST.PERSON_ID=P.ID" +
			 * " where ST.FEATURE_ID ="+ featureId;
			 */

			String selectQueryQues = "select ST.ID,ST.PERSON_ID,FV.ATTRIB_VALUE,ST.FEATURE_ID,FV.ATTRIB_ID from soCIAL_TENURE as ST"
					+ " LEFT JOIN FORM_VALUES AS FV ON  FV.GROUP_ID=ST.PERSON_ID AND ST.FEATURE_ID = '"
					+ featureId
					+ "'"
					+ " LEFT JOIN ATTRIBUTE_MASTER AS AM ON AM.ATTRIB_ID=FV.ATTRIB_ID"
					+ " WHERE LISTING = '1'";

			try {
				Cursor cursor = database.rawQuery(selectQueryQues, null);
				if (cursor.moveToFirst()) {
					do {
						StringBuffer fieldValue = new StringBuffer();
						Attribute attrib = new Attribute();
						attrib.setGroupId(cursor.getInt(0));
						attrib.setPeronId(cursor.getInt(1));
						String value1;
						if (TextUtils.isEmpty(cursor.getString(2)))
							value1 = "";
						else
							value1 = cursor.getString(2);
						if (lang != null) {
							try {
								if (!value1.isEmpty()) {
									if (lang.equalsIgnoreCase("sw"))
										fieldValue.append(
												value1.split("&#&")[0]).append(
												" ");
									else
										fieldValue.append(
												value1.split("&#&")[1]).append(
												" ");

								}
							} catch (Exception e) {
								fieldValue.append(value1);
							}
						} else
							fieldValue.append(value1);
						attrib.setFieldValue(fieldValue.toString());
						attrib.setPeronId(cursor.getInt(3));

						attribList.add(attrib);
					} while (cursor.moveToNext());
				}
				cursor.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			String selectQueryQues = "SELECT * from SOCIAL_TENURE AS ST"
					+ " left join peRSON AS P ON ST.PERSON_ID=P.ID"
					+ " where ST.FEATURE_ID =" + featureId;

			Cursor cursor = database.rawQuery(selectQueryQues, null);
			if (cursor.moveToFirst()) {
				do {
					StringBuffer fieldValue = new StringBuffer();
					Attribute attrib = new Attribute();
					attrib.setGroupId(cursor.getInt(0));
					attrib.setPeronId(cursor.getInt(3));
					String value1, value2;

					if (TextUtils.isEmpty(cursor.getString(8)))
						value2 = "";
					else
						value2 = cursor.getString(8);

					if (TextUtils.isEmpty(cursor.getString(7)))
						value1 = "";
					else
						value1 = cursor.getString(7);
					if (lang != null) {
						try {
							if (!value1.isEmpty()) {
								if (lang.equalsIgnoreCase("sw"))
									fieldValue.append(value1.split("&#&")[0])
											.append(" ");
								else
									fieldValue.append(value1.split("&#&")[1])
											.append(" ");

							}
							if (!value2.isEmpty()) {
								if (lang.equalsIgnoreCase("sw"))
									fieldValue.append(value2.split("&#&")[0]);
								else
									fieldValue.append(value1.split("&#&")[1])
											.append(" ");
							}
						} catch (Exception e) {
							fieldValue.append(value1 + " " + value2);
						}
					} else
						fieldValue.append(value1 + " " + value2);
					attrib.setFieldValue(fieldValue.toString());
					attrib.setPeronId(cursor.getInt(3));

					attribList.add(attrib);
				} while (cursor.moveToNext());
			}
			cursor.close();

		}

		return attribList;
	}

	public String getVerifiedFeaturesForUpload() {
		String q = "SELECT SERVER_FEATURE_ID FROM SPATIAL_FEATURES where status = '"
				+ STATUS_VERIFIED + "'";
		JSONObject json_obj = new JSONObject();
		JSONArray usins = new JSONArray();
		try {
			myDataBase = getReadableDatabase();
			Long userid = getLoggedUser().getUserId();
			Cursor cursor = myDataBase.rawQuery(q, null);
			if (cursor.moveToFirst()) {
				do {
					usins.put(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			cursor.close();
			if (usins.length() > 0) {
				json_obj.put(userid.toString(), usins);
			} else
				return "";
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		return json_obj.toString();
	}

	public boolean inserPersontMedia(Media MediaValue) {
		SQLiteDatabase database = getWritableDatabase();
		try {
			// PERSON_ID TEXT,TYPE TEXT,PATH TEXT,FEATURE_ID TEXT
			ContentValues values = new ContentValues();
			values.put("PERSON_ID", MediaValue.getMediaId());
			values.put("TYPE", MediaValue.getMediaType());
			values.put("FEATURE_ID", MediaValue.getFeatureId());
			values.put("PATH", MediaValue.getMediaPath());

			database.insert("PERSON_MEDIA", null, values);

		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		} finally {
			database.close();
		}
		return true;
	}

	public void insertMedia(Media MediaValue) {
		SQLiteDatabase database = getWritableDatabase();
		try {
			// FEATURE_ID TEXT,TYPE TEXT,PATH TEXT
			ContentValues values = new ContentValues();
			values.put("FEATURE_ID", MediaValue.getFeatureId());
			values.put("TYPE", MediaValue.getMediaType());
			values.put("PATH", MediaValue.getMediaPath());
			values.put("MEDIA_ID", MediaValue.getMediaId());
			database.insert("MEDIA", null, values);

			System.out.println("Data Inserted");
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		database.close();
	}

	private boolean insertSocialTenure(int i, int personId, JSONArray arry,
			Long featureId) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			JSONArray attribSocialTenureDetailsArr = new JSONArray(arry.get(1)
					.toString());
			int groupId = getNewGroupId();

			// For insert into social tenure
			ContentValues attribListing = new ContentValues();
			attribListing.put("ID", groupId);
			attribListing.put("FEATURE_ID", featureId);

			JSONArray attribChildArr = new JSONArray(
					attribSocialTenureDetailsArr.get(i).toString());

			for (int j = 0; j < attribChildArr.length(); j++) {
				JSONArray attribChildValuesArr = new JSONArray(attribChildArr
						.get(j).toString());
				ContentValues attribValues = new ContentValues();
				attribValues.put("GROUP_ID", groupId);
				attribValues.put("ATTRIB_ID", attribChildValuesArr.getInt(0));
				attribValues.put("ATTRIB_VALUE",
						attribChildValuesArr.getString(1));
				attribValues.put("FEATURE_ID", featureId);

				db.insert("FORM_VALUES", null, attribValues);

				if (attribChildValuesArr.getString(3).equalsIgnoreCase("5")) // checking
																				// for
																				// control
																				// type
				{
					String optionId = attribChildValuesArr.getString(1);
					String optionText = getOptionText(optionId);
					if (attribChildValuesArr.getString(2).equalsIgnoreCase("1")) {
						attribListing.put("ATTRIB_1", optionText);
						attribListing.put("PERSON_ID", personId);
					}
					if (attribChildValuesArr.getString(2).equalsIgnoreCase("2")) {
						attribListing.put("ATTRIB_2", optionText);
						attribListing.put("PERSON_ID", personId);
					}
				}
			}
			db.insert("SOCIAL_TENURE", null, attribListing);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	// MOVE CONTENT VALUES HERE
	public void InsertValues(List<ContentValues> valueList, String TableName) {
		SQLiteDatabase database = getWritableDatabase();

		int rows = database.delete(TableName, "1", null);
		System.out.println(rows + " rows deleted from table " + TableName);

		try {
			for (ContentValues contentValues : valueList) {
				database.insert(TableName, null, contentValues);

			}
			System.out.println("Data Inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.close();
	}

	public boolean IsCustomAttribute() {
		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		String spatialFeatureSql = "SELECT * from ATTRIBUTE_MASTER WHERE ATTRIBUTE_TYPE='"
				+ CAT_Custom + "'";
		Cursor cursor = database.rawQuery(spatialFeatureSql, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	public boolean IsCustomAttributeValue(long featureId) {
		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		String spatialFeatureSql = "SELECT * FROM FORM_VALUES WHERE ATTRIB_ID = (SELECT ATTRIB_ID from ATTRIBUTE_MASTER WHERE ATTRIBUTE_TYPE='"
				+ CAT_Custom + "') and FEATURE_ID =" + featureId;
		Cursor cursor = database.rawQuery(spatialFeatureSql, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	public boolean IsNonNaturalPerson(long featureId) {
		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		String spatialFeatureSql = "SELECT * FROM SPATIAL_FEATURES where PERSON_TYPE = '"
				+ NonNaturalPerson + "' and FEATURE_ID =" + featureId;
		Cursor cursor = database.rawQuery(spatialFeatureSql, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	public boolean IsPropertyAttribute() {
		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		String spatialFeatureSql = "SELECT * from ATTRIBUTE_MASTER WHERE ATTRIBUTE_TYPE='"
				+ CAT_General_Property + "'";
		Cursor cursor = database.rawQuery(spatialFeatureSql, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	public boolean IsPropertyAttribValue(long featureId) {
		boolean flag = false;
		SQLiteDatabase database = getReadableDatabase();
		String spatialFeatureSql = "SELECT * FROM FORM_VALUES WHERE ATTRIB_ID = (SELECT ATTRIB_ID from ATTRIBUTE_MASTER WHERE ATTRIBUTE_TYPE='"
				+ CAT_General_Property + "') and FEATURE_ID =" + featureId;
		Cursor cursor = database.rawQuery(spatialFeatureSql, null);
		if (cursor.moveToFirst()) {
			flag = true;
		}
		cursor.close();
		return flag;
	}

	// String spatialFeatureSql =
	// "SELECT * FROM SPATIAL_FEATURES where status = '"+STATUS_COMPLETE+"' and SERVER_FEATURE_ID IS NULL OR SERVER_FEATURE_ID = ''";

	public boolean markFeatureAsComplete(Long featureid) {
		SQLiteDatabase database = getWritableDatabase();
		ContentValues values = new ContentValues();
		String whereClause = "FEATURE_ID = " + featureid;
		try {
			String time = sdf.format(new Date());
			values.put("status", STATUS_COMPLETE);
			values.put("completedtime", time);

			database.update("SPATIAL_FEATURES", values, whereClause, null);
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean markFeatureAsVerified(Long featureid) {
		SQLiteDatabase database = getWritableDatabase();
		ContentValues values = new ContentValues();
		String whereClause = "FEATURE_ID = " + featureid;
		try {
			String time = sdf.format(new Date());
			values.put("status", STATUS_VERIFIED);
			values.put("completedtime", time);

			database.update("SPATIAL_FEATURES", values, whereClause, null);
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// myDataBase = getWritableDatabase();

		// CONTROL 1=STRING,2=DATE , 3=BOOLEAN 4=Numeric 5=SPINNER

		String query_table1 = "CREATE TABLE SPATIAL_FEATURES (FEATURE_ID INTEGER PRIMARY KEY AUTOINCREMENT,SERVER_FEATURE_ID TEXT,COORDINATES TEXT,GEOMTYPE TEXT,CREATEDTIME TEXT,STATUS TEXT,COMPLETEDTIME TEXT,SERVER_PK TEXT,IMEI TEXT,PERSON_TYPE TEXT)";
		String query_table2 = "CREATE TABLE ATTRIBUTE_MASTER(ID INTEGER PRIMARY KEY AUTOINCREMENT,ATTRIB_ID INTEGER,ATTRIBUTE_TYPE STRING,ATTRIBUTE_CONTROLTYPE INTEGER,ATTRIBUTE_NAME TEXT,LISTING TEXT,ATTRIBUTE_NAME_OTHER TEXT,VALIDATION TEXT)";
		String query_table3 = "CREATE TABLE OPTIONS(OPTION_ID TEXT,ATTRIB_ID INTEGER,OPTION_NAME TEXT,OPTION_NAME_OTHER TEXT)";
		String query_table4 = "CREATE TABLE FORM_VALUES(GROUP_ID INTEGER,ATTRIB_ID INTEGER,ATTRIB_VALUE TEXT,FEATURE_ID TEXT)";
		String query_table5 = "CREATE TABLE USER(USER_ID TEXT,USER_NAME TEXT,PASSWORD TEXT,ROLE_ID TEXT,ROLE_NAME TEXT)";
		String query_table6 = "CREATE TABLE BOOKMARKS(ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,LATITUDE TEXT,LONGITUDE TEXT,ZOOMLEVEL TEXT)";
		String query_table7 = "CREATE TABLE MEDIA(MEDIA_ID INTEGER PRIMARY KEY,FEATURE_ID TEXT,TYPE TEXT,PATH TEXT,ATTRIB_1 TEXT,ATTRIB_2 TEXT,SYNCED INTEGER DEFAULT 0)";
		String query_table8 = "CREATE TABLE PERSON(ID INTEGER PRIMARY KEY,ATTRIB_1 TEXT,ATTRIB_2 TEXT,FEATURE_ID TEXT,SERVER_PK TEXT)";

		String query_table10 = "CREATE TABLE SOCIAL_TENURE(ID INTEGER PRIMARY KEY,ATTRIB_1 TEXT,ATTRIB_2 TEXT,PERSON_ID INTEGER,FEATURE_ID TEXT,SERVER_PK TEXT)";
		String query_table11 = "CREATE TABLE GROUPID_SEQ(VALUE INTEGER)";
		String query_table12 = "CREATE TABLE PERSON_MEDIA(ID INTEGER PRIMARY KEY AUTOINCREMENT,PERSON_ID TEXT,TYPE TEXT,PATH TEXT,FEATURE_ID TEXT,SYNCED INTEGER DEFAULT 0)";
		String query_table13 = "CREATE TABLE PROJECT_SPATIAL_DATA(SERVER_PK INTEGER,PROJECT_NAME TEXT,FILE_NAME TEXT,FILE_EXT TEXT,ALIAS TEXT)";
		try {
			dropTable(db, "SPATIAL_FEATURES");
			db.execSQL(query_table1);
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		try {
			dropTable(db, "ATTRIBUTE_MASTER");
			db.execSQL(query_table2);

		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}
		try {
			db.execSQL(query_table3);
			db.execSQL(query_table4);
			db.execSQL(query_table5);
			db.execSQL(query_table6);
			db.execSQL(query_table7);
			db.execSQL(query_table8);

			db.execSQL(query_table10);
			db.execSQL(query_table11);
			db.execSQL(query_table12);
			db.execSQL(query_table13);
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
		}

		// insertValues(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public boolean removeLoggedUser() {
		SQLiteDatabase database = getWritableDatabase();
		try {

			int rows = database.delete("USER", null, null);
			cleandb(database);
			close();
		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean resetMediaStatus() {
		SQLiteDatabase db = getWritableDatabase();
		// updating synced status

		String whereClauseForMedia = "SYNCED = "
				+ CommonFunctions.MEDIA_SYNC_ERROR;
		String whereClauseForPersonMedia = "SYNCED = "
				+ CommonFunctions.MEDIA_SYNC_ERROR;
		ContentValues value = new ContentValues();
		value.put("SYNCED", CommonFunctions.MEDIA_SYNC_PENDING);

		int updatedMediaRow = db.update("MEDIA", value, whereClauseForMedia,
				null);
		int updatedPersonRow = db.update("PERSON_MEDIA", value,
				whereClauseForPersonMedia, null);

		close();
		if (updatedMediaRow < 1 && updatedPersonRow < 1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean saveBookmark(Bookmark bkmrk) {
		try {
			// query to remove the oldest bookmarks if more than 5 are in the
			// database
			String removeSql = "id = (SELECT id FROM BOOKMARKS order by id desc LIMIT 1 OFFSET 5)";
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("NAME", bkmrk.getName());
			values.put("LATITUDE", bkmrk.getLatitude());
			values.put("LONGITUDE", bkmrk.getLongitude());
			values.put("ZOOMLEVEL", bkmrk.getZoomlevel());

			db.insert("BOOKMARKS", null, values);

			db.delete("BOOKMARKS", removeSql, null);

		} catch (Exception e) {
			cf.appLog("", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean saveFormDataTemp(List<Attribute> attribList, int groupid,
			long featureId, String keyword) {
		ContentValues values = new ContentValues();
		List<ContentValues> valueList = new ArrayList<ContentValues>();
		SQLiteDatabase db = getWritableDatabase();
		// "CREATE TABLE FORM_VALUES(ATTRIB_ID INTEGER,ATTRIB_VALUE TEXT,OPTION_ID TEXT,OPTION_VALUE TEXT,FEATURE_ID)";
		if (groupid != 0) {
			try {
				String deleteWhereSql = "GROUP_ID=" + groupid;
				db.delete("FORM_VALUES", deleteWhereSql, null);

				String deleteDataWhereID = "ID=" + groupid;
				db.delete("PERSON", deleteDataWhereID, null);

				db.delete("SOCIAL_TENURE", deleteDataWhereID, null);

				// for adding values in person,property,socialTenure and media

				ContentValues tableValues = new ContentValues();
				tableValues.put("ID", groupid);
				tableValues.put("FEATURE_ID", featureId);

				for (Attribute attribute : attribList) {
					if (attribute.getFieldValue() != null) {
						values = new ContentValues();
						values.put("GROUP_ID", groupid);
						values.put("ATTRIB_ID", attribute.getAttributeid());
						values.put("ATTRIB_VALUE", attribute.getFieldValue());
						values.put("FEATURE_ID", featureId);
						valueList.add(values);
						if (attribute.getListing() == 1)
							tableValues.put("ATTRIB_1",
									attribute.getFieldValue());
						if (attribute.getListing() == 2)
							tableValues.put("ATTRIB_2",
									attribute.getFieldValue());
					}
				}

				for (ContentValues contentValues : valueList) {
					db.insert("FORM_VALUES", null, contentValues);
				}

				if (keyword.equalsIgnoreCase("PERSON")) {
					db.insert("PERSON", null, tableValues);
				}

				else if (keyword.equalsIgnoreCase("SOCIAL_TENURE")) {

					db.insert("SOCIAL_TENURE", null, tableValues);

				} else if (keyword.equalsIgnoreCase("MEDIA")) {
					tableValues.remove("ID");
					db.update("MEDIA", tableValues, "MEDIA_ID = " + groupid,
							null);

				}

				else if (keyword.equalsIgnoreCase("Natural")
						|| keyword.equalsIgnoreCase("Non-Natural")) {
					ContentValues valuesForpersonType = new ContentValues();
					valuesForpersonType.put("PERSON_TYPE", keyword);
					valueList.add(values);
					db.update("SPATIAL_FEATURES", valuesForpersonType,
							"FEATURE_ID = " + featureId, null);

				}

				return true;

			} catch (Exception e) {
				cf.appLog("", e);
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public Long saveNewFeature(String geomtype, String wKTStr, String imei) {
		Long featureId = 0L;
		try {
			SQLiteDatabase db = getWritableDatabase();
			// Inserting into Features
			String time = sdf.format(new Date());
			ContentValues value = new ContentValues();
			value.put("coordinates", wKTStr);
			value.put("geomtype", geomtype);
			value.put("createdtime", time);
			value.put("status", STATUS_DRAFT);
			value.put("IMEI", imei);

			db.insert("SPATIAL_FEATURES", null, value);

			featureId = getNewFeatureId();

		} catch (Exception e) {
			e.printStackTrace();
			cf.appLog("", e);
			return featureId;
		}
		return featureId;
	}

	public boolean saveProjectData(String data) {
		SQLiteDatabase database = getWritableDatabase();
		// ContentValues values = new ContentValues();

		try {
			JSONObject projectdata = new JSONObject(data);
			database.delete("PROJECT_SPATIAL_DATA", null, null);
			database.delete("OPTIONS", null, null);
			database.delete("ATTRIBUTE_MASTER", null, null);
			if (projectdata.has("SpatialData")) {
				ContentValues projectValues = new ContentValues();
				JSONArray project_info = projectdata
						.getJSONArray("SpatialData");
				if (project_info.length() > 0) {
					for (int i = 0; i < project_info.length(); i++) {
						JSONObject project_detail = new JSONObject(project_info
								.get(i).toString());
						projectValues.put("SERVER_PK",
								project_detail.getInt("id"));
						projectValues.put("PROJECT_NAME",
								project_detail.getString("name"));
						projectValues.put("FILE_NAME",
								project_detail.getString("fileName"));
						projectValues.put("FILE_EXT",
								project_detail.getString("fileExtension"));
						projectValues.put("ALIAS",
								project_detail.getString("alias"));

						database.insert("PROJECT_SPATIAL_DATA", null,
								projectValues);
					}
				}
			}
			if (projectdata.has("Attributes")) {
				ContentValues attributeValues = new ContentValues();
				JSONArray attribute_info = projectdata
						.getJSONArray("Attributes");
				if (attribute_info.length() > 0) {
					for (int i = 0; i < attribute_info.length(); i++) {
						JSONObject attribute_detail = new JSONObject(
								attribute_info.get(i).toString());
						attributeValues.put("ATTRIB_ID",
								attribute_detail.getInt("id"));
						if (attribute_detail.has("attributeCategory")) {
							JSONObject attributeCategory = attribute_detail
									.getJSONObject("attributeCategory");
							attributeValues.put("ATTRIBUTE_TYPE",
									attributeCategory
											.getString("attributecategoryid"));
						}
						if (attribute_detail.has("datatypeIdBean")) {
							JSONObject datatypeIdBean = attribute_detail
									.getJSONObject("datatypeIdBean");
							attributeValues.put("ATTRIBUTE_CONTROLTYPE",
									datatypeIdBean.getInt("datatypeId"));
							if (datatypeIdBean.getInt("datatypeId") == 5) {
								if (!attribute_detail.getString(
										"attributeOptions").equalsIgnoreCase(
										"null")) {

									JSONArray attributeOptions = attribute_detail
											.getJSONArray("attributeOptions");
									if (attributeOptions.length() > 0) {
										for (int j = 0; j < attributeOptions
												.length(); j++) {
											ContentValues option_value = new ContentValues();
											JSONObject optionValues = attributeOptions
													.getJSONObject(j);
											option_value.put("OPTION_ID",
													optionValues
															.getString("id"));
											option_value
													.put("ATTRIB_ID",
															optionValues
																	.getInt("attributeId"));
											option_value
													.put("OPTION_NAME",
															optionValues
																	.getString("optiontext"));
											option_value
													.put("OPTION_NAME_OTHER",
															optionValues
																	.getString("optiontext_second_language"));
											database.insert("OPTIONS", null,
													option_value);
											option_value.clear();
										}
									}
								}
							}
						}
						attributeValues.put("ATTRIBUTE_NAME",
								attribute_detail.getString("alias"));
						if (attribute_detail.has("listing")
								&& !TextUtils.isEmpty(attribute_detail
										.getString("listing"))
								&& !attribute_detail.getString("listing")
										.equalsIgnoreCase("null")) {
							attributeValues.put("LISTING",
									attribute_detail.getString("listing"));
						}
						if (attribute_detail.has("mandatory")
								&& !TextUtils.isEmpty(attribute_detail
										.getString("mandatory"))
								&& !attribute_detail.getString("mandatory")
										.equalsIgnoreCase("null")) {
							attributeValues.put("VALIDATION",
									attribute_detail.getString("mandatory"));
						}
						if (attribute_detail.has("alias_second_language")
								&& !TextUtils.isEmpty(attribute_detail
										.getString("alias_second_language"))
								&& !attribute_detail.getString(
										"alias_second_language")
										.equalsIgnoreCase("null")) {
							attributeValues
									.put("ATTRIBUTE_NAME_OTHER",
											attribute_detail
													.getString("alias_second_language"));
						}
						database.insert("ATTRIBUTE_MASTER", null,
								attributeValues);
						attributeValues.clear();
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			cf.syncLog("", e);
			return false;
		}
		return true;
	}

	public boolean saveProjectDataForAdjuticator(String data, String status) {
		if (data != null) {
			SQLiteDatabase db = getWritableDatabase();
			int groupId = 0;
			String personType = null;
			JSONObject jsonObj = null;
			/*
			 * db.delete("FORM_VALUES", null, null); db.delete("PERSON", null,
			 * null); db.delete("MEDIA", null, null);
			 * db.delete("SPATIAL_FEATURES", null, null);
			 * db.delete("PERSON_MEDIA", null, null); db.delete("SOCIAL_TENURE",
			 * null, null);
			 */

			try {
				jsonObj = new JSONObject(data);

				Iterator<String> iterator = jsonObj.keys();
				while (iterator.hasNext()) {
					try {
						db = getWritableDatabase();

						Long featureId = 0L;
						String key = iterator.next();
						String server_featureId = key;

						if (status.equalsIgnoreCase(STATUS_FINAL)) {
							ContentValues value = new ContentValues();
							String whereClause = "SERVER_FEATURE_ID = "
									+ server_featureId;
							value.put("STATUS", STATUS_FINAL);
							int rows = db.update("SPATIAL_FEATURES", value,
									whereClause, null);
							if (rows > 0) {
								continue;
							}
						} else {
							ContentValues value = new ContentValues();
							String whereClause = "SERVER_FEATURE_ID = "
									+ server_featureId;
							value.put("SERVER_FEATURE_ID", server_featureId);
							int rows = db.update("SPATIAL_FEATURES", value,
									whereClause, null);
							if (rows > 0) {
								continue;
							}
						}
						db.beginTransaction();
						ContentValues spatialFeatureValues = new ContentValues();
						ContentValues attribValues = new ContentValues();
						ContentValues attribListing = new ContentValues();
						spatialFeatureValues.clear();
						spatialFeatureValues.put("SERVER_FEATURE_ID",
								server_featureId);

						JSONArray arry = jsonObj.getJSONArray(key);

						if (((JSONArray) arry.get(3)).length() == 0) {
							personType = "Natural";
						} else if (((JSONArray) arry.get(3)).length() > 0) {
							personType = "Non-Natural";
						}
						attribValues.clear();
						// Spatial details[0]

						JSONArray spatialDetailsArr1 = new JSONArray(arry
								.get(0).toString());

						// Spatial details at index 0
						JSONObject spatialAttribDetailsJsonObj = new JSONObject(
								spatialDetailsArr1.get(0).toString());
						if (spatialAttribDetailsJsonObj.has("geometry")) {
							String geom = spatialAttribDetailsJsonObj
									.getString("geometry")
									.replaceAll(", ", ",");
							spatialFeatureValues.put("COORDINATES", geom);
						}
						//
						if (spatialAttribDetailsJsonObj.has("gtype")) {
							spatialFeatureValues.put("GEOMTYPE",
									spatialAttribDetailsJsonObj
											.getString("gtype"));
						}
						if (spatialAttribDetailsJsonObj.has("imeiNumber")) {
							spatialFeatureValues.put("IMEI",
									spatialAttribDetailsJsonObj
											.getString("imeiNumber"));
						}
						spatialFeatureValues.put("STATUS", status);
						// insert spatial data

						db.insert("SPATIAL_FEATURES", null,
								spatialFeatureValues);

						featureId = getNewFeatureId();

						// general attribute related data at index 1

						JSONArray attribDetailsArr = new JSONArray(
								spatialDetailsArr1.get(1).toString());
						if (attribDetailsArr != null) {
							groupId = getNewGroupId();
							for (int i = 0; i < attribDetailsArr.length(); i++) {
								JSONArray attribValuesArr = new JSONArray(
										attribDetailsArr.get(i).toString());
								attribValues.clear();
								attribValues.put("GROUP_ID", groupId);
								attribValues.put("ATTRIB_ID",
										attribValuesArr.getInt(0));
								attribValues.put("ATTRIB_VALUE",
										attribValuesArr.getString(1));
								attribValues.put("FEATURE_ID", featureId);

								db.insert("FORM_VALUES", null, attribValues);
							}
						}

						// fetching Social tenure data [1]

						/*
						 * JSONArray attribSocialTenureDetailsArr = new
						 * JSONArray(arry.get(1).toString()); for(int
						 * i=0;i<attribSocialTenureDetailsArr.length();i++) {
						 * groupId = cf.getGroupId(); JSONArray attribChildArr =
						 * new
						 * JSONArray(attribSocialTenureDetailsArr.get(i).toString
						 * ());
						 * 
						 * for(int j=0;j<attribChildArr.length();j++) {
						 * JSONArray attribChildValuesArr = new
						 * JSONArray(attribChildArr.get(j).toString());
						 * attribValues.clear();
						 * attribValues.put("GROUP_ID",groupId);
						 * attribValues.put
						 * ("ATTRIB_ID",attribChildValuesArr.getInt(0));
						 * attribValues
						 * .put("ATTRIB_VALUE",attribChildValuesArr.getString
						 * (1)); attribValues.put("FEATURE_ID",featureId);
						 * 
						 * db.insert("FORM_VALUES", null, attribValues);
						 * attribListing.clear();
						 * attribListing.put("ID",groupId);
						 * attribValues.put("FEATURE_ID",featureId);
						 * if(attribChildValuesArr
						 * .getString(2).equalsIgnoreCase("1")) {
						 * 
						 * attribListing.put("ATTRIB_1",attribChildValuesArr.
						 * getString(1)); db.insert("SOCIAL_TENURE", null,
						 * attribListing); }
						 * 
						 * else
						 * if(attribChildValuesArr.getString(2).equalsIgnoreCase
						 * ("2")) {
						 * attribListing.put("ATTRIB_2",attribChildValuesArr
						 * .getString(1)); db.insert("SOCIAL_TENURE", null,
						 * attribListing); }
						 * 
						 * 
						 * }
						 */

						// fetching Natural Person data [2] & Social Tenure [1]

						JSONArray attribNaturalPersonDetailsArr = new JSONArray(
								arry.get(2).toString());

						for (int i = 0; i < attribNaturalPersonDetailsArr
								.length(); i++) {

							int personId = 0;
							JSONArray attribChildArr = new JSONArray(
									attribNaturalPersonDetailsArr.get(i)
											.toString());

							groupId = getNewGroupId();

							// for adding to person table
							attribListing.clear();
							attribListing.put("ID", groupId);
							attribListing.put("FEATURE_ID", featureId);

							for (int j = 0; j < attribChildArr.length(); j++) {

								JSONArray attribChildValuesArr = new JSONArray(
										attribChildArr.get(j).toString());
								attribValues.clear();

								attribValues.put("GROUP_ID", groupId);
								attribValues.put("ATTRIB_ID",
										attribChildValuesArr.getInt(0));
								attribValues.put("ATTRIB_VALUE",
										attribChildValuesArr.getString(1));
								attribValues.put("FEATURE_ID", featureId);

								db.insert("FORM_VALUES", null, attribValues);

								if (attribChildValuesArr.getString(2)
										.equalsIgnoreCase("1")) // listing
								{
									attribListing.put("ATTRIB_1",
											attribChildValuesArr.getString(1));
								}

								if (attribChildValuesArr.getString(2)
										.equalsIgnoreCase("2")) {
									attribListing.put("ATTRIB_2",
											attribChildValuesArr.getString(1));
								}
								personId = groupId;
							}
							db.insert("PERSON", null, attribListing);
							if (personType.equalsIgnoreCase("Natural")) {
								insertSocialTenure(i, personId, arry, featureId);
							}
						}

						// fetching Non-Natural Person data [3]

						JSONArray attribNonNaturalPersonDetailsArr = new JSONArray(
								arry.get(3).toString());
						String whereClause = "SERVER_FEATURE_ID = "
								+ server_featureId;

						if (attribNonNaturalPersonDetailsArr.length() > 0) {
							spatialFeatureValues.put("PERSON_TYPE",
									"Non-Natural");
							db.update("SPATIAL_FEATURES", spatialFeatureValues,
									whereClause, null);
							for (int i = 0; i < attribNonNaturalPersonDetailsArr
									.length(); i++) {
								JSONArray attribChildArr = new JSONArray(
										attribNonNaturalPersonDetailsArr.get(i)
												.toString());
								groupId = getNewGroupId();
								int personId = 0;
								for (int j = 0; j < attribChildArr.length(); j++) {
									JSONArray attribChildValuesArr = new JSONArray(
											attribChildArr.get(j).toString());
									attribValues.clear();

									attribValues.put("GROUP_ID", groupId);
									attribValues.put("ATTRIB_ID",
											attribChildValuesArr.getInt(0));
									attribValues.put("ATTRIB_VALUE",
											attribChildValuesArr.getString(1));
									attribValues.put("FEATURE_ID", featureId);

									db.insert("FORM_VALUES", null, attribValues);

									personId = groupId;
								}

								insertSocialTenure(i, personId, arry, featureId);
							}
						} else if (attribNonNaturalPersonDetailsArr.length() == 0) {
							spatialFeatureValues.put("PERSON_TYPE", "Natural");
							db.update("SPATIAL_FEATURES", spatialFeatureValues,
									whereClause, null);
						}

						// fetching Media Data data [4]

						JSONArray attribMediaDetailsArr = new JSONArray(arry
								.get(4).toString());
						if (attribMediaDetailsArr.length() > 0) {
							for (int i = 0; i < attribMediaDetailsArr.length(); i++) {
								JSONArray attribChildArr = new JSONArray(
										attribMediaDetailsArr.get(i).toString());
								if (attribChildArr.length() > 0) {
									groupId = getNewGroupId();
									// For inserting values in MEDIA table
									attribListing.clear();
									attribListing.put("MEDIA_ID", groupId);
									attribListing.put("FEATURE_ID", featureId);

									for (int j = 0; j < attribChildArr.length(); j++) {

										JSONArray attribChildValuesArr = new JSONArray(
												attribChildArr.get(j)
														.toString());
										attribValues.clear();

										attribValues.put("GROUP_ID", groupId);
										attribValues.put("ATTRIB_ID",
												attribChildValuesArr.getInt(0));
										attribValues.put("ATTRIB_VALUE",
												attribChildValuesArr
														.getString(1));
										attribValues.put("FEATURE_ID",
												featureId);

										db.insert("FORM_VALUES", null,
												attribValues);

										if (attribChildValuesArr.getString(2)
												.equalsIgnoreCase("1")) {
											attribListing.put("ATTRIB_1",
													attribChildValuesArr
															.getString(1));
										}

										else if (attribChildValuesArr
												.getString(2).equalsIgnoreCase(
														"2")) {
											attribListing.put("ATTRIB_2",
													attribChildValuesArr
															.getString(1));
										}
									}
									db.insert("MEDIA", null, attribListing);
								}
							}
						}
						db.setTransactionSuccessful();
					} catch (Exception e) {
						cf.syncLog("", e);
						e.printStackTrace();
					} finally {
						if (db.inTransaction())
							db.endTransaction();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
			return true;

		}
		return false;

	}

	public boolean saveSocialTenureFormData(List<Attribute> attribList,
			int groupid, long featureId, String keyword, long personId) {
		ContentValues values = new ContentValues();
		List<ContentValues> valueList = new ArrayList<ContentValues>();
		SQLiteDatabase db = getWritableDatabase();
		// "CREATE TABLE FORM_VALUES(ATTRIB_ID INTEGER,ATTRIB_VALUE TEXT,OPTION_ID TEXT,OPTION_VALUE TEXT,FEATURE_ID)";
		if (groupid != 0) {
			try {
				String deleteWhereSql = "GROUP_ID=" + groupid;
				db.delete("FORM_VALUES", deleteWhereSql, null);

				String deleteDataWhereID = "ID=" + groupid;

				db.delete("SOCIAL_TENURE", deleteDataWhereID, null);

				// for adding values in person table
				ContentValues tableValues = new ContentValues();
				tableValues.put("ID", groupid);
				tableValues.put("FEATURE_ID", featureId);

				for (Attribute attribute : attribList) {
					values = new ContentValues();
					values.put("GROUP_ID", groupid);
					values.put("ATTRIB_ID", attribute.getAttributeid());
					values.put("ATTRIB_VALUE", attribute.getFieldValue());
					values.put("FEATURE_ID", featureId);
					valueList.add(values);

					if (attribute.getControlType() == 5) {
						if (attribute.getListing() == 1)
							tableValues.put("ATTRIB_1",
									attribute.getOptionText());

						if (attribute.getListing() == 2)
							tableValues.put("ATTRIB_2",
									attribute.getOptionText());
					} else {
						if (attribute.getListing() == 1)
							tableValues.put("ATTRIB_1",
									attribute.getFieldValue());

						if (attribute.getListing() == 2)
							tableValues.put("ATTRIB_2",
									attribute.getFieldValue());
					}
				}

				for (ContentValues contentValues : valueList) {
					db.insert("FORM_VALUES", null, contentValues);
				}

				if (keyword.equalsIgnoreCase("PERSON")) {
					db.insert("PERSON", null, tableValues);
				}

				else if (keyword.equalsIgnoreCase("SOCIAL_TENURE")) {

					tableValues.put("PERSON_ID", personId);
					db.insert("SOCIAL_TENURE", null, tableValues);

				} else if (keyword.equalsIgnoreCase("MEDIA")) {
					tableValues.remove("ID");
					db.update("MEDIA", tableValues, "MEDIA_ID = " + groupid,
							null);

				}

				else if (keyword.equalsIgnoreCase("Natural")
						|| keyword.equalsIgnoreCase("Non-Natural")) {
					ContentValues valuesForpersonType = new ContentValues();
					valuesForpersonType.put("PERSON_TYPE", keyword);
					valueList.add(values);
					db.update("SPATIAL_FEATURES", valuesForpersonType,
							"FEATURE_ID = " + featureId, null);

				}

				return true;

			} catch (Exception e) {
				cf.appLog("", e);
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public boolean setRejectedStatus(String json_string) {
		try {
			String sptialids = json_string.substring(1,
					json_string.length() - 1);
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("STATUS", STATUS_REJECTED);
			String sqlwhere = " SERVER_FEATURE_ID in (" + sptialids + ")";
			db.update("SPATIAL_FEATURES", values, sqlwhere, null);
		} catch (Exception e) {
			cf.syncLog("", e);
			e.printStackTrace();
			return false;
		} finally {
			close();
		}
		return true;
	}

	public boolean updateFeature(String wKTStr, Long featureId) {
		String whereClause = "FEATURE_ID = " + featureId;
		try {
			SQLiteDatabase db = getWritableDatabase();
			// updating Features
			ContentValues value = new ContentValues();
			value.put("coordinates", wKTStr);

			db.update("SPATIAL_FEATURES", value, whereClause, null);

		} catch (Exception e) {
			e.printStackTrace();
			cf.appLog("", e);
			return false;
		}
		return true;
	}

	public boolean updateMediaSyncedStatus(String mediaId, int syncStatus) {
		SQLiteDatabase db = getWritableDatabase();
		// updating synced status

		String whereClauseForMedia = "MEDIA_ID = " + mediaId;
		String whereClauseForPersonMedia = "ID = " + mediaId;
		ContentValues value = new ContentValues();
		value.put("SYNCED", syncStatus);

		int updatedMediaRow = db.update("MEDIA", value, whereClauseForMedia,
				null);
		int updatedPersonRow = db.update("PERSON_MEDIA", value,
				whereClauseForPersonMedia, null);

		close();
		if (updatedMediaRow < 1 && updatedPersonRow < 1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean updateServerFeatureId(String data) throws JSONException {
		if (data != null) {
			JSONObject jsonObj = new JSONObject(data);
			Iterator<String> iterator = jsonObj.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String featureId = key;
				String whereClause = "FEATURE_ID = " + featureId;
				try {
					if (jsonObj.get(key) instanceof JSONArray) {
						JSONArray arry = jsonObj.getJSONArray(key);
						int size = arry.length();
						for (int i = 0; i < size; i++) {
							arry.getJSONObject(i);
							String server_featureId = arry.getJSONObject(i)
									.toString();
							SQLiteDatabase db = getWritableDatabase();
							// updating Features
							ContentValues value = new ContentValues();
							value.put("SERVER_FEATURE_ID", server_featureId);
							db.update("SPATIAL_FEATURES", value, whereClause,
									null);
						}
					} else if (jsonObj.get(key) instanceof JSONObject) {
						jsonObj.getJSONObject(key);
						String server_featureId = jsonObj.getJSONObject(key)
								.toString();
						SQLiteDatabase db = getWritableDatabase();
						// updating Featureso
						ContentValues value = new ContentValues();
						value.put("SERVER_FEATURE_ID", server_featureId);
						db.update("SPATIAL_FEATURES", value, whereClause, null);

					} else {
						System.out.println("" + key + " : "
								+ jsonObj.optString(key));
						String server_featureId = jsonObj.optString(key);
						SQLiteDatabase db = getWritableDatabase();
						// updating Features
						ContentValues value = new ContentValues();
						value.put("SERVER_FEATURE_ID", server_featureId);
						int row = db.update("SPATIAL_FEATURES", value,
								whereClause, null);
						if (row < 1) {
							Toast.makeText(contxt, "0 rows updated",
									Toast.LENGTH_SHORT).show();
						}
					}

				} catch (Exception e) {
					System.out.println("" + key + " : "
							+ jsonObj.optString(key));
					e.printStackTrace();
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean updateSyncedVerifiedStatus(String data) {
		try {
			String sptialids = data.substring(1, data.length() - 1);
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("STATUS", STATUS_VERIFIED_SYNCED);
			String sqlwhere = " SERVER_FEATURE_ID in (" + sptialids + ")";
			db.update("SPATIAL_FEATURES", values, sqlwhere, null);
		} catch (Exception e) {
			cf.syncLog("", e);
			e.printStackTrace();
			return false;
		} finally {
			close();
		}
		return true;
	}

}