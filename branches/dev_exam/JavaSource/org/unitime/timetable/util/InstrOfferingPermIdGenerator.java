/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.util;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * 
 * @author Tomas Muller
 *
 */
public class InstrOfferingPermIdGenerator {
    private static IdentifierGenerator sGenerator = null;
    
    protected static String sSequence = "instr_offr_permid_seq";
    
    public static IdentifierGenerator getGenerator() throws HibernateException {
        try {
            if (sGenerator!=null) return sGenerator;
            UniqueIdGenerator idGen = new UniqueIdGenerator();
            Dialect dialect = (Dialect)Class.forName(InstructionalOfferingDAO.getConfiguration().getProperty("hibernate.dialect")).getConstructor(new Class[]{}).newInstance(new Object[]{});
            Type type = new IntegerType();
            Properties params = new Properties();
            params.put(SequenceGenerator.SEQUENCE, sSequence);
            params.put(PersistentIdentifierGenerator.SCHEMA, _RootDAO.getConfiguration().getProperty("default_schema"));
            idGen.configure(type, params, dialect);
            sGenerator = idGen;
            return sGenerator;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }
}
