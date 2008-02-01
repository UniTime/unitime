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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.EditRoomPerPrefForm" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomPerPrefForm";	
	EditRoomPerPrefForm frm = (EditRoomPerPrefForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/editRoomPerPref">
	<html:hidden property="id"/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><bean:write name="editRoomPerPrefForm" property="name"/></tt:section-title>
					<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.update">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.back">
						<bean:message key="button.returnToDetail" />
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
		
		<tr><td colspan='2'><table broder='0' cellspacing="0" cellpadding="3">
		<logic:iterate name="editRoomPerPrefForm" property="periods" id="period" indexId="pidx">
		<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
			<TD nowrap>
				<bean:write name="period"/>
			</TD>
			<logic:iterate name="editRoomPerPrefForm" property="preferenceLevels" id="preference">
				<% PreferenceLevel p = (PreferenceLevel)preference; %>
				<TD>
					<html:radio property="<%="pref["+pidx+"]"%>" value="<%=p.getPrefProlog()%>">&nbsp;
						<font color="<%=PreferenceLevel.prolog2color(p.getPrefProlog())%>">
							<%=p.getPrefName()%>
						</font>&nbsp;&nbsp;&nbsp;&nbsp;
					</html:radio>					
				</TD>
			</logic:iterate>
		</TR>
		</logic:iterate>
		</table></td></tr>


		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.update">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.back">
						<bean:message key="button.returnToDetail" />
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>