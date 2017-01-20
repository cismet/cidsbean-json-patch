package de.cismet.cids.jsonpatch;

import Sirius.server.middleware.types.MetaClass;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cismet.cids.utils.MetaClassCacheService;
import de.cismet.cidsx.server.api.types.CidsClass;
import de.cismet.cidsx.server.api.types.legacy.CidsClassFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import org.testng.log4testng.Logger;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@org.openide.util.lookup.ServiceProvider(
        service = MetaClassCacheService.class,
        supersedes = {"Sirius.server.middleware.impls.domainserver.DomainServerMetaClassService"})
public class OfflineMetaClassCacheService implements MetaClassCacheService {

    protected final static HashMap<Integer, MetaClass> allClassesById = new HashMap<Integer, MetaClass>();
    protected final static HashMap<String, MetaClass> allClassesByTableName = new HashMap<String, MetaClass>();

    protected final static Logger LOGGER = Logger.getLogger(OfflineMetaClassCacheService.class);
    protected static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    public OfflineMetaClassCacheService() throws Exception {

        if (allClassesById.isEmpty() && allClassesByTableName.isEmpty()) {
            LOGGER.info("loading meta classes");
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resources;
            try {
                resources = classLoader.getResource("de/cismet/cids/jsonpatch/classes/");
            } catch (Exception ex) {
                LOGGER.error("could not locate meta class json files: " + ex.getMessage(), ex);
                throw ex;
            }

            final Scanner scanner = new Scanner((InputStream) resources.getContent()).useDelimiter("\\n");
            while (scanner.hasNext()) {
                final String jsonFile = "de/cismet/cids/jsonpatch/classes/" + scanner.next();
                LOGGER.info("loading cids class from json file " + jsonFile);
                try {

                    final CidsClass cidsClass = MAPPER.readValue(
                            new BufferedReader(
                                    new InputStreamReader(classLoader.getResourceAsStream(jsonFile))),
                            CidsClass.class);
                    LOGGER.debug(cidsClass.getKey() + " deserialized");
                    final MetaClass metaClass = CidsClassFactory.getFactory().legacyCidsClassFromRestCidsClass(cidsClass);
                    this.allClassesById.put(metaClass.getId(), metaClass);
                    this.allClassesByTableName.put(metaClass.getTableName(), metaClass);

                } catch (Exception ex) {
                    LOGGER.error("could not deserialize cids class from url " + jsonFile, ex);
                    throw ex;
                }
            }

        } else {
            LOGGER.info("meta classes already loaded");
        }
    }

    @Override
    public MetaClass getMetaClass(final String domain, final String tableName) {
        return allClassesByTableName.get(tableName);
    }

    @Override
    public MetaClass getMetaClass(final String domain, final int classId) {
        return allClassesById.get(classId);
    }

    @Override
    public HashMap<String, MetaClass> getAllClasses(final String domain) {

        // this is madness!
        final HashMap<String, MetaClass> allClasses = new HashMap<String, MetaClass>();
        for (Integer classId : allClassesById.keySet()) {
            final String classKey = domain + classId;
            allClasses.put(classKey, allClassesById.get(classId));
        }

        return allClasses;
    }

}
