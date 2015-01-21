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
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/lastChanges">
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>Department:</TD>
		<TD>
			<html:select property="departmentId">
				<html:option value="-1">All Departments</html:option>
				<html:options collection="departments" labelProperty="label" property="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD>Subject Area:</TD>
		<TD>
			<html:select property="subjAreaId">
				<html:option value="-1">All Subjects</html:option>
				<html:options collection="subjAreas" labelProperty="subjectAreaAbbreviation" property="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD>Manager:</TD>
		<TD>
			<html:select property="managerId">
				<html:option value="-1">All Managers</html:option>
				<html:options collection="managers" labelProperty="name" property="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD>Number of Changes:</TD>
		<TD>
			<html:text property="n" maxlength="5" size="8" />
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<html:submit onclick="displayLoading();" property="op" value="Apply"/>
			<html:submit property="op" value="Export PDF"/>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
					<html:submit property="op" value="Export PDF"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>

	<BR><BR>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=request.getAttribute("table")%>
	</TABLE>
</html:form>
