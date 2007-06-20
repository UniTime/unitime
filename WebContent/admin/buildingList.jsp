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
<%@ page import="org.unitime.commons.web.*" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<table width="90%" border="0" cellspacing="0" cellpadding="3">
	<tr><td colspan='5' nowrap>
		<tt:section-header>
			<tt:section-title>Buildings</tt:section-title>
				<html:form action="buildingEdit" styleClass="FormWithNoPadding">
					<html:hidden property="op" value="Add"/>
					<html:submit onclick="displayLoading();" styleClass="btn" accesskey="A" title="Add Building (Alt+B)" value="Add Building"/>
				</html:form>
				</td><td nowrap>
				<html:form action="buildingEdit" styleClass="FormWithNoPadding">
					<html:hidden property="op" value="Export PDF"/>
					<html:submit onclick="displayLoading();" styleClass="btn" accesskey="P" title="Export PDF (Alt+P)" value="Export PDF"/>
				</html:form>
		</tt:section-header>
	</td></tr>
<%
    WebTable webTable = new WebTable( 5,
    null, "buildingList.do?ord=%%",
    new String[] {"Abbreviation", "Name", "External Id", "X-Coordinate", "Y-Coordinate"},
    new String[] {"left", "left","left","right","right"},
    new boolean[] {true,true,true,true,true} );
    WebTable.setOrder(session, "BuildingList.ord", request.getParameter("ord"), 1);
%>

<logic:iterate name="buildingListForm" property="buildings" id="bldg" >
<%
org.unitime.timetable.model.Building b = (org.unitime.timetable.model.Building) bldg;
DecimalFormat df5 = new DecimalFormat("####0");
webTable.addLine(
	"onClick=\"document.location='buildingEdit.do?op=Edit&id="+b.getUniqueId()+"';\"",
	new String[] {
		b.getAbbreviation(),
		b.getName(),
		b.getExternalUniqueId()==null?"<i>N/A</i>":b.getExternalUniqueId().toString(),
		df5.format(b.getCoordinateX()),
		df5.format(b.getCoordinateY()),
		}, 
	new Comparable[] {
		b.getAbbreviation(),
		b.getName(),
		b.getExternalUniqueId(),
		b.getCoordinateX(),
		b.getCoordinateY(),
		});
%>

</logic:iterate>

<%	out.println( webTable.printTable(WebTable.getOrder(session, "BuildingList.ord")) ); %>


	<TR>
		<TD colspan='5' align="right" class="WelcomeRowHead">
		&nbsp;
		</TD>
	</TR>
	<TR>
		<TD colspan='4' align="right" nowrap width="99%">
				<html:form action="buildingEdit" styleClass="FormWithNoPadding">
					<html:hidden property="op" value="Add"/>
					<html:submit onclick="displayLoading();" styleClass="btn" accesskey="A" title="Add Building (Alt+B)" value="Add Building"/>
				</html:form>
		</td><td nowrap align="right" >
				<html:form action="buildingEdit" styleClass="FormWithNoPadding">
					<html:hidden property="op" value="Export PDF"/>
					<html:submit onclick="displayLoading();" styleClass="btn" accesskey="P" title="Export PDF (Alt+P)" value="Export PDF"/>
				</html:form>
		</TD>
	</TR>
</table>
