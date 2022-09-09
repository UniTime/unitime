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
<tt:back-mark back="true" clear="true" title="${MSG.backCbs()}" uri="ecbs.action"/>
<s:form action="ecbs">
	<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
	<table class="unitime-MainTable">
		<TR>
			<TD><loc:message name="propCbsMode"/></TD>
			<TD>
				<s:select name="form.type" list="form.types" listKey="value" listValue="label"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propCbsLimit"/></TD>
			<TD>
				<s:textfield name="form.limit" size="5" maxlength="5"/> %
				&nbsp;<s:fielderror name="form.limit"/>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<s:submit name='form.op' value="%{#msg.buttonChange()}" accesskey="%{#msg.accessChange()}" title="%{#msg.titleChange()}"/>
				<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
			</TD>
		</TR>
	</table>
	<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<table class="unitime-MainTable">
			<TR>
				<TD colspan='2' align='right'>
					<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
				</TD>
			</TR>
		</table>
	<script type="text/javascript">blEndCollapsed('dispFilter');</script>
	
	<s:if test="#request.cbs != null">
		<s:property value="%{printTable()}" escapeHtml="false"/>
		<table class="unitime-MainTable"><tr><td>
			<tt:displayPrefLevelLegend/>
		</td></tr></table>
	</s:if>
	<s:if test="#request.warning != null">
		<table class="unitime-MainTable">
			<TR>
				<TD colspan="2">	
					<i><s:property value="#request.warning"/></i>
				</TD>
			</TR>
		</table>
	</s:if>
</s:form>
</loc:bundle>