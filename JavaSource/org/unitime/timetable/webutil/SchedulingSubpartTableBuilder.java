/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
*/
package org.unitime.timetable.webutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.SimpleItypeConfig;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;


/**
 * Build Configuration Edit Tree
 * 
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
public class SchedulingSubpartTableBuilder {
    
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	/**
     * Reads the user defined config object and generates html code to display it
     * @param request
     * @param limit
     * @param uid
     * @param createAsNew
     * @param unlimitedEnroll
     * @return Html code for displaying user defined config
     */
    public static String buildSubpartsTable(
            HttpServletRequest request, SessionContext context, int limit, String uid, boolean createAsNew, boolean unlimitedEnroll) throws Exception {
        
        // Check if variable limits is selected 
		boolean varLimits = "y".equals(request.getParameter("varLimits"));
		
        // Read user defined config
        Vector sp = (Vector) context.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME);
        
        // Read setting for auto calculation 
        boolean autoCalc = true;
        if (!CommonValues.Yes.eq(UserProperty.ConfigAutoCalc.get(context.getUser())))
            autoCalc = false;
        
        // Get external depts
        Collection extDepts = (Collection) request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME);
        String extDeptsOption = "<OPTION value='-1'>Department</OPTION>";
        for(Iterator it = extDepts.iterator(); it.hasNext();){
        	Department d = (Department) it.next();
        	extDeptsOption += "<OPTION value='" + d.getUniqueId().toString() + "'>" + d.getManagingDeptLabel() + "</OPTION>";
        }
        
        // Subparts exist
        if(sp!=null && sp.size()>0) {
            
            if (!varLimits) {
		        for(int i=0; i<sp.size(); i++) {
		            SimpleItypeConfig sic = (SimpleItypeConfig) sp.elementAt(i);
		            if ( hasVarLimitsInSubpart(request, sic) ) {
		                varLimits=true;
		                break;
		            }
		        }
            }
            
            // Create a table
            WebTable tbl = new WebTable(9, 
        			"",  
        			new String[] {	unlimitedEnroll 
                    					? ""
                    					: "<<00>>", 
                    				"&nbsp;", 
                    				!varLimits ? "<<1>>" : MSG.columnSubpartMinLimitPerClass(), 
                    				!varLimits ? "<<11>>" : MSG.columnSubpartMaxLimitPerClass(), 
                    										MSG.columnSubpartNumberOfClasses(),
                    										MSG.columnSubpartMinutesPerWeek(), 
                    										MSG.columnSubpartNumberOfRooms(),
                    										MSG.columnSubpartRoomRatio(), 
                    										MSG.columnSubpartManagingDepartment()},
        			new String[] { "left", "left", "center", "center", "center", "center", "center", "center", "center"},
        			null);
            tbl.setSuppressRowHighlight(true);
            
	        // Loop through itypes
	        for(int i=0; i<sp.size(); i++) {
	            SimpleItypeConfig sic = (SimpleItypeConfig) sp.elementAt(i);
	            // Recursively process each itype config
	            setupSubpart(request, context, sic, 1, tbl, i, sp.size(), 
	                    -1, -1, limit, null, autoCalc, createAsNew, extDeptsOption, unlimitedEnroll, varLimits);
	        }
	        request.setAttribute("subpartsExist", "true");
	        
	        String varLimitsCheckBox = "<input type='checkbox' name='varLimits' value='y' <<0>>" +  (varLimits ? "checked":"") + " onClick=\"doClick('multipleLimits', 0);\"> <small>Allow variable limits</small>";
	        String tblStr = tbl.printTable();	        
	        if (request.getAttribute("varLimits")!=null) {
	            tblStr = tblStr.replaceAll("<<00>>", varLimitsCheckBox);
	            tblStr = tblStr.replaceAll("<<0>>", "checked");
	            tblStr = tblStr.replaceAll("<<1>>", MSG.columnSubpartMinLimitPerClass());
	            tblStr = tblStr.replaceAll("<<11>>", MSG.columnSubpartMaxLimitPerClass());
	        }
	        else {
	            if (CommonValues.Yes.eq(UserProperty.VariableClassLimits.get(context.getUser()))) {
		            tblStr = tblStr.replaceAll("<<00>>", varLimitsCheckBox);
	                tblStr = tblStr.replaceAll("<<0>>", " ");
	            }
	            else
	                tblStr = tblStr.replaceAll("<<00>>", " ");
	            
	            tblStr = tblStr.replaceAll("<<1>>", " ");
	            tblStr = tblStr.replaceAll("<<11>>", MSG.columnSubpartLimitPerClass());
	        }
	        
	        return (tblStr);
        }
        else {
	        request.setAttribute("subpartsExist", "false");
	        return "";
        }
    }
    
    
    
    
    /**
     * Checks if any subpart in the config has variable limits
     * @param request Http Request object
     * @param sic SimpleItypeConfig object
     * @return true if var limits found, false otherwise
     */
    private static boolean hasVarLimitsInSubpart(HttpServletRequest request, SimpleItypeConfig sic) {
        if(request.getParameter("mnlpc" + sic.getId())!=null)
            sic.setMinLimitPerClass(Constants.getPositiveInteger(request.getParameter("mnlpc" + sic.getId()), -1));
        if(request.getParameter("mxlpc" + sic.getId())!=null)
            sic.setMaxLimitPerClass(Constants.getPositiveInteger(request.getParameter("mxlpc" + sic.getId()), -1));

		int mnlpc = sic.getMinLimitPerClass();
		int mxlpc = sic.getMaxLimitPerClass();
 
		if (mnlpc!=mxlpc)
		    return true;
		
        Vector v = sic.getSubparts();
        for(int i=0; i<v.size(); i++) {
            SimpleItypeConfig sic1 = (SimpleItypeConfig) v.elementAt(i);
            if (hasVarLimitsInSubpart(request, sic1))
                return true;
        }
        
		return false;
    }




    /**
     * Recursive function generates the html code for displaying the config
     * @param request Http Request object
     * @param sic SimpleItypeConfig object
     * @param level Recurse Level
     * @param tbl WebTable object
     * @param rowNum Row Number (in config)
     * @param maxRows Max elements in config
     * @param spRowNum row number of subpart
     * @param maxSp Max subparts
     * @param limit 
     * @param parentSic
     * @param autoCalc 
     */
    private static void setupSubpart(
            HttpServletRequest request, SessionContext context, SimpleItypeConfig sic, 
            int level, WebTable tbl, int rowNum, int maxRows, 
            int spRowNum, int maxSp, int limit, 
            SimpleItypeConfig parentSic, boolean autoCalc, 
            boolean createAsNew, String extDepts, boolean unlimitedEnroll, boolean varLimits) throws Exception {
        
        ItypeDesc itype = sic.getItype();

        // Set attributes
        if(request.getParameter("mnlpc" + sic.getId())!=null)
            sic.setMinLimitPerClass(Constants.getPositiveInteger(request.getParameter("mnlpc" + sic.getId()), -1));
        if(request.getParameter("mxlpc" + sic.getId())!=null)
            sic.setMaxLimitPerClass(Constants.getPositiveInteger(request.getParameter("mxlpc" + sic.getId()), -1));
        if(request.getParameter("mpw" + sic.getId())!=null)
            sic.setMinPerWeek(Constants.getPositiveInteger(request.getParameter("mpw" + sic.getId()), -1));
        if(request.getParameter("nc" + sic.getId())!=null)
            sic.setNumClasses(Constants.getPositiveInteger(request.getParameter("nc" + sic.getId()), -1));
        if(request.getParameter("nr" + sic.getId())!=null)
            sic.setNumRooms(Constants.getPositiveInteger(request.getParameter("nr" + sic.getId()), -1));
        if(request.getParameter("rr" + sic.getId())!=null)
            sic.setRoomRatio(Constants.getPositiveFloat(request.getParameter("rr" + sic.getId()), 1.0f));
        if(request.getParameter("md" + sic.getId())!=null)
            sic.setManagingDeptId(Long.parseLong(request.getParameter("md" + sic.getId())));
        if(request.getParameter("disabled" + sic.getId())!=null)
            sic.setDisabled(new Boolean(request.getParameter("disabled" + sic.getId())).booleanValue());

		// Read attributes
		int mnlpc = sic.getMinLimitPerClass();
		int mxlpc = sic.getMaxLimitPerClass();
        int mpw = sic.getMinPerWeek();
        int nc = sic.getNumClasses();
        int nr = sic.getNumRooms();
        float rr = sic.getRoomRatio();
        long md = sic.getManagingDeptId();
        long subpartId = -1L;
        if (!createAsNew)
            subpartId = sic.getSubpartId();
        long sicId = sic.getId();
        boolean disabled = sic.isDisabled();
        boolean notOwned = sic.isNotOwned();
        boolean hasError = sic.getHasError();
        boolean uDisabled = unlimitedEnroll;
        Vector v = sic.getSubparts();
        
        // If status is not LLR Edit then do not show option to change to external manager
        boolean mgrDisabled = false;
		if (subpartId >= 0 && !context.hasPermission(subpartId, "SchedulingSubpart", Right.InstrOfferingConfigEditSubpart)) {
		    mgrDisabled = true;
		    if (createAsNew) {
		        md = -1;
		    }
		}
		
        if (unlimitedEnroll) {
    		mnlpc = -1;
    		mxlpc = -1;
            nr = -1;
            rr = -1;
        }
        
        Debug.debug("setting up subpart: " + itype.getAbbv() + ", Level: " + level);

        // Generate Javascript
        String onBlur1 = "";
        String onBlur2 = "";
        String maxClasses = "if ( document.forms[0].nc" + sicId + ".value > 999) { document.forms[0].nc" + sicId + ".value=0 } ";
        
        if (autoCalc) {
            
	        onBlur1 = " onBlur=\"if (this.value!=0 && (document.forms[0].mxlpc" + sicId + ".value==''|| document.forms[0].mxlpc" + sicId + ".value==null) ) {" +
	        		" document.forms[0].mxlpc" + sicId + ".value=this.value; }\"";

	        if (parentSic!=null) {
		    	onBlur2 = " onBlur=\"if (this.value!=0) " 
		    	    + "{ document.forms[0].nc" + sicId 
			    	+ ".value=Math.ceil( (document.forms[0].mxlpc" + parentSic.getId() 
			    	+ ".value * document.forms[0].nc" + parentSic.getId() 
			    	+ ".value) / this.value ); " + maxClasses + " } ";
	        }
	        else {
		    	onBlur2 = " onBlur=\"if (this.value!=0) { document.forms[0].nc" + sicId 
			    	+ ".value=Math.ceil(document.forms[0].limit.value/this.value); " + maxClasses + "} ";
	        }

        }
        
        if (!varLimits) {
            if (onBlur2.length()==0)
                onBlur2 = "onBlur=\"document.forms[0].mnlpc" + sicId + ".value=this.value; \"";
            else 
                onBlur2 += "document.forms[0].mnlpc" + sicId + ".value=this.value; \"";
        }
        else {
    	    onBlur2 += "\"";
        }
        
        // Generate indentation depending on recursive level
        String indent = "";
        for(int i=1; i<level; i++) 
            indent += "\n<IMG width=\"10\" align=\"absmiddle\" src=\"images/blank.gif\">";
        
        if(indent.length()!=0) 
            indent += "\n<IMG align=\"absmiddle\" src=\"images/dot_line.gif\">&nbsp;";

        if (!varLimits && mnlpc!=mxlpc) {
            if (mnlpc==-1) 
                mnlpc=mxlpc;
            else
                request.setAttribute("varLimits", "1");
        }
            
        // Generate html row for itype config
        tbl.addLine(
                new String[] {
                        indent 
                        	+ "<span style='font-weight:bold;'>" + itype.getDesc() + "</span>"
                        	+ (hasError ? "&nbsp; <IMG align=\"absmiddle\" src=\"images/cancel.png\">" : ""),
                        
                        (!disabled) 
                        	? getIcons(sic, level, rowNum, maxRows, spRowNum, maxSp) 
                        	: (notOwned)
                        		? "<img border=\"0\" src=\"images/lock.png\">"
                        		: "",
                         
                        "\n\t <INPUT type=\"hidden\" name=\"subpartId" + sicId + "\" value=\"" + subpartId + "\">"                        
                        + "\n\t <INPUT type=\"hidden\" name=\"disabled" + sicId + "\" value=\"" + disabled + "\">"                        
                        + ( (disabled || uDisabled || (!varLimits && mnlpc==mxlpc)) 
                            ? ( "\n\t<INPUT " 
	                            + " name=\"mnlpc" + sicId 
	                            + "\" type=\"hidden\" value=\"" 
	                        	+ (mnlpc>=0?""+mnlpc:"") + "\"" 
	                        	+ " >" 
	                        	+ (( (disabled || uDisabled) && mnlpc>=0 && varLimits) || (!varLimits && mnlpc!=mxlpc) 
	                        	        ?""+mnlpc
	                        	        :"" ) ) 
                            : ( "\n\t<INPUT " 
	                            + " name=\"mnlpc" + sicId 
	                            + "\" type=\"text\" size=\"4\" maxlength=\"4\" value=\"" 
	                        	+ (mnlpc>=0?""+mnlpc:"") + "\"" 
	                        	+ onBlur1
	                        	+ " >" ) ),
                        	
                    	((disabled || uDisabled) 
	                        ? ( "\n\t<INPUT " 
	                            + " name=\"mxlpc" + sicId 
	                            + "\" type=\"hidden\" value=\"" 
	                        	+ (mxlpc>=0?""+mxlpc:"") + "\"" 
	                        	+ " >" + (mxlpc>=0?""+mxlpc:"") ) 
	                        : ( "\n\t<INPUT " 
	                            + " name=\"mxlpc" + sicId 
	                            + "\" type=\"text\" size=\"4\" maxlength=\"4\" value=\"" 
	                        	+ (mxlpc>=0?""+mxlpc:"") + "\"" 
	                        	+ onBlur2
	                        	+ " >" ) ),
                            	
                       	((disabled) 
	                        ? ( "\n\t<INPUT " 
	                        	+ " name=\"nc" + sicId 
	                            + "\" type=\"hidden\" value=\"" 
	                        	+ (nc>=0?""+nc:"")  + "\">" + (nc>=0?""+nc:"") )
	                        : ( "\n\t<INPUT " 
	                        	+ " name=\"nc" + sicId 
	                        	+ "\" type=\"text\" size=\"3\" maxlength=\"3\" value=\"" 
	                        	+ (nc>=0?""+nc:"")  + "\" onblur=\"if (!confirmNumClasses(this.value)) { this.value = 0 }\">" ) ),

                       	((disabled) 
   	                        ? ( "\n\t<INPUT " 
   	                         	+ " name=\"mpw" + sicId 
   	                        	+ "\" type=\"hidden\" value=\"" 
   	                        	+ (mpw>=0?""+mpw:"") + "\">" + (mpw>=0?""+mpw:"") )
   	                        : ( "\n\t<INPUT " 
   	                         	+ " name=\"mpw" + sicId 
   	                        	+ "\" type=\"text\" size=\"4\" maxlength=\"4\" value=\"" 
   	                        	+ (mpw>=0?""+mpw:"") + "\">" ) ),                        	
 
                    	((disabled || uDisabled) 
	                        ? ( "\n\t<INPUT " 
    	                    	+ " name=\"nr" + sicId 
    	                    	+ "\" type=\"hidden\" value=\"" 
    	                    	+ (nr>=0?""+nr:"") + "\">" + (nr>=0?""+nr:"") )
	                        : ( "\n\t<INPUT " 
    	                    	+ " name=\"nr" + sicId 
    	                    	+ "\" type=\"text\" size=\"4\" maxlength=\"2\" value=\"" 
    	                    	+ (nr>=0?""+nr:"") + "\">" ) ),                        	

                    	((disabled || uDisabled) 
	                        ? ( "\n\t<INPUT " 
                            	+ " name=\"rr" + sicId 
                            	+ "\" type=\"hidden\" value=\"" 
                            	+ (rr>=0?""+rr:"") + "\">" + (rr>=0?""+rr:"") )
	                        : ( "\n\t<INPUT " 
                            	+ " name=\"rr" + sicId 
                            	+ "\" type=\"text\" size=\"4\" maxlength=\"4\" value=\"" 
                            	+ (rr>=0?""+rr:"") + "\">" ) ),
                        	
                    	((disabled || mgrDisabled) 
	                        ? ( "\n\t<INPUT " 
                            	+ " name=\"md" + sicId 
                            	+ "\" type=\"hidden\" value=\""
                            	+ md + "\">" + getManagingDeptLabel(request, md) )
	                        : ( "\n\t<SELECT " 
                            	+ " name=\"md" + sicId 
                            	+ "\">" + extDepts.replaceAll("'" + md + "'", "'" + md + "' selected") +
                            	"</SELECT>" ) )             
                }, null);
        
        // Loop through children sub-parts
        for(int i=0; i<v.size(); i++) {
            SimpleItypeConfig sic1 = (SimpleItypeConfig) v.elementAt(i);
            setupSubpart(request, context, sic1, level+1, tbl, rowNum, maxRows, 
                    i, v.size(), limit, sic, autoCalc, createAsNew, extDepts, unlimitedEnroll, varLimits);
        }
    }
    
    
    /**
     * @param md
     * @return
     */
    private static String getManagingDeptLabel(HttpServletRequest request, long md) {
        if (md<0)
            return "Department";
        
        if (md==Constants.MANAGED_BY_MULTIPLE_DEPTS)
            return "Multiple Departments";

        Department d = new DepartmentDAO().get(new Long (md));
        if (d!=null) {
            if (d.isExternalManager().booleanValue())
                return d.getExternalMgrLabel();
            else
                return "Department";
        }
            
        return "Not Found";
    }




    /**
     * Generates icons for shifting and deleting operations on itype config elements
     * @param sic SimpleItypeConfig object
     * @param level Recurse Level
     * @param rowNum Row Number (in config)
     * @param maxRows Max elements in config
     * @param uid Unique Id of course offering
     * @param spRowNum row number of subpart
     * @param maxSp Max subparts
     * @return Html code for arrow images
     */
    private static String getIcons(SimpleItypeConfig sic, int level, int rowNum, int maxRows, int spRowNum, int maxSp) {
        
        String html = "";
               
        // Right Arrow
        if ( (level==1 && rowNum>0)
             || (level>1 && spRowNum>0) ) 
        	html += "<IMG border=\"0\" alt=\""+ MSG.titleMoveToChildLevel() + "\" title=\"" + MSG.titleMoveToChildLevel() + "\" align=\"top\" src=\"images/arrow_r.gif\" " +
        			"onClick=\"doClick('shiftRight', " + sic.getId() + ");\" onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\">";
        else
            html += "<IMG align=\"top\" src=\"images/blank.gif\">";
        
        // Left Arrow
        if (level>1)
        	html += "<IMG border=\"0\" alt=\""+ MSG.titleMoveToParentLevel()+"\" title=\""+MSG.titleMoveToParentLevel() +"\" align=\"top\" src=\"images/arrow_l.gif\" " +
        			"onClick=\"doClick('shiftLeft', " + sic.getId() + ");\" onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\">";
        else
            html += "<IMG align=\"top\" src=\"images/blank.gif\">"; 
        
        // Up Arrow
        if( (level==1 && rowNum>0 )
             || (level>1 && spRowNum>0) ) 
        	html += "<IMG border=\"0\" alt=\""+MSG.altMoveUp()+"\" align=\"absmiddle\" src=\"images/arrow_u.gif\" " +
        			"onClick=\"doClick('shiftUp', " + sic.getId() + ");\" onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\">";
        else
            html += "<IMG align=\"absmiddle\" src=\"images/blank.gif\">";

        // Down Arrow
        if ( (level==1 && (rowNum+1)<maxRows)
             || (level>1 && (spRowNum+1)<maxSp) )
        	html += "<IMG border=\"0\" alt=\""+MSG.altMoveDown()+"\" align=\"absmiddle\" src=\"images/arrow_d.gif\" " +
        			"onClick=\"doClick('shiftDown', " + sic.getId() + ");\" onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\">";
        else
            html += "<IMG align=\"absmiddle\" src=\"images/blank.gif\">";

        // Delete
        html += "<IMG border=\"0\" alt=\""+MSG.altDelete()+"\" title=\""+MSG.titleDeleteInstructionalType()+"\" align=\"top\" src=\"images/action_delete.png\" " +
        		"onClick=\"doClick('delete', " + sic.getId() + ");\" onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\">&nbsp; ";

        html += "&nbsp; &nbsp;";
        
        return html;
    }
    
    
}
