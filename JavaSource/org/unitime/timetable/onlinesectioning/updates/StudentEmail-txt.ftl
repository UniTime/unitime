<#if message?? && message?has_content>
${message}</#if><#if reason?? && reason?has_content>

-- ${msg.emailReschedulingReason()} -----------
${reason}
</#if><#if changedCourse??>
-- ${msg.emailCourseEnrollment(changedCourse.subjectArea,changedCourse.courseNumber)} ---<#if changes??>
<@classTableHeader/>
<#list changes as line>
<@classTableLine line/></#list>
</#if><#if changeMessage??>

${changeMessage?replace("<br>","\n")}</#if><#elseif changes??>

-- ${msg.emailEnrollmentChanges()} -----------
<@classTableHeader/>
<#list changes as line>
<@classTableLine line/></#list>
<#if changes?size == 0>${msg.emailNoChange()}</#if></#if><#if advisor?? && advisor.lines??>

-- ${msg.emailAdvisorRequests()} -----------
<@advisorRequestsHeader/>
<#list advisor.lines as line>
<@advisorRequestsLine line/></#list>
<#if disclaimer??>

${disclaimer}</#if></#if><#if requests??>

-- ${msg.emailCourseRequests()} -----------
<#if requests.lines??>
<@courseRequestsHeader/>
<#list requests.lines as line>
<@courseRequestsLine line/></#list>
<#else>${msg.emptyRequests()}</#if></#if><#if classes??>

-- ${msg.emailClassList()} -----------
<#if classes?size == 0>${msg.emailNoSchedule()}<#else><@classTableHeader/><#list classes as line><@classTableLine line/></#list>
<#if credit??>
${msg.totalCredit(credit)}</#if></#if></#if><#if manager>
<#if changed>
${msg.emailChangesMadeBy(helper.user.name)}<#else>
${msg.emailSentBy(helper.user.name)}</#if></#if><#if link??>

${msg.textEmailLinkToUniTime(link)}</#if>

~~~
${version}        ${server.academicSession}        ${ts}
<#macro classTableHeader></#macro>
<#macro classTableLine line><#if line.freeTime>
${msg.freeTimeSubject()} ${msg.freeTimeCourse()} ${line.days} ${line.start} ${line.end}
<#elseif line.assigned><#if line.first>
${line.subject} ${line.courseNumber}<#if line.courseTitle?? && line.courseTitle?has_content> - ${line.courseTitle}</#if>
</#if>
   ${line.type} ${line.name}
<#if line.time??><#if line.time.days gt 0>
      ${line.days} ${line.start} - ${line.end}
<#else>
      ${line.arrangeHours}
</#if><#if line.date?has_content>
      ${line.date}
</#if><#else>
      ${line.arrangeHours}
</#if><#if line.rooms?has_content>
      ${line.rooms}
</#if><#if line.instructors?has_content>
      ${line.instructors}
</#if><#if line.note?? && line.note?has_content>
      ${line.note?replace("\n","\n   ")}
</#if><#if line.credit?? && line.credit?has_content>
      ${msg.colCredit()}: ${line.credit}
</#if><#if line.last && line.courseNote?? && line.courseNote?has_content>
      ${line.courseNote?replace("\n","\n      ")}
</#if><#else>
${line.subject} ${line.courseNumber}
<#if line.note?? && line.note?has_content>
      ${line.note?replace("\n","\n      ")}
</#if></#if>
</#macro>
<#macro classTableNote note>

${note}
</#macro>
<#macro courseRequestsHeader></#macro>
<#macro courseRequestsLine line><#if line.last>

${line.courseName}: ${line.credit}<#if line.note?? && line.note?has_content>

${line.note}
</#if>
<#else>
<#if line.priority?has_content>
${line.priority}: ${line.courseName}<#if line.courseTitle?has_content> - ${line.courseTitle}</#if>
<#else> - ${line.courseName}<#if line.courseTitle?has_content> - ${line.courseTitle}</#if>
</#if><#if requests.hasPref && line.prefs?has_content>   ${msg.colPreferences()}: ${line.prefs}
</#if><#if line.credit?has_content>   ${msg.colCredit()}: ${line.credit}
</#if><#if requests.hasCritical && line.critical>   ${advisor.criticalColumnDescription}
</#if><#if requests.hasWait && wlMode == 'WaitList' && line.waitlist && line.waitListDate??>   ${msg.courseWaitListed()} ${line.waitListDate}
</#if><#if line.note?? && line.note?has_content>
   ${line.note?replace("\n","\n   ")}
</#if></#if>
</#macro>
<#macro advisorRequestsHeader></#macro>
<#macro advisorRequestsLine line><#if line.last>

${line.courseName}: ${line.credit}<#if line.note?? && line.note?has_content>

${line.note}
</#if>
<#else>
<#if line.priority?has_content>
${line.priority}: ${line.courseName}<#if line.courseTitle?has_content> - ${line.courseTitle}</#if>
<#else><#if line.idx == 1>   ${msg.courseRequestsHintAlt(line.firstChoice)}:
</#if>    - ${line.courseName}<#if line.courseTitle?has_content> - ${line.courseTitle}</#if>
</#if><#if advisor.hasPref && line.prefs?has_content && !line.last>   ${msg.colPreferences()}: ${line.prefs}
</#if><#if line.credit?has_content>   ${msg.colCredit()}: ${line.credit}
</#if><#if advisor.hasCritical && line.critical>   ${advisor.criticalColumnDescription}
</#if><#if advisor.hasWait && awlMode == 'WaitList' && !line.last && line.waitlist>   ${msg.courseWaitListed()}
</#if><#if advisor.hasWait && awlMode == 'NoSubs' && !line.last && line.waitlist>   ${msg.courseNoSubs()}
</#if><#if line.note?? && line.note?has_content>
   ${line.note?replace("\n","\n   ")}
</#if>
</#if>
</#macro>