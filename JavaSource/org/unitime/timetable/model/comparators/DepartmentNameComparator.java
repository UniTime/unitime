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
package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.Department;


/**
 * @author Heston Fernandes
 */
public class DepartmentNameComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        
        // Check if objects are of Department
        if (!(o1 instanceof Department)) {
            throw new ClassCastException(
                    "o1 Class must be of type Department");
        }
        if (!(o2 instanceof Department)) {
            throw new ClassCastException(
                    "o2 Class must be of type Department");
        }

        Department d1 = (Department) o1;
        Department d2 = (Department) o2;

        if (d1.getUniqueId() != null && d2.getUniqueId() != null) {
	        if (d1.getUniqueId().equals(d2.getUniqueId())) {
	            return 0;
	        }else {
	        	if (d1.getName().equals(d2.getName())){
        			return(d1.getSessionId().compareTo(d2.getSessionId()));
        		} else {
        			return (d1.getName().compareTo(d2.getName()));
        		}
	        }
        } else {
        	if (d1.getName() != null && d2.getName() != null){
        		if (d1.getName().equals(d2.getName())){
        			if (d1.getSessionId() != null && d2.getSessionId() != null){
        				return(d1.getSessionId().compareTo(d2.getSessionId()));
        			} else {
        				return(-1);
        			}
        		} else {
        			return (d1.getName().compareTo(d2.getName()));
        		}
        	} else {
    			return(-1);
    		}
        }
    }
}
