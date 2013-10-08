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
<%@ page import="org.unitime.timetable.ApplicationProperties"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.regex.Pattern"%>
<%@page import="org.unitime.commons.web.WebTable"%>
<%@page import="org.unitime.commons.Debug"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />

<tt:session-context/>
<tt:confirm name="confirmDelete">The application setting will be deleted. Continue?</tt:confirm>

<html:form action="/applicationConfig">
<logic:notEqual name="applicationConfigForm" property="op" value="list">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="applicationConfigForm" property="op" value="edit">
							Edit 
						</logic:equal>
						<logic:notEqual name="applicationConfigForm" property="op" value="edit">
							Add
						</logic:notEqual>
						Application Setting
					</tt:section-title>
					<logic:equal name="applicationConfigForm" property="op" value="edit">
						<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateAppConfig">
							<bean:message key="button.updateAppConfig" />
						</html:submit> 
						<html:submit property="op" styleClass="btn" accesskey="D" onclick="return confirmDelete();" titleKey="title.deleteAppConfig">
							<bean:message key="button.deleteAppConfig" />
						</html:submit> 
					</logic:equal>
					<logic:notEqual name="applicationConfigForm" property="op" value="edit">
						<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.createAppConfig">
							<bean:message key="button.createAppConfig" />
						</html:submit> 
					</logic:notEqual>
					<html:submit property="op" styleClass="btn" accesskey="B" titleKey="title.cancelUpdateAppConfig">
						<bean:message key="button.cancelUpdateAppConfig" />
					</html:submit> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Name:</TD>
			<TD valign="top">
				<logic:equal name="applicationConfigForm" property="op" value="edit">
					<bean:write name="applicationConfigForm" property="key"/>
					<html:hidden property="key"/>
				</logic:equal>
				<logic:notEqual name="applicationConfigForm" property="op" value="edit">
					<html:text property="key" size="75" maxlength="1000"/>
				</logic:notEqual>									
				&nbsp;<html:errors property="key"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top" rowspan="2">Applies To:</TD>
			<TD valign="top">
				<html:checkbox property="allSessions" onchange="document.getElementById('sessionsCell').style.display = (this.checked ? 'none' : 'table-cell');"/> All Sessions
			</TD>
		</TR>
		
		<TR>
			<TD valign="top" id="sessionsCell" style="max-width: 700px;">
				<logic:iterate name="applicationConfigForm" property="listSessions" id="s" type="org.unitime.timetable.model.Session">
					<div style="display: inline-block; width: 200px; white-space: nowrap; margin-left: 20px; overflow: hidden;">
						<html:multibox property="sessions"><bean:write name="s" property="uniqueId"/></html:multibox> <bean:write name="s" property="label"/>
					</div>
				</logic:iterate>
			</TD>
			<logic:equal name="applicationConfigForm" property="allSessions" value="true">
				<script>document.getElementById('sessionsCell').style.display = 'none';</script>
			</logic:equal>
		</TR>

		<TR>
			<TD valign="top">Value:</TD>
			<TD valign="top">
				<html:textarea property="value" rows="10" cols="120"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Description:</TD>
			<TD valign="top">
				<html:textarea property="description" rows="5" cols="120"/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="applicationConfigForm" property="op" value="edit">
					<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.updateAppConfig">
						<bean:message key="button.updateAppConfig" />
					</html:submit> 
					<html:submit property="op" styleClass="btn" accesskey="D" onclick="return confirmDelete();" titleKey="title.deleteAppConfig">
						<bean:message key="button.deleteAppConfig" />
					</html:submit> 
				</logic:equal>
				<logic:notEqual name="applicationConfigForm" property="op" value="edit">
					<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.createAppConfig">
						<bean:message key="button.createAppConfig" />
					</html:submit> 
				</logic:notEqual>
				<html:submit property="op" styleClass="btn" accesskey="C" titleKey="title.cancelUpdateAppConfig">
					<bean:message key="button.cancelUpdateAppConfig" />
				</html:submit> 
			</TD>
		</TR>
	</TABLE>
	
</logic:notEqual>
<logic:equal name="applicationConfigForm" property="op" value="list">

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='3'>
			<tt:section-header>
				<tt:section-title>Application Settings</tt:section-title>
				<sec:authorize access="hasPermission(null, null, 'ApplicationConfigEdit')">
					<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.addAppConfig">
						<bean:message key="button.addAppConfig" />
					</html:submit>
				</sec:authorize> 
			</tt:section-header>
		</TD>
	</TR>
	<%= request.getAttribute(org.unitime.timetable.model.ApplicationConfig.APP_CFG_ATTR_NAME) %> 
	<TR>
		<TD colspan="3" align="right">
			<span class="unitime-Hint">s) Applies to current academic session.</span>
		</TD>
	</TR>
</TABLE>

<BR>&nbsp;<BR>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<% 
		WebTable.setOrder(sessionContext,"applicationConfig.ord2",request.getParameter("ord2"),1);
		Vector props = new Vector (ApplicationProperties.getProperties().keySet()); 
		Collections.sort(props);
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(ApplicationProperties.getProperty("tmtbl.appConfig.pattern","^tmtbl\\..*$"));
		} catch (Exception e) {
			Debug.error(e);
			pattern = Pattern.compile("^tmtbl\\..*$");
		}
		WebTable table = new WebTable(2, "Application Properties", "applicationConfig.do?ord2=%%", new String[] {"Name","Value"}, new String[] {"left","left"}, null);
		table.enableHR("#9CB0CE");
		for (Object prop: props) {
			if (!pattern.matcher(prop.toString()).matches()) continue;
			String value = ApplicationProperties.getProperty(prop.toString());
			table.addLine(null, new String[] {prop.toString(), value}, new String[] {prop.toString(), value});
		}			
		out.println(table.printTable(WebTable.getOrder(sessionContext,"applicationConfig.ord2")));
	%>
</TABLE>
</logic:equal>
</html:form>
