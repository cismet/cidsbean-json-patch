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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatchException;

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
public class MoveOperation extends com.github.fge.jsonpatch.operation.MoveOperation implements CidsBeanPatchOperation {

    //~ Static fields/initializers ---------------------------------------------

    protected static final CidsBeanJsonPatchUtils UTILS = CidsBeanJsonPatchUtils.getInstance();
    protected static final ResourceBundle RESOURCE_BUNDLE = UTILS.getResourceBundle();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ReplaceOperation object.
     *
     * @param  from  value DOCUMENT ME!
     * @param  path  DOCUMENT ME!
     */
    @JsonCreator
    public MoveOperation(@JsonProperty("from") final JsonPointer from,
            @JsonProperty("path") final JsonPointer path) {
        super(from, path);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        final String cidsBeanPointer = UTILS.jsonPointerToCidsBeanPointer(this.from);
        if ((cidsBeanPointer == null) || cidsBeanPointer.isEmpty()) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.rootNodeNotPermitted"));
        }

        final Object copyObject = cidsBean.getProperty(cidsBeanPointer);
        if (copyObject == null) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.nullValue"));
        }

        final JsonNode value;
        if (CidsBean.class.isAssignableFrom(copyObject.getClass())) {
            value = UTILS.cidsBeanToJsonNode((CidsBean)copyObject);
        } else if (List.class.isAssignableFrom(copyObject.getClass())) {
            value = JsonNodeFactory.instance.arrayNode();
            for (final CidsBean listEntry : (List<CidsBean>)copyObject) {
                ((ArrayNode)value).add(UTILS.cidsBeanToJsonNode(listEntry));
            }
        } else {
            value = UTILS.getCidsBeanMapper().valueToTree(copyObject);
        }

        return new AddOperation(path, value).apply(cidsBean);
    }
}
