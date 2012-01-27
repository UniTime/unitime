package org.unitime.timetable.security.permissions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.security.roles.DepartmentDependentRole;
import org.unitime.timetable.security.spring.UniTimeUser;

@Service("permissionClassDetail")
public class ClassDetailPermission implements Permission<Class_> {
	@Autowired
	SimpleSessionPermission permissionSession;

	@Override
	public boolean check(UniTimeUser user, Class_ clazz) {
		if (!permissionSession.check(user, clazz.getControllingDept().getSession(), Right.ClassDetail)) return false;
		
		if (user.getRole() instanceof DepartmentDependentRole) {
			List<Long> departmentIds = ((DepartmentDependentRole)user.getRole()).getDepartmentIds();
			
			if (clazz.getManagingDept() != null && departmentIds.contains(clazz.getManagingDept().getUniqueId())) {
				// Check department / session status -- for the manager
				if (clazz.getManagingDept().effectiveStatusType().canManagerView()) return true;
			}
			
			if (clazz.getControllingDept() != null && departmentIds.contains(clazz.getControllingDept().getUniqueId())) {
				// Check department / session status -- for the owner
				if (clazz.getControllingDept().effectiveStatusType().canOwnerView()) return true;
			}			
		} else {
			return true;
		}
		
		return false;
	}

	@Override
	public Class<Class_> type() {
		return Class_.class;
	}

}
