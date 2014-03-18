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
