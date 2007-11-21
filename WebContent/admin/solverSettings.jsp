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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The solver configuration will be deleted. Continue?</tt:confirm>

	<html:form action="/solverSettings">
<%  try {
	String frmName = "solverSettingsForm";		
	SolverSettingsForm frm = (SolverSettingsForm)request.getAttribute(frmName);
	if (request.getAttribute("SolverSettings.table")!=null) {
%>

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
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

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
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
					</logic:notEqual>
					<html:submit property="op" value="Export" title="Export to Property File (Alt+E)" accesskey="E"/> 
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
				<html:select property="appearance">
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
			List defs = SolverParameterDef.findByGroup(group);
			for (Iterator j=defs.iterator();j.hasNext();) {
				SolverParameterDef def = (SolverParameterDef)j.next();
				if (def.isVisible().booleanValue()) {
					groupVisible = true; break;
				}
			}
			if (!groupVisible) continue;
%>
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<%=group.getDescription()%>
				</DIV>
			</TD>
		</TR>
<%
			for (Iterator j=defs.iterator();j.hasNext();) {
				SolverParameterDef def = (SolverParameterDef)j.next();
				if (!def.isVisible().booleanValue()) continue;
%>
 		<TR>
			<TD>
<%
				if (def.getDefault()!=null) {
					if ("boolean".equals(def.getType())) { 
%>
						<html:checkbox onclick="<%="solverSettingsForm['parameter["+def.getUniqueId()+"]'].disabled=this.checked;solverSettingsForm['parameter["+def.getUniqueId()+"]'].checked="+def.getDefault()+";"%>" property="<%="useDefault["+def.getUniqueId()+"]"%>"/>
<%
					} else if ("timepref".equals(def.getType())) {
%>
						<html:checkbox onclick="<%="document.getElementById('tp_enable_"+def.getUniqueId()+"').style.display=(this.checked?'none':'block');document.getElementById('tp_disable_"+def.getUniqueId()+"').style.display=(this.checked?'block':'none');"%>" property="<%="useDefault["+def.getUniqueId()+"]"%>"/>
<%
					} else {
%>
						<html:checkbox onclick="<%="solverSettingsForm['parameter["+def.getUniqueId()+"]'].disabled=this.checked;solverSettingsForm['parameter["+def.getUniqueId()+"]'].value='"+def.getDefault()+"';"%>" property="<%="useDefault["+def.getUniqueId()+"]"%>"/>
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
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="false">
					<html:checkbox property="<%="parameter["+def.getUniqueId()+"]"%>" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="true">
					<html:checkbox property="<%="parameter["+def.getUniqueId()+"]"%>" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property="<%="parameter["+def.getUniqueId()+"]"%>"/>
<%
 				} else if (def.getType().startsWith("enum(") && def.getType().endsWith(")")) { 
%>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="false">
					<html:select property="<%="parameter["+def.getUniqueId()+"]"%>" disabled="false">
						<html:options property="<%="enum("+def.getType()+")"%>"/>
					</html:select>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="true">
					<html:select property="<%="parameter["+def.getUniqueId()+"]"%>" disabled="true">
						<html:options property="<%="enum("+def.getType()+")"%>"/>
					</html:select>
				</logic:equal>
<%
 				} else if ("double".equals(def.getType())) { 
%>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="false">
					<html:text property="<%="parameter["+def.getUniqueId()+"]"%>" size="10" maxlength="10" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="true">
					<html:text property="<%="parameter["+def.getUniqueId()+"]"%>" size="10" maxlength="10" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property="<%="parameter["+def.getUniqueId()+"]"%>"/>
<%
 				} else if ("integer".equals(def.getType())) { 
%>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="false">
					<html:text property="<%="parameter["+def.getUniqueId()+"]"%>" size="10" maxlength="10" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="true">
					<html:text property="<%="parameter["+def.getUniqueId()+"]"%>" size="10" maxlength="10" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property="<%="parameter["+def.getUniqueId()+"]"%>"/>
<%
 				} else if ("timepref".equals(def.getType())) { 
%>
					</TD></TR><TR><TD colspan='2'>
<%
					RequiredTimeTable rtt = TimePattern.getDefaultRequiredTimeTable();
					rtt.getModel().setPreferences(frm.getParameter(def.getUniqueId()));
					((TimePatternModel)rtt.getModel()).setAllowHard(false);
%>
						<div id='tp_enable_<%=def.getUniqueId()%>' style='display:<%=frm.getUseDefault(def.getUniqueId()).booleanValue()?"none":"block"%>'>
<%
					rtt.setName("tp_"+def.getUniqueId());
					out.print(rtt.print(true,false));
%>
						</div><div id='tp_disable_<%=def.getUniqueId()%>' style='display:<%=frm.getUseDefault(def.getUniqueId()).booleanValue()?"block":"none"%>'>
<%					
					rtt.setName("tp_def_"+def.getUniqueId());
					out.print(rtt.print(false,false,false,false));
%>
						</div>
  				&nbsp;<html:errors property="<%="parameter["+def.getUniqueId()+"]"%>"/>
<%
 				} else { 
%>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="false">
					<html:text property="<%="parameter["+def.getUniqueId()+"]"%>" size="30" maxlength="100" disabled="false"/>
				</logic:equal>
				<logic:equal name="solverSettingsForm" property="<%="useDefault["+def.getUniqueId()+"]"%>" value="true">
					<html:text property="<%="parameter["+def.getUniqueId()+"]"%>" size="30" maxlength="100" disabled="true"/>
				</logic:equal>
  				&nbsp;<html:errors property="<%="parameter["+def.getUniqueId()+"]"%>"/>
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
				</logic:notEqual>
				<html:submit property="op" value="Export" title="Export to Property File (Alt+E)" accesskey="E"/> 
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