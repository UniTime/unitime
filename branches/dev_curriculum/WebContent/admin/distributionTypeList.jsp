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
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %>
<%@ page import="org.unitime.timetable.model.Session" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.commons.web.WebTable" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>

<table width="100%" border="0" cellspacing="0" cellpadding="3">
<%
    WebTable webTable = new WebTable( 10,
    "Distribution Types",
    "distributionTypeList.do?ord=%%",
    new String[] {"Id", "Reference", "Abbreviation", "Name", "Type", "Allow Instructor Preference", "Sequencing Required", "Allow Preferences", "Departments", "Description"},
    new String[] {"left", "left", "left", "left", "center", "center", "center", "center", "left", "left"}, 
    new boolean[] {true, true, true, true, true, true, true, true, true, true} );
   	Long sessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getSessionId();
%>    

<logic:iterate name="distributionTypeListForm" property="refTableEntries" id="distType" >
<%
WebTable.setOrder(session,"DistributionTypeList.ord",request.getParameter("ord"),1);
DecimalFormat df5 = new DecimalFormat("####0");
org.unitime.timetable.model.DistributionType d = (org.unitime.timetable.model.DistributionType) distType;
String allowPref = null;
if ("".equals(d.getAllowedPref())) {
	allowPref = "<i>None</i>";
} else if ("P43210R".equals(d.getAllowedPref())) {
	allowPref = "<i>All</i>";
} else {
	for (Enumeration e=PreferenceLevel.elements();e.hasMoreElements();) {
		PreferenceLevel p = (PreferenceLevel)e.nextElement();
		if (d.getAllowedPref().indexOf(PreferenceLevel.prolog2char(p.getPrefProlog()))<0) continue;
		if (allowPref==null)
			allowPref="";
		else
			allowPref+="<br>";
		if (PreferenceLevel.sNeutral.equals(p.getPrefProlog()))
			allowPref += p.getPrefName();
		else
			allowPref += "<span style='color:"+p.prefcolor()+";'>"+p.getPrefName().replaceAll(" ","&nbsp;")+"</span>";
	}
}
String deptStr = "";
String deptCmp = "";
for (Iterator i=d.getDepartments(sessionId).iterator();i.hasNext();) {
	Department x = (Department)i.next();
	deptStr += x.getManagingDeptAbbv().trim();
	deptCmp += x.getDeptCode();
	if (i.hasNext()) { deptStr += ", "; deptCmp += ","; }
}
webTable.addLine(
	"onClick=\"document.location='distributionTypeEdit.do?id="+d.getUniqueId()+"';\"",
	new String[] {
		d.getRequirementId().toString(),
		d.getReference(),
		d.getAbbreviation(),
		d.getLabel(),
		d.isExamPref().booleanValue()?"Examination":"Course",
		d.isExamPref().booleanValue()?"N/A":d.isInstructorPref().booleanValue()?"Yes":"No",
		d.isSequencingRequired()?"Yes":"No",
		allowPref,
		(deptStr.length()==0?"<i>All</i>":deptStr),
		d.getDescr()
	}, 
	new Comparable[] {
		d.getRequirementId(),
		d.getReference(),
		d.getAbbreviation(),
		d.getLabel(),
		new Integer(d.isExamPref().booleanValue()?1:0),
		new Integer(d.isInstructorPref().booleanValue()?1:0),
		new Integer(d.isSequencingRequired()?1:0),
		null,
		deptCmp,
		d.getDescr()
	},null);
%>

</logic:iterate> <%-- end interate --%>
<%	out.println( webTable.printTable(WebTable.getOrder(session,"DistributionTypeList.ord")) ); %>

<%-- print out the add link --%>
<br><br>
<tr>
<td colspan="10" align="center">
<%--
	<html:form action="distributionTypeEdit">
		<html:hidden property="do" value="addDistributionType"/>
		<html:submit value="Add Distribution Type"/>
	</html:form>
--%>

</table>

