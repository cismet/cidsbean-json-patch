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

import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatchException;

import java.util.ResourceBundle;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.jsonpatch.CidsBeanPatchUtils;
import de.cismet.cids.jsonpatch.operation.CidsBeanPatchOperation;

/**
 * JSON Patch {@code test} operation.
 *
 * <p>The two arguments for this operation are the pointer containing the value to test ({@code path}) and the value to
 * test equality against ({@code value}).</p>
 *
 * <p>It is an error if no value exists at the given path.</p>
 *
 * <p>Also note that equality as defined by JSON Patch is exactly the same as it is defined by JSON Schema itself. As
 * such, this operation reuses {@link JsonNumEquals} for testing equality.</p>
 *
 * @version  $Revision$, $Date$
 */
public class TestOperation extends com.github.fge.jsonpatch.operation.TestOperation implements CidsBeanPatchOperation {

    //~ Static fields/initializers ---------------------------------------------

    protected static final ResourceBundle RESOURCE_BUNDLE = CidsBeanPatchUtils.getInstance().getResourceBundle();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TestOperation object.
     *
     * @param  path   DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    @JsonCreator
    public TestOperation(@JsonProperty("path") final JsonPointer path,
            @JsonProperty("value") final JsonNode value) {
        super(path, value);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        final JsonNode node = CidsBeanPatchUtils.getInstance().cidsBeanToJsonNode(cidsBean);
        final JsonNode tested = super.apply(node);

        if (tested != null) {
            return cidsBean;
        } else {
            return null;
        }

//        final String cidsBeanPointer = CidsBeanPatchUtils.getInstance().jsonPointerToCidsBeanPointer(this.path);
//        final Object tested;
//        if(cidsBeanPointer != null && !cidsBeanPointer.isEmpty()) {
//            tested =  cidsBean.getProperty(cidsBeanPointer);
//        } else {
//            tested = cidsBean;
//        }
//
//        if(tested == null) {
//            throw new JsonPatchException("jsonPatch.noSuchPath");
//        }
//
//        final JsonNode testedNode = CidsBeanPatchUtils.getInstance().getCidsBeanMapper().valueToTree(value);
//
//
//        if (!tested.equals(value))
//            throw new JsonPatchException(
//                "jsonPatch.valueTestFailure");
//
//        return null;
    }
}
