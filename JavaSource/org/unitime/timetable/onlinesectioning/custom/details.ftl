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
</table>