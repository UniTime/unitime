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
package org.unitime.timetable.spring.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
public class SpringLdapExternalUidLookup implements ExternalUidLookup {
	private static Log sLog = LogFactory.getLog(SpringLdapExternalUidLookup.class);

	@Override
	public UserInfo doLookup(String uid) throws Exception {
		try {
			ContextSource source = (ContextSource)SpringApplicationContextHolder.getBean("unitimeLdapContextSource");
			
			String query = ApplicationProperties.getProperty("unitime.authentication.ldap.identify", "uid={0},ou=identify");
			String idAttributeName = ApplicationProperties.getProperty("unitime.authentication.ldap.group-role-attribute", "uid");

			SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(source);
			DirContextOperations user = template.retrieveEntry(query.replaceAll("\\{0\\}", uid), new String[] {"uid", idAttributeName, "cn", "givenName", "sn", "mail"});

			if (user == null || user.getStringAttribute(idAttributeName) == null)
				return null;
            
        	UserInfo info = new UserInfo();
        	info.setExternalId(user.getStringAttribute(idAttributeName));
        	
        	info.setUserName(user.getStringAttribute("uid"));
        	if (info.getUserName() == null) info.setUserName(uid);
        	info.setName(user.getStringAttribute("cn"));
        	info.setFirstName(user.getStringAttribute("givenName"));
        	info.setLastName(user.getStringAttribute("sn"));
        	info.setEmail(user.getStringAttribute("mail"));

        	if (info.getEmail() == null) {
            	String email = info.getUserName() + "@";
        		for (String x: user.getNameInNamespace().split(","))
        			if (x.startsWith("dc=")) email += (email.endsWith("@") ? "" : ".") + x.substring(3);
            	if (!email.endsWith("@")) info.setEmail(email);
        	}
        	
        	return info;
		} catch (Exception e) {
			sLog.warn("Lookup for " + uid + " failed: " + e.getMessage());
		}

		return null;
	}

}
