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

import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;


/**
 * Compares ManagerRole or Roles objects and orders by role reference
 * 
 * @author Heston Fernandes
 */
public class RolesComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        
        if (o1==null || o2==null)
            return 0;
        
        // Check if ManagerRole object
        if ((o1 instanceof ManagerRole) && (o2 instanceof ManagerRole) ) {
	        ManagerRole r1 = (ManagerRole) o1;
	        ManagerRole r2 = (ManagerRole) o2;
	        
	        return (r1.getRole().getReference().compareTo(r2.getRole().getReference()));
        }

        // Check if Roles object
        if ((o1 instanceof Roles) && (o2 instanceof Roles) ) {
            Roles r1 = (Roles) o1;
            Roles r2 = (Roles) o2;
	        
	        return (r1.getReference().compareTo(r2.getReference()));
        }

        // All other cases
        return 0;        
    }

}
