/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.jsonpatch;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.operation.AddOperation;
import com.github.fge.jsonpatch.operation.CopyOperation;
import com.github.fge.jsonpatch.operation.MoveOperation;
import com.github.fge.jsonpatch.operation.RemoveOperation;
import com.github.fge.jsonpatch.operation.ReplaceOperation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.CidsBeanInfo;
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
    public String jsonPointerToCidsBeanPointer(final JsonPointer jsonPointer) {
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

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  JsonPatchException  DOCUMENT ME!
     */
    public CidsBean jsonNodeToCidsBean(final JsonNode node) throws JsonPatchException {
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(false, node.toString());
            return cidsBean;
        } catch (Exception ex) {
            throw new JsonPatchException(this.resourceBundle.getString("jsonPatch.deserBeanFailed"), ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  JsonPatchException  DOCUMENT ME!
     */
    public List<CidsBean> jsonNodeArrayToCidsBeanArray(final JsonNode node) throws JsonPatchException {
        if (this.isCidsBeanArray(node)) {
            final List<CidsBean> beanList = new ArrayList<CidsBean>();
            final Iterator<JsonNode> nodeIterator = ((ArrayNode)node).elements();
            while (nodeIterator.hasNext()) {
                final JsonNode arrayElementNode = nodeIterator.next();
                beanList.add(this.jsonNodeToCidsBean(arrayElementNode));
            }

            return beanList;
        } else {
            throw new JsonPatchException(this.resourceBundle.getString("jsonPatch.valueNoArray"));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  JsonPatchException  DOCUMENT ME!
     */
    public JsonNode cidsBeanToJsonNode(final CidsBean cidsBean) throws JsonPatchException {
        // does not work! cids bean json serialisation broken final JsonNode node =
        // CidsBeanJsonPatchUtils.getInstance().getCidsBeanMapper().valueToTree(cidsBean);

        final String json = cidsBean.toJSONString(false);
        final JsonNode node;
        try {
            node = CidsBeanJsonPatchUtils.getInstance().getCidsBeanMapper().readTree(json);
            return node;
        } catch (IOException ex) {
            throw new JsonPatchException(this.resourceBundle.getString("jsonPatch.serBeanFailed"), ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCidsBean(final JsonNode node) {
        return node.isObject()
                    && (node.hasNonNull(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER)
                        || node.hasNonNull(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCidsBeanArray(final JsonNode node) {
        if (node.isArray()) {
            final Iterator<JsonNode> nodeIterator = ((ArrayNode)node).elements();
            while (nodeIterator.hasNext()) {
                final JsonNode arrayElementNode = nodeIterator.next();
                if (!this.isCidsBean(arrayElementNode)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCidsBeanReference(final JsonNode node) {
        return node.isObject() && node.hasNonNull(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  JsonPatchException  DOCUMENT ME!
     */
    public Object deserializeAndVerifyCidsBean(final JsonNode value) throws JsonPatchException {
        if (this.isCidsBean(value)) {
            if (this.isCidsBeanReference(value)) {
                final CidsBeanInfo cidsBeanInfo = new CidsBeanInfo(value.get(
                            CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER).textValue());
                if (cidsBeanInfo.getObjectKey().equals("-1") || (value.size() != 1)) {
                    throw new JsonPatchException(resourceBundle.getString("jsonPatch.valueNoReferenceBean"));
                } else {
                    final CidsBean cidsBean = this.jsonNodeToCidsBean(value);
                    return cidsBean;
                }
            } else {
                final CidsBeanInfo cidsBeanInfo = new CidsBeanInfo(value.get(
                            CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER).textValue());
                if (!cidsBeanInfo.getObjectKey().equals("-1")) {
                    throw new JsonPatchException(resourceBundle.getString("jsonPatch.valueNoNewBean"));
                } else {
                    final CidsBean cidsBean = this.jsonNodeToCidsBean(value);
                    this.applyCidsBeanUpdateStatus(cidsBean, false);
                    return cidsBean;
                }
            }
        } else if (this.isCidsBeanArray(value)) {
            final List<CidsBean> beanList = new ArrayList<CidsBean>();
            final Iterator<JsonNode> nodeIterator = ((ArrayNode)value).elements();
            while (nodeIterator.hasNext()) {
                final JsonNode arrayElementNode = nodeIterator.next();
                final Object arrayElement = this.deserializeAndVerifyCidsBean(arrayElementNode);
                if ((arrayElement != null) && CidsBean.class.isAssignableFrom(arrayElement.getClass())) {
                    final CidsBean cidsBean = (CidsBean)arrayElement;
                    beanList.add(cidsBean);
                } else {
                    throw new JsonPatchException(resourceBundle.getString("jsonPatch.invalidArrayElement"));
                }
            }
            return beanList;
        } else if (value.isValueNode()) {
            final ValueNode valueNode = (ValueNode)value;
            return valueNode;
        } else {
            throw new JsonPatchException(resourceBundle.getString("jsonPatch.invalidValue"));
        }
    }

    /**
     * Recursively applies the update status of a CidsBeans MetaObject and all descendant MetaObject Attributes
     * according to the following rules:
     *
     * <p>If the id of the CidsBean / MetObject is -1, the status of the MetObject ist set to MetaObject.NEW. Otherwise,
     * the status is the to MetaObject.MODIFIED if the setChanged parameter is true.</p>
     *
     * @param  cidsBean    DOCUMENT ME!
     * @param  setChanged  Apply MetaObject.MODIFIED to all metaObject Attributes
     */
    public void applyCidsBeanUpdateStatus(final CidsBean cidsBean, final boolean setChanged) {
        this.applyMetaObjectUpdateStatus(cidsBean.getMetaObject(), setChanged);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaObject  DOCUMENT ME!
     * @param  setChanged  DOCUMENT ME!
     */
    protected void applyMetaObjectUpdateStatus(final MetaObject metaObject, final boolean setChanged) {
        if (metaObject.getID() == -1) {
            metaObject.setStatus(MetaObject.NEW);
        } else if (setChanged) {
            metaObject.setStatus(MetaObject.MODIFIED);
        }

        for (final ObjectAttribute objectAttribute : metaObject.getAttribs()) {
            if (objectAttribute.referencesObject() && (objectAttribute.getValue() != null)) {
                final MetaObject attributeMetaObject = (MetaObject)objectAttribute.getValue();
                attributeMetaObject.setChanged(true);
                this.applyMetaObjectUpdateStatus(attributeMetaObject, setChanged);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ewkt  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Geometry fromEwkt(final String ewkt) throws Exception {
        final int skIndex = ewkt.indexOf(';');

        final String wkt;
        final int srid;

        if (skIndex > 0) {
            final String sridKV = ewkt.substring(0, skIndex);
            final int eqIndex = sridKV.indexOf('=');
            wkt = ewkt.substring(skIndex + 1);
            srid = Integer.parseInt(sridKV.substring(eqIndex + 1));
        } else {
            wkt = ewkt;
            srid = -1;
        }

        final Geometry geom = new WKTReader(new GeometryFactory()).read(wkt);
        if (srid >= 0) {
            geom.setSRID(srid);
        }
        return geom;
    }
}
