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
package org.unitime.timetable.util;

/**
 * @author Heston Fernandes
 */
public class ComboBoxLookup implements Comparable {

    private String label;
    private String value;
    
    public ComboBoxLookup(String label, String value) {
        this.label = label;
        this.value = value;
    }
    
    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    public int compareTo(Object o2) {
        // Check if objects are of class ComboBoxLookup
        if (! (o2 instanceof ComboBoxLookup)){
            throw new ClassCastException("o2 Class must be of type ComboBoxLookup");
        }
        
        ComboBoxLookup cbl2 = (ComboBoxLookup) o2;
        
        return getLabel().compareTo(cbl2.getLabel());
    }
    
}
