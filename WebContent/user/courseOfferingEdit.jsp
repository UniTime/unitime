<%--
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
--%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ page import="org.unitime.timetable.model.OfferingConsentType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditUnitType" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ page import="org.unitime.timetable.model.FixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.ArrangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableFixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableRangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.form.CourseOfferingEditForm" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />
<loc:bundle name="CourseMessages">
<tt:session-context/>
<%
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>

<html:form action="/courseOfferingEdit" styleClass="FormWithNoPadding">
	<html:hidden property="instrOfferingId"/>
	<html:hidden property="courseOfferingId"/>
	<html:hidden property="add"/>
	<html:hidden property="isControl"/>
	<html:hidden property="courseName"/>
	<html:hidden property="ioNotOffered"/>
	<html:hidden property="defaultTeachingResponsibilityId"/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<logic:notEqual name="courseOfferingEditForm" property="add" value="true">
							<A  title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>"
								accesskey="<%=MSG.accessBackToIOList() %>"
								class="l8"
								href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="courseOfferingEditForm" property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="courseOfferingEditForm" property="instrOfferingId" />"
							><bean:write name="courseOfferingEditForm" property="courseName" /></A>
						</logic:notEqual>
					</tt:section-title>

					<logic:notEqual name="courseOfferingEditForm" property="add" value="true">
						<html:submit property="op"
							styleClass="btn" 
							accesskey="<%=MSG.accessUpdateCourseOffering() %>" 
							title="<%=MSG.titleUpdateCourseOffering(MSG.accessUpdateCourseOffering()) %>">
							<loc:message name="actionUpdateCourseOffering" />
						</html:submit>
					</logic:notEqual>
				
					<logic:equal name="courseOfferingEditForm" property="add" value="true">
						<html:submit property="op"
							styleClass="btn" 
							accesskey="<%=MSG.accessSaveCourseOffering() %>" 
							title="<%=MSG.titleSaveCourseOffering(MSG.accessSaveCourseOffering()) %>">
							<loc:message name="actionSaveCourseOffering" />
						</html:submit>
					</logic:equal>
										
					<logic:notEqual name="courseOfferingEditForm" property="add" value="true">
						<bean:define id="instrOfferingId">
							<bean:write name="courseOfferingEditForm" property="instrOfferingId" />
						</bean:define>
						<html:button property="op"
							styleClass="btn" 
							accesskey="<%=MSG.accessBackToIODetail() %>" 
							title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>"
							onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
							<loc:message name="actionBackToIODetail" />
						</html:button>
					</logic:notEqual>
					
					<logic:equal name="courseOfferingEditForm" property="add" value="true">
						<html:button property="op"
							styleClass="btn" 
							accesskey="<%=MSG.accessBackToIOList()%>" 
							title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>"
							onclick="document.location.href='instructionalOfferingShowSearch.do';">
							<loc:message name="actionBackToIOList" />
						</html:button>
					</logic:equal>

				</tt:section-header>

			</TD>
		</TR>

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errors"/></U></B><BR>
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
		
	<logic:equal name="courseOfferingEditForm" property="add" value="true">
		<TR>
			<TD><loc:message name="filterSubject"/> </TD>
			<TD>
				<html:select
					name="courseOfferingEditForm"
					property="subjectAreaId"
					onchange="submit();"
					styleId="subjectId"
					>
					<html:options collection="subjects" property="uniqueId" labelProperty="subjectAreaAbbreviation" />
				</html:select>
			</TD>
		</TR>
	</logic:equal>
	<logic:notEqual name="courseOfferingEditForm" property="add" value="true">
		<html:hidden property="subjectAreaId" styleId="subjectId"/>
	</logic:notEqual>
	
		
	<sec:authorize access="(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
						(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<TR>
			<TD><loc:message name="filterCourseNumber"/> </TD>
			<TD>
				<html:text property="courseNbr" size="40" maxlength="40" styleId="course" />
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyCourseTitle"/> </TD>
			<TD>
				<html:text property="title" size="100" maxlength="200" />
			</TD>
		</TR>
		<tt:propertyEquals name="unitime.course.editExternalIds" value="true">
			<TR>
				<TD><loc:message name="propertyExternalId"/> </TD>
				<TD>
					<html:text property="externalId" maxlength="40" size="20"/>
				</TD>
			</TR>
		</tt:propertyEquals>
		<tt:propertyNotEquals name="unitime.course.editExternalIds" value="true">
			<logic:notEmpty name="courseOfferingEditForm" property="externalId">
				<TR>
					<TD><loc:message name="propertyExternalId"/> </TD>
					<TD>
						<bean:write name="courseOfferingEditForm" property="externalId"/>
					</TD>
				</TR>
			</logic:notEmpty>
			<html:hidden property="externalId"/>
		</tt:propertyNotEquals>
	</sec:authorize>
	<sec:authorize access="!(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and
							!(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<html:hidden property="courseNbr" styleId="course"/>
		<logic:notEmpty name="courseOfferingEditForm" property="title">
			<TR>
				<TD><loc:message name="propertyCourseTitle"/> </TD>
				<TD>
					<bean:write name="courseOfferingEditForm" property="title"/>				
				</TD>
			</TR>
		</logic:notEmpty>
		<html:hidden property="title"/>
		<logic:notEmpty name="courseOfferingEditForm" property="externalId">
			<TR>
				<TD><loc:message name="propertyExternalId"/> </TD>
				<TD>
					<bean:write name="courseOfferingEditForm" property="externalId"/>
				</TD>
			</TR>
		</logic:notEmpty>
		<html:hidden property="externalId"/>
	</sec:authorize>
	
	<logic:notEmpty scope="request" name="courseTypes">
		<sec:authorize access="(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
							(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<TR>
				<TD><loc:message name="propertyCourseType"/></TD>
				<TD>
					<html:select
						name="courseOfferingEditForm"
						property="courseTypeId">
						<html:option value=""></html:option>
						<html:options collection="courseTypes" property="uniqueId" labelProperty="label" />
					</html:select>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and
								!(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<html:hidden property="courseTypeId"/>
			<logic:notEmpty name="courseOfferingEditForm" property="courseTypeId">
				<logic:iterate name="courseTypes" scope="request" id="type" type="org.unitime.timetable.model.CourseType">
					<logic:equal name="courseOfferingEditForm" property="courseTypeId" value="<%=type.getUniqueId().toString()%>">
						<TR>
							<TD><loc:message name="propertyCourseType"/></TD>
							<TD><bean:write name="type" property="label"/></TD>
						</TR>
					</logic:equal>
				</logic:iterate>
			</logic:notEmpty>
		</sec:authorize>
	</logic:notEmpty>

	<sec:authorize access="(not #courseOfferingEditForm.add and (hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering') or
							 hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOfferingNote')))
						or (#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<TR>
			<TD valign="top"><loc:message name="propertyScheduleOfClassesNote"/> </TD>
			<TD>
				<html:textarea property="scheduleBookNote" rows="4" cols="57" />
			</TD>
		</TR>
	</sec:authorize>
	<sec:authorize access="!(not #courseOfferingEditForm.add and (hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering') or
							 hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOfferingNote')))
						and !(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<logic:notEmpty name="courseOfferingEditForm" property="scheduleBookNote">
			<TR>
				<TD valign="top"><loc:message name="propertyScheduleOfClassesNote"/> </TD>
				<TD>
					<bean:write name="courseOfferingEditForm" property="scheduleBookNote" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>
		<html:hidden property="scheduleBookNote"/>
	</sec:authorize>
	
		
	<sec:authorize access="(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
						(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<TR>
			<TD valign="top"><loc:message name="propertyConsent" /></TD>
			<TD>
				<html:select name="courseOfferingEditForm" property="consent">
					<html:option value="-1"><loc:message name="noConsentRequired"/></html:option>
					<html:options collection="<%=OfferingConsentType.CONSENT_TYPE_ATTR_NAME%>" labelProperty="label" property="uniqueId" />
				</html:select>
			</TD>
		</TR>

		<%
		// Get Form
		String frmName = "courseOfferingEditForm";
		CourseOfferingEditForm frm = (CourseOfferingEditForm) request.getAttribute(frmName);
		%>
		<TR>
			<TD><loc:message name="propertyCredit"/></TD>
			<TD>
				<html:select style="width:200px;" property="creditFormat" onchange="<%= \"if (this.value == '\" + FixedCreditUnitConfig.CREDIT_FORMAT + \"') { document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true } else if (this.value == '\" + ArrangeCreditUnitConfig.CREDIT_FORMAT + \"'){document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '\" + VariableFixedCreditUnitConfig.CREDIT_FORMAT + \"') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '\" + VariableRangeCreditUnitConfig.CREDIT_FORMAT + \"') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = false} else {document.forms[0].creditType.disabled = true; document.forms[0].creditUnitType.disabled = true; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true}\"%>">
					<loc:bundle name="ConstantsMessages" id="CONST">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><loc:message name="select" id="CONST"/></html:option>
					</loc:bundle>
					<html:options collection="<%=org.unitime.timetable.model.CourseCreditFormat.COURSE_CREDIT_FORMAT_ATTR_NAME%>" property="reference" labelProperty="label"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD> &nbsp;</TD>
			<TD>
				<table>
				<tr>
				<td nowrap><loc:message name="propertyCreditType"/> </td>
				<td>
				<html:select style="width:200px;" property="creditType" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0)?false:true%>">
					<html:options collection="<%=CourseCreditType.COURSE_CREDIT_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
				</html:select>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyCreditUnitType"/></td>
				<td>
				<html:select style="width:200px;" property="creditUnitType" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0)?false:true%>">
					<html:options collection="<%=CourseCreditUnitType.COURSE_CREDIT_UNIT_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label" />
				</html:select>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyUnits"/> </td>
				<td>
				<html:text property="units" maxlength="4" size="4" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0 && !frm.getCreditFormat().equals(ArrangeCreditUnitConfig.CREDIT_FORMAT))?false:true%>"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyMaxUnits"/></td>
				<td>
				<html:text property="maxUnits" maxlength="4" size="4" disabled="<%=(frm.getCreditFormat() != null && (frm.getCreditFormat().equals(VariableFixedCreditUnitConfig.CREDIT_FORMAT) || frm.getCreditFormat().equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT)))?false:true%>"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyFractionalIncrementsAllowed"/></td>
				<td>
				<html:checkbox property="fractionalIncrementsAllowed" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT))?false:true%>"/>
				</td>
				</tr>
				</table>
			</TD>
		</TR>

		<logic:equal name="courseOfferingEditForm" property="allowDemandCourseOfferings" value="true">
			<logic:notEmpty name="<%=CourseOffering.CRS_OFFERING_LIST_ATTR_NAME%>" scope="request">
			<TR>
				<TD><loc:message name="propertyTakeCourseDemandsFromOffering"/> </TD>
				<TD>
					<html:select
						name="courseOfferingEditForm"
						property="demandCourseOfferingId">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"> </html:option>
						<html:options collection="<%=CourseOffering.CRS_OFFERING_LIST_ATTR_NAME%>" property="uniqueId" labelProperty="courseNameWithTitle" />
					</html:select>
				</TD>
			</TR>
			</logic:notEmpty>
		</logic:equal>

		<logic:equal name="courseOfferingEditForm" property="allowAlternativeCourseOfferings" value="true">
			<logic:notEmpty name="altOfferingList" scope="request">
			<TR>
				<TD><loc:message name="propertyAlternativeCourseOffering"/> </TD>
				<TD>
					<html:select
						name="courseOfferingEditForm"
						property="alternativeCourseOfferingId">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"> </html:option>
						<html:options collection="altOfferingList" property="uniqueId" labelProperty="courseNameWithTitle" />
					</html:select>
				</TD>
			</TR>
			</logic:notEmpty>
		</logic:equal>		
	</sec:authorize>
	
	<sec:authorize access="!(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and
						!(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<logic:notEqual name="courseOfferingEditForm" property="consent" value="-1">
			<TR>
				<TD valign="top"><loc:message name="propertyConsent" /></TD>
				<TD>
					<logic:iterate name="<%=OfferingConsentType.CONSENT_TYPE_ATTR_NAME%>" scope="request" id="consent" type="org.unitime.timetable.model.OfferingConsentType">
						<logic:equal name="courseOfferingEditForm" property="consent" value="<%=consent.getUniqueId().toString()%>">
							<bean:write name="consent" property="label"/>
						</logic:equal>
					</logic:iterate>
				</TD>
			</TR>
		</logic:notEqual>
		<html:hidden property="consent"/>

		<logic:equal name="courseOfferingEditForm" property="isControl" value="true">
			<logic:notEmpty name="courseOfferingEditForm" property="creditText">
				<TR>
					<TD><loc:message name="propertyCredit"/></TD>
					<TD>
						<bean:write name="courseOfferingEditForm" property="creditText"/>
					</TD>
				</TR>
			</logic:notEmpty>
			<html:hidden property="creditFormat"/>
			<html:hidden property="creditType"/>
			<html:hidden property="creditUnitType"/>
			<html:hidden property="units"/>
			<html:hidden property="maxUnits"/>
			<html:hidden property="creditText"/>
		</logic:equal>
		
		<logic:equal name="courseOfferingEditForm" property="allowDemandCourseOfferings" value="true">
			<logic:notEmpty name="courseOfferingEditForm" property="demandCourseOfferingId">
				<logic:iterate name="<%=CourseOffering.CRS_OFFERING_LIST_ATTR_NAME%>" scope="request" id="course" type="org.unitime.timetable.model.CourseOffering">
					<logic:equal name="courseOfferingEditForm" property="demandCourseOfferingId" value="<%=course.getUniqueId().toString()%>">
						<TR>
							<TD><loc:message name="propertyTakeCourseDemandsFromOffering"/> </TD>
							<TD>
								<bean:write name="course" property="courseNameWithTitle"/>
							</TD>
						</TR>				
					</logic:equal>
				</logic:iterate>
			</logic:notEmpty>
		</logic:equal>
		<html:hidden property="demandCourseOfferingId"/>
	</sec:authorize>

	<logic:notEmpty name="courseOfferingEditForm" property="catalogLinkLabel">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="<bean:write name="courseOfferingEditForm" property="catalogLinkLocation" />" target="_blank"><bean:write name="courseOfferingEditForm" property="catalogLinkLabel" /></A>
			</TD>
		</TR>
	</logic:notEmpty>
	<tt:hasProperty name="unitime.custom.CourseUrlProvider">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<span id='UniTimeGWT:CourseLink' style="display: none;">subjectId,course</span>
			</TD>
		</TR>
	</tt:hasProperty>

	<logic:equal name="courseOfferingEditForm" property="isControl" value="true">
		<sec:authorize access="(not #courseOfferingEditForm.add and (hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')
								or hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOfferingCoordinators')))
								or (#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<logic:notEmpty name="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" scope="request">
			<TR>
				<TD valign="top"><loc:message name="propertyCoordinators"/> </TD>
				<TD nowrap>
				<table border='0'>
					<tr>
						<td>&nbsp;<i><loc:message name="columnInstructorName"/></i>&nbsp;</td>
						<td>&nbsp;<i><loc:message name="columnInstructorShare"/></i>&nbsp;</td>
						<td>
						<logic:notEmpty name="responsibilities" scope="request">
							&nbsp;<i><loc:message name="columnTeachingResponsibility"/></i>&nbsp;
						</logic:notEmpty>
						</td><td></td>
					</tr>
				<logic:iterate name="courseOfferingEditForm" property="instructors" id="instructor" indexId="ctr">
					<tr><td>
					<html:select style="width:200px;"
						property='<%= "instructors[" + ctr + "]" %>'>
						<html:option value="-">-</html:option>
						<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" property="value" labelProperty="label" />
					</html:select>
					</td><td align="center">
						<html:text property='<%= "percentShares[" + ctr + "]" %>' size="3" maxlength="3" style="text-align: right;"/>
					</td><td>
					<logic:notEmpty name="responsibilities" scope="request">
						<html:select
							property='<%= "responsibilities[" + ctr + "]" %>'>
							<logic:equal name="courseOfferingEditForm" property='<%= "responsibilities[" + ctr + "]" %>' value="">
								<html:option value="">-</html:option>
							</logic:equal>
							<logic:notEqual name="courseOfferingEditForm" property='<%= "responsibilities[" + ctr + "]" %>' value="">
								<logic:empty name="courseOfferingEditForm" property='defaultTeachingResponsibilityId'>
									<html:option value="">-</html:option>
								</logic:empty>
							</logic:notEqual>
							<html:options collection="responsibilities" property="uniqueId" labelProperty="label" />
						</html:select>
					</logic:notEmpty>
					<logic:empty name="responsibilities" scope="request">
						<html:hidden property='<%= "responsibilities[" + ctr + "]" %>'/>
					</logic:empty>
					</td><td>
					<html:submit property="op" 
								styleClass="btn"
								onclick="<%= \"javascript: doDel('coordinator', '\" + ctr + \"');\"%>">
								<loc:message name="actionRemoveCoordinator" />
					</html:submit>
					</td></tr>
   				</logic:iterate>
   				<tr><td colspan='4'>
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessAddCoordinator() %>" 
						title="<%=MSG.titleAddCoordinator(MSG.accessAddCoordinator()) %>">
						<loc:message name="actionAddCoordinator" />
					</html:submit> 			
				</td></tr>
				</table>
				</TD>
			</TR>
			</logic:notEmpty>
		</sec:authorize>
		<sec:authorize access="!(not #courseOfferingEditForm.add and (hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')
								or hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOfferingCoordinators')))
								and !(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<logic:notEmpty name="courseOfferingEditForm" property="instructors">
				<TD valign="top"><loc:message name="propertyCoordinators"/> </TD>
				<TD nowrap>
					<bean:define name="courseOfferingEditForm" property="responsibilities" id="r" type="java.util.List"/>
					<logic:iterate name="courseOfferingEditForm" property="instructors" id="instructor" indexId="ctr">
						<logic:iterate name="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" scope="request" id="lookup" type="org.unitime.timetable.util.ComboBoxLookup">
							<logic:equal name="lookup" property="value" value="<%=instructor.toString()%>">
								<logic:greaterThan name="ctr" value="0"><br></logic:greaterThan>
								<bean:write name="lookup" property="label"/>
								<logic:iterate id="responsibility" name="responsibilities" scope="request">
									<logic:equal name="responsibility" property="uniqueId" value="<%=(String)r.get(ctr)%>">
										(<bean:write name="responsibility" property="label"/>)
									</logic:equal>
								</logic:iterate>
							</logic:equal>
						</logic:iterate>
					</logic:iterate>
				</TD>
			</logic:notEmpty>
			<logic:iterate name="courseOfferingEditForm" property="instructors" id="instructor" indexId="ctr">
				<html:hidden property='<%= "instructors[" + ctr + "]" %>'/>
				<html:hidden property='<%= "responsibilities[" + ctr + "]" %>'/>
			</logic:iterate>
		</sec:authorize>

		<sec:authorize access="(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
							(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<TR>
				<TD valign="top"><loc:message name="propertyByReservationOnly"/> </TD>
				<TD>
					<html:checkbox name="courseOfferingEditForm" property="byReservationOnly" />
					<i><loc:message name="descriptionByReservationOnly"/></i>
				</TD>
			</TR>
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekEnrollment"/></TD>
				<TD valign="top">
					<html:text property="wkEnroll" maxlength="4" size="4"/>
					<i><loc:message name="descriptionLastWeekEnrollment"><bean:write name="courseOfferingEditForm" property="wkEnrollDefault" /></loc:message></i>
					<html:hidden property="wkEnrollDefault"/>
				</TD>
			</TR>
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekChange"/></TD>
				<TD valign="top">
					<html:text property="wkChange" maxlength="4" size="4"/>
					<i><loc:message name="descriptionLastWeekChange"><bean:write name="courseOfferingEditForm" property="wkChangeDefault" /></loc:message></i>
					<html:hidden property="wkChangeDefault"/>
				</TD>
			</TR>
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekDrop"/></TD>
				<TD valign="top">
					<html:text property="wkDrop" maxlength="4" size="4"/>
					<i><loc:message name="descriptionLastWeekDrop"><bean:write name="courseOfferingEditForm" property="wkDropDefault" /></loc:message></i>
					<html:hidden property="wkDropDefault"/>
					<br><i><loc:message name="descriptionEnrollmentDeadlines"><bean:write name="courseOfferingEditForm" property="weekStartDayOfWeek" /></loc:message></i>
					<html:hidden property="weekStartDayOfWeek"/>
				</TD>
			</TR>
			
			<!-- Requests / Notes -->
			<TR>
				<TD valign="top"><loc:message name="propertyRequestsNotes"/></TD>
				<TD align="left">
				<html:textarea property="notes" rows="4" cols="57"></html:textarea>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!(not #courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and 
							!(#courseOfferingEditForm.add and hasPermission(#courseOfferingEditForm.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<logic:equal name="courseOfferingEditForm" property="byReservationOnly" value="true">
				<TR>
					<TD><loc:message name="propertyByReservationOnly"/></TD>
					<TD>
						<IMG src="images/accept.png" alt="ENABLED" title="<%=MSG.descriptionByReservationOnly2() %>" border="0">
						<i><loc:message name="descriptionByReservationOnly2"/></i>
					</TD>
				</TR>
			</logic:equal>
			<html:hidden property="byReservationOnly"/>
			<logic:notEmpty name="courseOfferingEditForm" property="wkEnroll">
				<TR>
					<TD valign="top"><loc:message name="propertyLastWeekEnrollment"/></TD>
					<TD>
						<loc:message name="textLastWeekEnrollment"><bean:write name="courseOfferingEditForm" property="wkEnroll" /></loc:message>
						<logic:empty name="courseOfferingEditForm" property="wkChange">
							<logic:empty name="courseOfferingEditForm" property="wkDrop">
								<br><i><loc:message name="descriptionEnrollmentDeadlines"><bean:write name="courseOfferingEditForm" property="weekStartDayOfWeek" /></loc:message></i>
							</logic:empty>
						</logic:empty>
					</TD>
				</TR>
			</logic:notEmpty>
			<logic:notEmpty name="courseOfferingEditForm" property="wkChange">
				<TR>
					<TD valign="top"><loc:message name="propertyLastWeekChange"/></TD>
					<TD>
						<loc:message name="textLastWeekChange"><bean:write name="courseOfferingEditForm" property="wkChange" /></loc:message>
						<logic:empty name="courseOfferingEditForm" property="wkDrop">
							<br><i><loc:message name="descriptionEnrollmentDeadlines"><bean:write name="courseOfferingEditForm" property="weekStartDayOfWeek" /></loc:message></i>
						</logic:empty>
					</TD>
				</TR>
			</logic:notEmpty>
			<logic:notEmpty name="courseOfferingEditForm" property="wkDrop">
				<TR>
					<TD valign="top"><loc:message name="propertyLastWeekDrop"/></TD>
					<TD>
						<loc:message name="textLastWeekDrop"><bean:write name="courseOfferingEditForm" property="wkDrop" /></loc:message>
						<br><i><loc:message name="descriptionEnrollmentDeadlines"><bean:write name="courseOfferingEditForm" property="weekStartDayOfWeek" /></loc:message></i>
					</TD>
				</TR>
			</logic:notEmpty>
			<html:hidden property="wkEnroll"/>
			<html:hidden property="wkEnrollDefault"/>
			<html:hidden property="wkChange"/>
			<html:hidden property="wkChangeDefault"/>
			<html:hidden property="wkDrop"/>
			<html:hidden property="wkDropDefault"/>
			<html:hidden property="weekStartDayOfWeek"/>
			<html:hidden property="notes"/>
		</sec:authorize>		
	</logic:equal>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<logic:notEqual name="courseOfferingEditForm" property="add" value="true">
					<html:submit property="op"
						styleClass="btn" 
						accesskey="<%=MSG.accessUpdateCourseOffering() %>" 
						title="<%=MSG.titleUpdateCourseOffering(MSG.accessUpdateCourseOffering()) %>">
						<loc:message name="actionUpdateCourseOffering" />
					</html:submit>
				</logic:notEqual>
				
				<logic:equal name="courseOfferingEditForm" property="add" value="true">
					<html:submit property="op"
						styleClass="btn" 
						accesskey="<%=MSG.accessSaveCourseOffering() %>" 
						title="<%=MSG.titleSaveCourseOffering(MSG.accessSaveCourseOffering()) %>">
						<loc:message name="actionSaveCourseOffering" />
					</html:submit>
				</logic:equal>

				<logic:notEqual name="courseOfferingEditForm" property="add" value="true">
					<bean:define id="instrOfferingId">
						<bean:write name="courseOfferingEditForm" property="instrOfferingId" />
					</bean:define>
					<html:button property="op"
						styleClass="btn" 
						accesskey="<%=MSG.accessBackToIODetail() %>" 
						title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>"
						onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
						<loc:message name="actionBackToIODetail" />
					</html:button>
				</logic:notEqual>
				
				<logic:equal name="courseOfferingEditForm" property="add" value="true">
						<html:button property="op"
						styleClass="btn" 
						accesskey="<%=MSG.accessBackToIOList()%>" 
						title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>"
						onclick="document.location.href='instructionalOfferingShowSearch.do';">
						<loc:message name="actionBackToIOList" />
					</html:button>
				</logic:equal>
			</TD>
		</TR>

	</TABLE>
	
	<INPUT type="hidden" name="deleteType" id="deleteType" value="">
	<INPUT type="hidden" name="deleteId" id="deleteId" value="">
	<SCRIPT type="text/javascript" language="javascript">
		function doDel(type, id) {
			document.courseOfferingEditForm.deleteType.value = type;
			document.courseOfferingEditForm.deleteId.value = id;
		}
	</SCRIPT>				
</html:form>

</loc:bundle>
