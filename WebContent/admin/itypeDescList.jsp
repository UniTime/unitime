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
