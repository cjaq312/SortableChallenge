package com.jagan.SortableChallenge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jagan.SortableChallenge.utils.Sort;

public class Tester {

	public static void main(String[] args) {
		String outputPath = System.getProperty("user.dir") + File.separator + "data" + File.separator+"outputData"+ File.separator;
		String inputPath = System.getProperty("user.dir") + File.separator + "data" + File.separator+"inputData"+ File.separator;

		String productsPath = inputPath + "products.txt";
		String productsKey = "product_name";

		String listingsPath = inputPath + "listings.txt";
		String listingsKey = "title";

		String productsOutputPath = outputPath + "sortedProducts.txt";
		String matchingsOutputPath = outputPath + "productListingsOutput.txt";

		try {

			Sort.externalSort(productsPath, productsOutputPath, productsKey);
			Sort.matchListings(listingsPath, matchingsOutputPath, listingsKey, productsOutputPath);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			
		}

	}

}
