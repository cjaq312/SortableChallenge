#!/bin/sh

#git clone https://github.com/cjaq312/SortableChallenge.git
#cd SortableChallenge
mvn clean compile assembly:single
mvn install
java -jar ./target/Sortable-0.0.1-jar-with-dependencies.jar com.jagan.SortableChallenge.Tester