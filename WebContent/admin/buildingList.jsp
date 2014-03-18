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
<%@ page import="org.unitime.commons.web.*" %>
<%@ page import="java.text.DecimalFormat" %>
<%@page import="org.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
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
