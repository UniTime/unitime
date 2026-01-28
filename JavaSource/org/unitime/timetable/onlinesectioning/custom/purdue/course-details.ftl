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
 <div class='unitime-CourseCatalogDetails'>
<div class='section'>${base.subject} ${base.courseNumber} - ${descriptors.longTitleAndUrl.courseTitle}</div>
<div class='text'>${descriptors.courseDescription.description}</div>
<div class='table'>
	<span class='row'><span class='key'>Short Title:</span><span class='value'>${base.courseDetail.shortTitle}</span></span>
	<span class='row'><span class='key'>College:</span><span class='value'>${base.courseDetail.college} - ${base.courseDetail.collegeDescription}</span></span>
	<span class='row'><span class='key'>Department:</span><span class='value'>${base.courseDetail.department} - ${base.courseDetail.departmentDescription}</span></span>
	<#if base.courseDetail.creditCeuHoursMaximum??>
		<span class='row'><span class='key'>Credit Hours:</span><span class='value'>${base.courseDetail.creditCeuHoursMinimum} ${base.courseDetail.creditCeuHoursConnector?lower_case} ${base.courseDetail.creditCeuHoursMaximum}</span></span>
	<#elseif base.courseDetail.creditCeuHoursMinimum??>
		<span class='row'><span class='key'>Credit Hours:</span><span class='value'>${base.courseDetail.creditCeuHoursMinimum}</span></span>
	</#if>
	<@section 'Levels' base.levelDetail 'level'/>
	<@section 'Grading Modes' base.gradingModeDetail 'gradingMode'/>
	<@section 'Schedule Types' base.scheduleDetail 'scheduleType' 'schedules'/>
	<#if base.courseDetail.repeatStatus??>
		<span class='row'><span class='key'>Repeatable:</span><span class='value'>
			${base.courseDetail.repeatStatusDescription} (${base.courseDetail.repeatStatus})
		</span></span>
	</#if>
	<#if details??>
		<@section 'Attributes' details.attributeDetails 'attribute'/>
	</#if>
	<#if fees?? && fees.SCRFEES?? && (fees.SCRFEES?size > 0)>
		<span class='row'><span class='key'>Fees:</span><span class='value'><#list fees.SCRFEES as fee>
			<#if (fee?index > 0)><br></#if>${fee.feesDescription} $${fee.feeAmount}
		</#list></span></span>
	</#if>
	<#if base.courseDetail.status??>
		<span class='row'><span class='key'>Status:</span><span class='value'>
			${base.courseDetail.statusDescription} (${base.courseDetail.status})
		</span></span>
	</#if>
	<#if restrictions?? && restrictions.campusScheduleRestriction?? && restrictions.campusScheduleRestriction.restrictionType = 'includeRestriction'>
		<@section 'Campuses' restrictions.campusScheduleRestriction 'campus' 'campuses'/>
	</#if>
</div>

<#if descriptors.learningObjectives??>
	<div class='section'>Learning Objectives</div>
	<div class='text'>${descriptors.learningObjectives.objectives}</div>
</#if>

<#if restrictions?? && 
	( restrictions.collegeRegistrationRestriction?? || restrictions.programRegistrationRestriction??
	|| restrictions.fieldOfStudyRegistrationRestrictions?? || restrictions.classRegistrationRestriction??
	|| restrictions.levelRegistrationRestriction?? || restrictions.degreeRegistrationRestriction??
	|| restrictions.cohortRegistrationRestriction?? || restrictions.campusRegistrationRestriction??
	)>
<div class='section'>Restrictions</div>
<#assign hasRestriction = false>
<span class='restrictions'>
<#if restrictions.collegeRegistrationRestriction?? && (restrictions.collegeRegistrationRestriction.colleges?size > 1)>
	<@rsection 'Colleges' restrictions.collegeRegistrationRestriction 'college'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.programRegistrationRestriction?? && (restrictions.programRegistrationRestriction.programs?size > 1)>
	<@rsection 'Programs' restrictions.programRegistrationRestriction 'program'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.fieldOfStudyRegistrationRestrictions??>
	<#list restrictions.fieldOfStudyRegistrationRestrictions?filter(r -> r.fieldsOfStudy??) as r>
		<#if r.fieldOfStudyTypeDescription??>
			<@rsection (r.fieldOfStudyTypeDescription + 's') r 'fieldOfStudy' 'fieldsOfStudy'/>
		<#else>
			<@rsection 'Fields of Study (Major, Minor, or Concentration)' r 'fieldOfStudy' 'fieldsOfStudy'/>
		</#if>
		<#assign hasRestriction = true>
		<#break/>
	</#list>
</#if>
<#if restrictions.classRegistrationRestriction?? && (restrictions.classRegistrationRestriction.classes?size > 1)>
	<@rsection 'Classes' restrictions.classRegistrationRestriction 'classes' 'classes' 'classDescription'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.levelRegistrationRestriction?? && (restrictions.levelRegistrationRestriction.levels?size > 1)>
	<@rsection 'Levels' restrictions.levelRegistrationRestriction 'level'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.degreeRegistrationRestriction?? && (restrictions.degreeRegistrationRestriction.degrees?size > 1)>
	<@rsection 'Degrees' restrictions.degreeRegistrationRestriction 'degree'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.cohortRegistrationRestriction?? && (restrictions.cohortRegistrationRestriction.cohorts?size > 1)>
	<@rsection 'Cohorts' restrictions.cohortRegistrationRestriction 'cohort'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.campusRegistrationRestriction?? && (restrictions.campusRegistrationRestriction.campuses?size > 1)>
	<@rsection 'Campuses' restrictions.campusRegistrationRestriction 'campus' 'campuses'/>
	<#assign hasRestriction = true>
</#if>

<#if !hasRestriction><div class='text'>No restrictions listed.</div></#if>
</span>

</#if>
<#if prerequisites?? && prerequisites.courseCorequisite?? && prerequisites.courseCorequisite.corequisites??>
<div class='section'>Corequisites</div>
<table class='corequisites'>
	<tr class='header'><th>Subject</th><th>Course</th></tr>
	<#list prerequisites.courseCorequisite.corequisites as line>
		<tr><td>${line.courseSubject}</td>
			<td>${line.courseNumber}</td>
		</tr>
	</#list>
</table>
</#if>


<#if prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.prerequisites?? && prerequisites.coursePrerequisite.prerequisites.basic??>
<div class='section'>Prerequisites</div>
<table class='prerequisites'>
	<tr class='header'><th>And/Or</th><th></th><th>Subject</th><th>Course</th><th>Level</th><th>Grade</th><th>Concurrent</th><th></th></tr>
	<#list prerequisites.coursePrerequisite.prerequisites.basic?filter(r -> r.lineOrderSequence??)?sort_by("lineOrderSequence") as line>
		<tr><td><#if line.logicalOperator??>${line.logicalOperator?capitalize}</#if></td>
		<td><#if line.leftParenthesis?? && line.leftParenthesis>(</#if></td>
		<td><#if line.requirement.course?? && line.requirement.course.subject??>${line.requirement.course.subject}</#if></td>
		<td><#if line.requirement.course?? && line.requirement.course.number??>${line.requirement.course.number}</#if></td>
		<td><#if line.requirement.course?? && line.requirement.course.academicLevel??><@studentLevel line.requirement.course.academicLevel/></#if></td>
		<td><#if line.requirement.course?? && line.requirement.course.minimumGrade??>${line.requirement.course.minimumGrade}</#if></td>
		<td><#if line.requirement.course?? && line.requirement.course.concurrentEnrollment?? && line.requirement.course.concurrentEnrollment == 'allowed'>Yes<#else>No</#if></td>
		<td><#if line.rightParenthesis?? && line.rightParenthesis>)</#if></td></tr>
	</#list>
</table>
<#elseif prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.checkMethodDetails?? &&
	prerequisites.coursePrerequisite.checkMethodDetails.checkMethod?? && prerequisites.coursePrerequisite.checkMethodDetails.checkMethod != 'basic'>
	<#assign prereqs = lookup.getPrereqsFromCatalog()/>
	<#if (prereqs?? && prereqs?length > 0) >
		<div class='section'>Prerequisites</div>
		<div class='catalog-section'>${prereqs}</div>		
	</#if>
</#if>

<#if restrictions?? && restrictions.mutualCourseExclusion?? && restrictions.mutualCourseExclusion.courseExclusions?? && (restrictions.mutualCourseExclusion.courseExclusions?size>1) >
<div class='section'>Mutual Exclusions</div>
<table class='mutual-exclusions'>
	<tr class='header'><th>Subject</th><th>Course</th><th>Level</th><th>Min Grade</th></tr>
	<#list restrictions.mutualCourseExclusion.courseExclusions?filter(x -> x.subject??) as line>
		<tr><td>${line.subject}</td>
			<td>${line.courseNumber}</td>
			<td><#if line.level??><@studentLevel line.level/></#if></td>
			<td><#if line.gradeMinimum??>${line.gradeMinimum}</#if></td>
		</tr>
	</#list>
</table>
</#if>

<#if descriptors?? && descriptors.courseText??>
<div class='section'>Course Configurations</div>
<table class='configurations'>
<#list descriptors.courseText.textLines?sort_by("sequenceNumber") as line><#list line.text?split('|') as x>
	<#if x?index = 0><tr class='configuration'><th colspan='3'>Configuration ${line.sequenceNumber}: ${x} Credits</td></tr>
	<tr class='header'><th>Schedule Type</th><th>Weekly Contact Hours</th><th>Instructional Credit Distribution</th></tr>
	<#elseif (x?length > 0)><tr><#list x?trim?split(' ') as z><td><#if z?index=0><@scheduleType z/><#else>${z}</#if></td></#list></tr></#if>
</#list></#list>
</table>
</#if>

<#if disclaimer??>
<div class='disclaimer'>${disclaimer}</div>
</#if>

<#macro shortitem code desc><span class='shortitem' title='${code} - ${desc}'>${desc} (${code})</span></#macro>
<#macro item code desc><span class='item' title='${code} - ${desc}'>${desc} (${code})</span></#macro>
<#macro section sectionLabel items vname vnames = (vname + 's') vdesc = (vname + 'Description')>
	<#if items?? && items[vnames]??>
		<span class='row'><span class='key'><#if adjustedSectionLabel??>${adjustedSectionLabel}<#else>${sectionLabel}</#if>:</span><span class='value'>
			<#list items[vnames]?filter(x -> x[vname]?? && x[vdesc]??)?sort_by(vdesc) as x><@item x[vname] x[vdesc]/></#list>
		</span></span>
	</#if>
</#macro>
<#macro rsection sectionLabel items vname vnames = (vname + 's') vdesc = (vname + 'Description')>
	<#if items.restrictionType?? && items.restrictionType = 'includeRestriction'>
 		<#assign adjustedSectionLabel = 'Must be enrolled in one of the following ' + sectionLabel>
 	<#elseif items.restrictionType?? && items.restrictionType = 'excludeRestriction'>
 		<#assign adjustedSectionLabel = 'Cannot be enrolled in one of the following ' + sectionLabel>
 	</#if>
	<#if items?? && items[vnames]??>
		<span class='row'><span class='key'><#if adjustedSectionLabel??>${adjustedSectionLabel}<#else>${sectionLabel}</#if>:</span><span class='value'>
			<#list items[vnames]?filter(x -> x[vname]?? && x[vdesc]??)?sort_by(vdesc) as x><@item x[vname] x[vdesc]/></#list>
		</span></span>
	</#if>
</#macro>
<#macro scheduleType ref>
	<#if base.scheduleDetail?? && base.scheduleDetail.schedules??>
		<#list base.scheduleDetail.schedules as scheduleType><#if scheduleType.scheduleType = ref>${scheduleType.scheduleTypeDescription}<#return/></#if></#list>
	</#if>${ref}	
</#macro>
<#macro studentLevel ref>
	<#if base.levelDetail?? && base.levelDetail.levels??>
		<#list base.levelDetail.levels as level><#if level.level = ref>${level.levelDescription}<#return/></#if></#list>
	</#if>${ref}	
</#macro>
