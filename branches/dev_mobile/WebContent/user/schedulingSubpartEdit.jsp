<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
--%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.action.SchedulingSubpartEditAction" %>
<%@ page import="org.unitime.timetable.form.SchedulingSubpartEditForm" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.CourseCreditType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditUnitType" %>
<%@ page import="org.unitime.timetable.model.FixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.ArrangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableFixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableRangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<tt:session-context/>
<%
	// Get Form 
	String frmName = "SchedulingSubpartEditForm";
	SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) request.getAttribute(frmName);
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>
<loc:bundle name="CourseMessages">
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	
	function datePatternChanged(){			
		var op2Obj = document.getElementById('op2');
		if (op2Obj!=null) {
			op2Obj.value='updateDatePattern';
			document.forms[0].submit();
		}			
	}
	
	// -->
</SCRIPT>
		
<tiles:importAttribute />
<html:form action="/schedulingSubpartEdit" focus="timePattern" >
	<html:hidden property="schedulingSubpartId"/>
	<html:hidden property="creditText"/>
	<html:hidden property="subpartCreditEditAllowed"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name="<%=frmName%>" property="subjectArea" />&nbsp;
						<bean:write name="<%=frmName%>" property="courseNbr" /> : 
						<bean:write name="<%=frmName%>" property="parentSubpart" />
						<B><bean:write name="<%=frmName%>" property="instructionalTypeLabel" /></B>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessUpdatePreferences()%>' 
						title='<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>' >
						<loc:message name="actionUpdatePreferences"/>
					</html:submit>
					<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEditClearPreferences')"> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessClearSubpartPreferences() %>' 
						title='<%=MSG.titleClearSubpartPreferences(MSG.accessClearSubpartPreferences()) %>'>
						<loc:message name="actionClearSubpartPreferences" />
					</html:submit> 
					</sec:authorize>
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessPreviousSubpart() %>" 
							title="<%=MSG.titlePreviousSubpartWithUpdate(MSG.accessPreviousSubpart()) %>">
							<loc:message name="actionPreviousSubpart" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextSubpart() %>" 
							title="<%=MSG.titleNextSubpartWithUpdate(MSG.accessNextSubpart()) %>">
							<loc:message name="actionNextSubpart" />
						</html:submit>
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessBackToDetail()%>" 
						title="<%=MSG.titleBackToDetail(MSG.accessBackToDetail()) %>">
						<loc:message name="actionBackToDetail"/>
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>
		

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errorsSubpartEdit"/></U></B><BR>
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

		<logic:notEmpty name="<%=frmName%>" property="managingDeptName">
			<TR>
				<TD><loc:message name="filterManager"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="managingDeptName" />
				</TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="parentSubpartId">
			<TR>
				<TD><loc:message name="propertyParentSchedulingSubpart"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
				</TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD><loc:message name="filterInstructionalType"/></TD>
			<TD>				 
				<html:select style="width:200px;" property="instructionalType" onchange="javascript: itypeChanged(this);">
					<html:options collection="<%=ItypeDesc.ITYPE_ATTR_NAME%>" property="itype" labelProperty="desc" />
					<logic:equal name="<%=frmName%>" property="itypeBasic" value="true">
						<html:option value="more" style="background-color:rgb(223,231,242);">
							<loc:message name="selectMoreOptions"/></html:option>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="itypeBasic" value="false">
						<html:option value="less" style="background-color:rgb(223,231,242);">
							<loc:message name="selectLessOptions"/></html:option>
					</logic:equal>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<html:hidden property="op2" value="" styleId="op2"/>
				<html:select style="width:200px;" property="datePattern" onchange='<%= "datePatternChanged();"%>'>
					<html:options collection="<%=org.unitime.timetable.model.DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value" />
				</html:select>
				<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('Preview of '+SchedulingSubpartEditForm.datePattern.options[SchedulingSubpartEditForm.datePattern.selectedIndex].text, 'user/dispDatePattern.jsp?id='+SchedulingSubpartEditForm.datePattern.value+'&subpart='+SchedulingSubpartEditForm.schedulingSubpartId.value,'840','520');">
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyAutomaticSpreadInTime"/></TD>
			<TD>
				<html:checkbox property="autoSpreadInTime"/> <i><loc:message name="descriptionAutomaticSpreadInTime"/></i>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyStudentOverlaps"/></TD>
			<TD>
				<html:checkbox property="studentAllowOverlap"/> <i><loc:message name="descriptionStudentOverlaps"/></i>
			</TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="sameItypeAsParent" value="false">
		<logic:equal name="<%=frmName%>" property="subpartCreditEditAllowed" value="true">
		<TR>
			<TD><loc:message name="propertySubpartCredit"/></TD>
			<TD>
				<html:select style="width:200px;" property="creditFormat" onchange="<%= \"if (this.value == '\" + FixedCreditUnitConfig.CREDIT_FORMAT + \"') { document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true } else if (this.value == '\" + ArrangeCreditUnitConfig.CREDIT_FORMAT + \"'){document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '\" + VariableFixedCreditUnitConfig.CREDIT_FORMAT + \"') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = true} else if (this.value == '\" + VariableRangeCreditUnitConfig.CREDIT_FORMAT + \"') {document.forms[0].creditType.disabled = false; document.forms[0].creditUnitType.disabled = false; document.forms[0].units.disabled = false; document.forms[0].maxUnits.disabled = false; document.forms[0].fractionalIncrementsAllowed.disabled = false} else {document.forms[0].creditType.disabled = true; document.forms[0].creditUnitType.disabled = true; document.forms[0].units.disabled = true; document.forms[0].maxUnits.disabled = true; document.forms[0].fractionalIncrementsAllowed.disabled = true}\"%>">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=org.unitime.timetable.model.CourseCreditFormat.COURSE_CREDIT_FORMAT_ATTR_NAME%>" property="reference" labelProperty="label"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD> &nbsp;</TD>
			<TD>
				<table>
				<tr>
				<td nowrap><loc:message name="propertyCreditType"/></td>
				<td>
				<html:select style="width:200px;" property="creditType" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0)?false:true%>">
					<html:options collection="<%=CourseCreditType.COURSE_CREDIT_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
				</html:select>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyCreditUnitType"/></td>
				<td>
				<html:select style="width:200px;" property="creditUnitType" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0)?false:true%>">
					<html:options collection="<%=CourseCreditUnitType.COURSE_CREDIT_UNIT_TYPE_ATTR_NAME%>" property="uniqueId" labelProperty="label" />
				</html:select>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyUnits"/></td>
				<td>
				<html:text property="units" maxlength="4" size="4" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().length() > 0 && !frm.getCreditFormat().equals(ArrangeCreditUnitConfig.CREDIT_FORMAT))?false:true%>"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyMaxUnits"/></td>
				<td>
				<html:text property="maxUnits" maxlength="4" size="4" disabled="<%=(frm.getCreditFormat() != null && (frm.getCreditFormat().equals(VariableFixedCreditUnitConfig.CREDIT_FORMAT) || frm.getCreditFormat().equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT)))?false:true%>"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyFractionalIncrementsAllowed"/></td>
				<td>
				<html:checkbox property="fractionalIncrementsAllowed" disabled="<%=(frm.getCreditFormat() != null && frm.getCreditFormat().equals(VariableRangeCreditUnitConfig.CREDIT_FORMAT))?false:true%>"/>
				</td>
				</tr>
				</table>
			</TD>
		</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="subpartCreditEditAllowed" value="false">
		<TR>
		
			<TD><loc:message name="propertyCredit"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="creditText" />
					<html:hidden property="creditFormat"/>
					<html:hidden property="creditType"/>
					<html:hidden property="creditUnitType"/>
					<html:hidden property="units"/>
					<html:hidden property="maxUnits"/>				
					<html:hidden property="fractionalIncrementsAllowed"/>				
			</TD>
			
		</TR>
		</logic:equal>
		</logic:equal>
<!-- Preferences -->
		<logic:equal value="true" name="<%=frmName%>" property="unlimitedEnroll">
			<jsp:include page="preferencesEdit.jspf">
				<jsp:param name="frmName" value="<%=frmName%>"/>
				<jsp:param name="distPref" value="false"/>
				<jsp:param name="periodPref" value="false"/>
				<jsp:param name="bldgPref" value="false"/>
				<jsp:param name="roomFeaturePref" value="false"/>
				<jsp:param name="roomGroupPref" value="false"/>
			</jsp:include>
		</logic:equal>
		<logic:notEqual value="true" name="<%=frmName%>" property="unlimitedEnroll">
			<jsp:include page="preferencesEdit.jspf">
				<jsp:param name="frmName" value="<%=frmName%>"/>
				<jsp:param name="distPref" value="false"/>
				<jsp:param name="periodPref" value="false"/>
			</jsp:include>
		</logic:notEqual>

<!-- buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessUpdatePreferences()%>' 
						title='<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>' >
						<loc:message name="actionUpdatePreferences"/>
					</html:submit>
					<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEditClearPreferences')"> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessClearSubpartPreferences() %>' 
						title='<%=MSG.titleClearSubpartPreferences(MSG.accessClearSubpartPreferences()) %>'>
						<loc:message name="actionClearSubpartPreferences" />
					</html:submit> 
					</sec:authorize>
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessPreviousSubpart() %>" 
							title="<%=MSG.titlePreviousSubpartWithUpdate(MSG.accessPreviousSubpart()) %>">
							<loc:message name="actionPreviousSubpart" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextSubpart() %>" 
							title="<%=MSG.titleNextSubpartWithUpdate(MSG.accessNextSubpart()) %>">
							<loc:message name="actionNextSubpart" />
						</html:submit>
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessBackToDetail()%>" 
						title="<%=MSG.titleBackToDetail(MSG.accessBackToDetail()) %>">
						<loc:message name="actionBackToDetail"/>
					</html:submit>
			</TD>
		</TR>

	</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(SchedulingSubpartEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(SchedulingSubpartEditAction.HASH_ATTR)%>";
	<% } %>
	    self.focus();
  	}
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
							options[count+1] = new Option("<%=MSG.selectMoreOptions()%>","more",false);
						else
							options[count+1] = new Option("<%=MSG.selectLessOptions()%>","less",false);
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