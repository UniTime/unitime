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
			if (ApplicationProperty.AuthenticationCasIdAlwaysTranslate.isTrue())
				userId = iTranslation.translate(userId, Source.LDAP, Source.User);
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
