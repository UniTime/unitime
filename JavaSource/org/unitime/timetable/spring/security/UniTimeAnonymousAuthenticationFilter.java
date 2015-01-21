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

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.unitime.timetable.security.context.AnonymousUserContext;

/**
 * @author Tomas Muller
 */
@Service("unitimeAnonymousFilter")
public class UniTimeAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {

	public UniTimeAnonymousAuthenticationFilter() {
		super("guest");
	}

	@Override
	protected Authentication createAuthentication(HttpServletRequest request) {
		try {
			AnonymousUserContext user = new AnonymousUserContext();
			if (!user.getAuthorities().isEmpty())
				return new AnonymousAuthenticationToken("guest", user, user.getAuthorities());
			else
				return super.createAuthentication(request);
		} catch (Throwable t) {
			return super.createAuthentication(request);
		}
    }
}