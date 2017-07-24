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
		<td class='unitime-MainTableHeader' colspan='2'> ${syllabusCourse.get("name").getAsString()}</td>
	</tr>
	<tr>
		<td>Osoba odpowiedzialna:</td><td class='unitime-SubTitle'> ${owner.get("title").getAsString()}  ${owner.get("name").getAsString()}, ${owner.get("email").getAsString()}  </td>
	</tr>
	<tr>
	<td>Prowadzący: </td>
	</tr>
	<#list instructorsList as instructor>
	<tr>
	<td> </td><td>${instructor.get("title").getAsString()} ${instructor.get("name").getAsString()}, ${instructor.get("email").getAsString()} </td>
	</tr>
	</#list>	
	<#list classList as module>
	<tr>
	<td>${module.get("classes_type").getAsString()}: </td><td>${module.get("classes_hours")} godzin</td>
	</tr>
	<tr>
	
	</tr>
	</#list>
     
	
    
	
      <tr>
		<td> Punkty ECTS:</td><td> ${syllabusCourse.get("ects_points").getAsString()} ECTS</td>
	  </tr>
	<tr>
	
		<td>Link: </td><td><a target="_blank" href="${url}"> syllabus</a></td>
	</tr>
	
	<#if utCourse.scheduleBookNote?has_content>
		<tr>
			<td>${msg.colNote()}:</td><td>${utCourse.scheduleBookNote}</td>
		</tr>
	</#if>
	
	<#if utCourse.instructionalOffering.offeringCoordinators?size != 0>
		<tr>
			<td>${msg.colCoordinator()}:</td><td>
				<#list utCourse.instructionalOffering.offeringCoordinators as coordinator>
					<#if coordinator.instructor.email??>
						<a href='mailto:${coordinator.instructor.email}' class='unitime-NoFancyLink'><div>${coordinator.instructor.getName('last-first-middle')}<#if coordinator.responsibility??> (${coordinator.responsibility.abbreviation})</#if></div></a>
					<#else>
						<div>${coordinator.instructor.getName('last-first-middle')}<#if coordinator.responsibility??> (${coordinator.responsibility.abbreviation})</#if></div>
					</#if>
				</#list>
			</td>
		</tr>
	</#if>

	<#list plansList as study_plan>
	
	<tr>
	<td>W planie: </td>
	</tr>
	<tr>
	<td></td><td> ${study_plan.get("academic_year").getAsString()}, ${study_plan.get("course").getAsString()}, ${study_plan.get("level_of_study").getAsString()}, ${study_plan.get("type_of_study").getAsString()}, semestr: ${study_plan.get("semester_number").getAsString()} </td>
	</tr>

	</#list>
	</table>
