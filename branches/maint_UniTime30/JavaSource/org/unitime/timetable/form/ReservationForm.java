/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
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
package org.unitime.timetable.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * Base Reservation Form
 * Subclasses: IndividualReservation, CharacteristicReservation forms
 * 
 * @author Heston Fernandes
 */
public class ReservationForm extends ActionForm {

    public final int RESV_ROWS_ADDED = 1;   
    
    // --------------------------------------------------------- Instance Variables
    
    private String op;

    private List reservationId;
    private List priority;
    private List reservationType;
    
    private Long ownerId;
	private String ownerName;
    private String ownerType;
    private String ownerTypeLabel;
    private String reservationClass;
    private Boolean addBlankRow;

	private String ioLimit;
	private String crsLimit;
	private Boolean unlimited;
    
    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Preference */
    protected DynamicListObjectFactory factoryResv = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Constants.BLANK_OPTION_VALUE);
        }
    };

    // --------------------------------------------------------- Methods

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    	 op = null;
    	 ownerId = null;
    	 ownerType = null;
    	 ownerName = null;
    	 ownerTypeLabel = null;    	 
    	 reservationClass = null;
    	 addBlankRow = new Boolean(true);
    	 
    	 reservationId = DynamicList.getInstance(new ArrayList(), factoryResv);
    	 priority = DynamicList.getInstance(new ArrayList(), factoryResv);
    	 reservationType = DynamicList.getInstance(new ArrayList(), factoryResv);
    }
    
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
        
        if(!checkListNumber(priority, false, new Integer(1), new Integer(10))) {
            errors.add("priority", 
                    new ActionMessage("errors.range", "Priority", "1", "10") );
        }        
        
        if(!checkList(reservationType, true)) {
            errors.add("reservationType", 
                    new ActionMessage(
                            "errors.generic", 
                            "Invalid Reservation Type: Check for blank values. ") );
        }        
        
        return errors;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }
    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }
    
    public String getReservationClass() {
        return reservationClass;
    }
    public void setReservationClass(String reservationClass) {
        this.reservationClass = reservationClass;
    }
    
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
        
    public String getOwnerName() {
        return ownerName;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getOwnerTypeLabel() {
        return ownerTypeLabel;
    }
    public void setOwnerTypeLabel(String ownerTypeLabel) {
        this.ownerTypeLabel = ownerTypeLabel;
    }    

    public Boolean getAddBlankRow() {
        return addBlankRow;
    }
    public void setAddBlankRow(Boolean addBlankRow) {
        this.addBlankRow = addBlankRow;
    }
    
	public String getCrsLimit() {
		return crsLimit;
	}

	public void setCrsLimit(String crsLimit) {
		this.crsLimit = crsLimit;
	}

	public String getIoLimit() {
		return ioLimit;
	}

	public void setIoLimit(String ioLimit) {
		this.ioLimit = ioLimit;
	}
    
   public Boolean getUnlimited() {
		return unlimited;
	}

	public void setUnlimited(Boolean unlimited) {
		this.unlimited = unlimited;
	}

	public List getPriority() {
        return priority;
    }
    public void setPriority(List priority) {
        this.priority = priority;
    }
    public String getPriority(int key) {
        return priority.get(key).toString();
    }
    public void setPriority(int key, Object value) {
        this.priority.set(key, value);
    }
    
    public List getReservationId() {
        return reservationId;
    }
    public void setReservationId(List reservationId) {
        this.reservationId = reservationId;
    }
    public String getReservationId(int key) {
        return reservationId.get(key).toString();
    }
    public void setReservationId(int key, Object value) {
        this.reservationId.set(key, value);
    }
    
    public List getReservationType() {
        return reservationType;
    }
    public void setReservationType(List reservationType) {
        this.reservationType = reservationType;
    }
    public String getReservationType(int key) {
        return reservationType.get(key).toString();
    }
    public void setReservationType(int key, Object value) {
        this.reservationType.set(key, value);
    }
    
        
    public void addToPriority(String priority) {
        this.priority.add(priority);
    }

    public void addToReservationId(String reservationId) {
        this.reservationId.add(reservationId);
    }

    public void addToReservationType(String reservationType) {
        this.reservationType.add(reservationType);
    }
    
   public void addBlankRows() {
       for (int i=0; i<RESV_ROWS_ADDED; i++) {
           addToPriority(""+Constants.RESV_DEFAULT_PRIORITY);
           addToReservationId(Constants.BLANK_OPTION_VALUE);
           addToReservationType(Constants.BLANK_OPTION_VALUE);
       }
    }
    
    public void clear() {
        this.reservationId.clear();
        this.reservationType.clear();
        this.priority.clear();
    }

    public void addReservation(Reservation resv) {
        addToReservationId(resv.getUniqueId().toString());
        addToPriority(resv.getPriority().toString());
        addToReservationType(resv.getReservationType().getUniqueId().toString());
    }

    public void removeRow(int rowNum) {
        if (rowNum>=0) {
            reservationId.remove(rowNum);
            reservationType.remove(rowNum);
            priority.remove(rowNum);
        }
    }
    
    /**
     * Checks that there are no duplicates and that all elements have a value
     * @param lst List of values
     * @param ignoreDuplicates true ignores duplicate values
     * @return true if checks ok, false otherwise
     */
    public boolean checkList(List lst, boolean ignoreDuplicates) {
        
        HashMap map = new HashMap();
        for(int i=0; i<lst.size(); i++) {
            String value = ((String) lst.get(i));
            // No selection made            
            if( value==null || value.trim().equals(Constants.BLANK_OPTION_VALUE)) {
                return false;
            }
            
            // Duplicate selection made
            if(!ignoreDuplicates && map.get(value.trim())!=null) {
                lst.set(i, Constants.BLANK_OPTION_VALUE);
                return false;
            }
            map.put(value, value);
        }
        return true;
    }
    

    /**
     * Checks list for invalid numbers
     * @param lst
     * @param ignoreNulls ignore null / blank values
     * @param minValue check for min value if not null
     * @param maxValue check for max value if not null
     * @return true if all checks pass, false otherwise
     */
    public boolean checkListNumber(List lst, boolean ignoreNulls, Integer minValue, Integer maxValue) {
        
        for(int i=0; i<lst.size(); i++) {
            String value = ((String) lst.get(i));
            int intval = 0;
            
            // No selection made            
            if(value==null || value.trim().equals(Constants.BLANK_OPTION_VALUE)) {
                if (!ignoreNulls)
                    return false;
                else 
                    continue;
            }
            
            // Check is number 
            try {
                intval = Integer.parseInt(value);
            }
            catch (NumberFormatException nfe) {
                return false;
            }
            
            // Check min value
            if (minValue!=null && intval<minValue.intValue()) {
                return false;
            }
            
            // Check max value
            if (maxValue!=null && intval>maxValue.intValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks list for invalid dates
     * @param lst
     * @param ignoreNulls ignore null / blank values
     * @return true if all checks pass, false otherwise
     */
    public boolean checkListDate(List lst, boolean ignoreNulls) {
        for(int i=0; i<lst.size(); i++) {
            String value = ((String) lst.get(i));
            
            // No selection made            
            if(value==null || value.trim().equals(Constants.BLANK_OPTION_VALUE)) {
                if (!ignoreNulls)
                    return false;
                else 
                    continue;
            }
            
            // Check valid date
            Date dateval = null;
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setLenient(false);
            try {
                dateval = sdf.parse(value);
            } 
            catch (ParseException pe) {
                return false;
            }            
            
            // Check date is in future
            Date now = new Date();
            if (dateval.before(now))
                return false;
        }
        return true;
    }
}
