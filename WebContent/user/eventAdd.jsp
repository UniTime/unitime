<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
 * <bean:write name="eventDetailForm" property="additionalInfo"/> 
--%>

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/eventAdd">
	<input type="hidden" name="op2" value="">
	<html:hidden property="isAddMeetings"/>
	<html:hidden property="eventId"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>  		
						<logic:equal name="eventAddForm" property="isAddMeetings" value="true"><bean:write name="eventAddForm" property="eventName"/> - Add Meetings </logic:equal>
						<logic:notEqual name="eventAddForm" property="isAddMeetings" value="true">Add a New Event</logic:notEqual>
					</tt:section-title>
	<!-- 			<html:submit property="op" styleClass="btn" accesskey="S" 
						title="Show Scheduled Events (Alt+S)" value="Show Scheduled Events"/> -->
					<html:submit property="op" styleClass="btn" accesskey="N" 
						title="Next: Show Location Availability (Alt+N)" value="Show Availability"/>
		 			<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back (Alt+B)" value="Back"/> 
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD nowrap>Event Type: </TD>
			<TD>
				<logic:equal name="eventAddForm" property="isAddMeetings" value="true">
					<bean:write name="eventAddForm" property="eventType"/>
				</logic:equal>
				<logic:notEqual name="eventAddForm" property="isAddMeetings" value="true">
					<html:select name="eventAddForm" property="eventType"
						onfocus="setUp();"
						onkeypress="return selectSearch(event, this);"
						onkeydown="return checkKey(event, this);"
						onchange="op2.value='EventTypeChanged'; submit();">
						<html:options name="eventAddForm" property="eventTypes"/>
					</html:select>
				</logic:notEqual>
			</TD>
		</TR>
<!-- 		<TR>
			<TD>&nbsp;</TD>
		</TR>
		<TR>
 			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>Dates</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
 -->	<TR>
			<TD nowrap>Academic Session: </TD>
			<TD>
				<html:select name="eventAddForm" property="sessionId"  
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);"
					onchange="op2.value='SessionChanged'; submit();" > 
 					<html:optionsCollection property="academicSessions"	label="label" value="value" />
				</html:select>
			</TD>
		</TR>
		<logic:equal name="eventAddForm" property="eventType" value="Special Event">
			<TR>
				<TD nowrap>Expected Attendance: </TD>
				<TD>
					<logic:notEqual name="eventAddForm" property="isAddMeetings" value="true">
						<html:text property="capacity" maxlength="10" size="5"/>
					</logic:notEqual>
					<logic:equal name="eventAddForm" property="isAddMeetings" value="true">
						<bean:write name="eventAddForm" property="capacity"/>
						<html:hidden property="capacity"/>
					</logic:equal>
				</TD>
			</TR>
		</logic:equal>
		<logic:notEqual name="eventAddForm" property="eventType" value="Special Event">
			<html:hidden property="capacity"/>
		</logic:notEqual>
		

<!-- Courses/Classes if this is a course event -->
		<logic:equal name="eventAddForm" property="eventType" value="Course Related Event">

<!-- Not editable if just adding meetings -->
		<logic:equal name="eventAddForm" property="isAddMeetings" value = "true">
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Related Classes / Courses
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<logic:empty scope="request" name="EventAddMeetings.table">
					<i>No relation defined for this event.</i>
				</logic:empty>
				<logic:notEmpty scope="request" name="EventAddMeetings.table">
					<table border='0' cellspacing="0" cellpadding="3" width='100%'>
					<bean:write scope="request" name="EventAddMeetings.table" filter="false"/>
					</table>
				</logic:notEmpty>
			</TD>
		</TR>
		</logic:equal>

<!-- Editable if adding a new event -->
		<logic:notEqual name="eventAddForm" property="isAddMeetings" value="true"> 
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-header>
					<tt:section-title><a name="objects">Classes / Courses</a></tt:section-title>
					<html:submit property="op" styleClass="btn" accesskey="O" 
						title="Add Object (Alt+O)" value="Add Object"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<html:checkbox property="attendanceRequired"/> Students are required to attend this event.
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<table border='0'>
				<tr>
					<td width='100'><i>Subject</i></td>
					<td width='100'><i>Course<br>Number</i></td>
					<td width='170'><i>Config<br>Subpart</i></td>
					<td width='100'><i>Class<br>Number</i></td>
					<td></td>
				</tr>
				<html:hidden property="selected"/>
				<logic:iterate name="eventAddForm" property="subjectAreaList" id="m" indexId="idx">
					<tr><td>
					<html:select style="width:80;" property='<%="subjectArea["+idx+"]"%>' styleId='<%="subjectArea"+idx%>' 
						onfocus="setUp();" 
						onkeypress="return selectSearch(event, this);"
						onchange="<%= \"javascript: doAjax('subjectArea', '\"+idx+\"');\" %>" >
						<html:option value="-1">-</html:option>
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId"/>
					</html:select>
					</td><td>
					<html:select style="width:80;" property='<%="courseNbr["+idx+"]"%>' styleId='<%="courseNbr"+idx%>'
						onfocus='<%= " setUp(); changeWidth(\'courseNbr[" + idx + "]\', 275);" %>'
						onblur='<%= " changeWidth(\'courseNbr[" + idx + "]\', 80)" %>'
						onkeypress="return selectSearch(event, this);"
						onchange="<%= \"javascript: doAjax('courseNbr', '\"+idx+\"');\" %>" >
						<html:optionsCollection property='<%="courseNbrs["+idx+"]"%>' label="value" value="id"/>
					</html:select>
					</td><td>
					<html:select style="width:150;" property='<%="itype["+idx+"]"%>' styleId='<%="itype"+idx%>'
						onchange="<%= \"javascript: doAjax('itype', '\"+idx+\"');\" %>" >
						<html:optionsCollection property='<%="itypes["+idx+"]"%>' label="value" value="id" filter="false"/>
					</html:select>
					</td><td>
					<html:select style="width:80;" property='<%="classNumber["+idx+"]"%>' styleId='<%="classNumber"+idx%>'>
						<html:optionsCollection property='<%="classNumbers["+idx+"]"%>' label="value" value="id"/>
					</html:select>
					</td><td>
					<html:submit property="op" styleClass="btn" onclick="<%=\"selected.value='\"+idx+\"';\"%>"
						title="Delete Course/Class" value="Delete"/>
					</td></tr>
   				</logic:iterate>
				</table>
			</TD>
		</TR>
		</logic:notEqual>
		</logic:equal>
<!-- End of Course Event classes/courses -->
		
		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-header>
					<tt:section-title><a name="objects">Dates / Times</a></tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan = '2'>
				<tt:displayPrefLevelLegend prefs="false" dpBackgrounds="true" separator=""/>
			</TD>
		</TR>
		<TR>
			<TD colspan = '2'>
				<bean:write name="eventAddForm" property="datesTable" filter="false"/>
			</TD>
		</TR>
		
<!--		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>Times</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
-->		
		<TR>
			<TD nowrap>Time: </TD>
			<TD> Start:&nbsp;
				<html:select name="eventAddForm" property="startTime"
					onfocus="setUp();" 
    				onkeypress="return selectSearchTime(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:optionsCollection name="eventAddForm" property="times"/>
				</html:select>
			
				&nbsp;&nbsp;
				Stop: 
				<html:select name="eventAddForm" property="stopTime"
					onfocus="setUp();" 
    				onkeypress="return selectSearchTime(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:optionsCollection name="eventAddForm" property="stopTimes"/>
				</html:select> 
		</TR>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-header>
					<tt:section-title><a name="objects">Locations</a></tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD valign='top' rowspan='2'> 
				Location:
			</TD>
			<TD>
				<table border='0'>
					<tr>
						<logic:empty name="eventAddForm" property="allRoomTypes">
							<font color='red'>No rooms are available for the selected academic session.</font>
						</logic:empty>
						<logic:iterate name="eventAddForm" property="allRoomTypes" id="rf" indexId="rfIdx">
							<td nowrap>
								<html:multibox property="roomTypes">
								<bean:write name="rf" property="uniqueId"/>
								</html:multibox>
								<bean:write name="rf" property="label"/>&nbsp;&nbsp;&nbsp;
							</td>
							<% if (rfIdx%4==3) { %>
								</tr><tr>
							<% } %>
						</logic:iterate>
					</tr>
				</table>
			</TD>
		</TR>
		<TR>
			<!-- <TD>Location: </TD> -->
			<TD>Building:
				<html:select name="eventAddForm" property="buildingId"
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="-1">Select...</html:option>
					<html:optionsCollection name="eventAddForm" property="buildings" label="abbrName" value="uniqueId"/>
				</html:select> 			
			&nbsp; Room Number:&nbsp; <html:text property="roomNumber" maxlength="20" size="10"/>
			&nbsp;&nbsp;&nbsp; <html:checkbox property="lookAtNearLocations"/> Include close by locations
			</TD>
		</TR>
		<!-- 
		<TR>
			<TD> &nbsp; </TD>
			<TD>
				<html:checkbox property="lookAtNearLocations"/> Also look at other locations close by.
			</TD>
		</TR>
		-->
		<TR>
			<TD> 
				Room Capacity:
			</TD>
			<TD>
				Min: <html:text property="minCapacity" maxlength="5" size="5"/> &nbsp; Max: <html:text property="maxCapacity" maxlength="5" size="5"/>
			</TD>
		</TR>

		<TR>
			<TD valign='top'> 
				Room Groups:
			</TD>
			<TD>
				<table border='0'>
					<tr>
						<logic:iterate name="eventAddForm" property="allRoomGroups" id="rf" indexId="rfIdx">
							<td nowrap>
								<html:multibox property="roomGroups">
								<bean:write name="rf" property="uniqueId"/>
								</html:multibox>
								<bean:write name="rf" property="name"/>&nbsp;&nbsp;&nbsp;
							</td>
							<% if (rfIdx%4==3) { %>
								</tr><tr>
							<% } %>
						</logic:iterate>
					</tr>
				</table>
			</TD>
		</TR>

		<TR>
			<TD valign='top'> 
				Room Features:
			</TD>
			<TD>
				<table border='0'>
					<tr>
						<logic:iterate name="eventAddForm" property="allRoomFeatures" id="rf" indexId="rfIdx">
							<td nowrap>
								<html:multibox property="roomFeatures">
								<bean:write name="rf" property="uniqueId"/>
								</html:multibox>
								<bean:write name="rf" property="label"/>&nbsp;&nbsp;&nbsp;
							</td>
							<% if (rfIdx%4==3) { %>
								</tr><tr>
							<% } %>
						</logic:iterate>
					</tr>
				</table>
			</TD>
		</TR>

		<TR>
			<TD colspan = '2'>
				<tt:section-title/>
			</TD>
		</TR>

	<TR>
		<TD colspan = '2' align="right">
<!-- 			<html:submit property="op" styleClass="btn" accesskey="S" 
					title="Show Scheduled Events (Alt+S)" value="Show Scheduled Events"/> -->
					<html:submit property="op" styleClass="btn" accesskey="N" 
						title="Next: Show Location Availability (Alt+N)" value="Show Availability"/>
	  			<html:submit property="op" styleClass="btn" accesskey="B"
				title="Back to List of Events (Alt+B)" value="Back"/>
		</TD>
	</TR>


</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function doAjax(type, idx) {
		var subjAreaObj = document.getElementById('subjectArea'+idx);
		var courseNbrObj = document.getElementById('courseNbr'+idx);
		var itypeObj = document.getElementById('itype'+idx);
		var classNumberObj = document.getElementById('classNumber'+idx);

		var id = null;
		var options = null;
		var next = null;
		
		if (type=='subjectArea') {
			id = subjAreaObj.value;
			options = courseNbrObj.options;
			next = 'courseNbr';
			courseNbrObj.options.length=1;
			itypeObj.options.length=1;
			itypeObj.options[0]=new Option('N/A', '-1', false);
			classNumberObj.options.length=1;
			classNumberObj.options[0]=new Option('N/A', '-1', false);
			if (id==0) return;
		} else if (type=='courseNbr') {
			id = courseNbrObj.value;
			options = itypeObj.options;
			next = 'itype';
			itypeObj.options.length=1;
			classNumberObj.options.length=1;
			classNumberObj.options.length=1;
			classNumberObj.options[0]=new Option('N/A', '-1', false);
			if (id==-1 || id<=<%=Long.MIN_VALUE%>+2) return;
		} else if (type=='itype') {
			id = itypeObj.value;
			options = classNumberObj.options;
			classNumberObj.options.length=1;
			if (id==-1) return;
		}
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			options.length=0;
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						// Course numbers options creation
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							while (optVal.indexOf('_')>=0)
								optVal = optVal.replace("_",String.fromCharCode(160,160,160,160));
							options[i]=new Option(optVal, optId, false);
						}
					}
					if (options.length==1) {
						options[0].selected=true;
						if (next!=null) doAjax(next,idx);
					}
				}
			}
		};
	
		// Request
		var vars = "id="+id+"&type="+type;
		req.open( "POST", "examEditAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
</SCRIPT>
