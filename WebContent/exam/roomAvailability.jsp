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
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<tt:back-mark back="true" clear="true" title="Room Availability" uri="roomAvailability.do"/>
<tiles:importAttribute />
<html:form action="/roomAvailability">
	<html:hidden property="showSections"/>
	<html:hidden property="subjectArea" />
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='2'>
			<tt:section-header>
				<tt:section-title>Filter</tt:section-title>
				<html:submit onclick="displayLoading();" accesskey="A" property="op" value="Apply"/>
				<logic:notEmpty name="roomAvailabilityForm" property="table">
					<html:submit property="op" value="Export PDF"/>
				</logic:notEmpty>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Examination Problem:</TD>
		<TD>
			<html:select property="examType">
				<html:option value="-1">Select...</html:option>
				<html:options collection="examTypes" property="uniqueId" labelProperty="label"/>
			</html:select>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Room Filter:</TD>
		<TD>
			<html:text property="filter" size="80"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Display Examinations:</TD>
		<TD>
			<html:checkbox property="includeExams"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Compare Examinations:</TD>
		<TD>
			<html:checkbox property="compare"/>
		</TD>
	</TR>
	<logic:notEmpty scope="request" name="timestamp">
		<TR>
  			<TD width="10%" nowrap>Last Update:</TD>
			<TD>
				<bean:write scope="request" name="timestamp"/>
			</TD>
		</TR>
	</logic:notEmpty>
	</TABLE>

	<BR><BR>
	<logic:empty name="roomAvailabilityForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<tr><td><i>
				<logic:equal name="roomAvailabilityForm" property="examType" value="-1">
					Examination problem not selected.
				</logic:equal>
				<logic:notEqual name="roomAvailabilityForm" property="examType" value="-1">
					Nothing to display.
				</logic:notEqual>
			</i></td></tr>
		</table>
	</logic:empty>
	<logic:notEmpty name="roomAvailabilityForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<bean:write name="roomAvailabilityForm" property="table" filter="false"/>
		</table>
	</logic:notEmpty>
	
	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD align="right">
			<html:submit onclick="displayLoading();" accesskey="A" property="op" value="Apply"/>
			<logic:notEmpty name="roomAvailabilityForm" property="table">
				<html:submit property="op" value="Export PDF"/>
			</logic:notEmpty>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>	
</html:form>
