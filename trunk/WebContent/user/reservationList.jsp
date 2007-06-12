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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/struts-layout.tld" prefix="layout" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />

<html:form action="/reservationList">
	<TABLE border="0" cellspacing="1" cellpadding="3">
		<TR>
			<TD colspan="5">
				<script language="JavaScript" type="text/javascript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B>View reservations for:</B>
						</TD>
						<TD>
							<html:checkbox property="ioResv" />
							Instructional Offering
						</TD>
					</TR>
					<!-- TODO Reservations - functionality to be made visible later -->
					<!--
					<TR>
						<TD></TD>
						<TD>
							<htm : checkbox property="configResv" />
							Configuration
						</TD>
					</TR>
					-->
					<!-- TODO Reservations Bypass - to be removed later -->
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="courseResv" />
							Course Offering
						</TD>
					</TR>
					<!-- End Bypass -->
					<TR>
						<TD></TD>
						<TD>
							<!-- TODO Reservations Bypass - to be removed later -->
							<html:hidden property="configResv" value="false" />
							<!-- End Bypass -->
							
							<html:checkbox property="classResv" />
							Class
						</TD>
					</TR>

					<!-- TODO Reservations - functionality to be made visible later -->
					<!--
					<TR>
						<TD><B>Display:</B></TD>
						<TD>
							<htm : checkbox property="iResv" />
							Individual Reservations
						</TD>
					</TR>

					<TR>
						<TD>&nbsp;</TD>
						<TD>
							<htm : checkbox property="sgResv" />
							Student Group Reservations
						</TD>
					</TR>

					<TR>
						<TD>&nbsp;</TD>
						<TD>
							<htm : checkbox property="posResv" />
							POS Reservations
						</TD>
					</TR>
					-->

					<TR>
						<TD><B>Display:</B></TD>
						<TD>
							<html:checkbox property="crsResv" />
							Course Reservations
						</TD>
					</TR>

					<TR>
						<TD>&nbsp;</TD>
						<TD>
							<html:checkbox property="aaResv" />
							Academic Area Reservations
						</TD>
					</TR>
				</TABLE>

				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
				<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>
				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');</script>
			
			</TD>
		</TR>


		<TR>
			<TD valign="top"><B>Subject: </B></TD>
			<TD valign="top">
				<html:select name="reservationListForm" property="subjectAreaId"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TD valign="top"><B>Course Number: </B></TD>
			<TD valign="top">
				<layout:suggest 
					suggestAction="/getCourseNumbers" property="courseNbr" styleId="courseNbr" 
					suggestCount="15" size="5" maxlength="5" layout="false" all="true"
					minWordLength="2"
					tooltip="Course numbers can be specified using wildcard (*). E.g. 2*"
					onblur="hideSuggestionList('courseNbr');" />
			</TD>
			<TD valign="top">
				&nbsp;&nbsp;&nbsp;
				<html:submit property="op" 
					accesskey="S" styleClass="btn" titleKey="title.displayReservationList"
					onclick="displayLoading();">
					<bean:message key="button.displayReservationList" />
				</html:submit> 

				<html:submit property="op"
					accesskey="P" styleClass="btn" titleKey="title.exportPDF"
					onclick="displayLoading();">
					<bean:message key="button.exportPDF" />
				</html:submit> 

				<html:submit property="op" 
					accesskey="A" styleClass="btn" titleKey="title.addReservationIo">
					<bean:message key="button.addReservationIo" />
				</html:submit> 
			</TD>
		</TR>
		<TR>
			<TD colspan="5" align="center">
				<html:errors />
			</TD>
		</TR>
	</TABLE>	
</html:form>

<%
	if (request.getAttribute("reservationList")!=null) {
%>
	<script language="javascript">displayLoading();</script>
	<TABLE border="0" cellspacing="1" cellpadding="3">
		<%=request.getAttribute("reservationList")%>
	</TABLE>	
	<script language="javascript">displayElement('loading', false);</script>
<%		
	}
%>
		