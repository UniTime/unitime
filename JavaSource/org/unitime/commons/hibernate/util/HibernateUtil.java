/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.commons.hibernate.util;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.cfgxml.spi.MappingReference;
import org.hibernate.boot.cfgxml.spi.MappingReference.Type;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.sqm.function.NamedSqmFunctionDescriptor;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.ServiceBinding;
import org.hibernate.sql.ast.SqlAstNodeRenderingMode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.unitime.commons.LocalContext;
import org.unitime.commons.hibernate.connection.LoggingConnectionProvider;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.dao._RootDAO;

import jakarta.persistence.Entity;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

/**
 * @author Tomas Muller
 */
public class HibernateUtil {
    private static Log sLog = LogFactory.getLog(HibernateUtil.class);
	protected static HibernateContext sContext;
	protected static ThreadLocal<Session> sSessions;
    
    public static void configureHibernate(String connectionUrl) throws Exception {
        Properties properties = ApplicationProperties.getProperties();
        properties.setProperty("connection.url", connectionUrl);
        configureHibernate(properties);
    }
	
    public static String getProperty(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value!=null) {
            sLog.debug("   -- " + name + "=" + value);
        	return value;
        }
        sLog.debug("   -- using application properties for " + name);
        value = ApplicationProperties.getProperty(name);
        sLog.debug("     -- " + name + "=" + value);
        return value;
    }
    
    public static void fixSchemaInFormulas(Metadata meta, String schema, Class dialect) throws ClassNotFoundException {
    	for (PersistentClass pc: meta.getEntityBindings()) {
    		for (Property p : pc.getProperties()) {
    			for (Selectable c: p.getSelectables()) {
                    if (c instanceof Formula) {
                        Formula f = (Formula)c;
                        boolean updated = false;
                        if (schema != null && f.getFormula() != null && f.getFormula().indexOf("%SCHEMA%")>=0) {
                            f.setFormula(f.getFormula().replaceAll("%SCHEMA%", schema));
                            sLog.debug("Schema updated in "+pc.getClassName()+"."+p.getName()+" to "+f.getFormula());
                        }
                        if (f.getFormula()!=null && (f.getFormula().indexOf("%TRUE%")>=0 || f.getFormula().indexOf("%FALSE%")>=0)) {
                        	if (isPostgress(dialect)) {
                        		f.setFormula(f.getFormula().replaceAll("%TRUE%", "'t'").replaceAll("%FALSE%", "'f'"));
                        	} else {
                        		f.setFormula(f.getFormula().replaceAll("%TRUE%", "1").replaceAll("%FALSE%", "0"));
                        	}
                        }
                        if (updated)
                        	sLog.debug("Schema updated in "+pc.getClassName()+"."+p.getName()+" to "+f.getFormula());
                    }
                }
            }
        }
    }
    
    public static HibernateContext configureHibernateFromProperties(Properties properties) throws ClassNotFoundException {
		if (properties == null)
			properties = ApplicationProperties.getProperties();
		
		sLog.info("Connecting to "+getProperty(properties, "connection.url"));
		ClassLoader classLoader = HibernateUtil.class.getClassLoader();
		sLog.debug("  -- class loader retrieved");
		
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
		LoadedConfig config = registryBuilder.getConfigLoader().loadConfigXmlUrl(classLoader.getResource("hibernate.cfg.xml"));
		
        String dialect = ApplicationProperty.DatabaseDialect.value();
        if ("org.hibernate.dialect.MySQLInnoDBDialect".equals(dialect))
        	dialect = MySQLDialect.class.getName();
        else if ("org.hibernate.dialect.Oracle10gDialect".equals(dialect))
        	dialect = OracleDialect.class.getName();
        if (dialect!=null) {
        	config.getConfigurationValues().put("dialect", dialect);
        	config.getConfigurationValues().put("hibernate.dialect", dialect);
        }

        String idgen = getProperty(properties, "tmtbl.uniqueid.generator");
        if (idgen!=null)
        	config.getConfigurationValues().put("tmtbl.uniqueid.generator", idgen);

        // Remove second level cache
        config.getConfigurationValues().put("hibernate.cache.use_second_level_cache", "false");
        config.getConfigurationValues().put("hibernate.cache.use_query_cache", "false");
        config.getConfigurationValues().remove("hibernate.cache.region.factory_class");
        config.getConfigurationValues().remove("hibernate.cache.infinispan.cfg");
        config.getConfigurationValues().put("cache.use_second_level_cache", "false");
        config.getConfigurationValues().put("cache.use_query_cache", "false");
        config.getConfigurationValues().remove("cache.region.factory_class");
        config.getConfigurationValues().remove("cache.infinispan.cfg");
        

        for (Enumeration e=properties.propertyNames();e.hasMoreElements();) {
        	String name = (String)e.nextElement();
            if (name.startsWith("hibernate.") || name.startsWith("tmtbl.hibernate.")) {
				String value = ApplicationProperties.getProperty(name);
                if ("NULL".equals(value))
                	config.getConfigurationValues().remove(name);
                else
                	config.getConfigurationValues().put(name, value);
                if (!name.equals("connection.password"))
                    sLog.debug("  -- set "+name+": "+value);
                else
                    sLog.debug("  -- set "+name+": *****");
            }
            if (name.startsWith("connection.")) {
				String value = ApplicationProperties.getProperty(name);
                if ("NULL".equals(value)) {
                	config.getConfigurationValues().remove(name);
                	config.getConfigurationValues().remove("hibernate." + name);
                } else {
                	config.getConfigurationValues().put(name, value);
                	config.getConfigurationValues().put("hibernate." + name, value);
                }
                if (!name.equals("connection.password"))
                    sLog.debug("  -- set "+name+": "+value);
                else
                    sLog.debug("  -- set "+name+": *****");
            }
        }
        
        String default_schema = getProperty(properties, "default_schema");
        if (default_schema != null)
        	config.getConfigurationValues().put("default_schema", default_schema);
        else
        	default_schema = "timetable";
        
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        for (MappingReference mr: new ArrayList<MappingReference>(config.getMappingReferences())) {
        	if (mr.getType() == Type.PACKAGE)
        		for (BeanDefinition bd : scanner.findCandidateComponents(mr.getReference())) {
                	config.getMappingReferences().add(new MappingReference(Type.CLASS, bd.getBeanClassName()));
                }	
        }
        
        UniqueIdGenerator.configure(config);
        
        registryBuilder.configure(config);
        
        ServiceRegistry registry = registryBuilder.build();
        
        if (ApplicationProperty.ConnectionLogging.isTrue()) {
        	ConnectionProvider cp = registry.getService(ConnectionProvider.class);
        	if (cp != null) {
        		ServiceBinding<ConnectionProvider> scp = ((StandardServiceRegistryImpl)registry).locateServiceBinding(ConnectionProvider.class);
            	if (scp != null)
            		scp.setService(new LoggingConnectionProvider(registry.getService(ConnectionProvider.class)));
        	}
        }

        MetadataBuilder metaBuild = new MetadataSources(registry).getMetadataBuilder();
        Class d = Class.forName((String)config.getConfigurationValues().get("dialect"));
        addOperations(metaBuild, d);
        
        Metadata meta = metaBuild.build();
        
        fixSchemaInFormulas(meta, default_schema, d);
        
    	return new HibernateContext(config, registry, meta, meta.buildSessionFactory());
    }
    
	public static void configureHibernate(Properties properties) throws NamingException, ClassNotFoundException {
		if (sContext != null) {
			sContext.close();
			sContext = null;
		}
		
		if (!NamingManager.hasInitialContextFactoryBuilder())
			NamingManager.setInitialContextFactoryBuilder(new LocalContext(null));

		sContext = configureHibernateFromProperties(properties);
        
        DatabaseUpdate.update();
    }
    
    public static void closeHibernate() {
		if (sContext != null) {
			sContext.close();
			sContext=null;
		}
	}
    
    public static HibernateContext configureHibernateFromRootDAO() throws ClassNotFoundException {
    	sLog.info("Connecting to "+ApplicationProperty.ConnectionUrl.value());
		ClassLoader classLoader = HibernateUtil.class.getClassLoader();

        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
        LoadedConfig config = registryBuilder.getConfigLoader().loadConfigXmlUrl(classLoader.getResource("hibernate.cfg.xml"));
        
        String dialect = ApplicationProperty.DatabaseDialect.value();
        if ("org.hibernate.dialect.MySQLInnoDBDialect".equals(dialect))
        	dialect = MySQLDialect.class.getName();
        else if ("org.hibernate.dialect.Oracle10gDialect".equals(dialect))
        	dialect = OracleDialect.class.getName();
        if (dialect!=null) {
        	config.getConfigurationValues().put("dialect", dialect);
        	config.getConfigurationValues().put("hibernate.dialect", dialect);
        }

        String idgen = ApplicationProperty.DatabaseUniqueIdGenerator.value();
        if (idgen!=null)
        	config.getConfigurationValues().put("tmtbl.uniqueid.generator", idgen);

        if (ApplicationProperty.HibernateCacheConfiguration.value() != null)
        	config.getConfigurationValues().put("hibernate.cache.infinispan.cfg", ApplicationProperty.HibernateCacheConfiguration.value());
        else if (ApplicationProperty.HibernateClusterEnabled.isTrue())
        	config.getConfigurationValues().put("hibernate.cache.infinispan.cfg", "infinispan-cluster.xml");
        else if (ApplicationProperty.HibernateClusterEnabled.isFalse())
        	config.getConfigurationValues().put("hibernate.cache.infinispan.cfg", "infinispan-local.xml");
        config.getConfigurationValues().put("hibernate.cache.infinispan.jgroups_cfg", ApplicationProperty.HibernateClusterConfiguration.value());

        for (Enumeration e=ApplicationProperties.getProperties().propertyNames();e.hasMoreElements();) {
            String name = (String)e.nextElement();
            if (name.startsWith("hibernate.") || name.startsWith("tmtbl.hibernate.")) {
				String value = ApplicationProperties.getProperty(name);
                if ("NULL".equals(value))
                	config.getConfigurationValues().remove(name);
                else
                	config.getConfigurationValues().put(name, value);
                if (!name.equals("connection.password"))
                    sLog.debug("  -- set "+name+": "+value);
                else
                    sLog.debug("  -- set "+name+": *****");
            }
            if (name.startsWith("connection.")) {
				String value = ApplicationProperties.getProperty(name);
                if ("NULL".equals(value)) {
                	config.getConfigurationValues().remove(name);
                	config.getConfigurationValues().remove("hibernate." + name);
                } else {
                	config.getConfigurationValues().put(name, value);
                	config.getConfigurationValues().put("hibernate." + name, value);
                }
                if (!name.equals("connection.password"))
                    sLog.debug("  -- set "+name+": "+value);
                else
                    sLog.debug("  -- set "+name+": *****");
            }
        }

        String default_schema = ApplicationProperty.DatabaseSchema.value();
        if (default_schema != null)
        	config.getConfigurationValues().put("default_schema", default_schema);
        
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        for (MappingReference mr: new ArrayList<MappingReference>(config.getMappingReferences())) {
        	if (mr.getType() == Type.PACKAGE)
        		for (BeanDefinition bd : scanner.findCandidateComponents(mr.getReference())) {
                	config.getMappingReferences().add(new MappingReference(Type.CLASS, bd.getBeanClassName()));
                }	
        }
        
        UniqueIdGenerator.configure(config);
        
        registryBuilder.configure(config);
        
        ServiceRegistry registry = registryBuilder.build();
        
        if (ApplicationProperty.ConnectionLogging.isTrue()) {
        	ConnectionProvider cp = registry.getService(ConnectionProvider.class);
        	if (cp != null) {
        		ServiceBinding<ConnectionProvider> scp = ((StandardServiceRegistryImpl)registry).locateServiceBinding(ConnectionProvider.class);
            	if (scp != null)
            		scp.setService(new LoggingConnectionProvider(registry.getService(ConnectionProvider.class)));
        	}
        }

        
        MetadataBuilder metaBuild = new MetadataSources(registry).getMetadataBuilder();
        Class d = Class.forName((String)config.getConfigurationValues().get("dialect"));
        addOperations(metaBuild, d);
        
        Metadata meta = metaBuild.build();
        
        fixSchemaInFormulas(meta, default_schema, d);
        
        return new HibernateContext(config, registry, meta, meta.buildSessionFactory());
    }
    
    private static String sConnectionUrl = null;
    
    public static String getConnectionUrl() {
        if (sConnectionUrl==null) {
            try {
                SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
                Connection connection = session.getJdbcConnectionAccess().obtainConnection();
                sConnectionUrl = connection.getMetaData().getURL();
                session.getJdbcConnectionAccess().releaseConnection(connection);
            } catch (Exception e) {
                sLog.error("Unable to get connection string, reason: "+e.getMessage(),e);
            }
        }
        return sConnectionUrl;
    }

    public static String getDatabaseName() {
    	String schema = (String)getHibernateContext().getConfig().getConfigurationValues().get("default_schema");
        String url = getConnectionUrl();
        if (url==null) return "N/A";
        if (url.startsWith("jdbc:oracle:")) {
            return schema+"@"+url.substring(1+url.lastIndexOf(':'));
        }
        return schema;
    }
    
    public static void clearCache() {
        clearCache(null, true);
    }
    
    public static void clearCache(Class persistentClass) {
        clearCache(persistentClass, false);
    }

    public static void clearCache(Class persistentClass, boolean evictQueries) {
        _RootDAO dao = new _RootDAO();
        org.hibernate.Session hibSession = dao.getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory();
        if (persistentClass==null) {
            hibSessionFactory.getCache().evictEntityData();
            hibSessionFactory.getCache().evictCollectionData();
        } else {
            hibSessionFactory.getCache().evictEntityData(persistentClass);
            EntityType et = null;
            try {
            	et = hibSession.getMetamodel().entity(persistentClass);
            } catch (IllegalArgumentException e) {}
            if (et != null) {
            	for (Attribute a: (Set<Attribute>)et.getAttributes()) {
            		if (a.isCollection())
            			try {
                            hibSessionFactory.getCache().evictCollectionData(persistentClass.getClass().getName()+"."+a.getName());
                        } catch (MappingException e) {}
            	}
            }
        }
        if (evictQueries) {
            hibSessionFactory.getCache().evictQueryRegions();
            hibSessionFactory.getCache().evictDefaultQueryRegion();
        }
    }
    
    public static Class<?> getDialect() {
    	try {
    		return Class.forName((String)getHibernateContext().getConfig().getConfigurationValues().get("dialect"));
    	} catch (ClassNotFoundException e) {
    		return null;
    	}
    }
    
    public static boolean isMySQL() {
    	return MySQLDialect.class.isAssignableFrom(getDialect());
    }
    
    public static boolean isOracle() {
    	return OracleDialect.class.isAssignableFrom(getDialect());
    }
    
    public static boolean isPostgress() {
    	return PostgreSQLDialect.class.isAssignableFrom(getDialect());
    }
    
    public static boolean isPostgress(Class dialect) {
    	return PostgreSQLDialect.class.isAssignableFrom(dialect);
    }
    
    public static String addDate(String dateSQL, String incrementSQL) {
        if (isMySQL() || isPostgress())
            return "adddate("+dateSQL+","+incrementSQL+")";
        else
        	return dateSQL + " + numtodsinterval(" + incrementSQL + ", 'day')";
//            return "(" + dateSQL+(incrementSQL.startsWith("+")||incrementSQL.startsWith("-")?"":"+")+incrementSQL + ")";
    }
    
    public static String dayOfWeek(String field) {
    	if (isOracle())
    		return "weekday(" + field + ")";
    	else if (isPostgress())
    		return "extract(isodow from " + field + ") - 1";
    	else
    		return "weekday(" + field + ")";
    }
    
    public static String date(Date date) {
    	if (isOracle() || isPostgress())
    		return "to_date('" + new SimpleDateFormat("yyyy-MM-dd").format(date) + "', 'YYYY-MM-DD')";
    	else
    		return "str_to_date('" + new SimpleDateFormat("yyyy-MM-dd").format(date) + "', '%Y-%m-%d')";
    }
    
    public static void addOperations(MetadataBuilder builder, Class dialect) {
    	if (PostgreSQLDialect.class.isAssignableFrom(dialect)) {
    		builder.applySqlFunction("adddate", PostgreSQLAddDateFunction.INSTANCE);
    		builder.applySqlFunction("days", PostgreSQLDaysFunction.INSTANCE);
        } else if (OracleDialect.class.isAssignableFrom(dialect)) {
        	builder.applySqlFunction("weekday", OracleWeekdayFunction.INSTANCE);
        	builder.applySqlFunction("days", OracleDaysFunction.INSTANCE);
        } else if (MySQLDialect.class.isAssignableFrom(dialect)) {
        	builder.applySqlFunction("days", MySQLDaysFunction.INSTANCE);
        }
    }
    
    public static class PostgreSQLAddDateFunction extends NamedSqmFunctionDescriptor {
    	public static final PostgreSQLAddDateFunction INSTANCE = new PostgreSQLAddDateFunction();
    	public PostgreSQLAddDateFunction() {
    		super("adddate", false, StandardArgumentsValidators.exactly(2), null);
    	}
    	
    	@Override
    	public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> translator) {
    		// ?1 + (?2) * interval '1 day'
    		translator.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(" + (");
    		translator.render(sqlAstArguments.get(1), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(") * interval '1 day'");
    	}
    }
    
    public static class OracleWeekdayFunction extends NamedSqmFunctionDescriptor {
    	public static final OracleWeekdayFunction INSTANCE = new OracleWeekdayFunction();
    	public OracleWeekdayFunction() {
    		super("weekday", false, StandardArgumentsValidators.exactly(1), new FunctionReturnTypeResolver() {
    			@Override
				public ReturnableType<?> resolveFunctionReturnType(ReturnableType<?> impliedType, List<? extends SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
					return typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.INTEGER);
				}
				@Override
				public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
					return impliedTypeAccess.get();
				}
			});
    	}
    	
    	@Override
    	public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> translator) {
    		// (trunc(?) - trunc(?, 'IW'));
    		sqlAppender.appendSql("(trunc(");
    		translator.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(") - trunc(");
    		translator.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(", 'IW'))");
    	}
    }
    
    public static class OracleDaysFunction extends NamedSqmFunctionDescriptor {
    	public static final OracleDaysFunction INSTANCE = new OracleDaysFunction();
    	public OracleDaysFunction() {
    		super("days", false, StandardArgumentsValidators.exactly(2), new FunctionReturnTypeResolver() {
    			@Override
				public ReturnableType<?> resolveFunctionReturnType(ReturnableType<?> impliedType, List<? extends SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
					return typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.INTEGER);
				}
				@Override
				public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
					return impliedTypeAccess.get();
				}
			});
    	}
    	
    	@Override
    	public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> translator) {
    		// (trunc(?) - trunc(?))
    		sqlAppender.appendSql("(trunc(");
    		translator.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(") - trunc(");
    		translator.render(sqlAstArguments.get(1), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql("))");
    	}
    }
    
    public static class MySQLDaysFunction extends NamedSqmFunctionDescriptor {
    	public static final MySQLDaysFunction INSTANCE = new MySQLDaysFunction();
    	public MySQLDaysFunction() {
    		super("days", false, StandardArgumentsValidators.exactly(2), new FunctionReturnTypeResolver() {
    			@Override
				public ReturnableType<?> resolveFunctionReturnType(ReturnableType<?> impliedType, List<? extends SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
					return typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.INTEGER);
				}
				@Override
				public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
					return impliedTypeAccess.get();
				}
			});
    	}
    	
    	@Override
    	public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> translator) {
    		// datediff(?, ?)
    		sqlAppender.appendSql("datediff(");
    		translator.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(", ");
    		translator.render(sqlAstArguments.get(1), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(")");
    	}
    }
    
    public static class PostgreSQLDaysFunction extends NamedSqmFunctionDescriptor {
    	public static final PostgreSQLDaysFunction INSTANCE = new PostgreSQLDaysFunction();
    	public PostgreSQLDaysFunction() {
    		super("days", false, StandardArgumentsValidators.exactly(2), new FunctionReturnTypeResolver() {
    			@Override
				public ReturnableType<?> resolveFunctionReturnType(ReturnableType<?> impliedType, List<? extends SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
					return typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.INTEGER);
				}
				@Override
				public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
					return impliedTypeAccess.get();
				}
			});
    	}
    	
    	@Override
    	public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> translator) {
    		// (date(?) - date(?))
    		sqlAppender.appendSql("(date(");
    		translator.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql(") - date(");
    		translator.render(sqlAstArguments.get(1), SqlAstNodeRenderingMode.DEFAULT);
    		sqlAppender.appendSql("))");
    	}
    }
    
    public static String escapeSql(String str) {
    	if (str == null) return null;
    	return StringUtils.replace(str, "'", "''");
    }
    
    /**
	 * Configure the session factory by reading hibernate config file
	 */
	public static void initialize() throws ClassNotFoundException {
		if (sContext != null) return;
		sContext = configureHibernateFromRootDAO();
		DatabaseUpdate.update();
	}
	
	public static void reconnect(Properties properties) throws ClassNotFoundException {
		sLog.info("Reconnecting database ...");
		HibernateContext oldContect = sContext;
		sLog.info("Configuring new Hibernate context ...");
		HibernateContext newContext = (properties != null ? configureHibernateFromProperties(properties) : configureHibernateFromRootDAO());
		sContext = newContext;
		sLog.info("Closing old Hibernate context ...");
		oldContect.close();
	}
	
	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public static Session getSession() {
		return getSession(false);
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public static Session createNewSession() {
		return getSession(true);
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	private static Session getSession(boolean createNew) {
		if (createNew) {
			return sContext.getSessionFactory().openSession();
		} else {
			if (sSessions == null)
				sSessions = new ThreadLocal<Session>();
			Session session = sSessions.get();
			if (session == null || !session.isOpen()) {
				session = sContext.getSessionFactory().openSession();
				// session.beginTransaction();
				sSessions.set(session);
			}
			return session;
		}
	}
	
	/**
	 * Get current thread opened session, if there is any
	 */
	public static Session getCurrentThreadSession() {
		if (sSessions != null) {
			Session session = sSessions.get();
			if (session != null) return session;
		}
		return null;
	}

	/**
	 * Close all sessions for the current thread
	 */
	public static boolean closeCurrentThreadSessions() {
		return closeCurrentThreadSessions(true);
	}
	
	/**
	 * Rollback all sessions for the current thread
	 */
	public static boolean rollbackCurrentThreadSessions() {
		return closeCurrentThreadSessions(false);
	}
	
	private static boolean closeCurrentThreadSessions(boolean commit) {
		boolean ret = false;
		if (sSessions != null) {
			Session session = sSessions.get();
			if (session != null && session.isOpen()) {
				if (session.getTransaction() != null && session.getTransaction().isActive()) {
					if (commit)
						session.getTransaction().commit();
					else
						session.getTransaction().rollback();
				}
				session.close();
				ret = true;
			}
			sSessions.remove();
		}
		return ret;
	}
	
	/**
	 * @return Returns true if configured
	 */
	public static boolean isConfigured() {
		return sContext != null && sContext.getSessionFactory() != null;
	}	 	 
	 
	/**
	 * @return Returns the configuration.
	 */
	public static HibernateContext getHibernateContext() {
		return sContext;
	}
	
	public static HibernateContext getConfiguration() {
		return sContext;
	}
}
