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
package org.unitime.commons.hibernate.id;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * @author Tomas Muller
 */
public class UniqueIdGenerator implements IdentifierGenerator {
    IdentifierGenerator iGenerator = null;
    private static String sGenClass = null;
    private static String sDefaultSchema = null;
    
    public static void configure(LoadedConfig config) {
        sGenClass = (String)config.getConfigurationValues().get("tmtbl.uniqueid.generator");
        if (sGenClass==null) sGenClass = "org.hibernate.id.enhanced.SequenceStyleGenerator";
        sDefaultSchema = (String)config.getConfigurationValues().get("default_schema");
    }
    
    public IdentifierGenerator getGenerator() throws HibernateException {
        if (iGenerator==null) {
        	if (sGenClass==null) {
                throw new HibernateException("UniqueIdGenerator is not configured, please call configure(Config) first.");
        	} else if ("org.hibernate.id.TableHiLoGenerator".equals(sGenClass)) {
        		iGenerator = new TableGenerator();
        	} else if ("org.hibernate.id.SequenceGenerator".equals(sGenClass)) {
        		iGenerator = new SequenceStyleGenerator();
        	} else { 
                try {
                    iGenerator = (IdentifierGenerator)Class.forName(sGenClass).getConstructor(new Class[]{}).newInstance(new Object[]{});
                } catch (Exception e) {
                    throw new HibernateException("Unable to initialize uniqueId generator, reason: "+e.getMessage(),e);
                }
        	}
        }
        return iGenerator;
    }
    
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return getGenerator().generate(session, object);
    }
    
    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
    	if (params.getProperty("schema") == null && sDefaultSchema != null)
            params.setProperty("schema", sDefaultSchema);
    	if (getGenerator() instanceof TableGenerator) {
    		params.setProperty("segment_value", "default");//params.getProperty("sequence_name", params.getProperty("sequence")));
    		params.setProperty("optimizer", "legacy-hilo");
    		params.setProperty("increment_size", "32767");
    		params.setProperty("value_column_name", "next_hi");
    		params.setProperty("table_name", "hibernate_unique_key");
    		
    	}
        getGenerator().configure(type, params, serviceRegistry);
    }
    
    @Override
	public void initialize(SqlStringGenerationContext context) {
    	getGenerator().initialize(context);
    }
    
    @Override
	public void registerExportables(Database database) {
    	getGenerator().registerExportables(database);
    }
}
