/*******************************************************************************
 * Copyright (c) 2014-2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package nl.cwi.swat.jmh_dscg_benchmarks;

import static nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.ValueFactoryFactory.VF_CLOJURE;
import static nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.ValueFactoryFactory.VF_PDB_PERSISTENT_CURRENT;
import static nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.ValueFactoryFactory.VF_PDB_PERSISTENT_MEMOIZED_LAZY;
import static nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.ValueFactoryFactory.VF_SCALA;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory;
import org.eclipse.imp.pdb.facts.util.ImmutableMap;
import org.eclipse.imp.pdb.facts.util.ImmutableSet;
import org.eclipse.imp.pdb.facts.util.TrieMap_5Bits;
import org.eclipse.imp.pdb.facts.util.TrieSet_5Bits;
import org.eclipse.imp.pdb.facts.util.TrieMap_5Bits_Memoized_LazyHashCode;
import org.eclipse.imp.pdb.facts.util.TrieSet_5Bits_Memoized_LazyHashCode;

import nl.cwi.swat.jmh_dscg_benchmarks.FootprintUtils.Archetype;
import nl.cwi.swat.jmh_dscg_benchmarks.FootprintUtils.DataType;
import nl.cwi.swat.jmh_dscg_benchmarks.FootprintUtils.MemoryFootprintPreset;
import scala.Tuple2;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;

public final class CalculateFootprints {

	private static boolean reportSet = true;
	private static boolean reportMap = true;

	public static java.util.Set<IValue> setUpTestSetWithRandomContent(int size, int run) {
		java.util.Set<IValue> setWriter = new HashSet<>();

		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial);

		for (int i = size; i > 0; i--) {
			final int j = rand.nextInt();
			final IValue current = ValueFactory.getInstance().integer(j);			

			setWriter.add(current);
		}

		return Collections.unmodifiableSet(setWriter);
	}

	public static Object invokeFactoryMethodAndYieldEmptyInstance(final Class<?> target) {
		final Method factoryMethodOfEmpty;

		try {
			factoryMethodOfEmpty = target.getMethod("of");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

		try {
			return factoryMethodOfEmpty.invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static final String classToName(Class<?> clazz) {
		return Objects.toString(clazz.getCanonicalName());
	}

	public static String measureFootprintOfPersistentChampSet(final Set<IValue> testSet,
			int elementCount, int run, Optional<String> shortName, final Class<?> clazz) {

		@SuppressWarnings("unchecked")
		ImmutableSet<IValue> set = (ImmutableSet<IValue>) invokeFactoryMethodAndYieldEmptyInstance(clazz);

		for (IValue v : testSet) {
			set = set.__insert(v);
		}

		return FootprintUtils.measureAndReport(set, shortName.orElse(classToName(clazz)),
				DataType.SET, Archetype.PERSISTENT, true, elementCount, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	public static String measureFootprintOfPersistentChampMap(final Set<IValue> testSet,
			int elementCount, int run, Optional<String> shortName, final Class<?> clazz) {

		@SuppressWarnings("unchecked")
		ImmutableMap<IValue, IValue> map = (ImmutableMap<IValue, IValue>) invokeFactoryMethodAndYieldEmptyInstance(clazz);

		for (IValue v : testSet) {
			map = map.__put(v, v);
		}

		return FootprintUtils.measureAndReport(map, shortName.orElse(classToName(clazz)),
				DataType.MAP, Archetype.PERSISTENT, true, elementCount, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	public static String measureFootprintOfPersistentClojureSet(final Set<IValue> testSet,
			int elementCount, int run, Optional<String> shortName) {
		final Class<?> clazz = clojure.lang.PersistentHashSet.class;

		IPersistentSet set = (IPersistentSet) PersistentHashSet.EMPTY;

		for (IValue v : testSet) {
			set = (IPersistentSet) set.cons(v);
		}

		return FootprintUtils.measureAndReport(set, shortName.orElse(classToName(clazz)),
				DataType.SET, Archetype.PERSISTENT, true, elementCount, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	public static String measureFootprintOfPersistentClojureMap(final Set<IValue> testSet,
			int elementCount, int run, Optional<String> shortName) {
		final Class<?> clazz = clojure.lang.PersistentHashMap.class;

		IPersistentMap map = (IPersistentMap) PersistentHashMap.EMPTY;

		for (IValue v : testSet) {

			map = (IPersistentMap) map.assoc(v, v);
		}

		return FootprintUtils.measureAndReport(map, shortName.orElse(classToName(clazz)),
				DataType.MAP, Archetype.PERSISTENT, true, elementCount, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	public static String measureFootprintOfPersistentScalaSet(final Set<IValue> testSet,
			int elementCount, int run, Optional<String> shortName) {
		final Class<?> clazz = scala.collection.immutable.HashSet.class;

		scala.collection.immutable.HashSet<IValue> set = new scala.collection.immutable.HashSet<>();

		for (IValue v : testSet) {
			set = set.$plus(v);
		}

		return FootprintUtils.measureAndReport(set, shortName.orElse(classToName(clazz)),
				DataType.SET, Archetype.PERSISTENT, false, elementCount, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	public static String measureFootprintOfPersistentScalaMap(final Set<IValue> testSet,
			int elementCount, int run, Optional<String> shortName) {
		final Class<?> clazz = scala.collection.immutable.HashMap.class;

		scala.collection.immutable.HashMap<IValue, IValue> map = new scala.collection.immutable.HashMap<>();

		for (IValue v : testSet) {
			map = map.$plus(new Tuple2<>(v, v));
		}

		return FootprintUtils.measureAndReport(map, shortName.orElse(classToName(clazz)),
				DataType.MAP, Archetype.PERSISTENT, false, elementCount, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	public static void main(String[] args) {
		final List<String> results = new LinkedList<>();

		for (int exp = 0; exp <= 23; exp += 1) {
			final int count = (int) Math.pow(2, exp);

			for (int run = 0; run < 5; run++) {
				final Set<IValue> testSet = setUpTestSetWithRandomContent(count, run);

				if (reportSet) {
					results.add(measureFootprintOfPersistentChampSet(testSet, count, run,
							Optional.of(VF_PDB_PERSISTENT_CURRENT.toString()), TrieSet_5Bits.class));

					results.add(measureFootprintOfPersistentChampSet(testSet, count, run,
							Optional.of(VF_PDB_PERSISTENT_MEMOIZED_LAZY.toString()),
							TrieSet_5Bits_Memoized_LazyHashCode.class));

					results.add(measureFootprintOfPersistentClojureSet(testSet, count, run,
							Optional.of(VF_CLOJURE.toString())));

					results.add(measureFootprintOfPersistentScalaSet(testSet, count, run,
							Optional.of(VF_SCALA.toString())));
				}

				if (reportMap) {
					results.add(measureFootprintOfPersistentChampMap(testSet, count, run,
							Optional.of(VF_PDB_PERSISTENT_CURRENT.toString()), TrieMap_5Bits.class));

					results.add(measureFootprintOfPersistentChampMap(testSet, count, run,
							Optional.of(VF_PDB_PERSISTENT_MEMOIZED_LAZY.toString()),
							TrieMap_5Bits_Memoized_LazyHashCode.class));

					results.add(measureFootprintOfPersistentClojureMap(testSet, count, run,
							Optional.of(VF_CLOJURE.toString())));

					results.add(measureFootprintOfPersistentScalaMap(testSet, count, run,
							Optional.of(VF_SCALA.toString())));
				}
			}
		}

		FootprintUtils.writeToFile(Paths.get("map-sizes-and-statistics.csv"), false, results);
	}

}
