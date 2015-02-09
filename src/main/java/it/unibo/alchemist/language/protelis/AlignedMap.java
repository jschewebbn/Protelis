/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.language.protelis;

import gnu.trove.list.TByteList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.language.protelis.datatype.Field;
import it.unibo.alchemist.language.protelis.datatype.Tuple;
import it.unibo.alchemist.language.protelis.interfaces.AnnotatedTree;
import it.unibo.alchemist.language.protelis.util.CodePath;
import it.unibo.alchemist.language.protelis.util.Stack;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.utils.FasterString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.danilopianini.io.FileUtilities;
import org.danilopianini.lang.Couple;
import org.danilopianini.lang.Pair;

/**
 * @author Danilo Pianini
 *
 */
public class AlignedMap extends AbstractSATree<Map<Object, Couple<DotOperator>>, Tuple> {

	private static final long serialVersionUID = -7655993075803732148L;
	private static final String APPLY = "apply";
	private static final byte FILTER_POS = -1;
	private static final byte RUN_POS = -2;
	private static final FasterString CURFIELD = new FasterString("§CURFIELD§");
	private final AnnotatedTree<Field> fgen;
	private final AnnotatedTree<FunctionDefinition> filterOp;
	private final AnnotatedTree<FunctionDefinition> runOp;
	private final AnnotatedTree<?> defVal;

	public AlignedMap(final AnnotatedTree<Field> arg, final AnnotatedTree<FunctionDefinition> filter, final AnnotatedTree<FunctionDefinition> op, final AnnotatedTree<?> def) {
		super(arg, filter, op, def);
		fgen = arg;
		filterOp = filter;
		runOp = op;
		defVal = def;
	}
	
	@Override
	public AnnotatedTree<Tuple> copy() {
		return new AlignedMap(fgen.copy(), filterOp.copy(), runOp.copy(), defVal.copy());
	}

	@Override
	public void eval(final INode<Object> sigma, final TIntObjectMap<Map<CodePath, Object>> theta, final Stack gamma, final Map<CodePath, Object> lastExec, final Map<CodePath, Object> newMap, final TByteList currentPosition) {
		evalEveryBranchWithProjection(sigma, theta, gamma, lastExec, newMap, currentPosition);
		gamma.push();
		final Field origin = fgen.getAnnotation();
		if(!(origin instanceof Field)) {
			throw new IllegalStateException("The argument must be a field of tuples of tuples. It is a " + origin.getClass() + " instead.");
		}
		/*
		 * Extract one field for each key
		 */
		final Map<Object, Field> fieldKeys = new HashMap<>();
		for(final Pair<INode<Object>, Object> pair: origin.coupleIterator()) {
			final INode<Object> node = pair.getFirst();
			final Object mapo = pair.getSecond();
			if (mapo instanceof Tuple) {
				final Tuple map = (Tuple) mapo;
				for(final Object mappingo: map) {
					if (mappingo instanceof Tuple) {
						final Tuple mapping = (Tuple) mappingo;
						if(mapping.size() == 2) {
							final Object key = mapping.get(0);
							final Object value = mapping.get(1);
							Field ref = fieldKeys.get(key);
							if(ref == null) {
								ref = Field.create(map.size());
								fieldKeys.put(key, ref);
							}
							ref.addSample(node, value);
						} else {
							throw new IllegalStateException("The tuple must have length 2, this has length " + mapping.size());
						}
					} else {
						throw new IllegalStateException("Expected " + Tuple.class + ", got " + mappingo.getClass());
					}
				}
			} else {
				throw new IllegalStateException("Expected " + Tuple.class + ", got " + mapo.getClass());
			}
		}
		/*
		 * Get or initialize the mapping between keys and functions
		 */
		Map<Object, Couple<DotOperator>> funmap = getSuperscript();
		if(funmap == null) {
			funmap = new HashMap<>();
		}
		final Map<Object, Couple<DotOperator>> newFunmap = new HashMap<>(funmap.size());
		setSuperscript(newFunmap);
		final List<Tuple> resl = new ArrayList<>(fieldKeys.size());
		for(final Entry<Object, Field> kf: fieldKeys.entrySet()) {
			final Field value = kf.getValue();
			final Object key = kf.getKey();
			/*
			 * Restrict the domain to the field domain
			 * Create a new theta, fix codepaths, create a new lastexec.
			 */
			final TIntObjectMap<Map<CodePath, Object>> restrictedTheta = theta == null ? null : new TIntObjectHashMap<>();
			if(restrictedTheta != null) {
				/*
				 * Restrict theta
				 */
				for(final INode<Object> n : value.nodeIterator()) {
					if(!n.equals(sigma)) {
						final int id = n.getId();
						restrictedTheta.put(id, theta.get(id));
					}
				}
			}
			/*
			 * Make sure that self is present in each field
			 */
			if(!value.containsNode(sigma)) {
				value.addSample(sigma, defVal.getAnnotation());
			}
			/*
			 * Compute arguments
			 */
			final List<AnnotatedTree<?>> args = new ArrayList<>(2);
			args.add(new Constant<>(key));
			args.add(new Variable(CURFIELD, null, null));
			gamma.put(CURFIELD, value, true);
			/*
			 * Compute the code path
			 */
			final byte[] hash = FileUtilities.serializeObject((Serializable) key);
			currentPosition.add(hash);
			/*
			 * Compute functions if needed
			 */
			Couple<DotOperator> funs = funmap.get(key);
			if(funs == null) {
				funs = new Couple<>(new DotOperator(APPLY, filterOp, args), new DotOperator(APPLY, runOp, args));
			}
			/*
			 * Run the actual filtering and operation
			 */
			final DotOperator fop = funs.getFirst();
			currentPosition.add(FILTER_POS);
			fop.eval(sigma, restrictedTheta, gamma, lastExec, newMap, currentPosition);
			removeLast(currentPosition);
			final Object cond = fop.getAnnotation();
			if(cond instanceof Boolean) {
				if((Boolean) cond) {
					/*
					 * Filter passed, run operation.
					 */
					currentPosition.add(RUN_POS);
					final DotOperator rop = funs.getSecond();
					rop.eval(sigma, restrictedTheta, gamma, lastExec, newMap, currentPosition);
					removeLast(currentPosition);
					resl.add(Tuple.create(key, rop.getAnnotation()));
					/*
					 * If both the key exists and the filter passes, save the state.
					 */
					newFunmap.put(key, funs);
				}
			} else {
				throw new IllegalStateException("Filter must return a Boolean, got " + cond.getClass());
			}
			currentPosition.remove(currentPosition.size() - hash.length, hash.length);
		}
		// return type: [[key0, compval0], [key1, compval1], [key2, compval2]]
		setAnnotation(Tuple.create(resl));
		gamma.pop();
	}

	@Override
	protected String innerAsString() {
		return "alignedMap(" + fgen + "," + filterOp + "," + runOp + "," + defVal + ")";
	}

}
