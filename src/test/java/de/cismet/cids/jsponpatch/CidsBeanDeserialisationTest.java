package de.cismet.cids.jsponpatch;

import de.cismet.cids.dynamics.CidsBean;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.openide.util.Exceptions;
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
            assertEquals((long)cidsBean.getPrimaryKeyValue(), 11973);
            assertEquals(cidsBean.getProperty("representation[1].type.name"), "original data");
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
        }
    }
}
