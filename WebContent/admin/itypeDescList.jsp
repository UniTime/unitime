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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='7'>
			<tt:section-header>
				<tt:section-title>Instructional Types</tt:section-title>
				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR><TD nowrap>
						<sec:authorize access="hasPermission(null, null, 'InstructionalTypeAdd')">
							<html:form action="itypeDescEdit" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="I" titleKey="title.addIType">
									<bean:message key="button.addIType" />
								</html:submit>
							</html:form>
						</sec:authorize>
					</TD><TD nowrap>
						<input type='button' onclick="document.location='itypeDescList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
					</TD></TR>
				</TABLE>
			</tt:section-header>
		</TD>
	</TR>
	<bean:write name="itypeDescList" scope="request" filter="false"/>
	<TR>
		<TD colspan='7'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='7' align="right">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR><TD nowrap>
					<sec:authorize access="hasPermission(null, null, 'InstructionalTypeAdd')">
						<html:form action="itypeDescEdit" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="I" titleKey="title.addIType">
								<bean:message key="button.addIType" />
							</html:submit>
						</html:form>
					</sec:authorize>
				</TD><TD nowrap>
					<input type='button' onclick="document.location='itypeDescList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
				</TD></TR>
			</TABLE>
		</TD>
	</TR>
</TABLE>
