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
<table width="90%" border="0" cellspacing="0" cellpadding="3">
<%
    WebTable webTable = new WebTable( 4,
    "Buildings",
    new String[] {"Abbreviation", "Name", "X-Coordinate", "Y-Coordinate"},
    new String[] {"left", "left","right","right"},
    null );
%>

<logic:iterate name="buildingListForm" property="buildings" id="bldg" >
<%
org.unitime.timetable.model.Building b = (org.unitime.timetable.model.Building) bldg;
DecimalFormat df5 = new DecimalFormat("####0");
webTable.addLine(
	null,
	new String[] {
		b.getAbbreviation(),
		b.getName(),
		df5.format(b.getCoordinateX()),
		df5.format(b.getCoordinateY()),
		}, null,null);
%>

</logic:iterate>

<%	out.println( webTable.printTable() ); %>


</table>
