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
<#if descriptors.courseDescription??>
	<div class='text'>${descriptors.courseDescription.description}</div>
</#if>
<div class='table'>
	<span class='row'><span class='key'>Short Title:</span><span class='value'><@longitem base.courseDetail.shortTitle/></span></span>
	<#if base.courseDetail.college??>
	<span class='row'><span class='key'>College:</span><span class='value'><@longitem (base.courseDetail.collegeDescription + ' (' + base.courseDetail.college + ')')/></span></span>
	</#if>
	<#if base.courseDetail.department??>
	<span class='row'><span class='key'>Department:</span><span class='value'><@longitem (base.courseDetail.departmentDescription + ' (' + base.courseDetail.department + ')')/></span></span>
	</#if>
	<#if base.courseDetail.creditCeuHoursMaximum??>
		<span class='row'><span class='key'>Credit Hours:</span><span class='value'><@longitem (base.courseDetail.creditCeuHoursMinimum + ' ' + base.courseDetail.creditCeuHoursConnector?lower_case + ' ' + base.courseDetail.creditCeuHoursMaximum)/></span></span>
	<#elseif base.courseDetail.creditCeuHoursMinimum??>
		<span class='row'><span class='key'>Credit Hours:</span><span class='value'><@longitem base.courseDetail.creditCeuHoursMinimum/></span></span>
	</#if>
	<@section 'Levels' base.levelDetail 'level'/>
	<@section 'Grading Modes' base.gradingModeDetail 'gradingMode'/>
	<@section 'Schedule Types' base.scheduleDetail 'scheduleType' 'schedules'/>
	<#if base.courseDetail.repeatStatus??>
		<span class='row'><span class='key'>Repeatable:</span><span class='value'>
			<@longitem (base.courseDetail.repeatStatusDescription + ' (' + base.courseDetail.repeatStatus + ')')/>
		</span></span>
	</#if>
	<#if details?? && details.attributeDetails??>
		<@section 'Attributes' details.attributeDetails 'attribute'/>
	<#elseif details?? && details.attributeDetail??>
		<@section 'Attributes' details.attributeDetail 'attribute'/>
	</#if>
	<#if fees?? && fees.SCRFEES?? && (fees.SCRFEES?size > 0)>
		<span class='row'><span class='key'>Fees:</span><span class='value'><#list fees.SCRFEES as fee>
			<#if (fee?index > 0)></#if><@longitem (fee.feesDescription + ' $' + fee.feeAmount)/>
		</#list></span></span>
	</#if>
	<#if base.courseDetail.status??>
		<span class='row'><span class='key'>Status:</span><span class='value'>
			<@longitem (base.courseDetail.statusDescription + ' (' + base.courseDetail.status + ')')/>
		</span></span>
	</#if>
	<#if restrictions?? && restrictions.campusScheduleRestriction?? && restrictions.campusScheduleRestriction.restrictionType = 'includeRestriction'>
		<@section 'Campuses' restrictions.campusScheduleRestriction 'campus' 'campuses'/>
	</#if>
</div>

<#if descriptors.learningObjectives?? && descriptors.learningObjectives.objectives??>
	<@header 'Learning Objectives' 'LearnObj'/>
	<div class='text' id='LearnObj'>${descriptors.learningObjectives.objectives}</div>
<#elseif descriptors.learningObjective?? && descriptors.learningObjective.objective??>
	<@header 'Learning Objectives' 'LearnObj'/>
	<div class='text' id='LearnObj'>${descriptors.learningObjective.objective}</div>
</#if>

<#if restrictions?? && 
	( restrictions.collegeRegistrationRestriction?? || restrictions.programRegistrationRestriction??
	|| restrictions.fieldOfStudyRegistrationRestrictions?? || restrictions.classRegistrationRestriction??
	|| restrictions.levelRegistrationRestriction?? || restrictions.degreeRegistrationRestriction??
	|| restrictions.cohortRegistrationRestriction?? || restrictions.campusRegistrationRestriction??
	)>
<@header 'Restrictions' 'Restrictions'/>
<#assign hasRestriction = false>
<span class='restrictions' id='Restrictions'>
<#if restrictions.levelRegistrationRestriction?? && (restrictions.levelRegistrationRestriction.levels?filter(x -> x.level??)?size > 0)>
	<@rsection 'Levels' restrictions.levelRegistrationRestriction 'level'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.degreeRegistrationRestriction?? && (restrictions.degreeRegistrationRestriction.degrees?filter(x -> x.degree??)?size > 0)>
	<@rsection 'Degrees' restrictions.degreeRegistrationRestriction 'degree'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.collegeRegistrationRestriction?? && (restrictions.collegeRegistrationRestriction.colleges?filter(x -> x.college??)?size > 0)>
	<@rsection 'Colleges' restrictions.collegeRegistrationRestriction 'college'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.programRegistrationRestriction?? && (restrictions.programRegistrationRestriction.programs?filter(x -> x.program??)?size > 0)>
	<@rsection 'Programs' restrictions.programRegistrationRestriction 'program'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.fieldOfStudyRegistrationRestrictions??>
	<#list restrictions.fieldOfStudyRegistrationRestrictions?filter(r -> r.fieldsOfStudy??) as r>
		<#if (r.fieldsOfStudy?filter(x -> x.fieldOfStudy??)?size > 0)>
			<#if r.fieldOfStudyTypeDescription??>
				<@rsection (r.fieldOfStudyTypeDescription + 's') r 'fieldOfStudy' 'fieldsOfStudy'/>
			<#else>
				<@rsection 'Fields of Study (Major, Minor, or Concentration)' r 'fieldOfStudy' 'fieldsOfStudy'/>
			</#if>
		</#if>
		<#assign hasRestriction = true>
		<#break/>
	</#list>
</#if>
<#if restrictions.cohortRegistrationRestriction?? && (restrictions.cohortRegistrationRestriction.cohorts?filter(x -> x.cohort??)?size > 0)>
	<@rsection 'Cohorts' restrictions.cohortRegistrationRestriction 'cohort'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.classRegistrationRestriction?? && (restrictions.classRegistrationRestriction.classes?filter(x -> x.classes??)?size > 0)>
	<@rsection 'Classes' restrictions.classRegistrationRestriction 'classes' 'classes' 'classDescription'/>
	<#assign hasRestriction = true>
</#if>
<#if restrictions.campusRegistrationRestriction?? && (restrictions.campusRegistrationRestriction.campuses?filter(x -> x.campus??)?size > 0)>
	<@rsection 'Campuses' restrictions.campusRegistrationRestriction 'campus' 'campuses'/>
	<#assign hasRestriction = true>
</#if>

<#if !hasRestriction><div class='text'>No restrictions listed.</div></#if>
</span>

</#if>
<#if prerequisites?? && prerequisites.courseCorequisite?? && prerequisites.courseCorequisite.corequisites?? && (prerequisites.courseCorequisite.corequisites?filter(x -> x.courseSubject??)?size > 0) >
<@header 'Corequisites' 'Corequisites'/>
<table class='corequisites' id='Corequisites'>
	<tr class='header'><th>Subject</th><th>Course</th></tr>
	<#list prerequisites.courseCorequisite.corequisites?filter(x -> x.courseSubject??) as line>
		<tr><td>${line.courseSubject!'-'}</td>
			<td>${line.courseNumber!'-'}</td>
		</tr>
	</#list>
</table>
</#if>

<#if prerequisites?? && prerequisites.courseEquivalent?? && prerequisites.courseEquivalent.equivalents?? && (prerequisites.courseEquivalent.equivalents?filter(x -> x.courseSubject??)?size > 0) >
<@header 'Equivalents' 'Equivalents'/>
<table class='equivalents' id='Equivalents'>
	<tr class='header'><th>Subject</th><th>Course</th></tr>
	<#list prerequisites.courseEquivalent.equivalents?filter(x -> x.courseSubject??) as line>
		<tr><td>${line.courseSubject!'-'}</td>
			<#if line.courseNumber??><td>${line.courseNumber}</td><#else><td>${line.course!'-'}</td></#if>
		</tr>
	</#list>
</table>
</#if>


<#if prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.prerequisites?? && prerequisites.coursePrerequisite.prerequisites.basic??>
<@header 'Prerequisites' 'Prerequisites'/>
<table class='prerequisites' id='Prerequisites'>
	<tr class='header'><th>And/Or</th><th><span style='display:none;'>Left Parenthesis</span></th><th>Subject</th><th>Course</th><th>Level</th><th>Grade</th><th>Concurrent</th><th><span style='display:none;'>Right Parenthesis</span></th></tr>
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
<#elseif prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.prerequisite?? && prerequisites.coursePrerequisite.prerequisite.basicPrerequisites??>
<@header 'Prerequisites' 'Prerequisites'/>
<table class='prerequisites' id='Prerequisites'>
	<tr class='header'><th>And/Or</th><th><span style='display:none;'>Left Parenthesis</span></th><th>Subject</th><th>Course</th><th>Level</th><th>Grade</th><th>Concurrent</th><th><span style='display:none;'>Right Parenthesis</span></th></tr>
	<#list prerequisites.coursePrerequisite.prerequisite.basicPrerequisites?filter(r -> r.lineOrderSequence??)?sort_by("lineOrderSequence") as line>
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
		<@header 'Prerequisites' 'Prerequisites'/>
		<div class='catalog-section' id='Prerequisites'>${prereqs}</div>
	</#if>
<#elseif prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.checkMethodDetail?? &&
	prerequisites.coursePrerequisite.checkMethodDetail.checkMethod?? && prerequisites.coursePrerequisite.checkMethodDetail.checkMethod != 'basic'>
	<#assign prereqs = lookup.getPrereqsFromCatalog()/>
	<#if (prereqs?? && prereqs?length > 0) >
		<@header 'Prerequisites' 'Prerequisites'/>
		<div class='catalog-section' id='Prerequisites'>${prereqs}</div>
	</#if>
</#if>

<#if restrictions?? && restrictions.mutualCourseExclusion?? && restrictions.mutualCourseExclusion.courseExclusions?? && (restrictions.mutualCourseExclusion.courseExclusions?size>1) >
<@header 'Mutual Exclusions' 'Exclusions'/>
<table class='mutual-exclusions' id='Exclusions'>
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
<@header 'Course Configurations' 'Configurations'/>
<div class='configurations' id='Configurations'>
<#list descriptors.courseText.textLines?sort_by("sequenceNumber") as line><#list line.text?split('|') as x>
	<#if x?index = 0><div class='text'>Configuration ${line.sequenceNumber}: ${x} Credits</div>
	<table class='configuration'>
	<tr class='header'><th>Schedule Type</th><th>Weekly Contact Hours</th><th>Instructional Credit Distribution</th></tr>
	<#elseif (x?length > 0)><tr><#list x?trim?split(' ') as z><#if z?index=0><td><#else><td align='center'></#if><#if z?index=0><@scheduleType z/><#else>${z}</#if></td></#list></tr></#if>
</#list></table></#list>
</div>
</#if>

<#if disclaimer??>
<div class='disclaimer'>${disclaimer}</div>
</#if>

<#macro shortitem code desc><span class='shortitem' title='${code?xhtml} - ${desc?xhtml}'>${desc} (${code})</span></#macro>
<#macro longitem text><span class='longitem' title='${text?xhtml}'>${text}</span></#macro>
<#macro item code desc><span class='item' title='${code?xhtml} - ${desc?xhtml}'>${desc} (${code})</span></#macro>
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
			<#list items[vnames]?filter(x -> x[vname]?? && x[vdesc]??)?sort_by(vname) as x><@item x[vname] x[vdesc]/></#list>
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
<#macro header name id>
<div class='section'>
	<img alt='Open ${name?xhtml}' src='images/expand_node_btn.gif' onClick="document.getElementById('${id}').style.display='block';this.style.display='none';document.getElementById('${id}-close').style.display='inline-block';localStorage.setItem('UniTime:CourseCatalog${id}', '1');" id='${id}-open' style='display:none;'/>
	<img alt='Close ${name?xhtml}' src='images/collapse_node_btn.gif' onClick="document.getElementById('${id}').style.display='none';this.style.display='none';document.getElementById('${id}-open').style.display='inline-block';localStorage.setItem('UniTime:CourseCatalog${id}', '0');" id='${id}-close' style='display:none;'/>
	${name}</div>
	<script>
		if ('0' == localStorage.getItem('UniTime:CourseCatalog${id}')) {
			$doc.getElementById('${id}').style.display='none';
			$doc.getElementById('${id}-open').style.display='inline-block';
			$doc.getElementById('${id}-close').style.display='none';
		} else {
			$doc.getElementById('${id}-close').style.display='inline-block';
		}
	</script>
</#macro>
