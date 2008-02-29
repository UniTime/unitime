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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.ui.ConflictStatisticsInfo" %>
<%@ page import="org.unitime.timetable.model.Solution" %>
<%@ page import="org.unitime.timetable.model.dao.SolutionDAO" %>
<%@ page import="org.unitime.commons.Debug" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<script language="JavaScript">
	function go(evt) {
		if (evt.ctrlKey || evt.altKey) return;
		s = 'suggestions.do?op=Try&id='+suggestionsForm['id'].value;
		for (i=0;i<suggestionsForm.nrRooms.value;i++) {
			if (suggestionsForm['room'+i].value=='') {
				/*
				if (i==1)
					alert('Room not selected.');
				else if (i==2)
					alert('Second room not selected.');
				else if (i==3)
					alert('Third room not selected.');
				else 
					alert(i+'.th room not selected.');
				*/
				return;
			}
			for (j=0;j<i;j++) {
				if (suggestionsForm['room'+j].value==suggestionsForm['room'+i].value) {
					/*
					alert(i+'. selected room is the same as '+j+'. selected room.');
					*/
					return;
				}
			}
			s += '&room'+i+'='+suggestionsForm['room'+i].value;
		}

		if (suggestionsForm['days'].value=='') {
			/*
			alert("Time not selected.");
			*/
			return;
		}
		
		
		if (suggestionsForm.roomState.value==1 && suggestionsForm.nrRooms.value>0) {
			return;
		}
		
		s += '&days='+suggestionsForm['days'].value;
		s += '&slot='+suggestionsForm['slot'].value;
		s += '&pattern='+suggestionsForm['pattern'].value;
		
		displayLoading();
		document.location = s;
	}
	
	function selectRoom(evt, roomId) {
		if (suggestionsForm.roomState.value==0)
			suggestionsForm.roomState.value=1;
	
		for (i=0;i<suggestionsForm.nrRooms.value;i++) {
			if (i!=suggestionsForm.curRoom.value && suggestionsForm['room'+i].value==roomId) {
				go(evt); return;
			}
		}

		if (suggestionsForm['room'+suggestionsForm.curRoom.value].value!='')
			document.getElementById('room_'+suggestionsForm['room'+suggestionsForm.curRoom.value].value).style.border='none';	
		
		document.getElementById('room_'+roomId).style.border='black 1px dashed';
		suggestionsForm['room'+suggestionsForm.curRoom.value].value=roomId;
		
		suggestionsForm.curRoom.value ++;
		if (suggestionsForm.curRoom.value>=suggestionsForm.nrRooms.value) {
			suggestionsForm.curRoom.value = 0;
			suggestionsForm.roomState.value = 2;
		}
		
		go(evt);
  }
  
  function selectTime(evt, days, slot, pattern) {
		if (suggestionsForm['days'].value!='')
			document.getElementById('time_'+suggestionsForm['days'].value+"_"+suggestionsForm['slot'].value+"_"+suggestionsForm['pattern'].value).style.border='none';	
		suggestionsForm['days'].value = days;
		suggestionsForm['slot'].value = slot;
		suggestionsForm['pattern'].value = pattern;
		document.getElementById('time_'+days+"_"+slot+"_"+pattern).style.border='black 1px dashed';
		
		go(evt);
  }
  
  function initRooms() {
		for (i=0;i<suggestionsForm.nrRooms.value;i++) {
			if (suggestionsForm['room'+i].value!='') {
				document.getElementById('room_'+suggestionsForm['room'+i].value).style.border='black 1px dashed';
			}
		}  	
  }
  
  function initTime() {
		if (suggestionsForm['days'].value!='')
			document.getElementById('time_'+suggestionsForm['days'].value+"_"+suggestionsForm['slot'].value+"_"+suggestionsForm['pattern'].value).style.border='black 1px dashed';	
  }
  
</script>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/suggestions">
<logic:equal name="suggestionsForm" property="op" value="close">
<script language="JavaScript" type="text/javascript">
window.close();
</script>
</logic:equal>
<%
try {
%>
<logic:equal name="suggestionsForm" property="displayCBS" value="true">
<%
	ConflictStatisticsInfo.printHtmlHeader(out);
%>
</logic:equal>
	<html:hidden property="id" /> 
	<html:hidden property="depth" /> 
	<html:hidden property="timeout" /> 
	<script language="JavaScript">blToggleHeader('Filter','dispSugFilter');blStart('dispSugFilter');</script>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width='10%'>Display conflict table:</TD>
			<TD>
				<html:checkbox property="displayConfTable"/>
			</TD>
		</TR>
		<TR>
			<TD>Display suggestions:</TD>
			<TD>
				<html:checkbox property="displaySuggestions"/>
			</TD>
		</TR>
		<TR>
			<TD>Display placements:</TD>
			<TD>
				<html:checkbox property="displayPlacements"/>
			</TD>
		</TR>
		<TR>
			<TD width='10%' nowrap>Display conflict statistics:</TD>
			<TD>
				<html:checkbox property="displayCBS"/>
			</TD>
		</TR>
		<TR>
			<TD>Simplified mode:</TD>
			<TD>
				<html:checkbox property="simpleMode"/>
			</TD>
		</TR>
		<logic:equal name="suggestionsForm" property="canAllowBreakHard" value="true">
			<TR>
				<TD nowrap>Allow breaking of hard constraints:</TD>
				<TD>
					<html:checkbox property="allowBreakHard"/>
				</TD>
			</TR>
		</logic:equal>
		<TR>
			<TD nowrap style='border-bottom:1px black dashed;'>Maximum number of suggestions/placements:</TD>
			<TD style='border-bottom:1px black dashed;'>
				<html:text property="limit" maxlength="4" size="4"/>
			</TD>
		</TR>
		</table>
	<script language="JavaScript">blEnd('dispSugFilter');blStartCollapsed('dispSugFilter');</script>
		</table>
	<script language="JavaScript">blEndCollapsed('dispSugFilter');</script>	
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width="10%" nowrap>Allow placements:</TD>
			<TD>
				<html:select property="filter">
					<html:options name="suggestionsForm" property="filters"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Text filter:</TD>
			<TD>
				<html:text property="filterText" maxlength="1000" size="20"/>
			</TD>
		</TR>
		<TR>
			<TD nowrap>Room size filter:</TD>
			<TD>
				&lt;
				<html:text property="minRoomSizeText" maxlength="4" size="4"/> 
				...
				<html:text property="maxRoomSizeText" maxlength="4" size="4"/>
				&gt;
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" title="Apply changes" value="Apply"/>
				<html:button onclick="displayLoading();" accesskey="C" title="Close window (Alt+C)" property="op" value="Close" onclick="window.close();"/>
			</TD>
		</TR>
		</TABLE>
<%
	if (request.getAttribute("Suggestions.assignmentInfo")!=null) {
%>
		<br><br>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Current Assignment of <%=request.getAttribute("Suggestions.assignment")%>
					</DIV>
				</TD>
			</TR>
			<%=request.getAttribute("Suggestions.assignmentInfo")%>
			<% if (request.getAttribute("Suggestions.currentAssignmentMessage")!=null) {%><%=request.getAttribute("Suggestions.currentAssignmentMessage")%><%}%>
			<TR>
				<TD colspan='2'>
					<tt:displayPrefLevelLegend/>
				</TD>
			</TR>
		</TABLE>
<%
	}
%>
	<logic:equal name="suggestionsForm" property="showFilter" value="false">
		<BR><BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Suggestions
					</DIV>
				</TD>
			</TR>
			<TR>
				<TD>
					<I>No timetable is loaded. However, you can load one <a target='__idContentFrame' href='listSolutions.do' onclick='window.close();'>here</a>.</I>
				</TD>
			</TR>
		</TABLE>
	</logic:equal>

<%
	String selectedAssignment = (String)request.getAttribute("Suggestions.selectedAssignments");
	String selectedSuggestion = (String)request.getAttribute("Suggestions.selectedSuggestion");
	String conflictAssignments = (String)request.getAttribute("Suggestions.conflictAssignments");
	String selectedInfo = (String)request.getAttribute("Suggestions.selectedInfo");
	String suggestions = (String)request.getAttribute("Suggestions.suggestions");
	String suggestionsMessage = (String)request.getAttribute("Suggestions.suggestionsMessage");
	String placements = (String)request.getAttribute("Suggestions.placements");
	String placementsMessage = (String)request.getAttribute("Suggestions.placementsMessage");
	String conftable = (String)request.getAttribute("Suggestions.confTable");
	if (selectedAssignment!=null || selectedSuggestion!=null || conflictAssignments!=null) {
%>
		<BR><BR>	
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<%=(selectedAssignment==null?"":selectedAssignment)%>
			<%=(selectedSuggestion==null?"":selectedSuggestion)%>
			<%=(conflictAssignments==null?"":conflictAssignments)%>
		</TABLE>
<%
		if (selectedInfo!=null) {
%>
			<BR>
			<TABLE border="0" cellspacing="0" cellpadding="3" width="90%">
				<%=selectedInfo%>
				<TR><TD align='right' colspan='3'>
					<tt:section-header/>
				</TD></TR>
				<TR><TD align='right' colspan='3'>
					<html:submit onclick="displayLoading();" property="op" value="Assign"/>
				</TD></TR>
			</TABLE>
<%
		}
	}
%>
<%
	if (conftable!=null) {
%>
		<BR><BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<%=conftable%>
		</TABLE>
<%
	}
%>
<%
	if (suggestions!=null) {
%>
		<BR><BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<%=suggestions%>
		</TABLE>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD><I><%=(suggestionsMessage==null?"":suggestionsMessage)%></I></TD>
				<TD align='right'>
					<html:submit onclick="displayLoading();" accesskey="D" property="op" value="Search Deeper"/>
					<logic:equal name="suggestionsForm" property="timeoutReached" value="true">
						<html:submit onclick="displayLoading();" accesskey="L" property="op" value="Search Longer"/>
					</logic:equal>
				</TD>
			</TR>
		</TABLE>
<%
	}
%>
<%
	if (placements!=null) {
%>
		<BR><BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<%=placements%>
		</TABLE>
<%
		if (placementsMessage!=null) {
%>
			<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
				<TR><TD><I><%=placementsMessage%></I></TD></TR>
			</TABLE>
<%
		}
%>
<%
	}
%>
<logic:equal name="suggestionsForm" property="displayCBS" value="true">
<%
	ConflictStatisticsInfo cbs = null;
	SolverProxy solver = WebSolver.getSolver(session);
	Long classId = (Long)request.getAttribute("Suggestions.id");
	if (classId!=null) {
		if (solver!=null) {
			cbs = solver.getCbsInfo(classId);
		} else {
			String solutionIdsStr = (String)session.getAttribute("Solver.selectedSolutionId");
			if (solutionIdsStr!=null && solutionIdsStr.length()==0) {
				for (StringTokenizer s=new StringTokenizer(solutionIdsStr);s.hasMoreTokens();) {
					Long solutionId = Long.valueOf(s.nextToken());
					Solution solution = (new SolutionDAO()).get(solutionId);
					if (solution==null) continue;
					ConflictStatisticsInfo x = (ConflictStatisticsInfo)solution.getInfo("CBSInfo");
					if (x==null) continue;
					if (cbs==null) cbs = x;
					else cbs.merge(x);
				}
			}
		}
	}
	if (cbs!=null && classId!=null && cbs.getCBS(classId)!=null) {
%>
	<BR>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				Conflict-based Statistics
				</DIV>
			</TD>
		</TR>
	</TABLE>
	<font size='2'>
<%
	cbs.printHtml(out, classId, 1.0, ConflictStatisticsInfo.TYPE_CONSTRAINT_BASED, true);
%>
	</font>
<%
	}
%>
</logic:equal>
<%
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>