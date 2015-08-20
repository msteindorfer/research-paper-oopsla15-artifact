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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import objectexplorer.ObjectGraphMeasurer.Footprint;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public final class FootprintUtils {

	final static String CSV_HEADER = "elementCount,run,className,dataType,archetype,supportsStagedMutability,footprintInBytes,footprintInObjects,footprintInReferences"; // ,footprintInPrimitives

	public enum Archetype {
		MUTABLE, IMMUTABLE, PERSISTENT
	}

	public enum DataType {
		MAP, SET
	}

	public enum MemoryFootprintPreset {
		RETAINED_SIZE, DATA_STRUCTURE_OVERHEAD
	}

	public static String measureAndReport(final Object objectToMeasure,
			final String className, DataType dataType, Archetype archetype,
			boolean supportsStagedMutability, int size, int run,
			MemoryFootprintPreset preset) {
		final Predicate<Object> predicate;

		switch (preset) {
		case DATA_STRUCTURE_OVERHEAD:
			predicate = Predicates.not(Predicates.instanceOf(org.eclipse.imp.pdb.facts.IValue.class));
			break;
		case RETAINED_SIZE:
			predicate = Predicates.alwaysTrue();
			break;
		default:
			throw new IllegalStateException();
		}

		// System.out.println(GraphLayout.parseInstance(objectToMeasure).totalSize());

		long memoryInBytes = objectexplorer.MemoryMeasurer.measureBytes(
				objectToMeasure, predicate);

		Footprint memoryFootprint = objectexplorer.ObjectGraphMeasurer.measure(
				objectToMeasure, predicate);

		final String statString = String.format("%d\t %60s\t[%s]\t %s",
				memoryInBytes, className, dataType, memoryFootprint);
		System.out.println(statString);

		final String statFileString = String.format(
				"%d,%d,%s,%s,%s,%b,%d,%d,%d", size, run, className, dataType,
				archetype, supportsStagedMutability, memoryInBytes,
				memoryFootprint.getObjects(), memoryFootprint.getReferences());

		return statFileString;
	}

	static List<Integer> createLinearRange(int start, int end, int stride) {
		int count = (end - start) / stride;
		ArrayList<Integer> samples = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			samples.add(start);
			start += stride;
		}

		return samples;
	}

	static List<Integer> createExponentialRange(int start, int end) {
		ArrayList<Integer> samples = new ArrayList<>(end - start);

		for (int exp = start; exp < end; exp++) {
			samples.add((int) Math.pow(2, exp));
		}

		return samples;
	}

	static void writeToFile(Path file, boolean isAppendingToFile,
			List<String> lines) {
		// write stats to file
		try {
			if (isAppendingToFile) {
				Files.write(file, lines, StandardCharsets.UTF_8,
						StandardOpenOption.APPEND);
			} else {
				Files.write(file, Arrays.asList(CSV_HEADER),
						StandardCharsets.UTF_8);
				Files.write(file, lines, StandardCharsets.UTF_8,
						StandardOpenOption.APPEND);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
