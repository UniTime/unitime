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
<%@page import="org.unitime.timetable.model.ExamType"%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
	
<tiles:importAttribute />
<html:form action="roomList">
	<script language="JavaScript">blToggleHeader('Filter','dispRoomFilter');</script>
	<table border='0' cellspacing="0" cellpadding="3" width="100%">
		<tr><td>
				<b>Department:</b>
				<html:select property="deptCodeX"
					onchange="if (blIsColapsed('dispRoomFilter')) {displayLoading(); submit();}"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:option value="<%=Constants.ALL_OPTION_VALUE%>">All Managed</html:option>
					<tt:canSeeExams>
						<logic:iterate scope="request" name="examTypes" id="type" type="org.unitime.timetable.model.ExamType">
							<html:option value='<%="Exam" + type.getUniqueId() %>'>All <bean:write name="type" property="label"/> Examination Rooms</html:option>
						</logic:iterate>
					</tt:canSeeExams>
					<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
				</html:select>
		</td><td align="right" valign="top">
					<html:submit property="op" onclick="displayLoading();" accesskey="S" styleClass="btn" titleKey="title.searchRooms">
						<bean:message key="button.search" />
					</html:submit>
					<sec:authorize access="hasPermission(null, 'Department', 'RoomsExportPdf')">
						&nbsp;&nbsp;
						<html:submit property="op" accesskey="P" styleClass="btn" titleKey="title.exportPDF">
							<bean:message key="button.exportPDF" />
						</html:submit>
					</sec:authorize>
					<sec:authorize access="hasPermission(null, 'Department', 'RoomsExportCsv')">
						&nbsp;&nbsp;
						<html:submit property="op" accesskey="C" styleClass="btn" titleKey="title.exportCSV">
							<bean:message key="button.exportCSV" />
						</html:submit>
					</sec:authorize>
		</td></tr>
	</table>
	<script language="JavaScript">blStart('dispRoomFilter');</script>
	<table border='0' cellspacing="0" cellpadding="3" width="100%">
		<tr><td nowrap>
					Capacity:
					<html:text property="minRoomSize" size="5" maxlength="5"/> - <html:text property="maxRoomSize" size="5" maxlength="5"/>
		</td><td nowrap>
					Name:
					<html:text property="filter" size="50" maxlength="100"/>
		</td></tr>
	</table>
	<table border='0' cellspacing="0" cellpadding="3" width="100%">
		<tr>
			<td nowrap>Room Types:</td>
			<logic:iterate name="roomListForm" property="allRoomTypes" id="rf" indexId="rfIdx">
				<td nowrap>
					<html:multibox property="roomTypes">
						<bean:write name="rf" property="uniqueId"/>
					</html:multibox>
					<bean:write name="rf" property="label"/>&nbsp;&nbsp;&nbsp;
				</td>
				<% if (rfIdx%4==3) { %>
					</tr><tr><td></td>
				<% } %>
			</logic:iterate>
		</tr><tr>
			<td nowrap>Room Groups:</td>
			<logic:iterate name="roomListForm" property="allRoomGroups" id="rf" indexId="rfIdx">
				<td nowrap>
					<html:multibox property="roomGroups">
						<bean:write name="rf" property="uniqueId"/>
					</html:multibox>
					<bean:write name="rf" property="name"/>&nbsp;&nbsp;&nbsp;
				</td>
				<% if (rfIdx%4==3) { %>
					</tr><tr><td></td>
				<% } %>
			</logic:iterate>
		</tr>
		<logic:iterate name="roomListForm" property="roomFeatureTypes" id="ft" type="org.unitime.timetable.model.RoomFeatureType">
			<tr>
				<td nowrap><bean:write name="ft" property="label"/>:</td>
				<logic:iterate name="roomListForm" property='<%="allRoomFeatures("+ft.getUniqueId()+")"%>' id="rf" indexId="rfIdx">
					<td nowrap>
						<html:multibox property="roomFeatures">
							<bean:write name="rf" property="uniqueId"/>
						</html:multibox>
						<bean:write name="rf" property="label"/>&nbsp;&nbsp;&nbsp;
					</td>
					<% if (rfIdx%4==3) { %>
						</tr><tr><td></td>
					<% } %>
				</logic:iterate>
			</tr>
		</logic:iterate>
	</table>
	<script language="JavaScript">blEnd('dispRoomFilter');blStartCollapsed('dispRoomFilter');</script>
		<br/>
	<script language="JavaScript">blEndCollapsed('dispRoomFilter');</script>
	<table border='0' cellspacing="0" cellpadding="3" width="100%">
		<tr><td valign="top" align="center">
			<html:errors />			
		</td></tr>
	</table>
</html:form>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>
