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
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ page import="org.unitime.timetable.model.OfferingConsentType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditUnitType" %>
<%@ page import="org.unitime.timetable.model.FixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.ArrangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableFixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableRangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ page import="org.unitime.timetable.form.CourseOfferingEditForm" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.commons.User" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />
<%
	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
	User user = Web.getUser(session);
%>

<html:form action="/courseOfferingEdit" styleClass="FormWithNoPadding">
	<html:hidden property="instrOfferingId"/>
	<html:hidden property="courseOfferingId"/>
	<html:hidden property="subjectAreaId"/>
	<html:hidden property="isControl"/>
	<html:hidden property="courseName"/>
	<html:hidden property="ioNotOffered"/>
	
<% if (user==null
		|| !user.getRole().equals(Roles.ADMIN_ROLE)) { %>
	<html:hidden property="courseNbr"/>
<% } %>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
							<A  title="Back to Instructional Offering List (Alt+I)"
								accesskey="I"
								class="l7"
								href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="courseOfferingEditForm" property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="courseOfferingEditForm" property="instrOfferingId" />"
							><bean:write name="courseOfferingEditForm" property="courseName" /></A>
					</tt:section-title>

					<bean:define id="instrOfferingId">
						<bean:write name="courseOfferingEditForm" property="instrOfferingId" />
					</bean:define>

					<logic:equal name="courseOfferingEditForm" property="ioNotOffered" value="false">
						<html:submit property="op"
							styleClass="btn" accesskey="U" titleKey="title.updateCourseOffering">
							<bean:message key="button.updateCourseOffering" />
						</html:submit>
					</logic:equal>
					
					<html:button property="op"
						styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail"
						onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
						<bean:message key="button.backToInstrOffrDetail" />
					</html:button>

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

<% if (user!=null
		&& user.getRole().equals(Roles.ADMIN_ROLE)) { %>
		<TR>
			<TD>Course Number: </TD>
			<TD>
				<html:text property="courseNbr" size="4" maxlength="4" />
			</TD>
		</TR>
<% } %>
		<TR>
			<TD>Title: </TD>
			<TD>
				<html:text property="title" size="60" maxlength="90" />
			</TD>
		</TR>

		<TR>
			<TD valign="top">Schedule of Classes Note: </TD>
			<TD>
				<html:textarea property="scheduleBookNote" rows="4" cols="57" />
			</TD>
		</TR>

		<logic:notEmpty name="courseOfferingEditForm" property="consent">
		<TR>
			<TD valign="top">Consent: </TD>
			<TD>
				<html:select name="courseOfferingEditForm" property="consent">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=OfferingConsentType.CONSENT_TYPE_ATTR_NAME%>" labelProperty="label" property="uniqueId" />
				</html:select>
			</TD>
		</TR>
		</logic:notEmpty>

		<logic:notEmpty name="courseOfferingEditForm" property="designatorRequired">
		<TR>
			<TD valign="top">Designator Required: </TD>
			<TD>
				<html:checkbox name="courseOfferingEditForm" property="designatorRequired" />
			</TD>
		</TR>
		</logic:notEmpty>
		<logic:notEmpty name="courseOfferingEditForm" property="designatorRequired">
		<%
		// Get Form
		String frmName = "courseOfferingEditForm";
		CourseOfferingEditForm frm = (CourseOfferingEditForm) request.getAttribute(frmName);
		%>
		<TR>
			<TD>Credit:</TD>
			<TD>
				<html:select style="width:200;" property="creditFormat" onchange="<%= \"if (this.value == '\" + FixedCreditUnitConfig.CREDIT_FORMAT + \"') { document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true } else if (this.value == '\" + ArrangeCreditUnitConfig.CREDIT_FORMAT + \"'){document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '\" + VariableFixedCreditUnitConfig.CREDIT_FORMAT + \"') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '\" + VariableRangeCreditUnitConfig.CREDIT_FORMAT + \"') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = false} else {document.forms[0].creditType.disabled = true; document.forms[0].creditUnitType.disabled = true; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true}\"%>">
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
		</logic:notEmpty>

		<logic:equal name="courseOfferingEditForm" property="allowDemandCourseOfferings" value="true">
			<TR>
				<TD>Take Course Demands from Offering: </TD>
				<TD>
					<html:select
						name="courseOfferingEditForm"
						property="demandCourseOfferingId"
						onfocus="setUp();"
						onkeypress="return selectSearch(event, this);"
						onkeydown="return checkKey(event, this);" >
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"> </html:option>
						<html:options collection="<%=CourseOffering.CRS_OFFERING_LIST_ATTR_NAME%>" property="uniqueId" labelProperty="courseName" />
					</html:select>
				</TD>
			</TR>
		</logic:equal>


<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<logic:equal name="courseOfferingEditForm" property="ioNotOffered" value="false">
					<html:submit property="op"
						styleClass="btn" accesskey="U" titleKey="title.updateCourseOffering">
						<bean:message key="button.updateCourseOffering" />
					</html:submit>
				</logic:equal>
				
				<html:button property="op"
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail"
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>
			</TD>
		</TR>

	</TABLE>
</html:form>
