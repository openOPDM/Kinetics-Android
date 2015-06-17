package org.kineticsfoundation.dao;

import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.dao.model.extension.ExtensionData;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;
import org.kineticsfoundation.dao.model.extension.ExtensionProperty;
import org.kineticsfoundation.dao.model.extension.ExtensionType;

import java.util.*;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

public class DaoUtils {

    private static final Random RANDOM = new Random();

    public static User generateUser() {
        return new User(generateInt(), makeUniqueId("email"), makeUniqueId("name"), makeUniqueId());
    }

    public static List<TestSession> generateTestSessions(int num) {
        List<TestSession> testSessions = newArrayListWithCapacity(num);
        for (int i = 0; i < num; ++i) {
            TestSession testSession = new TestSession(generateInt(), 1.1, makeUniqueId(), makeUniqueId());
            if (i % 2 == 0) {
                testSession.setExtension(Arrays.asList(generateExtensionData(), generateExtensionData()));
            }
            testSessions.add(testSession);
        }
        return testSessions;
    }

    public static List<ExtensionMetaData> generateMetaData(int num) {
        List<ExtensionMetaData> metaDataList = newArrayListWithCapacity(num);
        for (int i = 0; i < num; ++i) {
            ExtensionMetaData metaData;
            if (i % 2 == 0) {
                metaData = new ExtensionMetaData(generateInt(), makeUniqueId(CacheContract.Columns
                        .NAME), ExtensionType.LIST, Arrays.asList(makeUniqueId(), makeUniqueId()));
            } else {
                metaData = new ExtensionMetaData(generateInt(), makeUniqueId(CacheContract.Columns
                        .NAME), ExtensionType.NUMERIC, null);
            }
            metaData.setProperties(EnumSet.allOf(ExtensionProperty.class));

            metaDataList.add(metaData);
        }
        return metaDataList;
    }

    public static String makeUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static String makeUniqueId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString();
    }

    public static int generateInt() {
        return RANDOM.nextInt();
    }

    private static ExtensionData generateExtensionData() {
        return new ExtensionData(makeUniqueId("name"), makeUniqueId(), generateInt());
    }


}
