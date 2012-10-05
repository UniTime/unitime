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