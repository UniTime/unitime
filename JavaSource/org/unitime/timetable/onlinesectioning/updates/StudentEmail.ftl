<!DOCTYPE html>
<!-- 
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
 -->
 <html>
 	<head>
 		<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>
 		<title><#if subject??>${subject}<#else>${msg.emailDeafultTitle()}</#if></title>
 	</head>
 	<body style="font-family: sans-serif, verdana, arial;">
 		<table style="border: 1px solid #9CB0CE; padding: 5px; margin-top: 10px; min-width: 800px;" align="center">
 			<tr><td><table width="100%">
 				<tr>
 					<td rowspan="2"><img src="http://www.unitime.org/include/unitime.png" border="0" height="80px"/></td>
 					<td colspan="2" style="font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;"><#if subject??>${subject}<#else>${msg.emailDeafultTitle()}</#if></td>
 				</tr>
 				<tr>
 					<td style="color: #333333; text-align: right; vertical-align: top; padding: 10px 5px 5px 5px;"><#if name??>${name}<#else>${student.name}</#if></td>
 					<td style="color: #333333; text-align: right; vertical-align: top; padding: 10px 5px 5px 5px;">${server.academicSession}</td>
 				</tr>
 			</table></td></tr>
 			
 			<#if message?? && message?has_content>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailMessage()}</td></tr>
 				<tr><td style="white-space: pre-wrap;">${message?replace("\n","<br>")}</td></tr>
 			</#if>
 			
 			<#if reason?? && reason?has_content>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailReschedulingReason()}</td></tr>
 				<tr><td style="white-space: pre-wrap;">${reason?replace("\n","<br>")}</td></tr>
 			</#if>

 			<#if changedCourse??>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailCourseEnrollment(changedCourse.subjectArea,changedCourse.courseNumber)}</td></tr>
 				<#if changes??>
 					<tr><td><table width="100%">
 						<@classTableHeader/>
 						<#list changes as line>
 							<@classTableLine line/>
 						</#list>
 					</table></tr></tr>
 				</#if>
 				<#if changeMessage??>
 					<tr><td style="color: red; text-align: center; font-style: italic; font-weight: normal; white-space: pre-wrap;">${changeMessage?replace("\n","<br>")}</td></tr>
 				</#if>
 			<#elseif changes??>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailEnrollmentChanges()}</td></tr>
	 			<tr><td><table width="100%">
 					<@classTableHeader/>
 					<#list changes as line>
 						<@classTableLine line/>
 					</#list>
	 				<#if changes?size == 0>
 						<tr><td colspan="13"><i>${msg.emailNoChange()}</i></td></tr>
 					</#if>
 				</table></tr></tr>
 			</#if>
 			
 			<#if advisor?? && advisor.lines??>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailAdvisorRequests()}</td></tr>
 				<tr><td><table width="100%" cellspacing="0" cellpadding="3">
 					<@advisorRequestsHeader/>
 					<#list advisor.lines as line>
 						<@advisorRequestsLine line/>
 					</#list>
 				</table></td></tr>
 				<#if disclaimer??><tr><td>${disclaimer}</td></tr></#if>
 			</#if>
 			
 			<#if requests??>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailCourseRequests()}</td></tr>
 				<#if requests.lines??>
 					<tr><td><table width="100%" cellspacing="0" cellpadding="3">
 						<@courseRequestsHeader/>
 						<#list requests.lines as line>
 							<@courseRequestsLine line/>
 						</#list>
 					</table></td></tr>
 				<#else>
 					<tr><td style="color: red; text-align: center; font-style: italic; font-weight: normal;">${msg.emptyRequests()}</td></tr>
 				</#if>
 			</#if>
 			
 			<#if classes??>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailClassList()}</td></tr>
 				<#if classes?size == 0>
 					<tr><td style="color: red; text-align: center; font-style: italic; font-weight: normal;">${msg.emailNoSchedule()}</td></tr>
 				<#else>
 					<tr><td><table width="100%">
 						<@classTableHeader/>
 						<#list classes as line>
 							<@classTableLine line/>
 						</#list>
		 				<#if credit??>
 							<tr><td colspan="13" style="text-align: center;">${msg.totalCredit(credit)}</td></tr>
 						</#if>
 					</table></td></tr>
 				</#if>
 			</#if>
 			
 			<#if timetable??>
 				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailTimetable()}</td></tr>
 				<tr><td>${timetable}</tr></tr>
 			</#if>
 			
 			<#if manager>
 				<#if changed>
 					<tr><td>${msg.emailChangesMadeBy(helper.user.name)}</td></tr>
 				<#else>
 					<tr><td>${msg.emailSentBy(helper.user.name)}</td></tr>
 				</#if>
 			</#if>
 			
			<#if link??>
				<tr><td style="font-style: italic; color: #9CB0CE; text-align: right; padding-top: 5px;">${msg.emailLinkToUniTime(link)}</td></tr>
 			</#if>
		</table>
		<table style="width: 800px; margin-top: -3px;" align="center">
			<tr>
				<td width="33%" align="left" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${version}</td>
				<td width="34%" align="center" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${copyright}</td>
				<td width="33%" align="right" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${ts}</td>
			</tr>
		</table>
	</body>
</html>

<#macro classTableHeader>
 	<tr>
 		<#assign style="white-space: nowrap; font-weight: bold; padding-top: 5px;">
 		<td style="${style}">${msg.colSubject()}</td>
 		<td style="${style}">${msg.colCourse()}</td>
 		<td style="${style}">${msg.colSubpart()}</td>
 		<td style="${style}">${msg.colClass()}</td>
 		<td style="${style}">${msg.colDays()}</td>
 		<td style="${style}">${msg.colStart()}</td>
 		<td style="${style}">${msg.colEnd()}</td>
 		<td style="${style}">${msg.colDate()}</td>
 		<td style="${style}">${msg.colRoom()}</td>
 		<td style="${style}">${msg.colInstructor()}</td>
 		<td style="${style}">${msg.colParent()}</td>
 		<td style="${style}">${msg.colNote()}</td>
 		<td style="${style}">${msg.colCredit()}</td>
 	</tr>
</#macro>

<#macro classTableLine line>
	<#assign style="white-space: nowrap;">
	<#assign stylebr="">
	<#assign stylelink="color: inherit; text-decoration: none;">
	<#if line.first>
		<#assign style="white-space: nowrap; border-top: 1px dashed #9CB0CE;">
		<#assign stylebr="border-top: 1px dashed #9CB0CE;">
	</#if>
	<#if line.cancelled>
		<#assign style = style + "font-style: italic; color: gray;">
		<#assign stylebr = stylebr + " font-style: italic; color: gray;">
	</#if>
	<#if line.class.simpleName == "TableSectionDeletedLine">
		<#assign style = style + " text-decoration: line-through; font-style: italic; color: gray;">
		<#assign stylebr = stylebr + " text-decoration: line-through; font-style: italic; color: gray;">
	</#if>
	<#if line.freeTime>
		<tr style='vertical-align: top'>
			<td style="${style}">${msg.freeTimeSubject()}</td>
			<td style="${style}">${msg.freeTimeCourse()}</td>
			<td style="${style}"></td>
			<td style="${style}"></td>
	 		<td style="${style}">${line.days}</td>
			<td style="${style}">${line.start}</td>
			<td style="${style}">${line.end}</td>
			<td style="${stylebr}" colspan="6"></td>
		</tr>
	<#elseif line.assigned>
		<tr style='vertical-align: top'>
			<#if line.first>
				<#if line.url??>
					<td style="${style}"><a href="${line.url}" style="${stylelink}">${line.subject}</a></td>
		 			<td style="${style}"><a href="${line.url}" style="${stylelink}">${line.courseNumber}</a></td>
				<#else>
					<td style="${style}">${line.subject}</td>
		 			<td style="${style}">${line.courseNumber}</td>
				</#if>
	 		<#else>
	 			<td style="${style}" colspan='2'></td>
	 		</#if>
	 		<td style="${style}">${line.type}</td>
	 		<td style="${style}">${line.name}</td>
	 		<#if line.time??>
	 			<#if line.time.days gt 0>
	 				<td style="${style}">${line.days}</td>
					<td style="${style}">${line.start}</td>
					<td style="${style}">${line.end}</td>
				<#else>
					<td style="${style}" colspan="3">${line.arrangeHours}</td>
				</#if>
				<td style="${style}">${line.date}</td>
		 	<#else>
		 		<td style="${style}" colspan="4">${line.arrangeHours}</td>
	 		</#if>
	 		<td style="${style}">${line.rooms}</td>
	 		<td style="${stylebr}">${line.instructors}</td>
	 		<#if line.requires??><td style="${stylebr}">${line.requires}</td><#else><td style="${style}"></td></#if>
	 		<#if line.note??><td style="${stylebr} white-space: pre-wrap;">${line.note?replace("\n","<br>")}</td><#else><td style="${style}"></td></#if>
	 		<#if line.credit??><td style="${style}">${line.credit}</td><#else><td style="${style}"></td></#if>
		</tr>
		<#if line.last && line.courseNote??>
			<tr style='vertical-align: top'>
				<td colspan='2'></td>
				<td colspan='11'>${line.courseNote}</td>
			</tr>
		</#if>
	<#else>
		<#if !line.waitList>
			<#assign style="white-space: nowrap; color: red; border-top: 1px dashed #9CB0CE;">
			<#assign stylebr="color: red; border-top: 1px dashed #9CB0CE;">
		</#if>
	 	<tr style='vertical-align: top'>
	 		<#if line.url??>
				<td style="${style}"><a href="${line.url}" style="${stylelink}">${line.subject}</a></td>
		 		<td style="${style}"><a href="${line.url}" style="${stylelink}">${line.courseNumber}</a></td>
			<#else>
				<td style="${style}">${line.subject}</td>
	 			<td style="${style}">${line.courseNumber}</td>
			</#if>
	 		<td style="${style}"></td>
	 		<td style="${style}"></td>
	 		<td style="${stylebr}" colspan="9" align="center">${line.note}</td>
	 	</tr>	
	</#if>
</#macro>

<#macro classTableNote note>
	<tr style='vertical-align: top'>
		<td colspan='2'></td>
		<td colspan='11'>${note}</td>
	</tr>
</#macro>

<#macro courseRequestsHeader>
 	<tr>
 		<#assign style="white-space: nowrap; font-weight: bold; padding-top: 5px; border-bottom: 1px dashed #9CB0CE;">
 		<td style="${style}">${msg.colPriority()}</td>
 		<td style="${style}">${msg.colCourse()}</td>
 		<td style="${style}">${msg.colTitle()}</td>
 		<td style="${style}" align="right">${msg.colCredit()}</td>
 		<#if requests.hasPref><td style="${style}">${msg.colPreferences()}</td></#if>
 		<#if requests.hasWarn><td style="${style}">${msg.colWarnings()}</td></#if>
 		<td style="${style}">${msg.colStatus()}</td>
 		<#if requests.hasWait && wlMode == 'WaitList'><td style="${style}">${msg.colWaitList()}</td></#if>
 		<#if requests.hasWait && wlMode == 'NoSubs'><td style="${style}">${msg.colNoSubs()}</td></#if>
 	</tr>
</#macro>

<#macro courseRequestsLine line>
	<#assign style="white-space: nowrap; vertical-align: top;">
	<#assign stylelink="color: inherit; text-decoration: none;">
	<#if line.first>
		<#assign style="border-top: 1px dashed #9CB0CE; white-space: nowrap; vertical-align: top;">
	</#if>
	<#if line.firstalt>
		<#assign style="border-top: 1px solid #9CB0CE; white-space: nowrap; vertical-align: top;">
	</#if>
	<#if line.last>
		<#assign style="border-top: 1px solid #9CB0CE; white-space: nowrap; vertical-align: top;">
	</#if>
 	<tr>
 		<#if line.last>
 			<#if line.url??>
 				<td style="${style} font-weight: bold;" colspan='2'><a href="${line.url}" style="${stylelink}">${line.courseName}</a></td>
 			<#else>
				<td style="${style} font-weight: bold;" colspan='2'>${line.courseName}</td>
			</#if>
 		</#if>
 		<#if !line.last>
 			<td style="${style}">${line.priority}</td>
 			<#if line.url??>
		 		<td style="${style}"><a href="${line.url}" style="${stylelink}">${line.courseName}</a></td>
			<#else>
				<td style="${style}">${line.courseName}</td>
			</#if>
 		</#if>
 		<td style="${style}">${line.courseTitle}</td>
 		<td style="${style} padding-right: 5px;" align="right">${line.credit}</td>
 		<#if requests.hasPref><td style="${style}">${line.prefs}</td></#if>
 		<#if requests.hasWarn>
 			<#if line.note?? && line.note?has_content>
 				<td style="${style} white-space: pre-wrap;">${line.note?replace("\n","<br>")}</td>
 			<#else>
 				<td style="${style}"></td>
 			</#if>
 		</#if>
 		<#if line.icon?? && line.icon?has_content>
 			<#if line.iconText??>
 				<td style="${style}"><img src='http://www.unitime.org/icons/${line.icon}' width='16' height='16' alt='' title='${line.iconText}' style='padding-right:3px;'> ${line.status}</td>
 			<#else>
 				<td style="${style}"><img src='http://www.unitime.org/icons/${line.icon}' width='16' height='16' alt='' style='padding-right:3px;'> ${line.status}</td>
 			</#if>
 		<#else>
 			<td style="${style}">${line.status}</td>
 		</#if>
 		<#if requests.hasWait && wlMode == 'WaitList'><td style="${style}">
 			<#if line.waitlist>
 				<#if line.waitListDate??>${line.waitListDate}
 				<#else><img src='http://www.unitime.org/icons/action_check.png' width='16' height='16' title='${msg.descriptionRequestWaitListed()}' alt='${msg.courseWaitListed()}'></#if>
 			</#if>
 		</td></#if>
 		<#if requests.hasWait && wlMode == 'NoSubs'><td style="${style}">
 			<#if line.waitlist>
 				<img src='http://www.unitime.org/icons/action_check.png' width='16' height='16' title='${msg.descriptionRequestNoSubs()}' alt='${msg.courseNoSubs()}'>
 			</#if>
 		</td></#if>
 	</tr>
</#macro>

<#macro advisorRequestsHeader>
 	<tr>
 		<#assign style="white-space: nowrap; font-weight: bold; padding-top: 5px; border-bottom: 1px dashed #9CB0CE;">
 		<td style="${style}">${msg.colPriority()}</td>
 		<td style="${style}">${msg.colCourse()}</td>
 		<td style="${style}">${msg.colTitle()}</td>
 		<td style="${style}" align="right">${msg.colCredit()}</td>
 		<#if advisor.hasPref><td style="${style}">${msg.colPreferences()}</td></#if>
 		<td style="${style}">${msg.colNotes()}</td>
 		<#if advisor.hasCritical><td style="${style}">${advisor.criticalColumn}</td></#if>
 		<#if advisor.hasWait && awlMode == 'WaitList'><td style="${style}">${msg.colWaitList()}</td></#if>
 		<#if advisor.hasWait && awlMode == 'NoSubs'><td style="${style}">${msg.colNoSubs()}</td></#if>
 	</tr>
</#macro>

<#macro advisorRequestsLine line>
	<#assign style="white-space: nowrap; vertical-align: top;">
	<#assign stylelink="color: inherit; text-decoration: none;">
	<#if line.first>
		<#assign style="border-top: 1px dashed #9CB0CE; white-space: nowrap; vertical-align: top;">
	</#if>
	<#if line.firstalt>
		<#assign style="border-top: 1px solid #9CB0CE; white-space: nowrap; vertical-align: top;">
	</#if>
	<#if line.last>
		<#assign style="border-top: 1px solid #9CB0CE; white-space: nowrap; vertical-align: top;">
	</#if>
 	<tr>
 		<#if line.last>
 			<#if line.url??>
 				<td style="${style} font-weight: bold;" colspan='2'><a href="${line.url}" style="${stylelink}">${line.courseName}</a></td>
 			<#else>
				<td style="${style} font-weight: bold;" colspan='2'>${line.courseName}</td>
			</#if>
 		</#if>
 		<#if !line.last>
 			<td style="${style}">${line.priority}</td>
 			<#if line.url??>
		 		<td style="${style}"><a href="${line.url}" style="${stylelink}">${line.courseName}</a></td>
			<#else>
				<td style="${style}">${line.courseName}</td>
			</#if>
 		</#if>
 		<td style="${style}">${line.courseTitle}</td>
 		<td style="${style} padding-right: 5px;" align="right">${line.credit}</td>
 		<#if advisor.hasPref && !line.last><td style="${style} white-space:pre-wrap;">${line.prefs?replace("\n","<br>")}</td></#if>
 		<#if line.rows gt 0>
 			<#assign noteColSpan="1">
 			<#if line.last>
 				<#if advisor.hasCritical>
 					<#if (advisor.hasWait && awlMode == 'WaitList') || (advisor.hasWait && awlMode == 'NoSubs')>
 						<#assign noteColSpan="4">
 					<#else>
 						<#assign noteColSpan="3">
	 				</#if>
 				<#else>
 					<#if (advisor.hasWait && awlMode == 'WaitList') || (advisor.hasWait && awlMode == 'NoSubs')>
 						<#assign noteColSpan="3">
 					<#else>
 						<#assign noteColSpan="2">
	 				</#if>
 				</#if>
 			</#if>
 			<#if line.note?? && line.note?has_content>
 				<td style="${style} white-space: pre-wrap;" rowSpan="${line.rows}" colSpan="${noteColSpan}">${line.note?replace("\n","<br>")}</td>
 			<#else>
 				<td style="${style}" rowSpan="${line.rows}" colSpan="${noteColSpan}"></td>
 			</#if>
 		</#if>
 		<#if advisor.hasCritical><td style="${style}">
 			<#if line.critical>
 				<img src='http://www.unitime.org/icons/action_check.png' width='16' height='16' title='${advisor.criticalColumnDescription}' alt='${advisor.criticalColumnDescription}'>
 			</#if>
 		</td></#if>
 		<#if advisor.hasWait && awlMode == 'WaitList' && !line.last><td style="${style}">
 			<#if line.waitlist>
 				<img src='http://www.unitime.org/icons/action_check.png' width='16' height='16' title='${msg.descriptionRequestWaitListed()}' alt='${msg.courseWaitListed()}'>
 			</#if>
 		</td></#if>
 		<#if advisor.hasWait && awlMode == 'NoSubs' && !line.last><td style="${style}">
 			<#if line.waitlist>
 				<img src='http://www.unitime.org/icons/action_check.png' width='16' height='16' title='${msg.descriptionRequestNoSubs()}' alt='${msg.courseNoSubs()}'>
 			</#if>
 		</td></#if>
 	</tr>
</#macro>