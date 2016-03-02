/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.cismet.cids.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.operation.*;

import com.google.common.collect.ImmutableList;

import java.io.IOException;

import java.util.List;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.jsonpatch.operation.CidsBeanPatchOperation;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonPatch implements JsonSerializable {

    //~ Instance fields --------------------------------------------------------

    protected final List<CidsBeanPatchOperation> operations;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor.
     *
     * <p>Normally, you should never have to use it.</p>
     *
     * @param  operations  the list of operations for this patch
     *
     * @see    JsonPatchOperation
     */
    @JsonCreator
    public CidsBeanJsonPatch(final List<CidsBeanPatchOperation> operations) {
        this.operations = ImmutableList.copyOf(operations);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Apply this patch to a JSON value.
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  the patched JSON value
     *
     * @throws  JsonPatchException  failed to apply patch
     */
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        CidsBean ret = cidsBean;
        for (final CidsBeanPatchOperation operation : operations) {
            ret = operation.apply(cidsBean);
        }

        return ret;
    }

    @Override
    public String toString() {
        return operations.toString();
    }

    @Override
    public void serialize(final JsonGenerator jgen,
            final SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        for (final JsonPatchOperation op : operations) {
            op.serialize(jgen, provider);
        }
        jgen.writeEndArray();
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen,
            final SerializerProvider provider,
            final TypeSerializer typeSer) throws IOException {
        serialize(jgen, provider);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<CidsBeanPatchOperation> getOperations() {
        return operations;
    }
}
