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
