/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.hibernate.engine.SessionImplementor;
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
