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
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %> 

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
