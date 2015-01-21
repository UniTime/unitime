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

import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.SimpleItypeConfig;


/**
 * Compares SimpleItypeConfig objects based on Itype
 * 
 * @author Heston Fernandes
 */
public class SicComparator implements Comparator {

    /**
     * Compares SimpleItypeConfig objects based on Itype
     * @param o1 SimpleItypeConfig
     * @param o2 SimpleItypeConfig
     * @return 0 if equal, -1 if o1<o2, +1 if o1>o2
     */
    public int compare(Object o1, Object o2) {

        if(!(o1 instanceof SimpleItypeConfig) || o1==null)
            throw new RuntimeException("Object o1 must be of type SimpleItypeConfig and cannot be null");
        
        if(!(o2 instanceof SimpleItypeConfig) || o2==null)
            throw new RuntimeException("Object o2 must be of type SimpleItypeConfig and cannot be null");
        
        SimpleItypeConfig sic1 = (SimpleItypeConfig) o1; 
        SimpleItypeConfig sic2 = (SimpleItypeConfig) o2; 
        
        ItypeDesc id1 = sic1.getItype();
        if(id1==null)
            throw new RuntimeException("Object o1 does not have an assigned Itype");

        ItypeDesc id2 = sic2.getItype();
        if(id2==null)
            throw new RuntimeException("Object o2 does not have an assigned Itype");
        
        int itype1 = id1.getItype().intValue();
        int itype2 = id2.getItype().intValue();
        
        int retValue = 0;
        if(itype1>itype2) retValue = 1;
        if(itype1<itype2) retValue = -1;
        
        return retValue;
    }        
}

