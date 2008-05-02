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
<%@page import="org.unitime.timetable.util.RoomAvailability"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ include file="/checkLogin.jspf" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css" />
	<tt:hasProperty name="tmtbl.custom.css">
		<LINK rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
	</tt:hasProperty>
	<script language="javascript" type="text/javascript" src="scripts/tree.js"></script>
</head>
<body class="MenuBody">
<script language="javascript" type="text/javascript">
<% User user = Web.getUser(session); 
   TimetableManager manager = (user==null?null:TimetableManager.getManager(user)); 
   Session acadSession = (user==null?null:Session.getCurrentAcadSession(user));
%>
<% if (user!=null && manager!=null && acadSession!=null) { %>
	
	menu_item('1','Course Timetabling','Course Timetabling','','collapse');
		menu_item('10','Input Data','Course Timetabling Input Data','','collapse');
			leaf_item('Instructional Offerings','Instructional Offerings','instructionalOfferingShowSearch.do');
			leaf_item('Classes','Classes','classShowSearch.do');
			menu_item('100','Instructors','Instructors','instructorSearch.do','collapse');
				leaf_item('Designator List','Designator List','designatorList.do');
			enditem(); //100
			menu_item('101','Rooms','Rooms','roomSearch.do','collapse');
				leaf_item('Features','Room Features','roomFeatureSearch.do');
				leaf_item('Groups','Room Groups','roomGroupSearch.do');
			enditem(); //101
			leaf_item('Distribution Prefs','Distribution Preferences','distributionPrefs.do');
			leaf_item('Reservations','Reservations','reservationList.do');
		<%  if (manager.canSeeTimetable(acadSession, user)) { %>
			leaf_item('Class Assignments','Class Assignment Report','classAssignmentsReportShowSearch.do');
		<%  } %>
		enditem(); //10
<%  if (manager.hasASolverGroup(acadSession, user)) { %>
<%  if (manager.canSeeTimetable(acadSession, user)) { %>		
		menu_item('11','Timetables','List of Timetables','listSolutions.do','collapse');
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
<%          if (user!=null && user.getRole().equals(Roles.ADMIN_ROLE)) { %>
				leaf_item('Manage Solvers','Manage Running Solvers','manageSolvers.do');
<%          } %>
		enditem(); //11
<% } else if (manager.canAudit(acadSession, user)) { %>
		menu_item('12','Audit','Audit Input Data','','collapse');
			leaf_item('Solver','Audit Problem','solver.do');
			leaf_item('Conflict Statistics','Browse Conflict-based Statistics','cbs.do');
			leaf_item('Log','Display Solver Log','solverLog.do');
			leaf_item('Reports','Display Solution Reports','solutionReport.do');
		enditem(); //12
<% }} %>
	enditem(); //1
<% if (user!=null
		&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>
	leaf_item('Student Sectioning','Student Sectioning','sectioningDemo.do');
<% } %>
	<%  if (manager.canSeeExams(acadSession, user)) { %>
	menu_item('2','Examination Timetabling','Examination Timetabling','','expand');
		menu_item('20','Input Data','Course Timetabling Input Data','','collapse');
			leaf_item('Instructors','Instructors','instructorSearch.do');
			menu_item('200','Rooms','Rooms','roomSearch.do?default=Exam','collapse');
				leaf_item('Features','Room Features','roomFeatureSearch.do?default=Exam');
				leaf_item('Groups','Room Groups','roomGroupSearch.do?default=Exam');
<%				if (RoomAvailability.getInstance()!=null) { %>
				leaf_item('Availability','Room A','roomAvailability.do');
<%				} %>
			enditem(); //200
			leaf_item('Examinations','View/Edit Examinations','examList.do');
			leaf_item('Distribution Prefs','Examination Distribution Preferences','examDistributionPrefs.do');
		enditem(); //20
	<%  if (manager.canTimetableExams(acadSession, user)) { %>		
		menu_item('21','Examination Solver','Examination Solver','examSolver.do','collapse');
			leaf_item('Timetable','Examination Timetable Grid','examGrid.do');
			leaf_item('Assigned','Assigned Examinations','assignedExams.do');
			leaf_item('Not-assigned','Not-Assigned Examinations','unassignedExams.do');
			leaf_item('Changes','Examination Assignment Changes','examChanges.do');
			leaf_item('Coflicts Statistics','Examination Coflicts Statistics','ecbs.do');
			leaf_item('Log','Examination Solver Log','examSolverLog.do');
		enditem(); //21
	<% } %>
		leaf_item('Reports','Examination Reports','examAssignmentReport.do');
	<%  if (user!=null && (user.getRole().equals(Roles.ADMIN_ROLE) || user.getRole().equals(Roles.EXAM_MGR_ROLE))) { %>
		leaf_item('Pdf Reports','Examination Pdf Reports','examPdfReport.do');
	<% } %>
	enditem(); //2
	<% } %>

<%  if (manager.canSeeEvents(acadSession, user)) { %>
	menu_item('3','Event Management','Events','','expand');
		leaf_item('Events','Events','eventList.do');
		enditem(); //3
<% } %>
		
	menu_item('4','User Preferences','User Preferences','','expand');
		leaf_item('Change Role','Change Role','selectPrimaryRole.do?list=Y');
<% if ( ( user!=null && user.getRole().equals(Roles.ADMIN_ROLE) )
			|| ( session.getAttribute("hdnAdminAlias")!=null && session.getAttribute("hdnAdminAlias").toString().equals("1") ) ) { %>
		leaf_item('Switch User','Switch to a different user','chameleon.do');
<% } %>
		leaf_item('Settings','Default Application Settings','managerSettings.do');
	enditem(); //4

<% if (user!=null
		&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>

		menu_item('5','Administration','Administration','','expand');
			menu_item('51','Academic Sessions','Academic Session Management','sessionList.do','expand');
				leaf_item('Managers','Timetable Managers','timetableManagerList.do');
				leaf_item('Departments','Manage Departments','departmentList.do');
				leaf_item('Subject Areas','Manage Subject Areas','subjectList.do');
				leaf_item('Buildings','Manage Buildings','buildingList.do');
				<%--
				leaf_item('Rooms','Manage Rooms','roomList.do');
				--%>
				leaf_item('Date Patterns','Manage Date Patterns','datePatternEdit.do');
				menu_item('511','Time Patterns','Manage Time Patterns','timePatternEdit.do','expand');
					leaf_item('Exact Time','Exact Time Pattern','exactTimeEdit.do');
				enditem(); //511
				leaf_item('Instructional Types','Manage Instructional Types','itypeDescList.do');
				<%--
				leaf_item('Preference Levels','Preference Levels','preferenceLevelList.do');
				--%>
				leaf_item('Status Types','Manage Status Types','deptStatusTypeEdit.do');
				leaf_item('Examination Periods','Examination Periods','examPeriodEdit.do');
				leaf_item('Import Data','Import Data','dataImport.do');
				leaf_item('Roll Forward Session','Roll Forward Session','rollForwardSession.do');
				leaf_item('Change Log','View Change Log','lastChanges.do');
			enditem(); //51
	
			menu_item('52','Solver','Solver Management','manageSolvers.do','expand');
				leaf_item('Parameter Groups', 'Manage Solver Parameter Groups','solverParamGroups.do');
				leaf_item('Parameters', 'Manage Solver Parameters','solverParamDef.do');
				leaf_item('Configurations', 'Manage Solver Configurations','solverSettings.do');
				leaf_item('Solver Groups', 'Manage Solver Groups','solverGroupEdit.do');
				leaf_item('Distribution Types','Manage Distribution Types','distributionTypeList.do');
				// leaf_item('Definitions', 'Manage Solution Info Definitions','solverInfoDef.do');
			enditem(); //52

			<tt:hasProperty name="tmtbl.menu.admin.extra">
				menu_item('53','Custom','Custom Menus','','expand');
					<tt:property name="tmtbl.menu.admin.extra"/>
				enditem(); //53
			</tt:hasProperty>
			
			menu_item('54','Defaults','Defaults','','expand');
				leaf_item('Configuration','Application Configuration','applicationConfig.do');
				leaf_item('Settings','Set Default Settings for Users','settings.do');
			enditem(); //54

			menu_item('55','Utilities','Miscellaneous Application Utilities','','expand');
				leaf_item('Hibernate Statistics','Display Hibernate Session Statistics','hibernateStats.do');
				leaf_item('Test HQL','Test HQL Queries','hibernateQueryTest.do');
			enditem(); //55
			
		enditem(); //5

<% } %>
	<tt:hasProperty name="tmtbl.help.root">
	menu_item('6','Help','Help Manual','%tmtbl.help.root%','expand','_help');
	</tt:hasProperty>
	<tt:notHasProperty name="tmtbl.help.root">
	menu_item('6','Help','Help Manual','','expand');
	</tt:notHasProperty>
		<tt:hasProperty name="tmtbl.help.manual.input_data">
			leaf_item('Data Entry Manual','Manual for Data Entry','%tmtbl.help.manual.input_data%', '_help');
		</tt:hasProperty>
		<tt:hasProperty name="tmtbl.help.manual.solver">
			leaf_item('Solver Manual','Manual for Solving Departmental Problem','%tmtbl.help.manual.solver%', '_help');
		</tt:hasProperty>
		<tt:hasProperty name="tmtbl.help.tricks">
			leaf_item('Tips &amp; Tricks','Tips &amp; Tricks','%tmtbl.help.tricks%', '_help');
		</tt:hasProperty>
		<tt:hasProperty name="tmtbl.help.faq">
			leaf_item('FAQ','Frequently Asked Questions','%tmtbl.help.faq%', '_help');
		</tt:hasProperty>
		<tt:hasProperty name="tmtbl.help.release_notes">
			leaf_item('Release Notes','Release Notes','%tmtbl.help.release_notes%', '_help');
		</tt:hasProperty>
		<tt:hasProperty name="tmtbl.inquiry.email">
			leaf_item('Contact Us','Contact Us','inquiry.do');
		</tt:hasProperty>
	enditem(); //6
	<%--
	leaf_item('System Messages','View System Messages','blank.jsp');
	--%>
	leaf_item('Log Out','Exit Timetabling Appplication','logOut.do');
<% } %>
</script>
<br></body>
</html>
</script><br>