/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.dataexchange;

import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class PermissionsImport extends BaseImport {

	@Override
	public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("permissions")) {
        	throw new Exception("Given XML file is not a Permissions load file.");
        }
		try {
			beginTransaction();
			
			for (Iterator i = root.elementIterator("role"); i.hasNext(); ) {
                Element r = (Element) i.next();
                String ref = r.attributeValue("reference");
                
                Roles role = (Roles)getHibSession().createQuery("from Roles where reference = :ref").setString("ref", ref).uniqueResult();
                
                if (role == null) {
                	role = new Roles();
                	role.setReference(ref);
                	role.setRights(new HashSet<String>());
                }
                
                role.setAbbv(r.attributeValue("name"));
                role.setManager("true".equals(r.attributeValue("manager", "true")));
                role.setEnabled("true".equals(r.attributeValue("enabled", "true")));
                role.setInstructor("true".equals(r.attributeValue("instructor", "false")));
                
                role.getRights().clear();
                
                for (Iterator j = r.elementIterator("right"); j.hasNext(); ) {
                	Element p = (Element) j.next();
                	try {
                		role.getRights().add(Right.valueOf(p.getText()).name());
                	} catch (IllegalArgumentException e) {
                	} catch (NullPointerException e) {
                	}
                }
                
                getHibSession().saveOrUpdate(role);
			}
			
			
        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}

}
