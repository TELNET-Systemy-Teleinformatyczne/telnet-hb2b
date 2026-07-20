/*
 * Copyright (C) 2024 The Holodeck B2B Team, Sander Fieten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.holodeckb2b.storage.metadata;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.dialect.SQLServer2016Dialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.holodeckb2b.interfaces.storage.providers.StorageException;

/**
 * Contains the database configuration used by the default Meta-data Storage Provider of Holodeck B2B. It connects to
 * a SQL Server database configured through the <code>HB2B_DB_URL</code>, <code>HB2B_DB_USER</code> and
 * <code>HB2B_DB_PASSWORD</code> environment variables.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 * @since  7.0.0
 */
final class DatabaseConfiguration implements PersistenceUnitInfo {

	public static final DatabaseConfiguration INSTANCE = new DatabaseConfiguration();
	private static final String DB_URL_ENV = "HB2B_DB_URL";
	private static final String DB_USER_ENV = "HB2B_DB_USER";
	private static final String DB_PASSWORD_ENV = "HB2B_DB_PASSWORD";

    @Override
    public String getPersistenceUnitName() {
        return "hb2b-default-persistency";
    }

    @Override
    public String getPersistenceProviderClassName() {
        return HibernatePersistenceProvider.class.getName();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public List<String> getManagedClassNames() {
        return Arrays.asList("org.holodeckb2b.storage.metadata.jpa.AgreementReference",
                             "org.holodeckb2b.storage.metadata.jpa.CollaborationInfo",
                             "org.holodeckb2b.storage.metadata.jpa.Description",
                             "org.holodeckb2b.storage.metadata.jpa.EbmsError",
                             "org.holodeckb2b.storage.metadata.jpa.ErrorMessage",
                             "org.holodeckb2b.storage.metadata.jpa.MessageUnit",
                             "org.holodeckb2b.storage.metadata.jpa.MessageUnitProcessingState",
                             "org.holodeckb2b.storage.metadata.jpa.PartyId",
                             "org.holodeckb2b.storage.metadata.jpa.PayloadInfo",
                             "org.holodeckb2b.storage.metadata.jpa.Property",
                             "org.holodeckb2b.storage.metadata.jpa.PullRequest",
                             "org.holodeckb2b.storage.metadata.jpa.Receipt",
                             "org.holodeckb2b.storage.metadata.jpa.SchemaReference",
                             "org.holodeckb2b.storage.metadata.jpa.SelectivePullRequest",
                             "org.holodeckb2b.storage.metadata.jpa.Service",
                             "org.holodeckb2b.storage.metadata.jpa.TradingPartner",
                             "org.holodeckb2b.storage.metadata.jpa.UserMessage");
    }

    @Override
    public Properties getProperties() {
        try {
            return createProperties();
        } catch (StorageException missingConfiguration) {
            throw new IllegalStateException(missingConfiguration.getMessage(), missingConfiguration);
        }
    }

    Properties getConfiguredProperties() throws StorageException {
        return createProperties();
    }

    private Properties createProperties() throws StorageException {
        Properties props = new Properties();
        props.put(org.hibernate.cfg.AvailableSettings.DRIVER, com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
        props.put(org.hibernate.cfg.AvailableSettings.URL, requireEnv(DB_URL_ENV));
        props.put(org.hibernate.cfg.AvailableSettings.USER, requireEnv(DB_USER_ENV));
        props.put(org.hibernate.cfg.AvailableSettings.PASS, requireEnv(DB_PASSWORD_ENV));
        props.put(org.hibernate.cfg.AvailableSettings.DIALECT, SQLServer2016Dialect.class);
        props.put(org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO, "update");
        props.put(org.hibernate.cfg.AvailableSettings.SHOW_SQL, false);
        props.put(org.hibernate.cfg.AvailableSettings.QUERY_STARTUP_CHECKING, false);
        props.put(org.hibernate.cfg.AvailableSettings.GENERATE_STATISTICS, false);
        props.put(org.hibernate.cfg.AvailableSettings.USE_REFLECTION_OPTIMIZER, false);
        props.put(org.hibernate.cfg.AvailableSettings.USE_SECOND_LEVEL_CACHE, false);
        props.put(org.hibernate.cfg.AvailableSettings.USE_QUERY_CACHE, false);
        props.put(org.hibernate.cfg.AvailableSettings.USE_STRUCTURED_CACHE, false);
        props.put(org.hibernate.cfg.AvailableSettings.STATEMENT_BATCH_SIZE, 20);

        return props;
    }

    private String requireEnv(final String name) throws StorageException {
        final String value = System.getenv(name);
        if (value == null || value.trim().isEmpty())
            throw new StorageException("Missing required SQL Server metadata database environment variable: " + name);
        return value;
    }

    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return null;
    }

    @Override
    public List<String> getMappingFileNames() {
        return Collections.emptyList();
    }

    @Override
    public List<URL> getJarFileUrls() {
        return Collections.emptyList();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return true;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() {
        return null;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {}

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }

    private DatabaseConfiguration() {}
}
