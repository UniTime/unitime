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
package org.unitime.timetable.spring.security;

import java.util.Collection;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.security.context.UniTimeUserContext;

/**
 * @author Tomas Muller
 */
@Service("unitimeUserContextMapper")
public class UniTimeUserContextMapper implements UserDetailsContextMapper {
	private ExternalUidTranslation iTranslation = null;
	
	public UniTimeUserContextMapper() {
		if (ApplicationProperty.ExternalUserIdTranslation.value() != null) {
            try {
            	iTranslation = (ExternalUidTranslation) Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) {
            	Debug.error("Unable to instantiate external uid translation class, "+e.getMessage());
            }
        }
	}
	
	@Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
		String userId = username;
		
		if (!authorities.isEmpty())
			userId = authorities.iterator().next().getAuthority();
		
		if (iTranslation != null && ApplicationProperty.AuthenticationLdapIdTranslate.isTrue())
			userId = iTranslation.translate(userId, Source.LDAP, Source.User);
		
		if (ApplicationProperty.AuthenticationLdapIdTrimLeadingZeros.isTrue())
			while (userId.startsWith("0")) userId = userId.substring(1);
		
		return new UniTimeUserContext(userId, username, ctx.getStringAttribute("cn"), null);
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

}
