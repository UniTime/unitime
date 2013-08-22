/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.io.Serializable;
import java.util.Vector;

/**
 * Config for user manipulations
 * Not stored in database till user hits commit 
 */
public class SimpleItypeConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Generates unique ids for itypes 
    private static long currId = 0;

    private long id;
    private long subpartId;
    private ItypeDesc itype;
    private SimpleItypeConfig parent;
    private Vector subparts;
    private int numClasses;
    private int numRooms;
    private int minLimitPerClass;
    private int maxLimitPerClass;
    private int minPerWeek;
    private float roomRatio;
    private boolean disabled;
    private boolean notOwned;
    private boolean hasError;
    private long managingDeptId;
    
    /** Request attribute name for user defined config **/
    public static String CONFIGS_ATTR_NAME = "configsList";

    /** Default Constructor **/
    public SimpleItypeConfig(ItypeDesc itype) {
        this(itype, -1L);
    }
    
    public SimpleItypeConfig (ItypeDesc itype, long subpartId) {
        if(currId>Long.MAX_VALUE)
            currId = 0;
        this.id = ++currId;
        this.subpartId = subpartId;
        this.itype = itype;
        this.numClasses=-1;
        this.numRooms=1;
        this.minLimitPerClass=-1;
        this.maxLimitPerClass=-1;
        this.roomRatio=1.0f;
        this.minPerWeek=-1;
        this.managingDeptId=-1;
        subparts = new Vector();
        disabled = false;
        notOwned = false;
        hasError = false;
    }
    
    /**
     * Add subpart to itype
     * @param config subpart config
     */
    public void addSubpart(SimpleItypeConfig config) {
        config.setParent(this);
        subparts.addElement(config);
    }    
    
    /**
     * @return Returns the subparts.
     */
    public Vector getSubparts() {
        return subparts;
    }
    /**
     * @param subparts The children to set.
     */
    public void setSubparts(Vector subparts) {
        this.subparts = subparts;
    }
    /**
     * @return Returns the itype.
     */
    public ItypeDesc getItype() {
        return itype;
    }
    /**
     * @param itype The itype to set.
     */
    public void setItype(ItypeDesc itype) {
        this.itype = itype;
    }
    /**
     * @return Returns the minLimitPerClass.
     */
    public int getMinLimitPerClass() {
        return minLimitPerClass;
    }
    /**
     * @param minLimitPerClass The minLimitPerClass to set.
     */
    public void setMinLimitPerClass(int minLimitPerClass) {
        this.minLimitPerClass = minLimitPerClass;
    }
    /**
     * @return Returns the maxLimitPerClass.
     */
    public int getMaxLimitPerClass() {
        return maxLimitPerClass;
    }
    /**
     * @param maxLimitPerClass The maxLimitPerClass to set.
     */
    public void setMaxLimitPerClass(int maxLimitPerClass) {
        this.maxLimitPerClass = maxLimitPerClass;
    }
    /**
     * @return Returns the minPerWeek.
     */
    public int getMinPerWeek() {
        return minPerWeek;
    }
    /**
     * @param minPerWeek The minPerWeek to set.
     */
    public void setMinPerWeek(int minPerWeek) {
        this.minPerWeek = minPerWeek;
    }
    /**
     * @return Returns the numClasses.
     */
    public int getNumClasses() {
        return numClasses;
    }
    /**
     * @param numClasses The numClasses to set.
     */
    public void setNumClasses(int numClasses) {
        this.numClasses = numClasses;
    }    
    /**
     * @return Returns the id.
     */
    public long getId() {
        return id;
    }
    /**
     * @return Returns the parent.
     */
    public SimpleItypeConfig getParent() {
        return parent;
    }
    /**
     * @param parent The parent to set.
     */
    public void setParent(SimpleItypeConfig parent) {
        this.parent = parent;
    }    
    /**
     * @return Returns the roomRatio.
     */
    public float getRoomRatio() {
        return roomRatio;
    }
    /**
     * @param roomRatio The roomRatio to set.
     */
    public void setRoomRatio(float roomRatio) {
        this.roomRatio = roomRatio;
    }
    
    /**
     * @return Returns the disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }
    /**
     * @param disabled The disabled to set.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @return Returns the notOwned.
     */
    public boolean isNotOwned() {
        return notOwned;
    }
    /**
     * @param notOwned The notOwned to set.
     */
    public void setNotOwned(boolean notOwned) {
        this.notOwned = notOwned;
    }
    
    public long getSubpartId() {
        return subpartId;
    }
    public void setSubpartId(long subpartId) {
        this.subpartId = subpartId;
    }
    
    public boolean getHasError() {
        return hasError;
    }
    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
    
    public int getNumRooms() {
        return numRooms;
    }
    public void setNumRooms(int numRooms) {
        this.numRooms = numRooms;
    }    
    
    public long getManagingDeptId() {
        return managingDeptId;
    }
    public void setManagingDeptId(long managingDeptId) {
        this.managingDeptId = managingDeptId;
    }
}
