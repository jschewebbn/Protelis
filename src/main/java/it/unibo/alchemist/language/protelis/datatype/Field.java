/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.language.protelis.datatype;

import it.unibo.alchemist.language.protelis.util.Device;
import it.unibo.alchemist.model.interfaces.INode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.danilopianini.lang.Pair;
import org.danilopianini.lang.TriFunction;

/**
 * @author Danilo Pianini
 *
 */
public interface Field extends Serializable {
	
	/**
	 * @param defaultSize creates a new and empty {@link Field}, defaulting on the specified size
	 * @return an empty {@link Field}
	 */
	static Field create(int defaultSize) {
		return new FieldTroveMapImpl(defaultSize + 1, 1f);
	}
	
	static Field applyWithSingleParam(UnaryOperator<Object> fun, int[] fields, Object a) {
		return apply((t, p) -> fun.apply(p[0]), false, fields, null, a);
	}

	static Field apply(BinaryOperator<Object> fun, int[] fields, Object a, Object b) {
		return apply((t, p) -> fun.apply(p[0], p[1]), false, fields, null, a, b);
	}

	static Field apply(final TriFunction<Object, Object, Object, Object> fun, int[] fields, Object a, Object b, Object c){
		return apply((t, p) -> fun.apply(p[0], p[1], p[2]), false, fields, null, a, b, c);
	}
	
	/**
	 * @param fun
	 *            the function to apply.
	 * @param fieldIndexes
	 *            the indexes of which among the arguments are fields
	 * @param args
	 *            the arguments
	 * @return a new field resulting from the application of the
	 *         {@link Function} to the target and the arguments
	 */
	static Field apply(final Function<Object[], Object> fun, int[] fieldIndexes, Object... args){
	    return apply(fun, false, fieldIndexes, null, args);
	}

	/**
	 * @param fun
	 *            the function to apply.
	 * @param fieldTarget
	 *            true if the target is a field
	 * @param fieldIndexes
	 *            the indexes of which among the arguments are fields
	 * @param target
	 *            the object this method will be invoked on
	 * @param args
	 *            the arguments
	 * @return a new field resulting from the application of the
	 *         {@link Function} to the target and the arguments
	 */
	static Field apply(final Function<Object[], Object> fun, final boolean fieldTarget, int[] fieldIndexes, final Object target, Object... args){
		return apply((t, p) -> fun.apply(p), fieldTarget, fieldIndexes, target, args);
	}

	/**
	 * @param fun
	 *            the function to apply. It must accept as a first argument the
	 *            data type carried by the target, and an Object array as second
	 *            argument (namely, it is a vararg)
	 * @param fieldTarget
	 *            true if the target is a field
	 * @param fieldIndexes
	 *            the indexes of which among the arguments are fields
	 * @param target
	 *            the object this method will be invoked on
	 * @param args
	 *            the arguments
	 * @return a new field resulting from the application of the
	 *         {@link BiFunction} to the target and the arguments
	 */
	static Field apply(final BiFunction<Object, Object[], Object> fun, final boolean fieldTarget, final int[] fieldIndexes, final Object target, final Object... args) {
		if (!fieldTarget && fieldIndexes.length == 0) {
			throw new IllegalArgumentException("To use this field application at least one of the parameters must be a field.");
		}
		/*
		 * A consistency check may make sense here.
		 */
		final Field refField = (Field) (fieldTarget ? target : args[fieldIndexes[0]]);
		final Field result = create(refField.size());
		for (final Device node : refField.nodeIterator()) {
			final Object actualTarget = fieldTarget ? (((Field) target).getSample(node)) : target;
			Object[] actualArgs = Arrays.copyOf(args, args.length);
			for (final int i : fieldIndexes) {
				final Object arg = ((Field) actualArgs[i]).getSample(node);
				if (arg == null) {
					throw new IllegalStateException("Field " + actualArgs[i] + " is not aligned with " + args[fieldIndexes[0]] + " (missing " + node + ")");
				}
				actualArgs[i] = arg;
			}
			result.addSample(node, fun.apply(actualTarget, actualArgs));
		}
		return result;
	}
	
	void addSample(Device n, Object v);
	
	Object removeSample(Device n);
	
	Object getSample(Device n);
	
	Device reduceKeys(final BinaryOperator<Device> op, final Device exclude);
	
	Object reduceVals(final BinaryOperator<Object> op, final Device exclude, final Object defaultVal);
	
	Pair<Device, Object> reducePairs(final BinaryOperator<Pair<Device, Object>> accumulator, final Device exclude);

	Iterable<Device> nodeIterator();

	Iterable<Object> valIterator();
	
	Iterable<Pair<Device, Object>> coupleIterator();

	int size();

	boolean isEmpty();
	
	boolean containsNode(Device n);
	
	boolean containsNode(long n);
	
	Class<?> getExpectedType();

	
}
