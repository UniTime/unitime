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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.form.DistributionPrefsForm" %>
<%@ page import="org.unitime.timetable.model.DistributionPref" %>
<%@ page import="org.unitime.timetable.model.DistributionType" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />
<%
	String focusElement = "distType";
	if (request.getAttribute("addedClass")!=null) {
		focusElement = "subjectArea[" + request.getAttribute("addedClass").toString() + "]";
	}
	if (request.getAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR)!=null)
		focusElement = null;
%> 
<html:form action="/distributionPrefs" focus="<%= focusElement %>">
<loc:bundle name="CourseMessages"><tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('<%=MSG.confirmDeleteDistributionPreference()%>')) {
				return false;
			}

			return true;
		}
	// -->
</SCRIPT>  	
	<INPUT type="hidden" name="deleteType" id="deleteType" value="">
	<INPUT type="hidden" name="deleteId" id="deleteId" value="">
	<INPUT type="hidden" name="reloadCause" id="reloadCause" value="">
	<INPUT type="hidden" name="reloadId" id="reloadId" value="">
	<INPUT type="hidden" name="op2" value="">
	<html:hidden property="distPrefId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<% if (request.getAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR)==null) { %>	
		
		<TR>
			<TD valign="middle" colspan='3'>
				<tt:section-header>
					<tt:section-title>
						<logic:notEmpty name="distributionPrefsForm" property="distPrefId">
							<loc:message name="sectionTitleEditDistributionPreference"/>
						</logic:notEmpty>
						<logic:empty name="distributionPrefsForm" property="distPrefId">
							<loc:message name="sectionTitleAddDistributionPreference"/>
						</logic:empty>
					</tt:section-title>
					<logic:notEmpty name="distributionPrefsForm" property="distPrefId">
						<html:submit 
							styleClass="btn" 
							property="op" 
							accesskey="<%=MSG.accessUpdateDistributionPreference() %>" 
							title="<%=MSG.titleUpdateDistributionPreference(MSG.accessUpdateDistributionPreference()) %>">
							<loc:message name="actionUpdateDistributionPreference" />
						</html:submit>
						
						<sec:authorize access="hasPermission(#distributionPrefsForm.distPrefId, 'DistributionPref', 'DistributionPreferenceDelete')">
							&nbsp;
							<html:submit styleClass="btn" property="op" 
								accesskey="<%=MSG.accessDeleteDistributionPreference() %>" 
								title="<%=MSG.titleDeleteDistributionPreference(MSG.accessDeleteDistributionPreference()) %>" 
								onclick="javascript: doDel('distPref', '-1'); return confirmDelete();">
								<loc:message name="actionDeleteDistributionPreference" />
							</html:submit>
						</sec:authorize>				
					</logic:notEmpty>
				
					<logic:empty name="distributionPrefsForm" property="distPrefId">

					<html:submit styleClass="btn" property="op" 
						accesskey="<%=MSG.accessSaveNewDistributionPreference() %>" 
						title="<%=MSG.titleSaveNewDistributionPreference(MSG.accessSaveNewDistributionPreference()) %>">
						<loc:message name="actionSaveNewDistributionPreference" />
					</html:submit>
				
					</logic:empty>

					<!-- 
					<html:submit property="op" accesskey="C">
						<bean:message key="button.cancel" />
					</html:submit>
					-->

					&nbsp;
					<logic:notEmpty name="distributionPrefsForm" property="distPrefId">
						<tt:back styleClass="btn" 
							name="<%=MSG.actionBackDistributionPreference()%>" 
							title="<%=MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference()) %>"
							accesskey="<%=MSG.accessBackDistributionPreference() %>" 
							back="1" 
							type="PreferenceGroup">
							<bean:write name="distributionPrefsForm" property="distPrefId"/>
						</tt:back>
					</logic:notEmpty>
					<logic:empty name="distributionPrefsForm" property="distPrefId">
						<tt:back styleClass="btn" 
							name="<%=MSG.actionBackDistributionPreference()%>" 
							title="<%=MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference()) %>"
							accesskey="<%=MSG.accessBackDistributionPreference() %>" 
							back="1"/>
					</logic:empty>
					
				</tt:section-header>
			</TD>
		</TR>
				
		<logic:messagesPresent>
		<TR>
			<TD colspan="3" align="left" class="errorCell">
					<B><U><loc:message name="errors"/></U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		
		<TR>
			<TD nowrap valign='top'><loc:message name="propertyDistributionType"></loc:message><font class="reqField">*</font></TD>
			<TD colspan='2' width='100%'>
				<html:select style="width:300px;" property="distType" onchange="javascript: distTypeChanged(this.value);"> <!-- op2.value='DistTypeChange';submit(); -->
					<html:option value="-">-</html:option>
					<html:options collection="<%=DistributionType.DIST_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label" />
				</html:select>
				<span id='distTypeDesc' style='display:block;padding:3px; max-width: 800px;'>
					<bean:write name="distributionPrefsForm" property="description" filter="false"/>
				</span>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap valign='top'><loc:message name="propertyDistributionStructure"/><font class="reqField">*</font></TD>
			<TD colspan='2'>
				<html:select property="grouping" onchange="javascript: groupingChanged(this.value);" > <!-- onchange="op2.value='GroupingChange';submit();" -->
					<html:option value="-">-</html:option>
					<html:options name="distributionPrefsForm" property="groupings"/>
				</html:select>
				<span id='groupingDesc' style='display:block;padding:3px; max-width: 800px;'>
					<bean:write name="distributionPrefsForm" property="groupingDescription" filter="false"/>
				</span>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyDistributionPreference"/> <font class="reqField">*</font></TD>
			<TD>
				<html:select style="width:200px;" property="prefLevel">					
					<html:option value="-">-</html:option>
					<logic:iterate scope="request" name="<%=PreferenceLevel.PREF_LEVEL_ATTR_NAME%>" id="prLevel">
					<% PreferenceLevel pr = (PreferenceLevel)prLevel; %>			
					<html:option
						style='<%="background-color:" + pr.prefcolor() + ";"%>'
						value="<%=pr.getUniqueId().toString()%>"> <%=pr.getPrefName() %>
					</html:option>
				   	</logic:iterate>
				</html:select>
			</TD>
			<TD>&nbsp;</TD>
		</TR>

		<TR><TD colspan='3'>&nbsp;</TD></TR>
		<TR>
			<TD valign="middle" colspan='3'>
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectionTitleClassesInDistribution"/>
					</tt:section-title>
					<html:submit styleClass="btn" property="op" 
						accesskey="<%=MSG.accessAddClassToDistribution() %>" 
						title="<%=MSG.titleAddClassToDistribution(MSG.accessAddClassToDistribution()) %>" 
						style="width: 100px">
						<loc:message name="actionAddClassToDistribution" />
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>

		<logic:iterate name="distributionPrefsForm" property="subjectArea" id="sa" indexId="ctr">
		<TR>
			<TD colspan="3">
			
				<!-- Class / Subpart -->
				<html:select style="width:90px;" 
					property='<%= "subjectArea[" + ctr + "]" %>' 
					onchange="<%= \"javascript: doReload('subjectArea', '\" + ctr + \"');\" %>"
					styleId='<%="subjectArea"+ctr%>' >
					<html:option value="-">-</html:option>
					<html:options collection="<%=DistributionPrefsForm.SUBJ_AREA_ATTR_LIST+ctr%>" property="value" labelProperty="label" />
				</html:select>

				<html:select style="width:470px;" 
					property='<%= "courseNbr[" + ctr + "]" %>' 
					onchange="<%= \"javascript: doReload('courseNbr', '\" + ctr + \"');\" %>"
					styleId='<%="courseNbr"+ctr%>' >
					<html:option value="-">-</html:option>
					<html:options collection="<%=DistributionPrefsForm.CRS_NUM_ATTR_LIST+ctr%>" property="value" labelProperty="label" />
				</html:select>

				<html:select style="width:150px;" 
					property='<%= "itype[" + ctr + "]" %>' 
					onchange="<%= \"javascript: doReload('itype', '\" + ctr + \"');\" %>"
					styleId='<%="itype"+ctr%>' >
					<html:option value="-">-</html:option>
					<html:options collection="<%=DistributionPrefsForm.ITYPE_ATTR_LIST+ctr%>" property="value" labelProperty="label" filter="false"/>
				</html:select>
				
				<tt:propertyEquals name="unitime.distributions.showClassSuffixes" value="true">
					<html:select style="width:150px;" property='<%= "classNumber[" + ctr + "]" %>' styleId='<%="classNumber"+ctr%>'>
						<html:option value="-">-</html:option>
						<html:option value="-1"><loc:message name="dropDistrPrefAll"/></html:option>
						<html:options collection="<%=DistributionPrefsForm.CLASS_NUM_ATTR_LIST+ctr%>" property="value" labelProperty="label" />
					</html:select>
				</tt:propertyEquals>
				<tt:propertyNotEquals name="unitime.distributions.showClassSuffixes" value="true">
					<html:select style="width:80px;" property='<%= "classNumber[" + ctr + "]" %>' styleId='<%="classNumber"+ctr%>'>
						<html:option value="-">-</html:option>
						<html:option value="-1"><loc:message name="dropDistrPrefAll"/></html:option>
						<html:options collection="<%=DistributionPrefsForm.CLASS_NUM_ATTR_LIST+ctr%>" property="value" labelProperty="label" />
					</html:select>
				</tt:propertyNotEquals>
				
				<!-- Arrows -->
				<logic:greaterThan name="ctr" value="0">
					<IMG border="0" src="images/arrow_up.png" alt="<%=MSG.titleMoveUp()%>" title="<%=MSG.titleMoveUp()%>" align='absmiddle'
						onMouseOver="this.style.cursor='hand';this.style.cursor='pointer';"
						onClick="javascript: doReload('moveUp', '<%=ctr%>');">
				</logic:greaterThan>

				<logic:equal name="ctr" value="0">
					<IMG border="0" src="images/blank.png" align='absmiddle'>
				</logic:equal>

				<logic:lessThan name="ctr" value="<%=request.getAttribute(DistributionPrefsForm.LIST_SIZE_ATTR).toString()%>">
					<IMG border="0" src="images/arrow_down.png" alt="<%=MSG.titleMoveDown()%>" title="<%=MSG.titleMoveDown()%>" align='absmiddle'
						onMouseOver="this.style.cursor='hand';this.style.cursor='pointer';"
						onClick="javascript: doReload('moveDown', '<%=ctr%>');">
				</logic:lessThan>

				<logic:equal name="ctr" value="<%=request.getAttribute(DistributionPrefsForm.LIST_SIZE_ATTR).toString()%>">
					<IMG border="0" src="images/blank.png" align='absmiddle'>
				</logic:equal>

				<!-- Delete button -->
				&nbsp;&nbsp;				
				<html:submit styleClass="btn" property="op" 
					onclick="<%= \"javascript: doDel('distObject', '\" + ctr + \"');\" %>">
					<loc:message name="actionDelete" />
				</html:submit>
				<!--
				<IMG src="images/action_delete.png" border="0" align="middle">	
				-->
			</TD>
		</TR>
		</logic:iterate>
		
		<TR>
			<TD colspan="3">
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan="3" align="right">
				<logic:notEmpty name="distributionPrefsForm" property="distPrefId">
					<html:submit styleClass="btn" property="op" 
						accesskey="<%=MSG.accessUpdateDistributionPreference() %>" 
						title="<%=MSG.titleUpdateDistributionPreference(MSG.accessUpdateDistributionPreference()) %>">
						<loc:message name="actionUpdateDistributionPreference" />
					</html:submit>
					
					&nbsp;
					<html:submit styleClass="btn" property="op" 
						accesskey="<%=MSG.accessDeleteDistributionPreference() %>" 
						title="<%=MSG.titleDeleteDistributionPreference(MSG.accessDeleteDistributionPreference()) %>" 
						onclick="javascript: doDel('distPref', '-1'); return confirmDelete();">
						<loc:message name="actionDeleteDistributionPreference" />
					</html:submit>
				</logic:notEmpty>
				
				<logic:empty name="distributionPrefsForm" property="distPrefId">
					<html:submit styleClass="btn" property="op" 
						accesskey="<%=MSG.accessSaveNewDistributionPreference() %>" 
						title="<%=MSG.titleSaveNewDistributionPreference(MSG.accessSaveNewDistributionPreference()) %>">
						<loc:message name="actionSaveNewDistributionPreference" />
					</html:submit>
				</logic:empty>
				
				<!-- 
				<html:submit property="op" accesskey="C">
					<bean:message key="button.cancel" />
				</html:submit>
				-->
				
				&nbsp;
				<logic:notEmpty name="distributionPrefsForm" property="distPrefId">
					<tt:back styleClass="btn" 
						name="<%=MSG.actionBackDistributionPreference()%>" 
						title="<%=MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference()) %>"
						accesskey="<%=MSG.accessBackDistributionPreference() %>" 
						back="1" 
						type="PreferenceGroup">
						<bean:write name="distributionPrefsForm" property="distPrefId"/>
					</tt:back>
				</logic:notEmpty>
				<logic:empty name="distributionPrefsForm" property="distPrefId">
					<tt:back styleClass="btn" 
						name="<%=MSG.actionBackDistributionPreference()%>" 
						title="<%=MSG.titleBackDistributionPreference(MSG.accessBackDistributionPreference()) %>"
						accesskey="<%=MSG.accessBackDistributionPreference() %>" 
						back="1"/>
				</logic:empty>
			</TD>
		</TR>
			
		<!-- 
		<TR>
			<TD colspan="2" class="font8Gray">
				To edit/delete any preference from the list below, simply click on the line
				<br>data will be shown in the form above with the buttons to update/delete
			</TD>
		</TR>
		-->

		<% } else { %>
			<TR>
				<TD colspan="2">
					<B><loc:message name="filterSubject"/></B>
					<html:select name="distributionPrefsForm" property="filterSubjectAreaId" styleId="subjectId">
						<loc:bundle name="ConstantsMessages" id="CONST">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><loc:message name="select" id="CONST"/></html:option>
							<html:option value="<%=Constants.ALL_OPTION_VALUE%>"><loc:message name="all" id="CONST"/></html:option>
						</loc:bundle>
						<html:optionsCollection property="filterSubjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
					<B><loc:message name="filterCourseNumber"/></B>
					<tt:course-number property="filterCourseNbr" configuration="subjectId=\${subjectId};notOffered=exclude" size="10"/>
					&nbsp;&nbsp;&nbsp;
					<html:submit property="op" 
						onclick="displayLoading();"
						accesskey="<%=MSG.accessSearchDistributionPreferences() %>"
						styleClass="btn" 
						title="<%=MSG.titleSearchDistributionPreferences(MSG.accessSearchDistributionPreferences()) %>">
						<loc:message name="actionSearchDistributionPreferences" />
					</html:submit> 
					&nbsp;&nbsp;&nbsp;
					<html:submit property="op" 
						accesskey="<%=MSG.accessExportPdf() %>" 
						styleClass="btn" 
						title="<%=MSG.titleExportPdf(MSG.accessExportPdf()) %>">
						<loc:message name="actionExportPdf"/>
					</html:submit> 
				</TD>
			</TR>		
		
			<TR>
				<TD colspan="2">
					&nbsp;
				</TD>
			</TR>		
			
			<TR>
				<TD colspan="2">
					<script language="javascript">displayLoading();</script>
					<TABLE width="100%" cellspacing="0" cellpadding="0" border="0" style="margin:0;">
						<%=request.getAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR)%>
					</TABLE>
					<script language="javascript">hideLoading();</script>
				</TD>
			</TR>
			<TR>
				<TD colspan="2" align="right">
					<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
				</TD>
			</TR>
			<TR>
				<sec:authorize access="hasPermission(null, 'Department', 'DistributionPreferenceAdd')">
					<TD colspan="2" align="right">
						<html:submit property="op" styleClass="btn" 
							accesskey="<%=MSG.accessAddDistributionPreference() %>" 
							title="<%=MSG.titleAddDistributionPreference(MSG.accessAddDistributionPreference()) %>" >
							<loc:message name="actionAddDistributionPreference" />
						</html:submit>
					</TD>
				</sec:authorize>
			</TR>
		<% } %>

	</TABLE>
	</loc:bundle>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	var reload = false;

	function doDel(type, id) {
		var delType = document.distributionPrefsForm.deleteType;
		delType.value = type;

		var delId = document.distributionPrefsForm.deleteId;
		delId.value = id;
	}
	
	function doReload(type, id) {	
		if (type=='subjectArea' || type=='courseNbr' || type=='itype') {
			doAjax(type,id);
			return;
		}
		var reloadId = document.distributionPrefsForm.reloadId;
		reloadId.value = id;

		var reloadCause = document.distributionPrefsForm.reloadCause;
		reloadCause.value = type;
		
		document.distributionPrefsForm.submit();
	}

	function doAjax(type, idx) {
		var subjAreaObj = document.getElementById('subjectArea'+idx);
		var courseNbrObj = document.getElementById('courseNbr'+idx);
		var itypeObj = document.getElementById('itype'+idx);
		var classNumberObj = document.getElementById('classNumber'+idx);

		var id = null;
		var options = null;
		var next = null;
		
		if (type=='subjectArea') {
			id = subjAreaObj.value;
			options = courseNbrObj.options;
			next = 'courseNbr';
			courseNbrObj.options.length=1;
			itypeObj.options.length=1;
			classNumberObj.options.length=1;
		} else if (type=='courseNbr') {
			id = courseNbrObj.value;
			options = itypeObj.options;
			next = 'itype';
			itypeObj.options.length=1;
			classNumberObj.options.length=1;
		} else if (type=='itype') {
			id = itypeObj.value;
			options = classNumberObj.options;
			classNumberObj.options.length=1;
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
							while (optVal.indexOf('_')>=0 && next=='itype')
								optVal = optVal.replace("_",String.fromCharCode(160,160,160,160));
							options[i+1]=new Option(optVal, optId, false);
						}
					}
				}
			}
			if (options.length==2 || type=='itype') {
				options.selectedIndex = 1;
				if (next!=null) doReload(next,idx);
			}
		};
	
		// Request
		var vars = "id="+id+"&type="+type;
		req.open( "POST", "distributionPrefsAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
	
	function distTypeChanged(id) {
		var descObj = document.getElementById('distTypeDesc');
		var prefLevObj = document.getElementsByName('prefLevel')[0];
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
		req.open( "POST", "distributionPrefsAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}	

	function groupingChanged(id) {
		var descObj = document.getElementById('groupingDesc');
		
		if (id=='-') {
			descObj.innerHTML='';
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
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							if (optId=='desc') {
								var desc = optVal;
								while (desc.indexOf('@lt@')>=0) desc = desc.replace('@lt@','<');
								while (desc.indexOf('@gt@')>=0) desc = desc.replace('@gt@','>');
								while (desc.indexOf('@quot@')>=0) desc = desc.replace('@quot@','"');
								while (desc.indexOf('@amp@')>=0) desc = desc.replace('@amp@','&');
								descObj.innerHTML = desc;
							}
						}
					}
				}
			}
		};
	
		// Request
		var vars = "id="+id+"&type=grouping";
		req.open( "POST", "distributionPrefsAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}	
</SCRIPT>
				
		
