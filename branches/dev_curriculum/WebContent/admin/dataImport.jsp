<%-- 
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 --%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<html:form action="/dataImport" focus="file" enctype="multipart/form-data">

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

	<logic:messagesPresent>
		<TR>
			<TD colspan='2'>
				<tt:section-title><font color='red'>Errors</font></tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
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
		<TR><TD>&nbsp;</TD></TR>
	</logic:messagesPresent>
	
	<logic:notEmpty name="dataImportForm" property="log">
		<TR>
			<TD colspan='2'>
				<tt:section-title>Export/Import Log</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<BLOCKQUOTE>
					<bean:write name="dataImportForm" property="log" filter="false"/>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		<TR><TD>&nbsp;</TD></TR>
	</logic:notEmpty>

		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Data Import</tt:section-title>
					<html:submit property="op" onclick="displayLoading()">Import</html:submit>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>File:</TD>
			<TD>
				<html:file name="dataImportForm" property="file" size="100" maxlength="255"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				&nbsp;
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>Data Export</tt:section-title>
					<html:submit property="op" onclick="displayLoading()">Export</html:submit>
				</tt:section-header>
			</TD>
		</TR>
	
		<TR>
			<TD nowrap>Course Offerings:</TD>
			<TD>
				<html:checkbox property="exportCourses"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Final Examinations:</TD>
			<TD>
				<html:checkbox property="exportFinalExams"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Midterm Examinations:</TD>
			<TD>
				<html:checkbox property="exportMidtermExams"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Course Timetable:</TD>
			<TD>
				<html:checkbox property="exportTimetable"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-title>Options</tt:section-title>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Email (Log, Export XML):</TD>
			<TD>
				<html:checkbox property="email" onclick="document.getElementById('eml').style.display=(this.checked?'inline':'none');"/>
				<html:text property="address" size="70" styleId="eml" style="display:none;"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan='2'>
				<html:submit property="op" onclick="displayLoading()">Import</html:submit>
				<html:submit property="op" onclick="displayLoading()">Export</html:submit>
			</TD>
		</TR>
		
	</TABLE>
</html:form>
