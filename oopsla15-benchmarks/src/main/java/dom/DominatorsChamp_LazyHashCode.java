package dom;

import static dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME;
import static dom.AllDominatorsRunner.LOG_BINARY_RESULTS;
import static dom.AllDominatorsRunner.LOG_TEXTUAL_RESULTS;
import static dom.Util_LazyHashCode.EMPTY;
import static dom.Util_LazyHashCode.carrier;
import static dom.Util_LazyHashCode.intersect;
import static dom.Util_LazyHashCode.project;
import static dom.Util_LazyHashCode.subtract;
import static dom.Util_LazyHashCode.toMap;
import static dom.Util_LazyHashCode.union;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.eclipse.imp.pdb.facts.io.StandardTextWriter;
import org.eclipse.imp.pdb.facts.util.TrieMap_5Bits_LazyHashCode;
import org.eclipse.imp.pdb.facts.util.TrieSet_5Bits_LazyHashCode;
import org.eclipse.imp.pdb.facts.util.ImmutableMap;
import org.eclipse.imp.pdb.facts.util.ImmutableSet;
import org.eclipse.imp.pdb.facts.util.TransientMap;
import org.eclipse.imp.pdb.facts.util.TransientSet;
import org.openjdk.jmh.infra.Blackhole;
import org.rascalmpl.interpreter.utils.Timing;

@SuppressWarnings("deprecation")
public class DominatorsChamp_LazyHashCode implements DominatorBenchmark {

	private ImmutableSet setofdomsets(ImmutableMap dom, ImmutableSet preds) {
		TransientSet result = TrieSet_5Bits_LazyHashCode.transientOf();

		for (Object p : preds) {
			ImmutableSet ps = (ImmutableSet) dom.get(p);

			result.__insert(ps == null ? EMPTY : ps);
		}

		return result.freeze();
	}

	public ImmutableSet<IConstructor> top(ImmutableSet<ITuple> graph) {
		return subtract(project(graph, 0), project(graph, 1));
	}

	public IConstructor getTop(ImmutableSet<ITuple> graph) {
		for (IConstructor candidate : top(graph)) {
			switch (candidate.getName()) {
			case "methodEntry":
			case "functionEntry":
			case "scriptEntry":
				return candidate;
			}
		}

		throw new NoSuchElementException("No candidate found.");
	}

	public ImmutableMap<IConstructor, ImmutableSet<IConstructor>> calculateDominators(
					ImmutableSet<ITuple> graph) {
		IConstructor n0 = getTop(graph);
		ImmutableSet<IConstructor> nodes = carrier(graph);
		// if (!nodes.getElementType().isAbstractData()) {
		// throw new RuntimeException("nodes is not the right type");
		// }
		ImmutableMap<IConstructor, ImmutableSet<IConstructor>> preds = toMap(project(graph, 1, 0));
		// nodes = nodes.delete(n0);

		TransientMap<IConstructor, ImmutableSet<IConstructor>> w = TrieMap_5Bits_LazyHashCode.transientOf();
		w.__put(n0, TrieSet_5Bits_LazyHashCode.of(n0));
		for (IConstructor n : nodes.__remove(n0)) {
			w.__put(n, nodes);
		}
		ImmutableMap<IConstructor, ImmutableSet<IConstructor>> dom = w.freeze();

		ImmutableMap prev = TrieMap_5Bits_LazyHashCode.of();
		/*
		 * solve (dom) for (n <- nodes) dom[n] = {n} + intersect({dom[p] | p <-
		 * preds[n]?{}});
		 */
		while (!prev.equals(dom)) {
			prev = dom;

			TransientMap<IConstructor, ImmutableSet<IConstructor>> newDom = TrieMap_5Bits_LazyHashCode
							.transientOf();

			for (IConstructor n : nodes) {
				ImmutableSet ps = (ImmutableSet) preds.get(n);
				if (ps == null) {
					ps = EMPTY;
				}
				ImmutableSet sos = setofdomsets(dom, ps);
				// if (!sos.getType().isSet() ||
				// !sos.getType().getElementType().isSet() ||
				// !sos.getType().getElementType().getElementType().isAbstractData())
				// {
				// throw new RuntimeException("not the right type: " +
				// sos.getType());
				// }
				ImmutableSet intersected = intersect(sos);
				// if (!intersected.getType().isSet() ||
				// !intersected.getType().getElementType().isAbstractData()) {
				// throw new RuntimeException("not the right type: " +
				// intersected.getType());
				// }
				ImmutableSet newValue = union(intersected, TrieSet_5Bits_LazyHashCode.of(n));
				// ImmutableSet newValue = intersected.__insert(n);
				// if (!newValue.getElementType().isAbstractData()) {
				// System.err.println("problem");
				// }
				newDom.__put(n, newValue);
			}

			// if
			// (!newDom.done().getValueType().getElementType().isAbstractData())
			// {
			// System.err.println("not good");
			// }
			dom = newDom.freeze();
		}

		return dom;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		testOne();
		assertDominatorsEqual();
	}

	public static IMap testOne() throws IOException, FileNotFoundException {
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		ISet data = (ISet) new BinaryValueReader().read(vf, new FileInputStream(
						DATA_SET_SINGLE_FILE_NAME));

		// convert data to remove PDB dependency
		ImmutableSet<ITuple> graph = pdbSetToImmutableSet(data);

		long before = Timing.getCpuTime();
		ImmutableMap<IConstructor, ImmutableSet<IConstructor>> results = new DominatorsChamp_LazyHashCode()
						.calculateDominators(graph);
		System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: "
						+ ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

		IMap pdbResults = immutableMapToPdbMap(results);

		if (LOG_BINARY_RESULTS)
			new BinaryValueWriter().write(pdbResults, new FileOutputStream(
							"data/dominators-java-without-pdb-single.bin"));

		if (LOG_TEXTUAL_RESULTS)
			new StandardTextWriter().write(pdbResults, new FileWriter(
							"data/dominators-java-without-pdb-single.txt"));

		return pdbResults;
	}

	public static ISet testAll(IMap sampledGraphs) throws IOException, FileNotFoundException {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> graphs = pdbMapToArrayListOfValues(sampledGraphs);

		TransientSet<ImmutableMap<IConstructor, ImmutableSet<IConstructor>>> result = TrieSet_5Bits_LazyHashCode
						.transientOf();
		long before = Timing.getCpuTime();
		for (ImmutableSet<ITuple> graph : graphs) {
			try {
				result.__insert(new DominatorsChamp_LazyHashCode().calculateDominators(graph));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}
		System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: "
						+ ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

		// convert back to PDB for serialization
		ISet pdbResults = immutableSetOfMapsToSetOfMapValues(result.freeze());

		if (LOG_BINARY_RESULTS)
			new BinaryValueWriter().write(pdbResults, new FileOutputStream(
							"data/dominators-java.bin"));

		if (LOG_TEXTUAL_RESULTS)
			new StandardTextWriter().write(pdbResults, new FileWriter(
							"data/dominators-java-without-pdb.txt"));

		return pdbResults;
	}

	private static ArrayList<ImmutableSet<ITuple>> pdbMapToArrayListOfValues(IMap data) {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> graphs = new ArrayList<>(data.size());
		for (IValue key : data) {
			ISet value = (ISet) data.get(key);

			TransientSet<ITuple> convertedValue = TrieSet_5Bits_LazyHashCode.transientOf();
			for (IValue tuple : value) {
				convertedValue.__insert((ITuple) tuple);
			}

			graphs.add(convertedValue.freeze());
		}

		return graphs;
	}

	private static ISet immutableSetOfMapsToSetOfMapValues(
					ImmutableSet<ImmutableMap<IConstructor, ImmutableSet<IConstructor>>> result) {
		// convert back to PDB for serialization
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		ISetWriter resultBuilder = vf.setWriter();

		for (ImmutableMap<IConstructor, ImmutableSet<IConstructor>> dominatorResult : result) {
			IMapWriter builder = vf.mapWriter();

			for (Map.Entry<IConstructor, ImmutableSet<IConstructor>> entry : dominatorResult
							.entrySet()) {
				builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
			}

			resultBuilder.insert(builder.done());
		}

		return resultBuilder.done();
	}

	private static IMap immutableMapToPdbMap(
					ImmutableMap<IConstructor, ImmutableSet<IConstructor>> result) {
		// convert back to PDB for serialization
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		IMapWriter builder = vf.mapWriter();

		for (Map.Entry<IConstructor, ImmutableSet<IConstructor>> entry : result.entrySet()) {
			builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
		}

		return builder.done();
	}

	private static <K extends IValue> ISet immutableSetToPdbSet(ImmutableSet<K> set) {
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		ISetWriter builder = vf.setWriter();

		for (K key : set) {
			builder.insert(key);
		}

		return builder.done();
	}

	// private static <K extends IValue, V extends IValue> IMap
	// immutableMapToPdbMap(
	// ImmutableMap<K, V> map) {
	// IValueFactory vf =
	// org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();
	//
	// IMapWriter builder = vf.mapWriter();
	//
	// for (Map.Entry<K, V> entry : map.entrySet()) {
	// builder.put(entry.getKey(), entry.getValue());
	// }
	//
	// return builder.done();
	// }

	private static ImmutableSet<ITuple> pdbSetToImmutableSet(ISet set) {
		TransientSet<ITuple> builder = TrieSet_5Bits_LazyHashCode.transientOf();

		for (IValue tuple : set) {
			builder.__insert((ITuple) tuple);
		}

		return builder.freeze();
	}

	public static void assertDominatorsEqual() throws FileNotFoundException, IOException {
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		ISet dominatorsRascal = (ISet) new BinaryValueReader().read(vf, new FileInputStream(
						"data/dominators-rascal.bin"));
		ISet dominatorsJava = (ISet) new BinaryValueReader().read(vf, new FileInputStream(
						"data/dominators-java.bin"));

		if (!dominatorsRascal.equals(dominatorsJava)) {
			throw new Error("Dominator calculations do differ!");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative) {
		for (ImmutableSet<ITuple> graph : (ArrayList<ImmutableSet<ITuple>>) sampledGraphsNative) {
			try {
				bh.consume(new DominatorsChamp_LazyHashCode().calculateDominators(graph));
			} catch (NoSuchElementException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs) {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> sampledGraphsNative = new ArrayList<>(sampledGraphs.size());

		for (ISet graph : sampledGraphs) {
			TransientSet<ITuple> convertedValue = TrieSet_5Bits_LazyHashCode.transientOf();

			for (IValue tuple : graph) {
				convertedValue.__insert((ITuple) tuple);
			}

			sampledGraphsNative.add(convertedValue.freeze());
		}

		return sampledGraphsNative;
	}

}

class Util_LazyHashCode {

	@SuppressWarnings("rawtypes")
	public final static ImmutableSet EMPTY = TrieSet_5Bits_LazyHashCode.of();

	/*
	 * Intersect many sets.
	 */
	@SuppressWarnings("unchecked")
	public static <K> ImmutableSet<K> intersect(ImmutableSet<ImmutableSet<K>> sets) {
		if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
			return EMPTY;
		}

		ImmutableSet<K> first = sets.iterator().next();
		sets = sets.__remove(first);

		ImmutableSet<K> result = first;
		for (ImmutableSet<K> elem : sets) {
			result = Util_LazyHashCode.intersect(result, elem);
		}

		return result;
	}

	/*
	 * Intersect two sets.
	 */
	public static <K> ImmutableSet<K> intersect(ImmutableSet<K> set1, ImmutableSet<K> set2) {
		if (set1 == set2)
			return set1;
		if (set1 == null)
			return TrieSet_5Bits_LazyHashCode.of();
		if (set2 == null)
			return TrieSet_5Bits_LazyHashCode.of();

		final ImmutableSet<K> smaller;
		final ImmutableSet<K> bigger;

		final ImmutableSet<K> unmodified;

		if (set2.size() >= set1.size()) {
			unmodified = set1;
			smaller = set1;
			bigger = set2;
		} else {
			unmodified = set2;
			smaller = set2;
			bigger = set1;
		}

		final TransientSet<K> tmp = smaller.asTransient();
		boolean modified = false;

		for (Iterator<K> it = tmp.iterator(); it.hasNext();) {
			final K key = it.next();
			if (!bigger.contains(key)) {
				it.remove();
				modified = true;
			}
		}

		if (modified) {
			return tmp.freeze();
		} else {
			return unmodified;
		}
	}

	/*
	 * Subtract one set from another.
	 */
	public static <K> ImmutableSet<K> subtract(ImmutableSet<K> set1, ImmutableSet<K> set2) {
		if (set1 == null && set2 == null)
			return TrieSet_5Bits_LazyHashCode.of();
		if (set1 == set2)
			return TrieSet_5Bits_LazyHashCode.of();
		if (set1 == null)
			return TrieSet_5Bits_LazyHashCode.of();
		if (set2 == null)
			return set1;

		final TransientSet<K> tmp = set1.asTransient();
		boolean modified = false;

		for (K key : set2) {
			if (tmp.__remove(key)) {
				modified = true;
			}
		}

		if (modified) {
			return tmp.freeze();
		} else {
			return set1;
		}
	}

	/*
	 * Union two sets.
	 */
	public static <K> ImmutableSet<K> union(ImmutableSet<K> set1, ImmutableSet<K> set2) {
		if (set1 == null && set2 == null)
			return TrieSet_5Bits_LazyHashCode.of();
		if (set1 == null)
			return set2;
		if (set2 == null)
			return set1;

		if (set1 == set2)
			return set1;

		final ImmutableSet<K> smaller;
		final ImmutableSet<K> bigger;

		final ImmutableSet<K> unmodified;

		if (set2.size() >= set1.size()) {
			unmodified = set2;
			smaller = set1;
			bigger = set2;
		} else {
			unmodified = set1;
			smaller = set2;
			bigger = set1;
		}

		final TransientSet<K> tmp = bigger.asTransient();
		boolean modified = false;

		for (K key : smaller) {
			if (tmp.__insert(key)) {
				modified = true;
			}
		}

		if (modified) {
			return tmp.freeze();
		} else {
			return unmodified;
		}
	}

	/*
	 * Flattening of a set (of ITuple elements). Because of the untyped nature
	 * of ITuple, the implementation is not strongly typed.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Iterable<?>, T> ImmutableSet<T> carrier(ImmutableSet<K> set1) {
		TransientSet<Object> builder = TrieSet_5Bits_LazyHashCode.transientOf();

		for (K iterable : set1) {
			for (Object nested : iterable) {
				builder.__insert(nested);
			}
		}

		return (ImmutableSet<T>) builder.freeze();
	}

	/*
	 * Projection from a tuple to single field.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends IValue> ImmutableSet<K> project(ImmutableSet<ITuple> set1, int field) {
		TransientSet<K> builder = TrieSet_5Bits_LazyHashCode.transientOf();

		for (ITuple tuple : set1) {
			builder.__insert((K) tuple.select(field));
		}

		return builder.freeze();
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	public static ImmutableSet<ITuple> project(ImmutableSet<ITuple> set1, int field1, int field2) {
		TransientSet<ITuple> builder = TrieSet_5Bits_LazyHashCode.transientOf();

		for (ITuple tuple : set1) {
			builder.__insert((ITuple) tuple.select(field1, field2));
		}

		return builder.freeze();
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> ImmutableMap<K, ImmutableSet<V>> toMap(ImmutableSet<ITuple> st) {
		Map<K, TransientSet<V>> hm = new HashMap<>();

		for (ITuple t : st) {
			K key = (K) t.get(0);
			V val = (V) t.get(1);
			TransientSet<V> wValSet = hm.get(key);
			if (wValSet == null) {
				wValSet = TrieSet_5Bits_LazyHashCode.transientOf();
				hm.put(key, wValSet);
			}
			wValSet.__insert(val);
		}

		TransientMap<K, ImmutableSet<V>> w = TrieMap_5Bits_LazyHashCode.transientOf();
		for (K k : hm.keySet()) {
			w.__put(k, hm.get(k).freeze());
		}
		return w.freeze();
	}

}
