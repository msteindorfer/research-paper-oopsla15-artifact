.PHONY: javaversion

javaversion:
	@(java -version 2>&1 | grep "1\.8" ) || (echo "Wrong Java version: we need Java 8"; exit 1)

clean:
	(rm -f ../oopsla15-benchmarks/data/wordpress-cfgs-as-graphs.bin)
	(mvn clean)

unpack_data:
	# unpack data for real-world evaluation
	(cd ../oopsla15-benchmarks/data && gunzip --keep --force wordpress-cfgs-as-graphs.bin.gz)

prepare: javaversion
	# build all oopsla15-artifacts
	(mvn clean install)

run: javaversion unpack_data
	(cd ../oopsla15-benchmarks && \
		mvn clean package && \
		mkdir -p target/results && \
		mkdir -p target/result-logs)

	(cd ../oopsla15-benchmarks && ./runMicrobenchmarks.sh && ./runRealWorldEvaluation.sh)

postprocessing: javaversion
	# run postprocessing of benchmark data (create plots, etc) as of paper submission
	(Rscript ../oopsla15-benchmarks/resources/r/benchmarks.r `cd ../oopsla15-benchmarks/resources/r && pwd` `cd ../oopsla15-benchmarks/resources/r && pwd` `cat ../oopsla15-benchmarks/LAST_TIMESTAMP_MICROBENCHMARKS.txt`)

run_prebuilt: javaversion unpack_data
	(cd ../oopsla15-benchmarks && \
		rm -rf target && \
		mkdir -p target/results && \
		mkdir -p target/result-logs)

	(cd ../oopsla15-benchmarks && \
		cp lib/benchmarks.jar target)

	(cd ../oopsla15-benchmarks && ./runMicrobenchmarks.sh && ./runRealWorldEvaluation.sh)

postprocessing_cached: javaversion
	# run postprocessing of benchmark data (create plots, etc) as of paper submission
	(Rscript ../oopsla15-benchmarks/resources/r/benchmarks.r `cd ../oopsla15-benchmarks/resources/r && pwd` `cd ../oopsla15-benchmarks/resources/r && pwd` 20150817_0732)
