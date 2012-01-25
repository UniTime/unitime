package org.unitime.timetable.security.spring;

import java.util.Collection;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Service;

@Service("unitimeUserContextMapper")
public class UniTimeUserContextMapper implements UserDetailsContextMapper {

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
		if (authorities.isEmpty()) {
			return new UniTimeUser(username, null, username);
		} else {
			String id = authorities.iterator().next().getAuthority();
			if (id.startsWith("ROLE_")) id = id.substring("ROLE_".length());
			return new UniTimeUser(username, null, id);
		}
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
	}

}
