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
<%@ page language="java" autoFlush="true"%>
<%@page import="org.unitime.timetable.model.Exam"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<tiles:importAttribute />

<html:form action="/examPeriodEdit">

<logic:notEqual name="examPeriodEditForm" property="op" value="List">
	<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
	<html:hidden property="autoSetup"/>
	<html:hidden property="editable"/>
	<logic:equal name="examPeriodEditForm" property="autoSetup" value="true">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						Setup Examination Periods
					</tt:section-title>
					<html:submit property="op">
						<bean:write name="examPeriodEditForm" property="op" />
					</html:submit> 
					<html:submit property="op" value="Back" /> 
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="examType" disabled="true">
					<html:option value="">Select...</html:option>
					<html:options collection="examTypes" property="uniqueId" labelProperty="label" />
				</html:select>
				<html:hidden property="examType"/>
				&nbsp;<html:errors property="examType"/>
			</TD>
		</TR>
		

		<TR>
			<TD>1st Period Start Time:</TD>
			<TD>
			<html:text property="start" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start"/>
			</TD>
		</TR>

		<TR>
			<TD>1st Period Length:</TD>
			<TD>
			<html:text property="length" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length"/>
			</TD>
		</TR>

		<TR>
			<TD>1st Period Event Start Offset:</TD>
			<TD>
			<html:text property="startOffset" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="startOffset"/>
			</TD>
		</TR>

		<TR>
			<TD>1st Period Event Stop Offset:</TD>
			<TD>
			<html:text property="stopOffset" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="stopOffset"/>
			</TD>
		</TR>

		<TR>
			<TD>2nd Period Start Time:</TD>
			<TD>
			<html:text property="start2" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start2"/>
			</TD>
		</TR>

		<TR>
			<TD>2nd Period Length:</TD>
			<TD>
			<html:text property="length2" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length2"/>
			</TD>
		</TR>

		<TR>
			<TD>2nd Period Event Start Offset:</TD>
			<TD>
			<html:text property="startOffset2" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="startOffset2"/>
			</TD>
		</TR>

		<TR>
			<TD>2nd Period Event Stop Offset:</TD>
			<TD>
			<html:text property="stopOffset2" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="stopOffset2"/>
			</TD>
		</TR>

		<TR>
			<TD>3rd Period Start Time:</TD>
			<TD>
			<html:text property="start3" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start3"/>
			</TD>
		</TR>

		<TR>
			<TD>3rd Period Length:</TD>
			<TD>
			<html:text property="length3" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length3"/>
			</TD>
		</TR>

		<TR>
			<TD>3rd Period Event Start Offset:</TD>
			<TD>
			<html:text property="startOffset3" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="startOffset3"/>
			</TD>
		</TR>

		<TR>
			<TD>3rd Period Event Stop Offset:</TD>
			<TD>
			<html:text property="stopOffset3" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="stopOffset3"/>
			</TD>
		</TR>

		<TR>
			<TD>4th Period Start Time:</TD>
			<TD>
			<html:text property="start4" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start4"/>
			</TD>
		</TR>

		<TR>
			<TD>4th Period Length:</TD>
			<TD>
			<html:text property="length4" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length4"/>
			</TD>
		</TR>

		<TR>
			<TD>4th Period Event Start Offset:</TD>
			<TD>
			<html:text property="startOffset4" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="startOffset4"/>
			</TD>
		</TR>

		<TR>
			<TD>4th Period Event Stop Offset:</TD>
			<TD>
			<html:text property="stopOffset4" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="stopOffset4"/>
			</TD>
		</TR>
		<TR>
			<TD>5th Period Start Time:</TD>
			<TD>
			<html:text property="start5" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start5"/>
			</TD>
		</TR>

		<TR>
			<TD>5th Period Length:</TD>
			<TD>
			<html:text property="length5" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length5"/>
			</TD>
		</TR>

		<TR>
			<TD>5th Period Event Start Offset:</TD>
			<TD>
			<html:text property="startOffset5" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="startOffset5"/>
			</TD>
		</TR>

		<TR>
			<TD>5th Period Event Stop Offset:</TD>
			<TD>
			<html:text property="stopOffset5" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="stopOffset5"/>
			</TD>
		</TR>

		<TR>
			<TD colspan='2'><br>Examination Dates:</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<bean:write name="examPeriodEditForm" property="patternHtml" filter="false"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="examPeriodEditForm" property="op" />
				</html:submit> 
				<html:submit property="op" value="Back" /> 
			</TD>
		</TR>
	</TABLE>
	</logic:equal>
	<logic:equal name="examPeriodEditForm" property="autoSetup" value="false">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="examPeriodEditForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Examination Period
					</tt:section-title>
					<html:submit property="op">
						<bean:write name="examPeriodEditForm" property="op" />
					</html:submit>
					<logic:equal name="examPeriodEditForm" property="editable" value="true"> 
						<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
							<html:submit property="op" value="Delete"/> 
						</logic:notEqual>
					</logic:equal>
					<html:submit property="op" value="Back" /> 
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD>Type:</TD>
			<TD>
				<logic:equal name="examPeriodEditForm" property="op" value="Save">
					<input type='hidden' name='op2' value=''>
					<html:select property="examType" onchange="op2.value='Reload'; submit();">
						<html:option value="-1">Select...</html:option>
						<html:options collection="examTypes" property="uniqueId" labelProperty="label" />
					</html:select>
					&nbsp;<html:errors property="examType"/>
				</logic:equal>
				<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
					<html:select property="examType" disabled="true">
						<html:options collection="examTypes" property="uniqueId" labelProperty="label" />
					</html:select>
					<html:hidden property="examType"/>
				</logic:notEqual>
			</TD>
		</TR>
		

		<TR>
			<TD>Date:</TD>
			<TD>
				<logic:equal name="examPeriodEditForm" property="editable" value="true">
					<tt:calendar property="date"/>
				</logic:equal>
				<logic:notEqual name="examPeriodEditForm" property="editable" value="true">
					<bean:write name="examPeriodEditForm" property="date"/>
					<html:hidden property="date"/>
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Start Time:</TD>
			<TD>
				<logic:equal name="examPeriodEditForm" property="editable" value="true">
					<html:text property="start" size="4" maxlength="4"/> (in military format)
					&nbsp;<html:errors property="start"/>
				</logic:equal>
				<logic:notEqual name="examPeriodEditForm" property="editable" value="true">
					<bean:write name="examPeriodEditForm" property="start"/>  (in military format)
					<html:hidden property="start"/>
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Length:</TD>
			<TD>
				<logic:equal name="examPeriodEditForm" property="editable" value="true">
					<html:text property="length" size="4" maxlength="4"/> (in minutes)
					&nbsp;<html:errors property="length"/>
				</logic:equal>
				<logic:notEqual name="examPeriodEditForm" property="editable" value="true">
					<bean:write name="examPeriodEditForm" property="length"/> minutes
					<html:hidden property="length"/>
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Event Start Offset:</TD>
			<TD>
				<logic:equal name="examPeriodEditForm" property="editable" value="true">
					<html:text property="startOffset" size="4" maxlength="4"/> (in minutes)
					&nbsp;<html:errors property="startOffset"/>
				</logic:equal>
				<logic:notEqual name="examPeriodEditForm" property="editable" value="true">
					<bean:write name="examPeriodEditForm" property="startOffset"/> minutes
					<html:hidden property="startOffset"/>
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Event Stop Offset:</TD>
			<TD>
				<logic:equal name="examPeriodEditForm" property="editable" value="true">
					<html:text property="stopOffset" size="4" maxlength="4"/> (in minutes)
					&nbsp;<html:errors property="stopOffset"/>
				</logic:equal>
				<logic:notEqual name="examPeriodEditForm" property="editable" value="true">
					<bean:write name="examPeriodEditForm" property="stopOffset"/> minutes
					<html:hidden property="stopOffset"/>
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Preference:</TD>
			<TD>
			<html:select property="prefLevel">
				<html:optionsCollection property="prefLevels" label="prefName" value="uniqueId"/>
			</html:select>
			&nbsp;<html:errors property="prefLevel"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="examPeriodEditForm" property="op" />
				</html:submit> 
				<logic:equal name="examPeriodEditForm" property="editable" value="true">
					<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
						<html:submit property="op" value="Delete"/> 
					</logic:notEqual>
				</logic:equal>
				<html:submit property="op" value="Back" /> 
			</TD>
		</TR>
	</TABLE>
	</logic:equal>
<BR>
</logic:notEqual>
<logic:equal name="examPeriodEditForm" property="op" value="List">
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='8'>
			<tt:section-header>
				<tt:section-title>Examination Periods</tt:section-title>
				<html:submit property="op" value="Add Period" title="Create a new examination period"/>
				<logic:iterate scope="request" name="examTypes" id="type" type="org.unitime.timetable.model.ExamType">
					<logic:equal name="examPeriodEditForm" property='<%="canAutoSetup("+type.getUniqueId()+")"%>' value="true">
						<html:submit property="op" value='<%=type.getLabel() + " Periods"%>' title="Setup periods for exams"/>
					</logic:equal>
				</logic:iterate>
			</tt:section-header>
		</TD>
	</TR>
	<%= request.getAttribute("ExamPeriods.table") %>
	<TR>
		<TD colspan='8'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='8' align="right">
			<html:submit property="op" value="Add Period" title="Create a new examination period"/>
			<logic:iterate scope="request" name="examTypes" id="type" type="org.unitime.timetable.model.ExamType">
				<logic:equal name="examPeriodEditForm" property='<%="canAutoSetup("+type.getUniqueId()+")"%>' value="true">
					<html:submit property="op" value='<%=type.getLabel() + " Periods"%>' title="Setup periods for exams"/>
				</logic:equal>
			</logic:iterate>
		</TD>
	</TR>
	<% if (request.getAttribute("hash") != null) { %>
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	<% } %>
</TABLE>
</logic:equal>

</html:form>
