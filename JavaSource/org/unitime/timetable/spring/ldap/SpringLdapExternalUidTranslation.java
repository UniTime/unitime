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
package org.unitime.timetable.spring.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
public class SpringLdapExternalUidTranslation implements ExternalUidTranslation {
	private static Log sLog = LogFactory.getLog(SpringLdapExternalUidTranslation.class);

	public String translate(String uid, Source source, Source target) {
        if (uid==null || source.equals(target)) return uid;
        if (source.equals(Source.LDAP)) return uid2ext(uid);
        if (target.equals(Source.LDAP)) return ext2uid(uid);
        return uid;
    }
    
    public String uid2ext(String uid) {
    	String externalIdAttribute = ApplicationProperty.AuthenticationLdapIdAttribute.value();
    	if ("uid".equals(externalIdAttribute)) return uid; // Nothing to translate
        try {
        	
			ContextSource source = (ContextSource)SpringApplicationContextHolder.getBean("unitimeLdapContextSource");
			
			String query = ApplicationProperty.AuthenticationLdapLogin2UserId.value();

			SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(source);
			DirContextOperations user = template.retrieveEntry(query.replaceAll("\\{0\\}", uid), new String[] {externalIdAttribute});
			
			return user == null ? null : user.getStringAttribute(externalIdAttribute);
			
        } catch (Exception e) {
        	sLog.warn("Unable to translate uid to " + externalIdAttribute + ": " + e.getMessage());
        }
        
        return null;
    }
    
    public String ext2uid(String externalUserId) {
    	String externalIdAttribute = ApplicationProperty.AuthenticationLdapIdAttribute.value();
    	if ("uid".equals(externalIdAttribute)) return externalUserId; // Nothing to translate
        try {
        	
        	ContextSource source = (ContextSource)SpringApplicationContextHolder.getBean("unitimeLdapContextSource");
			
			String query = ApplicationProperty.AuthenticationLdapUserId2Login.value().replace("%", externalIdAttribute);
			
			SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(source);
			DirContextOperations user = template.retrieveEntry(query.replaceAll("\\{0\\}", externalIdAttribute), new String[] {"uid"});
			
			return user == null ? null : user.getStringAttribute("uid");

        } catch (Exception e) {
        	sLog.warn("Unable to translate " + externalIdAttribute + " to uid: " + e.getMessage());
        }
        return null;
    }
}
