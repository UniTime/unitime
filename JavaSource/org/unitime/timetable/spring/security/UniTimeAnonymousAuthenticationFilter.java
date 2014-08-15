/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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