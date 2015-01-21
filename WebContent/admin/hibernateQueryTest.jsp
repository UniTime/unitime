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
<%@ page language="java" %>
<%@ page errorPage="../error.jsp" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<html:form action="/hibernateQueryTest">

	<TABLE width="100%">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>HQL</tt:section-title>
					<html:submit property="op" accesskey="S" titleKey="button.submit">
						<bean:message key="button.submit"/>
					</html:submit>
					<html:submit property="op" accesskey="C" value="Clear Cache" title="Clear Hibernate Cache (Alt+C)"/>
				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD valign="top">
				Errors:
			</TD>
			<TD class="errorCell">
				<html:messages id="error">
					${error}<br>
			    </html:messages>
			</TD>
		</TR>
		</logic:messagesPresent>

		<TR>
			<TD valign="top">
				Query:
			</TD>
			<TD>
				<html:textarea property="query" rows="12" cols="120"></html:textarea>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>

		<logic:notEmpty name="hibernateQueryTestForm" property="listSize">		
			<TR>
				<TD colspan='2'>
					<tt:section-title>Result (<bean:write name="hibernateQueryTestForm" property="listSize" /> lines)</tt:section-title>
				</TD>
			</TR>
		
			<logic:notEmpty scope="request" name="result">
				<TR>
					<TD colspan='2'>
						<bean:write scope="request" name="result" filter="false"/>
					</TD>
				</TR>
			</logic:notEmpty>
		</logic:notEmpty>
		
		<logic:notEmpty scope="request" name="sql">
			<TR>
				<TD colspan='2'>
					<br><tt:section-title>Generated SQL</tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<bean:write scope="request" name="sql" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>	
		
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align="right">
				<html:submit property="op" accesskey="S" titleKey="button.submit">
					<bean:message key="button.submit"/>
				</html:submit>
				<html:submit property="op" accesskey="C" value="Clear Cache" title="Clear Hibernate Cache (Alt+C)"/>
			</TD>
		</TR>

	</TABLE>

</html:form>	
