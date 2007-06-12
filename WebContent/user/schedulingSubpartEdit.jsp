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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.action.SchedulingSubpartEditAction" %>
<%@ page import="org.unitime.timetable.form.SchedulingSubpartEditForm" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.CourseCreditType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditUnitType" %>
<%@ page import="org.unitime.timetable.model.FixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.ArrangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableFixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableRangeCreditUnitConfig" %>
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
	// -->
</SCRIPT>
		
<tiles:importAttribute />
<html:form action="/schedulingSubpartEdit" focus="timePattern" >
	<html:hidden property="schedulingSubpartId"/>
	<html:hidden property="creditText"/>
	<html:hidden property="subpartCreditEditAllowed"/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name="<%=frmName%>" property="subjectArea" />&nbsp;
						<bean:write name="<%=frmName%>" property="courseNbr" /> : 
						<bean:write name="<%=frmName%>" property="parentSubpart" />
						<B><bean:write name="<%=frmName%>" property="instructionalTypeLabel" /></B>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" accesskey="U" titleKey="title.updatePrefs" >
						<bean:message key="button.updatePrefs" />
					</html:submit> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="C" titleKey="title.clearSubpartPrefs">
						<bean:message key="button.clearSubpartPrefs" />
					</html:submit> 
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousSchedulingSubpartWithUpdate">
							<bean:message key="button.previousSchedulingSubpart" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" accesskey="N" titleKey="title.nextSchedulingSubpartWithUpdate">
							<bean:message key="button.nextSchedulingSubpart" />
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

		<logic:notEmpty name="<%=frmName%>" property="managingDeptName">
			<TR>
				<TD>Manager:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="managingDeptName" />
				<TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="parentSubpartId">
			<TR>
				<TD>Parent Scheduling Subpart:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
				<TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD>Instructional Type:</TD>
			<TD>				 
				<html:select style="width:200;" property="instructionalType">					
					<html:options collection="<%=ItypeDesc.ITYPE_ATTR_NAME%>" property="itype" labelProperty="desc" />
				</html:select>
				<!-- 
				<logic:iterate scope="request" name="<%=ItypeDesc.ITYPE_ATTR_NAME%>" id="itp">
					<logic:equal name="<%=frmName%>" property="instructionalType" value="<%=((ItypeDesc)itp).getItype().toString()%>">
						<bean:write name="itp" property="desc"/>
					</logic:equal>
				</logic:iterate>
				 -->
			</TD>
		</TR>
		<TR>
			<TD>Date Pattern:</TD>
			<TD>
				<html:select style="width:200;" property="datePattern">
					<html:options collection="<%=org.unitime.timetable.model.DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value" />
				</html:select>
				<img style="cursor: pointer;" src="scripts/jscalendar/calendar_1.gif" border="0" onclick="window.open('user/dispDatePattern.jsp?id='+SchedulingSubpartEditForm.datePattern.value+'&subpart='+SchedulingSubpartEditForm.schedulingSubpartId.value,'datepatt','width=800,height=410,resizable=no,scrollbars=no,toolbar=no,location=no,directories=no,status=no,menubar=no,copyhistory=no');">
			<TD>
		</TR>
		<TR>
			<TD>Automatic Spread In Time:</TD>
			<TD>
				<html:checkbox property="autoSpreadInTime"/>
			<TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="sameItypeAsParent" value="false">
		<logic:equal name="<%=frmName%>" property="subpartCreditEditAllowed" value="true">
		<TR>
			<TD>Credit:</TD>
			<TD>
				<html:select style="width:200;" property="creditFormat" onchange="<%= "if (this.value == '" + FixedCreditUnitConfig.CREDIT_FORMAT + "') { document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true } else if (this.value == '" + ArrangeCreditUnitConfig.CREDIT_FORMAT + "'){document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '" + VariableFixedCreditUnitConfig.CREDIT_FORMAT + "') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '" + VariableRangeCreditUnitConfig.CREDIT_FORMAT + "') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = false} else {document.forms[0].creditType.disabled = true; document.forms[0].creditUnitType.disabled = true; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true}"%>">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=org.unitime.timetable.model.CourseCreditFormat.COURSE_CREDIT_FORMAT_ATTR_NAME%>" property="reference" labelProperty="label"/>
				</html:select>
			<TD>
		</TR>
		<TR>
			<TD> &nbsp;</TD>
			<TD>
				<table>
				<tr>
				<td nowrap>Credit Type: </td>
				<td>
				<html:select style="width:200;" property="creditType" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0)?false:true%>">
					<html:options collection="<%=CourseCreditType.COURSE_CREDIT_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
				</html:select>
				</td>
				</tr>
				<tr>
				<td nowrap>Credit Unit Type: </td>
				<td>
				<html:select style="width:200;" property="creditUnitType" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0)?false:true%>">
					<html:options collection="<%=CourseCreditUnitType.COURSE_CREDIT_UNIT_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label" />
				</html:select>
				</td>
				</tr>
				<tr>
				<td nowrap>Units: </td>
				<td>
				<html:text property="units" maxlength="4" size="4" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0 && !frm.getCreditFormat().equals(ArrangeCreditUnitConfig.CREDIT_FORMAT))?false:true%>"/>
				</td>
				</tr>
				<tr>
				<td nowrap>Max Units: </td>
				<td>
				<html:text property="maxUnits" maxlength="4" size="4" disabled="<%=(frm.getCreditFormat() != null && (frm.getCreditFormat().equals(VariableFixedCreditUnitConfig.CREDIT_FORMAT) || frm.getCreditFormat().equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT)))?false:true%>"/>
				</td>
				</tr>
				<tr>
				<td nowrap>Fractional Increments Allowed: </td>
				<td>
				<html:checkbox property="fractionalIncrementsAllowed"disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT))?false:true%>"/>
				</td>
				</tr>
				</table>
			<TD>
		</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="subpartCreditEditAllowed" value="false">
		<TR>
		
			<TD>Credit:</TD>
			<TD>
				<bean:write name="<%=frmName%>" property="creditText" />
					<html:hidden property="creditFormat"/>
					<html:hidden property="creditType"/>
					<html:hidden property="creditUnitType"/>
					<html:hidden property="units"/>
					<html:hidden property="maxUnits"/>				
					<html:hidden property="fractionalIncrementsAllowed"/>				
			<TD>
			
		</TR>
		</logic:equal>
		</logic:equal>
<!-- Preferences -->
		<%
			boolean roomGroupDisabled = false;
			boolean roomPrefDisabled = false;
			boolean bldgPrefDisabled = false;
			boolean roomFeaturePrefDisabled = false;
			boolean distPrefDisabled = true;
			boolean restorePrefsDisabled = false;
			
			if (frm.getUnlimitedEnroll().booleanValue()) {
				roomGroupDisabled = true;
				bldgPrefDisabled = true;
				roomFeaturePrefDisabled = true;
			}
		%>
		<%@ include file="preferencesEdit.jspf" %>

	</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(SchedulingSubpartEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(SchedulingSubpartEditAction.HASH_ATTR)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>