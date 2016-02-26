package de.cismet.cids.jsponpatch.operation.cidsbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonNumEquals;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatchException;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.jsponpatch.operation.CidsBeanPatchOperation;

/**
 * JSON Patch {@code test} operation
 *
 * <p>The two arguments for this operation are the pointer containing the value
 * to test ({@code path}) and the value to test equality against ({@code
 * value}).</p>
 *
 * <p>It is an error if no value exists at the given path.</p>
 *
 * <p>Also note that equality as defined by JSON Patch is exactly the same as it
 * is defined by JSON Schema itself. As such, this operation reuses {@link
 * JsonNumEquals} for testing equality.</p>
 */
public class TestOperation
    extends com.github.fge.jsonpatch.operation.TestOperation implements CidsBeanPatchOperation
{
    public static final String OPERATION_NAME = "test";

    @JsonCreator
    public TestOperation(@JsonProperty("path") final JsonPointer path,
        @JsonProperty("value") final JsonNode value)
    {
        super(path, value);
    }

//    @Override
//    public JsonNode apply(final JsonNode node)
//        throws JsonPatchException
//    {
//        final JsonNode tested = path.path(node);
//        if (tested.isMissingNode())
//            throw new JsonPatchException(BUNDLE.getMessage(
//                "jsonPatch.noSuchPath"));
//        if (!EQUIVALENCE.equivalent(tested, value))
//            throw new JsonPatchException(BUNDLE.getMessage(
//                "jsonPatch.valueTestFailure"));
//        return node.deepCopy();
//    }

    @Override
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        return null;
    }
}
