package org.unitime.timetable.security.authority;

import org.unitime.timetable.model.Roles;
import org.unitime.timetable.security.rights.HasRights;

public class NoRoleAuthority extends AbstractAuthority {
	private static final long serialVersionUID = 1L;
	public static final String TYPE = Roles.ROLE_NONE;
	
	public NoRoleAuthority(HasRights permissions) {
		super(0l, TYPE, "No Role", permissions);
	}

}
