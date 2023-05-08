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
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteSolverConfig"/></tt:confirm>
<s:form action="solverSettings">
<s:if test="form.op == #msg.actionAddNewSolverConfig()">
 	<table class="unitime-MainTable">
		<TR>
			<TD colspan="4">
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectSolverConfigurations"/>
					</tt:section-title>
					<s:submit name='op' value='%{#msg.actionAddNewSolverConfig()}'
							accesskey='%{#msg.accessAddNewSolverConfig()}' title='%{#msg.titleAddNewSolverConfig(#msg.accessAddNewSolverConfig())}'/>
				</tt:section-header>
			</TD>
		</TR>
		<s:property value="getSolverSettingsTable()" escapeHtml="false"/>
		<TR>
			<TD align="right" colspan="4">
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align="right" colspan="4">
				<s:submit name='op' value='%{#msg.actionAddNewSolverConfig()}'
						accesskey='%{#msg.accessAddNewSolverConfig()}' title='%{#msg.titleAddNewSolverConfig(#msg.accessAddNewSolverConfig())}'/>			
			</TD>
		</TR>
	</TABLE>
</s:if><s:else>
	<s:hidden name="form.uniqueId"/><s:fielderror fieldName="form.uniqueId"/>
	<input type='hidden' name='op2' value=''>

	<table class="unitime-MainTable unitime-Table unitime-SolverConfigEdit">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveSolverConfig()">
							<loc:message name="sectAddSolverConfiguration"/>
						</s:if><s:else>
							<loc:message name="sectEditSolverConfiguration"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.op == #msg.actionSaveSolverConfig()">
						<s:submit name='op' value='%{#msg.actionSaveSolverConfig()}'
							accesskey='%{#msg.accessSaveSolverConfig()}' title='%{#msg.titleSaveSolverConfig(#msg.accessSaveSolverConfig())}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionUpdateSolverConfig()}'
							accesskey='%{#msg.accessUpdateSolverConfig()}' title='%{#msg.titleUpdateSolverConfig(#msg.accessUpdateSolverConfig())}'/>
						<s:submit name='op' value='%{#msg.actionDeleteSolverConfig()}'
							accesskey='%{#msg.accessDeleteSolverConfig()}' title='%{#msg.titleDeleteSolverConfig(#msg.accessDeleteSolverConfig())}'
							onclick="return confirmDelete();"/>
						<s:submit name='op' value='%{#msg.actionExportSolverConfig()}'
							accesskey='%{#msg.accessExportSolverConfig()}' title='%{#msg.titleExportSolverConfig(#msg.accessExportSolverConfig())}'/>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToSolverConfigs()}'
						accesskey='%{#msg.accessBackToSolverConfigs()}' title='%{#msg.titleBackToSolverConfigs(#msg.accessBackToSolverConfigs())}'/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="fieldReference"/>:</TD>
			<TD>
				<s:textfield name="form.name" size="30" maxlength="100"/>
				<s:fielderror fieldName="form.name"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldName"/>:</TD>
			<TD>
				<s:textfield name="form.description" size="30" maxlength="1000"/>
				<s:fielderror fieldName="form.description"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldAppearance"/>:</TD>
			<TD>
				<s:select name="form.appearance" onchange="op2.value='Refresh'; submit();"
					list="form.appearances" listKey="value" listValue="label"
					/>
				<s:fielderror fieldName="form.appearance"/>
			</TD>
		</TR>
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="3" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>	
		
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<s:iterator value="solverParameterGroups" var="group">
			<s:if test="#group.solverType == form.appearanceType.solverType && #group.visible">
				<TR><TD colspan="2">
					<DIV class="WelcomeRowHead"><s:property value="#group.description"/></DIV>
				</TD></TR>
				<s:iterator value="#group.visibleParameters" var="def">
					<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';" 
						onmouseout="this.style.backgroundColor='transparent';"><TD>
						<s:if test="#def.type == 'boolean'">
							<s:checkbox name='form.useDefault[%{#def.uniqueId}]'
								onclick="var e = document.getElementById('p%{#def.uniqueId}'); e.disabled = this.checked; e.checked = %{#def.default};"
								/>
						</s:if><s:elseif test="#def.type == 'timepref'">
							<s:checkbox name='form.useDefault[%{#def.uniqueId}]'
								onclick="document.getElementById('pe%{#def.uniqueId}').style.display=(this.checked?'none':'block'); document.getElementById('pd%{#def.uniqueId}').style.display=(this.checked?'block':'none');"
								/>
						</s:elseif><s:else>
							<s:checkbox name='form.useDefault[%{#def.uniqueId}]'
								onclick="var e = document.getElementById('p%{#def.uniqueId}'); e.disabled = this.checked; e.value = '%{#def.default}';"
								/>
						</s:else>
						<s:property value="#def.description" escapeHtml="false"/>:
					</TD><TD>
						<s:if test="#def.type == 'boolean'">
							<s:checkbox name="form.parameter[%{#def.uniqueId}]" disabled="%{form.useDefault[#def.uniqueId]}" id="p%{#def.uniqueId}"/>
						</s:if><s:elseif test="#def.type.startsWith('enum(') && #def.type.endsWith(')')">
							<s:select name="form.parameter[%{#def.uniqueId}]" disabled="%{form.useDefault[#def.uniqueId]}" list="#def.options" id="p%{#def.uniqueId}"/>
						</s:elseif><s:elseif test="#def.type == 'double' || #def.type == 'integer'">
							<s:textfield name="form.parameter[%{#def.uniqueId}]" disabled="%{form.useDefault[#def.uniqueId]}" id="p%{#def.uniqueId}"
								size="10" maxlength="10" cssStyle="text-align: right;"/>
						</s:elseif><s:elseif test="#def.type == 'date'">
							<tt:calendar name="form.parameter[%{#def.uniqueId}]" disabled="%{form.useDefault[#def.uniqueId]}" id="p%{#def.uniqueId}" format="yyyy-MM-dd"/>
						</s:elseif><s:elseif test="#def.type == 'timepref'">
							<div id='pd${def.uniqueId}' style="display: block;">
								<img border="0"
									onmouseover="showGwtInstructorAvailabilityHint(this, '${def.getDefault()}');"
									onmouseout="hideGwtInstructorAvailabilityHint();"
									src="pattern?p=${def.getDefault()}">
							</div>
							</TD></TR><TR><TD colspan='2'>
							<div id='pe${def.uniqueId}' style="display: none;">
								<div id='UniTimeGWT:InstructorAvailability'><s:hidden name='form.parameter[%{#def.uniqueId}]' id="p%{#def.uniqueId}"/></div>
							</div>
							<s:if test="form.useDefault[#def.uniqueId] == false">
								<script type="text/javascript">
									document.getElementById('pd${def.uniqueId}').style.display = 'none';
									document.getElementById('pe${def.uniqueId}').style.display = 'block';
								</script>
							</s:if>
						</s:elseif><s:else>
							<s:textfield name="form.parameter[%{#def.uniqueId}]" disabled="%{form.useDefault[#def.uniqueId]}" id="p%{#def.uniqueId}"
								size="30" maxlength="2048"/>
						</s:else>						
						<s:fielderror><s:param>form.parameter[${def.uniqueId}]</s:param></s:fielderror>
					</TD></TR>
				</s:iterator>
			</s:if><s:else>
				<s:iterator value="#group.visibleParameters" var="def">
					<s:hidden name="form.parameter[%{#def.uniqueId}]"/>
					<s:hidden name="form.useDefault[%{#def.uniqueId}]"/>
				</s:iterator>
			</s:else>
		</s:iterator>
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>		
		<TR>
			<TD align="right" colspan="2">
				<s:if test="form.op == #msg.actionSaveSolverConfig()">
						<s:submit name='op' value='%{#msg.actionSaveSolverConfig()}'
							accesskey='%{#msg.accessSaveSolverConfig()}' title='%{#msg.titleSaveSolverConfig(#msg.accessSaveSolverConfig())}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionUpdateSolverConfig()}'
							accesskey='%{#msg.accessUpdateSolverConfig()}' title='%{#msg.titleUpdateSolverConfig(#msg.accessUpdateSolverConfig())}'/>
						<s:submit name='op' value='%{#msg.actionDeleteSolverConfig()}'
							accesskey='%{#msg.accessDeleteSolverConfig()}' title='%{#msg.titleDeleteSolverConfig(#msg.accessDeleteSolverConfig())}'
							onclick="return confirmDelete();"/>
						<s:submit name='op' value='%{#msg.actionExportSolverConfig()}'
							accesskey='%{#msg.accessExportSolverConfig()}' title='%{#msg.titleExportSolverConfig(#msg.accessExportSolverConfig())}'/>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToSolverConfigs()}'
						accesskey='%{#msg.accessBackToSolverConfigs()}' title='%{#msg.titleBackToSolverConfigs(#msg.accessBackToSolverConfigs())}'/>
			</TD>
		</TR>
	</TABLE>
</s:else>
</s:form>
</loc:bundle>
