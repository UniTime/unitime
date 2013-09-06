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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<tiles:importAttribute />

<html:form action="/datePatternEdit">

<logic:notEqual name="datePatternEditForm" property="op" value="List">
	<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
	<html:hidden property="isUsed"/><html:errors property="isUsed"/>
	<html:hidden property="sessionId"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="datePatternEditForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="datePatternEditForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Date Pattern
					</tt:section-title>
					<html:submit property="op">
						<bean:write name="datePatternEditForm" property="op" />
					</html:submit> 
					<logic:notEqual name="datePatternEditForm" property="op" value="Save">
						<logic:equal name="datePatternEditForm" property="isUsed" value="false">
							<logic:equal name="datePatternEditForm" property="isDefault" value="false">
								<html:submit property="op" value="Delete"/> 
							</logic:equal>
						</logic:equal>
					</logic:notEqual>
					<logic:equal name="datePatternEditForm" property="isDefault" value="false">
						<html:submit property="op" value="Make Default"/> 
					</logic:equal>
					<html:submit property="op" value="Back" /> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="50" maxlength="100"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="type" onchange="hideParentsIfNeeded();" styleId="__type">
					<html:options name="datePatternEditForm" property="types"/>
				</html:select>
				&nbsp;<html:errors property="type"/>
			</TD>
		</TR>

		<TR>
			<TD>Visible:</TD>
			<TD>
				<html:checkbox property="visible"/>
				&nbsp;<html:errors property="visible"/>
			</TD>
		</TR>

		<TR>
			<TD>Default:</TD>
			<TD>
				<html:checkbox property="isDefault" disabled="true"/>
				&nbsp;<html:errors property="isDefault"/>
			</TD>
		</TR>
		
		<TR>
			<TD valign="top">Departments:</TD>
			<TD>
				<logic:iterate name="datePatternEditForm" property="departmentIds" id="deptId">
					<logic:iterate scope="request" name="<%=Department.DEPT_ATTR_NAME%>" id="dept">
						<logic:equal name="dept" property="value" value="<%=deptId.toString()%>">
							<bean:write name="dept" property="label"/>
							<input type="hidden" name="depts" value="<%=deptId%>">
							<BR>
						</logic:equal>
					</logic:iterate>
				</logic:iterate>
				<html:select property="departmentId">
					<html:option value="-1"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
				</html:select>
				&nbsp;
				<html:submit property="op" value="Add Department"/>
				&nbsp;
				<html:submit property="op" value="Remove Department"/>
				&nbsp;
				<html:errors property="department"/>
			</TD>
		</TR>
		
		<logic:notEmpty scope="request" name="<%=DatePattern.DATE_PATTERN_PARENT_LIST_ATTR%>">
		<TR id="__parents">		
		    <TD valign="top">Alternative Pattern Sets:</TD>
			<TD>
				<logic:iterate name="datePatternEditForm" property="parentIds" id="parentId">
					<logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_PARENT_LIST_ATTR%>" id="parent">
						<logic:equal name="parent" property="uniqueId" value="<%=parentId.toString()%>">							
							<bean:write name="parent" property="name"/>
							<input type="hidden" name="prnts" value="<%=parentId%>">
							<BR>
						</logic:equal>
					</logic:iterate>
				</logic:iterate>
				<html:select property="parentId">
					<html:option value="-1"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=DatePattern.DATE_PATTERN_PARENT_LIST_ATTR%>" property="uniqueId" labelProperty="name"/>
				</html:select>
				&nbsp;
				<html:submit property="op" value="Add Pattern Set"/>
				&nbsp;
				<html:submit property="op" value="Remove Pattern Set"/>
				&nbsp;
				<html:errors property="parent"/>
			</TD>			
		</TR>
		</logic:notEmpty>
		

		<TR id="__pattern">
			<TD>Pattern:</TD><TD><html:errors property="pattern"/></TD>
		</TR>
		<TR id="__patternTable">
			<TD colspan='2'>
				<%=request.getAttribute("DatePatterns.pattern")%>
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
					<bean:write name="datePatternEditForm" property="op" />
				</html:submit> 
				<logic:notEqual name="datePatternEditForm" property="op" value="Save">
					<logic:equal name="datePatternEditForm" property="isUsed" value="false">
						<logic:equal name="datePatternEditForm" property="isDefault" value="false">
							<html:submit property="op" value="Delete"/> 
						</logic:equal>
					</logic:equal>
				</logic:notEqual>
				<logic:equal name="datePatternEditForm" property="isDefault" value="false">
					<html:submit property="op" value="Make Default"/> 
				</logic:equal>
				<html:submit property="op" value="Back" /> 
			</TD>
		</TR>
	</TABLE>

<BR>
</logic:notEqual>
<logic:equal name="datePatternEditForm" property="op" value="List">
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='6'>
			<tt:section-header>
				<tt:section-title>Date Patterns</tt:section-title>
				<html:submit property="op" value="Add Date Pattern" title="Create a new date pattern"/>
				<html:submit property="op" value="Assign Departments" title="Assign departments to extended date patterns"/> 
				<html:submit property="op" value="Push Up" title="Move date patterns from classes to subparts whenever possible"/> 
				<html:submit property="op" value="Export CSV" title="Export date patterns to CSV"/> 
			</tt:section-header>
		</TD>
	</TR>
	<%= request.getAttribute("DatePatterns.table") %>
	<TR>
		<TD colspan='6'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='6' align="right">
			<html:submit property="op" value="Add Date Pattern" title="Add a new date pattern"/>
			<html:submit property="op" value="Assign Departments" title="Assign departments to extended date patterns"/> 
			<html:submit property="op" value="Push Up" title="Move date patterns from classes to subparts whenever possible"/> 
			<html:submit property="op" value="Export CSV" title="Export date patterns to CSV"/> 
		</TD>
	</TR>
	<% if (request.getAttribute("hash") != null) { %>
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	<% } %>
</TABLE>
</logic:equal>
<script>
function isParentSelected() {
	var type = document.getElementById('__type');
	return type.selectedIndex >= 0 && type.options[type.selectedIndex].value == '<%=DatePattern.sTypes[DatePattern.sTypePatternSet]%>';
}
function hideParentsIfNeeded() {
	var type = document.getElementById('__type');
	if (type == null) return;
	var selected = type.selectedIndex >= 0 && type.options[type.selectedIndex].value == '<%=DatePattern.sTypes[DatePattern.sTypePatternSet]%>';
	var parents = document.getElementById('__parents');
	if (parents != null) parents.style.display = ( selected ? 'none' : 'table-row');
	var pattern = document.getElementById('__pattern');
	if (pattern != null) pattern.style.display = ( selected ? 'none' : 'table-row');
	var patternTable = document.getElementById('__patternTable');
	if (patternTable != null) patternTable.style.display = ( selected ? 'none' : 'table-row');
}
hideParentsIfNeeded();
</script>
</html:form>
