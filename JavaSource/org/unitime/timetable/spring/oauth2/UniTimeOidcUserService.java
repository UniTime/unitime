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
package org.unitime.timetable.spring.oauth2;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;


/**
 * @author Tomas Muller
 */
@Service("unitimeOidcUserService")
public class UniTimeOidcUserService extends OidcUserService {
	private ExternalUidTranslation iTranslation = null;
	private RestOperations restOperations = null;
	
	public UniTimeOidcUserService() {
        if (ApplicationProperty.ExternalUserIdTranslation.value()!=null) {
            try {
            	iTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) { Debug.error("Unable to instantiate external uid translation class, "+e.getMessage()); }
        }
        RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		this.restOperations = restTemplate;
	}
	
	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser user = super.loadUser(userRequest);
		
		Map<String, Object> additionalAttributes = null;
		if (ApplicationProperty.AuthenticationOAuht2AdditionalAttributes.value() != null && !ApplicationProperty.AuthenticationOAuht2AdditionalAttributes.value().isEmpty()) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
				URI uri = new URI(ApplicationProperty.AuthenticationOAuht2AdditionalAttributes.value());
				RequestEntity<?> request = new RequestEntity(headers, HttpMethod.GET, uri);
				ResponseEntity<Map<String, Object>> response = restOperations.exchange(request, new ParameterizedTypeReference<Map<String, Object>>() {});
				additionalAttributes = response.getBody();
			} catch (Exception e) {
				Debug.error("Failed to query additional attributes: " + e.getMessage(), e);
			}
		}
			
		String userId = user.getName();
		if (ApplicationProperty.AuthenticationOAuht2IdAttribute.value() != null) {
			String[] keys = ApplicationProperty.AuthenticationOAuht2IdAttribute.value().split(",");
			String[] translate = ApplicationProperty.AuthenticationOAuht2IdAlwaysTranslate.value().split(",");
			boolean found = false;
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				String tr = translate[i < translate.length ? i : translate.length - 1];
				Object value = user.getAttribute(key);
				if (value == null && additionalAttributes != null)
					value = additionalAttributes.get(key);
				if (value != null) {
					if (value instanceof List) {
						for (Object o: ((List)value)) {
							userId = o.toString(); break;
						}
					} else {
						userId = value.toString();
					}
					if ("true".equalsIgnoreCase(tr) && iTranslation != null)
						userId = iTranslation.translate(userId, Source.LDAP, Source.User);
					found = true;
					break;
				}
			}
			if (!found && iTranslation != null)
				userId = iTranslation.translate(userId, Source.LDAP, Source.User);
		} else if (iTranslation != null) {
			userId = iTranslation.translate(userId, Source.LDAP, Source.User);
		}
		String name = null;
		if (ApplicationProperty.AuthenticationOAuht2NameAttribute.value() != null) {
			Object value = user.getAttribute(ApplicationProperty.AuthenticationOAuht2NameAttribute.value());
			if (value == null && additionalAttributes != null)
				value = additionalAttributes.get(ApplicationProperty.AuthenticationOAuht2NameAttribute.value());
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
		if (ApplicationProperty.AuthenticationOAuht2IdTrimLeadingZerosFrom.isTrue()) {
			while (userId.startsWith("0")) userId = userId.substring(1);
		}
		return new UniTimeOidcUserContext(userId, name, user);
	}
}
