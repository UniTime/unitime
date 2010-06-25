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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.action.InstructorPrefEditAction" %>
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

<html:form action="instructorPrefEdit">
	<html:hidden property="instructorId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="deptCode"/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='name'/>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" accesskey="U" titleKey="title.updatePrefs" >
						<bean:message key="button.updatePrefs" />
					</html:submit> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="C" titleKey="title.clearInstrPrefs" >
						<bean:message key="button.clearInstrPrefs" />
					</html:submit> 
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" accesskey="P" titleKey="title.previousInstructorWithUpdate">
							<bean:message key="button.previousInstructor" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" accesskey="N" titleKey="title.nextInstructorWithUpdate">
							<bean:message key="button.nextInstructor" />
						</html:submit> 
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
						<bean:message key="button.returnToDetail" />
					</html:submit>
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

<!-- Preferences -->
		<%
			boolean roomGroupDisabled = false;
			boolean roomPrefDisabled = false;
			boolean bldgPrefDisabled = false;
			boolean roomFeaturePrefDisabled = false;
			boolean distPrefDisabled = false;
			boolean restorePrefsDisabled = true;
			boolean timePrefDisabled = false;
			boolean periodPrefDisabled = true;
		%>
		<%@ include file="preferencesEdit.jspf" %>
		
	</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(InstructorPrefEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(InstructorPrefEditAction.HASH_ATTR)%>";
	<% } %>
		self.focus();
  	}
	
</SCRIPT>
	
