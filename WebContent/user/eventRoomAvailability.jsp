<!-- 
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
-->

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<script language='JavaScript'>
function tClick(date,location) {
	var x = document.getElementById('x'+date+'_'+location);
	var td = document.getElementById('td'+date+'_'+location);
	if (x.value==1) {
		x.value=0;
		if (td.innerHTML=='&nbsp\;')  
			td.style.backgroundColor='transparent';
		else
			td.style.backgroundColor='rgb(200,200,200)';
	} else {
		x.value=1;
		td.style.backgroundColor='yellow';
	}
}
function tOver(source,date,location) {
	source.style.cursor='hand';source.style.cursor='pointer';
}
function tOut(date,location) {
}
function tAll(location, admin) {
	var allSelected = true;
	for (var i=0;i<tDates.length;i++) {
		var x = document.getElementById('x'+tDates[i]+'_'+location);
		var td = document.getElementById('td'+tDates[i]+'_'+location);
		var unused = (td.innerHTML=='&nbsp;');
		if ((unused||admin) && x.value==0) {
			allSelected = false; break;
		}
	}
	for (var i=0;i<tDates.length;i++) {
		var x = document.getElementById('x'+tDates[i]+'_'+location);
		var td = document.getElementById('td'+tDates[i]+'_'+location);
		var unused = (td.innerHTML=='&nbsp;');
		x.value=(allSelected?0:(unused||admin?1:0));
		if (x.value==1)
			td.style.backgroundColor='yellow';
		else if (unused)
			td.style.backgroundColor='transparent';
		else
			td.style.backgroundColor='rgb(200,200,200)';
	}
}
</script>


<html:form action="/eventRoomAvailability">
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
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
						Available Rooms For 
						<bean:write name="eventRoomAvailabilityForm" property="startTimeString"/>
						 - <bean:write name="eventRoomAvailabilityForm" property="stopTimeString"/>
					</tt:section-title>
					<html:submit property="op" styleClass="btn" accesskey="N"
						title="Continue With Reservation (Alt+N)" value="Continue"/>
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Change Request (Alt+R)" value="Change Request"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD>
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD>
				<bean:write name="eventRoomAvailabilityForm" property="availabilityTable" filter="false"/>
			</TD>
		</TR>
		<TR>
			<TD>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan = '2' align="right">
					<html:submit property="op" styleClass="btn" accesskey="N"
						title="Continue With Reservation (Alt+N)" value="Continue"/>
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Change Request (Alt+R)" value="Change Request"/>
			</TD>
		</TR>

</TABLE>
</html:form>
