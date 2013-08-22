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
package org.unitime.timetable.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is a workaround to a bug in Struts 
 * (does not re-initialize the ArrayList with new values)
 * It overrides the get method to create a new element at 
 * the given index if one does not exist 
 * 
 * @author Heston Fernandes
 */

public class DynamicList implements List {

    /**
     * The factory creating new instances of the class 
     * which are included in the list
     */
    private DynamicListObjectFactory objectFactory;

    /** list holding the values */
    private List list;

    /**
     * function to be used to get a new instance from the decorator
     * @param list List
     * @param objectFactory object factory to create elements in the list
     */
    public static List getInstance(List list,
            DynamicListObjectFactory objectFactory) {
        
        return new DynamicList(list, objectFactory);
    }

    /**
     * Constructor
     * @param list List
     * @param objectFactory object factory to create elements in the list
     */
    private DynamicList(List list,
            DynamicListObjectFactory objectFactory) {

        if (objectFactory == null) {
            throw new IllegalArgumentException("Factory must not be null");
        }
        if (list == null) {
            throw new IllegalArgumentException("List must not be null");
        }
        this.list = list;
        this.objectFactory = objectFactory;
    }

    /**
     * If the list size is less than the index
     * expand the list to the given index by creating new entries
     * and initializing them 
     * @param index Index
     */
    public Object get(int index) {
        int size = list.size();

        // Within bounds, get the object
        if (index < size) {
            Object object = list.get(index);

            // item is a place holder, create new one, set and return
            if (object == null) {                
                object = objectFactory.create();
                list.set(index, object);
                return object;
            } 
            else {
                return object;
            }
        }
        
        // Out of bounds
        else {
            // Grow the list
            for (int i = size; i < index; i++) {
                list.add(null);
            }
            // Create the last object, set and return
            Object object = objectFactory.create();
            list.add(object);
            return object;
        }
    }

    /**
     * Sets the list at index with the object
     * If the index is out of bounds the list is expanded to the index
     * @param index Index
     * @object element Object to set at index
     */
    public Object set(int index, Object element) {
        int size = list.size();

        // Grow the list
        if (index >= size) {
            for (int i = size; i <= index; i++) {
                list.add(null);
            }
        }
        return list.set(index, element);
    }

    
    /*
     * From here on - just delegate methods to the list
     */
    
    public void add(int index, Object element) {
        list.add(index, element);
    }

    public boolean add(Object o) {
        return list.add(o);
    }

    public boolean addAll(int index, Collection c) {
        return list.addAll(index, c);
    }

    public boolean addAll(Collection c) {
        return list.addAll(c);
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    public boolean equals(Object obj) {
        return list.equals(obj);
    }

    public int hashCode() {
        return list.hashCode();
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return list.listIterator();
    }

    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }

    public int size() {
        return list.size();
    }

    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

    public String toString() {
        return list.toString();
    }
}
