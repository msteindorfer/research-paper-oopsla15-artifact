package dom;

import static dom.UtilClojure.EMPTY;
import static dom.UtilClojure.carrier;
import static dom.UtilClojure.intersect;
import static dom.UtilClojure.project;
import static dom.UtilClojure.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.openjdk.jmh.infra.Blackhole;

import clojure.set$difference;
import clojure.set$intersection;
import clojure.set$union;
import clojure.lang.IFn;
import clojure.lang.IPersistentSet;
import clojure.lang.ITransientMap;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;

public class DominatorsClojure implements DominatorBenchmark {

	private PersistentHashSet setofdomsets(PersistentHashMap dom, PersistentHashSet preds) {
		ITransientSet result = (ITransientSet) PersistentHashSet.EMPTY.asTransient();

		for (Object p : preds) {
			PersistentHashSet ps = (PersistentHashSet) dom.get(p);

			result.conj(ps == null ? EMPTY : ps);
		}

		return (PersistentHashSet) result.persistent();
	}

	public PersistentHashSet top(PersistentHashSet graph) {
		return (PersistentHashSet) UtilClojure.set$difference(project(graph, 0), project(graph, 1));
	}

	@SuppressWarnings("unchecked")
	public IConstructor getTop(PersistentHashSet graph) {
		for (IConstructor candidate : (Iterable<IConstructor>) top(graph)) {
			switch (candidate.getName()) {
			case "methodEntry":
			case "functionEntry":
			case "scriptEntry":
				return candidate;
			}
		}

		throw new NoSuchElementException("No candidate found.");
	}

	@SuppressWarnings("unchecked")
	public PersistentHashMap calculateDominators(PersistentHashSet graph) {
		IConstructor n0 = getTop(graph);
		PersistentHashSet nodes = carrier(graph);
		// if (!nodes.getElementType().isAbstractData()) {
		// throw new RuntimeException("nodes is not the right type");
		// }
		PersistentHashMap preds = toMap(project(graph, 1, 0));
		// nodes = nodes.delete(n0);

		ITransientMap w = PersistentHashMap.EMPTY.asTransient();
		w.assoc(n0, PersistentHashSet.create(n0));
		for (IConstructor n : (Iterable<IConstructor>) nodes.disjoin(n0)) {
			w.assoc(n, nodes);
		}
		PersistentHashMap dom = (PersistentHashMap) w.persistent();

		PersistentHashMap prev = PersistentHashMap.create();
		/*
		 * solve (dom) for (n <- nodes) dom[n] = {n} + intersect({dom[p] | p <-
		 * preds[n]?{}});
		 */
		while (!prev.equals(dom)) {
			prev = dom;

			ITransientMap newDom = PersistentHashMap.EMPTY.asTransient();

			for (IConstructor n : (Iterable<IConstructor>) nodes) {
				PersistentHashSet ps = (PersistentHashSet) preds.get(n);
				if (ps == null) {
					ps = EMPTY;
				}
				PersistentHashSet sos = setofdomsets(dom, ps);
				// if (!sos.getType().isSet() ||
				// !sos.getType().getElementType().isSet() ||
				// !sos.getType().getElementType().getElementType().isAbstractData())
				// {
				// throw new RuntimeException("not the right type: " +
				// sos.getType());
				// }
				PersistentHashSet intersected = intersect(sos);
				// if (!intersected.getType().isSet() ||
				// !intersected.getType().getElementType().isAbstractData()) {
				// throw new RuntimeException("not the right type: " +
				// intersected.getType());
				// }
				PersistentHashSet newValue = (PersistentHashSet) UtilClojure.set$union(intersected, PersistentHashSet.create(n));
				// PersistentHashSet newValue = intersected.__insert(n);
				// if (!newValue.getElementType().isAbstractData()) {
				// System.err.println("problem");
				// }
				newDom.assoc(n, newValue);
			}

			// if
			// (!newDom.done().getValueType().getElementType().isAbstractData())
			// {
			// System.err.println("not good");
			// }
			dom = (PersistentHashMap) newDom.persistent();
		}

		return dom;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative) {
		for (PersistentHashSet graph : (ArrayList<PersistentHashSet>) sampledGraphsNative) {
			try {
				bh.consume(new DominatorsClojure().calculateDominators(graph));
			} catch (NoSuchElementException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs) {
		// convert data to remove PDB dependency
		ArrayList<PersistentHashSet> sampledGraphsNative = new ArrayList<>(sampledGraphs.size());

		for (ISet graph : sampledGraphs) {
			ITransientSet convertedValue = (ITransientSet) PersistentHashSet.EMPTY.asTransient();

			for (IValue tuple : graph) {
				convertedValue.conj((ITuple) tuple);
			}

			sampledGraphsNative.add((PersistentHashSet) convertedValue.persistent());
		}

		return sampledGraphsNative;
	}

}

class UtilClojure {

	public final static PersistentHashSet EMPTY = PersistentHashSet.create();

	/*
	 * Intersect many sets.
	 */
	@SuppressWarnings("unchecked")
	public static <K> PersistentHashSet intersect(PersistentHashSet sets) {
		if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
			return EMPTY;
		}

		PersistentHashSet first = ((Iterable<PersistentHashSet>) sets).iterator().next();
		sets = (PersistentHashSet) sets.disjoin(first);

		PersistentHashSet result = first;
		for (PersistentHashSet elem : (Iterable<PersistentHashSet>) sets) {
			result = (PersistentHashSet) UtilClojure.set$intersection(result, elem);
		}

		return result;
	}

//	/*
//	 * Intersect two sets.
//	 */
//	public static <K> PersistentHashSet intersect(PersistentHashSet set1, PersistentHashSet set2) {
//		if (set1 == set2)
//			return set1;
//		if (set1 == null)
//			return PersistentHashSet.create();
//		if (set2 == null)
//			return PersistentHashSet.create();
//
//		final PersistentHashSet smaller;
//		final PersistentHashSet bigger;
//
//		final PersistentHashSet unmodified;
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
//		final ITransientSet tmp = (ITransientSet) smaller.asTransient();
//		boolean modified = false;
//
//		for (Iterator<K> it = ((Iterable<K>) tmp).iterator(); it.hasNext();) {
//			final K key = it.next();
//			if (!bigger.contains(key)) {
//				it.remove();
//				modified = true;
//			}
//		}
//
//		if (modified) {
//			return (PersistentHashSet) tmp.persistent();
//		} else {
//			return unmodified;
//		}
//	}
//
//	/*
//	 * Subtract one set from another.
//	 */
//	public static <K> PersistentHashSet subtract(PersistentHashSet set1, PersistentHashSet set2) {
//		if (set1 == null && set2 == null)
//			return PersistentHashSet.create();
//		if (set1 == set2)
//			return PersistentHashSet.create();
//		if (set1 == null)
//			return PersistentHashSet.create();
//		if (set2 == null)
//			return set1;
//
//		final ITransientSet tmp = (ITransientSet) set1.asTransient();
//		boolean modified = false;
//
//		for (K key : (Iterable<K>) set2) {
//			if (tmp.contains(key)) {
//				tmp.disjoin(key);
//				modified = true;
//			}
//		}
//
//		if (modified) {
//			return (PersistentHashSet) tmp.persistent();
//		} else {
//			return set1;
//		}
//	}
//
//	/*
//	 * Union two sets.
//	 */
//	public static <K> PersistentHashSet union(PersistentHashSet set1, PersistentHashSet set2) {
//		if (set1 == null && set2 == null)
//			return PersistentHashSet.create();
//		if (set1 == null)
//			return set2;
//		if (set2 == null)
//			return set1;
//
//		if (set1 == set2)
//			return set1;
//
//		final PersistentHashSet smaller;
//		final PersistentHashSet bigger;
//
//		final PersistentHashSet unmodified;
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
//		final ITransientSet tmp = (ITransientSet) bigger.asTransient();
//		boolean modified = false;
//
//		for (K key : (Iterable<K>) smaller) {
//			if (tmp.contains(key)) {
//				tmp.conj(key);
//				modified = true;
//			}
//		}
//
//		if (modified) {
//			return (PersistentHashSet) tmp.persistent();
//		} else {
//			return unmodified;
//		}
//	}

	public final static IFn set$union = new set$union();
	public final static IPersistentSet set$union(IPersistentSet xs, IPersistentSet ys) {
		return (IPersistentSet) set$union.invoke(xs, ys);
	}
	
	public final static IFn set$intersection = new set$intersection();
	public final static IPersistentSet set$intersection(IPersistentSet xs, IPersistentSet ys) {
		return (IPersistentSet) set$intersection.invoke(xs, ys);
	}

	public final static IFn set$difference = new set$difference();
	public final static IPersistentSet set$difference(IPersistentSet xs, IPersistentSet ys) {
		return (IPersistentSet) set$difference.invoke(xs, ys);
	}	
	
	/*
	 * Flattening of a set (of ITuple elements). Because of the untyped nature
	 * of ITuple, the implementation is not strongly typed.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Iterable<?>, T> PersistentHashSet carrier(PersistentHashSet set1) {
		ITransientSet builder = (ITransientSet) PersistentHashSet.EMPTY.asTransient();

		for (K iterable : (Iterable<K>) set1) {
			for (Object nested : iterable) {
				builder.conj(nested);
			}
		}

		return (PersistentHashSet) builder.persistent();
	}

	/*
	 * Projection from a tuple to single field.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends IValue> PersistentHashSet project(PersistentHashSet set1, int field) {
		ITransientSet builder = (ITransientSet) PersistentHashSet.EMPTY.asTransient();

		for (ITuple tuple : (Iterable<ITuple>) set1) {
			builder.conj((K) tuple.select(field));
		}

		return (PersistentHashSet) builder.persistent();
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	@SuppressWarnings("unchecked")
	public static PersistentHashSet project(PersistentHashSet set1, int field1, int field2) {
		ITransientSet builder = (ITransientSet) PersistentHashSet.EMPTY.asTransient();

		for (ITuple tuple : (Iterable<ITuple>) set1) {
			builder.conj((ITuple) tuple.select(field1, field2));
		}

		return (PersistentHashSet) builder.persistent();
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> PersistentHashMap toMap(PersistentHashSet st) {
		Map<K, ITransientSet> hm = new HashMap<>();

		for (ITuple t : (Iterable<ITuple>) st) {
			K key = (K) t.get(0);
			V val = (V) t.get(1);
			ITransientSet wValSet = hm.get(key);
			if (wValSet == null) {
				wValSet = (ITransientSet) PersistentHashSet.EMPTY.asTransient();
				hm.put(key, wValSet);
			}
			wValSet.conj(val);
		}

		ITransientMap w = PersistentHashMap.EMPTY.asTransient();
		for (K k : hm.keySet()) {
			w.assoc(k, hm.get(k).persistent());
		}
		return (PersistentHashMap) w.persistent();
	}

}
