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

package de.cismet.cids.jsponpatch.operation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.operation.PathValueOperation;
import de.cismet.cids.dynamics.CidsBean;

/**
 * Base abstract class for one CidsBean patch operation
 *
 * <p>Two more abstract classes extend this one according to the arguments of
 * the operation:</p>
 *
 * <ul>
 *     <li>{@link com.github.fge.jsonpatch.operation.DualPathOperation} for operations taking a second pointer as
 *     an argument ({@code copy} and {@code move});</li>
 *     <li>{@link PathValueOperation} for operations taking a value as an
 *     argument ({@code add}, {@code replace} and {@code test}).</li>
 * </ul>
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "op")

@JsonIgnoreProperties(ignoreUnknown = true)
public interface CidsBeanPatchOperation
    extends JsonSerializable
{
    /**
     * Apply this operation to a JSON value
     *
     * @param cidsBean the value to patch
     * @return the patched value
     * @throws com.github.fge.jsonpatch.JsonPatchException operation failed to apply to this value
     */
    public CidsBean apply(final CidsBean cidsBean)
        throws JsonPatchException;
}
