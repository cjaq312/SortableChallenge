package com.jagan.SortableChallenge.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.jagan.SortableChallenge.models.*;

public class FileUtils {

	private JSONParser parser = new JSONParser();

	public long estimateBlockSize(File file) throws Exception {
		if (file == null)
			return 0;

		int nuberOfFiles = 1024;
		long freeMemory = Runtime.getRuntime().freeMemory();
		long fileSize = file.length();
		long blockSize = fileSize / nuberOfFiles;

		if (freeMemory <= blockSize)
			throw new Exception("Not Possible");
		else {
			if (freeMemory / 2 > blockSize)
				blockSize = freeMemory / 2;
		}
		return blockSize;
	}

	public List<File> createSortedBatches(File inputFile, Comparator<String> comparator, String jsonKey)
			throws Exception {
		if (inputFile == null)
			return null;

		List<File> outputList = new ArrayList<File>();

		// estimate block/fragment size
		long estimatedBlockSize = estimateBlockSize(inputFile);

		// file inputstream
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String cache = null;
		int currentBatchSize = 0;

		// fragmented batches of big file - create smaller list of records from
		// input
		// file
		List<DataObject> batch = new ArrayList<DataObject>();

		try {
			while ((cache = reader.readLine()) != null) {
				currentBatchSize += cache.length();

				JSONObject batchRecord = (JSONObject) parser.parse(cache);

				// when current batch is lesser than estimated batch size keep
				// adding the records
				if (currentBatchSize < estimatedBlockSize) {
					batch.add(new DataObject(batchRecord.get(jsonKey).toString(), cache));
				}
				// else save the older batch to list of fragmented files and
				// create a new batch
				else {
					outputList.add(saveBatch(batch));
					batch.clear();
					currentBatchSize = cache.length();
					batch.add(new DataObject(batchRecord.get(jsonKey).toString(), cache));
				}
			}

			if (batch.size() > 0) {
				outputList.add(saveBatch(batch));
				batch.clear();
			}

		} catch (EOFException ex) {
			if (batch.size() > 0) {
				outputList.add(saveBatch(batch));
				batch.clear();
			}
		} finally {
			reader.close();
		}
		return outputList;
	}

	public File saveBatch(List<DataObject> temp) throws IOException {

		// sort the list
		Collections.sort(temp, new Comparator<DataObject>() {

			public int compare(DataObject o1, DataObject o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		// save the values in temp file and return the file
		File tempFile = File.createTempFile("sortedTemp", "txt");
		tempFile.deleteOnExit();
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
		try {
			for (DataObject line : temp) {
				writer.write(line.getContent());
				writer.newLine();
				line = null;
			}
		} finally {
			writer.close();
		}
		return tempFile;
	}

	public File mergeSortedBatches(List<File> sortedFileList, final Comparator<String> comparator,
			String outputFileName) throws IOException {

		// product output stream
		File outputFile = new File(outputFileName);
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

		// priority queue for the products file objects
		PriorityQueue<FileBuffer> sortedQueue = new PriorityQueue<FileBuffer>(new Comparator<FileBuffer>() {
			public int compare(FileBuffer file1, FileBuffer file2) {
				return comparator.compare(file1.peek(), file2.peek());
			}
		});

		// add all the sorted fragments of products file (file buffer objects)
		// to priority queue
		for (File i : sortedFileList)
			sortedQueue.add(new FileBuffer(i));

		// merging process of sorted fragments in to one big file of sorted
		// records - external sorting
		try {
			while (sortedQueue.size() > 0) {
				FileBuffer current = sortedQueue.poll();
				if (current.peek() != null) {
					outputWriter.write(current.pop());
					outputWriter.newLine();
				}
				if (current.isEmpty()) {
					current.close();
				} else {
					sortedQueue.add(current);
				}

			}
		} finally {
			outputWriter.close();
		}
		return outputFile;
	}

	public File matchBatchRecordsAndOutput(List<File> sortedFileList, final Comparator<String> comparator,
			String outputFileName, String productFileName) throws IOException, ParseException {

		// final outputfile stream
		File outputFile = new File(outputFileName);
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

		// products input stream - sorted products file reader
		File productsFile = new File(productFileName);
		BufferedReader productsReader = new BufferedReader(new FileReader(productsFile));

		// first line of products file
		String cache = productsReader.readLine();

		// priority queue for listings fragments list
		PriorityQueue<FileBuffer> sortedQueue = new PriorityQueue<FileBuffer>(new Comparator<FileBuffer>() {
			public int compare(FileBuffer file1, FileBuffer file2) {
				return comparator.compare(file1.peek(), file2.peek());
			}
		});

		// jsonobject for entity extraction form products file record
		JSONObject productRecord = (JSONObject) parser.parse(cache);

		// custom listing record object - to get json form of listings records
		ListingObject listObj = new ListingObject();

		// addition of sorted listings fragments to priority queue
		for (File i : sortedFileList)
			sortedQueue.add(new FileBuffer(i));

		try {
			while (sortedQueue.size() > 0) {
				FileBuffer current = sortedQueue.poll();
				if (current.peek() != null) {

					JSONObject listingRecord = (JSONObject) parser.parse(current.peek());
					// extraction of product_name entity from products record
					// and standardizing the value for matching with listings
					// records
					String normalizedProduct = productRecord.get("product_name").toString().replaceAll("_", "")
							.toLowerCase();

					// extraction of title entity from listings record
					// and standardizing the value for matching with products
					// records
					String normalizedListing = listingRecord.get("title").toString().replaceAll("\\s", "")
							.toLowerCase();

					// when standardized values of products and listings records
					// matches
					if (normalizedListing.startsWith(normalizedProduct)) {
						String tempRecord = current.pop();

						// set list title and listings records to matchings list
						if (!listObj.hasTitle()) {
							listObj.setTitle(productRecord.get("product_name").toString());
							listObj.addRecordToArrayList(tempRecord);
						} else
							listObj.addRecordToArrayList(tempRecord);
					}
					// if listings record is smaller than products record the
					// move the listings pointer to next record
					else if (normalizedListing.compareTo(normalizedProduct) < 0) {
						current.pop();
					}
					// if products record is smaller than listings record then
					// save the older matchings list and move the products
					// pointer to next record
					else {

						// finalize and create new obj if listings > 0
						if (listObj.hasListings()) {
							JSONObject obj = listObj.finalizeListings();
							outputWriter.write(obj.toString());
							// System.out.println(obj.toString());
							outputWriter.newLine();
							// listObj = new ListingObject();
							listObj.reset();
						} else {
							// do nothing
						}

						cache = productsReader.readLine();
						if (cache == null)
							return outputFile;
						productRecord = (JSONObject) parser.parse(cache);
					}
				}
				if (current.isEmpty()) {
					current.close();
				} else {
					sortedQueue.add(current);
				}

			}
		} finally {
			outputWriter.close();
			for (FileBuffer i : sortedQueue) {
				i.close();
			}
		}
		return outputFile;
	}

}
