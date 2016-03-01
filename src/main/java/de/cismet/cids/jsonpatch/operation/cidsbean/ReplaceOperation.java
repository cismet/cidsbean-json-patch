/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.jsonpatch.operation.cidsbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatchException;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.jsonpatch.operation.CidsBeanPatchOperation;

/**
 * DOCUMENT ME!
 *
 * @author   Pascal Dihé <pascal.dihe@cismet.de>
 * @version  $Revision$, $Date$
 */
public class ReplaceOperation extends com.github.fge.jsonpatch.operation.ReplaceOperation
        implements CidsBeanPatchOperation {

    //~ Instance fields --------------------------------------------------------

    @JsonIgnore protected final AddOperation replaceOperationDelegate;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ReplaceOperation object.
     *
     * @param  path   DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    @JsonCreator
    public ReplaceOperation(@JsonProperty("path") final JsonPointer path,
            @JsonProperty("value") final JsonNode value) {
        super(path, value);
        replaceOperationDelegate = new AddOperation(path, value, true);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        return replaceOperationDelegate.apply(cidsBean);
    }
}
