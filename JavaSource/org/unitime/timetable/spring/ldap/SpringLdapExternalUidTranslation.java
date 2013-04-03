/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

public class SpringLdapExternalUidTranslation implements ExternalUidTranslation {
	private static Log sLog = LogFactory.getLog(SpringLdapExternalUidTranslation.class);

	public String translate(String uid, Source source, Source target) {
        if (uid==null || source.equals(target)) return uid;
        if (source.equals(Source.LDAP)) return uid2ext(uid);
        if (target.equals(Source.LDAP)) return ext2uid(uid);
        return uid;
    }
    
    public String uid2ext(String uid) {
    	String externalIdAttribute = ApplicationProperties.getProperty("unitime.authentication.ldap.group-role-attribute", "uid");
    	if ("uid".equals(externalIdAttribute)) return uid; // Nothing to translate
        try {
        	
			ContextSource source = (ContextSource)SpringApplicationContextHolder.getBean("unitimeLdapContextSource");
			
			String query = ApplicationProperties.getProperty("unitime.authentication.ldap.uid2ext",
					ApplicationProperties.getProperty("unitime.authentication.ldap.identify", "uid={0},ou=identify"));

			SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(source);
			DirContextOperations user = template.retrieveEntry(query.replaceAll("\\{0\\}", uid), new String[] {externalIdAttribute});
			
			return user == null ? null : user.getStringAttribute(externalIdAttribute);
			
        } catch (Exception e) {
        	sLog.warn("Unable to translate uid to " + externalIdAttribute + ": " + e.getMessage());
        }
        
        return null;
    }
    
    public String ext2uid(String externalUserId) {
    	String externalIdAttribute = ApplicationProperties.getProperty("unitime.authentication.ldap.group-role-attribute", "uid");
    	if ("uid".equals(externalIdAttribute)) return externalUserId; // Nothing to translate
        try {
        	
        	ContextSource source = (ContextSource)SpringApplicationContextHolder.getBean("unitimeLdapContextSource");
			
			String query = ApplicationProperties.getProperty("unitime.authentication.ldap.ext2uid", externalIdAttribute + "={0},ou=identify");
			
			SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(source);
			DirContextOperations user = template.retrieveEntry(query.replaceAll("\\{0\\}", externalIdAttribute), new String[] {"uid"});
			
			return user == null ? null : user.getStringAttribute("uid");

        } catch (Exception e) {
        	sLog.warn("Unable to translate " + externalIdAttribute + " to uid: " + e.getMessage());
        }
        return null;
    }
}
