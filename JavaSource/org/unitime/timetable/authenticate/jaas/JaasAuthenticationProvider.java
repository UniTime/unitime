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
package org.unitime.timetable.authenticate.jaas;


import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.unitime.timetable.security.context.UniTimeUserContext;


/**
 * @author Tomas Muller
 */
public class JaasAuthenticationProvider extends DefaultJaasAuthenticationProvider {
	
	public JaasAuthenticationProvider() {
		setAuthorityGranters(new AuthorityGranter[] {
			new AuthorityGranter() {
				@Override
				public Set<String> grant(Principal principal) {
					Set<String> roles = new HashSet<String>();
					if (principal instanceof HasExternalId) {
						roles.add(((HasExternalId)principal).getExternalId());
					} else {
						String user = principal.getName();
						if (user.indexOf('@') >= 0) user = user.substring(0, user.indexOf('@'));
						roles.add(user);
					}
					return roles;
				}
			}
		});
	}
	
	
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
    	JaasAuthenticationToken ret = (JaasAuthenticationToken)super.authenticate(auth);
    	for (GrantedAuthority role: ret.getAuthorities()) {
    		UniTimeUserContext user = new UniTimeUserContext(role.getAuthority(), ret.getName(), null, null);
    		return new JaasAuthenticationToken(user, ret.getCredentials(), new ArrayList<GrantedAuthority>(user.getAuthorities()), ret.getLoginContext());
    	}
    	return null;
    }
}