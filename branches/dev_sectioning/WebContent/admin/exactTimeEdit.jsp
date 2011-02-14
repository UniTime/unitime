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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %> 

<tiles:importAttribute />

<html:form action="/exactTimeEdit">

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Number of <%=org.unitime.timetable.util.Constants.SLOT_LENGTH_MIN%> minute time slots per meeting &amp; break times</tt:section-title>
					<html:submit property="op" value="Update"/>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<table border="0" cellspacing="0" cellpadding="3">
					<tr>
						<td align='center'><i>Number of minutes<br>per meeting</i></td>
						<td align='center'><i>Number of slots<br>per meeting</i></td>
						<td align='center'><i>Break time</i></td>
					</tr>
					<logic:iterate name="exactTimeEditForm" id="ex" property="exactTimeMins" indexId="idx">
						<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
							<td align='center'>
								<logic:equal name="ex" property="minsPerMtgMax" value="0">
									0
								</logic:equal>
								<logic:notEqual name="ex" property="minsPerMtgMax" value="0">
									<bean:write name="ex" property="minsPerMtgMin"/> .. <bean:write name="ex" property="minsPerMtgMax"/>
								</logic:notEqual>
							</td>
							<td align='center'>
								<html:text property='<%="nrTimeSlots["+idx+"]"%>' size="4" maxlength="4"/>
							</td>
							<td align='center'>
								<html:text property='<%="breakTime["+idx+"]"%>' size="3" maxlength="3"/>
							</td>
						</tr>
					</logic:iterate>
				</table>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-header/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op" value="Update" /> 
			</TD>
		</TR>
	</TABLE>

</html:form>
