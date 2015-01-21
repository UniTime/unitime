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
<%@ page import="org.unitime.commons.web.*" %>
<%@ page import="java.text.DecimalFormat" %>
<%@page import="org.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<table width="100%" border="0" cellspacing="0" cellpadding="3">
	<tr><td colspan='5' nowrap>
		<tt:section-header>
			<tt:section-title>Buildings</tt:section-title>
				<table border='0'><tr><td>
				<sec:authorize access="hasPermission(null, 'Session', 'BuildingAdd')">
					<html:form action="buildingEdit" styleClass="FormWithNoPadding">
						<html:hidden property="op" value="Add"/>
						<html:submit onclick="displayLoading();" styleClass="btn" accesskey="A" title="Add Building (Alt+B)" value="Add Building"/>
					</html:form>
				</sec:authorize>
				</td><td>
				<sec:authorize access="hasPermission(null, 'Session', 'BuildingExportPdf')">
					<html:form action="buildingEdit" styleClass="FormWithNoPadding">
						<html:hidden property="op" value="Export PDF"/>
						<html:submit styleClass="btn" accesskey="P" title="Export PDF (Alt+P)" value="Export PDF"/>
					</html:form>
				</sec:authorize>
				</td><td>
				<sec:authorize access="hasPermission(null, 'Session', 'BuildingUpdateData')">
					<html:form action="buildingEdit" styleClass="FormWithNoPadding">
						<html:hidden property="op" value="Update Data"/>
						<html:submit onclick="displayLoading();" styleClass="btn" accesskey="U" title="Synchronize classrooms and computing labs with external rooms (Alt+U)" value="Update Data"/>
					</html:form>
				</sec:authorize>
				</td></tr></table>
		</tt:section-header>
	</td></tr>
	
	<bean:write name="table" scope="request" filter="false"/>

	<TR>
		<TD colspan='5' align="right" class="WelcomeRowHead">
		&nbsp;
		</TD>
	</TR>
	<TR>
		<TD colspan='5' align="right" nowrap width="100%">
				<table border='0'><tr><td>
				<sec:authorize access="hasPermission(null, 'Session', 'BuildingAdd')">
					<html:form action="buildingEdit" styleClass="FormWithNoPadding">
						<html:hidden property="op" value="Add"/>
						<html:submit onclick="displayLoading();" styleClass="btn" accesskey="A" title="Add Building (Alt+B)" value="Add Building"/>
					</html:form>
				</sec:authorize>
				</td><td>
				<sec:authorize access="hasPermission(null, 'Session', 'BuildingExportPdf')">
					<html:form action="buildingEdit" styleClass="FormWithNoPadding">
						<html:hidden property="op" value="Export PDF"/>
						<html:submit styleClass="btn" accesskey="P" title="Export PDF (Alt+P)" value="Export PDF"/>
					</html:form>
				</sec:authorize>
				</td><td>
				<sec:authorize access="hasPermission(null, 'Session', 'BuildingUpdateData')">
					<html:form action="buildingEdit" styleClass="FormWithNoPadding">
						<html:hidden property="op" value="Update Data"/>
						<html:submit onclick="displayLoading();" styleClass="btn" accesskey="U" title="Synchronize classrooms and computing labs with external rooms (Alt+U)" value="Update Data"/>
					</html:form>
				</sec:authorize>
				</td></tr></table>
		</TD>
	</TR>
</table>
