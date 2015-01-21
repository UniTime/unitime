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
package org.unitime.commons;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author Tomas Muller
 */
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
