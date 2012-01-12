package org.unitime.timetable.security.permissions;

import org.unitime.timetable.guice.context.UserContext;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.security.roles.DepartmentDependentRole;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class SimpleDepartmentPermission implements Permission<Department> {
	@Inject UserContext context;

	@Override
	public boolean check(Department department) {
		return check(department, right());
	}
	
	public boolean check(Department department, Right right) {
		// Not authenticated -> no editing
		if (!context.isAuthenticated()) return false;
		
		// System administrator can always edit
		if (context.getRole().hasRight(Right.IsSystemAdmin)) return true;
		
		// For all other users, session check must pass
		if (department == null || !department.getSessionId().equals(context.getAcademicSessionId()))
			return false;
		
		// Administrator can edit if the class is of a correct academic session
		if (context.getRole().hasRight(Right.IsAdmin)) return true;
		
		// Users with department dependent roles must have the correct department
		if (context.getRole() instanceof DepartmentDependentRole &&
			!context.hasDepartment(department.getUniqueId())) return false;
		
		// Check for the appropriate right
		if (right != null && !context.getRole().hasRight(right)) return false;
		
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
