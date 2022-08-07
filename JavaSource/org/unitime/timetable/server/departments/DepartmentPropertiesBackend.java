/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.server.departments;


import java.util.HashMap;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesRequest;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ReferenceList;

@GwtRpcImplements(DepartmentPropertiesRequest.class)
public class DepartmentPropertiesBackend implements GwtRpcImplementation<DepartmentPropertiesRequest, DepartmentPropertiesInterface>{

	public HashMap<String, String>  getStatusOptions() { 
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForDepartment());
		HashMap<String,String> map = new HashMap<String,String>();
		for (RefTableEntry r : ref) {
			 map.put(r.getReference(),r.getLabel());
		}
		return map;
	}
	
	   
		public HashMap<Long, String> getAllDependentDepartments( SessionContext context) { 
			 TreeSet<Department> departments =  Department.findAllNonExternal(context.getUser().getCurrentAcademicSessionId());
			 HashMap<Long, String> map = new HashMap<Long,String>();
			for (Department d: departments){
				  String deptCode = d.getDepartment().getDeptCode();
				  String deptName = d.getDepartment().getName();
				  String displayName = deptCode + " : " +deptName;
				  map.put(d.getDepartment().getUniqueId(),displayName);
			}
			  return map;   
		}

	@Override
	public DepartmentPropertiesInterface execute(DepartmentPropertiesRequest request, SessionContext context) {
		DepartmentPropertiesInterface d = new DepartmentPropertiesInterface();	
		d.setStatusOptions(getStatusOptions());
		d.setExtDepartmentOptions(getAllDependentDepartments(context));
		d.setAcademicSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
		d.setCoursesFundingDepartmentsEnabled(ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue());
		Department dept = null;
		if (request.getDepartmentId() != null) {
			dept = DepartmentDAO.getInstance().get(request.getDepartmentId());
		}
				
		if (dept != null) {
			d.setCanDelete(context.hasPermission(dept, Right.DepartmentDelete));
			d.setCanChangeExtManager(context.hasPermission(dept, Right.DepartmentEditChangeExternalManager));
			d.setCanEdit(context.hasPermission(dept, Right.DepartmentEdit));
		} else {
			d.setCanDelete(false);
			d.setCanChangeExtManager(true);
			d.setCanEdit(true);			
		}
		
		return d;
	}
}

