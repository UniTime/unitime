<#-- 
  Licensed to The Apereo Foundation under one or more contributor license
  agreements. See the NOTICE file distributed with this work for
  additional information regarding copyright ownership.

  The Apereo Foundation licenses this file to you under the Apache License,
  Version 2.0 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at:

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

  See the License for the specific language governing permissions and
  limitations under the License.
 -->
 <table class='unitime-MainTable'>
	<tr>
		<td class='unitime-MainTableHeader' colspan='2'>
			${course.courseNameWithTitle}
		</td>
	</tr>
	<#if course.scheduleBookNote?has_content>
		<tr>
			<td>${msg.colNote()}:</td><td>${course.scheduleBookNote}</td>
		</tr>
	</#if>
	<#if course.consentType??>
		<tr>
			<td>${msg.colConsent()}:</td><td>${course.consentType.label}</td>
		</tr>
	</#if>
	<#if course.instructionalOffering.credit??>
		<tr>
			<td>${msg.colCredit()}:</td><td>${course.instructionalOffering.credit.creditText()}</td>
		</tr>
	</#if>
	<#if course.instructionalOffering.coordinators?size != 0>
		<tr>
			<td>${msg.colCoordinator()}:</td><td>
				<#list course.instructionalOffering.coordinators as coordinator>
					<#if coordinator.email??>
						<a href='mailto:${coordinator.email}' class='unitime-NoFancyLink'><div>${coordinator.getName('last-first-middle')}</div></a>
					<#else>
						<div>${coordinator.getName('last-first-middle')}</div>
					</#if>
				</#list>
			</td>
		</tr>
	</#if>
	<#if url??>
		<tr>
			<td>${cmsg.propertyCourseCatalog()}</td><td><a href='${url}' target='_blank'>${gmsg.courseCatalogLink()}</a></td>
		</tr>
	</#if>
</table>