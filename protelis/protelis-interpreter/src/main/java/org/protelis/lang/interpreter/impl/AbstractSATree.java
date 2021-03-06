/*******************************************************************************
 * Copyright (C) 2010, 2015, Danilo Pianini and contributors
 * listed in the project's build.gradle or pom.xml file.
 *
 * This file is part of Protelis, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE.txt in this project's top directory.
 *******************************************************************************/
package org.protelis.lang.interpreter.impl;

import java.util.List;

import org.protelis.lang.interpreter.AnnotatedTree;
import org.protelis.lang.interpreter.SuperscriptedAnnotatedTree;
import org.protelis.lang.loading.Metadata;

/**
 * Basic implementation of a {@link SuperscriptedAnnotatedTree}.
 *
 * @param <S>
 *            Superscript type
 * @param <T>
 *            Annotation type
 */
public abstract class AbstractSATree<S, T> extends AbstractAnnotatedTree<T>
        implements SuperscriptedAnnotatedTree<S, T> {

    private static final long serialVersionUID = 457607604000217166L;
    private S superscript;

    /**
     * @param metadata
     *            A {@link Metadata} object containing information about the code that generated this AST node.
     * @param branches
     *            branches of this {@link AbstractSATree}
     */
    protected AbstractSATree(final Metadata metadata, final AnnotatedTree<?>... branches) {
        super(metadata, branches);
    }

    /**
     * @param metadata
     *            A {@link Metadata} object containing information about the code that generated this AST node.
     * @param branches
     *            branches of this {@link AbstractSATree}
     */
    protected AbstractSATree(final Metadata metadata, final List<AnnotatedTree<?>> branches) {
        super(metadata, branches);
    }

    @Override
    public final void erase() {
        setSuperscript(null);
        super.erase();
    }

    @Override
    public final S getSuperscript() {
        return superscript;
    }

    /**
     * @param obj
     *            the new superscript
     */
    protected final void setSuperscript(final S obj) {
        superscript = obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + "{ "
            + (superscript instanceof AnnotatedTree ? stringFor((AnnotatedTree<?>) superscript) : superscript)
            + " }";
    }

}
