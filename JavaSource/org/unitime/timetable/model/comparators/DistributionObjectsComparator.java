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

import org.unitime.timetable.model.DistributionObject;


/**
 * Compares 2 distribution objects based on sequence number
 * 
 * @author Heston Fernandes
 */
public class DistributionObjectsComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
        
        // Check if objects are of class Instructional Offering
        if (!(o1 instanceof DistributionObject)) {
            throw new ClassCastException(
                    "o1 Class must be of type DistributionObject");
        }
        if (!(o2 instanceof DistributionObject)) {
            throw new ClassCastException(
                    "o2 Class must be of type DistributionObject");
        }

        DistributionObject do1 = (DistributionObject) o1;
        DistributionObject do2 = (DistributionObject) o2;

        return (do1.getSequenceNumber().compareTo(do2.getSequenceNumber()));
    }
}
