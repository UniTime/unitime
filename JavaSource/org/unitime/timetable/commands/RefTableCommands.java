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
