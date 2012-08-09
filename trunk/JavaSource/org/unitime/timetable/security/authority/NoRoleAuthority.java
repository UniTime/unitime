package org.unitime.timetable.security.authority;

import org.unitime.timetable.security.rights.Right;

public class NoRoleAuthority extends AbstractAuthority {
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "NoRole";
	
	public NoRoleAuthority() {
		super(0l, TYPE, "No Role");
	}

	@Override
	public boolean hasRight(Right right) {
		switch (right) {
		case Inquiry:
			return true;
		default:
			return false;
		}
	}

}
