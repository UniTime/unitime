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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.action.SchedulingSubpartDetailAction" %>
<%@ page import="org.unitime.timetable.form.SchedulingSubpartEditForm" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.timetable.util.IdValue" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.timetable.webutil.WebClassListTableBuilder"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<jsp:directive.page import="org.unitime.timetable.webutil.JavascriptFunctions"/>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<tt:session-context/>
<%
	// Get Form 
	String frmName = "SchedulingSubpartEditForm";
	SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) request.getAttribute(frmName);
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>		

<loc:bundle name="CourseMessages">
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmClearAllClassPreference() {
			if (jsConfirm!=null && !jsConfirm) return;

			if (!confirm('<%=MSG.confirmClearAllClassPreferences()%>')) {
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

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<A title="<%=MSG.titleInstructionalOfferingDetail(MSG.accessInstructionalOfferingDetail()) %>" 
							accesskey="<%=MSG.accessInstructionalOfferingDetail() %>" class="l8"
							href="instructionalOfferingDetail.do?op=view&io=<bean:write name="<%=frmName%>" property="instrOfferingId"/>">
							<bean:write name="<%=frmName%>" property="subjectArea"/>
							<bean:write name="<%=frmName%>" property="courseNbr"/> - <bean:write name="<%=frmName%>" property="courseTitle"/></A>:
						<bean:write name="<%=frmName%>" property="parentSubpart" />
						<bean:write name="<%=frmName%>" property="instructionalTypeLabel" />
					</tt:section-title>
					
					<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEdit')">
						<html:submit property="op" styleClass="btn" 
							accesskey="<%=MSG.accessEditSubpart() %>" 
							title="<%=MSG.titleEditSubpart(MSG.accessEditSubpart()) %>" >
							<loc:message name="actionEditSubpart" />
						</html:submit>
					</sec:authorize> 
				
					<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'DistributionPreferenceSubpart')">
						&nbsp;
						<html:submit property="op" styleClass="btn" 
							accesskey="<%=MSG.accessAddDistributionPreference() %>" 
							title="<%=MSG.titleAddDistributionPreference(MSG.accessAddDistributionPreference()) %>" >
							<loc:message name="actionAddDistributionPreference" />
						</html:submit>
					</sec:authorize>
					
					<!-- for deletion
					&nbsp;
					<html:submit property="op" styleClass="btn" accesskey="C" title="Instructional Offering Detail">
						<bean:message key="button.backToInstrOffrDet" />
					</html:submit>
					-->

					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessPreviousSubpart()%>"
								title="<%=MSG.titlePreviousSubpart(MSG.accessPreviousSubpart()) %>">
							<loc:message name="actionPreviousSubpart" />
						</html:submit>
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextSubpart()%>" 
							title="<%=MSG.titleNextSubpart(MSG.accessNextSubpart())%>">
							<loc:message name="actionNextSubpart" />
						</html:submit> 
					</logic:notEmpty>

					&nbsp;
					<tt:back styleClass="btn" 
							name="<%=MSG.actionBackSubpartDetail()%>" 
							title="<%=MSG.titleBackSubpartDetail(MSG.accessBackSubpartDetail())%>" 
							accesskey="<%=MSG.accessBackSubpartDetail() %>"  
						type="PreferenceGroup">
						<bean:write name="<%=frmName%>" property="schedulingSubpartId"/>
					</tt:back>
				</tt:section-header>
			</TD>
		</TR>		

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errorsSubpartDetail"/></U></B><BR>
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
				<TD><loc:message name="filterManager"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="managingDeptName" />
				</TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="parentSubpartLabel">
			<TR>
				<TD><loc:message name="propertyParentSchedulingSubpart"/></TD>
				<TD>
					<logic:empty name="<%=frmName%>" property="parentSubpartId">
						<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
					</logic:empty>
					<logic:notEmpty name="<%=frmName%>" property="parentSubpartId">
						<A href="schedulingSubpartDetail.do?ssuid=<bean:write name="<%=frmName%>" property="parentSubpartId"/>">
							<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
						</A>
					</logic:notEmpty>
				</TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD> <loc:message name="filterInstructionalType"/> </TD>
			<TD>
				<logic:iterate scope="request" name="<%=ItypeDesc.ITYPE_ATTR_NAME%>" id="itp">
					<logic:equal name="<%=frmName%>" property="instructionalType" value="<%=((ItypeDesc)itp).getItype().toString()%>">
						<bean:write name="itp" property="desc"/>
					</logic:equal>
				</logic:iterate>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" id="dp">
					<logic:equal name="<%=frmName%>" property="datePattern" value="<%=((IdValue)dp).getId().toString()%>">
						<bean:write name="dp" property="value" />
						<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('Preview of <%=((IdValue)dp).getValue()%>', 'user/dispDatePattern.jsp?id=<%=((IdValue)dp).getId()%>&subpart='+SchedulingSubpartEditForm.schedulingSubpartId.value,'840','520');">
					</logic:equal>
				</logic:iterate>
			</TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="autoSpreadInTime" value="false">
			<TR>
				<TD><loc:message name="propertyAutomaticSpreadInTime"/></TD>
				<TD>
					<font color='red'><B>DISABLED</B></font><i> -- Classes of this subpart may be timetabled during overlapping times.</i>
				</TD>
			</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="studentAllowOverlap" value="true">
		<TR>
			<TD><loc:message name="propertyStudentOverlaps"/></TD>
			<TD>
				<font color='red'><B>ENABLED</B></font><i> -- Students are allowed to take classes from this subpart even when they overlap with other classes.</i>
			</TD>
		</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="sameItypeAsParent" value="false">
		<TR>
			<TD><loc:message name="propertySubpartCredit"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="creditText" />
			</TD>
		</TR>
		</logic:equal>
		
		<tt:last-change type='SchedulingSubpart'>
			<bean:write name="<%=frmName%>" property="schedulingSubpartId"/>
		</tt:last-change>		

<!-- Preferences -->
		<TR>
			<TD colspan="2" valign="middle">
				&nbsp;<BR>
				<tt:section-title><loc:message name="sectionTitlePreferences"/></tt:section-title>
			</TD>
		</TR>
		<logic:equal value="true" name="<%=frmName%>" property="unlimitedEnroll">
			<jsp:include page="preferencesDetail.jspf">
				<jsp:param name="frmName" value="<%=frmName%>"/>
				<jsp:param name="bldgPref" value="false"/>
				<jsp:param name="roomFeaturePref" value="false"/>
				<jsp:param name="roomGroupPref" value="false"/>
			</jsp:include>
		</logic:equal>
		<logic:notEqual value="true" name="<%=frmName%>" property="unlimitedEnroll">
			<jsp:include page="preferencesDetail.jspf">
				<jsp:param name="frmName" value="<%=frmName%>"/>
			</jsp:include>
		</logic:notEqual>

<!-- Classes -->
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectionTitleClasses"/></tt:section-title>
						<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartDetailClearClassPreferences')">
							<html:submit property="op" styleClass="btn" 
								title="<%=MSG.titleClearClassPreferencesOnSubpart() %>"
								onclick="confirmClearAllClassPreference();displayLoading();">
								<loc:message name="actionClearClassPreferencesOnSubpart" />
							</html:submit>
						</sec:authorize> 
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
									sessionContext,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        WebSolver.getExamSolver(session),
				    		        new Long(frm.getSchedulingSubpartId()), 
				    		        out,
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

				<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEdit')">
						<html:submit property="op" styleClass="btn" 
							accesskey="<%=MSG.accessEditSubpart() %>" 
							title="<%=MSG.titleEditSubpart(MSG.accessEditSubpart()) %>" >
							<loc:message name="actionEditSubpart" />
						</html:submit> 
				</sec:authorize>
				
				<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'DistributionPreferenceSubpart')">
					&nbsp;
						<html:submit property="op" styleClass="btn" 
							accesskey="<%=MSG.accessAddDistributionPreference() %>" 
							title="<%=MSG.titleAddDistributionPreference(MSG.accessAddDistributionPreference()) %>" >
							<loc:message name="actionAddDistributionPreference" />
						</html:submit>
				</sec:authorize>
				
				<!-- for deletion
				&nbsp;
				<html:submit property="op" styleClass="btn" accesskey="C" title="Instructional Offering Detail">
					<bean:message key="button.backToInstrOffrDet" />
				</html:submit>
				-->

				<logic:notEmpty name="<%=frmName%>" property="previousId">
					&nbsp;
						<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessPreviousSubpart()%>"
								title="<%=MSG.titlePreviousSubpart(MSG.accessPreviousSubpart()) %>">
							<loc:message name="actionPreviousSubpart" />
						</html:submit>
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextSubpart()%>" 
							title="<%=MSG.titleNextSubpart(MSG.accessNextSubpart())%>">
							<loc:message name="actionNextSubpart" />
						</html:submit> 
				</logic:notEmpty>

				&nbsp;
					<tt:back styleClass="btn" 
							name="<%=MSG.actionBackSubpartDetail()%>" 
							title="<%=MSG.titleBackSubpartDetail(MSG.accessBackSubpartDetail())%>" 
							accesskey="<%=MSG.accessBackSubpartDetail() %>"  
						type="PreferenceGroup">
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

</loc:bundle>