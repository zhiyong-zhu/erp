package com.erp.admin;

import liquibase.Liquibase;
import liquibase.database.OfflineConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

class LiquibaseChangelogTest {

    @Test
    void changelogShouldBeValid() throws LiquibaseException {
        ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        try (Liquibase liquibase = new Liquibase(
                "db/changelog-master.xml",
                resourceAccessor,
                new OfflineConnection("offline:postgresql", resourceAccessor)
        )) {
            liquibase.validate();
        }
    }
}
