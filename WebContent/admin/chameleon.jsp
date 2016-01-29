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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.TimetableManager" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />
<html:form action="/chameleon" styleId="form">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2" valign="middle">
				<tt:section-header>
					<html:submit property="op" 
						styleClass="btn" accesskey="S" titleKey="title.changeUser">			
						<bean:message key="button.changeUser" />
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
			<TD width="200">Timetable Manager:</TD>
			<TD>
				<html:select name="chameleonForm" property="puid">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%= TimetableManager.MGR_LIST_ATTR_NAME %>" 
						property="externalUniqueId" labelProperty="name"/>
					</html:select>
				
			</TD>
		</TR>
		<logic:equal value="true" name="chameleonForm" property="canLookup">
		<tt:propertyEquals name="unitime.chameleon.lookup" value="true">
		<TR>
			<TD>Other:</TD>
			<TD>
				<input type='hidden' name='uid' id='uid'>
				<input type='hidden' name='uname' id='uname'>
				<input type='button' value='Lookup' onclick="lookup();" style="btn">
			</TD>
		</TR>
		</tt:propertyEquals>
		</logic:equal>
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op" onclick="displayLoading();"
					styleClass="btn" accesskey="S" titleKey="title.changeUser">			
					<bean:message key="button.changeUser" />
				</html:submit>						
			</TD>
		</TR>
	</TABLE>
<script language="javascript">
	function lookup() {
		peopleLookup('', function(person) {
			if (person) {
				document.getElementById('uid').value = (person[0] == null ? '' : person[0]);
				document.getElementById('uname').value = (person[7] == null ? '' : person[7]);
				document.getElementById('form').submit();
			}
		}, "mustHaveExternalId");
	}
</script></html:form>

