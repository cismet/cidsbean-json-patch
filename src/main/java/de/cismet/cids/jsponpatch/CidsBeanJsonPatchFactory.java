package de.cismet.cids.jsponpatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatchFactory;
import de.cismet.cids.jsponpatch.operation.cidsbean.TestOperation;


/**
 * CidsBeanJsonPatchFactory can create a JsonPatchFactory configured to work with CidsBean Patch operations.
 */
public class CidsBeanJsonPatchFactory
{
    public static JsonPatchFactory create()
    {
        ObjectMapper mapper = JacksonUtils.newMapper();
        mapper.registerSubtypes(
                new NamedType(TestOperation.class, TestOperation.OPERATION_NAME)
        );
        return new JsonPatchFactory(mapper);
    }
}
