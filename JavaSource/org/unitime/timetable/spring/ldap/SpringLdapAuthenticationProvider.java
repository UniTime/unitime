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

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.unitime.timetable.defaults.ApplicationProperty;

public class SpringLdapAuthenticationProvider extends LdapAuthenticationProvider {
	
	public SpringLdapAuthenticationProvider(LdapAuthenticator authenticator) {
		super(authenticator);
	}
	
	public SpringLdapAuthenticationProvider(LdapAuthenticator authenticator, LdapAuthoritiesPopulator authoritiesPopulator) {
		super(authenticator, authoritiesPopulator);
	}

	@Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken authentication) {
		if (ApplicationProperty.AuthenticationLdapUrl.defaultValue().equals(ApplicationProperty.AuthenticationLdapUrl.value()))
			throw new BadCredentialsException("LDAP authentication is not configured.");
		return super.doAuthentication(authentication);
	}

}
