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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.EditRoomPrefForm" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomPrefForm";	
	EditRoomPrefForm frm = (EditRoomPrefForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/editRoomPref">
	<html:hidden property="id"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomPref">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomDetail" />
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
		
		<logic:iterate name="<%=frmName%>" property="depts" id="dept" indexId="ctr">
		<% Department d = (Department) dept; %>
		<TR>
			<TD nowrap><%=d.getDeptCode().trim() + " - " + d.getName().trim()%>&nbsp;&nbsp;&nbsp;
			</TD>
			
			<TD width='100%'>
				<html:select property='<%= "roomPrefLevels[" + ctr + "]" %>' >			
					<logic:iterate scope="request" name="<%=PreferenceLevel.PREF_LEVEL_ATTR_NAME%>" id="prLevel">
						<% PreferenceLevel pr = (PreferenceLevel)prLevel; %>			
					<html:option
						style='<%="background-color:" + pr.prefcolor() + ";"%>'
						value="<%=pr.getUniqueId().toString()%>" >
						<%=pr.getPrefName()%>
					</html:option>
				   	</logic:iterate>
				</html:select>
			</TD>

		</TR>
		</logic:iterate>
						
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomPref">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomDetail" />
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
