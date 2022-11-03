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
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteAppConfig"/></tt:confirm>
<s:form action="exactTimeEdit"> 
	<table class="unitime-MainTable unitime-Table">
		<TR>
			<TD colspan="4">
				<tt:section-header>
					<tt:section-title><loc:message name="sectExactTimeMins"/></tt:section-title>
					<s:submit name='op' value='%{#msg.actionUpdateExactTimeMins()}'/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<td align='center' class='unitime-TableHeader' nowrap="nowrap"><loc:message name="columnExactTimeMinuesRange"/></td>
			<td align='center' class='unitime-TableHeader' nowrap="nowrap"><loc:message name="columnExactTimeSlotsPerMeeting"/></td>
			<td align='center' class='unitime-TableHeader' nowrap="nowrap"><loc:message name="columnExactTimeBreakTime"/></td>
			<td style='width:100%;'></td>
		</TR>
		<s:iterator value="form.exactTimeMins" var="ex" status="stat"><s:set var="idx" value="#stat.index"/>
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
				<td align='center'>
					<s:if test="#ex.minsPerMtgMax == 0">0</s:if>
					<s:else><s:property value="#ex.minsPerMtgMin"/> .. <s:property value="#ex.minsPerMtgMax"/></s:else>
				</td>
				<td align='center'>
					<s:textfield name="form.nrTimeSlots[%{#idx}]" type="number" style="text-align: right;" min='0' max='999'/>
				</td>
				<td align='center'>
					<s:textfield name="form.breakTime[%{#idx}]" type="number" style="text-align: right;" min='0' max='999'/>
				</td>
				<td></td>
			</tr>
		</s:iterator>

		<TR>
			<TD colspan="4">
				<tt:section-header/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="4">
				<s:submit name='op' value='%{#msg.actionUpdateExactTimeMins()}'/>
			</TD>
		</TR>
	</TABLE>
</s:form>
</loc:bundle>
