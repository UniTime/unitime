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
package org.unitime.timetable.form;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SimpleItypeConfig;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-19-2005
 * 
 * XDoclet definition:
 * @struts:form name="InstructionalOfferingConfigEditForm"
 *
 * @author Stephanie Schluttenhofer, Zuzana Mullerova, Tomas Muller
 */
public class InstructionalOfferingConfigEditForm extends ActionForm {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257570611432993077L;
	
	// --------------------------------------------------------- Instance Variables

	private Long configId;
    private String instrOfferingName;
    private String courseOfferingId;
    private String instrOfferingId;
    private String subjectArea;
    private String courseNumber;
    private int limit;
    private Boolean notOffered;
    private String itype;
    private String op;
    private String name;
    private Boolean unlimited;    
    private Integer configCount;
    private String catalogLinkLabel;
    private String catalogLinkLocation;
    
    // Error Codes
    private final short NO_ERR = 0;
    private final short ERR_NC = -1;
    private final short ERR_CL = -2;
    private final short ERR_LS = -3;
    
    // --------------------------------------------------------- Methods

    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        // Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        
        // Check limit in all cases
        if(limit<0) {
            errors.add("limit", new ActionMessage("errors.integerGtEq", "Limit", "0"));
        }
        
        String lblMax = "Limit";
        if (request.getParameter("varLimits")!=null) {
            lblMax = "Max limit";
        }
        
        // Check Itype is specified
        if(op.equals(rsc.getMessage("button.add"))) {
            if(itype==null || itype.trim().length()==0
                    || itype.equals(Constants.BLANK_OPTION_VALUE)) {
                errors.add("itype", new ActionMessage("errors.required", "Instructional Type"));
            }
        }
        
        if( op.equals(MSG.actionSaveConfiguration()) 
                || op.equals(MSG.actionUpdateConfiguration()) ) {

            HttpSession webSession = request.getSession();
            Vector sp = (Vector) webSession.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME);

            // Check that config name doesn't already exist
            InstructionalOffering io = new InstructionalOfferingDAO().get( new Long(this.getInstrOfferingId()) );
            if (io.existsConfig(this.getName(), this.getConfigId())) {
                errors.add("subparts", new ActionMessage("errors.generic", "A configuration with this name already exists in this offering. Use a unique name"));
            }
            
            // Read user defined config
            for(int i=0; i<sp.size(); i++) {
                SimpleItypeConfig sic = (SimpleItypeConfig) sp.elementAt(i);
                
                // Check top level subparts
                if (!this.getUnlimited().booleanValue() && ApplicationProperty.ConfigEditCheckLimits.isTrue()) {
	                int numClasses = sic.getNumClasses();
	                int maxLimitPerClass = sic.getMaxLimitPerClass();
	
	                if (numClasses == 1 && maxLimitPerClass!=this.limit) {
	                    sic.setHasError(true);
	                    errors.add("subparts", 
	                           	new ActionMessage("errors.equal", lblMax + " per class for <u>" + sic.getItype().getDesc() +  "</u>", "Configuration limit of " + this.limit ) );
	                }
	                    
	                if (numClasses>1 && (maxLimitPerClass*numClasses)<this.limit) {
	                    sic.setHasError(true);
	                    errors.add("subparts", 
	                           	new ActionMessage("errors.integerGtEq", "Sum of class limits <u>" + sic.getItype().getDesc() +  "</u>", "Configuration limit of " + this.limit ) );
	                }
                }
                
                // Check input text fields
                checkInputfields(request, errors, sic, lblMax, this.getUnlimited().booleanValue());
                
                // Check child subparts
                short errCode = checkChildSubpart(request, errors, sic, lblMax, this.getUnlimited().booleanValue());    
                
                if(errCode!=NO_ERR) {
                    String errM = "Subparts that are grouped under <u>" + sic.getItype().getDesc() +  "</u> must <br>";
                    if (errCode==ERR_NC)
                        errM += "&nbsp; &nbsp; &nbsp; have number of classes that is a multiple of " + sic.getNumClasses() + ".";
                    if (errCode==ERR_CL)
                        errM += "&nbsp; &nbsp; &nbsp; have a " + lblMax.toLowerCase() + " per class <= " + lblMax.toLowerCase() + " per class of " + sic.getMaxLimitPerClass() + ".";
                    if (errCode==ERR_LS)
                        errM += "&nbsp; &nbsp; &nbsp; not accomodate lesser number of students.";
                        
                    errors.add("subparts", 
                       	new ActionMessage("errors.generic", errM ) );
                }
            }
        }
        
        return errors;
    }

    /**
     * Checks input fields 
     * @param request
     * @param errors
     * @param sic
     * @param lblMax
     */
    private void checkInputfields (
            HttpServletRequest request, 
            ActionErrors errors, 
            SimpleItypeConfig sic, 
            String lblMax,
            boolean unlimited ) {
        
        int mxlpc = sic.getMaxLimitPerClass();
        int mnlpc = sic.getMinLimitPerClass();
        int nc = sic.getNumClasses();
        int nr = sic.getNumRooms();
        int mpw = sic.getMinPerWeek();
        float rc = sic.getRoomRatio();
        
        String lblSubpart = " for <u>" + sic.getItype().getDesc() + "</u>";
        long indx = sic.getId();
        int ct = errors.size();
        
        if (!unlimited) {
	        if(mxlpc<0) 
	            errors.add("subparts"+indx, new ActionMessage("errors.integerGtEq", lblMax + " per class" + lblSubpart, "0"));
	        else {
	            if(mxlpc>limit && ApplicationProperty.ConfigEditCheckLimits.isTrue()) {
	                if (nc>1)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerLtEq", lblMax + " per class of " + mxlpc + lblSubpart, " Configuration limit of "+limit ));
	            }
	            else {
	                
	                if (request.getParameter("varLimits")==null) {
	                    mnlpc = mxlpc;
	                }
	                
	                if(mnlpc<0)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerGtEq", "Min limit per class" + lblSubpart, "0"));
	                if(mnlpc>mxlpc)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerLtEq", "Min limit per class" + lblSubpart, "Max limit per class"));
	                
	                // Check no. of classes
	                if(nc<=0)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerGt", "Number of classes" + lblSubpart, "0" ));
	                
	                if(nc>ApplicationProperty.SubpartMaxNumClasses.intValue())
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerLtEq", "Number of classes" + lblSubpart, ApplicationProperty.SubpartMaxNumClasses.value() ));

	                // Check no. of rooms
	                if(nr<0)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerGtEq", "Number of rooms" + lblSubpart, "0" ));
	                
	                // Check min per week
	                if(mpw<0)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerGtEq", "Minutes per week" + lblSubpart, "0" ));
	                if(mpw==0 && nr!=0)
	                    errors.add("subparts"+indx, new ActionMessage("errors.generic", "Minutes per week " + lblSubpart + " can be 0 only if number of rooms is 0" ));
	                    
	                // Check room ratio
	                if(rc<0)
	                    errors.add("subparts"+indx, new ActionMessage("errors.integerGtEq", "Room ratio" + lblSubpart, "0.0" ));
	            }
	        }               
        }
        else {
            
            // Check no. of classes
            if(nc<=0)
                errors.add("subparts"+indx, new ActionMessage("errors.integerGt", "Number of classes" + lblSubpart, "0" ));
        	
            if(nc>ApplicationProperty.SubpartMaxNumClasses.intValue())
                errors.add("subparts"+indx, new ActionMessage("errors.integerLtEq", "Number of classes" + lblSubpart, ApplicationProperty.SubpartMaxNumClasses.value() ));

            if(mpw<0)
                errors.add("subparts"+indx, new ActionMessage("errors.integerGtEq", "Minutes per week" + lblSubpart, "0" ));
        }
        
        if (errors.size()>ct)
            sic.setHasError(true);
    }
    
    /**
     * Checks child subparts do not have a limit more than the parent
     * and that the number of classes in the child is a multiple of the 
     * parent
     * @param request
     * @param errors
     * @param sic
     * @param lblMax
     * @return code indicating error or no error
     */
    private short checkChildSubpart (
            HttpServletRequest request, 
            ActionErrors errors, 
            SimpleItypeConfig sic, 
            String lblMax,
            boolean unlimited ) {
        
        Vector csp = sic.getSubparts();
        if(csp!=null && csp.size()>0) {
	        for(int i=0; i<csp.size(); i++) {
	            SimpleItypeConfig csic = (SimpleItypeConfig)  csp.elementAt(i);
	            
                checkInputfields(request, errors, csic, lblMax, unlimited);

                if (!unlimited) {
	                if(sic.getNumClasses()!=0 && csic.getNumClasses() % sic.getNumClasses() != 0) {
		                csic.setHasError(true);
		                return ERR_NC;
		            }
	                if(csic.getMaxLimitPerClass()>sic.getMaxLimitPerClass()) {
		                csic.setHasError(true);
		                return ERR_CL;
	                }
	                if ( (csic.getNumClasses()*csic.getMaxLimitPerClass()) < (sic.getNumClasses()*sic.getMaxLimitPerClass()) ) {
		                csic.setHasError(true);
		                return ERR_LS;
	                }
                } else {
	                if(sic.getNumClasses()!=0 && csic.getNumClasses() % sic.getNumClasses() != 0) {
		                csic.setHasError(true);
		                return ERR_NC;
		            }                	
                }
                
                //csic.setHasError(false);
                short errCode = checkChildSubpart(request, errors, csic, lblMax, unlimited);	      
                if(errCode!=NO_ERR) 
                    return errCode;
	        }
        }        
        
        return NO_ERR;
    }
    
    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        courseOfferingId ="";
        subjectArea = "";
        courseNumber = "";
        itype="";
        limit = 0;
        op = "";
        unlimited = new Boolean(false);
        configCount = new Integer(0);
        configId = new Long(0);
        name=null;
        catalogLinkLabel = null;
        catalogLinkLocation = null;
    }

    /**
     * Get the no. of configs for the course offering
     * @return
     */
    public Integer getConfigCount() {
        return configCount;
    }
    
    /**
     * Set the no. of configs available to the course offering
     * @param configCount
     */
    public void setConfigCount(Integer configCount) {
        this.configCount = configCount;
    }
    
    /** 
     * Returns the subjectArea.
     * @return String
     */
    public String getSubjectArea() {
        return subjectArea;
    }

    /** 
     * Set the subjectArea.
     * @param subjectArea The subject to set
     */
    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    /** 
     * Returns the courseNumber.
     * @return String
     */
    public String getCourseNumber() {
        return courseNumber;
    }

    /** 
     * Set the courseNumber.
     * @param courseNumber The courseNumber to set
     */
    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    /**
     * @return Returns the courseOfferingId.
     */
    public String getCourseOfferingId() {
        return courseOfferingId;
    }
    
    /**
     * @param courseOfferingId The uniqueId to set.
     */
    public void setCourseOfferingId(String courseOfferingId) {
        this.courseOfferingId = courseOfferingId;
    }
    
    /**
     * @return Returns the limit.
     */
    public int getLimit() {
        return limit;
    }
    
    /**
     * @param limit The limit to set.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }
        
	/**
	 * @return Returns the notOffered.
	 */
	public Boolean getNotOffered() {
		return notOffered;
	}
	/**
	 * @param notOffered The notOffered to set.
	 */
	public void setNotOffered(Boolean notOffered) {
		this.notOffered = notOffered;
	}

	/**
     * @return Returns the itype.
     */
    public String getItype() {
        return itype;
    }
    
    /**
     * @param itype The itype to set.
     */
    public void setItype(String itype) {
        this.itype = itype;
    }
    
    /**
     * @return Returns the op.
     */
    public String getOp() {
        return op;
    }
    
    /**
     * @param op The op to set.
     */
    public void setOp(String op) {
        this.op = op;
    }
    
    public String getInstrOfferingId() {
        return instrOfferingId;
    }
    
    public void setInstrOfferingId(String instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
        
    public Long getConfigId() {
        return configId;
    }
    public void setConfigId(Long configId) {
        this.configId = configId;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getUnlimited() {
        return unlimited;
    }
    public void setUnlimited(Boolean unlimited) {
        this.unlimited = unlimited;
    }
        
    public String getInstrOfferingName() {
        return instrOfferingName;
    }
    public void setInstrOfferingName(String instrOfferingName) {
        this.instrOfferingName = instrOfferingName;
    }

	public String getCatalogLinkLabel() {
		return catalogLinkLabel;
	}

	public void setCatalogLinkLabel(String catalogLinkLabel) {
		this.catalogLinkLabel = catalogLinkLabel;
	}

	public String getCatalogLinkLocation() {
		return catalogLinkLocation;
	}

	public void setCatalogLinkLocation(String catalogLinkLocation) {
		this.catalogLinkLocation = catalogLinkLocation;
	}
}
