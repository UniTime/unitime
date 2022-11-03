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
<s:form action="hibernateStats">
	<s:hidden name="details"/>
<table class="unitime-MainTable">
	<TR>
		<TD>
			<tt:section-header>
				<tt:section-title>
					<s:if test="details == true">
						<loc:message name="sectDetailedStatistics"/>
					</s:if><s:else>
						<loc:message name="sectSummaryStatistics"/>
					</s:else>
					</tt:section-title>
					<s:if test="enabled == true">
						<s:submit name='op' value='%{#msg.actionDisableStatistics()}'/>
						<s:if test="details == true">
							<s:submit name='op' value='%{#msg.actionHideDetails()}'/>
						</s:if><s:else>
							<s:submit name='op' value='%{#msg.actionShowDetails()}'/>
						</s:else>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionEnableStatistics()}'/>
					</s:else>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD>
				<div style="position: relative; overflow-x: scroll; max-width: 99vw;">
					<s:property value="stats" escapeHtml="false"/>
				</div>
			</TD>
		</TR>
		<TR>
			<TD>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align='right'>
				<s:if test="enabled == true">
					<s:submit name='op' value='%{#msg.actionDisableStatistics()}'/>
					<s:if test="details == true">
						<s:submit name='op' value='%{#msg.actionHideDetails()}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionShowDetails()}'/>
					</s:else>
				</s:if><s:else>
					<s:submit name='op' value='%{#msg.actionEnableStatistics()}'/>
				</s:else>
			</TD>
		</TR>
</table>
</s:form>
</loc:bundle>