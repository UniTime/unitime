/*
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
*/

function tpGenVariables(tpName, nrTimes, nrDays, pref, prefTable, prefColors, selections, defPreference, prefCheck, prefNames) {
	document.writeln("<INPUT id='"+tpName+"_reqSelect' type='hidden' value='"+defPreference+"' name='"+tpName+"_reqSelect'>");
	document.writeln("<INPUT id='"+tpName+"_nrTimes' type='hidden' value='"+nrTimes+"' name='"+tpName+"_nrTimes'>");
	document.writeln("<INPUT id='"+tpName+"_nrDays' type='hidden' value='"+nrDays+"' name='"+tpName+"_nrDays'>");
	document.writeln("<INPUT id='"+tpName+"_nrSelections' type='hidden' value='"+(selections==null?1:selections.length)+"' name='"+tpName+"_nrSelections'>");
	var reqUsed=0;
	for (var t=0;t<nrTimes;t++)
		for (var d=0;d<nrDays;d++) {
			document.writeln("<INPUT id='"+tpName+"_req_"+d+"_"+t+"' name='"+tpName+"_req_"+d+"_"+t+"' type='hidden' value='"+pref[d][t]+"'>");
			if (pref[d][t]=='R') reqUsed=1;
		}
	document.writeln("<INPUT id='"+tpName+"_reqUsed' type='hidden' value='"+reqUsed+"' name='"+tpName+"_reqUsed'>");
	document.writeln("<script language='javascript'>");
	document.writeln("function fn_"+tpName+"_pref2color(pref) {");
	for (var i=0;i<prefTable.length;i++)
		document.writeln("if (pref=='"+prefTable[i]+"') return '"+prefColors[i]+"';");
	document.writeln("return 'rgb(240,240,240)';");
	document.writeln("}");
	document.writeln("function fn_"+tpName+"_pref2name(pref) {");
	for (var i=0;i<prefTable.length;i++)
		document.writeln("if (pref=='"+prefTable[i]+"') return '"+prefNames[i]+"';");
	document.writeln("return 'rgb(240,240,240)';");
	document.writeln("}");
	document.writeln("function fn_"+tpName+"_prefCheck(pref) {");
	if (prefCheck!=null) document.writeln(prefCheck);
	document.writeln("}");
	document.writeln("</script>");
}
function tpGetBorder(tpName, time, day, highlight, selected) {
	var b = (highlight==null?null:highlight[day][time]);
	if (b!=null) return b;
	if (selected) return "rgb(0,0,242) 1px solid";
	return "rgb(100,100,100) 1px solid";
}
function tpIsFieldEditable(tpName, editable, time, day) {
	return (editable==null?true:editable[day][time]);
}
function tpIsTimeEditable(tpName, editable, time, minDay, maxDay) {
	for (var d=minDay;d<=maxDay;d++)
		if (tpIsFieldEditable(tpName, editable, time,d)) return 1;
	return 0;
}
function tpIsDayEditable(tpName, editable, day, minTime, maxTime) {
	for (var t=minTime;t<=maxTime;t++)
		if (tpIsFieldEditable(tpName, editable, t, day)) return 1;
	return 0;
}
function tpIsBlockEditable(tpName, editable, minTime, maxTime, minDay, maxDay) {
	for (var t=minTime;t<=maxTime;t++)
		for (var d=minDay;d<=maxDay;d++)
			if (tpIsFieldEditable(tpName, editable, t,d)) return 1;
	return 0;
}
function tpIsEditable(tpName, editable) {
	for (var d=0;d<tpGetNrDays(tpName);d++)
		for (var t=0;t<tpGetNrTimes(tpName);t++)
			if (tpIsFieldEditable(tpName, editable, t, d)) return 1;
	return 0;
}
function tpGetNrDays(tpName) {
	return document.getElementById(tpName+"_nrDays").value;
}
function tpGetNrTimes(tpName) {
	return document.getElementById(tpName+"_nrTimes").value;
}
function tpGetNrSelections(tpName) {
	return document.getElementById(tpName+"_nrSelections").value;
}
function tpGetCurrentPreference(tpName) {
	return document.getElementById(tpName+"_reqSelect").value;
}
function tpGetPreference(tpName, time, day) {
	return document.getElementById(tpName+"_req_"+day+"_"+time).value;
}
function tpSetPreferenceNoCheck(tpName, time, day, pref) {
	document.getElementById(tpName+"_req_"+day+"_"+time).value=pref;
	for (var i=0;i<tpGetNrSelections(tpName);i++) {
		if (document.getElementById(tpName+"_"+day+"_"+time+"_"+i)!=null)
			document.getElementById(tpName+"_"+day+"_"+time+"_"+i).style.backgroundColor=tpPref2Color(tpName, pref);
	}
}
function tpCheckRequired(tpName, time, day, pref) {
	var reqUsed = document.getElementById(tpName+"_reqUsed").value;
	if (reqUsed=='1' && pref!='R' && pref!='0') {
		for (var xt=0;xt<tpGetNrTimes(tpName);xt++) {
			for (var xd=0;xd<tpGetNrDays(tpName);xd++) {
				if (tpGetPreference(tpName,xt,xd)!='@')
					tpSetPreferenceNoCheck(tpName, xt, xd, '0');
			}
		}
		document.getElementById(tpName+"_reqUsed").value='0';
	}
	if (reqUsed=='0' && pref=='R') {
		for (var xt=0;xt<tpGetNrTimes(tpName);xt++) {
			for (var xd=0;xd<tpGetNrDays(tpName);xd++) {
				if (tpGetPreference(tpName,xt,xd)!='@')
					tpSetPreferenceNoCheck(tpName, xt, xd, '0');
			}
		}
		document.getElementById(tpName+"_reqUsed").value='1';
	}
}
function tpSetPreference(tpName, time, day, pref) {
	tpCheckRequired(tpName, time, day, pref);
	tpSetPreferenceNoCheck(tpName, time, day, pref);
}
function tpPref2Color(tpName, pref) {
	return eval("fn_"+tpName+"_pref2color('"+pref+"');");
}
function tpPref2Name(tpName, pref) {
	return eval("fn_"+tpName+"_pref2name('"+pref+"');");
}
function tpTimeDaySelected(tpName, time, day) {
	tpSetPreference(tpName, time, day, tpGetCurrentPreference(tpName));
}
 
function tpGenTimeHeader(tpName, editable, time, startTime, endTime, minDay, maxDay) {
	if (tpIsTimeEditable(tpName, editable, time, minDay, maxDay)) {
 		var onclick = "";
		for (var d=minDay;d<=maxDay;d++) {
			if (tpIsFieldEditable(tpName, editable, time, d))
				onclick += "tpSetPreference('"+tpName+"', "+time+", "+d+",tpGetCurrentPreference('"+tpName+"'));";
		}
 		document.writeln("<th width='30' height='20' "+
 	  	"style=\"border:rgb(100,100,100) 1px solid;background-color:rgb(240,240,240);\" "+
 			"onmouseover=\"this.style.border='rgb(0,0,242) 1px solid';this.style.cursor='pointer';\" "+
 			"onmouseout=\"this.style.border='rgb(100,100,100) 1px solid';\" "+
 			"onclick=\""+onclick+"\">"+
 			"<font size=1>"+startTime+"<br><font color=gray>"+endTime+"</font></font>"+
 			"</th>");
	} else {
 		document.writeln("<th width='30' height='20' "+
 	  	"style=\"border:rgb(100,100,100) 1px solid;background-color:rgb(240,240,240);\" "+
 			"<font size=1>"+startTime+"<br><font color=gray>"+endTime+"</font></font>"+
 			"</th>");
	}
}
  
function tpGenTimeHeadersHorizontal(tpName, editable, startTimes, endTimes, minTime, maxTime, minDay, maxDay) {
	document.writeln("<TR>");
	if (tpIsBlockEditable(tpName,editable,minTime,maxTime,minDay,maxDay)) {
		var onclick = "";
		for (var t=minTime;t<=maxTime;t++) {
			for (var d=minDay;d<=maxDay;d++) {
				if (tpIsFieldEditable(tpName, editable, t, d))
					onclick += "tpSetPreference('"+tpName+"', "+t+", "+d+",tpGetCurrentPreference('"+tpName+"'));";
			}
		}
	 	document.writeln("<TH "+
 				"style=\"border:white 1px solid;\" "+
 				"onmouseover=\"this.style.border='rgb(0,0,242) 1px solid';this.style.cursor='pointer';\" "+
 				"onmouseout=\"this.style.border='white 1px solid';\" "+
	 			"onclick=\""+onclick+"\" "+
 				"align=right>from:<BR><FONT color=gray>to:</FONT></TH>");
 	} else {
	 	document.writeln("<TH align=right>from:<BR><FONT color=gray>to:</FONT></TH>");
 	}
 	for (var t=minTime;t<=maxTime;t++) {
 		tpGenTimeHeader(tpName,editable,t,startTimes[t],endTimes[t], minDay, maxDay);
 	}
 	document.writeln("</TR>");
}
  
function tpGenDayHeader(tpName, editable, day, text, minTime, maxTime) {
	if (tpIsDayEditable(tpName,editable,day, minTime, maxTime)) {
 		var onclick = "";
		for (var t=minTime;t<=maxTime;t++) {
			if (tpIsFieldEditable(tpName, editable, t, day))
				onclick += "tpSetPreference('"+tpName+"', "+t+", "+day+",tpGetCurrentPreference('"+tpName+"'));";
		}
		document.writeln("<th width='30' height='20' "+
			"style=\"border:rgb(100,100,100) 1px solid;background-color:rgb(240,240,240);\" "+
 			"onmouseover=\"this.style.border='rgb(0,0,242) 1px solid';this.style.cursor='pointer';\" "+
 			"onmouseout=\"this.style.border='rgb(100,100,100) 1px solid';\" "+
 			"onclick=\""+onclick+"\">"+
 			"<font size=1>"+text+"</font>"+
 			"</th>");
 	} else {
  	document.writeln("<th width='30' height='20' "+
 		  "style=\"border:rgb(100,100,100) 1px solid;background-color:rgb(240,240,240);\" "+
 			"<font size=1>"+text+"</font>"+
 			"</th>");
 	}
}
  
function tpGenDayHeadersHorizontal(tpName, editable, days, minTime, maxTime, minDay, maxDay) {
 	document.writeln("<TR>");
 	if (tpIsBlockEditable(tpName,editable,minTime,maxTime,minDay,maxDay)) {
		var onclick = "";
		for (var t=minTime;t<=maxTime;t++) {
			for (var d=minDay;d<=maxDay;d++) {
				if (tpIsFieldEditable(tpName, editable, t, d))
					onclick += "tpSetPreference('"+tpName+"', "+t+", "+d+",tpGetCurrentPreference('"+tpName+"'));";
			}
		}
	 	document.writeln("<TH "+
 				"style=\"border:white 1px solid;\" "+
 				"onmouseover=\"this.style.border='rgb(0,0,242) 1px solid';this.style.cursor='pointer';\" "+
 				"onmouseout=\"this.style.border='white 1px solid';\" "+
	 			"onclick=\""+onclick+"\" "+
				"align=right>from:<BR><FONT color=gray>to:</FONT></TH>");
	} else {
	 	document.writeln("<TH align=right>from:<BR><FONT color=gray>to:</FONT></TH>");
	}
 	for (var d=minDay;d<=maxDay;d++) {
 		tpGenDayHeader(tpName,editable,d,days[d], minTime, maxTime);
 	}
 	document.writeln("</TR>");
}
 
function tpGenField(tpName, editable, time, day, text, highlight, sel) {
	border = tpGetBorder(tpName, time, day, highlight, false);
 	borderSelected = tpGetBorder(tpName, time, day, highlight, true);
 	if (tpIsFieldEditable(tpName, editable, time, day)) {
  	document.writeln("<td width='30' height='20' id='"+tpName+"_"+day+"_"+time+"_"+sel+"' "+
 		  "style=\"border:"+border+";background-color:"+tpPref2Color(tpName, tpGetPreference(tpName, time,day))+";\" "+
 			"onmouseover=\"this.style.border='"+borderSelected+"';this.style.cursor='pointer';\" "+
 			"onmouseout=\"this.style.border='"+border+"';\" "+
 			"onclick=\"tpTimeDaySelected('"+tpName+"',"+time+","+day+");\">"+
 			"<font size=1>"+(text==null?"&nbsp;":text)+"</font>"+
 			"</td>");
 	} else {
  	document.writeln("<td width='30' height='20' id='"+tpName+"_"+day+"_"+time+"_"+sel+"' title='"+tpPref2Name(tpName, tpGetPreference(tpName, time,day))+"'"+
 		  "style=\"border:"+border+";background-color:"+tpPref2Color(tpName, tpGetPreference(tpName, time,day))+";\" "+
 			"<font size=1>"+(text==null?"&nbsp;":text)+"</font>"+
 			"</td>");
 	}
}
 
function tpGenRowHorizontal(tpName, editable, day, dayText, text, highlight, sel, minTime, maxTime) {
 	document.writeln("<tr>");
 	tpGenDayHeader(tpName, editable, day, dayText, minTime, maxTime);
 	for (var t=minTime;t<=maxTime;t++) {
 		tpGenField(tpName,editable,t,day,(text==null?null:text[t]), highlight, sel);
 	}
 	document.writeln("</tr>");
}
	
function tpGenRowVertical(tpName, editable, time, startTime, endTime, text, highlight, sel, minDay, maxDay) {
	document.writeln("<tr>");
 	tpGenTimeHeader(tpName, editable, time, startTime, endTime, minDay, maxDay);
 	for (var d=minDay;d<=maxDay;d++) {
 		tpGenField(tpName, editable,time,d,(text==null?null:text[d][t]), highlight, sel);
 	}
 	document.writeln("</tr>");
}

function tpGenTable(tpName, horizontal, editable, days, startTimes, endTimes, text, highlight, sel, minTime, maxTime, minDay, maxDay) {
	document.writeln("<table style='font-size:10px;' cellSpacing='0' cellPadding='1' border='0'>");
	if (horizontal) {
		tpGenTimeHeadersHorizontal(tpName, editable, startTimes, endTimes, minTime, maxTime, minDay, maxDay);
		for (var d=minDay;d<=maxDay;d++)
			tpGenRowHorizontal(tpName, editable,d,days[d],(text==null?null:text[d]), highlight, sel, minTime, maxTime);
	} else {
		tpGenDayHeadersHorizontal(tpName, editable, days, minTime, maxTime, minDay, maxDay);		
		for (var t=minTime;t<=maxTime;t++)
			tpGenRowVertical(tpName, editable,t,startTimes[t],endTimes[t],text, highlight, sel, minDay, maxDay);
	}
	document.writeln("</table>");
}
	
function tpPrefSelected(tpName, pref) {
	document.getElementById(tpName+'_req'+document.getElementById(tpName+"_reqSelect").value).style.border='rgb(0,0,0) 2px solid';
	document.getElementById(tpName+"_reqSelect").value=pref;
	document.getElementById(tpName+'_req'+pref).style.border='rgb(0,0,240) 2px solid';
	var reqUsed = document.getElementById(tpName+"_reqUsed").value;
	if (window.jsConfirm!=false) {
		if (pref!='R' && pref!='0' && reqUsed=='1')
			alert('WARNING: Application of this preference will remove all required preferences.');
		if (pref=='R' && reqUsed=='0')
			alert('WARNING: Application of required preference will remove all not required preferences.');
		eval("fn_"+tpName+"_prefCheck('"+pref+"');");
	}
}

function tpGenPreference(tpName, editable, pref, title, enable) {
	if (tpIsEditable(tpName, editable)) {
		if (enable) {
			document.writeln("<tr align=left "+
				"onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='pointer';\" "+
				"onclick=\"tpPrefSelected('"+tpName+"','"+pref+"');\" "+
				"onmouseout=\"this.style.backgroundColor='rgb(255,255,255)';\">"+
				"<td id='"+tpName+"_req"+pref+"' width='30' height='20' "+
				"style=\"border:"+(tpGetCurrentPreference(tpName)==pref?"rgb(0,0,240)":"rgb(0,0,0)")+" 2px solid;background-color:"+tpPref2Color(tpName, pref)+";\">"+
				"&nbsp;</td>"+
				"<th nowrap>"+title+"</th></tr>");
		} else {
			document.writeln("<tr align=left>"+
				"<td id='"+tpName+"_req"+pref+"' width='30' height='20' "+
				"style=\"border:gray 2px solid;background-color:"+tpPref2Color(tpName, pref)+";\">"+
				"&nbsp;</td>"+
				"<th nowrap><font color='gray'>"+title+"</font></th></tr>");
		}
	} else {
		document.writeln("<tr align=left>"+
			"<td id='"+tpName+"_req"+pref+"' width='30' height='20' "+
			"style=\"border:rgb(0,0,0) 2px solid;background-color:"+tpPref2Color(tpName, pref)+";\">"+
			"&nbsp;</td>"+
			"<th nowrap>"+title+"</th></tr>");
	}
}

function tpSelectionChanged(tpName, nrSelections) {
	for (var i=0;i<nrSelections;i++)
		document.getElementById(tpName+"_sel"+i).style.display='none';
	sel = document.getElementById(tpName+"_selections").value;
	document.getElementById(tpName+"_sel"+sel).style.display='block';
}

function tpGenLegend(tpName, editable, prefTable, prefNames, prefEnables) {
	document.writeln("<table style='font-size:10px' cellSpacing='2' cellPadding='2' border='0'>");
	for (var i=0;i<prefTable.length;i++)
		tpGenPreference(tpName,editable,prefTable[i],prefNames[i],prefEnables[i]);
	document.writeln("</table>");
}

function tpGenSelection(tpName, selections, defSelection) {
	document.writeln("<select onchange=\"tpSelectionChanged('"+tpName+"',"+selections.length+");\" name='"+tpName+"_selections' id='"+tpName+"_selections'>");
	for (var i=0;i<selections.length;i++)
		document.writeln("<option value='"+i+"' "+(defSelection==i?"selected":"")+">"+selections[i][0]+"</option>");
	document.writeln("</select>");
}

function tpGenerate(tpName, horizontal, title, nrTimes, nrDays, days, startTimes, endTimes, text, preferences, highlight, editable, prefTable, prefColors, prefNames, prefEnables, selections, defSelection, defPreference, prefCheck, showLegend) {
	tpGenVariables(tpName,nrTimes,nrDays,preferences, prefTable, prefColors, selections, defPreference, prefCheck, prefNames);
	document.writeln("<table border='0'>");
	if (title==null) {
		if (selections==null) {
			document.writeln("<tr><td>");
			tpGenTable(tpName,horizontal,editable,days,startTimes,endTimes,text, highlight, 0, 0, nrTimes-1, 0, nrDays-1);
			if (showLegend) {
				document.writeln("</td><td width='10'>&nbsp;</td><td>");
				tpGenLegend(tpName, editable, prefTable, prefNames, prefEnables);
			}
			document.writeln("</td></tr>");
		} else {
			document.writeln("<tr><td align='right'>");
			tpGenSelection(tpName, selections, defSelection);
			if (showLegend) {
				document.writeln("</td><td width='10' rowspan='2'>&nbsp;</td><td rowspan='2'>");
				tpGenLegend(tpName, editable, prefTable, prefNames, prefEnables);
			}
			document.writeln("</td></tr>");
			document.writeln("<tr><td valign='top'>");
			for (var i=0;i<selections.length;i++) {
				document.writeln("<div name='"+tpName+"_sel"+i+"' id='"+tpName+"_sel"+i+"' style='display:"+(i==defSelection?"block":"none")+";'>");
				tpGenTable(tpName,horizontal,editable,days,startTimes,endTimes,text, highlight, i, selections[i][1][0], selections[i][1][1], selections[i][1][2], selections[i][1][3]);
				document.writeln("</div>");
			}
			document.writeln("</td></tr>");
		}
	} else {
		if (selections==null) {
			document.writeln("<tr><td>");
			document.writeln(title);
			if (showLegend) {
				document.writeln("</td><td width='10' rowspan='2'>&nbsp;</td><td rowspan='2'>");
				tpGenLegend(tpName, editable, prefTable, prefNames, prefEnables);
			}
			document.writeln("</td></tr>");
			document.writeln("<tr><td valign='top'>");
			tpGenTable(tpName,horizontal,editable,days,startTimes,endTimes,text, highlight, 0, 0, nrTimes-1, 0, nrDays-1);
			document.writeln("</td></tr>");
		} else {
			document.writeln("<tr><td>");
			document.writeln(title);
			document.writeln("</td><td align='right'>");
			tpGenSelection(tpName, selections, defSelection);
			if (showLegend) {
				document.writeln("</td><td width='10' rowspan='2'>&nbsp;</td><td rowspan='2'>");
				tpGenLegend(tpName, editable, prefTable, prefNames, prefEnables);
			}
			document.writeln("</td></tr>");
			document.writeln("<tr><td colspan='2' valign='top'>");
			for (var i=0;i<selections.length;i++) {
				document.writeln("<div name='"+tpName+"_sel"+i+"' id='"+tpName+"_sel"+i+"' style='display:"+(i==defSelection?"block":"none")+";'>");
				tpGenTable(tpName,horizontal,editable,days,startTimes,endTimes,text, highlight, i, selections[i][1][0], selections[i][1][1], selections[i][1][2], selections[i][1][3]);
				document.writeln("</div>");
			}
			document.writeln("</td></tr>");
		}
	}
	document.writeln("</table>");
}
