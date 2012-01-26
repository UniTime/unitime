package org.unitime.timetable.security.permissions;

import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.security.roles.DepartmentDependentRole;
import org.unitime.timetable.security.spring.UniTimeUser;

@Service("permissionDepartment")
public class SimpleDepartmentPermission implements Permission<Department> {

	@Override
	public boolean check(UniTimeUser user, Department department) {
		return check(user, department, right());
	}
	
	public boolean check(UniTimeUser user, Department department, Right right) {
		// Not authenticated -> no editing
		if (user == null || !user.hasRole()) return false;
		
		// System administrator can always edit
		if (user.getRole().hasRight(Right.IsSystemAdmin)) return true;
		
		// For all other users, session check must pass
		if (department == null || !department.getSessionId().equals(user.getSessionId()))
			return false;
		
		// Administrator can edit if the class is of a correct academic session
		if (user.getRole().hasRight(Right.IsAdmin)) return true;
		
		// Users with department dependent roles must have the correct department
		if (user.getRole() instanceof DepartmentDependentRole &&
			!((DepartmentDependentRole)user.getRole()).getDepartmentIds().contains(department.getUniqueId())) return false;
		
		// Check for the appropriate right
		if (right != null && !user.getRole().hasRight(right)) return false;
		
		// Check department status
		if (!checkStatus(department.effectiveStatusType())) return false;
		
		return true;
	}

	@Override
	public Class<Department> type() {
		return Department.class;
	}
	
	public Right right() { return null; }
	
	public boolean checkStatus(DepartmentStatusType status) { return true; }
}
