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
<%@ page import="org.unitime.timetable.model.Reservation" %>
<%@ page import="org.unitime.timetable.util.Constants" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />
<%
	String frmName = "reservationEditForm";
%>

<html:form action="/reservationEdit">

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
	
		<TR>
			<TD colspan="2" align="right">
				<tt:section-header>
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.reservationNextStep">			
						<bean:message key="button.reservationNextStep" />
					</html:submit>						
				
					<tt:back styleClass="btn" name="Back" title="Return to %%" accesskey="B" back="1" />
					
				</tt:section-header>
			</TD>
		</TR>

	<%@ include file="reservationCommon.jspf" %>

		<TR>
			<TD>
				Type: 
			</TD>
			<TD>
				<html:select name="<%=frmName%>" property="reservationClass">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%= Reservation.RESV_CLASS_REQUEST_ATTR %>" labelProperty="label" property="value" />
				</html:select>
			</TD>
		</TR>
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
			
				<html:submit property="op" 
					styleClass="btn" accesskey="N" titleKey="title.reservationNextStep">			
					<bean:message key="button.reservationNextStep" />
				</html:submit>						
			
				<tt:back styleClass="btn" name="Back" title="Return to %%" accesskey="B" back="1" />
			</TD>
		</TR>

	</TABLE>	
	
</html:form>
	