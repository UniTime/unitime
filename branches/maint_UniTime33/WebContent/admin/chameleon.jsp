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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.TimetableManager" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />
<html:form action="/chameleon">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2" valign="middle">
				<tt:section-header>
					<html:submit property="op" 
						styleClass="btn" accesskey="S" titleKey="title.changeUser">			
						<bean:message key="button.changeUser" />
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
		
		<TR>
			<TD width="200">Timetable Manager</TD>
			<TD>
				<html:select name="chameleonForm" property="puid"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%= TimetableManager.MGR_LIST_ATTR_NAME %>" 
						property="externalUniqueId" labelProperty="name"/>
					</html:select>
				
			</TD>
		</TR>
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op" onclick="displayLoading();"
					styleClass="btn" accesskey="S" titleKey="title.changeUser">			
					<bean:message key="button.changeUser" />
				</html:submit>						
			</TD>
		</TR>
	</TABLE>
</html:form>

