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
