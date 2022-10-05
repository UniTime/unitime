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
<s:form action="hibernateQueryTest">
	<s:hidden name="form.start"/>
	<s:hidden name="form.next"/>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectHQL"/></tt:section-title>
					<s:submit name='op' value='%{#msg.actionSubmitQuery()}'
						accesskey='%{#msg.accessSubmitQuery()}' title='%{#msg.titleSubmitQuery(#msg.accessSubmitQuery())}'/>
					<s:submit name='op' value='%{#msg.actionClearCache()}'
						accesskey='%{#msg.accessClearCache()}' title='%{#msg.titleClearCache(#msg.accessClearCache())}'/>
				</tt:section-header>
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD valign="top">
				<loc:message name="propError"/>
			</TD><TD>
				<s:fielderror/>
			</TD></TR>
		</s:if>

		<TR>
			<TD valign="top">
				<loc:message name="propQuery"/>
			</TD>
			<TD>
				<s:textarea name="form.query" rows="12" cols="120"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>

		<s:if test="form.listSize != null && !form.listSize.isEmpty()">
			<TR>
				<TD colspan='2'>
					<tt:section-header>
						<tt:section-title><loc:message name="sectQueryResult"><s:property value="form.listSize"/></loc:message></tt:section-title>
						<s:if test="form.start > 0">
							<s:submit name='op' value='%{#msg.actionPreviousQueryResults()}'
								accesskey='%{#msg.accessPreviousQueryResults()}' title='%{#msg.titlePreviousQueryResults(#msg.accessPreviousQueryResults())}'/>
						</s:if>
						<s:if test="form.next == true">
							<s:submit name='op' value='%{#msg.actionNextQueryResults()}'
								accesskey='%{#msg.accessNextQueryResults()}' title='%{#msg.titleNextQueryResults(#msg.accessNextQueryResults())}'/>
						</s:if>
						<s:if test="form.export == true">
							<s:submit name='op' value='%{#msg.actionExportCsv()}'
								accesskey='%{#msg.accessExportCsv()}' title='%{#msg.titleExportCsv(#msg.accessExportCsv())}'/>
						</s:if>
					</tt:section-header>
				</TD>
			</TR>
			
			<s:if test="#request.result != null && !#request.result.isEmpty()">
				<TR>
					<TD colspan='2'>
						<div style="position: relative; overflow-x: scroll; max-width: 99vw;">
							<s:property value="#request.result" escapeHtml="false"/>
						</div>
					</TD>
				</TR>
			</s:if>
		</s:if>
		
		<s:if test="#request.sql != null && !#request.sql.isEmpty()">
			<TR>
				<TD colspan='2'>
					<br><tt:section-title><loc:message name="sectGeneratedSQL"/></tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<div style="position: relative; overflow-x: scroll; max-width: 99vw;">
						<s:property value="#request.sql" escapeHtml="false"/>
					</div>
				</TD>
			</TR>
		</s:if>
		
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align="right">
				<s:submit name='op' value='%{#msg.actionSubmitQuery()}'
					accesskey='%{#msg.accessSubmitQuery()}' title='%{#msg.titleSubmitQuery(#msg.accessSubmitQuery())}'/>
				<s:submit name='op' value='%{#msg.actionClearCache()}'
					accesskey='%{#msg.accessClearCache()}' title='%{#msg.titleClearCache(#msg.accessClearCache())}'/>
			</TD>
		</TR>
	</TABLE>

</s:form>
</loc:bundle>
