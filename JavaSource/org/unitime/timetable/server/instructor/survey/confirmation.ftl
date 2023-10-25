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
 					<td ><img src="http://www.unitime.org/include/unitime.png" border="0" height="80px"/></td>
 					<td style="font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;">${msg.sectInstructorSurvey()}</td>
 				</tr>
 			</table></td></tr>
			<!-- Instructor Survey section -->
			<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${survey.getFormattedName()}</td></tr>
			<tr><td><table width="100%">
				<tr><td>${msg.propAcademicSession()}</td><td>${academicSession}</td></tr>
				<#if survey.hasEmail()>
					<tr><td>${msg.propEmail()}</td><td>${survey.getEmail()}</td></tr>
				</#if>
				<#if submitted??>
					<tr><td>${msg.propSubmitted()}</td><td>${submitted}</td></tr>
				</#if>
				<#if survey.hasDepartments()>
					<tr><td style='vertical-align: top;'>${msg.propDepartment()}</td><td>
						<#list survey.getDepartments() as dept>
							<div>${dept.getLabel()}<#if dept.hasPosition()> (${dept.getPosition().getLabel()})</#if></div>
						</#list>
					</td></tr>
				</#if>
			</table></td></tr>
			<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.sectGeneralPreferences()}</td></tr>
			<tr><td><table width="100%">
				<#if timePrefs??>
					<tr><td style='vertical-align: top;'>${msg.propTimePrefs()}</td><td>${timePrefs}</td></tr>
					<#if survey.timePrefs.note?? && survey.timePrefs.hasHard()>
						<tr><td></td><td style='white-space: pre-wrap; padding-left: 5px;'>${survey.timePrefs.note}</td></tr>
					</#if>
				</#if>
				<#if survey.hasRoomPreferences()>
					<#list survey.getRoomPreferences() as p>
				 		<#if p.hasSelections()>
				 			<tr><td style='vertical-align: top;'>${p.getType()}</td><td>
				 				<#list p.getItems() as item>
				 					<#if p.hasSelections()><#list p.getSelections() as selection>
				 						<#if selection.getItem() == item.getId()>
					 						<#list survey.getPrefLevels() as prefLevel>
					 							<#if prefLevel.getId() == selection.getLevel()>
					 								<div style='color:${prefLevel.getColor()};'>${prefLevel.getTitle()} ${item.getLabel()}</div>
					 								<#if prefLevel.isHard() && selection.hasNote()>
					 									<div style='white-space: pre-wrap; padding-left: 5px;'>${selection.getNote()}</div>
				 									</#if>
				 								</#if>
				 							</#list>
				 						</#if>
				 					</#list></#if>
				 				</#list>
				 			</td></tr>
						</#if>
					</#list>
				</#if>
				<#if survey.hasDistributionPreferences()>
					<#assign p = survey.getDistributionPreferences()>
				 		<#if p.hasSelections()>
				 			<tr><td style='vertical-align: top;'>${p.getType()}</td><td>
				 				<#list p.getItems() as item>
				 					<#if p.hasSelections()><#list p.getSelections() as selection>
				 						<#if selection.getItem() == item.getId()>
					 						<#list survey.getPrefLevels() as prefLevel>
					 							<#if prefLevel.getId() == selection.getLevel()>
					 								<div style='color:${prefLevel.getColor()};'>${prefLevel.getTitle()} ${item.getLabel()}</div>
					 								<#if prefLevel.isHard() && selection.hasNote()>
					 									<div style='white-space: pre-wrap; padding-left: 5px;'>${selection.getNote()}</div>
				 									</#if>
				 								</#if>
				 							</#list>
				 						</#if>
				 					</#list></#if>
				 				</#list>
				 			</td></tr>
						</#if>
				</#if>
				<#if survey.hasNote()>
					<tr><td style='vertical-align: top;'>${msg.propOtherPreferences()}</td><td style='white-space: pre-wrap;'>${survey.getNote()}</td></tr>
				</#if>
			</table></td></tr>
			<tr><td style="width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;">${msg.sectCoursePreferences()}</td></tr>
			<tr><td><table width="100%">
				<tr>
					<#assign style="white-space: pre; font-weight: bold; padding-top: 5px; border-bottom: 1px dashed #9CB0CE;">
					<td style="${style}">${msg.colCourse()}</td>
					<#list survey.getCustomFields() as cf>
						<#assign max = (677 * cf.getLength() / 100)>
						<td style="${style} width: ${max} px;">${cf.getName()}</td>
					</#list>
				<tr>
				<#list survey.getCourses() as ci>
					<#if ci.hasCustomFields()>
						<tr>
							<td style='vertical-align: top;'>${ci.getCourseName()}</td>
							<#list survey.getCustomFields() as cf>
								<#assign max = (677 * cf.getLength() / 100)>
								<#if ci.hasCustomField(cf)>
									<td style='vertical-align: top; white-space: pre-wrap; width: ${max} px;'>${ci.getCustomField(cf)}</td>
								<#else>
									<td></td>
								</#if>
							</#list>		
						</tr>
					</#if>
				</#list>
			</table></td></tr>
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