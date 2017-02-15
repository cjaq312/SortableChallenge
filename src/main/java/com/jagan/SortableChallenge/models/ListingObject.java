package com.jagan.SortableChallenge.models;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ListingObject {

	private JSONObject record;
	private String title;
	private JSONArray listings;
	private List<String> recordsArrayList;
	private JSONParser parser;

	public ListingObject() {
		record = new JSONObject();
		title = "";
		listings = new JSONArray();
		recordsArrayList = new ArrayList<String>();
		parser = new JSONParser();
	}

	public ListingObject(String title) {
		record = new JSONObject();
		this.title = title;
		listings = new JSONArray();
		recordsArrayList = new ArrayList<String>();
		parser = new JSONParser();
	}

	public void addRecordToArrayList(String record) {
		this.recordsArrayList.add(record);
	}

	public List<String> getRecordsArrayList() {
		return this.recordsArrayList;
	}

	public JSONObject finalizeListings() throws ParseException {

		// create jsonobject from jsonstring and add it to jsonarray
		for (String i : getRecordsArrayList()) {
			listings.add(itemize(i));
		}

		record.put("product_name", title);
		record.put("listings", listings);
		return record;
	}

	// to create jsonobject from jsonstring
	public JSONObject itemize(String json) throws ParseException {

		JSONObject itemized = new JSONObject();

		JSONObject rawObj = (JSONObject) parser.parse(json);
		itemized.put("title", rawObj.get("title"));
		itemized.put("manufacturer", rawObj.get("manufacturer"));
		itemized.put("currency", rawObj.get("currency"));
		itemized.put("price", rawObj.get("price"));

		return itemized;
	}

	public void reset() {
		record = new JSONObject();
		title = "";
		listings = new JSONArray();
		recordsArrayList.clear();
		parser = new JSONParser();
	}

	public boolean hasTitle() {
		return !title.isEmpty();
	}

	public boolean hasListings() {
		if (!getRecordsArrayList().isEmpty())
			return true;
		return false;
	}

	public JSONObject getRecord() {
		return record;
	}

	public void setRecord(JSONObject record) {
		this.record = record;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public JSONArray getListings() {
		return listings;
	}

	public void setListings(JSONArray listings) {
		this.listings = listings;
	}

}
