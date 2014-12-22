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
<%@ page import="org.unitime.timetable.webutil.timegrid.TimetableGridTable" %>
<%@ page import="org.unitime.commons.Debug" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/timetable">
<%
try {
%>
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>Week:</TD>
			<TD>
				<html:select property="week">
					<html:optionsCollection name="timetableForm" property="weeks" label="value" value="id"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Resource:</TD>
			<TD>
				<html:select property="resource" onchange="document.getElementById('show_instructors').style.display=(this.value=='Instructor'?'none':''); document.getElementById('show_events').style.display=(this.value=='Room' || this.value=='Instructor'?'':'none');">
					<html:options name="timetableForm" property="resources"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Filter:</TD>
			<TD>
				<html:text property="find" maxlength="1000" size="40"/>
			</TD>
		</TR>
		<TR>
			<TD>Day:</TD>
			<TD>
				<html:select property="day">
					<html:options name="timetableForm" property="days"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Daytime/Evening:</TD>
			<TD>
				<html:select property="dayMode">
					<html:options name="timetableForm" property="dayModes"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Display Mode:</TD>
			<TD>
				<html:select property="dispMode">
					<html:options name="timetableForm" property="dispModes"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Background:</TD>
			<TD>
				<html:select property="bgColor">
					<html:options name="timetableForm" property="bgColors"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Show discouraged free times:</TD>
			<TD>
				<html:checkbox property="showUselessTimes"/>
			</TD>
		</TR>
		<TR>
			<TD>Show preferences:</TD>
			<TD>
				<html:checkbox property="showComments"/>
			</TD>
		</TR>
		<TR id="show_instructors">
			<TD>Show instructors:</TD>
			<TD>
				<html:checkbox property="showInstructors"/>
			</TD>
		</TR>
		<TR id="show_events">
			<TD>Show events:</TD>
			<TD>
				<html:checkbox property="showEvents"/>
			</TD>
		</TR>
		<TR>
			<TD>Show times:</TD>
			<TD>
				<html:checkbox property="showTimes"/>
			</TD>
		</TR>
		<TR>
			<TD>Order By:</TD>
			<TD>
				<html:select property="orderBy">
					<html:options name="timetableForm" property="orderBys"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Change"/>
				<html:submit property="op" value="Export PDF"/>
				<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>
	<logic:notEqual name="timetableForm" property="resource" value="Room">
		<logic:notEqual name="timetableForm" property="resource" value="Instructor">
			<script language="JavaScript">document.getElementById('show_events').style.display='none';</script>
		</logic:notEqual>
	</logic:notEqual>
	<logic:equal name="timetableForm" property="resource" value="Instructor">
		<script language="JavaScript">document.getElementById('show_instructors').style.display='none';</script>
	</logic:equal>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit property="op" value="Export PDF"/>
					<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	
	<br><br>
	<a name='timetable'></a>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						Timetable
					</tt:section-title>
					<A class="l7" href="#legend">Legend</A>&nbsp;
				</tt:section-header>
			</TD>
		</TR>
		<logic:equal name="timetableForm" property="loaded" value="false">
				<TR>
					<TD>
						<i>Neither a solver is started nor solution is selected.</i>
					</TD>
				</TR>
		</logic:equal>
		<logic:equal name="timetableForm" property="loaded" value="true">	
<%
			TimetableGridTable table = (TimetableGridTable)session.getAttribute("Timetable.table");
			if (table.models().isEmpty()) { 
%>
				<TR>
					<TD>
						<i>No resource matches the above criteria (or there is no resource at all).</i>
					</TD>
				</TR>
			</TABLE>
<%
			} else {
%>
			</TABLE>
	<script language="JavaScript">
		function mOvr(assignmentId) {
			var x = document.getElementsByName('c'+assignmentId);
			for (var i = 0; i < x.length; i++)
				x[i].style.backgroundColor='rgb(223,231,242)';
		}
		function mOut(assignmentId, bgColor) {
			var x = document.getElementsByName('c'+assignmentId);
			for (var i = 0; i < x.length; i++)
				x[i].style.backgroundColor=bgColor;
		}
		function mHnd(source) {
			source.style.cursor='hand';
			source.style.cursor='pointer';
		}
	</script>
<%

				table.printToHtml(out);
			}
%>
		
	<BR>
	<a name='legend'></a>
	<TABLE width="100%" border="0" >
		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						Legend
					</tt:section-title>
					<A class="l7" href="#timetable">Timetable</A>&nbsp;
				</tt:section-header>
			</TD>
		</TR>
<%
			table.printLegend(out);
%>
		</logic:equal>
	</TABLE>
<%
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>
