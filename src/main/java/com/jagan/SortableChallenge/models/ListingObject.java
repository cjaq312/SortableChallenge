package com.jagan.SortableChallenge.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListingObject {

	private JSONObject record;
	private String title;
	private JSONArray listings;
	private List<String> recordsArrayList;

	public ListingObject() {
		record = new JSONObject();
		title = "";
		listings = new JSONArray();
		recordsArrayList = new ArrayList<String>();
	}

	public ListingObject(String title) {
		record = new JSONObject();
		this.title = title;
		listings = new JSONArray();
		recordsArrayList = new ArrayList<String>();
	}

	public void addRecordToArrayList(String record) {
		this.recordsArrayList.add(record);
	}

	public List<String> getRecordsArrayList() {
		return this.recordsArrayList;
	}

	public JSONObject finalizeListings() {
		listings.put(getRecordsArrayList());
		record.put("title", title);
		record.put("listings", recordsArrayList);
		return record;
	}

	public void reset() {
		record = new JSONObject();
		title = "";
		listings = new JSONArray();
		recordsArrayList.clear();
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
		this.title=title;
	}

	public JSONArray getListings() {
		return listings;
	}

	public void setListings(JSONArray listings) {
		this.listings = listings;
	}

}
