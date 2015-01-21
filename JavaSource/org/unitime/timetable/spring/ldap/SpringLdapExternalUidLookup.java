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
			
			String query = ApplicationProperty.AuthenticationLdapIdentify.value(); 
			String idAttributeName = ApplicationProperty.AuthenticationLdapIdAttribute.value();

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
