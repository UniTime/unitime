package org.unitime.timetable.security.authority;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.rights.Right;

public class ManagerAuthority extends SimpleAuthority {
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "Manager";
	
	public ManagerAuthority(TimetableManager manager, Session session) {
		super(
				manager.getUniqueId(),
				session.getUniqueId(),
				TYPE,
				session.getReference(),
				manager.getName());
	}
	
	@Override
	public boolean hasRight(Right right) {
		return false;
	}
}
