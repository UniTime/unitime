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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.form.InstructorEditForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form 
	String frmName = "instructorEditForm";	
	InstructorEditForm frm = (InstructorEditForm) request.getAttribute(frmName);	
%>	
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
	// -->
</SCRIPT>

<html:form action="instructorDetail">
	<html:hidden property="instructorId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="deptCode"/>
	<html:hidden property="op2" value=""/>
	
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='name'/>
					</tt:section-title>
					
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
				<logic:equal name="<%=frmName%>" property="editable" value="true">
					<html:submit property="op" 
						styleClass="btn" accesskey="I" titleKey="title.editInstructorInfo" >
						<bean:message key="button.editInstructorInfo" />
					</html:submit> 
					<html:submit property="op" 
						styleClass="btn" accesskey="P" titleKey="title.editInstructorPref" >
						<bean:message key="button.editInstructorPref" />
					</html:submit> 
				</logic:equal>
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
			<TD>ID:</TD><TD> <bean:write name="<%=frmName%>" property="puId" /></TD>
		</TR>
		<TR>
			<TD>Career Account:</TD><TD> <bean:write name="<%=frmName%>" property="careerAcct" /></TD>
		</TR>
		<TR>
			<TD>Position:</TD><TD> <bean:write name="<%=frmName%>" property="posType" /></TD>
		</TR>
		<TR>
			<TD>Notes:</TD><TD> <bean:write name="<%=frmName%>" property="note" /></TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="ignoreDist" value="true">
			<TR>
				<TD>Ignore Too Far:</TD>
				<TD><font color='red'>ENABLED</font>&nbsp;&nbsp; -- <i>This instructor is allowed to teach two back-to-back classes that are too far away.
				</i></TD>
			</TR>
		</logic:equal>

<!-- Designator -->
		<TR>
			<TD colspan="2">
				&nbsp;<BR>
				<tt:section-header>
					<tt:section-title>
						Designator List
					</tt:section-title>
					
				<logic:equal name="<%=frmName%>" property="editable" value="true">
					<html:submit property="op" 
						styleClass="btn" accesskey="A" titleKey="title.addDesignator2" >
						<bean:message key="button.addDesignator2" />
					</html:submit> 
				</logic:equal>
					
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2" align="left">
				<TABLE width="50%" border="0" cellspacing="0" cellpadding="3">
					<%= request.getAttribute("designatorList") %>
				</TABLE>
			</TD>
		</TR>
		
<!-- Class Assignments -->
		<TR>
			<TD colspan="2">
				&nbsp;<BR>
				<DIV class="WelcomeRowHead">Class Assignments</DIV>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<table width="90%" border="0" cellspacing="0" cellpadding="3">
					<%if (request.getAttribute("classTable") != null ) {%>
						<%=request.getAttribute("classTable")%>
					<%} else { %>
					<TR><TD>&nbsp;</TD></TR>
					<%} %>					
				</table>
			</TD>
		</TR>
		
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
		<%
			boolean roomGroupDisabled = false;
			boolean roomPrefDisabled = false;
			boolean bldgPrefDisabled = false;
			boolean roomFeaturePrefDisabled = false;
			boolean distPrefDisabled = false;
			boolean restorePrefsDisabled = true;
		%>
		<%@ include file="preferencesDetail.jspf" %>
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
			<logic:equal name="<%=frmName%>" property="editable" value="true">
				<html:submit property="op" 
					styleClass="btn" accesskey="I" titleKey="title.editInstructorInfo" >
					<bean:message key="button.editInstructorInfo" />
				</html:submit> 
				<html:submit property="op" 
					styleClass="btn" accesskey="P" titleKey="title.editInstructorPref" >
					<bean:message key="button.editInstructorPref" />
				</html:submit> 
			</logic:equal>
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

