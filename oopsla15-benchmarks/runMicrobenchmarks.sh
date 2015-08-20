#!/bin/bash

# mvn clean install
# mkdir -p target/results
# mkdir -p target/result-logs

export VALUE_FACTORY_FACTORY="VF_PDB_PERSISTENT_CURRENT,VF_SCALA,VF_CLOJURE,VF_PDB_PERSISTENT_MEMOIZED_LAZY"

######
export AGGREGATED_SETTINGS="-jvmArgsPrepend -Xms4g -jvmArgsPrepend -Xmx4g -wi 10 -i 20 -f 1 -r 1 -gc true -rf csv -v NORMAL -foe true -bm avgt -p valueFactoryFactory=$VALUE_FACTORY_FACTORY -p sampleDataSelection=MATCH -p producer=PDB_INTEGER"

export SET_BENCHMARKS="nl.cwi.swat.jmh_dscg_benchmarks.JmhSetBenchmarks.(timeContainsKey|timeContainsKeyNotContained|timeInsert|timeInsertContained|timeRemoveKey|timeRemoveKeyNotContained|timeIteration|timeEqualsRealDuplicate|timeEqualsDeltaDuplicate)$"
export MAP_BENCHMARKS="nl.cwi.swat.jmh_dscg_benchmarks.JmhMapBenchmarks.(timeContainsKey|timeContainsKeyNotContained|timeInsert|timeInsertContained|timeRemoveKey|timeRemoveKeyNotContained|timeIteration|timeEntryIteration|timeEqualsRealDuplicate|timeEqualsDeltaDuplicate)$"

LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p run=0 -rff ./target/results/results.JmhSetBenchmarks.run0.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run0.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run0.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p run=1 -rff ./target/results/results.JmhSetBenchmarks.run1.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run1.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run1.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p run=2 -rff ./target/results/results.JmhSetBenchmarks.run2.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run2.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run2.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p run=3 -rff ./target/results/results.JmhSetBenchmarks.run3.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run3.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run3.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $SET_BENCHMARKS $AGGREGATED_SETTINGS -p run=4 -rff ./target/results/results.JmhSetBenchmarks.run4.log # 1>./target/result-logs/results.std-console.JmhSetBenchmarks.run4.log 2>./target/result-logs/results.err-console.JmhSetBenchmarks.run4.log

LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p run=0 -rff ./target/results/results.JmhMapBenchmarks.run0.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run0.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run0.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p run=1 -rff ./target/results/results.JmhMapBenchmarks.run1.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run1.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run1.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p run=2 -rff ./target/results/results.JmhMapBenchmarks.run2.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run2.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run2.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p run=3 -rff ./target/results/results.JmhMapBenchmarks.run3.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run3.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run3.log
LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar $MAP_BENCHMARKS $AGGREGATED_SETTINGS -p run=4 -rff ./target/results/results.JmhMapBenchmarks.run4.log # 1>./target/result-logs/results.std-console.JmhMapBenchmarks.run4.log 2>./target/result-logs/results.err-console.JmhMapBenchmarks.run4.log
######

TIMESTAMP=`date +"%Y%m%d_%H%M"`

echo $TIMESTAMP > LAST_TIMESTAMP_MICROBENCHMARKS.txt

INPUT_FILES=target/results/results.Jmh*.log
RESULTS_FILE=target/results/results.all-$TIMESTAMP.log

RESULT_HEADER=`echo $INPUT_FILES | xargs -n 1 head -n 1 | head -n 1`
{
	for f in $INPUT_FILES
	do
		tail -n +2 $f
	done
} | cat <(echo $RESULT_HEADER) - > $RESULTS_FILE

STD_CONSOLE_LOG_FILES=target/result-logs/results.std-console.*.log
PERF_STAT_LOG_FILES=target/result-logs/results.perf-stat.*.log

# RESULTS_FILE_PERF_STAT=target/results/results.all-$TIMESTAMP.perf-stat.log

# PERF_HEADER=`echo $PERF_STAT_LOG_FILES | xargs -n 1 head -n 1 | head -n 1 | sed -e 's/^/benchmark,/'`
# {
# 	for f in $PERF_STAT_LOG_FILES
# 	do
# 		CURRENT_BENCHMARK=`echo "$f" | sed 's/.*\.time\([^.]*\)\(.*\)/\1/'`
# 		tail -n +2 $f | sed -e "s/^/$CURRENT_BENCHMARK,/"
# 	done
# } | cat <(echo $PERF_HEADER) - | xz -9 > $RESULTS_FILE_PERF_STAT.xz

# java -Xmx12G -XX:+UseCompressedOops -javaagent:`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/google/memory-measurer/1.0-SNAPSHOT/memory-measurer-1.0-SNAPSHOT.jar -cp target/benchmarks.jar nl.cwi.swat.jmh_dscg_benchmarks.CalculateFootprints && mv map-sizes-and-statistics.csv target/map-sizes-and-statistics-32bit-$TIMESTAMP.csv
# java -Xmx12G -XX:-UseCompressedOops -javaagent:`echo $(cd $(dirname ~); pwd)/$(basename ~)`/.m2/repository/com/google/memory-measurer/1.0-SNAPSHOT/memory-measurer-1.0-SNAPSHOT.jar -cp target/benchmarks.jar nl.cwi.swat.jmh_dscg_benchmarks.CalculateFootprints && mv map-sizes-and-statistics.csv target/map-sizes-and-statistics-64bit-$TIMESTAMP.csv

# create empty placeholders
touch target/map-sizes-and-statistics-32bit-$TIMESTAMP.csv
touch target/map-sizes-and-statistics-64bit-$TIMESTAMP.csv

java -Xms4g -Xmx4g -XX:+UseCompressedOops -javaagent:`pwd`/lib/memory-measurer.jar -cp target/benchmarks.jar nl.cwi.swat.jmh_dscg_benchmarks.CalculateFootprints && mv map-sizes-and-statistics.csv target/map-sizes-and-statistics-32bit-$TIMESTAMP.csv
java -Xms4g -Xmx4g -XX:-UseCompressedOops -javaagent:`pwd`/lib/memory-measurer.jar -cp target/benchmarks.jar nl.cwi.swat.jmh_dscg_benchmarks.CalculateFootprints && mv map-sizes-and-statistics.csv target/map-sizes-and-statistics-64bit-$TIMESTAMP.csv

# clean temporary file (if cancelled)
rm -f map-sizes-and-statistics.csv

ARCHIVE_PATH=`pwd`/resources/r
ARCHIVE_NAME=$ARCHIVE_PATH/hamt-benchmark-results-$TIMESTAMP.tgz

echo "Current Working Directory: `pwd`"
echo "Archive Path: $ARCHIVE_PATH"

RESULTS_FILES=`pwd`/target/results/results.all-$TIMESTAMP*
FOOTPRINT_FILES=`pwd`/target/map-sizes-and-statistics-*.csv

cp $RESULTS_FILES $FOOTPRINT_FILES $ARCHIVE_PATH
(cd target && tar -czf $ARCHIVE_NAME results result-logs $RESULTS_FILES $FOOTPRINT_FILES)
