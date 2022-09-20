/*
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
*/
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SimpleItypeConfig;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.IdValue;


/** 
 * @author Stephanie Schluttenhofer, Zuzana Mullerova, Tomas Muller
 */
public class InstructionalOfferingConfigEditForm implements UniTimeForm {
	private static final long serialVersionUID = 3257570611432993077L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
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
    private String durationTypeDefault;
    private Long durationType;
    private boolean durationTypeEditable;
    private Long instructionalMethod;
    private String instructionalMethodDefault;
    private boolean instructionalMethodEditable;
    
    // Error Codes
    private final short NO_ERR = 0;
    private final short ERR_NC = -1;
    private final short ERR_CL = -2;
    private final short ERR_LS = -3;
    
    public InstructionalOfferingConfigEditForm() {
    	reset();
    }

    @Override
    public void validate(UniTimeAction action) {
        // Check limit in all cases
        if(limit<0) {
        	action.addFieldError("form.limit", MSG.errorIntegerGtEq(MSG.columnLimit(), "0"));
        }
        
        String lblMax = MSG.columnLimit();
        if (action.getRequest().getParameter("varLimits")!=null) {
            lblMax = MSG.columnMaxLimit();
        }
        
        // Check Itype is specified
        if (MSG.actionAddInstructionalTypeToConfig().equals(op)) {
            if(itype==null || itype.trim().length()==0 || itype.equals(Constants.BLANK_OPTION_VALUE)) {
            	action.addFieldError("form.itype", MSG.errorRequiredField(MSG.columnInstructionType().replace("\n", " ")));
            }
        }
        
        if (MSG.actionSaveConfiguration().equals(op) || MSG.actionUpdateConfiguration().equals(op)) {

            HttpSession webSession = action.getRequest().getSession();
            Collection<SimpleItypeConfig> sp = (Collection<SimpleItypeConfig>)webSession.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME);

            // Check that config name doesn't already exist
            InstructionalOffering io = new InstructionalOfferingDAO().get( Long.valueOf(this.getInstrOfferingId()) );
            if (io.existsConfig(this.getName(), this.getConfigId())) {
            	action.addFieldError("form.subparts", MSG.errorConfigurationAlreadyExists());
            }
            
            // Read user defined config
            for (SimpleItypeConfig sic: sp) {
                
                // Check top level subparts
                if (!this.getUnlimited().booleanValue() && ApplicationProperty.ConfigEditCheckLimits.isTrue()) {
	                int numClasses = sic.getNumClasses();
	                int maxLimitPerClass = sic.getMaxLimitPerClass();
	
	                if (numClasses == 1 && maxLimitPerClass!=this.limit) {
	                    sic.setHasError(true);
	                    action.addFieldError("form.subparts", MSG.errorEqual(MSG.messageLimitPerClassForIType(lblMax, sic.getItype().getDesc()),MSG.messageConfigurationLimit(this.limit))); 
	                }
	                    
	                if (numClasses>1 && (maxLimitPerClass*numClasses)<this.limit) {
	                    sic.setHasError(true);
	                    action.addFieldError("form.subparts", MSG.errorIntegerGtEq(MSG.messageSumClassLimitsForIType(sic.getItype().getDesc()),MSG.messageConfigurationLimit(this.limit)));
	                }
                }
                
                // Check input text fields
                checkInputfields(action, sic, lblMax, this.getUnlimited().booleanValue());
                
                // Check child subparts
                short errCode = checkChildSubpart(action, sic, lblMax, this.getUnlimited().booleanValue());    
                
                if (errCode!=NO_ERR) {
                    if (errCode==ERR_NC)
                    	action.addFieldError("form.subparts",
                    			MSG.errorConfigurationNC(sic.getItype().getDesc(), sic.getNumClasses()));
                    if (errCode==ERR_CL)
                    	action.addFieldError("form.subparts",
                    			MSG.errorConfigurationCL(sic.getItype().getDesc(), lblMax.toLowerCase(), sic.getMaxLimitPerClass()));
                    if (errCode==ERR_LS)
                    	action.addFieldError("form.subparts",
                    			MSG.errorConfigurationLS(sic.getItype().getDesc()));
                }
            }
        }
    }

    /**
     * Checks input fields 
     */
    private void checkInputfields (UniTimeAction action, SimpleItypeConfig sic, String lblMax, boolean unlimited) {
        
        int mxlpc = sic.getMaxLimitPerClass();
        int mnlpc = sic.getMinLimitPerClass();
        int nc = sic.getNumClasses();
        int nr = sic.getNumRooms();
        int mpw = sic.getMinPerWeek();
        float rc = sic.getRoomRatio();
        
        long indx = sic.getId();
        int ct = action.getFieldErrors().size();
        
        if (!unlimited) {
	        if (mxlpc<0) 
	        	action.addFieldError("form.subparts"+indx,
	        			MSG.errorIntegerGtEq(MSG.messageLimitPerClassForIType(lblMax, sic.getItype().getDesc()), "0"));
	        else {
	            if(mxlpc>limit && ApplicationProperty.ConfigEditCheckLimits.isTrue()) {
	                if (nc>1)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerLtEq(
	                					MSG.messageLimitPerClassOfLimitForIType(lblMax, mxlpc, sic.getItype().getDesc()),
	                					MSG.messageConfigurationLimit(limit)));
	            } else {
	                if (action.getRequest().getParameter("varLimits")==null) {
	                    mnlpc = mxlpc;
	                }
	                
	                if (mnlpc<0)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerGtEq(
	                					MSG.messageLimitPerClassForIType(MSG.columnMinLimit(), sic.getItype().getDesc()),
	                					"0"));
	                if (mnlpc>mxlpc)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerLtEq(
	                					MSG.messageLimitPerClassForIType(MSG.columnMinLimit(), sic.getItype().getDesc()),
	                					MSG.messageMaxLimitPerClass()
	                					));

	                // Check no. of classes
	                if (nc<=0)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerGt(MSG.messageNumberOfClassesForIType(sic.getItype().getDesc()), "0"));
	                
	                if (nc>ApplicationProperty.SubpartMaxNumClasses.intValue())
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerLtEq(
	                					MSG.messageNumberOfClassesForIType(sic.getItype().getDesc()),
	                					ApplicationProperty.SubpartMaxNumClasses.value()
	                					));

	                // Check no. of rooms
	                if (nr<0)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerGtEq(MSG.messageNumberOfRoomsForIType(sic.getItype().getDesc()), "0"));
	                
	                // Check min per week
	                if (mpw<0)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerGtEq(MSG.messageMinsPerWeekForIType(sic.getItype().getDesc()), "0"));

	                if (mpw==0 && nr!=0)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.messageMinsPerWeekForITypeCanBeZeroWhenNbrRoomsIsZero(sic.getItype().getDesc()));
	                    
	                // Check room ratio
	                if (rc<0)
	                	action.addFieldError("form.subparts"+indx,
	                			MSG.errorIntegerGtEq(MSG.messageRoomRatioForIType(sic.getItype().getDesc()), "0"));
	            }
	        }               
        } else {
            
            // Check no. of classes
            if(nc<=0)
            	action.addFieldError("form.subparts"+indx,
            			MSG.errorIntegerGt(MSG.messageNumberOfClassesForIType(sic.getItype().getDesc()), "0"));
        	
            if(nc>ApplicationProperty.SubpartMaxNumClasses.intValue())
            	action.addFieldError("form.subparts"+indx,
            			MSG.errorIntegerLtEq(
            					MSG.messageNumberOfClassesForIType(sic.getItype().getDesc()),
            					ApplicationProperty.SubpartMaxNumClasses.value()
            					));

            if(mpw<0)
            	action.addFieldError("form.subparts"+indx,
            			MSG.errorIntegerGtEq(MSG.messageMinsPerWeekForIType(sic.getItype().getDesc()), "0"));
        }
        
        if (action.getFieldErrors().size()>ct)
            sic.setHasError(true);
    }
    
    /**
     * Checks child subparts do not have a limit more than the parent
     * and that the number of classes in the child is a multiple of the 
     * parent
     * @return code indicating error or no error
     */
    private short checkChildSubpart (UniTimeAction action, SimpleItypeConfig sic, String lblMax, boolean unlimited) {
        List<SimpleItypeConfig> csp = sic.getSubparts();
        if(csp!=null && csp.size()>0) {
	        for(SimpleItypeConfig csic: csp) {
                checkInputfields(action, csic, lblMax, unlimited);

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
                short errCode = checkChildSubpart(action, csic, lblMax, unlimited);	      
                if(errCode!=NO_ERR) 
                    return errCode;
	        }
        }        
        
        return NO_ERR;
    }
    
    @Override
    public void reset() {
        courseOfferingId ="";
        subjectArea = "";
        courseNumber = "";
        itype="";
        limit = 0;
        op = "";
        unlimited = Boolean.valueOf(false);
        configCount = Integer.valueOf(0);
        configId = Long.valueOf(0);
        name=null;
        catalogLinkLabel = null;
        catalogLinkLocation = null;
        durationType = null;
        durationTypeDefault = null;
        durationTypeEditable = false;
        instructionalMethod = null;
        instructionalMethodDefault = null;
        instructionalMethodEditable = false;
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
	
    public Long getDurationType() { return durationType; }
    public void setDurationType(Long durationType) { this.durationType = durationType; }
    public String getDurationTypeDefault() { return durationTypeDefault; }
    public void setDurationTypeDefault(String durationTypeDefault) { this.durationTypeDefault = durationTypeDefault; }
    public List<IdValue> getDurationTypes() {
    	List<IdValue> ret = new ArrayList<IdValue>();
    	for (ClassDurationType type: ClassDurationType.findAll())
    		if (type.isVisible() || type.getUniqueId().equals(durationType))
    			ret.add(new IdValue(type.getUniqueId(), type.getLabel()));
    	return ret;
    }
    public String getDurationTypeText() {
    	for (ClassDurationType type: ClassDurationType.findAll())
    		if (type.getUniqueId().equals(durationType))
    			return type.getLabel();
    	return durationTypeDefault;
    }
    public boolean isDurationTypeEditable() { return durationTypeEditable; }
    public void setDurationTypeEditable(boolean durationTypeEditable) { this.durationTypeEditable = durationTypeEditable; }
    
    public Long getInstructionalMethod() { return instructionalMethod; }
    public void setInstructionalMethod(Long instructionalMethod) { this.instructionalMethod = instructionalMethod; }
    public String getInstructionalMethodDefault() { return instructionalMethodDefault; }
    public void setInstructionalMethodDefault(String instructionalMethodDefault) { this.instructionalMethodDefault = instructionalMethodDefault; }
    public List<IdValue> getInstructionalMethods() {
    	List<IdValue> ret = new ArrayList<IdValue>();
    	for (InstructionalMethod type: InstructionalMethod.findAll())
    		if (type.isVisible() || type.getUniqueId().equals(instructionalMethod))
    			ret.add(new IdValue(type.getUniqueId(), type.getLabel()));
    	return ret;
    }
    public boolean isInstructionalMethodEditable() { return instructionalMethodEditable; }
    public void setInstructionalMethodEditable(boolean instructionalMethodEditable) { this.instructionalMethodEditable = instructionalMethodEditable; }
}
