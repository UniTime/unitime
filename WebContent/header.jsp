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
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute name="showNavigation" scope="request"/>	

	<div id="loading" style="visibility:hidden;display:none">
		<table border="0" width='100%'>
			<tr>
				<td align="center" valign="middle">
					<img align="middle" vspace="5" border="0" src="images/loading.gif">
				</td>
			</tr>
		</table>
	</div>
	
	<logic:equal name="showNavigation" value="true"> 
		<% if (!"hide".equals(request.getParameter("menu"))) { %>
		<tt:has-back>
			<table border="0" width='100%'>
				<tr>
					<td style="max-width: 800px;">
						<tt:back styleClass="btn" name="[&larr;]" title="Return to %%"/>
						<tt:back-tree/>
						<tt:gwt-back/>
					</td>
				</tr>
			</table>
		</tt:has-back>
		<% } %>
	</logic:equal>
