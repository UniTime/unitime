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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />
<html:form action="/classOwnerChange">
	<html:hidden property="classId"/>
	<html:hidden property="className"/>
	<INPUT type="hidden" name="reloadCause" value="">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="2">
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHead">
					<bean:write name="classOwnerChangeForm" property="className" />
				</DIV>
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
			<TD>&nbsp;<BR>Owner Role:</TD>
			<TD>&nbsp;<BR>
				<html:select style="width:200;" property="ownerRole" onchange="reloadOwner('ownerRole');">					
					<html:option value="ADM">Administrator</html:option>
					<html:option value="DEP">Schedule Deputy</html:option>
					<html:option value="LLR">LLR Manager</html:option>
					<html:option value="LAB">Lab Manager</html:option>
				</html:select>
			</TD>
		</TR>

		<TR>
			<TD>Owner:</TD>
			<TD>
				<html:select style="width:300;" property="owner"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >	
					<html:options collection="ownerList" property="value" labelProperty="label" />
				</html:select>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op" accesskey="U">
					<bean:message key="button.update" />
				</html:submit> 
				
				<html:submit property="op" accesskey="E" styleId="reload">
					<bean:message key="button.reload" />
				</html:submit> 

				<html:submit property="op" accesskey="C">
					<bean:message key="button.cancel" />
				</html:submit>
			</TD>
		</TR>

	</TABLE>

</html:form>

<SCRIPT language="javascript" type="text/javascript">
<!--
	function reloadOwner(reloadCause) {
		document.classOwnerChangeForm.reloadCause.value=reloadCause; 
		document.getElementById('reload').click();
	}	
//-->
</SCRIPT>
