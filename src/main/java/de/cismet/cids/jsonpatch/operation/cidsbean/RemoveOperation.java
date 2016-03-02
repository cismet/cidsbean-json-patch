/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.jsonpatch.operation.cidsbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatchException;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.ResourceBundle;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.jsonpatch.CidsBeanJsonPatchUtils;
import de.cismet.cids.jsonpatch.operation.CidsBeanPatchOperation;

/**
 * DOCUMENT ME!
 *
 * @author   Pascal Dihé <pascal.dihe@cismet.de>
 * @version  $Revision$, $Date$
 */
public class RemoveOperation extends com.github.fge.jsonpatch.operation.RemoveOperation
        implements CidsBeanPatchOperation {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOGGER = Logger.getLogger(AddOperation.class);
    protected static final CidsBeanJsonPatchUtils UTILS = CidsBeanJsonPatchUtils.getInstance();
    protected static final ResourceBundle RESOURCE_BUNDLE = UTILS.getResourceBundle();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ReplaceOperation object.
     *
     * @param  path  DOCUMENT ME!
     */
    @JsonCreator
    public RemoveOperation(@JsonProperty("path") final JsonPointer path) {
        super(path);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        final String cidsBeanPointer = UTILS.jsonPointerToCidsBeanPointer(this.path);
        if ((cidsBeanPointer == null) || cidsBeanPointer.isEmpty()) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.rootNodeNotPermitted"));
        }

        final String cidsBeanParentPointer = UTILS.jsonPointerToCidsBeanPointer(this.path.parent());
        final Object parentObject;

        // parent is the root object
        if ((cidsBeanParentPointer != null) && !cidsBeanParentPointer.isEmpty()) {
            parentObject = cidsBean.getProperty(cidsBeanParentPointer);
        } else {
            parentObject = cidsBean;
        }

        if (parentObject == null) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.noSuchParent"));
        }

        final Object removeObject = cidsBean.getProperty(cidsBeanPointer);
        if (removeObject == null) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.noSuchProperty"));
        }

        if (List.class.isAssignableFrom(parentObject.getClass())) {
            final boolean removed = ((List)parentObject).remove(removeObject);
            if (!removed) {
                throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.removeFromArrayFailed"));
            }

            return cidsBean;
        } else if (CidsBean.class.isAssignableFrom(parentObject.getClass())) {
            try {
                cidsBean.setProperty(cidsBeanPointer, null);
            } catch (Exception ex) {
                LOGGER.error(RESOURCE_BUNDLE.getString(
                        "jsonPatch.removePropertyFailed")
                            + ": " + cidsBeanPointer, ex);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.removePropertyFailed"), ex);
            }

            return cidsBean;
        } else {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.parentNotContainer"));
        }
    }
}
