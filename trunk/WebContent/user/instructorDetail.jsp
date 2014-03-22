<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
--%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.form.InstructorEditForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "instructorEditForm";	
	InstructorEditForm frm = (InstructorEditForm) request.getAttribute(frmName);	
%>	
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<html:form action="instructorDetail">
	<html:hidden property="instructorId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="deptCode"/>
	<html:hidden property="op2" value=""/>
	<bean:define name='<%=frmName%>' property="instructorId" id="instructorId"/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='name'/>
					</tt:section-title>
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
					<html:submit property="op" 
						styleClass="btn" accesskey="I" titleKey="title.editInstructorInfo" >
						<bean:message key="button.editInstructorInfo" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
					<html:submit property="op" 
						styleClass="btn" accesskey="P" titleKey="title.editInstructorPref" >
						<bean:message key="button.editInstructorPref" />
					</html:submit>
				</sec:authorize> 
				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousInstructor">
						<bean:message key="button.previousInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextInstructor">
						<bean:message key="button.nextInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="instructorId"/>
				</tt:back>
				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		
		<TR>
			<TD>External Id:</TD><TD> <bean:write name="<%=frmName%>" property="puId" /></TD>
		</TR>
		<logic:notEmpty name="<%=frmName%>" property="careerAcct">
			<TR>
				<TD>Account Name:</TD><TD> <bean:write name="<%=frmName%>" property="careerAcct" /></TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="email">
			<TR>
				<TD>Email:</TD><TD> <bean:write name="<%=frmName%>" property="email" /></TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD>Position:</TD><TD> <bean:write name="<%=frmName%>" property="posType" /></TD>
		</TR>
		<logic:notEmpty name="<%=frmName%>" property="note">
			<TR>
				<TD valign="top">Note:</TD><TD> <bean:write name="<%=frmName%>" property="note" filter="false"/></TD>
			</TR>
		</logic:notEmpty>
		<logic:equal name="<%=frmName%>" property="ignoreDist" value="true">
			<TR>
				<TD>Ignore Too Far:</TD>
				<TD><font color='red'>ENABLED</font>&nbsp;&nbsp; -- <i>This instructor is allowed to teach two back-to-back classes that are too far away.
				</i></TD>
			</TR>
		</logic:equal>

<!-- Class Assignments -->
		<TR>
			<TD colspan="2">
				&nbsp;<BR>
				<DIV class="WelcomeRowHead">Class Assignments</DIV>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<table width="100%" border="0" cellspacing="0" cellpadding="3">
					<%if (request.getAttribute("classTable") != null ) {%>
						<%=request.getAttribute("classTable")%>
					<%} else { %>
					<TR><TD>&nbsp;</TD></TR>
					<%} %>					
				</table>
			</TD>
		</TR>
		
	<TR>
		<TD colspan="2">
			<tt:exams type='DepartmentalInstructor' add='false'>
				<bean:write name="<%=frmName%>" property="instructorId"/>
			</tt:exams>
		</TD>
	</TR>
	
	<logic:notEmpty scope="request" name="eventTable">
		<TR>
			<TD colspan="2">
				<br>
				<table width="100%" border="0" cellspacing="0" cellpadding="3">
					<bean:write name="eventTable" scope="request" filter="false"/>
				</table>
			</TD>
		</TR>
	</logic:notEmpty>
		
<!-- Preferences -->		
	<% if (frm.isDisplayPrefs()) { %>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<a style="border:0;background:0" accesskey="H" 
						title="Hide Instructor Preferences (ALT+H)"
						href="javascript:instructorEditForm.op2.value='Hide Instructor Preferences'; instructorEditForm.submit();">
					<img border='0' src='images/collapse_node_btn.gif' /></a>
					Preferences
				</tt:section-title>
			</TD>
		</TR>
		<jsp:include page="preferencesDetail.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
		</jsp:include>
	<% } else { %>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<a style="border:0;background:0" accesskey="S" 
						title="Show Instructor Preferences (ALT+S)"
						href="javascript:instructorEditForm.op2.value='Show Instructor Preferences'; instructorEditForm.submit();">
						<img border='0' src='images/expand_node_btn.gif' /></a>
					Preferences
					<!--  op2.value='Show Instructor Preferences';submit(); -->
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<br>
			</TD>
		</TR>
	<% } %>
	
		<tt:last-change type='DepartmentalInstructor'>
			<bean:write name="<%=frmName%>" property="instructorId"/>
		</tt:last-change>		
	
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR align="right">
			<TD valign="middle" colspan='2'>
<%--			
			<% if (frm.isDisplayPrefs()) { %>
				<html:submit property="op" 
					styleClass="btn" accesskey="H" titleKey="title.hidePrefs" >
					<bean:message key="button.hidePrefs" />
				</html:submit> 
			<% } else {%>
				<html:submit property="op" 
					styleClass="btn" accesskey="S" titleKey="title.displayPrefs" >
					<bean:message key="button.displayPrefs" />
				</html:submit> 
			<% } %>
--%>
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
					<html:submit property="op" 
						styleClass="btn" accesskey="I" titleKey="title.editInstructorInfo" >
						<bean:message key="button.editInstructorInfo" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
					<html:submit property="op" 
						styleClass="btn" accesskey="P" titleKey="title.editInstructorPref" >
						<bean:message key="button.editInstructorPref" />
					</html:submit>
				</sec:authorize> 
				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousInstructor">
						<bean:message key="button.previousInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextInstructor">
						<bean:message key="button.nextInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="instructorId"/>
				</tt:back>
<%--
				<html:submit property="op" 
					styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
					<bean:message key="button.returnToDetail" />
				</html:submit>
--%>
			</TD>
		</TR>
	
	</TABLE>
</html:form>

