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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/timePatternEdit">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<html:hidden property="editable"/><html:errors property="editable"/>

<logic:notEqual name="timePatternEditForm" property="op" value="List">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="timePatternEditForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="timePatternEditForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Time Pattern
					</tt:section-title>
					<html:submit property="op">
						<bean:write name="timePatternEditForm" property="op" />
					</html:submit> 
					<logic:notEqual name="timePatternEditForm" property="op" value="Save">
					<logic:equal name="timePatternEditForm" property="editable" value="true">
						<html:submit property="op" value="Delete"/> 
					</logic:equal>
					</logic:notEqual>
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
				<logic:equal name="timePatternEditForm" property="editable" value="true">
					<html:text property="nrMtgs" size="2" maxlength="2"/>
					&times;
					<html:text property="minPerMtg" size="3" maxlength="3"/>
					&nbsp;
					<html:select property="type">
						<html:options name="timePatternEditForm" property="types"/>
					</html:select>
					&nbsp;<html:errors property="nrMtgs"/>
					&nbsp;<html:errors property="minPerMtg"/>
					&nbsp;<html:errors property="type"/>
				</logic:equal>
				<logic:notEqual name="timePatternEditForm" property="editable" value="true">
					<bean:write name="timePatternEditForm" property="nrMtgs" />
					<html:hidden name="timePatternEditForm" property="nrMtgs" />
					&times;
					<bean:write name="timePatternEditForm" property="minPerMtg" />
					<html:hidden name="timePatternEditForm" property="minPerMtg" />
					&nbsp;
					<html:select property="type">
						<html:options name="timePatternEditForm" property="types"/>
					</html:select>
					&nbsp;<html:errors property="type"/>
				</logic:notEqual>
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
			<TD>Number of slots per meeting:</TD>
			<TD>
				<logic:equal name="timePatternEditForm" property="editable" value="true">
					<html:text property="slotsPerMtg" size="3" maxlength="3"/>
					(one slot represent <%=org.unitime.timetable.util.Constants.SLOT_LENGTH_MIN%> minutes)
					&nbsp;<html:errors property="slotsPerMtg"/>
				</logic:equal>
				<logic:notEqual name="timePatternEditForm" property="editable" value="true">
					<bean:write name="timePatternEditForm" property="slotsPerMtg" />
					<html:hidden name="timePatternEditForm" property="slotsPerMtg" />
					(one slot represent <%=org.unitime.timetable.util.Constants.SLOT_LENGTH_MIN%> minutes)
				</logic:notEqual>
				
			</TD>
		</TR>

		<TR>
			<TD>Break time [minutes]:</TD>
			<TD>
				<html:text property="breakTime" size="3" maxlength="3"/>&nbsp;<html:errors property="breakTime"/>
			</TD>
		</TR>

		<TR>
			<TD>Days:</TD>
			<TD>
				<logic:equal name="timePatternEditForm" property="editable" value="true">
					<html:textarea property="dayCodes" rows="5" cols="10"/>
					&nbsp;<html:errors property="dayCodes"/>
				</logic:equal>
				<logic:notEqual name="timePatternEditForm" property="editable" value="true">
					<bean:write name="timePatternEditForm" property="dayCodes" />
					<html:hidden name="timePatternEditForm" property="dayCodes" />
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Start times:</TD>
			<TD>
				<logic:equal name="timePatternEditForm" property="editable" value="true">
					<html:textarea property="startTimes" rows="5" cols="10"/>
					&nbsp;<html:errors property="startTimes"/>
				</logic:equal>
				<logic:notEqual name="timePatternEditForm" property="editable" value="true">
					<bean:write name="timePatternEditForm" property="startTimes" />
					<html:hidden name="timePatternEditForm" property="startTimes" />
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Departments:</TD>
			<TD>
				<logic:iterate name="timePatternEditForm" property="departmentIds" id="deptId">
					<logic:iterate scope="request" name="<%=Department.DEPT_ATTR_NAME%>" id="dept">
						<logic:equal name="dept" property="value" value="<%=deptId.toString()%>">
							<bean:write name="dept" property="label"/>
							<input type="hidden" name="depts" value="<%=deptId%>">
							<BR>
						</logic:equal>
					</logic:iterate>
				</logic:iterate>
				<html:select property="departmentId">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
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
		
<%
	String example = (String)request.getAttribute("TimePatterns.example");
	if (example!=null) {
%>
		<TR>
			<TD valign='top'>Example:</TD>
			<TD>
				<%=example%>
			</TD>
		</TR>
<%
	}
%>		

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="timePatternEditForm" property="op" />
				</html:submit> 
				<logic:notEqual name="timePatternEditForm" property="op" value="Save">
				<logic:equal name="timePatternEditForm" property="editable" value="true">
					<html:submit property="op" value="Delete"/> 
				</logic:equal>
				</logic:notEqual>
				<html:submit property="op" value="Back" /> 
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>
</logic:notEqual>
<logic:equal name="timePatternEditForm" property="op" value="List">
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<logic:messagesPresent>
		<TR>
			<TD colspan='10' align="left" class="errorCell">
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
		<TD colspan='10'>
			<tt:section-header>
				<tt:section-title>Time Patterns</tt:section-title>
				<html:submit property="op" value="Add Time Pattern" title="Create a new time pattern"/>
				<html:submit property="op" value="Assign Departments" title="Assign departments to extended time patterns"/> 
				<html:submit property="op" value="Exact Times CSV" title="Generate a CSV report with all classes that are using exact times"/> 
				<html:submit property="op" value="Export CSV" title="Export time patterns to CSV"/> 
			</tt:section-header>
		</TD>
	</TR>
	<%= request.getAttribute("TimePatterns.table") %>
	<TR>
		<TD colspan='10'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='10' align="right">
			<html:submit property="op" value="Add Time Pattern" title="Create a new time pattern"/>
			<html:submit property="op" value="Assign Departments" title="Assign departments to extended time patterns"/> 
			<html:submit property="op" value="Exact Times CSV" title="Generate a CSV report with all classes that are using exact times"/> 
			<html:submit property="op" value="Export CSV" title="Export time patterns to CSV"/> 
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
