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
<%@page import="org.unitime.timetable.model.ExamType"%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
	
<tiles:importAttribute />
<html:form action="roomList">
	<script language="JavaScript">blToggleHeader('Filter','dispRoomFilter');</script>
	<table border='0' cellspacing="0" cellpadding="3" width="100%">
		<tr><td>
				<b>Department:</b>
				<html:select property="deptCodeX"
					onchange="if (blIsColapsed('dispRoomFilter')) {displayLoading(); submit();}">
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
</html:form>
<logic:messagesPresent>
	<table border='0' cellspacing="0" cellpadding="3" width="100%">
		<tr><td valign="top" align="center">
			<html:errors />
		</td></tr>
		<tr><td>
			<tt:section-title/>
		</td></tr>
		<tr><td valign="middle" align="right">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddRoom')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddRoom')">
						<TD nowrap>
							<html:form action="editRoom" styleClass="FormWithNoPadding">
								<html:hidden property="op" value="Add"/>
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.addRoom">
									<bean:message key="button.addRoom" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddNonUnivLocation')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddNonUnivLocation')">
						<TD nowrap>
							<html:form action="addNonUnivLocation" styleClass="FormWithNoPadding">
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="N" titleKey="title.addNonUnivLocation">
									<bean:message key="button.addNonUnivLocation" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddSpecialUseRoom')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddSpecialUseRoom')">
						<TD nowrap>
							<html:form action="addSpecialUseRoom" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="S" titleKey="title.addSpecialUseRoom">
									<bean:message key="button.addSpecialUseRoom" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
				</TR>
			</TABLE>
		</td></tr>
	</table>
</logic:messagesPresent>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>
