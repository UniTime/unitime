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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.action.SchedulingSubpartDetailAction" %>
<%@ page import="org.unitime.timetable.form.SchedulingSubpartEditForm" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.timetable.util.IdValue" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.WebClassListTableBuilder"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<jsp:directive.page import="org.unitime.timetable.webutil.JavascriptFunctions"/>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%
	// Get Form 
	String frmName = "SchedulingSubpartEditForm";
	SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) request.getAttribute(frmName);

	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
%>		
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmClearAllClassPreference() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('Do you really want to clear all class preferences?')) {
				SchedulingSubpartEditForm.confirm.value='n';
			}
		}
	// -->
</SCRIPT>

<tiles:importAttribute />
<html:form action="/schedulingSubpartDetail">
	<input type='hidden' name='confirm' value='y'/>
	<html:hidden property="schedulingSubpartId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<A title="Instructional Offering Detail (Alt+I)" accesskey="I" class="l7"
							href="instructionalOfferingDetail.do?op=view&io=<bean:write name="<%=frmName%>" property="instrOfferingId"/>">
							<bean:write name="<%=frmName%>" property="subjectArea"/>
							<bean:write name="<%=frmName%>" property="courseNbr"/>
						</A> :
						<bean:write name="<%=frmName%>" property="parentSubpart" />
						<bean:write name="<%=frmName%>" property="instructionalTypeLabel" />
					</tt:section-title>
					
					<logic:equal name="<%=frmName%>" property="editable" value="true">
						<html:submit property="op" styleClass="btn" accesskey="E" titleKey="title.editPrefsSubpart" >
							<bean:message key="button.editPrefsSubpart" />
						</html:submit> 
				
						&nbsp;
						<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.addDistPref" >
							<bean:message key="button.addDistPref" />
						</html:submit>
					</logic:equal>
					
					<!-- 
					&nbsp;
					<html:submit property="op" styleClass="btn" accesskey="C" title="Instructional Offering Detail">
						<bean:message key="button.backToInstrOffrDet" />
					</html:submit>
					-->

					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" accesskey="P" titleKey="title.previousSchedulingSubpart">
							<bean:message key="button.previousSchedulingSubpart" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" accesskey="N" titleKey="title.nextSchedulingSubpart">
							<bean:message key="button.nextSchedulingSubpart" />
						</html:submit> 
					</logic:notEmpty>

					&nbsp;
					<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
						<bean:write name="<%=frmName%>" property="schedulingSubpartId"/>
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

		<logic:notEmpty name="<%=frmName%>" property="managingDeptName">
			<TR>
				<TD>Manager:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="managingDeptName" />
				<TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="parentSubpartLabel">
			<TR>
				<TD>Parent Scheduling Subpart:</TD>
				<TD>
					<logic:empty name="<%=frmName%>" property="parentSubpartId">
						<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
					</logic:empty>
					<logic:notEmpty name="<%=frmName%>" property="parentSubpartId">
						<A href="schedulingSubpartDetail.do?ssuid=<bean:write name="<%=frmName%>" property="parentSubpartId"/>">
							<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
						</A>
					</logic:notEmpty>
				<TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD>Instructional Type:</TD>
			<TD>
				<logic:iterate scope="request" name="<%=ItypeDesc.ITYPE_ATTR_NAME%>" id="itp">
					<logic:equal name="<%=frmName%>" property="instructionalType" value="<%=((ItypeDesc)itp).getItype().toString()%>">
						<bean:write name="itp" property="desc"/>
					</logic:equal>
				</logic:iterate>
			<TD>
		</TR>
		<TR>
			<TD>Date Pattern:</TD>
			<TD>
				<logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" id="dp">
					<logic:equal name="<%=frmName%>" property="datePattern" value="<%=((IdValue)dp).getId().toString()%>">
						<bean:write name="dp" property="value" />
						<img style="cursor: pointer;" src="scripts/jscalendar/calendar_1.gif" border="0" onclick="window.open('user/dispDatePattern.jsp?id=<%=((IdValue)dp).getId()%>&subpart='+SchedulingSubpartEditForm.schedulingSubpartId.value,'datepatt','width=800,height=410,resizable=no,scrollbars=no,toolbar=no,location=no,directories=no,status=no,menubar=no,copyhistory=no');">
					</logic:equal>
				</logic:iterate>
			<TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="autoSpreadInTime" value="false">
			<TR>
				<TD>Automatic Spread In Time:</TD>
				<TD>
					<font color='red'><B>DISABLED</B></font>
				<TD>
			</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="sameItypeAsParent" value="false">
		<TR>
			<TD>Subpart Credit:</TD>
			<TD>
				<bean:write name="<%=frmName%>" property="creditText" />
			<TD>
		</TR>
		</logic:equal>
		
		<tt:last-change type='SchedulingSubpart'>
			<bean:write name="<%=frmName%>" property="schedulingSubpartId"/>
		</tt:last-change>		

<!-- Preferences -->
		<%
			boolean roomGroupDisabled = false;
			boolean roomPrefDisabled = false;
			boolean bldgPrefDisabled = false;
			boolean roomFeaturePrefDisabled = false;
			boolean distPrefDisabled = false;
			
			if (frm.getUnlimitedEnroll().booleanValue()) {
				roomGroupDisabled = true;
				bldgPrefDisabled = true;
				roomFeaturePrefDisabled = true;
			}
		%>
		<TR>
			<TD colspan="2" valign="middle">
				&nbsp;<BR>
				<tt:section-title>Preferences</tt:section-title>
			</TD>
		</TR>
		<%@ include file="preferencesDetail.jspf" %>

<!-- Classes -->
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>Classes</tt:section-title>
						<logic:equal name="<%=frmName%>" property="editable" value="true">
							<html:submit property="op" styleClass="btn" titleKey="title.clearAllClassPrefs"
								onclick="confirmClearAllClassPreference();displayLoading();">
								<bean:message key="button.clearAllClassPrefs" />
							</html:submit> 
						</logic:equal>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" valign="middle">
<% 
	if (frm.getInstrOfferingId() != null){
		WebClassListTableBuilder subpartClsTableBuilder = new WebClassListTableBuilder();
		subpartClsTableBuilder.setDisplayDistributionPrefs(false);
		subpartClsTableBuilder.htmlTableForSubpartClasses(
									session,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        WebSolver.getExamSolver(session),
				    		        new Long(frm.getSchedulingSubpartId()), 
				    		        Web.getUser(session), out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
%>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				<tt:exams type='SchedulingSubpart' add='true'>
					<bean:write name="<%=frmName%>" property="schedulingSubpartId"/>
				</tt:exams>
			</TD>
		</TR>


<!-- Buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<INPUT type="hidden" name="doit" value="Cancel">

				<logic:equal name="<%=frmName%>" property="editable" value="true">
					<html:submit property="op" styleClass="btn" accesskey="E" titleKey="title.editPrefsSubpart" >
						<bean:message key="button.editPrefsSubpart" />
					</html:submit> 
				
					&nbsp;
					<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.addDistPref" >
						<bean:message key="button.addDistPref" />
					</html:submit>
				</logic:equal>
				
				<!-- 
				&nbsp;
				<html:submit property="op" styleClass="btn" accesskey="C" title="Instructional Offering Detail">
					<bean:message key="button.backToInstrOffrDet" />
				</html:submit>
				-->

				<logic:notEmpty name="<%=frmName%>" property="previousId">
					&nbsp;
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousSchedulingSubpart">
						<bean:message key="button.previousSchedulingSubpart" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextSchedulingSubpart">
						<bean:message key="button.nextSchedulingSubpart" />
					</html:submit> 
				</logic:notEmpty>

				&nbsp;
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="schedulingSubpartId"/>
				</tt:back>
			</TD>
		</TR>

	
	</TABLE>
	
	
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(SchedulingSubpartDetailAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(SchedulingSubpartDetailAction.HASH_ATTR)%>";
	<% } %>
  	}
</SCRIPT>
