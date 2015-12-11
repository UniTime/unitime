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
		<title>${subject}</title>
	</head>
	<body style="font-family: sans-serif, verdana, arial;">
		<table style="border: 1px solid #9CB0CE; padding: 5px; margin-top: 10px; min-width: 800px;" align="center">
			<!-- header table -->
			<tr><td><table width="100%">
				<tr>
					<td rowspan="2"><img src="http://www.unitime.org/include/unitime.png" border="0" height="80px"/></td>
					<td colspan="2" style="font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;">${subject}</td>
				</tr>
				</table></td></tr>
			<!-- event name -->
			<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${event.name}</td></tr>
			<!-- event details -->
			<tr><td><table>
				<tr><td>${msg.propEventType()}</td><td>${event.getType().getName(const)}</td></tr>
				<tr><td>${msg.propContacts()}</td><td>
				<table width="100%"><tr>
					<td style="white-space: nowrap; font-weight: bold;">${msg.colName()}</td>
					<td style="white-space: nowrap; font-weight: bold;">${msg.colEmail()}</td>
					<td style="white-space: nowrap; font-weight: bold;">${msg.colPhone()}</td>
				</tr>
				<#if event.hasContact()>
					<tr>
						<td>${event.getContact().getName(msg)}</td>
						<td><#if event.getContact().hasEmail()>${event.getContact().getEmail()}</#if></td>
						<td><#if event.getContact().hasPhone()>${event.getContact().getPhone()}</#if></td>
					</tr>
				</#if>
				<#if event.hasAdditionalContacts()>
					<#list event.getAdditionalContacts() as contact>
						<tr>
						<td>${contact.getName(msg)}</td>
						<td><#if contact.hasEmail()>${contact.getEmail()}</#if></td>
						<td><#if contact.hasPhone()>${contact.getPhone()}</#if></td>
						</tr>
					</#list>
				</#if>
				<#if event.hasInstructors()>
					<#list event.getInstructors() as contact>
						<tr>
						<td style='font-style: italic;'>${contact.getName(msg)}</td>
						<td style='font-style: italic;'><#if contact.hasEmail()>${contact.getEmail()}</#if></td>
						<td style='font-style: italic;'><#if contact.hasPhone()>${contact.getPhone()}</#if><#if !contact.hasPhone()>${msg.eventContactInstructorPhone()}</#if></td>
						</tr>
					</#list>
				</#if>
				<#if event.hasCoordinators()>
					<#list event.getCoordinators() as contact>
						<tr>
						<td style='font-style: italic;'>${contact.getName(msg)}</td>
						<td style='font-style: italic;'><#if contact.hasEmail()>${contact.getEmail()}</#if></td>
						<td style='font-style: italic;'><#if contact.hasPhone()>${contact.getPhone()}</#if><#if !contact.hasPhone()>${msg.eventContactCoordinatorPhone()}</#if></td>
						</tr>
					</#list>
				</#if>
				</table>
				</td></tr>
				<#if event.hasEmail()>
					<tr><td>${msg.propAdditionalEmails()}</td><td>${event.getEmail()?replace("\n", "<br>")}</td></tr>
				</#if>
				<#if event.hasSponsor()>
					<tr><td>${msg.propSponsor()}</td><td>${event.sponsor.name}</td></tr>
				</#if>
				<#if event.hasEnrollment()>
					<tr><td>${msg.propEnrollment()}</td><td>${event.enrollment}</td></tr>
				</#if>
				<#if event.hasMaxCapacity()>
					<tr><td>${msg.propAttendance()}</td><td>${event.maxCapacity}</td></tr>
				</#if>
				<#if event.hasExpirationDate()>
					<tr><td>${msg.propExpirationDate()}</td><td>${event.expirationDate?string(const.eventDateFormat())}</td></tr>
				</#if>
			</table></td></tr>
			<#if created??>
				<!-- created meetings -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailCreatedMeetings()}</td></tr>
				<tr><td>
					<table width="100%"><tr>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colDate()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colPublishedTime()}</td>
						<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colAllocatedTime()}</td>
						</#if>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colLocation()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colStatus()}</td>
					</tr>
					<#list created as meeting>
						<tr>
							<td>${meeting.getDays(const)} <#if meeting.getNrMeetings() <= 1>${meeting.getFirstMeetingDate()?string(const.eventDateFormatLong())}<#else>${meeting.getFirstMeetingDate()?string(const.eventDateFormatShort())} - ${meeting.getLastMeetingDate()?string(const.eventDateFormatLong())}</#if></td>
							<td>${meeting.getMeetingTime(const)}</td>
							<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
							<td>${meeting.getAllocatedTime(const)}</td>
							</#if>
							<td>${meeting.getLocationName()}</td>
							<#switch meeting.getApprovalStatus().name()>
								<#case "Pending">
									<#if meeting.isPast()>
										<td style='color: orange; font-style: italic;'>${msg.approvalNotApprovedPast()}</td>
									<#elseif event.hasExpirationDate()>
										<td style='color: red; font-style: italic;'>${msg.approvalExpire(event.getExpirationDate()?string(const.eventDateFormat()))}</td>
									<#else>
										<td style='color: red; font-style: italic;'>${msg.approvalNotApproved()}</td>
									</#if>									
									<#break>
								<#case "Approved">
									<#if meeting.isPast()>
										<td style='color: gray; font-style: italic;'>${msg.approvalApproved()}</td>
									<#else>
										<td style='color: gray;'>${msg.approvalApproved()}</td>
									</#if>
									<#break>
								<#case "Rejected">
									<td style='color: gray; font-style: italic;'>${msg.approvalRejected()}</td>
									<#break>
								<#case "Cancelled">
									<td style='color: gray; font-style: italic;'>${msg.approvalCancelled()}</td>
									<#break>
								<#case "Deleted">
									<td style='color: gray; font-style: italic;'>${msg.approvalDeleted()}</td>
									<#break>
							</#switch>		
						<tr>
					</#list>
					</table>
				</td></tr>
			</#if>
			<#if deleted??>
				<!-- deleted meetings -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailDeletedMeetings()}</td></tr>
				<tr><td>
					<table width="100%"><tr>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colDate()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colPublishedTime()}</td>
						<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colAllocatedTime()}</td>
						</#if>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colLocation()}</td>
					</tr>
					<#list deleted as meeting>
						<tr>
							<td>${meeting.getDays(const)} <#if meeting.getNrMeetings() <= 1>${meeting.getFirstMeetingDate()?string(const.eventDateFormatLong())}<#else>${meeting.getFirstMeetingDate()?string(const.eventDateFormatShort())} - ${meeting.getLastMeetingDate()?string(const.eventDateFormatLong())}</#if></td>
							<td>${meeting.getMeetingTime(const)}</td>
							<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
							<td>${meeting.getAllocatedTime(const)}</td>
							</#if>
							<td>${meeting.getLocationName()}</td>
						<tr>
					</#list>
					</table>
				</td></tr>
			</#if>
			<#if cancelled??>
				<!-- cancelled meetings -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailCancelledMeetingsInEdit()}</td></tr>
				<tr><td>
					<table width="100%"><tr>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colDate()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colPublishedTime()}</td>
						<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colAllocatedTime()}</td>
						</#if>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colLocation()}</td>
					</tr>
					<#list cancelled as meeting>
						<tr>
							<td>${meeting.getDays(const)} <#if meeting.getNrMeetings() <= 1>${meeting.getFirstMeetingDate()?string(const.eventDateFormatLong())}<#else>${meeting.getFirstMeetingDate()?string(const.eventDateFormatShort())} - ${meeting.getLastMeetingDate()?string(const.eventDateFormatLong())}</#if></td>
							<td>${meeting.getMeetingTime(const)}</td>
							<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
							<td>${meeting.getAllocatedTime(const)}</td>
							</#if>
							<td>${meeting.getLocationName()}</td>
						<tr>
					</#list>
					</table>
				</td></tr>
			</#if>			
			<#if updated??>
				<!-- updated meetings -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">
					<#switch operation>
					<#case "APPROVE">
						${msg.emailApprovedMeetings()}
						<#break>
					<#case "REJECT">
						${msg.emailRejectedMeetings()}
						<#break>
					<#case "INQUIRE">
						${msg.emailInquiredMeetings()}
						<#break>
					<#case "CANCEL">
						${msg.emailCancelledMeetings()}
						<#break>
					<#default>
						${msg.emailUpdatedMeetings()}
					</#switch>
				</td></tr>
				<tr><td>
					<table width="100%"><tr>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colDate()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colPublishedTime()}</td>
						<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colAllocatedTime()}</td>
						</#if>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colLocation()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colStatus()}</td>
					</tr>
					<#list updated as meeting>
						<#switch meeting.getApprovalStatus().name()>
						<#case "Rejected">
						<#case "Cancelled">
						<#case "Deleted">
							<tr style='color:gray; font-style: italic;'>
							<#break>
						<#default>
							<tr>
						</#switch>
							<td>${meeting.getDays(const)} <#if meeting.getNrMeetings() <= 1>${meeting.getFirstMeetingDate()?string(const.eventDateFormatLong())}<#else>${meeting.getFirstMeetingDate()?string(const.eventDateFormatShort())} - ${meeting.getLastMeetingDate()?string(const.eventDateFormatLong())}</#if></td>
							<td>${meeting.getMeetingTime(const)}</td>
							<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
							<td>${meeting.getAllocatedTime(const)}</td>
							</#if>
							<td>${meeting.getLocationName()}</td>
							<#switch meeting.getApprovalStatus().name()>
								<#case "Pending">
									<#if meeting.isPast()>
										<td style='color: orange; font-style: italic;'>${msg.approvalNotApprovedPast()}</td>
									<#elseif event.hasExpirationDate()>
										<td style='color: red; font-style: italic;'>${msg.approvalExpire(event.getExpirationDate()?string(const.eventDateFormat()))}</td>
									<#else>
										<td style='color: red; font-style: italic;'>${msg.approvalNotApproved()}</td>
									</#if>									
									<#break>
								<#case "Approved">
									<#if meeting.isPast()>
										<td style='color: gray; font-style: italic;'>${msg.approvalApproved()}</td>
									<#else>
										<td style='color: gray;'>${msg.approvalApproved()}</td>
									</#if>
									<#break>
								<#case "Rejected">
									<td style='color: gray; font-style: italic;'>${msg.approvalRejected()}</td>
									<#break>
								<#case "Cancelled">
									<td style='color: gray; font-style: italic;'>${msg.approvalCancelled()}</td>
									<#break>
								<#case "Deleted">
									<td style='color: gray; font-style: italic;'>${msg.approvalDeleted()}</td>
									<#break>
							</#switch>		
						<tr>
					</#list>
					</table>
				</td></tr>
			</#if>
			<#if message??>
				<!-- message -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">
				<#switch operation>
				<#case "APPROVE">${msg.emailMessageApproval()}<#break>
				<#case "REJECT">${msg.emailMessageReject()}<#break>
				<#case "INQUIRE">${msg.emailMessageInquiry()}<#break>
				<#case "CREATE">${msg.emailMessageCreate()}<#break>
				<#case "UPDATE">${msg.emailMessageUpdate()}<#break>
				<#case "DELETE">${msg.emailMessageDelete()}<#break>
				<#case "CANCEL">${msg.emailMessageCancel()}<#break>
				<#default>${msg.emailMessageUpdate()}
				</#switch>
				</td></tr>
				<tr><td>${message?replace("\n", "<br>")}</td></tr>
			</#if>
			<#if meetings??>
				<!-- meetings -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailAllMeetings(event.name)}</td></tr>
				<tr><td>
				<#if meetings?size == 0>
					${msg.emailEventDeleted(event.name)}
				<#else>
					<table width="100%"><tr>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colDate()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colPublishedTime()}</td>
						<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colAllocatedTime()}</td>
						</#if>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colLocation()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colApproval()}</td>
					</tr>
					<#assign empty = true>
					<#list meetings as meeting>
						<#switch meeting.getApprovalStatus().name()>
						<#case "Rejected">
						<#case "Cancelled">
						<#case "Deleted">
							<#break>
						<#default>
							<#assign empty = false>
							<tr>
								<td>${meeting.getDays(const)} <#if meeting.getNrMeetings() <= 1>${meeting.getFirstMeetingDate()?string(const.eventDateFormatLong())}<#else>${meeting.getFirstMeetingDate()?string(const.eventDateFormatShort())} - ${meeting.getLastMeetingDate()?string(const.eventDateFormatLong())}</#if></td>
								<td>${meeting.getMeetingTime(const)}</td>
								<#if event.type != 'FinalExam' && event.type != 'MidtermExam'>
								<td>${meeting.getAllocatedTime(const)}</td>
								</#if>
								<td>${meeting.getLocationName()}</td>
								<#switch meeting.getApprovalStatus().name()>
									<#case "Pending">
										<#if meeting.isPast()>
											<td style='color: orange; font-style: italic;'>${msg.approvalNotApprovedPast()}</td>
										<#elseif event.hasExpirationDate()>
											<td style='color: red; font-style: italic;'>${msg.approvalExpire(event.getExpirationDate()?string(const.eventDateFormat()))}</td>
										<#else>
											<td style='color: red; font-style: italic;'>${msg.approvalNotApproved()}</td>
										</#if>									
										<#break>
									<#case "Approved">
										<#if meeting.isPast()>
											<td style='color: gray; font-style: italic;'>${meeting.getApprovalDate()?string(const.eventDateFormat())}</td>
										<#else>
											<td>${meeting.getApprovalDate()?string(const.eventDateFormat())}</td>
										</#if>
										<#break>
									</#switch>		
								<tr>							
							</#switch>
						</#list>
						<#if empty>
							<tr><td colspan='5'><i>${msg.emailEventNoMeetings()}</i></td></tr>
						</#if>
					</table>
				</#if>
				</td></tr>
			</#if>
			<#if event.hasNotes()>
				<!-- notes -->
				<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.emailNotes(event.name)}</td></tr>
				<tr><td><table width=\"100%\" cellspacing='0' cellpadding='3'>
					<tr>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colDate()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colUser()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colAction()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colMeetings()}</td>
						<td style="white-space: nowrap; font-weight: bold;">${msg.colNote()}</td>
					</tr>
					<#list event.notes as note>
						<#switch note.getType().name()>
						<#case "Approve">
							<tr style='background-color: #D7FFD7;'>
							<#break>
						<#case "Reject">
							<tr style='background-color: #FFD7D7;'>
							<#break>
						<#case "Cancel">
							<tr style='background-color: #FFF0AB;'>
							<#break>
						<#default>
							<tr>
						</#switch>
							<td style='white-space: nowrap;'>${note.date?string(const.timeStampFormat())}</td>
							<td style='white-space: nowrap;'>${note.user}</td>
							<td style='white-space: nowrap;'>${note.getType().getName()}</td>
							<td style='white-space: nowrap;'><#if note.meetings??>${note.getMeetings()}</#if></td>
							<td><#if note.note??>${note.note?replace("\n", "<br>")}</#if></td>
						</tr>
					</#list>
					</table>
				</td></tr>
			</#if>
			<#if link?? && sessionId?? && event.id??>
				<tr><td style="width: 100%; padding-top: 10px; text-align: center;">${msg.emailOpenEventOnline(event.name, link, event.id, sessionId)}</td></tr>
			</#if>
		</table>
		<!-- footer -->
		<table style="width: 800px; margin-top: -3px;" align="center">
			<tr>
				<td width="33%" align="left" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${version}</td>
				<td width="34%" align="center" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${msg.pageCopyright()}</td>
				<td width="33%" align="right" style="font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;">${ts?string(const.timeStampFormat())}</td>
			</tr>
		</table>
	</body>
</html>