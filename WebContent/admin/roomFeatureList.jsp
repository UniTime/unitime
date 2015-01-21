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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

	<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="5">
			<tt:section-header>
				<tt:section-title>
					<span style='font-weight:normal;'>
					<% if (request.getAttribute("roomFeaturesGlobal") != null) {%>
						<A class="l7" href="#roomFeaturesGlobal">Global Room Features</A>&nbsp;
					<% } %>
					<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
						<A class="l7" href="#roomFeaturesDepartment">Department Room Features</A>&nbsp;
					<% } %>
					</span>
				</tt:section-title>
				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR>
						<sec:authorize access="hasPermission(null, 'Session', 'GlobalRoomFeatureAdd') or hasPermission(#roomFeatureListForm.deptCodeX, 'Department', 'DepartmentRoomFeatureAdd')">
							<TD align="right">
								<html:form action="roomFeatureAdd" styleClass="FormWithNoPadding">			
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.addRoomFeature">
										<bean:message key="button.addRoomFeature" />
									</html:submit>
								</html:form>
							</TD>
						</sec:authorize>
					</TR>
				</TABLE>
			</tt:section-header>
		</TD>
	</TR>

	<logic:messagesPresent>
	<TR>
		<TD colspan="5" align="left" class="errorCell">
			<B><U>ERRORS</U></B><BR>
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

<!-- room features list -->
	<% if (request.getAttribute("roomFeaturesGlobal") != null) {%>
		<TR>
			<TD valign="middle" colspan="5">
				&nbsp;<A name="roomFeaturesGlobal"></A>
			</TD>
		</TR>
		<%=request.getAttribute("roomFeaturesGlobal")%>
	<%}%>

	<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
		<TR>
			<TD valign="middle" colspan="5">
				&nbsp;<A name="roomFeaturesDepartment"></A>
			</TD>
		</TR>
		<%=request.getAttribute("roomFeaturesDepartment")%>
	<%}%>
	
	<% if (request.getAttribute("roomFeaturesDepartment") == null && request.getAttribute("roomFeaturesGlobal") == null) {%>
		<TR>
			<TD valign="middle" colspan="5">
				<i>There are no room features available for the selected department.</i>
			</TD>
		</TR>
	<% } %>

	<TR>
		<TD valign="middle" colspan="5">&nbsp;</TD>
	</TR>

	<TR>
		<TD valign="middle" colspan="5">
			<tt:section-header>
			<tt:section-title>
				<% if (request.getAttribute("roomFeaturesGlobal") != null) {%>
					<A class="l7" href="#roomFeaturesGlobal"><span style='font-weight:normal;'>Global Room Features</span></A>&nbsp;
				<% } %>
				<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
					<A class="l7" href="#roomFeaturesDepartment"><span style='font-weight:normal;'>Department Room Features</span></A>&nbsp;
				<% } %>
			</tt:section-title>
			</tt:section-header>
		</TD>
	</TR>

<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="5">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR>
						<sec:authorize access="hasPermission(null, 'Session', 'GlobalRoomFeatureAdd') or hasPermission(#roomFeatureListForm.deptCodeX, 'Department', 'DepartmentRoomFeatureAdd')">
							<TD align="right">
								<html:form action="roomFeatureAdd" styleClass="FormWithNoPadding">			
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.addRoomFeature">
										<bean:message key="button.addRoomFeature" />
									</html:submit>
								</html:form>
							</TD>
						</sec:authorize>
					</TR>
				</TABLE>
		</TD>
	</TR>
</TABLE>
			
<SCRIPT type="text/javascript" language="javascript">
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
</SCRIPT>			
