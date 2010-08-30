/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.authenticate.jaas.LdapAuthenticateModule;
import org.unitime.timetable.interfaces.ExternalUidLookup;

public class LdapExternalUidLookup implements ExternalUidLookup {
	

	@Override
	public UserInfo doLookup(String searchId) throws Exception {
		
		String query = ApplicationProperties.getProperty("tmtbl.authenticate.ldap.identify");
		if (query == null) return null;
		
        DirContext ctx = null;
        try {
            ctx = LdapAuthenticateModule.getDirContext();
            
    		String idAttributeName = ApplicationProperties.getProperty("tmtbl.authenticate.ldap.externalId","uid");
    		String loginAttributeName = ApplicationProperties.getProperty("tmtbl.authenticate.ldap.login", "uid");
    		Attributes attributes = ctx.getAttributes(query.replaceAll("%", searchId), new String[] {idAttributeName, loginAttributeName, "cn", "givenName", "sn", "mail"});
            Attribute idAttribute = attributes.get(idAttributeName);
            if (idAttribute == null) return null;
            
        	UserInfo user = new UserInfo();
        	user.setExternalId((String)idAttribute.get());
        	user.setUserName((String)attributes.get(loginAttributeName).get());
        	if (attributes.get("cn") != null)
        		user.setName((String)attributes.get("cn").get());
        	if (attributes.get("givenName") != null)
        		user.setFirstName((String)attributes.get("givenName").get());
        	if (attributes.get("cn") != null)
        		user.setName((String)attributes.get("cn").get());
        	if (attributes.get("sn") != null)
        		user.setLastName((String)attributes.get("sn").get());
        	if (attributes.get("mail") != null) {
        		user.setEmail((String)attributes.get("mail").get());
        	} else {
            	String email = user.getUserName() + "@";
            	for (String x: query.split(","))
            		if (x.startsWith("dc=")) email += (email.endsWith("@") ? "" : ".") + x.substring(3);
            	if (!email.endsWith("@")) user.setEmail(email);
        	}
        	
        	return user;			
		} finally {
			if (ctx != null) ctx.close();
		}
	}

}
