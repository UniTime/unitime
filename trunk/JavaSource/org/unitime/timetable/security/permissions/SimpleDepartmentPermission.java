package org.unitime.timetable.security.permissions;

import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.authority.DepartmentAuthority;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.rights.Right;

@Service("permissionDepartment")
public class SimpleDepartmentPermission implements PermissionDepartment {

	@Override
	public boolean check(UserContext user, Department department) {
		return check(user, department, right()) && checkStatus(department.effectiveStatusType());
	}
	
	@Override
	public boolean check(UserContext user, Department department, Right right, DepartmentStatusType.Status... status) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || department == null) return false;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !department.getSessionId().equals(authority.getAcademicSessionId()))
			return false;
		
		// Department check
		if (!authority.hasRight(Right.DepartmentIndependent) && !user.hasAuthority(DepartmentAuthority.TYPE, department.getUniqueId()))
			return false;

		// Right check
		if (right != null && !authority.hasRight(right)) return false;

		// Check department status
		if (status.length > 0 && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = department.effectiveStatusType();
			if (type == null) return false;
			for (DepartmentStatusType.Status s: status) {
				if (type.can(s)) return true;
			}
			return false;
		}
		
		return true;
	}

	@Override
	public Class<Department> type() {
		return Department.class;
	}
	
	public Right right() { return null; }
	
	public boolean checkStatus(DepartmentStatusType status) { return true; }
}
