package com.jagan.SortableChallenge;

import java.io.File;

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

			//sort products file
			Sort.externalSort(productsPath, productsOutputPath, productsKey);
			
			//match product records with listing records in ascending order using priority queue on listing records
			Sort.matchListings(listingsPath, matchingsOutputPath, listingsKey, productsOutputPath);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			
		}

	}

}
