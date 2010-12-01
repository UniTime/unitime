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
package org.unitime.commons;

import java.util.Collection;
import java.util.Comparator;

public class MultiComparable implements Comparable<MultiComparable>{
    private Comparable[] iCmp;
    private Comparator iStringCmp = new NaturalOrderComparator();
    
    public MultiComparable(Comparable[] cmp) {
        iCmp = cmp;
    }
    
    public MultiComparable(Collection cmp) {
        iCmp = new Comparable[cmp.size()];
        int idx = 0;
        for (Object c : cmp)
            iCmp[idx++] = (Comparable)c;
    }
    
    public MultiComparable(Comparable a) {
        iCmp = new Comparable[] {a};
    }
    
    public MultiComparable(Comparable a, Comparable b) {
        iCmp = new Comparable[] {a, b};
    }

    public MultiComparable(Comparable a, Comparable b, Comparable c) {
        iCmp = new Comparable[] {a, b, c};
    }

    public MultiComparable(Comparable a, Comparable b, Comparable c, Comparable d) {
        iCmp = new Comparable[] {a, b, c, d};
    }

    public MultiComparable(Comparable a, Comparable b, Comparable c, Comparable d, Comparable e) {
        iCmp = new Comparable[] {a, b, c, d, e};
    }
    
    public MultiComparable(Comparable a, Comparable b, Comparable c, Comparable d, Comparable e, Comparable f) {
        iCmp = new Comparable[] {a, b, c, d, e, f};
    }

    public MultiComparable(Comparable a, Comparable b, Comparable c, Comparable d, Comparable e, Comparable f, Comparable g) {
        iCmp = new Comparable[] {a, b, c, d, e, f, g};
    }

    public int compareTo(MultiComparable mc) {
        for (int i=0;i<Math.min(iCmp.length,mc.iCmp.length);i++) {
            if (iCmp[i]==null) {
                if (mc.iCmp[i]==null) continue;
                return -1;
            }
            if (mc.iCmp[i]==null) return 1;
            int cmp = 0;
            if (iCmp[i]!=null && iCmp[i] instanceof String && mc.iCmp[i]!=null && mc.iCmp[i] instanceof String)
                cmp = iStringCmp.compare(iCmp[i], mc.iCmp[i]);
            else
                cmp = iCmp[i].compareTo(mc.iCmp[i]);
            if (cmp!=0) return cmp;
        }
        if (iCmp.length>mc.iCmp.length) return 1;
        if (iCmp.length<mc.iCmp.length) return -1;
        return 0;
    }
    
    public Comparable[] getContent() { return iCmp; }
}
