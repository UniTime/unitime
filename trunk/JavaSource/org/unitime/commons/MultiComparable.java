/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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
package org.unitime.commons;

public class MultiComparable implements Comparable<MultiComparable>{
    private Comparable[] iCmp;
    
    public MultiComparable(Comparable[] cmp) {
        iCmp = cmp;
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
        for (int i=0;i<iCmp.length;i++) {
            int cmp = iCmp[i].compareTo(mc.iCmp[i]);
            if (cmp!=0) return cmp;
        }
        return 0;
    }
}
