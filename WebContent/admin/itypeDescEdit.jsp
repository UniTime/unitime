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
<s:form action="itypeDescEdit">
	<s:hidden name="form.uniqueId"/>
	<TABLE style="width:100%;">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="op == #msg.actionSaveIType()"><loc:message name="sectionAddInstructionalTypes"/></s:if>
						<s:else><loc:message name="sectionEditInstructionalTypes"/></s:else>
					</tt:section-title>
					<s:if test="op == #msg.actionSaveIType()">
						<s:submit accesskey='%{#msg.accessSaveIType()}' name='op' value='%{#msg.actionSaveIType()}' title='%{#msg.titleSaveIType(#msg.accessSaveIType())}'/>
					</s:if>
					<s:else>
						<s:submit accesskey='%{#msg.accessUpdateIType()}' name='op' value='%{#msg.actionUpdateIType()}' title='%{#msg.titleUpdateIType(#msg.accessUpdateIType())}'/>
						<sec:authorize access="hasPermission(#form.uniqueId, 'ItypeDesc', 'InstructionalTypeDelete')">
							<s:submit accesskey='%{#msg.accessDeleteIType()}' name='op' value='%{#msg.actionDeleteIType()}' title='%{#msg.titleDeleteIType(#msg.accessDeleteIType())}'/>
						</sec:authorize>
					</s:else>
					<s:submit accesskey='%{#msg.accessBackITypes()}' name='op' value='%{#msg.actionBackITypes()}' title='%{#msg.titleBackITypes(#msg.accessBackITypes())}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldIType"/>:</TD>
			<TD>
				<s:if test="op == #msg.actionSaveIType()">
					<s:textfield name='form.id' maxlength="2" size="2"/>
					<s:fielderror fieldName="form.id"/>
				</s:if>
				<s:else>
					<s:hidden name="form.id"/><s:property value="form.id"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldAbbreviation"/>:</TD>
			<TD>
				<s:textfield name='form.abbreviation' size="7" maxlength="7"/>
				<s:fielderror fieldName="form.abbreviation"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldName"/>:</TD>
			<TD>
				<s:textfield name="form.name" size="50" maxlength="50"/>
				<s:fielderror fieldName="form.name"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldReference"/>:</TD>
			<TD>
				<s:textfield name="form.reference" size="20" maxlength="20"/>
				<s:fielderror fieldName="form.reference"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldType"/>:</TD>
			<TD>
				<s:select name="form.type" list="form.types"/>
				<s:fielderror fieldName="form.type"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="fieldParent"/>:</TD>
			<TD>
				<s:select name="form.parent" list="%{#request.itypesList}" listKey="itype" listValue="desc" headerKey="" headerValue="-"/>
				<s:fielderror fieldName="form.parent"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldOrganized"/>:</TD>
			<TD>
				<s:checkbox name="form.organized"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<s:if test="op == #msg.actionSaveIType()">
					<s:submit accesskey='%{#msg.accessSaveIType()}' name='op' value='%{#msg.actionSaveIType()}' title='%{#msg.titleSaveIType(#msg.accessSaveIType())}'/>
				</s:if>
				<s:else>
					<s:submit accesskey='%{#msg.accessUpdateIType()}' name='op' value='%{#msg.actionUpdateIType()}' title='%{#msg.titleUpdateIType(#msg.accessUpdateIType())}'/>
					<sec:authorize access="hasPermission(#form.uniqueId, 'ItypeDesc', 'InstructionalTypeDelete')">
						<s:submit accesskey='%{#msg.accessDeleteIType()}' name='op' value='%{#msg.actionDeleteIType()}' title='%{#msg.titleDeleteIType(#msg.accessDeleteIType())}'/>
					</sec:authorize>
				</s:else>
				<s:submit accesskey='%{#msg.accessBackITypes()}' name='op' value='%{#msg.actionBackITypes()}' title='%{#msg.titleBackITypes(#msg.accessBackITypes())}'/>
			</TD>
		</TR>
	</TABLE>
</s:form>
</loc:bundle>