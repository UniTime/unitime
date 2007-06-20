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
<%@ page import="org.unitime.timetable.ApplicationProperties"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Collections"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />

<html:form action="/applicationConfig" focus="key">

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<logic:equal name="applicationConfigForm" property="op" value="edit">
					Edit 
				</logic:equal>
				<logic:notEqual name="applicationConfigForm" property="op" value="edit">
					Add
				</logic:notEqual>
				Configuration Setting
				</DIV>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Key:</TD>
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
			<TD valign="top">Value:</TD>
			<TD valign="top">
				<html:textarea property="value" rows="5" cols="70"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Description:</TD>
			<TD valign="top">
				<html:textarea property="description" rows="5" cols="70"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="applicationConfigForm" property="op" value="edit">
					<html:submit property="op" styleClass="btn" accesskey="A" titleKey="title.updateAppConfig">
						<bean:message key="button.updateAppConfig" />
					</html:submit> 
					<html:submit property="op" styleClass="btn" accesskey="D" titleKey="title.deleteAppConfig">
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
	
</html:form>

<BR>&nbsp;<BR>

<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute(org.unitime.timetable.model.ApplicationConfig.APP_CFG_ATTR_NAME) %> 
</TABLE>

<BR>&nbsp;<BR>

<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan="2"><div class="WelcomeRowHead">Application Properties</div></TD>
	</TR>
	<TR>
		<TD><div class="WebTableHeader">Key</div></TD>
		<TD><div class="WebTableHeader">Value</div></TD>
	</TR>
	
	<% 
		Vector props = new Vector (ApplicationProperties.getProperties().keySet()); 
		Collections.sort(props);
		for (Object prop: props) {
			String value = ApplicationProperties.getProperty(prop.toString());
	%>
	<TR>
		<TD valign="top" class="BottomBorderGray">
			<%if (prop.toString().startsWith("tmtbl")) { out.println("<font color='navy'>"); } %>
			<%= prop %>
			<%if (prop.toString().startsWith("tmtbl")) { out.println("</font>"); } %>
			<% if (value==null || value.length()==0) { out.println("<font class='errorCell'>&nbsp; *</font>"); } %>
		</TD>
		<TD class="BottomBorderGray">&nbsp;<%= value %></TD>
	</TR>
	<%			
		}
	%>
</TABLE>

