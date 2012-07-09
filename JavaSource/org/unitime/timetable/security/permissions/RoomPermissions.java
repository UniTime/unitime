package org.unitime.timetable.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class RoomPermissions {

	@PermissionForRight(Right.AddNonUnivLocation)
	public static class AddNonUnivLocation implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}

}
