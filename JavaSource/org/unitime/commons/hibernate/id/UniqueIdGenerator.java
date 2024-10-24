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

import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * @author Tomas Muller
 */
public class UniqueIdGenerator implements BeforeExecutionGenerator, AnnotationBasedGenerator<org.unitime.commons.annotations.UniqueIdGenerator> {
	private static final long serialVersionUID = -2217592981811005640L;
	IdentifierGenerator iGenerator = null;
    private static String sGenClass = null;
    private static String sDefaultSchema = null;
    private boolean iInitialized = false;
    
    public static void configure(LoadedConfig config) {
        sGenClass = (String)config.getConfigurationValues().get("tmtbl.uniqueid.generator");
        if (sGenClass==null) sGenClass = "org.hibernate.id.enhanced.SequenceStyleGenerator";
        sDefaultSchema = (String)config.getConfigurationValues().get("hibernate.default_schema");
    }
    
    @Override
    public void initialize(org.unitime.commons.annotations.UniqueIdGenerator config, Member idMember, GeneratorCreationContext context) {
    	Properties params = new Properties();
    	params.put(SequenceStyleGenerator.SEQUENCE_PARAM, config.sequence());
    	configure(context.getProperty().getType(), params, context.getServiceRegistry());
    	registerExportables(context.getDatabase());
    }
    
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
    	if (params.getProperty(PersistentIdentifierGenerator.SCHEMA) == null && sDefaultSchema != null)
            params.setProperty(PersistentIdentifierGenerator.SCHEMA, sDefaultSchema);
    	if (getGenerator() instanceof TableGenerator) {
    		params.setProperty(TableGenerator.SEGMENT_VALUE_PARAM, "default");
    		params.setProperty(TableGenerator.OPT_PARAM, "legacy-hilo");
    		params.setProperty(TableGenerator.INCREMENT_PARAM, "32767");
    		params.setProperty(TableGenerator.VALUE_COLUMN_PARAM, "next_hi");
    		params.setProperty(TableGenerator.TABLE_PARAM, "hibernate_unique_key");
    	} else if (getGenerator() instanceof SequenceStyleGenerator) {
    		params.setProperty(SequenceStyleGenerator.INCREMENT_PARAM, "1");
    	}
        getGenerator().configure(type, params, serviceRegistry);
    }
    
    public void registerExportables(Database database) {
    	getGenerator().registerExportables(database);
    }
    
    public synchronized void initialize(SqlStringGenerationContext context) {
    	if (!iInitialized)
    		getGenerator().initialize(context);
    	iInitialized = true;
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
	public EnumSet<EventType> getEventTypes() {
		return INSERT_ONLY;
	}

	public Object generate(SharedSessionContractImplementor session, Object owner) {
		initialize(session.getSessionFactory().getSqlStringGenerationContext());
		return getGenerator().generate(session, owner);
	}

	@Override
	public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
		return generate(session, owner);
	}
}
