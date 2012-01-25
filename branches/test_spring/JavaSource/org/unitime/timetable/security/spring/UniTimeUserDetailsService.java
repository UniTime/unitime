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
package org.unitime.timetable.security.spring;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.User;
import org.unitime.timetable.model.dao.UserDAO;

@Service("unitimeUserDetailsService")
public class UniTimeUserDetailsService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		org.hibernate.Session hibSession = null;
		try {
			hibSession = UserDAO.getInstance().createNewSession();
			User user = (User)hibSession.createQuery(
					"select u from User u where u.username=:userName")
					.setString("userName", username).setMaxResults(1).uniqueResult();
			if (user == null) throw new UsernameNotFoundException("User " + username + " is not known.");
			return new UniTimeUser(user.getUsername(), user.getPassword(), user.getExternalUniqueId());
		} finally {
			hibSession.close();
		}
	}

}
