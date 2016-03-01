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
package de.cismet.cids.jsonpatch.operation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.operation.JsonPatchOperation;
import com.google.common.collect.Lists;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.jsonpatch.CidsBeanJsonPatchUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;
import org.testng.log4testng.Logger;

public abstract class CidsBeanPatchOperationTest {

    protected final JsonNode errors;
    protected final JsonNode ops;
    protected final ObjectReader reader;
    protected final static Logger LOGGER = Logger.getLogger(CidsBeanPatchOperationTest.class);
    protected final String operationName;

    protected CidsBeanPatchOperationTest(final String operationName) throws IOException {
        try {
            this.operationName = operationName;
            final String resource = "/de/cismet/cids/jsonpatch/operation/" + operationName + ".json";
            final JsonNode node = JsonLoader.fromResource(resource);
            errors = node.get("errors");
            ops = node.get("ops");
            reader = CidsBeanJsonPatchUtils.getInstance().getCidsBeanMapper().reader().withType(CidsBeanPatchOperation.class);

            LOGGER.info(errors.size() + " error tests and " + ops.size()
                    + " operation tests available for operation '"
                    + operationName + "'");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            throw ex;
        }
    }

    @DataProvider
    public final Iterator<Object[]> getErrors()
            throws Exception {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node : errors) {
            try {
                list.add(new Object[]{
                    node.get("op"),
                    CidsBeanJsonPatchUtils.getInstance().getCidsBeanMapper().treeToValue(node.get("bean"), CidsBean.class),
                    CidsBeanJsonPatchUtils.getInstance().getResourceBundle().getString(node.get("message").textValue())
                });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        }

        LOGGER.info(list.size() + " '" + this.operationName + "' operation error tests loaded");
        return list.iterator();
    }

    @Test(dataProvider = "getErrors")
    public final void errorsAreCorrectlyReported(final JsonNode patch,
            final CidsBean cidsBean, final String message)
            throws IOException, JsonPatchException {
        final CidsBeanPatchOperation op = reader.readValue(patch);
        LOGGER.info("testing '" + this.operationName + "' operation error: " + op.toString());

        try {
            op.apply(cidsBean);
            LOGGER.error("No exception thrown for operation " + op.toString());
            fail("No exception thrown for operation " + op.toString());
        } catch (JsonPatchException e) {
            LOGGER.debug("JsonPatchException thrown: " + e.getMessage());
            assertEquals(e.getMessage(), message);
        }
    }

    @DataProvider
    public final Iterator<Object[]> getOps() {
        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node : ops) {
            try {
                list.add(new Object[]{
                    reader.readValue(node.get("op")),
                    CidsBeanJsonPatchUtils.getInstance().getCidsBeanMapper().treeToValue(node.get("bean"), CidsBean.class),
                    CidsBeanJsonPatchUtils.getInstance().getCidsBeanMapper().treeToValue(node.get("expected"), CidsBean.class)
                });
            } catch (IOException ex) {
                LOGGER.error("cannot deserilaize beans for operation '" + this.operationName + "':"
                        + ex.getMessage(), ex);
            }
        }

        LOGGER.info(list.size() + " '" + this.operationName + "' operation success tests loaded");
        return list.iterator();
    }

    @Test(dataProvider = "getOps")
    public final void operationsYieldExpectedResults(final CidsBeanPatchOperation op,
            final CidsBean cidsBean, final CidsBean expected) throws Exception {
        LOGGER.info("testing '" + this.operationName + "' operation: " + op.toString());

        final CidsBean actual;
        try {
            actual = op.apply(cidsBean);
            final String actualString = actual.toJSONString(true);
            final String expectedString = expected.toJSONString(true);

            assertEquals(actualString, expectedString);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } catch (AssertionError ae) {
            LOGGER.error(op.toString() + " failed");
            throw ae;
        }
    }
}
