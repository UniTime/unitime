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

public class DynamicList<T> implements List<T> {

    /**
     * The factory creating new instances of the class 
     * which are included in the list
     */
    private DynamicListObjectFactory<T> objectFactory;

    /** list holding the values */
    private List<T> list;

    /**
     * function to be used to get a new instance from the decorator
     * @param list List
     * @param objectFactory object factory to create elements in the list
     */
    public static <T> List<T> getInstance(List<T> list, DynamicListObjectFactory<T> objectFactory) {
        
        return new DynamicList(list, objectFactory);
    }

    /**
     * Constructor
     * @param list List
     * @param objectFactory object factory to create elements in the list
     */
    private DynamicList(List<T> list,
            DynamicListObjectFactory<T> objectFactory) {

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
    public T get(int index) {
        int size = list.size();

        // Within bounds, get the object
        if (index < size) {
            T object = list.get(index);

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
            T object = objectFactory.create();
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
    public T set(int index, T element) {
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
    
    public void add(int index, T element) {
        list.add(index, element);
    }

    public boolean add(T o) {
        return list.add(o);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
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

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    public T remove(int index) {
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

    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <X> X[] toArray(X[] a) {
        return list.toArray(a);
    }

    public String toString() {
        return list.toString();
    }
}
