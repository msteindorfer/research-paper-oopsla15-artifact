# Getting Started Guide

Our evaluation consists of microbenchmarks and real-world benchmarks, both are fully automated. The system requirements to execute them are as follows:

* Operating system: Apple OS X or Linux
* Command line tools: 
	* java (version 8),
	* maven (version >= 3.2), 
	* make (we used GNU Make 3.81),
	* ant (we used version 1.9.5), 
	* R and RScript (we used version 3.2.0)
* Internet connection (for automatically downloading dependencies)

The benchmarks requires heap sizes of 4GB, thus machines with at leas 8GB RAM are recommended.

We assume familiarity with UNIX terminals and command line tools. We do not go into details how to install the above mentioned command line tools. We ourselves used our artifact, both, under Apple OS X and Linux.

We wanted to make the use of our artifact as simple as possible. If the system requirements are fulfilled, the reproduction of our results require the execution of three commands in a console/terminal:

Moving into the artifacts directory:
> cd oopsla15-artifacts

Setting up and compiling the artifacts:
> make prepare

Running microbenchmarks and real-world benchmarks:
> make run

Running result analysis and post-processing:
> make postprocessing

The first command does not consume time. The second command should take approximately five minutes to complete and should complete without errors. The third command however will take several hours or even days. E.g., in our real-world evaluation the slowest single invocation completes in 30 minutes. For statistical testing of our results we invoke every benchmarks multiple times. Step four, the analysis and postprocessing takes around a minute or less usually.

As an alternative to **make prepare** and **make run** we provide a **make run_prebuilt** command that runs a prebuilt benchmarks JAR file. If you experience any issues in running the experiments, you might start with the **make run_prebuilt** command.

We further included all results that we obtained form step number three. Consequently our results can be evaluated without the necessity to execute our automated benchmark suite. We provide an extra command for this purpose:
> make postprocessing_cached

Furthermore, in section "Running the Benchmarks on Smaller Samples" we will point out how to run the experiments on smaller subsets, that consume less time.

To manually inspect what the **make** commands do, have a look at *oopsla15-artifacts/Makefile*.

## Key Data Items of our Evaluation
Our cached results are contained in the folder *oopsla15-benchmarks/resources/r*. 

The following files contain data from the microbenchmarks that are discussed in Section 6 of the paper:

* *results.all-20150817_0732.log*: comma-separated values (CSV) file containing microbenchmark results of runtimes of individual operations
* *map-sizes-and-statistics-32bit-20150817_0732.csv*: CSV file containing memory footprints in a 32-bit JVM setting.
* *map-sizes-and-statistics-64bit-20150817_0732.csv*: CSV file containing memory footprints in a 64-bit JVM setting.

These CSV files are then processed by *benchmarks.r*, a R script, and produce directly the boxplots of Figures 4, 5, 6 and 7 of the papers. The boxplots are named *all-benchmarks-vf_pdb_persistent_(current|memoized)_by_vf_(scala|clojure)-(set|map)-boxplot.pdf*. 

The following files contain data from the real-world benchmarks that are discussed in Section 7 of the paper:

* *results.all-real-world-20150404_1013.log*: a CSV file containing the runtime results of our real-word benchmarks (i.e., of the control-flow graph dominator tree calculations).

## Key Source Items of our Artifact
Our CHAMP hash trie implementations can be found under *pdb.values/src/org/eclipse/imp/pdb/facts/util/Trie(Set|Map)_5Bits.java*, and MEMCHAMP under *pdb.values/src/org/eclipse/imp/pdb/facts/util/Trie(Set|Map)_5Bits_Memoized_LazyHashCode.java*, for people interested in manually inspecting our implementations.

Projects *pdb.values.persistent.(clojure|scala)* contain simple interface facades that enables cross-library benchmarks under a common API.

The benchmark implementations can be found in the *oopsla15-benchmarks* project.  Files *Dominators(Champ|Clojure).java* and *DominatorsScala_Default.scala* implement the real-word experiment (Section 7 of the paper). For Champ and Scala there are addtional dominator implementations with postfix *LazyHashCode* for the normalized experiments.

Files *Jmh(Set|Map)Benchmarks.java* measure the runtimes of individual operations, whereas *CalculateFootprints.java* performs footprint measurements (cf. Section 6, Figures 4, 5, 6 and 7).  Note that the benchmarks contain default parameters for their invocation, the actual parameters are set in *runMicrobenchmarks.sh* and *runRealWorldEvaluation.sh*.

## Running the Benchmarks on Smaller Samples 

In order to run the microbenchmarks on smaller-sized examples we recommend (some of) the following changes in *runMicrobenchmarks.sh*:

* Lines 15-25: each lines performs a benchmark invocations for a specific benchmark/input size combination. To reduce the number of experiments, simply comment out multiple lines.
* Line 10: AGGREGATED_SETTINGS contains the general settings we use for microbenchmarking (-wi = warmup iterations, -i = benchmark invocations, -p run = a list of identifiers that will create different trees with other random data seeds). To minimize the number of runs set the following parameters: -wi 1 -i 3 -p run=0. These parameters render the benchmarks useless from a statistical point of view, but enable experimentation with the framework.
* Lines 64-65: those lines perform the footprint calculations that consume quite some time (~15 minutes). Thus for quick experiments one could comment out those lines.

In order to run the real-world benchmarks on smaller-sized examples we recommend (some of) the following changes in *runRealWorldEvaluation.sh*:

* Line 12: Set the following parameters (note that the size parameter is introduced to limit testing to a certain subset size instead of all tested data points): -wi 1 -i 3 -p size=8