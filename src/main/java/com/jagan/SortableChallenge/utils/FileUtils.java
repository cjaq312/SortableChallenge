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
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jagan.SortableChallenge.models.*;

public class FileUtils {
	int tempCounter = 0;

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
		long estimatedBlockSize = estimateBlockSize(inputFile);
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String cache = null;
		int currentBatchSize = 0;
		List<DataObject> batch = new ArrayList<DataObject>();

		try {
			while ((cache = reader.readLine()) != null) {
				currentBatchSize += cache.length();
				JSONObject batchRecord = new JSONObject(cache);

				if (currentBatchSize < estimatedBlockSize) {
					batch.add(new DataObject(batchRecord.get(jsonKey).toString(), cache));
				} else {
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

		Collections.sort(temp, new Comparator<DataObject>() {

			public int compare(DataObject o1, DataObject o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

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
		File outputFile = new File(outputFileName);
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

		PriorityQueue<FileBuffer> sortedQueue = new PriorityQueue<FileBuffer>(new Comparator<FileBuffer>() {
			public int compare(FileBuffer file1, FileBuffer file2) {
				return comparator.compare(file1.peek(), file2.peek());
			}
		});

		for (File i : sortedFileList)
			sortedQueue.add(new FileBuffer(i));
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
			String outputFileName, String productFileName) throws IOException {

		// outputfile writer
		File outputFile = new File(outputFileName);
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

		// products reader
		File productsFile = new File(productFileName);
		BufferedReader productsReader = new BufferedReader(new FileReader(productsFile));

		String cache = productsReader.readLine();

		PriorityQueue<FileBuffer> sortedQueue = new PriorityQueue<FileBuffer>(new Comparator<FileBuffer>() {
			public int compare(FileBuffer file1, FileBuffer file2) {
				return comparator.compare(file1.peek(), file2.peek());
			}
		});

		JSONObject productRecord = new JSONObject(cache);

		// listing record object
		ListingObject listObj = new ListingObject();

		for (File i : sortedFileList)
			sortedQueue.add(new FileBuffer(i));

		try {
			while (sortedQueue.size() > 0) {
				FileBuffer current = sortedQueue.poll();
				if (current.peek() != null) {

					JSONObject listingRecord = new JSONObject(current.peek());

					String normalizedProduct = productRecord.get("product_name").toString().replaceAll("_", "")
							.toLowerCase();
					String normalizedListing = listingRecord.get("title").toString().replaceAll("\\s", "")
							.toLowerCase();

					if (normalizedListing.startsWith(normalizedProduct)) {
						String tempRecord = current.pop();
						// set list title
						if (!listObj.hasTitle()) {
							listObj.setTitle(listingRecord.get("title").toString());
							listObj.addRecordToArrayList(tempRecord);
						} else
							listObj.addRecordToArrayList(tempRecord);
					} else if (normalizedListing.compareTo(normalizedProduct) < 0) {
						current.pop();
					} else {

						// finalize and create new obj if listings > 0
						if (listObj.hasListings()) {
							JSONObject obj = listObj.finalizeListings();
							outputWriter.write(obj.toString());
							outputWriter.newLine();
							listObj = new ListingObject();
						} else {
							// do nothing
						}

						cache = productsReader.readLine();
						if (cache == null)
							return outputFile;
						productRecord = new JSONObject(cache);
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
