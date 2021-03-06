/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.cismet.cids.jsonpatch.operation.cidsbean;

import Sirius.server.localserver.attribute.ObjectAttribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.github.fge.jsonpatch.JsonPatchException;

import com.google.common.collect.Iterables;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.math.BigDecimal;

import java.sql.Timestamp;

import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.jsonpatch.CidsBeanPatchUtils;
import de.cismet.cids.jsonpatch.operation.CidsBeanPatchOperation;

import de.cismet.commons.classloading.BlacklistClassloading;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class AddOperation extends com.github.fge.jsonpatch.operation.AddOperation implements CidsBeanPatchOperation {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOGGER = Logger.getLogger(AddOperation.class);
    protected static final CidsBeanPatchUtils UTILS = CidsBeanPatchUtils.getInstance();
    protected static final ResourceBundle RESOURCE_BUNDLE = UTILS.getResourceBundle();

    //~ Instance fields --------------------------------------------------------

    @JsonIgnore protected final com.github.fge.jsonpatch.operation.ReplaceOperation jsonPatchReplaceOperation;

    @JsonIgnore protected final boolean overwrite;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AddOperation valueObject.
     *
     * @param  path   DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    @JsonCreator
    public AddOperation(@JsonProperty("path") final JsonPointer path,
            @JsonProperty("value") final JsonNode value) {
        this(path, value, false);
    }

    /**
     * Creates a new AddOperation valueObject.
     *
     * @param  path       DOCUMENT ME!
     * @param  value      DOCUMENT ME!
     * @param  overwrite  DOCUMENT ME!
     */
    protected AddOperation(final JsonPointer path, final JsonNode value, final boolean overwrite) {
        super(path, value);
        this.overwrite = overwrite;
        if (this.overwrite) {
            jsonPatchReplaceOperation = new com.github.fge.jsonpatch.operation.ReplaceOperation(path, value);
        } else {
            jsonPatchReplaceOperation = null;
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public JsonNode apply(final JsonNode node) throws JsonPatchException {
        if (this.overwrite) {
            return this.jsonPatchReplaceOperation.apply(node);
        } else {
            return super.apply(node);
        }
    }
    @Override
    public CidsBean apply(final CidsBean cidsBean) throws JsonPatchException {
        if ((this.value == null) || this.value.isMissingNode()) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.nullValue"));
        }

        final String cidsBeanPointer = UTILS.jsonPointerToCidsBeanPointer(this.path);
        if ((cidsBeanPointer == null) || cidsBeanPointer.isEmpty()) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.rootNodeNotPermitted"));
        }

        final String cidsBeanParentPointer = UTILS.jsonPointerToCidsBeanPointer(this.path.parent());
        final Object parentObject;

        // parent is the root valueObject
        if ((cidsBeanParentPointer != null) && !cidsBeanParentPointer.isEmpty()) {
            parentObject = cidsBean.getProperty(cidsBeanParentPointer);
        } else {
            parentObject = cidsBean;
        }

        if (parentObject == null) {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.noSuchParent"));
        }

        if (List.class.isAssignableFrom(parentObject.getClass())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("performing add to array " + this.path.toString());
            }
            this.addToArray((List)parentObject);
            return cidsBean;
        } else if (CidsBean.class.isAssignableFrom(parentObject.getClass())) {
            this.addToObject((CidsBean)parentObject);
            return cidsBean;
        } else {
            throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.parentNotContainer"));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parentList  DOCUMENT ME!
     *
     * @throws  JsonPatchException  DOCUMENT ME!
     */
    protected void addToArray(final List parentList) throws JsonPatchException {
        final TokenResolver<JsonNode> token = Iterables.getLast(path);
        if (token.getToken().equals(LAST_ARRAY_ELEMENT)) {
            if (UTILS.isCidsBeanArray(value)) {
                final List<CidsBean> beanList = (List<CidsBean>)UTILS.deserializeAndVerifyCidsBean(this.value);
                if (this.overwrite) {
                    final ListIterator<CidsBean> listIterator = parentList.listIterator();
                    while (listIterator.hasNext()) {
                        final CidsBean listCidsBean = listIterator.next();
                        for (final CidsBean replacmentBean : beanList) {
                            if (replacmentBean.getCidsBeanInfo().getJsonObjectKey().equals(
                                            listCidsBean.getCidsBeanInfo().getJsonObjectKey())) {
                                listIterator.set(replacmentBean);
                            }
                        }
                    }
                } else {
                    parentList.addAll(beanList);
                }
            } else if (UTILS.isCidsBean(value)) {
                final CidsBean cidsBean = (CidsBean)UTILS.deserializeAndVerifyCidsBean(this.value);
                if (this.overwrite) {
                    final ListIterator<CidsBean> listIterator = parentList.listIterator();
                    while (listIterator.hasNext()) {
                        final CidsBean listCidsBean = listIterator.next();
                        if (cidsBean.getCidsBeanInfo().getJsonObjectKey().equals(
                                        listCidsBean.getCidsBeanInfo().getJsonObjectKey())) {
                            listIterator.set(cidsBean);
                        }
                    }
                } else {
                    parentList.add(cidsBean);
                }
            } else {
                throw new JsonPatchException(RESOURCE_BUNDLE.getString("jsonPatch.invalidValueForArray"));
            }
        } else {
            final int size = parentList.size();
            final int index;
            try {
                index = Integer.parseInt(token.toString());
            } catch (NumberFormatException ex) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.notAnIndex")
                            + ": " + ex.getMessage(), ex);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.notAnIndex"), ex);
            }

            if ((index < 0) || (index > size)) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.notAnIndex")
                            + ": " + index + " (array size: " + size + ")");
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.noSuchIndex"));
            }

            if (UTILS.isCidsBean(value)) {
                final CidsBean cidsBean = (CidsBean)UTILS.deserializeAndVerifyCidsBean(this.value);
                if (this.overwrite) {
                    parentList.set(index, cidsBean);
                } else {
                    parentList.add(index, cidsBean);
                }
            } else if (UTILS.isCidsBeanArray(value)) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.invalidValueForArrayIndex")
                            + ": arrays cannot be used as value in conjunction with an array index");
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.invalidValueForArrayIndex"));
            } else {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.invalidValueForArrayIndex")
                            + ": " + value);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.invalidValueForArrayIndex"));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parentBean  DOCUMENT ME!
     *
     * @throws  JsonPatchException  DOCUMENT ME!
     */
    protected void addToObject(final CidsBean parentBean) throws JsonPatchException {
        final String property = Iterables.getLast(path).getToken().getRaw();

        final ObjectAttribute objectAttribute = parentBean.getMetaObject().getAttributeByFieldName(property);
        if (objectAttribute == null) {
            LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.noSuchProperty")
                        + ": " + this.path.toString());
            throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                    "jsonPatch.noSuchProperty"));
        }

        final Object valueObject = UTILS.deserializeAndVerifyCidsBean(this.value);
        if (CidsBean.class.isAssignableFrom(valueObject.getClass())) {
            if (!objectAttribute.getMai().isForeignKey()) {
                LOGGER.error(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyValueMissmatch") + ": "
                            + "cids bean provided but not expected at " + this.path.toString());
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyValueMissmatch"));
            }

            if (!this.overwrite && (parentBean.getProperty(property) != null)) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.propertyNotEmpty")
                            + ": " + property);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyNotEmpty"));
            }

            try {
                parentBean.setProperty(property, (CidsBean)valueObject);
            } catch (Exception ex) {
                LOGGER.error(RESOURCE_BUNDLE.getString(
                        "jsonPatch.setPropertyFailed")
                            + ": " + property, ex);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.setPropertyFailed"), ex);
            }
        } else if (List.class.isAssignableFrom(valueObject.getClass())) {
            final List<CidsBean> beanCollectionProperty = parentBean.getBeanCollectionProperty(property);
            if (!objectAttribute.getMai().isArray() || (beanCollectionProperty == null)) {
                LOGGER.error(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyValueMissmatch") + ": "
                            + "cids bean array provided but not expected at " + this.path.toString());
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyValueMissmatch"));
            }

            if (!this.overwrite && !beanCollectionProperty.isEmpty()) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.propertyNotEmpty")
                            + ": array " + property + "size: " + beanCollectionProperty.size());
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyNotEmpty"));
            } else if (this.overwrite) {
                beanCollectionProperty.clear();
                beanCollectionProperty.addAll((List<CidsBean>)valueObject);
            } else {
                beanCollectionProperty.addAll((List<CidsBean>)valueObject);
            }
        } else if (ValueNode.class.isAssignableFrom(valueObject.getClass())) {
            if (!this.overwrite && (parentBean.getProperty(property) != null)) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.propertyNotEmpty")
                            + ": " + property);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.propertyNotEmpty"));
            }

            final Class attrClass = BlacklistClassloading.forName(objectAttribute.getMai().getJavaclassname());
            if (attrClass == null) {
                LOGGER.error(RESOURCE_BUNDLE.getString("jsonPatch.noSuchProperty")
                            + ": " + this.path.toString());
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.noSuchProperty"));
            }

            final ValueNode valueNode = (ValueNode)valueObject;

            try {
                if (valueNode.isNumber()) {
                    if (attrClass.equals(Integer.class)) {
                        final int i = valueNode.asInt();
                        parentBean.setProperty(property, i);
                    } else if (attrClass.equals(Long.class)) {
                        final long l = valueNode.asLong();
                        parentBean.setProperty(property, l);
                    } else if (attrClass.equals(Float.class)) {
                        final float f = (float)valueNode.asDouble();
                        parentBean.setProperty(property, f);
                    } else if (attrClass.equals(Double.class)) {
                        final double d = valueNode.asDouble();
                        parentBean.setProperty(property, d);
                    } else if (attrClass.equals(java.sql.Timestamp.class)) {
                        final Timestamp ts = new Timestamp(valueNode.asLong());
                        parentBean.setProperty(property, ts);
                    } else if (attrClass.equals(BigDecimal.class)) {
                        final BigDecimal bd = new BigDecimal(valueNode.asText());
                        parentBean.setProperty(property, bd);
                    } else {
                        throw new Exception("no numeric handler available for class " + attrClass.getSimpleName());
                    }
                } else if (valueNode.isBoolean()) {
                    final boolean bl = valueNode.asBoolean();
                    parentBean.setProperty(property, bl);
                } else if (valueNode.isMissingNode() || NullNode.class.isAssignableFrom(valueNode.getClass())) {
                    parentBean.setProperty(property, null);
                } else if (valueNode.isTextual()) {
                    final String str = valueNode.textValue();
                    if (attrClass.equals(Geometry.class)) {
                        final Geometry geom = UTILS.fromEwkt(str);
                        parentBean.setProperty(property, geom);
                    } else if (attrClass.equals(String.class)) {
                        parentBean.setProperty(property, str);
                    } else {
                        LOGGER.warn("expected geometry or string property but got " + attrClass.getSimpleName());
                        parentBean.setProperty(property, str);
                    }
                } else {
                    throw new Exception("no handler available for value " + valueNode.toString());
                }
            } catch (Exception ex) {
                LOGGER.error(RESOURCE_BUNDLE.getString(
                        "jsonPatch.setPropertyFailed")
                            + ": " + property, ex);
                throw new JsonPatchException(RESOURCE_BUNDLE.getString(
                        "jsonPatch.setPropertyFailed"), ex);
            }
        }
    }
}
