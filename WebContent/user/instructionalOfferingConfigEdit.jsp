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
<%@page import="org.unitime.timetable.webutil.JavascriptFunctions"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
	<tt:confirm name="confirmUpdate"><loc:message name="confirmMayDeleteSubpartsClasses"/></tt:confirm>
	<tt:confirm name="confirmDeleteConfig"><loc:message name="confirmDeleteExistingSubpartsClasses"/></tt:confirm>
<tt:session-context/>
<SCRIPT type="text/javascript">
	<!--
		<%=JavascriptFunctions.getJsConfirm(sessionContext)%>

		function confirmNumClasses(numClasses) {
			if (numClasses > 100) {
				if (!confirmDelete("<%=MSG.confirmCreateTooManyClasses()%>".replace("{0}",numClasses))){
			        return(false);
				}
			}
		    return(true);
		}

		function confirmDelete(msg) {
			if (jsConfirm!=null && !jsConfirm) {
				return true;
			} 
				
			if(confirm(msg)) {
				return true;
			} 
			else {
				return false;
			}
		}

		function doClick(op, id) {
			document.forms[0].elements["hdnOp"].value=op;
			document.forms[0].elements["id"].value=id;
			document.forms[0].submit();
		}
	// -->
</SCRIPT>
<s:form action="instructionalOfferingConfigEdit">
	<s:hidden name="form.configId" />
	<s:hidden name="form.instrOfferingId" />
	<s:hidden name="form.subjectArea"/>
	<s:hidden name="form.courseNumber"/>
	<s:hidden name="form.notOffered" />
	<s:hidden name="form.configCount" />
	<INPUT type="hidden" name="id" value = "">
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="doit" value="Cancel">
	
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<A  title="Back to Instructional Offering List (Alt+I)" 
							accesskey="I"
							class="l8" 
							href="instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1&subjectAreaIds=${subjArea}&courseNbr=${crsNbr}#A${form.courseOfferingId}">
						<B><s:property value="form.instrOfferingName"/></B></A>
						<s:hidden name="form.instrOfferingName"/>											
						<s:hidden name="form.courseOfferingId"/>
					</tt:section-title>
					<s:if test="form.configId == 0 && #request.subpartsExist == 'true'">
						<s:submit
							name='op' value='%{#msg.actionSaveConfiguration()}'
							accesskey='%{#msg.accessSaveConfiguration()}' title='%{#msg.titleSaveConfiguration(#msg.accessSaveConfiguration())}'
						/>
					</s:if>
					<s:if test="form.configId != 0 && #request.subpartsExist == 'true'">
						<s:submit
							name='op' value='%{#msg.actionUpdateConfiguration()}'
							accesskey='%{#msg.accessUpdateConfiguration()}' title='%{#msg.titleUpdateConfiguration(#msg.accessUpdateConfiguration())}'
							onclick="return confirmUpdate();"
						/>
						<sec:authorize access="hasPermission(#form.configId, 'InstrOfferingConfig', 'InstrOfferingConfigDelete')">
							<s:submit
								name='op' value='%{#msg.actionDeleteConfiguration()}'
								accesskey='%{#msg.accessDeleteConfiguration()}' title='%{#msg.titleDeleteConfiguration(#msg.accessDeleteConfiguration())}'
								onclick="return confirmDeleteConfig();"
							/>
						</sec:authorize>
					</s:if>
					<s:submit
						name='op' value='%{#msg.actionBackToIODetail()}'
						accesskey='%{#msg.accessBackToIODetail()}' title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'
					/>
				</tt:section-header>
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div>
				<s:fielderror escape="false"/>
			</TD></TR>
		</s:if>

		<TR>
			<TD><loc:message name="propertyConfigurationName"/></TD>
			<TD>
				<s:textfield name="form.name" size="20" maxlength="20" />
			</TD>
		</TR>
		
		<s:hidden name="form.instructionalMethodEditable"/>
		<s:if test="form.instructionalMethods != null && !form.instructionalMethods.isEmpty()">
			<TR>
				<TD><loc:message name="propertyInstructionalMethod"/></TD>
				<TD colspan="2">
					<s:if test="form.instructionalMethodEditable == true">
						<s:if test="form.instructionalMethodDefault == null">
							<s:select name="form.instructionalMethod"
								list="form.instructionalMethods" listKey="id" listValue="value"
								headerKey="-1" headerValue="%{#msg.selectNoInstructionalMethod()}" 
							/>
						</s:if>
						<s:if test="form.instructionalMethodDefault != null">
							<s:select name="form.instructionalMethod"
								list="form.instructionalMethods" listKey="id" listValue="value"
								headerKey="-1" headerValue="%{#msg.defaultInstructionalMethod(form.instructionalMethodDefault)}" 
							/>
						</s:if>
					</s:if>
					<s:else>
						<s:hidden name="form.instructionalMethod"/>
						<s:if test="form.instructionalMethod == -1">
							<s:if test="form.instructionalMethodDefault == null">
								<loc:message name="selectNoInstructionalMethod"/>
							</s:if>
							<s:if test="form.instructionalMethodDefault != null">
								<loc:message name="defaultInstructionalMethod"><s:property value="form.instructionalMethodDefault"/></loc:message>
							</s:if>
						</s:if>
						<s:if test="form.instructionalMethod != -1">
							<s:iterator value="form.instructionalMethods" var="im">
								<s:if test="#im.id == form.instructionalMethod">
									<s:property value="#im.value"/>
								</s:if>
							</s:iterator>
						</s:if>
					</s:else>
				</TD>
			</TR>
		</s:if>		

		<TR>
			<TD><loc:message name="propertyUnlimitedEnrollment"/></TD>
			<TD>
				<s:checkbox name="form.unlimited" onclick="doClick('unlimitedEnrollment', '');" />
			</TD>
		</TR>
		
		<s:if test="form.unlimited != true">
		<TR>
			<TD><loc:message name="propertyConfigurationLimit"/><font class="reqField">*</font></TD>
			<TD>
				<s:textfield name="form.limit" size="4" maxlength="4" id="limit"/>
			</TD>
		</TR>
		</s:if>
		
		<s:if test="form.catalogLinkLabel != null">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="${form.catalogLinkLocation}" target="_blank"><s:property value="form.catalogLinkLabel"/></A>
			</TD>
		</TR>
		</s:if>
		<tt:hasProperty name="unitime.custom.CourseUrlProvider">
			<TR>
				<TD><loc:message name="propertyCourseCatalog"/> </TD>
				<TD>
					<span id='UniTimeGWT:CourseLink' style="display: none;">-<s:property value="form.instrOfferingId"/></span>
				</TD>
			</TR>
		</tt:hasProperty>
		
		<s:hidden name="form.durationTypeEditable"/>
		<s:hidden name="form.durationTypeDefault"/>
		<s:if test="form.durationTypeEditable != true">
			<s:hidden name="form.durationType"/>
		</s:if>
		<s:else>
			<TR>
				<TD><loc:message name="propertyClassDurationType"/></TD>
				<TD colspan="2">
					<s:select name="form.durationType"
						list="form.durationTypes" listKey="id" listValue="value"
						headerKey="-1" headerValue="%{form.durationTypeDefault}"
						onchange="var el = document.getElementById('durationColumn'); if (el != null) el.innerText = this.options[this.selectedIndex].text;"/>
				</TD>
			</TR>
		</s:else>
		
		<TR>
			<TD><loc:message name="filterInstructionalType"/></TD>
			<TD>
				<s:select name="form.itype"
					list="itypes" listKey="value" listValue="label"
					onchange="itypeChanged(this);"
					/>
				&nbsp;
				<s:submit
					name='op' value='%{#msg.actionAddInstructionalTypeToConfig()}'
					accesskey='%{#msg.accessAddInstructionalTypeToConfig()}' title='%{#msg.titleAddInstructionalTypeToConfig(#msg.accessAddInstructionalTypeToConfig())}'
					/>
		</TR>		

		<TR>
			<TD colspan="2">
			&nbsp;
			</TD>
		</TR>

	</TABLE>
	
	<table class="unitime-MainTable">
		<s:property value="#request.configsList" escapeHtml="false"/>
	</table>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2"><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
					<s:if test="form.configId == 0 && #request.subpartsExist == 'true'">
						<s:submit
							name='op' value='%{#msg.actionSaveConfiguration()}'
							accesskey='%{#msg.accessSaveConfiguration()}' title='%{#msg.titleSaveConfiguration(#msg.accessSaveConfiguration())}'
						/>
					</s:if>
					<s:if test="form.configId != 0 && #request.subpartsExist == 'true'">
						<s:submit
							name='op' value='%{#msg.actionUpdateConfiguration()}'
							accesskey='%{#msg.accessUpdateConfiguration()}' title='%{#msg.titleUpdateConfiguration(#msg.accessUpdateConfiguration())}'
							onclick="return confirmUpdate();"
						/>
						<sec:authorize access="hasPermission(#form.configId, 'InstrOfferingConfig', 'InstrOfferingConfigDelete')">
							<s:submit
								name='op' value='%{#msg.actionDeleteConfiguration()}'
								accesskey='%{#msg.accessDeleteConfiguration()}' title='%{#msg.titleDeleteConfiguration(#msg.accessDeleteConfiguration())}'
								onclick="return confirmDeleteConfig();"
							/>
						</sec:authorize>
					</s:if>
					<s:submit
						name='op' value='%{#msg.actionBackToIODetail()}'
						accesskey='%{#msg.accessBackToIODetail()}' title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'
					/>			
			</TD>
		</TR>
	</TABLE>
</s:form>
<SCRIPT type="text/javascript">
	function itypeChanged(itypeObj) {
		var options = itypeObj.options;
		var currentId = itypeObj.options[itypeObj.selectedIndex].value;
		var basic = true;
		if (currentId=='more') {
			basic = false;
		} else if (currentId=='less') {
			basic = true;
		} else return;
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						options.length=1;
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							options[i+1] = new Option(optVal, optId, (currentId==optId));
						}
						if (basic)
							options[count+1] = new Option("${MSG.selectMoreOptions()}","more",false);
						else
							options[count+1] = new Option("${MSG.selectLessOptions()}","less",false);
						options[count+1].style.backgroundColor='rgb(223,231,242)';
					}
				}
			}
		};
	
		// Request
		var vars = "basic="+basic;
		req.open( "POST", "ajax/itypesAjax.action", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
</SCRIPT>
</loc:bundle>
