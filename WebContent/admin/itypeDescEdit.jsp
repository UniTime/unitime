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
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />

<html:form action="/itypeDescEdit">
	<html:hidden property="uniqueId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="itypeDescEditForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:equal name="itypeDescEditForm" property="op" value="Update">
							Edit
						</logic:equal>
						Instructional Type
					</tt:section-title>
					<logic:equal name="itypeDescEditForm" property="op" value="Save">
						<html:submit property="op" value="Save" title="Save (Alt+S)" accesskey="S"/> 
					</logic:equal>
					<logic:equal name="itypeDescEditForm" property="op" value="Update">
						<html:submit property="op" value="Update" title="Update (Alt+U)" accesskey="U"/>
						<sec:authorize access="hasPermission(#itypeDescEditForm.uniqueId, 'ItypeDesc', 'InstructionalTypeDelete')"> 
							<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D"/>
						</sec:authorize>
					</logic:equal>
					<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>IType:</TD>
			<TD>
				<logic:equal name="itypeDescEditForm" property="op" value="Save">
					<html:text property="id" size="2" maxlength="2"/>
					&nbsp;<html:errors property="id"/>
				</logic:equal>
				<logic:equal name="itypeDescEditForm" property="op" value="Update">
					<html:hidden property="id"/>
					<bean:write name="itypeDescEditForm" property="id"/>
				</logic:equal>
			</TD>
		</TR>

		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbreviation" size="7" maxlength="7"/>
				&nbsp;<html:errors property="abbreviation"/>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="50" maxlength="50"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Reference:</TD>
			<TD>
				<html:text property="reference" size="20" maxlength="20"/>
				&nbsp;<html:errors property="reference"/>
			</TD>
		</TR>

		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="type">
					<html:options name="itypeDescEditForm" property="types"/>
				</html:select>
				&nbsp;<html:errors property="type"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Parent:</TD>
			<TD>
				<html:select property="parent">
					<html:option value=""></html:option>
					<html:options collection="<%=ItypeDesc.ITYPE_ATTR_NAME%>" property="itype" labelProperty="desc" />
				</html:select>
				&nbsp;<html:errors property="parent"/>
			</TD>
		</TR>

		<TR>
			<TD>Organized:</TD>
			<TD>
				<html:checkbox property="organized"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="itypeDescEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" title="Save (Alt+S)" accesskey="S"/> 
				</logic:equal>
				<logic:equal name="itypeDescEditForm" property="op" value="Update">
					<html:submit property="op" value="Update" title="Update (Alt+U)" accesskey="U"/>
					<sec:authorize access="hasPermission(#itypeDescEditForm.uniqueId, 'ItypeDesc', 'InstructionalTypeDelete')"> 
						<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D"/>
					</sec:authorize>
				</logic:equal>
				<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
			</TD>
		</TR>
	</TABLE>

</html:form>
