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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.timetable.model.SimpleItypeConfig"%>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>


<loc:bundle name="CourseMessages">
<tt:session-context/>
<SCRIPT language="javascript">
	<!--

		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmNumClasses(numClasses) {
			/*integer maxNumClasses = 0;
			for (integer i = 1; i < document.forms[0].elements['nc'].length; i++){
			  if (maxNumClasses < document.forms[0].elements['nc' + i].value){
			    maxNumClasses = document.forms[0].elements['nc' + i].value;
			  }
			}
			  figure out max number of classes */
			if(numClasses > 100) {
				if (!confirmDelete("<%=MSG.confirmCreateTooManyClasses()%>".replace("{0}",numClasses))){
			        return(false);
				}
			}
		    return(true);
		}
		function confirmDelete1() {
		   /* if (!confirmNumClasses()){
		        return(false);
		    } */
			return confirmDelete("<%=MSG.confirmMayDeleteSubpartsClasses()%>");
		}

		function confirmDelete2() {
			return confirmDelete("<%=MSG.confirmDeleteExistingSubpartsClasses()%>");
		}

		function confirmDelete(msg) {
			if (jsConfirm!=null && !jsConfirm) {
				document.forms[0].elements['click'].value='y'; 
				return true;
			} 
				
			if(confirm(msg)) {
				document.forms[0].elements['click'].value='y'; 
				return true;
			} 
			else {
				return false;
			}
		}

		function doClick(op, id) {
			document.forms[0].elements["hdnOp"].value=op;
			document.forms[0].elements["id"].value=id;
			document.forms[0].elements["click"].value="y";
			document.forms[0].submit();
		}
		
	// -->
</SCRIPT>

<% 
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
	String subjArea = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
%>

<html:form action="/instructionalOfferingConfigEdit">
	<html:hidden property="configId" />
	<html:hidden property="instrOfferingId" />
	<html:hidden property="subjectArea" styleId="subjectId"/>
	<html:hidden property="courseNumber" styleId="course"/>
	<html:hidden property="notOffered" />
	<html:hidden property="configCount" />
	<INPUT type="hidden" name="id" value = "">
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="click" value = "n">
	<INPUT type="hidden" name="doit" value="Cancel">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<A  title="Back to Instructional Offering List (Alt+I)" 
							accesskey="I"
							class="l8" 
							href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<%=subjArea%>&courseNbr=<%=crsNbr%>#A<bean:write name="instructionalOfferingConfigEditForm" property="courseOfferingId" />">
						<B><bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingName" /></B></A>
						<html:hidden property="instrOfferingName"/>											
						<html:hidden property="courseOfferingId"/>
					</tt:section-title>						

					<logic:equal name="instructionalOfferingConfigEditForm" property="configId" value="0">
						<logic:equal name="subpartsExist" scope="request" value="true">
							<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessSaveConfiguration() %>" 
								title="<%=MSG.titleSaveConfiguration(MSG.accessSaveConfiguration()) %>" 
								onclick="if (confirmNumClasses()) {document.forms[0].elements['click'].value='y'; return true; } else {return false; }">			
								<loc:message name="actionSaveConfiguration" />
							</html:submit>						
						</logic:equal>
					</logic:equal>
					
					<logic:notEqual name="instructionalOfferingConfigEditForm" property="configId" value="0">
						<logic:equal name="subpartsExist" scope="request" value="true">
							<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessUpdateConfiguration() %>" 
								title="<%=MSG.titleUpdateConfiguration(MSG.accessUpdateConfiguration()) %>" 
								onclick="return (confirmDelete1());" >			
								<loc:message name="actionUpdateConfiguration" />
							</html:submit>						
						</logic:equal>
						<sec:authorize access="hasPermission(#instructionalOfferingConfigEditForm.configId, 'InstrOfferingConfig', 'InstrOfferingConfigDelete')">
							<html:submit property="op" 
								styleClass="btn" 
								accesskey="<%=MSG.accessDeleteConfiguration() %>" 
								title="<%=MSG.titleDeleteConfiguration(MSG.accessDeleteConfiguration()) %>" 
								onclick="return (confirmDelete2());" >			
								<loc:message name="actionDeleteConfiguration" />
							</html:submit>
						</sec:authorize>						
					</logic:notEqual>
	
					<bean:define id="instrOfferingId">
						<bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingId" />				
					</bean:define>
					 
					<html:button property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessBackToIODetail() %>" 
						title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>" 
						onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
						<loc:message name="actionBackToIODetail" />
					</html:button>

				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errorsConfigurationEdit"/></U></B><BR>
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
			<TD><loc:message name="propertyConfigurationName"/></TD>
			<TD>
				<html:text property="name" size="10" maxlength="10" />
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyUnlimitedEnrollment"/></TD>
			<TD>
				<html:checkbox property="unlimited" onclick="doClick('unlimitedEnrollment', '');" />
			</TD>
		</TR>

		<logic:notEqual name="instructionalOfferingConfigEditForm" property="unlimited" value="true" >
		<TR>
			<TD><loc:message name="propertyConfigurationLimit"/><font class="reqField">*</font></TD>
			<TD>
				<html:text property="limit" size="4" maxlength="4" />
			</TD>
		</TR>
		</logic:notEqual>

		<TR>
			<TD><loc:message name="filterInstructionalType"/></TD>
			<TD>
				<html:select property="itype" onchange="javascript: itypeChanged(this);">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=ItypeDesc.ITYPE_ATTR_NAME%>" property="itype" labelProperty="desc" />
					<html:option value="more" style="background-color:rgb(223,231,242);">More Options &gt;&gt;&gt;</html:option>
				</html:select>
				&nbsp;
				<html:submit property="op" 
					styleClass="btn" 
					accesskey="<%=MSG.accessAddInstructionalTypeToConfig() %>" 
					title="<%=MSG.titleAddInstructionalTypeToConfig(MSG.accessAddInstructionalTypeToConfig()) %>" 
					onclick="document.forms[0].elements['click'].value='y'" >
					<loc:message name="actionAddInstructionalTypeToConfig" />
				</html:submit>
		</TR>

		<logic:notEmpty name="instructionalOfferingConfigEditForm" property="catalogLinkLabel">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="<bean:write name="instructionalOfferingConfigEditForm" property="catalogLinkLocation" />" target="_blank"><bean:write name="instructionalOfferingConfigEditForm" property="catalogLinkLabel" /></A>
			</TD>
		</TR>
		</logic:notEmpty>
		<tt:hasProperty name="unitime.custom.CourseUrlProvider">
			<TR>
				<TD><loc:message name="propertyCourseCatalog"/> </TD>
				<TD>
					<span id='UniTimeGWT:CourseLink' style="display: none;">-<bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingId"/></span>
				</TD>
			</TR>
		</tt:hasProperty>

		<TR>
			<TD colspan="2">
			&nbsp;
			</TD>
		</TR>

	</TABLE>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="2">
		<% int cols = 9; %>
		<%= request.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME)!=null 
			? request.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME)
			: "" %>		
		
		<TR>
			<TD colspan="<%=cols%>"><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD>
		</TR>
		<TR>
			<TD colspan="<%=cols%>" align="right">
				<logic:equal name="instructionalOfferingConfigEditForm" property="configId" value="0">
					<logic:equal name="subpartsExist" scope="request" value="true">
						<html:submit property="op" 
							accesskey="<%=MSG.accessSaveConfiguration() %>" 
							title="<%=MSG.titleSaveConfiguration(MSG.accessSaveConfiguration()) %>" 
							onclick="if (confirmNumClasses()) {document.forms[0].elements['click'].value='y'; return true; } else {return false; }">			
							<loc:message name="actionSaveConfiguration" />
						</html:submit>						
					</logic:equal>
				</logic:equal>
				
				<logic:notEqual name="instructionalOfferingConfigEditForm" property="configId" value="0">
					<logic:equal name="subpartsExist" scope="request" value="true">
						<html:submit property="op" 
							styleClass="btn" 
								accesskey="<%=MSG.accessUpdateConfiguration() %>" 
								title="<%=MSG.titleUpdateConfiguration(MSG.accessUpdateConfiguration()) %>" 
								onclick="return (confirmDelete1());" >			
								<loc:message name="actionUpdateConfiguration" />						</html:submit>						
					</logic:equal>
					<sec:authorize access="hasPermission(#instructionalOfferingConfigEditForm.configId, 'InstrOfferingConfig', 'InstrOfferingConfigDelete')">
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessDeleteConfiguration() %>" 
							title="<%=MSG.titleDeleteConfiguration(MSG.accessDeleteConfiguration()) %>" 
							onclick="return (confirmDelete2());" >			
							<loc:message name="actionDeleteConfiguration" />
						</html:submit>
					</sec:authorize>						
				</logic:notEqual>

				<bean:define id="instrOfferingId">
					<bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingId" />				
				</bean:define>
				 
				<html:button property="op" 
					styleClass="btn" 
						accesskey="<%=MSG.accessBackToIODetail() %>" 
						title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>" 
						onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
						<loc:message name="actionBackToIODetail" />
				</html:button>
					
			</TD>
		</TR>
		
	</TABLE>

</html:form>


<SCRIPT language="javascript">
	<!--

	function checkClick() {
		
		if(document.forms[0].elements["click"].value=="y")
			return true;
		else
			return false;
	}
	// -->
</SCRIPT>

<SCRIPT type="text/javascript" language="javascript">
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
							options[count+1] = new Option("More Options >>>","more",false);
						else
							options[count+1] = new Option("<<< Less Options","less",false);
						options[count+1].style.backgroundColor='rgb(223,231,242)';
					}
				}
			}
		};
	
		// Request
		var vars = "basic="+basic;
		req.open( "POST", "itypesAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
</SCRIPT>

</loc:bundle>
