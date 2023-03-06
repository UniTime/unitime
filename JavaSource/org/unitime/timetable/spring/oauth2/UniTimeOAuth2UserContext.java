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

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.unitime.timetable.security.context.UniTimeUserContext;

/**
 * @author Tomas Muller
 */
public class UniTimeOAuth2UserContext extends UniTimeUserContext implements OAuth2User {
	private static final long serialVersionUID = 4447319990733138487L;
	private OAuth2User iOAuth2User;
	
	public UniTimeOAuth2UserContext(String externalId, String name, OAuth2User user) {
		super(externalId, user.getName(), name, null); 
		iOAuth2User = user;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return iOAuth2User.getAttributes();
	}
}
