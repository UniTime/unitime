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
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="accessStats">
<table class="unitime-MainTable">
	<TR><TD valign="middle" colspan='3'>
		<tt:section-title><loc:message name="filter"/></tt:section-title>
	</TD></TR>
	<TR><TD>
		<loc:message name="filterPage"/>
	</TD><TD colspan='2'>
		<s:select name="page" list="pages" listKey="id" listValue="label" headerKey="" headerValue="%{#msg.itemSelect()}"/>
	</TD></TR>
	<TR><TD>
		<loc:message name="filterChartType"/>
	</TD><TD colspan='2'>
		<s:select name="type" list="types" listKey="id" listValue="label" headerKey="" headerValue="%{#msg.itemSelect()}"/>
	</TD></TR>
	<TR><TD>
		<loc:message name="filterChartInterval"/>
	</TD><TD colspan='2'>
		<s:select name="interval" list="intervals" listKey="id" listValue="label" headerKey="" headerValue="%{#msg.itemSelect()}"
			onchange="document.getElementById('from').style.display = (this.value == 'CUSTOM' ? null : 'none'); document.getElementById('to').style.display = (this.value == 'CUSTOM' ? null : 'none');"
		/>
	</TD></TR>
	<TR id="from" style="display: none;">
		<TD></TD>
		<TD>
			<loc:message name="filterChartFrom"/>
		</TD>
		<TD style="white-space: nowrap;">
			<tt:calendar name="fromDate"/>
			<s:select name="fromSlot" list="times" listKey="id" listValue="value" style="vertical-align: top; height: 22px;"/>
		</TD>
	</TR>
	<TR id="to" style="display: none;">
		<TD></TD>
		<TD>
			<loc:message name="filterChartTo"/>
		</TD>
		<TD style="white-space: nowrap;">
			<tt:calendar name="toDate"/>
			<s:select name="toSlot" list="times" listKey="id" listValue="value" style="vertical-align: top; height: 22px;"/>
		</TD>
	</TR>
	<s:if test="interval == 'CUSTOM'">
		<script>
			document.getElementById('from').style.display = null;
			document.getElementById('to').style.display = null;
		</script>
	</s:if>
	
	<TR><TD colspan="3" align="right"><tt:section-title/></TD></TR>
	<TR><TD colspan="3" align="right"><s:submit value="%{#msg.actionFilterApply()}"/></TD></TR>
	<s:if test="hosts != null">
		<s:iterator value="hosts" var="host" status="stat">
			<s:set var="chartName" value="getChartLabel(#host)"/>
			<s:if test="#chartName != null">
				<s:set var="chartId" value="'Chart' + #stat.index"/>
				<tr><td colspan="3">
					<div id="${chartId}" style="width: calc(100vw - 50px); height: 50vh;"></div>
				</td></tr>
				<s:set var="chartData" value="getChartData(#host)"/>
<script type="text/javascript">
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(draw${chartId});
function draw${chartId}() {
    var data = google.visualization.arrayToDataTable(${chartData});
    var options = { title: '${chartName}', legend: { position: 'bottom' }};
    var chart = new google.visualization.LineChart(document.getElementById('${chartId}'));
    chart.draw(data, options);
}
</script>
			</s:if>
		</s:iterator>
	</s:if>
</table>
</s:form>
</loc:bundle>