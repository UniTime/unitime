/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.spring.security;

import java.util.List;
import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.security.context.UniTimeUserContext;

@Service("unitimeAuthenticationUserDetailsService")
public class UniTimeAuthenticationUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
	private ExternalUidTranslation iTranslation = null;
	
	public UniTimeAuthenticationUserDetailsService() {
        if (ApplicationProperty.ExternalUserIdTranslation.value()!=null) {
            try {
            	iTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) { Debug.error("Unable to instantiate external uid translation class, "+e.getMessage()); }
        }
	}

	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		Assertion assertion = token.getAssertion();
		Map attributes = assertion.getPrincipal().getAttributes();
		String userId = token.getName();
		if (ApplicationProperty.AuthenticationCasIdAttribute.value() != null) {
			Object value = attributes.get(ApplicationProperty.AuthenticationCasIdAttribute.value());
			if (value != null) {
				if (value instanceof List) {
					for (Object o: ((List)value)) {
						userId = o.toString(); break;
					}
				} else {
					userId = value.toString();
				}
			}
		} else if (iTranslation != null) {
			userId = iTranslation.translate(userId, Source.LDAP, Source.User);
		}
		String name = null;
		if (ApplicationProperty.AuthenticationCasNameAttribute.value() != null) {
			Object value = attributes.get(ApplicationProperty.AuthenticationCasNameAttribute.value());
			if (value != null) {
				if (value instanceof List) {
					for (Object o: ((List)value)) {
						name = o.toString(); break;
					}
				} else {
					name = value.toString();
				}
			}
		}
		if (ApplicationProperty.AuthenticationCasIdTrimLeadingZerosFrom.isTrue()) {
			while (userId.startsWith("0")) userId = userId.substring(1);
		}
		return new UniTimeUserContext(userId, token.getName(), name, null);
	}

}
