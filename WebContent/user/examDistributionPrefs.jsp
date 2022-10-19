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
<loc:bundle name="ExaminationMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="examDistributionPrefs" id="examDistributionPrefsForm"> 
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteDistributionPreference"/></tt:confirm>
	<INPUT type="hidden" name="deleteType" id="deleteType" value="">
	<INPUT type="hidden" name="deleteId" id="deleteId" value="">
	<INPUT type="hidden" name="reloadCause" id="reloadCause" value="">
	<INPUT type="hidden" name="reloadId" id="reloadId" value="">
	<s:if test="form.distPrefId != null"><s:hidden name="form.distPrefId"/></s:if>
	<table class="unitime-MainTable">
	<s:if test="#request.distPrefs == null">
		<TR>
			<TD valign="middle" colspan='3'>
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.distPrefId != null">
							<loc:message name="sectionEditDistributionPreference"/>
						</s:if>
						<s:else>
							<loc:message name="sectionAddDistributionPreference"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.distPrefId != null">
						<s:submit name='op' value="%{#msg.actionUpdateDistributionPreference()}"
							title="%{#msg.titleUpdateDistributionPreference(#msg.accessUpdateDistributionPreference())}"
							accesskey="%{#msg.accessUpdateDistributionPreference()}"/>
						<sec:authorize access="hasPermission(#form.distPrefId, 'DistributionPref', 'ExaminationDistributionPreferenceDelete')">
							<s:submit name='op' value="%{#msg.actionDeleteDistributionPreference()}"
								title="%{#msg.titleDeleteDistributionPreference(#msg.accessDeleteDistributionPreference())}"
								accesskey="%{#msg.accessDeleteDistributionPreference()}"
								onclick="doDel('distPref', '-1'); return confirmDelete();"
								/>
						</sec:authorize>	
					</s:if>
					<s:else>
						<s:submit name='op' value="%{#msg.actionSaveNewDistributionPreference()}"
							title="%{#msg.titleSaveNewDistributionPreference(#msg.accessSaveNewDistributionPreference())}"
							accesskey="%{#msg.accessSaveNewDistributionPreference()}"/>
					</s:else>
					
					<s:if test="form.distPrefId != null">
						<tt:back styleClass="btn"
							name="${MSG.actionBackDistributionPreference()}" 
							title="${MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference())}"
							accesskey="${MSG.accessBackDistributionPreference()}" 
							back="1" 
							type="PreferenceGroup">
							<s:property value="form.distPrefId"/>
						</tt:back>
					</s:if>
					<s:else>
						<tt:back styleClass="btn" 
							name="${MSG.actionBackDistributionPreference()}" 
							title="${MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference())}"
							accesskey="${MSG.accessBackDistributionPreference()}" 
							back="1"/>
					</s:else>
				</tt:section-header>
			</TD>
		</TR>
				
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="3" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
		
		<TR>
			<TD nowrap valign='top'><loc:message name="propExamType"/><font class="reqField">*</font></TD>
			<TD colspan='2' width='100%'>
				<s:if test="form.distPrefId != null">
					<s:hidden name="form.examType" id="examType"/>
					<s:iterator value="#request.examTypesAdd" var="type">
						<s:if test="#type.uniqueId == form.examType">
							<s:property value="#type.label"/>
						</s:if>
					</s:iterator>
				</s:if>
				<s:else>
					<s:select name="form.examType" id="examType"
						list="#request.examTypesAdd" listKey="uniqueId" listValue="label"
						onchange="doDel('examType', this.value); document.getElementById('examDistributionPrefsForm').submit();"
						/>
				</s:else>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap valign='top'><loc:message name="propertyDistributionType"/><font class="reqField">*</font></TD>
			<TD colspan='2' width='100%'>
				<s:select name="form.distType"
					list="#request.distributionTypeList" listKey="uniqueId" listValue="label"
					headerKey="-" headerValue="-"
					style="width:300px;"
					onchange="distTypeChanged(this.value);"
					/>
				<span id='distTypeDesc' style='display:block;padding:3px;max-width:800px;'>
					<s:property value="form.description" escapeHtml="false"/>
				</span>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyDistributionPreference"/><font class="reqField">*</font></TD>
			<TD>
				<s:select name="form.prefLevel" id="prefLevel"
					list="#request.prefLevelsList" listKey="uniqueId" listValue="prefName" listCssStyle="dropdownOptionStyle"
					headerKey="-" headerValue="-"
					style="width:200px;">
				</s:select>
			</TD>
			<TD>&nbsp;</TD>
		</TR>

		<TR><TD colspan='3'>&nbsp;</TD></TR>
		<TR>
			<TD valign="middle" colspan='3'>
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectionExaminationsInDistribution">
							<s:iterator value="#request.examTypes" var="et">
								<s:if test="#et.uniqueId == form.examType">
									<s:property value="#et.label"/>
								</s:if>
							</s:iterator>
						</loc:message>
					</tt:section-title>
					<s:submit name='op' value="%{#msg.actionAddExamToDistribution()}"
						title="%{#msg.titleAddClassToDistribution(#msg.accessAddExamToDistribution())}"
						accesskey="%{#msg.accessAddExamToDistribution()}"
						style="min-width:100px;"/>
				</tt:section-header>
			</TD>
		</TR>

		<s:iterator value="form.subjectArea" var="sa" status="stat">
		<TR>
			<TD colspan="3">
			
				<!-- Class / Subpart -->
				<s:select name="form.subjectArea[%{#stat.index}]"
					list="form.filterSubjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"
					headerKey="-1" headerValue="-"
					style="width:90px;"
					onchange="doReload('subjectArea', '%{#stat.index}');"
					id="subjectArea%{#stat.index}"
					/>

				<s:select name="form.courseNbr[%{#stat.index}]"
					list="form.courseNbrs[#stat.index]" listKey="id" listValue="value"
					style="width:290px;"
					onchange="doReload('exam', '%{#stat.index}');"
					id="courseNbr%{#stat.index}"
					/>
					
				<s:select name="form.exam[%{#stat.index}]"
					list="form.exams[#stat.index]" listKey="id" listValue="value"
					style="width:300px;"
					id="exam%{#stat.index}"
					/>

				<!-- Arrows -->
				<s:if test="#stat.index > 0">
					<IMG border="0" src="images/arrow_up.png" alt="${MSG.titleMoveUp()}" title="${MSG.titleMoveUp()}" align='middle'
						onMouseOver="this.style.cursor='hand';this.style.cursor='pointer';"
						onClick="doReload('moveUp', '${stat.index}');">
				</s:if>
				<s:if test="#stat.index == 0">
						<IMG border="0" src="images/blank.png" align='middle'>
				</s:if>
				<s:if test="#stat.index < #request.listSize">
					<IMG border="0" src="images/arrow_down.png" alt="${MSG.titleMoveDown()}" title="${MSG.titleMoveDown()}" align='middle'
						onMouseOver="this.style.cursor='hand';this.style.cursor='pointer';"
						onClick="javascript: doReload('moveDown', '${stat.index}');">
				</s:if>
				<s:if test="#stat.index == #request.listSize">
					<IMG border="0" src="images/blank.png" align='middle'>
				</s:if>

				<!-- Delete button -->
				&nbsp;&nbsp;
				<s:submit name='op' value="%{#msg.actionDelete()}" onclick="doDel('distObject', '%{#stat.index}');"/>
			</TD>
		</TR>
		</s:iterator>
		
		<TR>
			<TD colspan="3">
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan="3" align="right">
					<s:if test="form.distPrefId != null">
						<s:submit name='op' value="%{#msg.actionUpdateDistributionPreference()}"
							title="%{#msg.titleUpdateDistributionPreference(#msg.accessUpdateDistributionPreference())}"
							accesskey="%{#msg.accessUpdateDistributionPreference()}"/>
						<sec:authorize access="hasPermission(#form.distPrefId, 'DistributionPref', 'ExaminationDistributionPreferenceDelete')">
							<s:submit name='op' value="%{#msg.actionDeleteDistributionPreference()}"
								title="%{#msg.titleDeleteDistributionPreference(#msg.accessDeleteDistributionPreference())}"
								accesskey="%{#msg.accessDeleteDistributionPreference()}"
								onclick="doDel('distPref', '-1'); return confirmDelete();"
								/>
						</sec:authorize>	
					</s:if>
					<s:else>
						<s:submit name='op' value="%{#msg.actionSaveNewDistributionPreference()}"
							title="%{#msg.titleSaveNewDistributionPreference(#msg.accessSaveNewDistributionPreference())}"
							accesskey="%{#msg.accessSaveNewDistributionPreference()}"/>
					</s:else>
					
					<s:if test="form.distPrefId != null">
						<tt:back styleClass="btn"
							name="${MSG.actionBackDistributionPreference()}" 
							title="${MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference())}"
							accesskey="${MSG.accessBackDistributionPreference()}" 
							back="1" 
							type="PreferenceGroup">
							<s:property value="form.distPrefId"/>
						</tt:back>
					</s:if>
					<s:else>
						<tt:back styleClass="btn" 
							name="${MSG.actionBackDistributionPreference()}" 
							title="${MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference())}"
							accesskey="${MSG.accessBackDistributionPreference()}" 
							back="1"/>
					</s:else>
			</TD>
		</TR>
		</s:if>
		<s:else>
			<TR>
				<TD colspan="2">
					<B><loc:message name="propExamType"/> </B>
					<s:select name="form.examType"
						list="#request.examTypes" listKey="uniqueId" listValue="label"/>
					<B><loc:message name="propExamSubject"/> </B>
					<s:select name="form.filterSubjectAreaId" id="subjectId"
						list="form.subjectAreas" listKey="id" listValue="value"
						/>
					<B><loc:message name="propExamCourseNumber"/> </B>
					<tt:course-number name="form.filterCourseNbr" configuration="subjectId=\${subjectId};notOffered=exclude"
						title="%{#msg.tooltipCourseNumber()}" size="15"/>
					&nbsp;&nbsp;&nbsp;
					<s:submit name='op' value="%{#msg.actionSearchDistributionPreferences()}"
						title="%{#msg.titleSearchDistributionPreferences(#msg.accessSearchDistributionPreferences())}"
						accesskey="%{#msg.accessSearchDistributionPreferences()}"/>
					<s:submit name='op' value="%{#msg.actionExportPdf()}"
						title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
						accesskey="%{#msg.accessExportPdf()}"/>
					<s:submit name='op' value="%{#msg.actionExportCsv()}"
						title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"
						accesskey="%{#msg.accessExportCsv()}"/>
				</TD>
			</TR>		
		
			<TR>
				<TD colspan="2">
					&nbsp;
				</TD>
			</TR>		
		
		<s:if test="#request.distPrefs != null && !#request.distPrefs.isEmpty()">
			<TR>
				<TD colspan="2">
					<tt:section-header>
						<tt:section-title>
							<loc:message name="sectionDistributionPreferences">
								<s:iterator value="#request.examTypes" var="et">
									<s:if test="#et.uniqueId == form.examType">
										<s:property value="#et.label"/>
									</s:if>
								</s:iterator>
							</loc:message>
						</tt:section-title>
						<sec:authorize access="hasPermission(null, 'Session', 'ExaminationDistributionPreferenceAdd')">
							<s:submit name='op' value="%{#msg.actionAddDistributionPreference()}"
								title="%{#msg.titleAddDistributionPreference(#msg.accessAddDistributionPreference())}"
								accesskey="%{#msg.accessAddDistributionPreference()}"/>
						</sec:authorize>
					</tt:section-header>
				</TD>
			</TR>		

			<TR>
				<TD colspan="2">
					<script type="text/javascript">displayLoading();</script>
					<TABLE style="margin:0; width: 100%;">
						<s:property value="#request.distPrefs" escapeHtml="false"/>
					</TABLE>
					<script type="text/javascript">hideLoading();</script>
				</TD>
			</TR>
		</s:if>
			<TR>
				<TD colspan="2" align="right">
					<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
				</TD>
			</TR>
			<TR>
				<TD colspan="2" align="right">
					<sec:authorize access="hasPermission(null, 'Session', 'ExaminationDistributionPreferenceAdd')">
						<s:submit name='op' value="%{#msg.actionAddDistributionPreference()}"
							title="%{#msg.titleAddDistributionPreference(#msg.accessAddDistributionPreference())}"
							accesskey="%{#msg.accessAddDistributionPreference()}"/>
					</sec:authorize>
				</TD>
			</TR>
		</s:else>
	</TABLE>
</s:form>
</loc:bundle>

<SCRIPT type="text/javascript">
	var reload = false;

	function doDel(type, id) {
		var delType = document.getElementById('deleteType');
		delType.value = type;

		var delId = document.getElementById('deleteId');
		delId.value = id;
	}
	
	function doReload(type, id) {	
		if (type=='subjectArea' || type=='exam') {
			doAjax(type,id);
			return;
		}
		var reloadId = document.getElementById('reloadId');
		reloadId.value = id;

		var reloadCause = document.getElementById('reloadCause');
		reloadCause.value = type;
		
		document.getElementById('examDistributionPrefsForm').submit();
	}

	function doAjax(type, idx) {
		var subjAreaObj = document.getElementById('subjectArea'+idx);
		var courseNbrObj = document.getElementById('courseNbr'+idx);
		var examObj = document.getElementById('exam'+idx);
		var examType = document.getElementById('examType').value;

		var id = null;
		var options = null;
		var next = null;
		
		if (type=='subjectArea') {
			id = subjAreaObj.value;
			options = courseNbrObj.options;
			next = 'exam';
			courseNbrObj.options.length=1;
			examObj.options.length=1;
			examObj.options[0]=new Option('-', '-1', false);
		} else if (type=='exam') {
			id = courseNbrObj.value;
			options = examObj.options;
			examObj.options.length=0;
		}
		if (id=='-') return;
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			options.length=1;
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						// Course numbers options creation
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							while (optVal.indexOf('@amp@')>=0) optVal = optVal.replace('@amp@','&');
							options[(type=='exam'?i:i+1)]=new Option(optVal, optId, false);
						}
					}
				}
			}
			if (options.length==2) {
				options.selectedIndex = 1;
				if (next!=null) doReload(next,idx);
			}
		};
	
		// Request
		var vars = "id="+id+"&examType="+examType+"&type="+type;
		req.open( "POST", "ajax/distributionPrefsAjax.action", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
	
	function distTypeChanged(id) {
		var descObj = document.getElementById('distTypeDesc');
		var prefLevObj = document.getElementById('prefLevel');
		var options = prefLevObj.options;
		var prefId = prefLevObj.options[prefLevObj.selectedIndex].value;
		
		if (id=='-') {
			descObj.innerHTML='';
			options.length=1;
			return;
		}
		
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
						if (count>0) {
							var desc = xmlDoc.documentElement.childNodes[0].getAttribute("value");
							while (desc.indexOf('@lt@')>=0) desc = desc.replace('@lt@','<');
							while (desc.indexOf('@gt@')>=0) desc = desc.replace('@gt@','>');
							while (desc.indexOf('@quot@')>=0) desc = desc.replace('@quot@','"');
							while (desc.indexOf('@amp@')>=0) desc = desc.replace('@amp@','&');
							descObj.innerHTML=desc;
						}
						for(i=1; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							var optExt = xmlDoc.documentElement.childNodes[i].getAttribute("extra");
							options[i] = new Option(optVal, optId, (prefId==optId));
							options[i].style.backgroundColor=optExt;
						}
					}
				}
			}
		};
	
		// Request
		var vars = "id="+id+"&type=distType";
		req.open( "POST", "ajax/distributionPrefsAjax.action", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}	

</SCRIPT>
				
		
