<%-- 
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
<%@ page language="java" %>
<%@ page errorPage="../error.jsp" %>
<%@ page import="org.unitime.commons.User" %>
<%@ page import="org.unitime.commons.hibernate.util.HibernateUtil" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ page import="java.util.Map" %>
<%@ page import="net.sf.cpsolver.ifs.util.DataProperties" %>
<%@ page import="org.unitime.timetable.model.TimetableManager" %>
<%@ page import="org.unitime.timetable.model.dao.SolverGroupDAO" %>
<%@ page import="org.unitime.timetable.model.SolverGroup" %>
<%@ page import="org.unitime.commons.Debug" %>
<%@ page import="org.unitime.timetable.model.Session" %>
<jsp:directive.page import="org.unitime.timetable.model.Roles"/>
<%@page import="org.unitime.timetable.solver.exam.ExamSolverProxy"%>
<%@page import="org.unitime.timetable.model.dao.SessionDAO"%>
<%@page import="org.unitime.timetable.model.Exam"%>
<%@page import="org.unitime.timetable.solver.studentsct.StudentSolverProxy"%>
<%@ include file="../checkLogin.jspf" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<HEAD>
<%!
	public String getName(String puid) {
		return getName(TimetableManager.findByExternalId(puid));
	}

	public String getName(TimetableManager mgr) {
		if (mgr==null) return null;
		return mgr.getShortName();
	}

	public String getName(SolverGroup gr) {
		if (gr==null) return null;
		return gr.getAbbv();
	}
%>
<%=Web.metaExpireNow()%>
<% 
	SolverProxy solver = WebSolver.getSolver(session);
	ExamSolverProxy examSolver = (solver==null?WebSolver.getExamSolverNoSessionCheck(session):null);
	StudentSolverProxy studentSolver = (solver==null && examSolver==null?WebSolver.getStudentSolverNoSessionCheck(session):null);  
	int tab = 0;
	if (session.getAttribute("UserInfo.tab")!=null)
		tab = ((Integer)session.getAttribute("UserInfo.tab")).intValue();
	if (request.getParameter("tab")!=null)
		tab = Integer.parseInt(request.getParameter("tab"));
	if (solver==null && examSolver==null && studentSolver==null) tab=0;
	session.setAttribute("UserInfo.tab",new Integer(tab));
%>

	<META http-equiv="refresh" content="<%=tab==0?(3600*4):10%>">
	<META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<LINK rel="stylesheet" type="text/css" href="../styles/timetabling.css" />
	<tt:hasProperty name="tmtbl.custom.css">
		<LINK rel="stylesheet" type="text/css" href="../%tmtbl.custom.css%" />
	</tt:hasProperty>
	<SCRIPT language="javascript" type="text/javascript" src="../tree.js"></SCRIPT>

</HEAD>

<BODY class="MenuBody" style="background-color:#e4e4e4;font-size:15px">
<FORM method="post" action="userinfo.jsp">
<INPUT type="hidden" name="hdnCallingScreen" value="userinfo.jsp">
<INPUT type="hidden" name="op" value="ChangeSemYr">
<%
	String userName="";
	String loginName="";
	String dept="";
	String role="";
	String yearTerm=" - ";
	String sessionStatus="";

	User user = Web.getUser(session);
	
	boolean chameleon = (tab==0 && 
		(user!=null && user.getRole().equals(Roles.ADMIN_ROLE))
		|| (session.getAttribute("hdnAdminAlias")!=null && session.getAttribute("hdnAdminAlias").toString().equals("1") ));
	
 	if (user==null)
 		loginName = "Not logged in.";
 	else {
 		loginName = user.getLogin();
 		userName = Constants.toInitialCase(user.getName(), "-".toCharArray());
 		
 		java.util.Vector depts = user.getDepartments();
 		for (int i=0; i<depts.size() ; i++) {
	 		dept += depts.elementAt(i).toString();
	 		if (i<depts.size()-1)
	 			dept += ",";
 		}
 		role = user.getRole();

 		Object objYearTerm = user.getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME);
 		if(objYearTerm!=null) {
	 		yearTerm = objYearTerm.toString();
	 		Object sessionId = user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
	 		if (sessionId!=null) {
		 		Session acadSession = Session.getSessionById((Long)sessionId);
		 		sessionStatus=acadSession.getStatusType().getLabel();
		 	}
	 	}
 	}
%>
<DIV align="left"><TABLE class="MenuBody" height='15' width="100%" border="0" cellspacing="0" cellpadding="2" style="border-top:2px solid #e4e4e4;">
	<TBODY>
		<TR align="left">
<% if (tab==0) { %>
			<TD valign="top" nowrap
				class="UserInfoTabSelect"
				onmouseover="this.style.cursor='pointer';"
				onclick="document.location='userinfo.jsp?tab=0';"
				title="Refresh Current User">
				&nbsp;Current User&nbsp;</TD>
<% } else { %> 
			<TD valign="top" nowrap
				onmouseover="this.style.cursor='pointer';" 
				class="UserInfoTabLeft" 
				onclick="document.location='userinfo.jsp?tab=0';"
				title="Show Current User">
				&nbsp;Current User&nbsp;</TD>
<% } %>
<% if (tab==0) { %>
	<% if (solver!=null) { %>
			<TD valign="top" nowrap 
				onmouseover="this.style.cursor='pointer';" 
				class="UserInfoTabRight"
				onclick="document.location='userinfo.jsp?tab=1';"
				title="Show Solver Status">
				&nbsp;Solver Status&nbsp;</TD>
	<% } else if (examSolver!=null) { %>
			<TD valign="top" nowrap 
				onmouseover="this.style.cursor='pointer';" 
				class="UserInfoTabRight"
				onclick="document.location='userinfo.jsp?tab=1';"
				title="Show Solver Status">
				&nbsp;Examination Solver&nbsp;</TD>
	<% } else if (studentSolver!=null) { %>
			<TD valign="top" nowrap 
				onmouseover="this.style.cursor='pointer';" 
				class="UserInfoTabRight"
				onclick="document.location='userinfo.jsp?tab=1';"
				title="Show Solver Status">
				&nbsp;Sectioning Solver&nbsp;</TD>
	<% } else { %>
			<TD valign="top" nowrap 
				class="UserInfoTabDisabled">
				<I>&nbsp;Solver Status&nbsp;</I></TD>
	<% } %>
<% } else { %>
			<TD valign="top" nowrap 
				onmouseover="this.style.cursor='pointer';" 
				class="UserInfoTabSelect"
				onclick="document.location='userinfo.jsp?tab=1';"
				title="Refresh Solver Status">
				&nbsp;<%=studentSolver!=null?"Sectioning Solver":examSolver!=null?"Examination Solver":"Solver Status"%>&nbsp;</TD>
<% } %>
			<TD width='50%' bgcolor="#e4e4e4" class="UserInfoTabBg">&nbsp;</TD>
		</TR>
	</TBODY>
</TABLE>
<TABLE class="UserInfo" width="100%" height='153'  border="0" cellspacing="0" cellpadding="2">
	<TBODY>
<% if (tab==0) { %>
		<TR align="left">
			<TD valign="top"><FONT color="#000000">&nbsp;Name</FONT></TD>
			<TD valign="top">
				<% if (chameleon) { %>
					<A href="../chameleon.do" target="__idContentFrame" title="Switch user">
				<% } %>
					<FONT color="#000040"><I><%=userName%></I></FONT>
				<% if (chameleon) { %>
					</A>
				<% } %>
			</TD>
		</TR>

		<TR align="left">
			<TD valign="top"><FONT color="#000000">&nbsp;Dept</FONT></TD>
			<TD valign="top"><FONT color="#000040"><I><%=dept%></I></FONT></TD>
		</TR>

		<TR align="left">
			<TD valign="top"><FONT color="#000000">&nbsp;Role</FONT></TD>
			<TD valign="top"><FONT color="#000040"><I><%=role%></I></FONT></TD>
		</TR>

		<TR align="left">
			<TD valign="top"><FONT color="#000000">&nbsp;Session</FONT></TD>
			<TD valign="top">
				<A href="../selectPrimaryRole.do?list=Y" target="__idContentFrame" title="Change academic session">
					<FONT color="#000040"><I><%=yearTerm%></I></FONT>
				</A>
			</TD>
		</TR>

		<TR align="left">
			<TD valign="top"><FONT color="#000000">&nbsp;Status</FONT></TD>
			<TD valign="top"><FONT color="#000040"><I><%=sessionStatus%></I></FONT></TD>
		</TR>

		<tt:propertyEquals name="tmtbl.userinfo.show_database" value="true">
			<TR align="left">
				<TD valign="top" height="15"><FONT color="#000000">&nbsp;Database</FONT></TD>
				<TD valign="top" height="15"><FONT color="#000040"><I><%=HibernateUtil.getDatabaseName()%></I></FONT></TD>
			</TR>
		</tt:propertyEquals>

		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Version</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040">
				<A href="../help/Release-Notes.xml" target="__new" title="View release notes"><FONT color="#000040"><I>
				<%=Constants.VERSION%>.<%=Constants.BLD_NUMBER.replaceAll("@build.number@","?")%></I></FONT></A>
			</TD>
		</TR>
		
		<TR align="left">
			<TD valign="top"><FONT color="#000000">&nbsp;Logged</FONT></TD>
			<TD valign="top"><FONT color="#000040"><I><%=new java.text.SimpleDateFormat("MM/dd/yy hh:mm a").format(new java.util.Date(session.getLastAccessedTime()))%></I></FONT></TD>
		</TR>

<% } else {
   try {
   Map progress = (studentSolver!=null?studentSolver.getProgress():examSolver!=null?examSolver.getProgress():solver.getProgress());
   DataProperties properties = (studentSolver!=null?studentSolver.getProperties():examSolver!=null?examSolver.getProperties():solver.getProperties());
   String progressStatus = (String)progress.get("STATUS");
   String progressPhase = (String)progress.get("PHASE");
   long progressCur = ((Long)progress.get("PROGRESS")).longValue();
   long progressMax = ((Long)progress.get("MAX_PROGRESS")).longValue();
   String version = (String)progress.get("VERSION");
   if (version==null || "-1".equals(version)) version = "N/A";
   if (version.indexOf("@build.number@")>=0)
   		version = version.replaceAll("@build.number@","?");
   double progressPercent = 100.0*((double)(progressCur<progressMax?progressCur:progressMax))/((double)progressMax);
   String runnerName = getName(properties.getProperty("General.OwnerPuid","N/A"));
   Long[] solverGroupId = properties.getPropertyLongArry("General.SolverGroupId",null);
   String ownerName = "";
   if (solverGroupId!=null) {
   	for (int i=0;i<solverGroupId.length;i++) {
		   if (i>0) ownerName += " & ";
	   	ownerName += getName((new SolverGroupDAO()).get(solverGroupId[i]));
   	}
   }
   if (examSolver!=null) ownerName = Exam.sExamTypes[examSolver.getExamType()];
   if (ownerName==null || ownerName.length()==0)
	   ownerName = "N/A";
   if (ownerName.equals("N/A"))
   	   ownerName = runnerName;
   if (runnerName.equals("N/A"))
       runnerName = ownerName;
   if (!ownerName.equals(runnerName))
       ownerName = runnerName+" as "+ownerName;
%>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Owner</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I><%=ownerName%></I></FONT></TD>
		</TR>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Host</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I><%=(studentSolver!=null?studentSolver.getHostLabel():examSolver!=null?examSolver.getHostLabel():solver.getHostLabel())%></I></FONT></TD>
		</TR>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Solver</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I><%=progressStatus%></I></FONT></TD>
		</TR>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Phase</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I><%=progressPhase%></I></FONT></TD>
		</TR>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Progress</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I>
<%		if (progressMax>0) { %>
					<%=(progressCur<progressMax?progressCur:progressMax)%> of <%=progressMax%>
					(<%=Web.format(progressPercent)%>%)
<% 		} %>
			</I></FONT></TD>
		</TR>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Version</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I><%=version%></I></FONT></TD>
		</TR>
		<TR align="left">
			<TD valign="top" height="15"><FONT color="#000000">&nbsp;Session</FONT></TD>
			<TD valign="top" height="15"><FONT color="#000040"><I><%=new SessionDAO().get(properties.getPropertyLong("General.SessionId",null)).getLabel()%></I></FONT></TD>
		</TR>
<% } catch (Exception e) { Debug.error(e); %>
<script language="JavaScript">
document.location='userinfo.jsp?tab=0';
</script>
<% }} %>
	</TBODY>
</TABLE></DIV>
</FORM>
</BODY>
</HTML>
