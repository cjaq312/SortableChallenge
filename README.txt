HOW TO RUN:
1. Clone the github repo to local machine
2. Move in to the project folder (cd SortableChallenge)
3. run go.sh
4. See the output in "./data/outputData/productListingsOutput.txt"

(OR)

1. Clone the github repo to local machine
2. Build the project using Maven "mvn clean compile assembly:single" "mvn install"
3. Run "java -jar ./target/Sortable-0.0.1-jar-with-dependencies.jar com.jagan.SortableChallenge.Tester"
4. See the output in "./data/outputData/productListingsOutput.txt"


ALGORITHM:
1. Sort Products File using external sort
2. Break Listings file in to small lists
	2a. sort lists (sorting based on title field)
	2b. save the list in temp files
3. Add the sorted temp files to priority queue
	3a. if product record prefix matches listings record, add the listings record to arraylist.
	3b. else if products record is prefix greater than listings record, then move the listings record pointer to next record
	3c. else 
		3ci. save the arraylist to jsonarray
		3cii. save the product name
		3ciii. save the matching record with listings to file
		3civ. move the product record pointer to next record
4. return the file


FILES:
Utility Files
-------------
Sort.java				-		Fascade for sorting and matching
FileUtils.java 			-		Utilities for external sort and perform matching between products and listings record

Models
------
DataObject.java			-		Model object for comparison using key
FileBuffer.java			-		Model object for sorted file fragments
ListingObject.java		-		Model object for product match listings

Tester.java				-		Main class


PATHS:
Codes
-----
MODELS:							\src\main\java\com\jagan\SortableChallenge\models
UTILITIES:						\src\main\java\com\jagan\SortableChallenge\utils
MAIN:							\src\main\java\com\jagan\SortableChallenge\Tester.java

Data
----
INPUT:							\data\inputData
OUTPUT(productListingsOutput):	\data\outputData
