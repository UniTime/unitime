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

