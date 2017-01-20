package de.cismet.cids.jsonpatch;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import de.cismet.cids.dynamics.CidsBean;
import java.util.List;
import org.apache.commons.io.IOUtils;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@Test
public class CidsBeanDeserialisationTest {

    protected final static Logger LOGGER = Logger.getLogger(CidsBeanDeserialisationTest.class);

    public CidsBeanDeserialisationTest() {

    }

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void createNewCidsBeanFromJSON() throws Exception {
        try {
            final String resource = IOUtils.toString(this.getClass().getResourceAsStream("resource.json"));
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, resource);

            assertEquals((long) cidsBean.getPrimaryKeyValue(), 11973);

            assertEquals(cidsBean.getProperty("representation[1].type.name"), "original data");

            assertEquals("representation[0].type.name",
                    CidsBeanPatchUtils.getInstance().jsonPointerToCidsBeanPointer(
                            new JsonPointer("/representation/0/type/name")));

            assertEquals(cidsBean.getProperty("representation[1].type.name"),
                    cidsBean.getProperty(CidsBeanPatchUtils.getInstance().jsonPointerToCidsBeanPointer(
                            new JsonPointer("/representation/1/type/name"))));

            assertTrue(((List) cidsBean.getProperty("representation[1].tags")).isEmpty());

            cidsBean.getMetaObject().getAttributeByFieldName("name").getMai().isForeignKey();

            LOGGER.debug(new JsonPointer("/"));
            LOGGER.debug(CidsBeanPatchUtils.getInstance().jsonPointerToCidsBeanPointer(
                    new JsonPointer("/")));

        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    public static void main(String args[]) {
        final CidsBeanDeserialisationTest cidsBeanDeserialisationTest = new CidsBeanDeserialisationTest();
        try {
            cidsBeanDeserialisationTest.createNewCidsBeanFromJSON();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}
