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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.commons.web.Web" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />
<SCRIPT language="javascript" type="text/javascript">
	<!--

		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm) {
				return true;
			} 
				
			if(confirm('This operation will delete the designator record. Continue?')) {
				return true;
			} 
			else {
				return false;
			}
		}

		
	// -->
</SCRIPT>


<html:form action="/designatorEdit">
	<html:hidden property="uniqueId"/>	
	<html:hidden property="readOnly"/>	
	<html:hidden property="instructorName"/>	
	<html:hidden property="subjectAreaAbbv"/>	
	
	<bean:define id="subjectAreaId" name="designatorEditForm" property="subjectAreaId" />
	<bean:define id="readOnly" name="designatorEditForm" property="readOnly" />
	
	<%
		boolean readOnlySubj = false;
		boolean readOnlyInstr = false;
		
		if (readOnly.toString().equals("subject") || readOnly.toString().equals("both")) {
			readOnlySubj = true;
		}
		if (readOnly.toString().equals("instructor") || readOnly.toString().equals("both")) {
			readOnlyInstr = true;
		}
		
		if (readOnlyInstr) {
	%>
	<html:hidden property="instructorId"/>	
	<%
		}
		if (readOnlySubj) {
	%>
	<html:hidden property="subjectAreaId"/>	
	<%
		}
	%>
	
	<TABLE width="93%" border="0" cellspacing="1" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
					
					</tt:section-title>				
					
					<logic:equal name="designatorEditForm" property="uniqueId" value="0">		
						<html:submit property="op" 
							styleClass="btn" accesskey="S" titleKey="title.saveDesignator"> 
							<bean:message key="button.saveDesignator" />
						</html:submit>
					</logic:equal>
	
					<logic:notEqual name="designatorEditForm" property="uniqueId" value="0">		
						<html:submit property="op" 
							styleClass="btn" accesskey="U" titleKey="title.updateDesignator"> 
							<bean:message key="button.updateDesignator" />
						</html:submit>
						<html:submit property="op" 
							styleClass="btn" accesskey="D" titleKey="title.deleteDesignator"
							onclick="return (confirmDelete());"> 
							<bean:message key="button.deleteDesignator" />
						</html:submit>
					</logic:notEqual>
					 
					<html:submit property="op" 
						styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
						<bean:message key="button.backToPrevious" />
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
			<TD width="150">Subject: <font class="reqField">*</font></TD>
			<TD>			
				<% if (!readOnlySubj) { %>
				<html:select name="designatorEditForm" property="subjectAreaId"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
				<% } else { %>
				<bean:write name="designatorEditForm" property="subjectAreaAbbv" />
				<% } %>
			</TD>
		</TR>
		<TR>
			<TD>Instructor: <font class="reqField">*</font></TD>
			<TD>
				<% if (!readOnlyInstr) { %>
				<html:select name="designatorEditForm" property="instructorId"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" property="value" labelProperty="label" />
				</html:select>
				<% } else { %>
				<bean:write name="designatorEditForm" property="instructorName" />
				<% } %>
			</TD>
		</TR>
		<TR>
			<TD>Code: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="code" size="5" maxlength="3" />
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<logic:equal name="designatorEditForm" property="uniqueId" value="0">		
					<html:submit property="op" 
						styleClass="btn" accesskey="S" titleKey="title.saveDesignator"> 
						<bean:message key="button.saveDesignator" />
					</html:submit>
				</logic:equal>

				<logic:notEqual name="designatorEditForm" property="uniqueId" value="0">		
					<html:submit property="op" 
						styleClass="btn" accesskey="U" titleKey="title.updateDesignator"> 
						<bean:message key="button.updateDesignator" />
					</html:submit>
					<html:submit property="op" 
						styleClass="btn" accesskey="D" titleKey="title.deleteDesignator"
						onclick="return (confirmDelete());"> 
						<bean:message key="button.deleteDesignator" />
					</html:submit>
				</logic:notEqual>
				 
				<html:submit property="op" 
					styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
					<bean:message key="button.backToPrevious" />
				</html:submit>
			</TD>
		</TR>

	</TABLE>	
</html:form>
	
