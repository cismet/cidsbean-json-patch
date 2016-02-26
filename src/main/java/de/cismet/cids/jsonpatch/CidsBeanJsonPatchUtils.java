/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.jsonpatch;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.github.fge.jsonpatch.operation.AddOperation;
import com.github.fge.jsonpatch.operation.CopyOperation;
import com.github.fge.jsonpatch.operation.MoveOperation;
import com.github.fge.jsonpatch.operation.RemoveOperation;
import com.github.fge.jsonpatch.operation.ReplaceOperation;

import java.util.Iterator;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.CidsBeanJsonDeserializer;
import de.cismet.cids.dynamics.CidsBeanJsonSerializer;

import de.cismet.cids.jsonpatch.operation.cidsbean.TestOperation;

/**
 * DOCUMENT ME!
 *
 * @author   Pascal Dihé <pascal.dihe@cismet.de>
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonPatchUtils {

    //~ Static fields/initializers ---------------------------------------------

    protected static final CidsBeanJsonPatchUtils INSTANCE = new CidsBeanJsonPatchUtils();

    //~ Instance fields --------------------------------------------------------

    protected final ObjectMapper cidsBeanMapper = new ObjectMapper();
    protected final ResourceBundle resourceBundle = PropertyResourceBundle.getBundle(
            "de.cismet.cids.jsonpatch.messages");

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeanJsonPatchUtils object.
     */
    protected CidsBeanJsonPatchUtils() {
        cidsBeanMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final SimpleModule regularModule = new SimpleModule("NOIOC", new Version(1, 0, 0, null, null, null));
        regularModule.addSerializer(new CidsBeanJsonSerializer());
        regularModule.addDeserializer(CidsBean.class, new CidsBeanJsonDeserializer());
        cidsBeanMapper.registerModule(regularModule);
        cidsBeanMapper.registerSubtypes(
            new NamedType(AddOperation.class, AddOperation.OPERATION_NAME),
            new NamedType(CopyOperation.class, CopyOperation.OPERATION_NAME),
            new NamedType(MoveOperation.class, MoveOperation.OPERATION_NAME),
            new NamedType(RemoveOperation.class, RemoveOperation.OPERATION_NAME),
            new NamedType(ReplaceOperation.class, ReplaceOperation.OPERATION_NAME),
            new NamedType(TestOperation.class, TestOperation.OPERATION_NAME));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CidsBeanJsonPatchUtils getInstance() {
        return INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   jsonPointer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String JsonPointerToCidsBeanPointer(final JsonPointer jsonPointer) {
        final StringBuilder pathBuilder = new StringBuilder();
        final Iterator<TokenResolver<JsonNode>> iterator = jsonPointer.iterator();
        while (iterator.hasNext()) {
            final TokenResolver resolver = iterator.next();
            final int arrayIndex = arrayIndexFor(resolver.getToken().getRaw());

            if (arrayIndex != -1) {
                if ((pathBuilder.length() > 0)
                            && (pathBuilder.charAt(pathBuilder.length() - 1) == '.')) {
                    pathBuilder.deleteCharAt(pathBuilder.length() - 1);
                }

                pathBuilder.append('[').append(arrayIndex).append(']');
            } else {
                pathBuilder.append(resolver.getToken().getRaw());
            }

            if (iterator.hasNext()) {
                pathBuilder.append('.');
            }
        }

        return pathBuilder.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ObjectMapper getCidsBeanMapper() {
        return cidsBeanMapper;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   raw  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static int arrayIndexFor(final String raw) {
        /*
         * Empty? No dice.
         */
        if (raw.isEmpty()) {
            return -1;
        }
        /*
         * Leading zeroes are not allowed in number-only refTokens for arrays. But then, 0 followed by anything else
         * than a number is invalid as well. So, if the string starts with '0', return 0 if the token length is 1 or -1
         * otherwise.
         */
        if (raw.charAt(0) == '0') {
            return (raw.length() == 1) ? 0 : -1;
        }

        /*
         * Otherwise, parse as an int. If we can't, -1.
         */
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
