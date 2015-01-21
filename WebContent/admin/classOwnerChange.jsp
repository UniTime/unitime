<%-- 
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 --%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

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
				<html:select style="width:300;" property="owner">	
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
