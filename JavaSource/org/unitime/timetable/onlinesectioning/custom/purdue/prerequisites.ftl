<#if prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.prerequisite??
	&& prerequisites.coursePrerequisite.prerequisite.basicPrerequisites??
	&& (prerequisites.coursePrerequisite.prerequisite.basicPrerequisites?filter(r -> r.lineOrderSequence??)?size > 0)
><#list prerequisites.coursePrerequisite.prerequisite.basicPrerequisites?filter(r -> r.lineOrderSequence??)?sort_by("lineOrderSequence") as line
><#if line.logicalOperator??> ${line.logicalOperator?lower_case} </#if
><#if line.leftParenthesis?? && line.leftParenthesis>(</#if
><#if line.requirement.course?? && line.requirement.course.subject?? && line.requirement.course.subject?has_content
><#if line.requirement.course.concurrentEnrollment?? && line.requirement.course.concurrentEnrollment == 'allowed'>c:<#else>p:</#if
><#if line.requirement.course.subject?? && line.requirement.course.number??>"${line.requirement.course.subject} ${line.requirement.course.number}"</#if
></#if><#if line.rightParenthesis?? && line.rightParenthesis>)</#if></#list
><#elseif prerequisites?? && prerequisites.coursePrerequisite?? && prerequisites.coursePrerequisite.prerequisites??
	&& prerequisites.coursePrerequisite.prerequisites.basic??
	&& (prerequisites.coursePrerequisite.prerequisites.basic?filter(r -> r.lineOrderSequence??)?size > 0)
><#list prerequisites.coursePrerequisite.prerequisites.basic?filter(r -> r.lineOrderSequence??)?sort_by("lineOrderSequence") as line
><#if line.logicalOperator??> ${line.logicalOperator?lower_case} </#if
><#if line.leftParenthesis?? && line.leftParenthesis>(</#if
><#if line.requirement.course?? && line.requirement.course.subject?? && line.requirement.course.subject?has_content
><#if line.requirement.course.concurrentEnrollment?? && line.requirement.course.concurrentEnrollment == 'allowed'>c:<#else>p:</#if
><#if line.requirement.course.subject?? && line.requirement.course.number??>"${line.requirement.course.subject} ${line.requirement.course.number}"</#if
></#if><#if line.rightParenthesis?? && line.rightParenthesis>)</#if></#list></#if>