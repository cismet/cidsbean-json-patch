/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.jsonpatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatchFactory;

import de.cismet.cids.jsonpatch.operation.cidsbean.TestOperation;

/**
 * CidsBeanJsonPatchFactory can create a JsonPatchFactory configured to work with CidsBean Patch operations.
 *
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonPatchFactory {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static JsonPatchFactory create() {
        final ObjectMapper mapper = JacksonUtils.newMapper();
        mapper.registerSubtypes(
            new NamedType(TestOperation.class, TestOperation.OPERATION_NAME));
        return new JsonPatchFactory(mapper);
    }
}
