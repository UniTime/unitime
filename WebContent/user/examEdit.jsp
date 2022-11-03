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
<s:form action="examEdit">
	<s:hidden name="form.examId"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<s:hidden name="form.clone"/>
	<s:hidden name="op2" value=""/>

	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.label"/>
					</tt:section-title>
					<s:if test="form.examId != null">
						<s:submit accesskey='%{#msg.accessExamUpdate()}' name='op' value='%{#msg.actionExamUpdate()}' title='%{#msg.titleExamUpdate()}'/>
					</s:if>
					<s:else>
						<s:submit accesskey='%{#msg.accessExamSave()}' name='op' value='%{#msg.actionExamSave()}' title='%{#msg.titleExamSave()}'/>
					</s:else>
					<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationEditClearPreferences')"> 
						<s:submit accesskey='%{#msg.accessClearExamPreferences()}' name='op' value='%{#msg.actionClearExamPreferences()}' title='%{#msg.titleClearExamPreferences()}'/>
					</sec:authorize> 
					<s:if test="form.previousId > 0">
						<s:submit accesskey='%{#msg.accessExamPrevious()}' name='op' value='%{#msg.actionExamPrevious()}' title='%{#msg.titleExamPrevious()}'/>
					</s:if>
					<s:if test="form.nextId > 0">
						<s:submit accesskey='%{#msg.accessExamNext()}' name='op' value='%{#msg.actionExamNext()}' title='%{#msg.titleExamNext()}'/>
					</s:if>
					<s:submit accesskey='%{#msg.accessBatckToDetail()}' name='op' value='%{#msg.actionBatckToDetail()}' title='%{#msg.titleBatckToDetail()}'/>
				</tt:section-header>
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
		<TR>
			<TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD>
		</TR>
		</s:if>
		
		<TR>
			<TD><loc:message name="propExamName"/></TD><TD><s:textfield name='form.name' maxlength='100' size='50'/><s:fielderror fieldName="form.name"/></TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamType"/></TD><TD>
				<s:if test="form.examId == null">
					<s:select name="form.examType" list="#request.examTypes"
						listKey="uniqueId" listValue="label"
						onchange="javascript: doDel('examType', this.value); submit();"/>
					<s:fielderror fieldName="form.examType"/>
				</s:if>
				<s:else>
					<s:hidden name="form.examType"/>
					<s:iterator value="#request.examTypes" var="et">
						<s:if test="#et.uniqueId == form.examType">
							<s:property value="label"/>
						</s:if>
					</s:iterator>
					<s:fielderror fieldName="form.examType"/>
				</s:else>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamLength"/></TD><TD><s:textfield name='form.length' type="number"/><s:fielderror fieldName="form.length"/></TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamSeatingType"/></TD><TD>
				<s:select name="form.seatingType" list="form.seatingTypes"
					onchange="javascript: doDel('seatingType', -1); submit();"/>
				<s:fielderror fieldName="form.seatingType"/>
			</TD>
		</TR>
		<TR>
			<TD nowrap><loc:message name="propExamMaxRooms"/></TD><TD><s:textfield name='form.maxNbrRooms' maxlength='5' size='5' type="number"/><s:fielderror fieldName="form.maxNbrRooms"/></TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamSize"/></TD><TD>
				<s:textfield name='form.size' type="number"/>
				<i>(<s:property value="form.sizeNote"/>)</i>
				<s:fielderror fieldName="form.size"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamPrintOffset"/></TD><TD>
				<s:textfield name='form.printOffset' type="number"/>
				 <i><loc:message name="noteExamPrintOffset"/></i>
				<s:fielderror fieldName="form.printOffset"/>
			</TD>
		</TR>
		<TR>
			<TD valign="top"><loc:message name="sectExamNotes"/></TD>
			<TD>
				<s:textarea name="form.note" cols="80" rows="5"/>
				<s:fielderror fieldName="form.note"/>
			</TD>
		</TR>
		<TR>
			<TD valign="top"><loc:message name="propExamInstructors"/></TD>
			<TD>	
				<table>
					<s:iterator value="form.instructors" var="instructor" status="instructorStat">
						<tr><td nowrap>
							<s:select name="form.instructors[%{#instructorStat.index}]" list="#request.instructorsList"
								listKey="value" listValue="label" headerKey="-" headerValue="-"
							/>
							<s:submit name='op' value='%{#msg.actionDeleteInstructor()}' title='%{#msg.titleDeleteInstructor()}' onclick="javascript: doDel('instructor', %{#instructorStat.index});"/>
						</td></tr>
					</s:iterator>
					<tr><td>
						<s:submit accesskey='%{#msg.accessAddInstructor()}' name='op' value='%{#msg.actionAddInstructor()}' title='%{#msg.titleAddInstructor()}'/>
						<s:fielderror fieldName="form.instructors"/>
					</td></tr>
				</table> 			
		   	</TD>
	   	</TR>
		<s:if test="form.accommodation != null">
			<TR>
				<TD valign="top"><loc:message name="propExamStudentAccommodations"/></TD><TD>
					<s:property value="form.accommodation" escapeHtml="false"/>
					<s:hidden name="form.accommodation"/>
				</TD>
			</TR>
		</s:if>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-header>
					<tt:section-title><a id="objects"><loc:message name="sectExamOwners"/></a></tt:section-title>
					<s:submit accesskey='%{#msg.accessAddObject()}' name='op' value='%{#msg.actionAddObject()}' title='%{#msg.titleAddObject()}'/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<table>
				<tr>
					<td width='100'><i><loc:message name="colExamOwnerSubject"/></i></td>
					<td width='350'><i><loc:message name="colExamOwnerCourseNumber"/></i></td>
					<td width='160'><i><loc:message name="colExamOwnerConfigSubpart"/></i></td>
					<td width='150'><i><loc:message name="colExamOwnerClassNumber"/></i></td>
					<td></td>
				</tr>
				<s:iterator value="form.subjectAreaList" var="m" status="s">
					<tr><td>
						<s:select style="width:90px;" id="subjectArea%{#s.index}"
							name="form.subjectArea[%{#s.index}]"
							list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"
							headerKey="-1" headerValue="-"
							onchange="javascript: doAjax('subjectArea', %{#s.index});"
							/>
					</td><td>
						<s:select style="width:340px;" id="courseNbr%{#s.index}"
							name="form.courseNbr[%{#s.index}]"
							list="form.getCourseNbrs(#s.index)" listKey="id" listValue="value"
							onchange="javascript: doAjax('courseNbr', %{#s.index});"
							/>
					</td><td>
						<s:select style="width:150px;" id="itype%{#s.index}"
							name="form.itype[%{#s.index}]"
							list="form.getItypes(#s.index)" listKey="id" listValue="value"
							onchange="javascript: doAjax('itype', %{#s.index});"
							/>
					</td><td>
						<s:select style="width:140px;" id="classNumber%{#s.index}"
							name="form.classNumber[%{#s.index}]"
							list="form.getClassNumbers(#s.index)" listKey="id" listValue="value"
							/>
					</td><td>
						<s:submit name='op' value='%{#msg.actionDeleteObject()}' title='%{#msg.titleDeleteObject()}' onclick="javascript: doDel('objects', %{#s.index});"/>
					</td></tr>
				</s:iterator>
				</table>
			</TD>
		</TR>

		<s:include value="preferencesEdit2.jspf">
			<s:param name="frmName" value="'examEdit'"/>
			<s:param name="distPref" value="false"/>
			<s:param name="timePref" value="false"/>
			<s:param name="datePatternPref" value="false"/>
			<s:param name="examSeating" value="form.isExamSeating()"/>
		</s:include>

<!-- buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
					<s:set var="msg" value="#attr.MSG"/>
					<s:if test="form.examId != null">
						<s:submit accesskey='%{#msg.accessExamUpdate()}' name='op' value='%{#msg.actionExamUpdate()}' title='%{#msg.titleExamUpdate()}'/>
					</s:if>
					<s:else>
						<s:submit accesskey='%{#msg.accessExamSave()}' name='op' value='%{#msg.actionExamSave()}' title='%{#msg.titleExamSave()}'/>
					</s:else>
					<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationEditClearPreferences')"> 
						<s:submit accesskey='%{#msg.accessClearExamPreferences()}' name='op' value='%{#msg.actionClearExamPreferences()}' title='%{#msg.titleClearExamPreferences()}'/>
					</sec:authorize>
					<s:if test="form.previousId > 0">
						<s:submit accesskey='%{#msg.accessExamPrevious()}' name='op' value='%{#msg.actionExamPrevious()}' title='%{#msg.titleExamPrevious()}'/>
					</s:if>
					<s:if test="form.nextId > 0">
						<s:submit accesskey='%{#msg.accessExamNext()}' name='op' value='%{#msg.actionExamNext()}' title='%{#msg.titleExamNext()}'/>
					</s:if>
					<s:submit accesskey='%{#msg.accessBatckToDetail()}' name='op' value='%{#msg.actionBatckToDetail()}' title='%{#msg.titleBatckToDetail()}'/>
			</TD>
		</TR>	
	</TABLE>
	
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
</s:form>
<SCRIPT type="text/javascript">
	function doAjax(type, idx) {
		var subjAreaObj = document.getElementById('subjectArea'+idx);
		var courseNbrObj = document.getElementById('courseNbr'+idx);
		var itypeObj = document.getElementById('itype'+idx);
		var classNumberObj = document.getElementById('classNumber'+idx);

		var id = null;
		var options = null;
		var next = null;
		var extra = "";
		
		if (type=='subjectArea') {
			id = subjAreaObj.value;
			options = courseNbrObj.options;
			next = 'courseNbr';
			courseNbrObj.options.length=1;
			itypeObj.options.length=1;
			itypeObj.options[0]=new Option('${MSG.examOwnerNotApplicable()}', '-1', false);
			classNumberObj.options.length=1;
			classNumberObj.options[0]=new Option('${MSG.examOwnerNotApplicable()}', '-1', false);
			if (id==0) return;
		} else if (type=='courseNbr') {
			id = courseNbrObj.value;
			options = itypeObj.options;
			next = 'itype';
			itypeObj.options.length=1;
			classNumberObj.options.length=1;
			classNumberObj.options.length=1;
			classNumberObj.options[0]=new Option('${MSG.examOwnerNotApplicable()}', '-1', false);
			if (id==-1 || id<=<%=Long.MIN_VALUE%>+2) return;
		} else if (type=='itype') {
			id = itypeObj.value;
			options = classNumberObj.options;
			classNumberObj.options.length=1;
			if (id==-1) return;
			extra = "&courseId=" + courseNbrObj.value;
		}
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			options.length=0;
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
							while (optVal.indexOf('_')>=0 && next=='itype')
								optVal = optVal.replace("_",String.fromCharCode(160,160,160,160));
							options[i]=new Option(optVal, optId, false);
						}
					}
					if (options.length==1) {
						options[0].selected=true;
						if (next!=null) doAjax(next,idx);
					}
				}
			}
		};
	
		// Request
		var vars = "id="+id+"&type="+type+extra;
		req.open( "POST", "ajax/examEditAjax.action", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
</SCRIPT>
</loc:bundle>