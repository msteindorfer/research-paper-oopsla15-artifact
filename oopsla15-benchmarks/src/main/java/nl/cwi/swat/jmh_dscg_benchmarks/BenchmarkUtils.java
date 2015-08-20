/*******************************************************************************
 * Copyright (c) 2013 CWI
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

import java.util.Random;

import org.eclipse.imp.pdb.facts.IValueFactory;

public class BenchmarkUtils {
	public static enum ValueFactoryFactory {
		VF_CLOJURE {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.TypelessValueFactory
						.getInstance();
			}
		},
		VF_SCALA {
			@Override
			public IValueFactory getInstance() {
				return new org.eclipse.imp.pdb.facts.impl.persistent.scala.TypelessValueFactory();
			}
		},
		VF_PDB_PERSISTENT_CURRENT {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryCurrent
						.getInstance();
			}
		},
		VF_PDB_PERSISTENT_MEMOIZED_LAZY {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryMemoizedLazy
						.getInstance();
			}
		};

		public abstract IValueFactory getInstance();
	}

	public static enum DataType {
		MAP, SET
	}

	public static enum SampleDataSelection {
		MATCH, RANDOM
	}

	public static int seedFromSizeAndRun(int size, int run) {
		return mix(size) ^ mix(run);
	}

	private static int mix(int n) {
		int h = n;

		h *= 0x5bd1e995;
		h ^= h >>> 13;
		h *= 0x5bd1e995;
		h ^= h >>> 15;

		return h;
	}

	static int[] generateTestData(int size, int run) {
		int[] data = new int[size];
	
		int seedForThisTrial = seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial);
	
		System.out.println(String.format("Seed for this trial: %d.", seedForThisTrial));
	
		for (int i = size - 1; i >= 0; i--) {
			data[i] = rand.nextInt();
		}
	
		return data;
	}
	
}
