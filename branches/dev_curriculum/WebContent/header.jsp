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
--%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute name="showNavigation" scope="request"/>	

<%--			
	<BR>
	<TABLE border="0" width="100%" cellspacing="0" cellpadding="0">
		<TR>
			<TD width="200" nowrap>
				<DIV id="loading" style="visibility:hidden;display:none">
					&nbsp; &nbsp; &nbsp; 
					<IMG align="middle" vspace="5" border="0" src="images/loading.gif">
				</DIV>
			</TD>

			<logic:notEqual name="helpFile" value=""> 
			<TD align="center" width="40">
				<A href='help.do?helpFile=<%=request.getAttribute("helpFile").toString()%>' accesskey="H" title="Help (Alt+H)" target="helpWindow"><IMG border="0" align="middle" src="images/Help24.gif" title="Help (Alt+H)" alt="Help (Alt+H)"></A>
			</TD>
			</logic:notEqual>
		
			<TD height="45" align="right" class="WelcomeHead" valign='middle'>
				<tt:wiki-help><bean:write scope="request" name="title"/></tt:wiki-help>
			</TD>
			
			<TD width="55" align="right">
				<IMG align="middle" src="images/logosmall.jpg" border="0">
			</TD>
			
			<TD width="40">&nbsp;</TD>
		</TR>
	</TABLE>
	--%>
	
	<DIV id="loading" style="visibility:hidden;display:none">
		&nbsp; &nbsp; &nbsp; 
		<IMG align="middle" vspace="5" border="0" src="images/loading.gif">
	</DIV>
	
	<logic:equal name="showNavigation" value="true"> 
		<tt:has-back>
			<TABLE border="0" width='100%'>
				<TR>
					<TD width="40">&nbsp;</TD>
					<TD>
						<tt:back styleClass="btn" name="[&larr;]" title="Return to %%"/>
						<tt:back-tree/>
					</TD>
					<TD width="40">&nbsp;</TD>
				</TR>
			</TABLE>
		</tt:has-back>
	</logic:equal>
<BR>
