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
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="chameleon" id="form"> 
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2" valign="middle">
				<tt:section-header>
					<s:submit name='op' value='%{#msg.actionChangeUser()}'
						accesskey='%{#msg.accessChangeUser()}' title='%{#msg.titleChangeUser(#msg.accessChangeUser())}'/>
				</tt:section-header>
			</TD>
		</TR>
		<s:if test="!actionErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:actionerror/>
			</TD></TR>
		</s:if>
		<TR>
			<TD width="200"><loc:message name="propertyTimetableManager"/></TD>
			<TD>
				<s:select name="form.puid"
					list="#request.managerList" listKey="externalUniqueId" listValue="name"
					headerKey="" headerValue="%{#msg.itemSelect()}"
					/>
			</TD>
		</TR>
		<s:if test="form.canLookup == true">
		<tt:propertyEquals name="unitime.chameleon.lookup" value="true">
		<TR>
			<TD><loc:message name="propertyOtherUser"/></TD>
			<TD>
				<s:hidden name="uid" id="uid"/>
				<s:hidden name="uname" id="uname"/>
				<input type='button' value='${MSG.actionLookupUser()}' onclick="lookup();" style="btn">
			</TD>
		</TR>
		</tt:propertyEquals>
		</s:if>
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<s:submit name='op' value='%{#msg.actionChangeUser()}'
						accesskey='%{#msg.accessChangeUser()}' title='%{#msg.titleChangeUser(#msg.accessChangeUser())}'/>
			</TD>
		</TR>
	</table>
<script type="text/javascript">
	function lookup() {
		peopleLookup('', function(person) {
			if (person) {
				document.getElementById('uid').value = (person[0] == null ? '' : person[0]);
				document.getElementById('uname').value = (person[7] == null ? '' : person[7]);
				document.getElementById('form').submit();
			}
		}, "mustHaveExternalId");
	}
</script>
</s:form>
</loc:bundle>

