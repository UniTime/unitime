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
package org.unitime.timetable.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DoAndActionRedirectFilter implements Filter {
	Map<String, String> iTranslations;

	@Override
	public void init(FilterConfig config) throws ServletException {
		iTranslations = new HashMap<String, String>();
		iTranslations.put("classDetail", "clazz");
		iTranslations.put("subpartDetail", "subpart");
		iTranslations.put("examDetail", "examination");
		iTranslations.put("classInfo", "classAssignment");
		iTranslations.put("classAssignmentsReportSearch", "classAssignments");
		iTranslations.put("classInstructorAssignment", "assignClassInstructors");
		iTranslations.put("courseOfferingEdit", "courseOffering");
		iTranslations.put("crossListsModify", "crosslist");
		iTranslations.put("dataImport", "dataExchange");
		iTranslations.put("datePatternEdit", "datePatterns");
		iTranslations.put("departmentEdit", "departments");
		iTranslations.put("departmentList", "departments");
		iTranslations.put("deptStatusTypeEdit", "statusTypes");
		iTranslations.put("dispDatePattern", "main");
		iTranslations.put("distributionPrefs", "distributions");
		iTranslations.put("distributionTypeEdit", "distributionTypes");
		iTranslations.put("distributionTypeList", "distributionTypes");
		iTranslations.put("exactTimeEdit", "admin?type=exactTimes");
		iTranslations.put("examDetail", "examination");
		iTranslations.put("examDistributionPrefs", "examDistributions");
		iTranslations.put("examInfo", "examAssignment");
		iTranslations.put("examList", "examinations");
		iTranslations.put("examPeriodEdit", "examPeriods");
		iTranslations.put("instructionalOfferingConfigEdit", "instrOfferingConfig");
		iTranslations.put("instructionalOfferingDetail", "offering");
		iTranslations.put("instructionalOfferingModify", "multipleClassSetup");
		iTranslations.put("instructionalOfferingSearch", "offerings");
		iTranslations.put("instructorAssignmentPref", "instrAssignmentPrefs");
		iTranslations.put("instructorDetail", "instructor");
		iTranslations.put("instructorInfoEdit", "instructorEdit");
		iTranslations.put("instructorPrefEdit", "instructorPrefs");
		iTranslations.put("instructorSearch", "instructors");
		iTranslations.put("itypeDescEdit", "admin?type=itype");
		iTranslations.put("itypeDescList", "admin?type=itype");
		iTranslations.put("personalSchedule", "personal");
		iTranslations.put("rollForwardSession", "rollForward");
		iTranslations.put("schedulingSubpartDetail", "subpart");
		iTranslations.put("schedulingSubpartEdit", "subpartEdit");
		iTranslations.put("sessionEdit", "sessions");
		iTranslations.put("sessionList", "sessions");
		iTranslations.put("solverGroupEdit", "solverGroups");
		iTranslations.put("solverSettings", "solverConfig");
		iTranslations.put("subjectAreaEdit", "admin?type=subjectArea");
		iTranslations.put("subjectList", "admin?type=subjectArea");
		iTranslations.put("timePatternEdit", "timePatterns");
		iTranslations.put("timetableManagerEdit", "managers");
		iTranslations.put("timetableManagerList", "managers");
		iTranslations.put("unassignedExams", "notAssignedExams");
		iTranslations.put("userEdit", "admin?type=user");
	}


	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			HttpServletRequest req = (HttpServletRequest)request;
			HttpServletResponse res = (HttpServletResponse)response;
			if (req.getRequestURI().endsWith(".do")) {
				String uri = req.getRequestURI().substring(req.getContextPath().length() + 1).replace(".do", "");
				if (iTranslations.containsKey(uri))
					uri = iTranslations.get(uri);
	    		if (req.getQueryString() != null && !req.getQueryString().isEmpty())
	    			uri += (uri.indexOf('?') > 0 ? "&" : "?") + req.getQueryString();		    	
				res.sendRedirect(uri);
				return;
			} else if (req.getRequestURI().endsWith(".action")) {
				String uri = req.getRequestURI().substring(req.getContextPath().length() + 1).replace(".action", "");
				if (iTranslations.containsKey(uri))
					uri = iTranslations.get(uri);
	    		if (req.getQueryString() != null && !req.getQueryString().isEmpty())
	    			uri += (uri.indexOf('?') > 0 ? "&" : "?") + req.getQueryString();		    	
				res.sendRedirect(uri);
				return;
			}
		}
		chain.doFilter(request, response);
	}


}
