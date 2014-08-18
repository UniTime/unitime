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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Building" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.SpecialUseRoomForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<%
	// Get Form 
	String frmName = "specialUseRoomForm";	
	SpecialUseRoomForm frm = (SpecialUseRoomForm) request.getAttribute(frmName);
%>	
<tiles:importAttribute />

<html:form action="/addSpecialUseRoom" focus="deptCode">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header title="Add Special Use Room">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Special Use Room (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>
	
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
				<TD>Department:</TD>
				<TD>
					<html:select property="deptCode">
						<logic:empty name="<%=frmName%>" property="deptCode">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:empty>
						<logic:equal name="<%=frmName%>" property="deptCode" value="<%=Constants.ALL_OPTION_VALUE%>">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:equal>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
				
			<TR>
				<TD>Building:</TD>
				<TD>
					<html:select property="bldgId">  
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Building.BLDG_LIST_ATTR_NAME%>" 
							property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
			
			<TR>
				<TD>Room Number:</TD>
				<TD>
					<html:text property="roomNum" maxlength="10" size="6" />
				</TD>
			</TR>
			
			<!-- 
			<TR>
				<TD nowrap>Ignore Too Far Distance:</TD>
				<TD width='100%'>
					<html:checkbox property="ignoreTooFar" />
				</TD>
			</TR>

			<TR>
				<TD nowrap>Ignore Room Checks:</TD>
				<TD>
					<html:checkbox property="ignoreRoomCheck" />
				</TD>
			</TR>
			-->

		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Special Use Room (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
