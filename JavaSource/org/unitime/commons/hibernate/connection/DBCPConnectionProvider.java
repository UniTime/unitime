/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitime.commons.hibernate.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.unitime.timetable.model.base._BaseRootDAO;

/**
 * <p>A connection provider that uses an Apache commons DBCP connection pool.</p>
 * 
 * <p>To use this connection provider set:<br>
 * <code>hibernate.connection.provider_class&nbsp;org.hibernate.connection.DBCPConnectionProvider</code></p>
 *
 * <pre>Supported Hibernate properties:
 *   hibernate.connection.driver_class
 *   hibernate.connection.url
 *   hibernate.connection.username
 *   hibernate.connection.password
 *   hibernate.connection.isolation
 *   hibernate.connection.autocommit
 *   hibernate.connection.pool_size
 *   hibernate.connection (JDBC driver properties)</pre>
 * <br>
 * All DBCP properties are also supported by using the hibernate.dbcp prefix.
 * A complete list can be found on the DBCP configuration page:
 * <a href="http://jakarta.apache.org/commons/dbcp/configuration.html">http://jakarta.apache.org/commons/dbcp/configuration.html</a>.
 * <br>
 * <pre>Example:
 *   hibernate.connection.provider_class org.hibernate.connection.DBCPConnectionProvider
 *   hibernate.connection.driver_class org.hsqldb.jdbcDriver
 *   hibernate.connection.username sa
 *   hibernate.connection.password
 *   hibernate.connection.url jdbc:hsqldb:test
 *   hibernate.connection.pool_size 20
 *   hibernate.dbcp.initialSize 10
 *   hibernate.dbcp.maxWait 3000
 *   hibernate.dbcp.validationQuery select 1 from dual</pre>
 * 
 * <p>More information about configuring/using DBCP can be found on the 
 * <a href="http://jakarta.apache.org/commons/dbcp/">DBCP website</a>.
 * There you will also find the DBCP wiki, mailing lists, issue tracking 
 * and other support facilities</p>  
 * 
 * @see org.hibernate.connection.ConnectionProvider
 * @author Dirk Verbeeck
 */
public class DBCPConnectionProvider implements ConnectionProvider {
	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(DBCPConnectionProvider.class);
    private static final String PREFIX = "hibernate.dbcp.";
    private BasicDataSource ds;

    // Old Environment property for backward-compatibility (property removed in Hibernate3)
    private static final String DBCP_PS_MAXACTIVE = "hibernate.dbcp.ps.maxActive";

    public void configure(Properties props) throws HibernateException {
        try {
            log.debug("Configure DBCPConnectionProvider");
            
            // DBCP properties used to create the BasicDataSource
            Properties dbcpProperties = new Properties();

            // DriverClass & url
            String jdbcDriverClass = props.getProperty(Environment.DRIVER);
            String jdbcUrl = props.getProperty(Environment.URL);
            dbcpProperties.put("driverClassName", jdbcDriverClass);
            dbcpProperties.put("url", jdbcUrl);
            
            // Username / password
            String username = props.getProperty(Environment.USER);
            String password = props.getProperty(Environment.PASS);
            dbcpProperties.put("username", username);
            dbcpProperties.put("password", password);

            // Isolation level
            String isolationLevel = props.getProperty(Environment.ISOLATION);
            if ((isolationLevel != null) && (isolationLevel.trim().length() > 0)) {
                dbcpProperties.put("defaultTransactionIsolation", isolationLevel);
            }
            
            // Turn off autocommit (unless autocommit property is set) 
            String autocommit = props.getProperty(Environment.AUTOCOMMIT);
            if ((autocommit != null) && (autocommit.trim().length() > 0)) {
                dbcpProperties.put("defaultAutoCommit", autocommit);
            } else {
                dbcpProperties.put("defaultAutoCommit", String.valueOf(Boolean.FALSE));
            }

            // Pool size
            String poolSize = props.getProperty(Environment.POOL_SIZE);
            if ((poolSize != null) && (poolSize.trim().length() > 0) 
                && (Integer.parseInt(poolSize) > 0))  {
                dbcpProperties.put("maxActive", poolSize);
            }

            // Copy all "driver" properties into "connectionProperties"
            Properties driverProps = getConnectionProperties(props);
            if (driverProps.size() > 0) {
                StringBuffer connectionProperties = new StringBuffer();
                for (Iterator iter = driverProps.keySet().iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    String value = driverProps.getProperty(key);
                    connectionProperties.append(key).append('=').append(value);
                    if (iter.hasNext()) {
                        connectionProperties.append(';');
                    }
                }
                dbcpProperties.put("connectionProperties", connectionProperties.toString());
            }

            // Copy all DBCP properties removing the prefix
            for (Iterator iter = props.keySet().iterator() ; iter.hasNext() ;) {
                String key = String.valueOf(iter.next());
                if (key.startsWith(PREFIX)) {
                    String property = key.substring(PREFIX.length());
                    String value = props.getProperty(key);
                    dbcpProperties.put(property, value);
                }
            }
            
            // Backward-compatibility
            if (props.getProperty(DBCP_PS_MAXACTIVE) != null) {
                dbcpProperties.put("poolPreparedStatements", String.valueOf(Boolean.TRUE));
                dbcpProperties.put("maxOpenPreparedStatements", props.getProperty(DBCP_PS_MAXACTIVE));
            }
            
            // Let the factory create the pool
            ds = (BasicDataSource) BasicDataSourceFactory.createDataSource(dbcpProperties);
            
            // The BasicDataSource has lazy initialization
            // borrowing a connection will start the DataSource
            // and make sure it is configured correctly.
            Connection conn = ds.getConnection();
            conn.close();

            // Log pool statistics before continuing.
            logStatistics();
        }
        catch (Exception e) {
            String message = "Could not create a DBCP pool";
            log.fatal(message, e);
            if (ds != null) {
                try {
                    ds.close();
                }
                catch (Exception e2) {
                    // ignore
                }
                ds = null;
            }
            throw new HibernateException(message, e);
        }
        log.debug("Configure DBCPConnectionProvider complete");
    }
    
    public static Properties getConnectionProperties(Properties properties) {
    	Iterator iter = properties.keySet().iterator();
    	Properties result = new Properties();
    	while ( iter.hasNext() ) {
    		String prop = (String) iter.next();
    		if ( prop.indexOf(Environment.CONNECTION_PREFIX) > -1 && !SPECIAL_PROPERTIES.contains(prop) ) {
    			result.setProperty( prop.substring( Environment.CONNECTION_PREFIX.length()+1 ), properties.getProperty(prop));
    		}
    	}
    	String userName = properties.getProperty(Environment.USER);
    	if (userName!=null) result.setProperty( "user", userName );
    	return result;
    }
    
    private static final Set SPECIAL_PROPERTIES;
    static {
    	SPECIAL_PROPERTIES = new HashSet();
    	SPECIAL_PROPERTIES.add(Environment.DATASOURCE);
    	SPECIAL_PROPERTIES.add(Environment.URL);
    	SPECIAL_PROPERTIES.add(Environment.CONNECTION_PROVIDER);
    	SPECIAL_PROPERTIES.add(Environment.POOL_SIZE);
    	SPECIAL_PROPERTIES.add(Environment.ISOLATION);
    	SPECIAL_PROPERTIES.add(Environment.DRIVER);
    	SPECIAL_PROPERTIES.add(Environment.USER);
    }

    public Connection getConnection() throws SQLException {
    	if (ds == null)
    		configure(_BaseRootDAO.getConfiguration().getProperties());
        Connection conn = null;
        try {
                conn = ds.getConnection();
        }
        finally {
            logStatistics();
        }
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        try {
            conn.close();
        }
        finally {
            logStatistics();
        }
    }

    public void close() throws HibernateException {
        log.debug("Close DBCPConnectionProvider");
        logStatistics();
        try {
            if (ds != null) {
                ds.close();
                    ds = null;
            }
            else {
                log.warn("Cannot close DBCP pool (not initialized)");
            }
        }
        catch (Exception e) {
            throw new HibernateException("Could not close DBCP pool", e);
        }
        log.debug("Close DBCPConnectionProvider complete");
    }
    
    protected void logStatistics() {
    	if (log.isDebugEnabled()) {
            log.debug("active: " + ds.getNumActive() + " (max: " + ds.getMaxActive() + ")   "
                    + "idle: " + ds.getNumIdle() + "(max: " + ds.getMaxIdle() + ")");
        }
    }
    
    public boolean supportsAggressiveRelease() {
    	return false;
    }

	public boolean isUnwrappableAs(Class arg0) {
		return false;
	}

	public <T> T unwrap(Class<T> arg0) {
		return null;
	}
}