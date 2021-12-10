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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
//import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentCheckCanDeleteRequest;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesRequest;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ReferenceList;
import org.unitime.timetable.defaults.ApplicationProperty;

@GwtRpcImplements(DepartmentPropertiesRequest.class)
public class DepartmentPropertiesBackend implements GwtRpcImplementation<DepartmentPropertiesRequest, DepartmentPropertiesInterface>{

	public HashMap<String, String>  getStatusOptions() { 
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForDepartment());
		HashMap<String,String> map = new HashMap<String,String>();
		for (Iterator it = ref.iterator(); it.hasNext();) {
			RefTableEntry r = (RefTableEntry) it.next();
			 map.put(r.getReference(),r.getLabel());
		}
		return map;
	}
	
	   
		public HashMap<Long, String> getAllDependentDepartments( SessionContext context) { 
			 TreeSet<Department> departments =  Department.findAllNonExternal(context.getUser().getCurrentAcademicSessionId());
			 List<String> ret = new ArrayList<String>();
			 HashMap<Long, String> map = new HashMap<Long,String>();
			for (Department d: departments){
				  String deptCode = d.getDepartment().getDeptCode();
				  String deptName = d.getDepartment().getName();
				  String displayName = deptCode + " : " +deptName;
				  map.put(d.getDepartment().getUniqueId(),displayName);
			}
			  return map;   
		}
/*
		public HashMap<String, String> getAllStatusOptions() { 
			 TreeSet<DepartmentStatusType> departmentStatusTypes =  DepartmentStatusType.findAllForDepartment();
			 List<String> ret = new ArrayList<String>();
			 HashMap<String,String> map = new HashMap<String,String>();

			 for (DepartmentStatusType d: departmentStatusTypes)
				 map.put(d.getReference(),d.getLabel());
				  //map.put(d.getStatus(),d.getLabel());
			 //map.put(d.getStatus(),d.getLabel());
				 //ret.add(d.toString());
			  return map;   
		}*/
	@Override
	public DepartmentPropertiesInterface execute(DepartmentPropertiesRequest request, SessionContext context) {
		DepartmentPropertiesInterface d = new DepartmentPropertiesInterface();	
		d.setSatusOptions(getStatusOptions());
		d.setExtDepartmentOptions(getAllDependentDepartments( context));
		d.setAcademicSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
		
		boolean coursesFundingDepartmentsEnabled = ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue();
		if (coursesFundingDepartmentsEnabled) 
			d.setCoursesFundingDepartmentsEnabled(true);
				
		
		System.out.print("getAcademicSessionName" + d.getAcademicSessionName());
		return d;
		//return new GwtRpcResponseBoolean(context.hasPermission(request.getDepartmentId(), "Department", Right.DepartmentDelete));
	}
}

