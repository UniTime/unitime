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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.form.SectioningDemoForm" %>
<%@ page import="org.unitime.timetable.form.SectioningDemoForm.CourseAssignmentBean" %>
<%@ page import="org.unitime.timetable.form.SectioningDemoForm.ClassAssignmentBean" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<script language="javascript">

	function disableAlternates(checkBox, idx) {
		document.getElementsByName('requests['+idx+'].alt1SubjectArea')[0].disabled=checkBox.checked;
		document.getElementsByName('requests['+idx+'].alt1CourseNbr')[0].disabled=checkBox.checked;
		document.getElementsByName('requests['+idx+'].alt2SubjectArea')[0].disabled=checkBox.checked;
		document.getElementsByName('requests['+idx+'].alt2CourseNbr')[0].disabled=checkBox.checked;
	}
	
	function updateCourseNumbers(subjectAreaId, comboBoxName) {
		var comboBox = document.getElementsByName(comboBoxName)[0];
		var courseNumbers = comboBox.options;

		if (subjectAreaId=='') {
			courseNumbers.length=1;
			return;
		}
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			courseNumbers.length=1;
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						// Course numbers options creation
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							courseNumbers[i+1]=new Option(
								xmlDoc.documentElement.childNodes[i].getAttribute("value"),
								xmlDoc.documentElement.childNodes[i].getAttribute("id"),
								false);
						}
					}
				}
			}
		};
	
		// Request
		var vars = "id="+subjectAreaId+"&type=subjectArea";
		req.open( "POST", "sectioningDemoAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}

	function updateDaysAndTimes(timePatternId, dayComboBoxName, timeComboBoxName) {
		var dayComboBox = document.getElementsByName(dayComboBoxName)[0];
		var days = dayComboBox.options;
		var timeComboBox = document.getElementsByName(timeComboBoxName)[0];
		var times = timeComboBox.options;

		if (timePatternId=='') {
			days.length=1;
			times.length=1;
			return;
		}
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			days.length=1;
			times.length=1;
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						// Course numbers options creation
						var daysCount = xmlDoc.documentElement.childNodes[0].childNodes.length;
						for(i=0; i<daysCount; i++) {
							days[i+1]=new Option(
								xmlDoc.documentElement.childNodes[0].childNodes[i].getAttribute("value"),
								xmlDoc.documentElement.childNodes[0].childNodes[i].getAttribute("id"),
								(daysCount==1));
						}
						var timesCount = xmlDoc.documentElement.childNodes[1].childNodes.length;
						for(i=0; i<timesCount; i++) {
							times[i+1]=new Option(
								xmlDoc.documentElement.childNodes[1].childNodes[i].getAttribute("value"),
								xmlDoc.documentElement.childNodes[1].childNodes[i].getAttribute("id"),
								(timesCount==1));
						}
					}
				}
			}
		};
	
		// Request
		var vars = "id="+timePatternId+"&type=timePattern";
		req.open( "POST", "sectioningDemoAjax.do", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		req.setRequestHeader("Content-Length", vars.length);
		//setTimeout("try { req.send('" + vars + "') } catch(e) {}", 1000);
		req.send(vars);
	}

	function typeChanged(type, idx, alt) {
		var dispNone = (type==<%=SectioningDemoForm.sTypeNone%>?"inline":"none");
		var dispCourse = (type==<%=SectioningDemoForm.sTypeCourse%>?"inline":"none");
		var dispFreeTime = (type==<%=SectioningDemoForm.sTypeFreeTime%>?"inline":"none");
		document.getElementById("span"+idx+"CrsN").style.display=dispNone;
		document.getElementById("span"+idx+"CrsC").style.display=dispCourse;
		document.getElementById("span"+idx+"CrsF").style.display=dispFreeTime;
		document.getElementById("span"+idx+"WN").style.display=dispNone;
		document.getElementById("span"+idx+"WC").style.display=dispCourse;
		document.getElementById("span"+idx+"WF").style.display=dispFreeTime;
		document.getElementById("span"+idx+"A1N").style.display=dispNone;
		document.getElementById("span"+idx+"A1C").style.display=dispCourse;
		document.getElementById("span"+idx+"A1F").style.display=dispFreeTime;
		document.getElementById("span"+idx+"A2N").style.display=dispNone;
		document.getElementById("span"+idx+"A2C").style.display=dispCourse;
		document.getElementById("span"+idx+"A2F").style.display=dispFreeTime;
	}
	
	function classClicked(source, subjectArea, courseNumber, classId) {
		var spanA = document.getElementById(subjectArea+':'+courseNumber+':'+classId+'a');
		var spanB = document.getElementById(subjectArea+':'+courseNumber+':'+classId+'b');
		spanA.style.display=(spanA.style.display=='none'?'inline':'none');
		spanB.style.display=(spanB.style.display=='none'?'inline':'none');
		source.title=(spanB.style.display=='none'?'Display choices...':'Hide choices...');
		var img = document.getElementById(subjectArea+':'+courseNumber+':'+classId+'x');
		img.src = (spanB.style.display=='none'?'images/expand_node_btn.gif':'images/collapse_node_btn.gif');
	}
	
<%
	SectioningDemoForm frm = (SectioningDemoForm) request.getAttribute("sectioningDemoForm");
%>

function getChoiceTR(subjectArea, courseNumber, chId) {
	return document.getElementById('ch_'+subjectArea+':'+courseNumber+':'+chId);
}

function getWaitlist(subjectArea,courseNumber,classId,chId) {
	return document.getElementById('chwl_'+subjectArea+':'+courseNumber+':'+classId+':'+chId);
}
	
function getSelect(subjectArea,courseNumber,classId,chId) {
	return document.getElementById('chs_'+subjectArea+':'+courseNumber+':'+classId+':'+chId);
}

function isWaitlisted(subjectArea,courseNumber,classId,chId) {
	var chbox = getWaitlist(subjectArea,courseNumber,classId,chId);
	return (chbox!=null && chbox.checked);
}
	
function isSelected(subjectArea,courseNumber,classId,chId) {
	var radio = getSelect(subjectArea,courseNumber,classId,chId);
	return (radio!=null && radio.checked);
}

function getSelectedChoice(subjectArea,courseNumber,classId) {
	return document.getElementById('cur_'+subjectArea+':'+courseNumber+':'+classId);
}

function updateMessage(subjectArea, courseNumber, classId) {
	var chbox = document.getElementsByName('chwl_'+subjectArea+':'+courseNumber+':'+classId);
	var radio = document.getElementsByName('chs_'+subjectArea+':'+courseNumber+':'+classId);
	var selected = "";
	var checked = "";
	var defSelected = (document.getElementById('def_'+subjectArea+':'+courseNumber+':'+classId)==null?null:document.getElementById('def_'+subjectArea+':'+courseNumber+':'+classId).value);
	
	for (var i=0;i<chbox.length;i++) {
		if (chbox[i].checked) {
			var timeStr = document.getElementById('time_'+subjectArea+':'+courseNumber+':'+chbox[i].value).innerHTML;
			var dateStr = document.getElementById('date_'+subjectArea+':'+courseNumber+':'+chbox[i].value).innerHTML;
			var insStr = document.getElementById('ins_'+subjectArea+':'+courseNumber+':'+chbox[i].value).innerHTML;
			var depStr = document.getElementById('dep_'+subjectArea+':'+courseNumber+':'+chbox[i].value).innerHTML;
			checked += "<tr><td><i><font color='orange'>Queue me for "+timeStr+" "+dateStr+(insStr==""?"":" "+insStr)+(depStr==""?"":" (requires "+depStr+")")+"</font></i></td></tr>";
		}
	}
	var noSelection = true;
	for (var i=0;i<radio.length;i++) {
		if (radio[i].checked) noSelection = false;
		if (radio[i].checked && (defSelected==null || defSelected!=radio[i].value)) {
			var timeStr = document.getElementById('time_'+subjectArea+':'+courseNumber+':'+radio[i].value).innerHTML;
			var dateStr = document.getElementById('date_'+subjectArea+':'+courseNumber+':'+radio[i].value).innerHTML;
			var insStr = document.getElementById('ins_'+subjectArea+':'+courseNumber+':'+radio[i].value).innerHTML;
			var depStr = document.getElementById('dep_'+subjectArea+':'+courseNumber+':'+radio[i].value).innerHTML;
			selected += "<tr><td><i><font color='blue'>Try to give me "+timeStr+" "+dateStr+(insStr==""?"":" "+insStr)+(depStr==""?"":" (requires "+depStr+")")+"</font></i></td></tr>";
		}
	}
	var spanA = document.getElementById(subjectArea+':'+courseNumber+':'+classId+'a');
	spanA.innerHTML=
		"<table border='0' cellspacing='0' cellpadding='3' style='border-left:40px solid transparent;'>"+
		(noSelection?"<tr><td><i><font color='gray'>This section is not available for the selected configuration.</font></i></td></tr>":selected)+
		checked+
		"</table>";
}

function selectionChanged(subjectArea, courseNumber, classId, chId) {
	choiceChanged(subjectArea, courseNumber, classId, chId, 1);
}

function waitlistChanged(subjectArea, courseNumber, classId, chId) {
	choiceChanged(subjectArea, courseNumber, classId, chId, 0);
}

function choiceChanged(subjectArea, courseNumber, classId, chId, type) {
	updateMessage(subjectArea, courseNumber, classId, chId);
	if (type==1) {
		var currentChoice = getSelectedChoice(subjectArea,courseNumber,classId);
		var lastChId = currentChoice.value;
		var select = getSelect(subjectArea, courseNumber, classId, chId);
		//alert('choiceChanged('+subjectArea+','+courseNumber+','+classId+','+chId+'), lastChId='+lastChId+', checked='+select.checked);
		if (lastChId!='' && chId!=lastChId && select!=null && select.checked) {
			//alert('unselect '+lastChId);
			choiceChangedX(subjectArea, courseNumber, classId, lastChId, 1);
			//alert('select '+chId);
			currentChoice.value=chId;
		} else if (lastChId!='' && chId==lastChId && (select==null || !select.checked)) {
			//alert('unselect '+lastChId);
			currentChoice.value='';
		} else if (lastChId=='' && select!=null && select.checked) {
			//alert('select '+chId);
			currentChoice.value=chId;
		}
	}
	
	choiceChangedX(subjectArea, courseNumber, classId, chId, type);
}

function choiceChangedX(subjectArea, courseNumber, classId, chId, type) {
<% frm.printOnChangeScript(out); %>
}
</script>	
<tiles:importAttribute />
<html:form action="/sectioningDemo">
<script language="javascript">displayLoading();</script>

	<logic:messagesPresent>
		<table width='93%' border='0' cellspacing='0' cellpadding='3'>
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
		</table>
	</logic:messagesPresent>

	<html:hidden property="nrRequests" />
	<html:hidden property="nrAltRequests" />
	<input type='hidden' name='reqIdx' value='-1'>
	<input type='hidden' name='op2' value=''>
	<table width='93%' border='0' cellspacing='0' cellpadding='3'>
		<tr>
			<td colspan="2" valign="middle">
				<tt:section-header>
					<tt:section-title>
						Student Selection
					</tt:section-title>
				<html:submit property="op" onclick="displayLoading();"
					styleClass="btn" accesskey="S" titleKey="title.submitStudentRequest">			
					<bean:message key="button.submitStudentRequest" />
				</html:submit>						
				</tt:section-header>					
			</td>
		</tr>
		<tr>
			<td width='1%' nowrap>Student Id:</td>
			<td>
				<html:hidden property="studentLoaded"/>
				<logic:equal name="sectioningDemoForm" property="studentLoaded" value="false">
					<html:text property="studentId" size="10"/>&nbsp;
					<html:submit property="op" value="Load" onclick="displayLoading();" styleClass="btn"/>
				</logic:equal>
				<logic:equal name="sectioningDemoForm" property="studentLoaded" value="true">
					<html:hidden property="studentId"/>
					<html:text property="studentId" size="10" disabled="true"/>&nbsp;
					<html:submit property="op" value="Unload" onclick="displayLoading();" styleClass="btn"/>&nbsp;
					<html:submit property="op" value="Save" onclick="displayLoading();" styleClass="btn"/>
				</logic:equal>
			</td>
		</tr>
	</table>
	<br>		
	<table width='93%' border='0' cellspacing='0' cellpadding='3'>
		<tr>
			<td colspan="7" valign="middle">
				<tt:section-header>
					<tt:section-title>Primary Course Requests</tt:section-title>
					<html:submit property="op" value="Add Request" title="Add Request (Alt+R)"
						onclick="displayLoading();" styleClass="btn" accesskey="R"/>
					<logic:equal name="sectioningDemoForm" property="nrAltRequests" value="0">
						<html:submit property="op" value="Add Alternative Request" title="Add Alternative Request (Alt+A)"
							onclick="displayLoading();" styleClass="btn" accesskey="A"/>
					</logic:equal>
					<%--
					<html:submit property="op" value="Add Alternative Request" title="Add Alternative Request (Alt+A)"
						onclick="displayLoading();" styleClass="btn" accesskey="A"/>
					<html:submit property="op" onclick="displayLoading();"
						styleClass="btn" accesskey="S" titleKey="title.submitStudentRequest">			
						<bean:message key="button.submitStudentRequest" />
					</html:submit>						
					--%>
				</tt:section-header>
			</td>
		</tr>

		<tr>
			<td>&nbsp;</td>
			<td><i>Type</i></td>
			<td><i>Course / Free Time</i></td>
			<td><i>Waitlist</i></td>
			<td><i>1st Alternative Course</i></td>
			<td><i>2nd Alternative Course</i></td>
			<td>&nbsp;</td>
		</tr>
		<nested:iterate property="requests" id="req" indexId="idx">
		
			<logic:equal name="sectioningDemoForm" property="nrRequests" value="<%=((Integer)idx).toString()%>">
				<tr>
					<td colspan='7'>&nbsp;</td>
				</tr>
				<tr>
					<td colspan='7'>
						<tt:section-header>
							<tt:section-title>Alternative Course Requests</tt:section-title>
							<html:submit property="op" value="Add Alternative Request" title="Add Alternative Request (Alt+A)"
								onclick="displayLoading();" styleClass="btn" accesskey="A"/>
						</tt:section-header>
					</td>
				</tr>
			</logic:equal>
			
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
				<td>
					<%	
						int index = ((Integer)idx).intValue()+1;
						if (index<=frm.getNrRequests()) {
							out.print(index+".");
						} else {
							out.print("A"+(index-frm.getNrRequests())+".");
						}
					 %>
				</td>
			
				<td nowrap>
					<nested:select property="type"
						onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
						onchange='<%="typeChanged(this.options[this.selectedIndex].value,"+idx+");"%>'
						>
						<html:optionsCollection name="req" property="types" value="id" label="value"/>
					</nested:select>
				</td>
				
				<bean:define id="type" name="req" property="type" />
				
				<td nowrap>
					<span id='span<%=idx%>CrsN' style='display:<%=SectioningDemoForm.sTypeNone.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
					<span id='span<%=idx%>CrsC' style='display:<%=SectioningDemoForm.sTypeCourse.equals(type)?"inline":"none"%>;'>
						<nested:select property="subjectArea"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							onchange="<%=\"updateCourseNumbers(this.options[this.selectedIndex].value,'requests[\"+idx+\"].courseNbr');\"%>"
							>
							<html:option value=""></html:option>
							<html:optionsCollection property="subjectAreas" value="id" label="value"/>
						</nested:select>
						<nested:select property="courseNbr"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							>
							<html:option value=""></html:option>
							<html:optionsCollection name="req" property="courseNumbers" value="uniqueId" label="courseNbr"/>
						</nested:select>
					</span>
					<span id='span<%=idx%>CrsF' style='display:<%=SectioningDemoForm.sTypeFreeTime.equals(type)?"inline":"none"%>;'>
						<nested:select property="freeTimePattern"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							onchange="<%=\"updateDaysAndTimes(this.options[this.selectedIndex].value,'requests[\"+idx+\"].freeTimeDay','requests[\"+idx+\"].freeTimeTime');\"%>"
							>
							<html:option value=""></html:option>
							<html:optionsCollection property="timePatterns" value="id" label="value"/>
						</nested:select>
						<nested:select property="freeTimeDay"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							>
							<html:option value=""></html:option>
							<html:optionsCollection name="req" property="freeTimeDays" value="id" label="value"/>
						</nested:select>
						<nested:select property="freeTimeTime"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							>
							<html:option value=""></html:option>
							<html:optionsCollection name="req" property="freeTimeTimes" value="id" label="value"/>
						</nested:select>
					</span>
				</td>
				
				<td align='center'>
					<span id='span<%=idx%>WN' style='display:<%=SectioningDemoForm.sTypeNone.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
					<span id='span<%=idx%>WC' style='display:<%=SectioningDemoForm.sTypeCourse.equals(type)?"inline":"none"%>;'>
						<nested:checkbox property="wait"
							onclick='<%="disableAlternates(this,"+idx+");"%>'
						/>
					</span>
					<span id='span<%=idx%>WF' style='display:<%=SectioningDemoForm.sTypeFreeTime.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
				</td>
				
				<td nowrap>
					<span id='span<%=idx%>A1N' style='display:<%=SectioningDemoForm.sTypeNone.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
					<span id='span<%=idx%>A1C' style='display:<%=SectioningDemoForm.sTypeCourse.equals(type)?"inline":"none"%>;'>
						<nested:select property="alt1SubjectArea"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							onchange="<%=\"updateCourseNumbers(this.options[this.selectedIndex].value,'requests[\"+idx+\"].alt1CourseNbr');\"%>"
							>
							<html:option value=""></html:option>
							<html:optionsCollection property="subjectAreas" value="id" label="value"/>
						</nested:select>
						<nested:select property="alt1CourseNbr"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							>
							<html:option value=""></html:option>
							<html:optionsCollection name="req" property="alt1CourseNumbers" value="uniqueId" label="courseNbr"/>
						</nested:select>
					</span>
					<span id='span<%=idx%>A1F' style='display:<%=SectioningDemoForm.sTypeFreeTime.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
				</td>
				
				<td nowrap>
					<span id='span<%=idx%>A2N' style='display:<%=SectioningDemoForm.sTypeNone.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
					<span id='span<%=idx%>A2C' style='display:<%=SectioningDemoForm.sTypeCourse.equals(type)?"inline":"none"%>;'>
						<nested:select property="alt2SubjectArea"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							onchange="<%=\"updateCourseNumbers(this.options[this.selectedIndex].value,'requests[\"+idx+\"].alt2CourseNbr');\"%>"
							>
							<html:option value=""></html:option>
							<html:optionsCollection property="subjectAreas" value="id" label="value"/>
						</nested:select>
						<nested:select property="alt2CourseNbr"
							onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);"
							>
							<html:option value=""></html:option>
							<html:optionsCollection name="req" property="alt2CourseNumbers" value="uniqueId" label="courseNbr"/>
						</nested:select>
					</span>
					<span id='span<%=idx%>A2F' style='display:<%=SectioningDemoForm.sTypeFreeTime.equals(type)?"inline":"none"%>;'>
						&nbsp;
					</span>
				</td>
				
				<td nowrap>
					<logic:notEqual name="idx" value="0">
						<img src='images/arrow_u.gif' border='0' title='Move Up This Request' onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].reqIdx.value='<%=idx%>';document.forms[0].op2.value='Move Up';document.forms[0].submit();"
						>
					</logic:notEqual>
					<logic:equal name="idx" value="0">
						<img src='images/clear16.gif' border='0' width='22'>
					</logic:equal>
					
					<logic:notEqual name="sectioningDemoForm" property="nrAllRequests" value="<%=String.valueOf(((Integer)idx).intValue()+1)%>">
						<img src='images/arrow_d.gif' border='0' title='Move Down This Request' onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].reqIdx.value='<%=idx%>';document.forms[0].op2.value='Move Down';document.forms[0].submit();"
						>
					</logic:notEqual>
					<logic:equal name="sectioningDemoForm" property="nrAllRequests" value="<%=String.valueOf(((Integer)idx).intValue()+1)%>">
						<img src='images/clear16.gif' border='0' width='22'>
					</logic:equal>
					
					<img src='images/Delete16.gif' border='0' title='Delete This Request' onmouseover="this.style.cursor='hand';this.style.cursor='pointer';" width='22'
						onclick="document.forms[0].reqIdx.value='<%=idx%>';document.forms[0].op2.value='Delete';document.forms[0].submit();"
					>
				</td>

				<script>
					disableAlternates(document.getElementsByName('requests[<%=idx%>].wait')[0],<%=idx%>);
				</script>
				
			</tr>
		</nested:iterate>
		
		<nested:notEmpty property="courseAssignments">
			<tr>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td colspan='7'>
					<tt:section-header>
						<tt:section-title>Solution</tt:section-title>
						<nested:notEmpty property="requestFile">
							<input class="btn" type="button" onclick="window.open('<%=frm.getRequestFile()%>','request.xml','width=1000,height=600,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no');" title='Show request XML file' value='Request XML'>
						</nested:notEmpty>
						<nested:notEmpty property="responseFile">
							<input class="btn" type="button" onclick="window.open('<%=frm.getResponseFile()%>','response.xml','width=1000,height=600,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no');" title='Show response XML file' value='Response XML'>
						</nested:notEmpty>
						<html:submit property="op" onclick="displayLoading();"
							styleClass="btn" accesskey="C" title="Clear Schedule (Alt+C)" value="Clear"/>			
						<%--
						<html:submit property="op" onclick="displayLoading();"
							styleClass="btn" accesskey="S" titleKey="title.submitStudentRequest">			
							<bean:message key="button.submitStudentRequest" />
						</html:submit>
						--%>
					</tt:section-header>
				</td>
			</tr>
				<nested:iterate property="courseAssignments" id="course" indexId="idx">
					<tr>
						<td valign='top'>
							<%=((Number)idx).intValue()+1%>.
						</td>
						<nested:define id="subjectArea" name="course" property="subjectArea"/>
						<nested:define id="courseNumber" name="course" property="courseNumber"/>
						<nested:equal property="subjectArea" value="Free">
							<td>Free Time</td>
							<td colspan='2'>
								<nested:iterate property="classAssignments" id="clazz">
									<nested:write property="time"/>
								</nested:iterate>
							</td>
						</nested:equal>
						<nested:notEqual property="subjectArea" value="Free">
							<td colspan='6'>
								<nested:write property="subjectArea"/>
								<nested:write property="courseNumber"/>
								<table border='0' cellspacing='0' cellpadding='3' style='border-left:20px solid transparent;'>
								<nested:empty property="classAssignments">
									<tr><td>Request for course <nested:write property="subjectArea"/> <nested:write property="courseNumber"/> will be wait listed.</td></tr>
								</nested:empty>
								<nested:notEmpty property="classAssignments">
								<nested:iterate property="classAssignments" id="clazz">
									<nested:define id="ind" name="clazz" property="indent"/>
									<nested:empty property="choices">
										<tr><td style='border-left:<%=30*((Number)ind).intValue()%>px solid transparent;'>
											<nested:write property="name"/>
											<nested:write property="time"/>
											<nested:write property="date"/>
											<nested:write property="location"/>
											<nested:write property="instructor"/>
										</td></tr>
									</nested:empty>
									<nested:notEmpty property="choices">
										<nested:define id="classId" name="clazz" property="id"/>
										<tr><td style='border-left:<%=30*((Number)ind).intValue()%>px solid transparent;'>
										<span 
											onclick="classClicked(this, '<%=subjectArea%>', '<%=courseNumber%>', '<%=classId%>');"
											onmouseover="this.style.cursor='hand';this.style.cursor='pointer';this.style.backgroundColor='rgb(223,231,242)';" 
											onmouseout="this.style.backgroundColor='transparent';"
											title='Show choices...'>
												<img src='images/expand_node_btn.gif' border='0' id='<%=subjectArea%>:<%=courseNumber%>:<%=classId%>x'>
												<nested:write property="name"/>
												<nested:write property="time"/>
												<nested:write property="date"/>
												<nested:write property="location"/>
												<nested:write property="instructor"/>
										</span>
										<span id='<%=subjectArea%>:<%=courseNumber%>:<%=classId%>a' style='display:inline'></span>
										<span id='<%=subjectArea%>:<%=courseNumber%>:<%=classId%>b' style='display:none'>
											<table border='0' cellspacing='0' cellpadding='3' style='border-left:40px solid transparent;'>
												<tr>
													<td><i>Sel</i></td>
													<td><i>Que</i></td>
													<td><i>Time</i></td>
													<td><i>Date</i></td>
													<td><i>Instructor</i></td>
													<td><i>Requires</i></td>
												</tr>
												<input 
													type='hidden' 
													id='cur_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>' 
													name='cur_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>' 
													value=''>
												<nested:iterate property="choices" id="choice">
													<nested:define id="chId" name="choice" property="id"/>
													<nested:define id="disp" name="choice" property="display"/>
													<tr style='display:<%=disp%>' id='ch_<%=subjectArea%>:<%=courseNumber%>:<%=chId%>'>
														<td align='center'>
															<nested:equal name="choice" property="available" value="true">
																<nested:define id="sel" name="choice" property="selected"/>
																<nested:define id="dis" name="choice" property="selectDisabled"/>
																<nested:equal name="choice" property="default" value="true">
																	<input type='hidden' id='def_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>' name='def_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>' value='<%=chId%>'>
																</nested:equal>
																<nested:equal name="choice" property="selected" value="true">
																	<script>
																		getSelectedChoice('<%=subjectArea%>','<%=courseNumber%>','<%=classId%>').value = '<%=chId%>';
																	</script>
																</nested:equal>
																<input type='radio' 
																	name='chs_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>' 
																	id='chs_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>:<%=chId%>' 
																	value='<%=chId%>' 
																	<%=Boolean.TRUE.equals(sel)?"checked":""%> 
																	<%=Boolean.TRUE.equals(dis)?"disabled":""%> 
																	onchange="selectionChanged('<%=subjectArea%>','<%=courseNumber%>','<%=classId%>','<%=chId%>');" 
																	/>
															</nested:equal>
														</td>
														<td align='center'>
															<nested:define id="sel" name="choice" property="waitlisted"/>
															<nested:define id="dis" name="choice" property="waitDisabled"/>
															<input type='checkbox' 
																name='chwl_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>' 
																id='chwl_<%=subjectArea%>:<%=courseNumber%>:<%=classId%>:<%=chId%>' 
																value='<%=chId%>'
																<%=Boolean.TRUE.equals(sel)?"checked":""%>
																<%=Boolean.TRUE.equals(dis)?"disabled":""%> 
																onchange="waitlistChanged('<%=subjectArea%>','<%=courseNumber%>','<%=classId%>','<%=chId%>');" 
																/>
														</td>
														<td nowrap id='time_<%=subjectArea%>:<%=courseNumber%>:<%=chId%>'><nested:write name="choice" property="time"/></td>
														<td nowrap id='date_<%=subjectArea%>:<%=courseNumber%>:<%=chId%>'><nested:write name="choice" property="date"/></td>
														<td nowrap id='ins_<%=subjectArea%>:<%=courseNumber%>:<%=chId%>'><nested:write name="choice" property="instructor"/></td>
														<td nowrap id='dep_<%=subjectArea%>:<%=courseNumber%>:<%=chId%>'><nested:write name="choice" property="parent"/></td>
													</tr>
												</nested:iterate>
											</table>
										</span>
										</td></tr>
									</nested:notEmpty>
								</nested:iterate>
								<nested:iterate property="classAssignments" id="clazz">
									<nested:define id="classId" name="clazz" property="id"/>
									<nested:notEmpty property="choices">
										<script>updateMessage('<%=subjectArea%>','<%=courseNumber%>','<%=classId%>');</script>
									</nested:notEmpty>
								</nested:iterate>
								</nested:notEmpty>
								</table>
							</td>
						</nested:notEqual>
					</tr>
			</nested:iterate>
		</nested:notEmpty>
		
		<nested:notEmpty property="messages">
			<tr>
				<td colspan='7'>
					&nbsp;
				</td>
			</tr>
			<tr>
				<td colspan='7'>
					<tt:section-title>Messages</tt:section-title>
				</td>
			</tr>
			<tr>
				<td colspan='7'>
					<ul>
						<nested:iterate property="messages" id="message">
							<nested:write property="html" filter="false"/>
						</nested:iterate>
					</ul>
				</td>
			</tr>
		</nested:notEmpty>

		<tr>
			<td colspan="7" class="WelcomeRowHead">&nbsp;</td>
		</tr>
		<tr>
			<td colspan="7" align="right">
				<%--
				<html:submit property="op" value="Add Request" title="Add Request (Alt+R)"
					onclick="displayLoading();" styleClass="btn" accesskey="R"/>
				<html:submit property="op" value="Add Alternative Request" title="Add Alternative Request (Alt+A)"
					onclick="displayLoading();" styleClass="btn" accesskey="A"/>
				--%>
				<html:submit property="op" onclick="displayLoading();"
					styleClass="btn" accesskey="S" titleKey="title.submitStudentRequest">			
					<bean:message key="button.submitStudentRequest" />
				</html:submit>						
			</td>
		</tr>
	</table>

	<script language="javascript">displayElement('loading', false);</script>	
</html:form>
