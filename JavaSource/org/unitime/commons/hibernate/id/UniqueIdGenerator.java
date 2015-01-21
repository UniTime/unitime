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
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.type.Type;

/**
 * @author Tomas Muller
 */
public class UniqueIdGenerator implements IdentifierGenerator, Configurable {
    IdentifierGenerator iGenerator = null;
    private static String sGenClass = null;
    private static String sDefaultSchema = null;
    private static ObjectNameNormalizer sNormalizer = null;
    
    public static void configure(Configuration config) {
        sGenClass = config.getProperty("tmtbl.uniqueid.generator");
        if (sGenClass==null) sGenClass = "org.hibernate.id.SequenceGenerator";
        sDefaultSchema = config.getProperty("default_schema");
        sNormalizer = config.createMappings().getObjectNameNormalizer();
    }
    
    public IdentifierGenerator getGenerator() throws HibernateException {
        if (iGenerator==null) {
            if (sGenClass==null)
                throw new HibernateException("UniqueIdGenerator is not configured, please call configure(Config) first.");
            try {
                iGenerator = (IdentifierGenerator)Class.forName(sGenClass).getConstructor(new Class[]{}).newInstance(new Object[]{});
            } catch (Exception e) {
                throw new HibernateException("Unable to initialize uniqueId generator, reason: "+e.getMessage(),e);
            }
        }
        return iGenerator;
    }
    
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        return getGenerator().generate(session, object);
    }
    
    public void configure(Type type, Properties params, Dialect d) throws MappingException {
        if (getGenerator() instanceof Configurable) {
            if (params.getProperty("schema") == null && sDefaultSchema != null)
                params.setProperty("schema", sDefaultSchema);
            if (params.get("identifier_normalizer") == null && sNormalizer != null)
            	params.put("identifier_normalizer", sNormalizer);
            ((Configurable)getGenerator()).configure(type, params, d);
        }
    }

}
