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

package de.cismet.cids.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.collect.Lists;
import de.cismet.cids.dynamics.CidsBean;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import static org.testng.Assert.*;
import org.testng.log4testng.Logger;

@Test
public class CidsBeanPatchTestSuite
{
    protected final JsonNode testNode;
    protected final ObjectReader reader;
    
    protected final static Logger LOGGER = Logger.getLogger(CidsBeanPatchTestSuite.class);
    
    protected final static ObjectMapper OBJECT_MAPPER = CidsBeanPatchUtils.getInstance().getCidsBeanMapper();
    protected final static ResourceBundle RESOURCE_BUNDLE = CidsBeanPatchUtils.getInstance().getResourceBundle();

    public CidsBeanPatchTestSuite()
        throws IOException
    {
        testNode = JsonLoader.fromResource("/de/cismet/cids/jsonpatch/testsuite.json");
        reader = OBJECT_MAPPER.reader().withType(CidsBeanPatch.class);
        
        LOGGER.info("loading test suite with " + testNode.size() + " tests");
    }

    @DataProvider
    public Iterator<Object[]> getTests()
        throws Exception
    {
        final List<Object[]> list = Lists.newArrayList();

        String comment = null;
        boolean valid;
        CidsBeanPatch patch;
        CidsBean bean, expected;

        int i = 0;
        for (final JsonNode element: testNode) {
            try {
            if (!element.has("patch"))
                continue;
            
            comment = element.hasNonNull("comment") ? element.get("comment").textValue() 
                    : (element.hasNonNull("error") ? element.get("error").textValue() : ("patch #" + i));

            patch = reader.readValue((element.get("patch")));
            
            bean = OBJECT_MAPPER.treeToValue(element.get("bean"), CidsBean.class);
            
            expected = element.hasNonNull("expected") ? OBJECT_MAPPER.treeToValue(element.get("expected"), CidsBean.class) : null;

            if (expected == null)
                expected = bean;
            valid = !element.has("error");
            list.add(new Object[]{comment, bean, patch, expected, valid});
            
            } catch (Exception ex) {
                LOGGER.error("cannot deserialize beans for patch #" + i + " ("
                        + comment + "): "
                        + ex.getMessage(), ex);
                throw ex;
            } finally {
                i++;
            }
        }

        return list.iterator();
    }

    @Test(dataProvider = "getTests")
    public void testsFromTestSuitePass(final String comment, final CidsBean bean,
        final CidsBeanPatch patch, final CidsBean expected, final boolean valid)
    {
        try {
            final CidsBean actual = patch.apply(bean);
            if (!valid) {
                fail(comment + " Test was expected to fail!!");
                LOGGER.error(comment + " Test was expected to fail!!");
            }
                
            final String actualString = actual.toJSONString(true);
            final String expectedString = expected.toJSONString(true);
            
            assertEquals(actualString, expectedString);
            
        } catch(AssertionError ae) {
            LOGGER.error(comment + "test failed with: " + ae.getMessage());
            throw ae;
        }
        catch (JsonPatchException ignored) {
            if (valid) {
                LOGGER.error(comment + " Test was expected to succeed!!", ignored);
                fail(comment + " Test was expected to succeed!!");
            } else {
                LOGGER.debug(comment + " Test failed as expected: " + ignored.getMessage());
            }
        } catch(Exception ex) {
            LOGGER.error(comment + " Test faild with unexpected exception!!", ex);
            fail(comment + " Test faild with unexpected exception!!");
        }
    }
}
