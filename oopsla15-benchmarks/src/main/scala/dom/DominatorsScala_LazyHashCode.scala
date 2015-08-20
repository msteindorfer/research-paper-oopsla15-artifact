package dom

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.ArrayList
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.immutable.HashMap_LazyHashCode
import scala.collection.immutable.HashSet_LazyHashCode
import scala.collection.mutable.Builder
import org.eclipse.imp.pdb.facts.IConstructor
import org.eclipse.imp.pdb.facts.IMap
import org.eclipse.imp.pdb.facts.ISet
import org.eclipse.imp.pdb.facts.ITuple
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.io.BinaryValueReader
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter
import org.eclipse.imp.pdb.facts.io.StandardTextWriter
import org.rascalmpl.interpreter.utils.Timing
import dom.AllDominatorsRunner.CURRENT_DATA_SET
import dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME
import dom.AllDominatorsRunner.LOG_BINARY_RESULTS
import dom.AllDominatorsRunner.LOG_TEXTUAL_RESULTS
import dom.DominatorsScala_LazyHashCode._
import org.openjdk.jmh.infra.Blackhole

/**
* Port from CHART implementation. Uses immutable.{HashSet_LazyHashCode,HashMap_LazyHashCode} instead of CHART's {TrieSet,TrieMap}.
* Transients became Builders.
*/
class DominatorsScala_LazyHashCode extends DominatorBenchmark {

	def setofdomsets(dom: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]], ps: HashSet_LazyHashCode[IConstructor]): HashSet_LazyHashCode[HashSet_LazyHashCode[IConstructor]] = {
		val bldr = HashSet_LazyHashCode.newBuilder[HashSet_LazyHashCode[IConstructor]]

		for (p <- ps) {
			bldr += dom.getOrElse(p, HashSet_LazyHashCode.empty)
		}

		bldr.result
	}

	def listofdomsets(dom: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]], ps: HashSet_LazyHashCode[IConstructor]): Option[ArrayList[HashSet_LazyHashCode[IConstructor]]] = {
		val resultList = new ArrayList[HashSet_LazyHashCode[IConstructor]](dom.size)

		for (p <- ps) {
			val value = dom.get(p)

			if (value.isDefined && !value.get.isEmpty) {
				resultList add value.get
			} else {
				return None
			}
		}

		Some(resultList)
	}

	def top(graph: HashSet_LazyHashCode[ITuple]): HashSet_LazyHashCode[IConstructor] = project(graph, 0) -- project(graph, 1)

	def getTop(graph: HashSet_LazyHashCode[ITuple]): IConstructor = {
		top(graph) foreach {
			candidate =>
				candidate.getName match {
					case "methodEntry" | "functionEntry" | "scriptEntry" => return candidate
					case _: String => {}
				}
		}

		throw new NoSuchElementException("No candidate found.")
	}

	def calculateDominators(graph: HashSet_LazyHashCode[ITuple]): HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]] = {
		val n0: IConstructor = getTop(graph)
		val nodes: HashSet_LazyHashCode[IConstructor] = carrier(graph)
		val preds: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]] = toMap(project(graph, 1, 0))

		var dom: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]] = {
			val domBldr = HashMap_LazyHashCode.newBuilder[IConstructor, HashSet_LazyHashCode[IConstructor]]
			domBldr += ((n0, HashSet_LazyHashCode(n0)))
			for (n <- (nodes - n0)) {
				domBldr += ((n, nodes))
			}
			domBldr.result
		}

		var prev: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]] = HashMap_LazyHashCode.empty

		while (prev != dom) {
			prev = dom

			val domBldr = HashMap_LazyHashCode.newBuilder[IConstructor, HashSet_LazyHashCode[IConstructor]]

			for (n <- nodes) {
				val ps = preds.getOrElse(n, HashSet_LazyHashCode.empty)

				val sos = setofdomsets(dom, ps)
				val intersected = if (sos == null || sos.isEmpty || sos.contains(HashSet_LazyHashCode.empty)) HashSet_LazyHashCode.empty[IConstructor] else sos reduce { _ intersect _ }
				val newValue = intersected union HashSet_LazyHashCode(n)

//				val alos = listofdomsets(dom, ps)
//				val intersected = if (alos.isEmpty || alos.get.isEmpty) HashSet_LazyHashCode.empty[IConstructor] else alos.get.asScala reduce { _ intersect _ }
//				val newValue = intersected + n

				domBldr += ((n, newValue))
			}

			dom = domBldr.result
		}

		dom
	}

	def performBenchmark(bh: Blackhole, sampledGraphsNative: ArrayList[_]): Unit = {
		for (graph <- sampledGraphsNative.asInstanceOf[ArrayList[HashSet_LazyHashCode[ITuple]]].asScala) {
			try {
				bh.consume(new DominatorsScala_LazyHashCode().calculateDominators(graph))
			} catch {
				case e: NoSuchElementException => System.err.println(e.getMessage)
			}
		}
	}

	def convertDataToNativeFormat(sampledGraphs: ArrayList[ISet]): ArrayList[_] = {
		val graphs: ArrayList[HashSet_LazyHashCode[ITuple]] = new ArrayList(sampledGraphs.size())

		for (graph <- sampledGraphs.asScala) {
			val convertedValueBldr = HashSet_LazyHashCode.newBuilder[ITuple]
			for (tuple <- graph.asScala) {
				convertedValueBldr += tuple.asInstanceOf[ITuple]
			}
			graphs add convertedValueBldr.result
		}

		graphs
	}

}

object DominatorsScala_LazyHashCode {

	def main(args: Array[String]) {
		testOne
		//		testAll
	}

	def testOne: IMap = {
		val vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance

		val data = new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME)).asInstanceOf[ISet]

		// convert data to remove PDB dependency
		val graph: HashSet_LazyHashCode[ITuple] = pdbSetToImmutableSet(data);

		val before = Timing.getCpuTime();
		val results: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]] = new DominatorsScala_LazyHashCode().calculateDominators(graph);

		System.err.println("SCALA" + "\nDuration: "
			+ ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

		val pdbResults: IMap = immutableMapToPdbMap(results)

		if (LOG_BINARY_RESULTS)
			new BinaryValueWriter().write(pdbResults, new FileOutputStream(
				"data/dominators-java-without-pdb-single.bin"));

		if (LOG_TEXTUAL_RESULTS)
			new StandardTextWriter().write(pdbResults, new FileWriter(
				"data/dominators-java-without-pdb-single.txt"));

		return pdbResults;
	}

	def testAll(sampledGraphs: IMap): ISet = {
		// convert data to remove PDB dependency
		val graphs: ArrayList[HashSet_LazyHashCode[ITuple]] = pdbMapToArrayListOfValues(sampledGraphs);

		val resultBldr = HashSet_LazyHashCode.newBuilder[HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]]]

		val before = Timing.getCpuTime();
		for (graph <- graphs.asScala) {
			try {
				resultBldr += new DominatorsScala_LazyHashCode().calculateDominators(graph)
			} catch {
				case e: RuntimeException => System.err.println(e.getMessage)
			}
		}

		System.err.println("SCALA" + "\nDuration: "
			+ ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n")

		// convert back to PDB for serialization
		val pdbResults = immutableSetOfMapsToSetOfMapValues(resultBldr.result);

		if (LOG_BINARY_RESULTS)
			new BinaryValueWriter().write(pdbResults, new FileOutputStream(
				"data/dominators-scala.bin"));

		if (LOG_TEXTUAL_RESULTS)
			new StandardTextWriter().write(pdbResults, new FileWriter(
				"data/dominators-scala.txt"));

		return pdbResults;
	}

	def pdbMapToArrayListOfValues(data: IMap): ArrayList[HashSet_LazyHashCode[ITuple]] = {
		// convert data to remove PDB dependency
		val graphs: ArrayList[HashSet_LazyHashCode[ITuple]] = new ArrayList(data.size())

		for (key <- data.asScala) {
			val value = data.get(key).asInstanceOf[ISet]
			val convertedValueBldr = HashSet_LazyHashCode.newBuilder[ITuple]
			for (tuple <- value.asScala) {
				convertedValueBldr += tuple.asInstanceOf[ITuple]
			}
			graphs add convertedValueBldr.result
		}

		graphs
	}

	def immutableSetOfMapsToSetOfMapValues(result: HashSet_LazyHashCode[HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]]]): ISet = {
		// convert back to PDB for serialization
		val vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance

		val resultBuilder = vf.setWriter

		for (dominatorResult <- result) {
			val builder = vf.mapWriter
			for (entry <- dominatorResult) {
				builder.put(entry._1, immutableSetToPdbSet(entry._2))
			}
			resultBuilder insert builder.done
		}

		resultBuilder.done
	}

	def pdbSetToImmutableSet(set: ISet): HashSet_LazyHashCode[ITuple] = {
		val bldr = HashSet_LazyHashCode.newBuilder[ITuple]

		for (tuple <- set.asScala) {
			bldr += tuple.asInstanceOf[ITuple]
		}

		return bldr.result
	}

	def immutableSetToPdbSet[K <: IValue](set: HashSet_LazyHashCode[K]): ISet = {
		val vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance

		val builder = vf.setWriter

		for (key <- set) {
			builder.insert(key)
		}

		return builder.done
	}

	def immutableMapToPdbMap(
		result: HashMap_LazyHashCode[IConstructor, HashSet_LazyHashCode[IConstructor]]): IMap = {
		// convert back to PDB for serialization
		val vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance

		val builder = vf.mapWriter

		for (entry <- result) {
			builder.put(entry._1, immutableSetToPdbSet(entry._2))
		}

		builder.done
	}

	//	@SuppressWarnings("rawtypes")
	//	public final static ImmutableSet EMPTY = DefaultTrieSet.of();
	//
	//	/*
	//	 * Intersect many sets.
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public static <K> ImmutableSet<K> intersect(ImmutableSet<ImmutableSet<K>> sets) {
	//		if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
	//			return EMPTY;
	//		}
	//
	//		ImmutableSet<K> first = sets.iterator().next();
	//		sets = sets.__remove(first);
	//
	//		ImmutableSet<K> result = first;
	//		for (ImmutableSet<K> elem : sets) {
	//			result = Util.intersect(result, elem);
	//		}
	//
	//		return result;
	//	}

	/*
	 * Intersect two sets.
	 */
	//	public static <K> ImmutableSet<K> intersect(ImmutableSet<K> set1, ImmutableSet<K> set2) {
	//		if (set1 == set2)
	//			return set1;
	//		if (set1 == null)
	//			return DefaultTrieSet.of();
	//		if (set2 == null)
	//			return DefaultTrieSet.of();
	//
	//		final ImmutableSet<K> smaller;
	//		final ImmutableSet<K> bigger;
	//
	//		final ImmutableSet<K> unmodified;
	//
	//		if (set2.size() >= set1.size()) {
	//			unmodified = set1;
	//			smaller = set1;
	//			bigger = set2;
	//		} else {
	//			unmodified = set2;
	//			smaller = set2;
	//			bigger = set1;
	//		}
	//
	//		final TransientSet<K> tmp = smaller.asTransient();
	//		boolean modified = false;
	//
	//		for (Iterator<K> it = tmp.iterator(); it.hasNext();) {
	//			final K key = it.next();
	//			if (!bigger.contains(key)) {
	//				it.remove();
	//				modified = true;
	//			}
	//		}
	//
	//		if (modified) {
	//			return tmp.freeze();
	//		} else {
	//			return unmodified;
	//		}
	//	}

	/*
	 * Subtract one set from another.
	 */
	//	public static <K> ImmutableSet<K> subtract(ImmutableSet<K> set1, ImmutableSet<K> set2) {
	//		if (set1 == null && set2 == null)
	//			return DefaultTrieSet.of();
	//		if (set1 == set2)
	//			return DefaultTrieSet.of();
	//		if (set1 == null)
	//			return DefaultTrieSet.of();
	//		if (set2 == null)
	//			return set1;
	//
	//		final TransientSet<K> tmp = set1.asTransient();
	//		boolean modified = false;
	//
	//		for (K key : set2) {
	//			if (tmp.__remove(key)) {
	//				modified = true;
	//			}
	//		}
	//
	//		if (modified) {
	//			return tmp.freeze();
	//		} else {
	//			return set1;
	//		}
	//	}

	/*
	 * Union two sets.
	 */
	//	public static <K> ImmutableSet<K> union(ImmutableSet<K> set1, ImmutableSet<K> set2) {
	//		if (set1 == null && set2 == null)
	//			return DefaultTrieSet.of();
	//		if (set1 == null)
	//			return set2;
	//		if (set2 == null)
	//			return set1;
	//
	//		if (set1 == set2)
	//			return set1;
	//
	//		final ImmutableSet<K> smaller;
	//		final ImmutableSet<K> bigger;
	//
	//		final ImmutableSet<K> unmodified;
	//
	//		if (set2.size() >= set1.size()) {
	//			unmodified = set2;
	//			smaller = set1;
	//			bigger = set2;
	//		} else {
	//			unmodified = set1;
	//			smaller = set2;
	//			bigger = set1;
	//		}
	//
	//		final TransientSet<K> tmp = bigger.asTransient();
	//		boolean modified = false;
	//
	//		for (K key : smaller) {
	//			if (tmp.__insert(key)) {
	//				modified = true;
	//			}
	//		}
	//
	//		if (modified) {
	//			return tmp.freeze();
	//		} else {
	//			return unmodified;
	//		}
	//	}

	/*
	 * Flattening of a set (of ITuple elements).
	 * 
	 * Because of the untyped nature of ITuple, the implementation is not
	 * strongly typed.
	 */
	def carrier[K <: java.lang.Iterable[IValue], T](set1: HashSet_LazyHashCode[K]): HashSet_LazyHashCode[T] = {
		val bldr = HashSet_LazyHashCode.newBuilder[T]

		for (iterable <- set1; nested <- iterable.asScala) {
			bldr += nested.asInstanceOf[T]
		}

		bldr.result
	}

	/*
	 * Projection from a tuple to single field.
	 */
	def project[K <: IValue](set1: HashSet_LazyHashCode[ITuple], field: Int): HashSet_LazyHashCode[K] = {
		val bldr = HashSet_LazyHashCode.newBuilder[K]

		set1 foreach {
			tuple => bldr += tuple.select(field).asInstanceOf[K]
		}

		bldr.result
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	def project[K <: IValue](set1: HashSet_LazyHashCode[ITuple], field1: Int, field2: Int): HashSet_LazyHashCode[K] = {
		val bldr = HashSet_LazyHashCode.newBuilder[K]

		set1 foreach {
			tuple => bldr += tuple.select(field1, field2).asInstanceOf[K]
		}

		bldr.result
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	def toMap[K, V](st: HashSet_LazyHashCode[ITuple]): HashMap_LazyHashCode[K, HashSet_LazyHashCode[V]] = {
		val hm: java.util.HashMap[K, Builder[V, HashSet_LazyHashCode[V]]] = new java.util.HashMap

		for (t <- st) {
			val key = t.get(0).asInstanceOf[K]
			val value = t.get(1).asInstanceOf[V]

			var wValSet = hm.get(key)
			if (wValSet == null) {
				wValSet = HashSet_LazyHashCode.newBuilder[V]
				hm.put(key, wValSet)
			}
			wValSet += value

		}

		val bldr = HashMap_LazyHashCode.newBuilder[K, HashSet_LazyHashCode[V]]

		for (k <- hm.keySet.asScala) {
			bldr += ((k, hm.get(k).result))
		}

		bldr.result
	}

}
