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

import java.util.Date;
import java.util.Properties;


import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.criterion.Order;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class PermissionsExport extends BaseExport {

    public void saveXml(Document document, Session session, Properties parameters) throws Exception {
    	try {
    		beginTransaction();

            Element root = document.addElement("permissions");
            root.addAttribute("created", new Date().toString());

            document.addDocType("curricula", "-//UniTime//DTD University Course Timetabling/EN", "http://www.unitime.org/interface/Permissions.dtd");
            
    		for (Roles role: RolesDAO.getInstance().findAll(getHibSession(), Order.asc("abbv"))) {
    			Element r = root.addElement("role");
    			r.addAttribute("reference", role.getReference());
    			r.addAttribute("name", role.getAbbv());
    			r.addAttribute("manager", role.isManager() ? "true" : "false");
    			r.addAttribute("enabled", role.isEnabled() ? "true" : "false");
    			r.addAttribute("instructor", role.isInstructor() ? "true" : "false");
    			for (Right right: Right.values()) {
    				if (role.hasRight(right))
    					r.addElement("right").setText(right.name());
    			}
    		}

            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    public static void main(String[] args) {
        try {
            if (args.length==0)
                args = new String[] {"/Users/muller/permissions.xml"};

            ToolBox.configureLogging();
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
            
            new PermissionsExport().saveXml(args[0], null, ApplicationProperties.getProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
