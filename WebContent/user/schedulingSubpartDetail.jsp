<%--
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
--%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="schedulingSubpartDetail">
	<tt:confirm name="confirmClearAllClassPreference"><loc:message name="confirmClearAllClassPreferences"/></tt:confirm>
	<input type='hidden' name='confirm' value='y'/>
	<s:hidden name="form.schedulingSubpartId"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>

	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<A title="${MSG.titleInstructionalOfferingDetail(MSG.accessInstructionalOfferingDetail())}" 
							accesskey="${MSG.accessInstructionalOfferingDetail()}" class="l8"
							href="instructionalOfferingDetail.action?op=view&io=${form.instrOfferingId}">
							<s:property value="form.subjectArea"/>
							<s:property value="form.courseNbr"/> - <s:property value="form.courseTitle"/>
						</A>:
						<s:property value="form.parentSubpart"/>
						<s:property value="form.instructionalTypeLabel"/>
					</tt:section-title>
					
					<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEdit')">
						<s:submit accesskey='%{#msg.accessEditSubpart()}' name='op' value='%{#msg.actionEditSubpart()}'
							title='%{#msg.titleEditSubpart(#msg.accessEditSubpart())}'/>
					</sec:authorize> 
				
					<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'DistributionPreferenceSubpart')">
						<s:submit accesskey='%{#msg.accessAddDistributionPreference()}' name='op' value='%{#msg.actionAddDistributionPreference()}'
							title='%{#msg.titleAddDistributionPreference(#msg.accessAddDistributionPreference())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousSubpart()}' name='op' value='%{#msg.actionPreviousSubpart()}'
							title='%{#msg.titlePreviousSubpart(#msg.accessPreviousSubpart())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextSubpart()}' name='op' value='%{#msg.actionNextSubpart()}'
							title='%{#msg.titleNextSubpart(#msg.accessEditClass())}'/>
					</s:if>
					<tt:back styleClass="btn" 
						name="${MSG.actionBackSubpartDetail()}" 
						title="${MSG.titleBackSubpartDetail(MSG.accessBackSubpartDetail())}" 
						accesskey="${MSG.accessBackSubpartDetail()}" 
						type="PreferenceGroup">
						<s:property value="form.schedulingSubpartId"/>
					</tt:back>
				</tt:section-header>
			</TD>
		</TR>		

		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>

		<s:if test="form.managingDeptName != null">
			<TR>
				<TD><loc:message name="filterManager"/></TD>
				<TD>
					<s:property value="form.managingDeptName"/>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.parentSubpartLabel != null">
			<TR>
				<TD><loc:message name="propertyParentSchedulingSubpart"/></TD>
				<TD>
					<s:if test="form.parentSubpartId == null">
						<s:property value="form.parentSubpartLabel"/>
					</s:if>
					<s:else>
						<A href="schedulingSubpartDetail.action?ssuid=${form.parentSubpartId}">
							<s:property value="form.parentSubpartLabel"/>
						</A>
					</s:else>
				</TD>
			</TR>
		</s:if>
		<TR>
			<TD> <loc:message name="filterInstructionalType"/> </TD>
			<TD>
				<s:iterator value="#request.itypesList" var="itp">
					<s:if test="#itp.itype == form.instructionalType">
						<s:property value="#itp.desc"/>
					</s:if>
				</s:iterator>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<s:iterator value="#request.datePatternList" var="dp">
					<s:if test="#dp.id == form.datePattern">
						<s:property value="#dp.value"/>
						<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('${MSG.sectPreviewOfDatePattern(dp.value)}', 'dispDatePattern.action?id=${dp.id}&subpartId=${form.schedulingSubpartId}','840','520');">
					</s:if>
				</s:iterator>
			</TD>
		</TR>
		<s:if test="form.autoSpreadInTime == false">
			<TR>
				<TD><loc:message name="propertyAutomaticSpreadInTime"/></TD>
				<TD>
					<loc:message name="classDetailNoSpread"/>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.studentAllowOverlap == true">
		<TR>
			<TD><loc:message name="propertyStudentOverlaps"/></TD>
			<TD>
				<loc:message name="classDetailAllowOverlap"/>
			</TD>
		</TR>
		</s:if>
		<s:if test="form.sameItypeAsParent == false && form.creditText != null && !form.creditText.isEmpty()">
			<TR>
				<TD><loc:message name="propertySubpartCredit"/></TD>
				<TD>
					<s:property value="form.creditText"/>
				</TD>
			</TR>
		</s:if>
		
		<tt:last-change type='SchedulingSubpart'><s:property value="form.schedulingSubpartId"/></tt:last-change>

<!-- Preferences -->
		<TR>
			<TD colspan="2" valign="middle" style="padding-top: 20px;">
				<tt:section-title><loc:message name="sectionTitlePreferences"/></tt:section-title>
			</TD>
		</TR>
		<s:if test="form.unlimitedEnroll == true">
			<s:include value="preferencesDetail2.jspf">
				<s:param name="bldgPref" value="false"/>
				<s:param name="roomFeaturePref" value="false"/>
				<s:param name="roomGroupPref" value="false"/>
			</s:include>
		</s:if>
		<s:else>
			<s:include value="preferencesDetail2.jspf"/>
		</s:else>

<!-- Classes -->
	<s:if test="#request.classTable != null">
		<TR>
			<TD colspan='2' style="padding-top: 20px;">
				<tt:section-header>
					<tt:section-title><loc:message name="sectionTitleClasses"/></tt:section-title>
						<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartDetailClearClassPreferences')">
							<s:submit name='op' value='%{#msg.actionClearClassPreferencesOnSubpart()}'
								onclick="return confirmClearAllClassPreference();"/>
						</sec:authorize> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" valign="middle">
				<s:property value="#request.classTable" escapeHtml="false"/>
			</TD>
		</TR>
	</s:if>
		
		<TR>
			<TD colspan="2">
				<tt:exams type='SchedulingSubpart' add='true'><s:property value="form.schedulingSubpartId"/></tt:exams>
			</TD>
		</TR>


<!-- Buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
					<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEdit')">
						<s:submit accesskey='%{#msg.accessEditSubpart()}' name='op' value='%{#msg.actionEditSubpart()}'
							title='%{#msg.titleEditSubpart(#msg.accessEditSubpart())}'/>
					</sec:authorize> 
				
					<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'DistributionPreferenceSubpart')">
						<s:submit accesskey='%{#msg.accessAddDistributionPreference()}' name='op' value='%{#msg.actionAddDistributionPreference()}'
							title='%{#msg.titleAddDistributionPreference(#msg.accessAddDistributionPreference())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousSubpart()}' name='op' value='%{#msg.actionPreviousSubpart()}'
							title='%{#msg.titlePreviousSubpart(#msg.accessPreviousSubpart())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextSubpart()}' name='op' value='%{#msg.actionNextSubpart()}'
							title='%{#msg.titleNextSubpart(#msg.accessEditClass())}'/>
					</s:if>
					<tt:back styleClass="btn" 
						name="${MSG.actionBackSubpartDetail()}" 
						title="${MSG.titleBackSubpartDetail(MSG.accessBackSubpartDetail())}" 
						accesskey="${MSG.accessBackSubpartDetail()}" 
						type="PreferenceGroup">
						<s:property value="form.schedulingSubpartId"/>
					</tt:back>
			</TD>
		</TR>
	</TABLE>
</s:form>
</loc:bundle>