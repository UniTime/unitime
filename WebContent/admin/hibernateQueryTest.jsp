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
<%@ page language="java" %>
<%@ page errorPage="../error.jsp" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<html:form action="/hibernateQueryTest">

	<TABLE align="left" width="95%">
		<TR>
			<TD>
				<DIV class="WelcomeRowHead">HQL</DIV>
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

		<TR>
			<TD align="center">
				<html:textarea property="query" rows="7" cols="80"></html:textarea>
			</TD>
		</TR>

		<TR>
			<TD align="center" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD align="right">
				<html:submit property="op">
					<bean:message key="button.submit" />
				</html:submit>
			</TD>
		</TR>

		<TR>
			<TD align="center">
				Resultset Size: <bean:write name="hibernateQueryTestForm" property="listSize" />
			</TD>
		</TR>

	</TABLE>

</html:form>	