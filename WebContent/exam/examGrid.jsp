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
<script type="text/javascript" src="scripts/block.js"></script>
<loc:bundle name="ExaminationMessages"><s:set var="msg" value="#attr.MSG"/> 
<tt:back-mark back="true" clear="true" title="${MSG.backExamGrid()}" uri="examGrid.action"/>
<s:form action="examGrid">
	<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
	<table class="unitime-MainTable">
		<TR>
			<TD width="10%" nowrap><loc:message name="filterShowClassesCourses"/></TD>
			<TD>
				<s:checkbox name="form.showSections"/>
			</TD>
		</TR>
		<TR>
  			<TD nowrap><loc:message name="filterExaminationProblem"/></TD>
			<TD>
				<s:select name="form.examType" list="#request.examTypes" listKey="uniqueId" listValue="label"
					onchange="showDates(this.value);"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyResource"/></TD>
			<TD>
				<s:select name="form.resource" list="form.resources" listKey="value" listValue="label"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="filterTextFilter"/></TD>
			<TD>
				<s:textfield name="form.filter" maxlength="1000" size="40"/>
			</TD>
		</TR>
		<script type="text/javascript">var rowToExamType = new Array();</script>
		<s:iterator value="#request.examTypes" var="et" status="stat">
			<script type="text/javascript">rowToExamType[${stat.index}] = ${et.uniqueId};</script>
			<TR id='daterow.${et.uniqueId}'>
				<TD><loc:message name="propertyPeriodDate"/></TD>
				<TD>
					<s:select name="form.date['%{#et.uniqueId}']" list="form.getDates(#et.uniqueId)" listKey="value" listValue="label"/>
				</TD>
			</TR>
			<TR id='timerow.${et.uniqueId}'>
				<TD><loc:message name="propertyPeriodTime"/></TD>
				<TD>
					<s:select name="form.startTime['%{#et.uniqueId}']" list="form.getStartTimes(#et.uniqueId)" listKey="value" listValue="label"/>
					-
					<s:select name="form.endTime['%{#et.uniqueId}']" list="form.getEndTimes(#et.uniqueId)" listKey="value" listValue="label"/>
				</TD>
			</TR>
		</s:iterator>
		<script type="text/javascript">
			function showDates(examType) {
				for (var x = 0; x < rowToExamType.length; x++) {
					var disp = (rowToExamType[x] == examType ? null : "none");
					document.getElementById("daterow."+rowToExamType[x]).style.display=disp;
					document.getElementById("timerow."+rowToExamType[x]).style.display=disp;
				}
			}
			showDates(document.getElementsByName('form.examType')[0].value);
		</script>
		<TR>
			<TD><loc:message name="propDisplayMode"/></TD>
			<TD>
				<s:select name="form.dispMode" list="form.dispModes" listKey="value" listValue="label"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propBackground"/></TD>
			<TD>
				<s:select name="form.background" list="form.backgrounds" listKey="value" listValue="label"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propShowPeriodPreferences"/></TD>
			<TD>
				<s:checkbox name="form.bgPreferences"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propOrderBy"/></TD>
			<TD>
				<s:select name="form.order" list="form.orders" listKey="value" listValue="label"/>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<s:submit name='form.op' value="%{#msg.buttonChange()}" accesskey="%{#msg.accessChange()}" title="%{#msg.titleChange()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
				<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
			</TD>
		</TR>
	</TABLE>
	<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<table class="unitime-MainTable">
			<TR>
				<TD colspan='2' align='right'>
					<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
					<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
				</TD>
			</TR>
		</TABLE>
	<script type="text/javascript">blEndCollapsed('dispFilter');</script>
	<br><br>
	<a id='timetable'></a>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectExaminationTimetable"/>
					</tt:section-title>
					<A class="l7" href="#legend"><loc:message name="sectLegend"/></A>&nbsp;
				</tt:section-header>
			</TD>
		</TR>
		<s:if test="#request.table.models().isEmpty()">
			<TR>
				<TD>
					<i><loc:message name="messageGridNoMatch"/></i>
				</TD>
			</TR>
		</s:if>
		<s:else>
			<s:property value="%{printTable()}" escapeHtml="false"/>
		</s:else>
	</table>
	<BR>
	<a id='legend'></a>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectLegend"/>
					</tt:section-title>
					<A class="l7" href="#timetable"><loc:message name="sectExaminationTimetable"/></A>&nbsp;
				</tt:section-header>
			</TD>
		</TR>
		<s:property value="%{printLegend()}" escapeHtml="false"/>
	</TABLE>
</s:form>
</loc:bundle>