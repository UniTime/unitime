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
<%@page import="org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/ecbs">
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>Mode:</TD>
			<TD>
				<html:select property="type">
					<html:options name="ecbsForm" property="types"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Limit:</TD>
			<TD>
				<html:text property="limit" size="5" maxlength="5"/> %
				&nbsp;<html:errors property="limit"/>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Change"/>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	
	<logic:notEmpty name="cbs" scope="request">
		<bean:define id="limit" name="ecbsForm" property="limit" type="Double"/>
		<bean:define id="type" name="ecbsForm" property="typeInt" type="Integer"/>
		<bean:define id="cbs" name="cbs" type="ExamConflictStatisticsInfo" scope="request"/>
		<font size='2'>
			<% ExamConflictStatisticsInfo.printHtmlHeader(out); %>
			<% cbs.printHtml(out, limit / 100.0, type, true); %>
		</font>
		<table border='0' width='100%'><tr><td>
			<tt:displayPrefLevelLegend/>
		</td></tr></table>
	</logic:notEmpty>
	<logic:notEmpty name="warning" scope="request">
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">	
					<i><bean:write name="warning" scope="request"/></i>
				</TD>
			</TR>
		</TABLE>	
	</logic:notEmpty>
</html:form>
