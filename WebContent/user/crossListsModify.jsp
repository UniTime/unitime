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
<s:form action="crossListsModify">
<SCRIPT type="text/javascript">
	<!--
	var ioLimit = ${form.ioLimit};
	var mismatchHtml = 
		" &nbsp;&nbsp; " +
		"<img src='images/cancel.png' alt='<%=MSG.altCrossListsLimitsDoNotMatch()%>' title='<%=MSG.titleCrossListsLimitsDoNotMatch() %>' border='0' align='top'> &nbsp;" +
		"<font color='#FF0000'><%= MSG.errorCrossListsLimitsDoNotMatch()%></font>";
	
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g,"");
	};
	String.prototype.ltrim = function() {
		return this.replace(/^\s+/,"");
	};
	String.prototype.rtrim = function() {
		return this.replace(/\s+$/,"");
	};
		
	function updateResvTotal() {
		i=0;
		total = 0;
		blanksExist = false;
		allBlank = true;
		while ( (o = document.getElementById("reserved_" + i++)) !=null ) {
			val = o.value = o.value.trim();
			if (val=="") {
				val = 0;
				blanksExist = true;
			} else  if (val == parseInt(val) && parseInt(val)>=0) {
				total += parseInt(val);
				allBlank = false;
			} else {
				document.getElementById("resvTotal").innerHTML = "<font color='red'><b>?</b></font>";
				return;
			}
		}
		
		str = "<b>" + total + "</b>&nbsp;&nbsp;";
		if (ioLimit >= 0) {
			if (total < ioLimit) {
				str = "<font color='red'><b>" + total + "</b></font>&nbsp;&nbsp;";
			} else {
				str = "<font color='green'><b>" + total + "</b></font>&nbsp;&nbsp;";
			}
		}
		if (allBlank) str = "";
		
		if (document.getElementById('resvTotal'))
			document.getElementById('resvTotal').innerHTML = str;
		
		if (!allBlank && total<ioLimit) 
			document.getElementById("resvTotalDiff").innerHTML = mismatchHtml;
		else 
			document.getElementById("resvTotalDiff").innerHTML = "";
	}
	
	// -->
</SCRIPT>
	<s:set var="resvTotal" value="0"/>
	<s:set var="projTotal" value="0"/>
	<s:set var="lastTermTotal" value="0"/>
	<s:set var="resvExists" value="false"/>
	<s:set var="projSpace" value="0"/>
	<s:set var="lastTermSpace" value="0"/>
	<s:hidden name="form.instrOfferingId"/>
	<s:hidden name="form.instrOfferingName"/>
	<s:hidden name="form.readOnlyCrsOfferingId"/>
	<s:if test="form.originalOfferings != null && !form.originalOfferings.isEmpty()">
		<s:iterator value="form.originalOfferings" var="org" status="stat">
			<s:hidden name="form.originalOfferings[%{#stat.index}]"/>
		</s:iterator>
	</s:if>
	<s:hidden name="form.ownedInstrOffr"/>
	<s:hidden name="form.ioLimit"/>
	<s:hidden name="form.unlimited"/>
	<s:hidden name="hdnOp" value = "" id="hdnOp"/>
	<s:hidden name="deletedCourseOfferingId" value="" id="deletedCourseOfferingId"/>
	<TABLE class="unitime-MainTable">
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHead">
					<A  title="${MSG.titleBackToIOList(MSG.accessBackToIOList())}"
						accesskey="I"
						class="l8"
						href="instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1&subjectAreaIds=${form.subjectAreaId}&courseNbr=${crsNbr}#A${form.instrOfferingId}"
					><s:property value="form.instrOfferingName" /></A>
				</DIV>
			</TD>
		</TR>
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
		<TR>
			<TD><loc:message name="propertyIOLimit"/></TD>
			<TD align="left">
				<s:if test="form.unlimited == true">
					<span title="Unlimited Enrollment"><font size="+1">&infin;</font></span>
				</s:if>
				<s:else>
					<TABLE class='unitime-Table'>
					<TR><TD align="left">
						<s:property value="form.ioLimit"/>
					</TD>
					<TD align="left">
						<DIV id='resvTotalDiff'></DIV>
					</TD></TR>
					</TABLE>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD valign="top" rowspan="2"><loc:message name="propertyCourseOfferings"/> </TD>
			<TD>
				<table class='unitime-Table'><tr><td>
				<s:select name="form.addCourseOfferingId"
					list="#request.crsOfferingList" listKey="uniqueId" listValue="courseNameWithTitle"
					headerKey="" headerValue="%{#msg.itemSelect()}" />
				</td><td style="padding-left: 5px;">
				<s:submit accesskey='%{#msg.accessAddCourseToCrossList()}' name='op' value='%{#msg.actionAddCourseToCrossList()}'
							title='%{#msg.titleAddCourseToCrossList(#msg.accessAddCourseToCrossList())}'/>
				</td></tr><tr><td class="unitime-Hint" colspan="2">
				<loc:message name="hintCrossLists"/>
				</td></tr></table>
			</TD>
		</TR>
		<TR>
			<TD align="left">
				<TABLE class="unitime-Table" style="width:100%;">
					<TR>
						<TD align="left" class="WebTableHeader"><loc:message name="columnCrossListsOffering"/> </TD>
						<TD align="center" class="WebTableHeader"> <loc:message name="columnCrossListsControlling"/> </TD>
						<TD align="center" class="WebTableHeader"> <loc:message name="columnCrossListsReserved"/> </TD>
						<TD align="right" class="WebTableHeader"><!-- I> Requested </I --></TD>
						<TD align="right" class="WebTableHeader"> <loc:message name="columnCrossListsProjected"/></TD>
						<TD align="right" class="WebTableHeader"> <loc:message name="columnCrossListsLastTerm"/></TD>
						<TD class="WebTableHeader">&nbsp;</TD>
					</TR>
					
					<s:iterator value="form.courseOfferingIds" var="co" status="stat"><s:set var="ctr" value="%{#stat.index}"/>
						<s:set var="style" value="'BottomBorderGray'"/>
						<s:if test="#stat.last"><s:set var="style" value="''"/></s:if>
					<TR>
						<TD class="${style}">
							<s:hidden name="form.courseOfferingIds[%{#ctr}]"/>
							<s:hidden name="form.courseOfferingNames[%{#ctr}]"/>
							<s:hidden name="form.ownedCourse[%{#ctr}]"/>
							<s:hidden name="form.canDelete[%{#ctr}]"/>
							<s:property value="form.courseOfferingNames[#ctr]"/> &nbsp;
						</TD>
						<TD align="center" class="${style}">
							&nbsp;
							<s:radio name="form.ctrlCrsOfferingId" list="#{#co:''}" disabled="%{form.ownedCourse[#ctr] == false}"/>
							&nbsp;
						</TD>
						<TD align="center" class="${style}">
							&nbsp;
							<s:hidden name="form.resvId[%{#ctr}]"/>
							<s:if test="form.courseOfferingIds.size() == 1 && modifyCrossListSingleCourseLimit == false">
								<s:property value="form.limits[#ctr]"/>
								<s:hidden name="form.limits[%{#ctr}]"/>
							</s:if><s:else>
								<s:if test="form.ownedInstrOffr == true">
									<s:textfield name="form.limits[%{#ctr}]" id='reserved_%{#ctr}' type="number" onchange="updateResvTotal();" style="text-align:right; width:75px;"/>
								</s:if><s:else>
									<s:property value="form.limits[#ctr]"/>
									<s:hidden name="form.limits[%{#ctr}]"/>
								</s:else>
							</s:else>
							<s:if test="form.limits[#ctr] != null">
								<s:set var="resvExists" value="true"/>
								<s:set var="resvTotal" value="#resvTotal + form.limits[#ctr]"/>
							</s:if>
						</TD>
						<TD align="right" class="${style}">
							<s:hidden name="form.requested[%{#ctr}]"/>
							<s:property value="form.requested[#ctr]"/>
						</TD>
						<TD align="right" class="${style}" style="padding-right:5px;">
							<s:hidden name="form.projected[%{#ctr}]"/>
							<s:if test="form.projected[#ctr] != null">
								<s:property value="form.projected[#ctr]"/>
								<s:set var="projSpace" value="#projSpace + form.projected[#ctr]"/>
							</s:if><s:else>
								-
							</s:else>
						</TD>
						<TD align="right" class="${style}" style="padding-right:5px;">
							<s:hidden name="form.lastTerm[%{#ctr}]"/>
							<s:if test="form.lastTerm[#ctr] != null">
								<s:property value="form.lastTerm[#ctr]"/>
								<s:set var="lastTermSpace" value="#lastTermSpace + form.lastTerm[#ctr]"/>
							</s:if><s:else>
								-
							</s:else>
						</TD>
						<TD align="center" nowrap class="${style}">
							<s:if test="(form.readOnlyCrsOfferingId == null || form.readOnlyCrsOfferingId != #co) && form.canDelete[#ctr]">
								<IMG border="0" src="images/action_delete.png" title="${MSG.titleRemoveCourseFromCrossList()}"
									onMouseOver="this.style.cursor='hand';this.style.cursor='pointer';"
									onClick="document.getElementById('hdnOp').value='delete';document.getElementById('deletedCourseOfferingId').value='${co}';document.forms[0].submit();">
							</s:if>
						</TD>
					</TR>
					</s:iterator>
					<s:if test="form.courseOfferingIds.size() > 1">
					<TR>
						<TD align="left" class='rowTotal'><I> <loc:message name="rowCrossListsTotal"/> </I></TD>
						<TD align="center" class='rowTotal'><I> &nbsp; </I></TD>
						<TD class='rowTotal' align='right' style="padding-right:24px;"><DIV id='resvTotal'><s:if test="#resvExists==true"><s:property value="#resvTotal"/></s:if></DIV></TD>
						<TD align="right" class='rowTotal'><!-- I> Requested </I --></TD>
						<TD class='rowTotal' align='right' style="padding-right:5px;"><s:property value="#projTotal"/></TD>
						<TD class='rowTotal' align='right' style="padding-right:5px;"><s:property value="#lastTermTotal"/></TD>
						<TD class='rowTotal'>&nbsp;</TD>
					</TR>
					</s:if>
				</TABLE>
			</TD>
		</TR>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<s:submit accesskey='%{#msg.accessUpdateCrossLists()}' name='op' value='%{#msg.actionUpdateCrossLists()}'
						title='%{#msg.titleUpdateCrossLists(#msg.accessUpdateCrossLists())}'/>
				<s:submit accesskey='%{#msg.accessBackToIODetail()}' name='op' value='%{#msg.actionBackToIODetail()}'
						title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
			</TD>
		</TR>

	</TABLE>
</s:form>

<SCRIPT type="text/javascript">
	<!--
	updateResvTotal();
	// -->
</SCRIPT>

</loc:bundle>