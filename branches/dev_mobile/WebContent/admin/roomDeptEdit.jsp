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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />
<html:form action="/roomDeptEdit">
	<html:hidden property="id"/>
	<html:hidden property="examType"/>
	<input type='hidden' name='ord' value=''>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title><bean:write name="roomDeptEditForm" property="name"/></tt:section-title>
					<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" 
							title="Update Room Department (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					<html:submit property="op"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomList" />
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>

		<logic:messagesPresent>
			<TR>
				<TD align="left" class="errorCell">
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

		<tr><td><table border="0" cellspacing="0" cellpadding="3" width='100%'>
			<bean:write name="roomDeptEditForm" property="table" filter="false"/>
		</table></td></tr>
		
		<tr><td><tt:section-title/></td></tr>

		<TR>
			<TD align='right'>
				<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" 
						title="Update Room Department (Alt+U)">
					<bean:message key="button.update" />
				</html:submit>
				<html:submit property="op"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
					<bean:message key="button.returnToRoomList" />
				</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
