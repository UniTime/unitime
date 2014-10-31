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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.form.ExamEditForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "examEditForm";	
	ExamEditForm frm = (ExamEditForm) request.getAttribute(frmName);	
%>	
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<html:form action="examEdit">
	<html:hidden property="examId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="clone"/>
	<html:hidden property="op2" value=""/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='label'/>
					</tt:section-title>
				<logic:notEmpty name="<%=frmName%>" property="examId">
					<html:submit property="op" 
						styleClass="btn" accesskey="U" titleKey="title.updateExam" >
						<bean:message key="button.updateExam" />
					</html:submit> 
				</logic:notEmpty>
				<logic:empty name="<%=frmName%>" property="examId">
					<html:submit property="op" 
						styleClass="btn" accesskey="S" titleKey="title.saveExam" >
						<bean:message key="button.saveExam" />
					</html:submit> 
				</logic:empty>
				<logic:greaterEqual name="<%=frmName%>" property="previousId" value="0">
					&nbsp;
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousExam">
						<bean:message key="button.previousExam" />
					</html:submit> 
				</logic:greaterEqual>
				<logic:greaterEqual name="<%=frmName%>" property="nextId" value="0">
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextExam">
						<bean:message key="button.nextExam" />
					</html:submit> 
				</logic:greaterEqual>
				&nbsp;
				<html:submit property="op" 
					styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
					<bean:message key="button.returnToDetail" />
				</html:submit>
				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
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
		
		<TR>
			<TD>Name:</TD><TD><html:text property="name" size="50" maxlength="100"/> <i>(A default name is generated when blank)</i></TD>
		</TR>
		<TR>
			<TD>Type:</TD><TD>
				<logic:empty name="<%=frmName%>" property="examId">
					<html:select property="examType" onchange="javascript: doDel('examType', this.value); submit();">
						<html:options collection="examTypes" property="uniqueId" labelProperty="label"/>
					</html:select>
				</logic:empty>
				<logic:notEmpty name="<%=frmName%>" property="examId">
					<html:hidden property="examType"/>
					<logic:iterate name="examTypes" scope="request" id="type">
						<bean:define name="type" property="uniqueId" id="typeId" type="Long"/>
						<logic:equal name="<%=frmName%>" property="examType" value="<%=typeId.toString()%>">
							<bean:write name="type" property="label"/>
						</logic:equal>
					</logic:iterate>
				</logic:notEmpty>
			</TD>
		</TR>
		<TR>
			<TD>Length:</TD><TD><html:text property="length" size="5" maxlength="5"/></TD>
		</TR>
		<TR>
			<TD>Seating Type:</TD><TD>
				<html:select property="seatingType" onchange="javascript: doDel('seatingType', this.value); submit();">
					<html:options property="seatingTypes"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD nowrap>Maximum Number of Rooms:</TD><TD><html:text property="maxNbrRooms" size="5" maxlength="5"/></TD>
		</TR>
		<TR>
			<TD>Size:</TD><TD><html:text property="size" size="5" maxlength="5"/> <i>(<bean:write name="<%=frmName%>" property="sizeNote" />)</i></TD>
		</TR>
		<TR>
			<TD>Print Offset:</TD><TD><html:text property="printOffset" size="5" maxlength="5"/> <i>(in minutes, only used for reports)</i></TD>
		</TR>
		<TR>
			<TD valign="top">Notes</TD>
			<TD>
				<html:textarea property="note" cols="80" rows="5"/>
			</TD>
		</TR>
		<TR>
			<TD valign="top">Instructors:</TD>
			<TD>	
				<table border='0'>
				<logic:iterate name="<%=frmName%>" property="instructors" id="instructor" indexId="ctr">
					<tr><td nowrap>
					<html:select style="width:200px;"
						property='<%= "instructors[" + ctr + "]" %>'>
						<html:option value="-">-</html:option>
						<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME + ctr%>" property="value" labelProperty="label" />
					</html:select>
					<html:submit property="op" 
								styleClass="btn"
								onclick="<%= \"javascript: doDel('instructor', '\" + ctr + \"');\"%>">
								<bean:message key="button.delete" />
					</html:submit>
					</td></tr>
   				</logic:iterate>
   				<tr><td>
   					<html:submit property="op" 
						styleId="addInstructor" 
						styleClass="btn" accesskey="I" titleKey="title.addInstructor">
						<bean:message key="button.addInstructor" />
					</html:submit>
				</td></tr>
				</table> 			
		   	</TD>
	   	</TR>
		<logic:notEmpty name="<%=frmName%>" property="accommodation">
			<TR>
				<TD valign="top">Student Accommodations:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="accommodation" filter="false"/>
					<html:hidden property="accommodation"/>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-header>
					<tt:section-title><a name="objects">Classes / Courses</a></tt:section-title>
					<html:submit property="op"
						styleClass="btn" accesskey="O" titleKey="title.addObject">
						<bean:message key="button.addObject" />
					</html:submit> 			
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<table border='0'>
				<tr>
					<td width='100'><i>Subject</i></td>
					<td width='350'><i>Course<br>Number</i></td>
					<td width='160'><i>Config<br>Subpart</i></td>
					<td width='150'><i>Class<br>Number</i></td>
					<td></td>
				</tr>
				<logic:iterate name="<%=frmName%>" property="subjectAreaList" id="m" indexId="idx">
					<tr><td>
					<html:select style="width:90px;" property='<%="subjectArea["+idx+"]"%>' styleId='<%="subjectArea"+idx%>' 
						onchange="<%= \"javascript: doAjax('subjectArea', '\"+idx+\"');\" %>" >
						<html:option value="-1">-</html:option>
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId"/>
					</html:select>
					</td><td>
					<html:select style="width:340px;" property='<%="courseNbr["+idx+"]"%>' styleId='<%="courseNbr"+idx%>'
						onchange="<%= \"javascript: doAjax('courseNbr', '\"+idx+\"');\" %>" >
						<html:optionsCollection property='<%="courseNbrs["+idx+"]"%>' label="value" value="id"/>
					</html:select>
					</td><td>
					<html:select style="width:150px;" property='<%="itype["+idx+"]"%>' styleId='<%="itype"+idx%>'
						onchange="<%= \"javascript: doAjax('itype', '\"+idx+\"');\" %>" >
						<html:optionsCollection property='<%="itypes["+idx+"]"%>' label="value" value="id" filter="false"/>
					</html:select>
					</td><td>
					<html:select style="width:140px;" property='<%="classNumber["+idx+"]"%>' styleId='<%="classNumber"+idx%>'>
						<html:optionsCollection property='<%="classNumbers["+idx+"]"%>' label="value" value="id"/>
					</html:select>
					</td><td>
					<html:submit property="op" styleClass="btn" onclick="<%= \"javascript: doDel('objects', '\"+idx+\"');\"%>">
						<bean:message key="button.delete" />
					</html:submit>
					</td></tr>
   				</logic:iterate>
				</table>
			</TD>
		</TR>
		
		<jsp:include page="preferencesEdit.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
			<jsp:param name="distPref" value="false"/>
			<jsp:param name="timePref" value="false"/>
			<jsp:param name="datePatternPref" value="false"/>
			<jsp:param name="examSeating" value="<%=frm.getSeatingTypeIdx() == 1%>"/>
		</jsp:include>

<!-- buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
				<logic:notEmpty name="<%=frmName%>" property="examId">
					<html:submit property="op" 
						styleClass="btn" accesskey="U" titleKey="title.updateExam" >
						<bean:message key="button.updateExam" />
					</html:submit> 
				</logic:notEmpty>
				<logic:empty name="<%=frmName%>" property="examId">
					<html:submit property="op" 
						styleClass="btn" accesskey="S" titleKey="title.saveExam" >
						<bean:message key="button.saveExam" />
					</html:submit> 
				</logic:empty>
				<logic:greaterEqual name="<%=frmName%>" property="previousId" value="0">
					&nbsp;
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousExam">
						<bean:message key="button.previousExam" />
					</html:submit> 
				</logic:greaterEqual>
				<logic:greaterEqual name="<%=frmName%>" property="nextId" value="0">
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextExam">
						<bean:message key="button.nextExam" />
					</html:submit> 
				</logic:greaterEqual>
				&nbsp;
				<html:submit property="op" 
					styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
					<bean:message key="button.returnToDetail" />
				</html:submit>
			</TD>
		</TR>	
	</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function doAjax(type, idx) {
		var subjAreaObj = document.getElementById('subjectArea'+idx);
		var courseNbrObj = document.getElementById('courseNbr'+idx);
		var itypeObj = document.getElementById('itype'+idx);
		var classNumberObj = document.getElementById('classNumber'+idx);

		var id = null;
		var options = null;
		var next = null;
		var extra = "";
		
		if (type=='subjectArea') {
			id = subjAreaObj.value;
			options = courseNbrObj.options;
			next = 'courseNbr';
			courseNbrObj.options.length=1;
			itypeObj.options.length=1;
			itypeObj.options[0]=new Option('N/A', '-1', false);
			classNumberObj.options.length=1;
			classNumberObj.options[0]=new Option('N/A', '-1', false);
			if (id==0) return;
		} else if (type=='courseNbr') {
			id = courseNbrObj.value;
			options = itypeObj.options;
			next = 'itype';
			itypeObj.options.length=1;
			classNumberObj.options.length=1;
			classNumberObj.options.length=1;
			classNumberObj.options[0]=new Option('N/A', '-1', false);
			if (id==-1 || id<=<%=Long.MIN_VALUE%>+2) return;
		} else if (type=='itype') {
			id = itypeObj.value;
			options = classNumberObj.options;
			classNumberObj.options.length=1;
			if (id==-1) return;
			extra = "&courseId=" + courseNbrObj.value;
		}
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			options.length=0;
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						// Course numbers options creation
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							while (optVal.indexOf('_')>=0 && next=='itype')
								optVal = optVal.replace("_",String.fromCharCode(160,160,160,160));
							options[i]=new Option(optVal, optId, false);
						}
					}
					if (options.length==1) {
						options[0].selected=true;
						if (next!=null) doAjax(next,idx);
					}
				}
			}
		};
	
		// Request
		var vars = "id="+id+"&type="+type+extra;
		req.open( "POST", "examEditAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}
</SCRIPT>
