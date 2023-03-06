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

import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
@Service("unitimeOAuth2ClientRegistrationRepository")
public class UniTimeOAuth2ClientRegistrationRepository implements ClientRegistrationRepository{

	@Override
	public ClientRegistration findByRegistrationId(String registrationId) {
		if (ApplicationProperty.AuthenticationOAuht2ClientId.value() == null || ApplicationProperty.AuthenticationOAuht2ClientId.value().isEmpty()) return null;
		
		if ("google".equals(registrationId)) {
			return CommonOAuth2Provider.GOOGLE.getBuilder(registrationId)
					.clientId(ApplicationProperty.AuthenticationOAuht2ClientId.value())
					.clientSecret(ApplicationProperty.AuthenticationOAuht2ClientSecret.value())
					.scope(ApplicationProperty.AuthenticationOAuht2Scope.value().split(","))
					.registrationId(registrationId)
					.build();
		}
		if ("facebook".equals(registrationId)) {
			return CommonOAuth2Provider.FACEBOOK.getBuilder(registrationId)
					.clientId(ApplicationProperty.AuthenticationOAuht2ClientId.value())
					.clientSecret(ApplicationProperty.AuthenticationOAuht2ClientSecret.value())
					.scope(ApplicationProperty.AuthenticationOAuht2Scope.value().split(","))
					.registrationId(registrationId)
					.build();
		}
		if ("github".equals(registrationId)) {
			return CommonOAuth2Provider.GITHUB.getBuilder(registrationId)
					.clientId(ApplicationProperty.AuthenticationOAuht2ClientId.value())
					.clientSecret(ApplicationProperty.AuthenticationOAuht2ClientSecret.value())
					.scope(ApplicationProperty.AuthenticationOAuht2Scope.value().split(","))
					.registrationId(registrationId)
					.build();
		}
		if ("okta".equals(registrationId)) {
			return CommonOAuth2Provider.OKTA.getBuilder(registrationId)
					.clientId(ApplicationProperty.AuthenticationOAuht2ClientId.value())
					.clientSecret(ApplicationProperty.AuthenticationOAuht2ClientSecret.value())
					.scope(ApplicationProperty.AuthenticationOAuht2Scope.value().split(","))
					.registrationId(registrationId)
					.build();
		}
		
		return null;
	}

}
