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
<%@page import="org.unitime.timetable.defaults.UserProperty"%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.model.dao.SolverParameterGroupDAO" %>
<%@ page import="org.hibernate.criterion.Order" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.unitime.timetable.model.SolverParameterGroup" %>
<%@ page import="org.unitime.timetable.model.SolverParameterDef" %>
<%@ page import="org.unitime.timetable.model.TimePattern" %>
<%@ page import="org.unitime.timetable.model.TimePatternModel" %>
<%@ page import="org.unitime.timetable.webutil.RequiredTimeTable" %>
<%@ page import="org.unitime.timetable.form.SolverSettingsForm" %>
<%@page import="org.unitime.timetable.model.SolverParameter"%>
<%@page import="org.unitime.timetable.model.SolverPredefinedSetting"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The solver configuration will be deleted. Continue?</tt:confirm>
<tt:session-context/>

	<html:form action="/solverSettings">
<%  try {
	String frmName = "solverSettingsForm";		
	SolverSettingsForm frm = (SolverSettingsForm)request.getAttribute(frmName);
	if (request.getAttribute("SolverSettings.table")!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="4">
				<tt:section-header>
					<tt:section-title>
						Solver Configurations		
					</tt:section-title>
					<html:submit property="op" value="Add Solver Configuration" accesskey="A" title="Create New Solver Configuration (Alt+A)"/>
				</tt:section-header>
			</TD>
		</TR>
		<%= request.getAttribute("SolverSettings.table") %> 
		<TR>
			<TD align="right" colspan="4">
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align="right" colspan="4">
				<html:submit property="op" value="Add Solver Configuration" accesskey="A" title="Create New Solver Configuration (Alt+A)"/> 
			</TD>
		</TR>
	</TABLE>
	<BR>&nbsp;<BR>
<% 
	} else { %>
	<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
	<input type='hidden' name='op2' value=''>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="solverSettingsForm" property="op" value="Save">
						Add
						</logic:equal>
						<logic:notEqual name="solverSettingsForm" property="op" value="Save">
						Edit
						</logic:notEqual>
						Sovler Configuration
					</tt:section-title>
					<logic:equal name="solverSettingsForm" property="op" value="Save">
						<html:submit property="op" value="Save" accesskey="S" title="Save Solver Parameter (Alt+S)"/>
					</logic:equal>
					<logic:notEqual name="solverSettingsForm" property="op" value="Save">
						<html:submit property="op" value="Update" accesskey="U" title="Update Solver Parameter (Alt+U)"/>
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Parameter (Alt+D)"/> 
						<html:submit property="op" value="Export" title="Export to Property File (Alt+E)" accesskey="E"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Return to Solver Parameters (Alt+B)" accesskey="B"/> 
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD>Reference:</TD>
			<TD>
				<html:text property="name" size="30" maxlength="100"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="description" size="30" maxlength="1000"/>
				&nbsp;<html:errors property="description"/>
			</TD>
		</TR>

		<TR>
			<TD>Appearance:</TD>
			<TD>
				<html:select property="appearance" onchange="op2.value='Refresh'; submit();">
					<html:options name="solverSettingsForm" property="appearances"/>
				</html:select>
				&nbsp;<html:errors property="appearance"/>
			</TD>
		</TR>
		<TR><TD colspan='2'>&nbsp;</TD></TR>

<%
		List groups = (new SolverParameterGroupDAO()).findAll(Order.asc("order"));
		for (Iterator i=groups.iterator();i.hasNext();) {
			SolverParameterGroup group = (SolverParameterGroup)i.next();
			boolean groupVisible = false;
			boolean correctType = true;
			if (frm.getAppearanceIdx()==SolverPredefinedSetting.APPEARANCE_STUDENT_SOLVER) {
				if (group.getSolverType()!=SolverParameterGroup.SolverType.STUDENT) correctType=false;
			} else if (frm.getAppearanceIdx()==SolverPredefinedSetting.APPEARANCE_EXAM_SOLVER) {
				if (group.getSolverType()!=SolverParameterGroup.SolverType.EXAM) correctType=false;
			} else if (frm.getAppearanceIdx()==SolverPredefinedSetting.APPEARANCE_INSTRUCTOR_SOLVER) {
				if (group.getSolverType()!=SolverParameterGroup.SolverType.INSTRUCTOR) correctType=false;
			} else {
				if (group.getSolverType()!=SolverParameterGroup.SolverType.COURSE) correctType=false;
			}
			List defs = SolverParameterDef.findByGroup(group);
			for (Iterator j=defs.iterator();j.hasNext();) {
				SolverParameterDef def = (SolverParameterDef)j.next();
				if (def.isVisible().booleanValue()) {
					groupVisible = true; break;
				}
			}
			if (!groupVisible) continue;
			if (correctType) {
%>
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<%=group.getDescription()%>
				</DIV>
			</TD>
		</TR>
<%
			}
			defs: for (Iterator j=defs.iterator();j.hasNext();) {
				SolverParameterDef def = (SolverParameterDef)j.next();
				if (!def.isVisible().booleanValue()) continue;
				if (!correctType) {
%>
			<html:hidden property='<%="useDefault["+def.getUniqueId()+"]"%>' />
			<html:hidden property='<%="parameter["+def.getUniqueId()+"]"%>' />
<%
				
					continue defs;
				}
%>
 		<TR>
			<TD>
<%
				if (def.getDefault()!=null) {
					if ("boolean".equals(def.getType())) { 
%>
						<html:checkbox onclick="<%=\"solverSettingsForm['parameter[\"+def.getUniqueId()+\"]'].disabled=this.checked;solverSettingsForm['parameter[\"+def.getUniqueId()+\"]'].checked=\"+def.getDefault()+\";\"%>" property='<%="useDefault["+def.getUniqueId()+"]"%>'/>
<%
					} else if ("timepref".equals(def.getType())) {
%>
						<html:checkbox onclick="<%=\"document.getElementById('tp_enable_\"+def.getUniqueId()+\"').style.display=(this.checked?'none':'block');document.getElementById('tp_disable_\"+def.getUniqueId()+\"').style.display=(this.checked?'block':'none');\"%>" property='<%="useDefault["+def.getUniqueId()+"]"%>'/>
<%
					} else {
%>
						<html:checkbox onclick="<%=\"solverSettingsForm['parameter[\"+def.getUniqueId()+\"]'].disabled=this.checked;solverSettingsForm['parameter[\"+def.getUniqueId()+\"]'].value='\"+def.getDefault()+\"';\"%>" property='<%="useDefault["+def.getUniqueId()+"]"%>'/>
<%
					}
				}
%>
				<%=def.getDescription()%>:
			</TD>
			<TD>
<%
				if ("boolean".equals(def.getType())) { 
%>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="false">
					<html:checkbox property='<%="parameter["+def.getUniqueId()+"]"%>' disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="true">
					<html:checkbox property='<%="parameter["+def.getUniqueId()+"]"%>' disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property='<%="parameter["+def.getUniqueId()+"]"%>'/>
<%
 				} else if (def.getType().startsWith("enum(") && def.getType().endsWith(")")) { 
%>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="false">
					<html:select property='<%="parameter["+def.getUniqueId()+"]"%>' disabled="false">
						<html:options property='<%=def.getType()%>'/>
					</html:select>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="true">
					<html:select property='<%="parameter["+def.getUniqueId()+"]"%>' disabled="true">
						<html:options property='<%=def.getType()%>'/>
					</html:select>
				</logic:equal>
<%
 				} else if ("double".equals(def.getType())) { 
%>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="false">
					<html:text property='<%="parameter["+def.getUniqueId()+"]"%>' size="10" maxlength="10" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="true">
					<html:text property='<%="parameter["+def.getUniqueId()+"]"%>' size="10" maxlength="10" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property='<%="parameter["+def.getUniqueId()+"]"%>'/>
<%
 				} else if ("date".equals(def.getType())) { 
%>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="false">
					<tt:calendar property='<%="parameter["+def.getUniqueId()+"]"%>' format="yyyy-MM-dd" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="true">
					<tt:calendar property='<%="parameter["+def.getUniqueId()+"]"%>' format="yyyy-MM-dd" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property='<%="parameter["+def.getUniqueId()+"]"%>'/>
<%
 				} else if ("integer".equals(def.getType())) { 
%>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="false">
					<html:text property='<%="parameter["+def.getUniqueId()+"]"%>' size="10" maxlength="10" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="true">
					<html:text property='<%="parameter["+def.getUniqueId()+"]"%>' size="10" maxlength="10" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property='<%="parameter["+def.getUniqueId()+"]"%>'/>
<%
 				} else if ("timepref".equals(def.getType())) { 
%>
					<div id='tp_disable_<%=def.getUniqueId()%>' style='display:<%=frm.getUseDefault(def.getUniqueId()).booleanValue()?"block":"none"%>'>
						<img border="0"
							onmouseover="showGwtInstructorAvailabilityHint(this, '<%=def.getDefault()%>');"
							onmouseout="hideGwtInstructorAvailabilityHint();"
							src="<%="pattern?v=" + RequiredTimeTable.getTimeGridVertical(sessionContext.getUser()) + "&amp;s=" + UserProperty.GridSize.get(sessionContext.getUser()) + "&amp;p=" + def.getDefault()%>">
					</div>

					</TD></TR><TR><TD colspan='2'>
						<div id='tp_enable_<%=def.getUniqueId()%>' style='display:<%=frm.getUseDefault(def.getUniqueId()).booleanValue()?"none":"block"%>'>
							<div id='UniTimeGWT:InstructorAvailability'><html:hidden property='<%="parameter["+def.getUniqueId()+"]"%>'/></div>
						</div>
  				&nbsp;<html:errors property='<%="parameter["+def.getUniqueId()+"]"%>'/>
<%
 				} else { 
%>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="false">
					<html:text property='<%="parameter["+def.getUniqueId()+"]"%>' size="30" maxlength="2048" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property='<%="useDefault["+def.getUniqueId()+"]"%>' value="true">
					<html:text property='<%="parameter["+def.getUniqueId()+"]"%>' size="30" maxlength="2048" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property='<%="parameter["+def.getUniqueId()+"]"%>'/>
<%
				}
%>
			</TD>
		</TR>
<%
			}
		}
%>		
		<TR></TR>
		
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>		
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="solverSettingsForm" property="op" value="Save">
					<html:submit property="op" value="Save" accesskey="S" title="Save Solver Parameter (Alt+S)"/>
				</logic:equal>
				<logic:notEqual name="solverSettingsForm" property="op" value="Save">
					<html:submit property="op" value="Update" accesskey="U" title="Update Solver Parameter (Alt+U)"/>
					<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Parameter (Alt+D)"/> 
					<html:submit property="op" value="Export" title="Export to Property File (Alt+E)" accesskey="E"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Return to Solver Parameters (Alt+B)" accesskey="B"/> 
			</TD>
		</TR>
	</TABLE>
<% 
	}
	} catch (Exception e) {
		e.printStackTrace();
		out.println("<font color='red'>ERROR: "+e.getMessage()+"</font>");
	}
%>
</html:form>
