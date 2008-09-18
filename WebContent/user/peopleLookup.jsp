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
<%@page import="org.unitime.commons.web.Web"%>
<%@page import="org.unitime.commons.User"%>
<%@page import="org.unitime.timetable.util.Constants"%>
<%
	User user = Web.getUser(session);
	if (user==null || user.getRole()==null) throw new Exception("Access denied.");
	String q = request.getParameter("query"); 
	if (q==null) q=""; else q=q.trim();
	String sid = request.getParameter("session");
	if (sid==null) {
		Object x = user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (x!=null) sid = x.toString();
	}
	boolean submit = "true".equals(request.getParameter("submit"));
 %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>People Lookup</title>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css">
  </head>
  <body class="bodyMain">
  	<form onSubmit="doQuery(query.value); return false;">
  	<p align='center'>
    <table border='0' width='750'>
    	<tr>
    		<td style='border-bottom:1px black solid;' align='center'>
    			<input type='text' name='query' size='95' id='query'>&nbsp;
    			<input type='button' value='Search' onClick="doQuery(query.value);">&nbsp;
    			<input type='button' value='Close' onClick="window.close();">
    		</td>
    	</tr>
    	<tr><td id='results'>
    		<i>Type in a name and click search...</i>
    	</td></tr>
    </table>
    </p>
    </form>
  </body>
  <script language="JavaScript">
  function doQuery(query) {
  	var out = document.getElementById('results');
  	if (query == "") {
  		out.innerHTML = "<i>No query provided.</i>";
  		return;
  	}
  	out.innerHTML = "<i>Looking for '"+query+"'...</i>";

	// Request initialization
	if (window.XMLHttpRequest) req = new XMLHttpRequest();
	else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );
	
	// Response
	req.onreadystatechange = function() {
		out.innerHTML = "<i>Response recieved.</i>";
		try {
			if (req.readyState == 4 && req.status == 200) {
				// Response
				var xmlDoc = req.responseXML;
				if (xmlDoc && xmlDoc.documentElement) {
					if (xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						var count = xmlDoc.documentElement.childNodes.length;
						var table = "<table width='100%' border='0' cellpadding='2' cellspacing='0'>"+
							"<tr><td><i>First Name</i></td><td><i>Middle Name</i></td><td><i>Last Name</i></td>"+
							"<td><i>Email</i></td><td><i>Phone</i></td><td><i>Department</i></td><td><i>Position</i></td><td><i>Source</i></td></tr>";
						for (i=0; i<count; i++) {
							var id = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var fname = xmlDoc.documentElement.childNodes[i].getAttribute("fname");
							var mname = xmlDoc.documentElement.childNodes[i].getAttribute("mname");
							var lname = xmlDoc.documentElement.childNodes[i].getAttribute("lname");
							var email = xmlDoc.documentElement.childNodes[i].getAttribute("email");
							var phone = xmlDoc.documentElement.childNodes[i].getAttribute("phone");
							var dept = xmlDoc.documentElement.childNodes[i].getAttribute("dept");
							var pos = xmlDoc.documentElement.childNodes[i].getAttribute("pos");
							var source = xmlDoc.documentElement.childNodes[i].getAttribute("source");
							table += "<tr "+
								"onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';this.style.backgroundColor='rgb(223,231,242)';\" "+
								"onMouseOut=\"this.style.backgroundColor='transparent';\" "+
								"onClick=\"onClose('"+(id==null?"":id)+"','"+(fname==null?"":fname)+"','"+(mname==null?"":mname)+"','"+(lname==null?"":lname)+"','"+(email==null?"":email)+"','"+(phone==null?"":phone)+"','"+(dept==null?"":dept)+"','"+(pos==null?"":pos)+"'); window.close();\" "+
								"><td>"+(fname==null?"":fname)+"</td><td>"+(mname==null?"":mname)+"</td><td>"+(lname==null?"":lname)+"</td>"+
								"<td>"+(email==null?"":email)+"</td><td>"+(phone==null?"":phone)+"</td>"+
								"<td>"+(dept==null?"":dept)+"</td><td>"+(pos==null?"":pos)+"</td>"+
								"<td>"+(source==null?"":source)+"</td></tr>";
						}
						table += "</table>";
						out.innerHTML = table;
					} else {
						out.innerHTML = "<i>No matching record found for '"+query+"'.</i>";
					}
				} else {
					out.innerHTML = "<i><font color='red'>Response recieved, no data provided.</font></i>";
				}
			}
		} catch(e) {
			out.innerHTML = "<i><font color='red'>Error: "+e+"</font></i>";
		}
	}
	
	// Request
	var vars = "query="+query+"<%=sid==null?"":"&session="+sid%>";
	req.open( "POST", "../peopleLookupAjax.do", true );
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	req.setRequestHeader("Content-Length", vars.length);
	req.send(vars);
  }
  function onClose(uid,fname, mname, lname, email, phone, dept, pos) {
  	if (window.opener==null) return;
  	var x = window.opener.document.getElementById('uid');
  	if (x!=null) x.value = uid;
  	x = window.opener.document.getElementById('fname');
  	if (x!=null) x.value = fname;
  	x = window.opener.document.getElementById('mname');
  	if (x!=null) x.value = mname;
  	x = window.opener.document.getElementById('lname');
  	if (x!=null) x.value = lname;
  	x = window.opener.document.getElementById('email');
  	if (x!=null) x.value = email;
  	x = window.opener.document.getElementById('phone');
  	if (x!=null) x.value = phone;
  	x = window.opener.document.getElementById('dept');
  	if (x!=null) x.value = dept;
  	x = window.opener.document.getElementById('pos');
  	if (x!=null) x.value = pos;
  	<%if (submit) {%>
  	window.opener.document.forms[0].submit();
  	<%}%>
  }
  var qObj = document.getElementById('query');
  <% if (q.length()>0) { %> qObj.value='<%=q%>'; doQuery('<%=q%>'); <% } %>
  	qObj.focus();
  </script>
</html>
