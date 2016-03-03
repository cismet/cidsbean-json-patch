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
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.github.fge.jsonpatch.JsonPatchException;

import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.jsonpatch.CidsBeanPatchUtils;
import de.cismet.cids.jsonpatch.operation.CidsBeanPatchOperation;

/**
 * DOCUMENT ME!
 *
 * @author   Pascal Dihé <pascal.dihe@cismet.de>
 * @version  $Revision$, $Date$
 */
public class MoveOperation extends com.github.fge.jsonpatch.operation.MoveOperation implements CidsBeanPatchOperation {

    //~ Static fields/initializers ---------------------------------------------

    protected static final CidsBeanPatchUtils UTILS = CidsBeanPatchUtils.getInstance();
    protected static final ResourceBundle RESOURCE_BUNDLE = UTILS.getResourceBundle();
    protected static final Logger LOGGER = Logger.getLogger(MoveOperation.class);

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
        if (from.equals(path)) {
            return cidsBean;
        }

        if (path.toString().indexOf(from.toString()) == 0) {
            LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.invalidFromPath")
                        + ": from=" + this.from + ", to=" + this.path);
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.invalidFromPath"));
        }

        // final TokenResolver<JsonNode> token = Iterables.removeAll(value, parentList).getLast(path);
        final String cidsBeanToPointer = UTILS.jsonPointerToCidsBeanPointer(this.path);
        final String cidsBeanFromPointer = UTILS.jsonPointerToCidsBeanPointer(this.from);
        if ((cidsBeanToPointer == null) || cidsBeanToPointer.isEmpty()
                    || (cidsBeanFromPointer == null) || cidsBeanFromPointer.isEmpty()) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.rootNodeNotPermitted"));
        }

        final Object moveObject = cidsBean.getProperty(cidsBeanFromPointer);
        if (moveObject == null) {
            LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.nullValue") + ": " + cidsBeanFromPointer);
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.nullValue"));
        }

        final JsonNode value;
        if (CidsBean.class.isAssignableFrom(moveObject.getClass())) {
            value = UTILS.cidsBeanToJsonNode((CidsBean)moveObject);
        } else if (List.class.isAssignableFrom(moveObject.getClass())) {
            value = JsonNodeFactory.instance.arrayNode();
            for (final CidsBean listEntry : (List<CidsBean>)moveObject) {
                ((ArrayNode)value).add(UTILS.cidsBeanToJsonNode(listEntry));
            }
        } else {
            value = UTILS.getCidsBeanMapper().valueToTree(moveObject);
        }

        final RemoveOperation removeOperation = new RemoveOperation(from);
        final AddOperation addOperation = new AddOperation(path, value);

        return addOperation.apply(removeOperation.apply(cidsBean));
    }
}
