<%--
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN" "http://www.w3.org/TR/REC-html40/strict.dtd">
<%@ page import="org.unitime.commons.User" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ page errorPage="error.jsp" %>
<%@ page import="org.unitime.timetable.model.Session" %>
<%@ page import="org.unitime.timetable.model.TimetableManager" %>
<%@ page import="org.unitime.timetable.ApplicationProperties" %>
<%@ include file="/checkLogin.jspf" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css" />
	<% if (ApplicationProperties.getProperty("tmtbl.custom.css")!=null) { %>
		<LINK rel="stylesheet" type="text/css" href="<%=ApplicationProperties.getProperty("tmtbl.custom.css")%>" />
	<% } %>
	<script language="javascript" type="text/javascript" src="scripts/tree.js"></script>
</head>
<body class="MenuBody">
<script language="javascript" type="text/javascript">
<% User user = Web.getUser(session); 
   TimetableManager manager = (user==null?null:TimetableManager.getManager(user)); 
   Session acadSession = (user==null?null:Session.getCurrentAcadSession(user));
%>
<% if (user!=null && manager!=null && acadSession!=null) { %>
	
	menu_item('1','Input Data','Input Data','','collapse');
		leaf_item('Instructional Offerings','Instructional Offerings','instructionalOfferingShowSearch.do');
		leaf_item('Classes','Classes','classShowSearch.do');
		menu_item('10','Instructors','Instructors','instructorSearch.do','collapse');
			leaf_item('Designator List','Designator List','designatorList.do');
		enditem(); //10
		menu_item('11','Rooms','View Rooms','roomSearch.do','collapse');
			leaf_item('Features','View Room Features','roomFeatureSearch.do');
			leaf_item('Groups','View Room Groups','roomGroupSearch.do');
		enditem(); //11
		leaf_item('Distribution Preferences','Manage Distribution Preferences','distributionPrefs.do');
		leaf_item('Reservations','Manage Reservations','reservationList.do');
		<%  if (manager.canSeeTimetable(acadSession, user)) { %>
			leaf_item('Class Assignments','Class Assignment Report','classAssignmentsReportShowSearch.do');
		<%  } %>
	enditem(); //1

<%  if (manager.hasASolverGroup(acadSession, user)) { %>
<%  if (manager.canSeeTimetable(acadSession, user)) { %>		
		menu_item('2','Timetables','List of Timetables','listSolutions.do','collapse');
<% 			if (manager.canDoTimetable(acadSession, user)) { %>		
				leaf_item('Solver','Create Timetable / Solver Problem','solver.do');
<% 			} %>
			leaf_item('Timetable','Timetable Grid','timetable.do');
<% 			if (manager.canDoTimetable(acadSession, user)) { %>		
				leaf_item('Assigned','Browse Assigned Classes','assignedClasses.do');
				leaf_item('Not-assigned','Browse Not-assigned Classes','unassigned.do');
				leaf_item('Changes','Browse Solution Changes','solutionChanges.do');
				leaf_item('History','Browse Assignment History','assignmentHistory.do');
<% 			} %>
			leaf_item('Conflict Statistics','Browse Conflict-based Statistics','cbs.do');
			leaf_item('Log','Display Solver Log','solverLog.do');
<% 			if (manager.canDoTimetable(acadSession, user)) { %>		
				leaf_item('Reports','Display Solution Reports','solutionReport.do');
<% 			} %>
<% 			if (user.getRole().equals(Roles.ADMIN_ROLE)) { %>
				leaf_item('Manage Solvers','Manage Running Solvers','manageSolvers.do');
<% 			} %>
		enditem(); //2
<% } else if (manager.canAudit(acadSession, user)) { %>
		menu_item('2','Audit','Audit Input Data','','collapse');
			leaf_item('Solver','Audit Problem','solver.do');
			leaf_item('Conflict Statistics','Browse Conflict-based Statistics','cbs.do');
			leaf_item('Log','Display Solver Log','solverLog.do');
			leaf_item('Reports','Display Solution Reports','solutionReport.do');
		enditem(); //2
<% }} %>

<% if (user!=null
		&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>
	leaf_item('Sectioning','Student Sectioning','sectioningDemo.do');
<% } %>
		
	menu_item('3','User Preferences','User Preferences','','expand');
		leaf_item('Change Role','Change Role','selectPrimaryRole.do?list=Y');
<% if ( ( user!=null && user.getRole().equals(Roles.ADMIN_ROLE) )
			|| ( session.getAttribute("hdnAdminAlias")!=null && session.getAttribute("hdnAdminAlias").toString().equals("1") ) ) { %>
		leaf_item('Switch User','Switch to a different user','chameleon.do');
<% } %>
		leaf_item('Settings','Default Application Settings','managerSettings.do');
	enditem(); //3

<% if (user!=null
		&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>

		menu_item('4','Administration','Administration','','expand');
			menu_item('41','Input Data','Application Level Input Data','','expand');
				leaf_item('Academic Sessions','Academic Session Management','sessionList.do');
				leaf_item('Managers','Timetable Managers','timetableManagerList.do');
				leaf_item('Departments','Manage Departments','departmentList.do');
				leaf_item('Subject Areas','Manage Subject Areas','subjectList.do');
				leaf_item('Buildings','Manage Buildings','buildingList.do');
				leaf_item('Rooms','Manage Rooms','roomList.do');
				leaf_item('Date Patterns','Manage Date Patterns','datePatternEdit.do');
				menu_item('411','Time Patterns','Manage Time Patterns','timePatternEdit.do','expand');
					leaf_item('Exact Time','Exact Time Pattern','exactTimeEdit.do');
				enditem(); //411
				leaf_item('Distribution Types','Manage Distribution Types','distributionTypeList.do');
				leaf_item('Instructional Types','Manage Instructional Types','itypeDescList.do');
				<%--
				leaf_item('Preference Levels','Preference Levels','preferenceLevelList.do');
				--%>
				leaf_item('Status Types','Manage Session/Department Status Types','deptStatusTypeEdit.do');
				leaf_item('Import Data','Import Data','dataImport.do');
				leaf_item('Change Log','View Change Log','lastChanges.do');
<%	if (ApplicationProperties.getProperty("tmtbl.menu.admin.extra")!=null) {
		out.println(ApplicationProperties.getProperty("tmtbl.menu.admin.extra"));
	}
%>
			enditem(); //41
	
			menu_item('42','Solver','Solver','','expand');
				leaf_item('Parameter Groups', 'Manage Solver Parameter Groups','solverParamGroups.do');
				leaf_item('Parameters', 'Manage Solver Parameter Definitions','solverParamDef.do');
				leaf_item('Settings', 'Manage Solver Settings','solverSettings.do');
				leaf_item('Definitions', 'Manage Solution Info Definitions','solverInfoDef.do');
				leaf_item('Solver Groups', 'Manage Solver Groups','solverGroupEdit.do');
			enditem(); //42
			
			menu_item('43','Defaults','Defaults','','expand');
				leaf_item('Configuration','Application Configuration','applicationConfig.do');
				leaf_item('Settings','Set Default Settings for Users','settings.do');
			enditem(); //43

			menu_item('44','Utilities','Miscellaneous Application Utilities','','expand');
				leaf_item('Hibernate Statistics','Display Hibernate Session Statistics','hibernateStats.do');
				leaf_item('Test HQL','Test HQL Queries','hibernateQueryTest.do');
			enditem(); //44
			
		enditem(); //4

<% } %>

	menu_item('5','Help','Help Manual','','expand');
		leaf_item('Data Entry Manual','Manual for Data Entry','help/Data-Entry-Manual.pdf', '_help');
		leaf_item('Solver Manual','Manual for Solving Departmental Problem','help/Solver-Manual.pdf', '_help');
		leaf_item('Tips &amp; Tricks','Tips &amp; Tricks','help/tips.html');
		leaf_item('FAQ','Frequently Asked Questions','help/faq.html');
		leaf_item('Release Notes','Release Notes','help/Release-Notes.xml', '_help');
		leaf_item('Contact Us','Contact Us','inquiry.do');
	enditem(); //5

	leaf_item('System Messages','View System Messages','blank.jsp');
	leaf_item('Log Out','Exit Timetabling Appplication','logOut.do');
<% } %>
</script>
</body>
</html>
