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
package org.unitime.timetable.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.dao.RefTableEntryDAO;


/**
 * @author Stephanie Schluttenhofer
 *
 * methods to return ref table information
 */
public class RefTableCommands {

    /**
     * 
     */
    public RefTableCommands() {
        super();
     }

    public static List validLabels(Class refClass){
        Session hSession = (new RefTableEntryDAO()).getSession();
        return hSession.createQuery("select ref.label from " + refClass.getName() + " ref").list();
    }

    public static List validReferences(Class refClass){
        Session hSession = (new RefTableEntryDAO()).getSession();
        return hSession.createQuery("select ref.reference from " + refClass.getName() + " ref").list();
    }
    
    public static String referenceForLabel(Class refClass, String label){
        String ref;
        
        Session hSession = (new RefTableEntryDAO()).getSession();
        ArrayList list = (ArrayList) hSession.createQuery("select ref.reference from " + refClass.getName() + " ref where ref.label = '" + label + "'").list();
        if (list.size() != 1){
            ref = null;          
        }
         else {
            ref = (String) list.get(0);
         }
        return ref;
    }

    public static String referenceForLabel(String label, ArrayList list) throws HibernateException {
        Iterator it = list.iterator();
        String reference = new String();
        RefTableEntry refEntry = null;
        while (it.hasNext() && reference == ""){
            refEntry = (RefTableEntry) it.next();
            if(refEntry.getLabel().equals(label)){
                reference = refEntry.getReference();
            }       
        }
        return(reference);
      }
    
    public static String labelForReference(Class refClass, String reference){
        String label;
        
        Session hSession = (new RefTableEntryDAO()).getSession();
        ArrayList list = (ArrayList) hSession.createQuery("select ref.label from " + refClass.getName() + " ref where ref.reference = '" + reference + "'").list();
        if (list.size() != 1){
            label = null;          
        }
         else {
            label = (String) list.get(0);
         }
        return label;
    }
   

}
