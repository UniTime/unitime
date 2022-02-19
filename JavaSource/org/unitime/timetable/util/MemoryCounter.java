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

import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.manager.EmbeddedCacheManager;
import org.unitime.commons.Debug;
import org.unitime.timetable.solver.jgroups.SolverContainer;

/**
 * Simple memory counter based on http://www.javaspecialists.eu/archive/Issue078.html
 */
public final class MemoryCounter {
	public static final MemorySizes sSizes = new MemorySizes();
	private Map iVisited = new IdentityHashMap();
	private Stack iStack = new Stack();
	
	public MemoryCounter() {}
	
	public synchronized long estimate(Object obj) {
		try {
			return new MemoryCounter().deepSizeOfObject(obj);
		} catch (Throwable t) {
			Debug.error("Failed to estimate size of " + obj.getClass().getSimpleName() + ": " + t.getMessage(), t);
			return 0;
		}
	}
	
	private boolean skipObject(Object obj) {
		if ((obj instanceof String) && (obj == ((String) obj).intern())) return true;
		if (obj instanceof Marshaller || obj instanceof EmbeddedCacheManager || obj instanceof Thread || obj instanceof Log || obj instanceof Logger || obj instanceof SolverContainer) return true;
		return (obj == null) || iVisited.containsKey(obj);
	}

	private static long roundUpToNearestEightBytes(long result) {
		if ((result % 8) != 0) {
			result += 8 - (result % 8);
		}
		return result;
	}

	public long deepSizeOfObject(Object obj) {
		try {
			long result = deepSizeOf(obj);
			while (!iStack.isEmpty()) {
				result += deepSizeOf(iStack.pop());
			}
			return result;
		} catch (Throwable e) {
			Debug.error("Unable to estimate size of " + obj.getClass().getSimpleName() + ": " + e.getMessage(), e);
			return 0;
		}
	}
	
	private long deepSizeOf(Object obj) {
		if (skipObject(obj)) return 0;
		if (obj instanceof Cache) {
			return deepSizeOf(((Cache)obj).getAdvancedCache().getDataContainer().entrySet().toArray());
		}
		iVisited.put(obj, null);
		Class clazz = obj.getClass();
		if (clazz.isArray()) {
			long result = 16;
			int length = Array.getLength(obj);
			if (length != 0) {
				Class arrayElementClazz = obj.getClass().getComponentType();
				if (arrayElementClazz.isPrimitive()) {
					result += length * sSizes.getPrimitiveArrayElementSize(arrayElementClazz);
				} else {
					for (int i = 0; i < length; i++) {
						result += sSizes.getPointerSize() + deepSizeOf(Array.get(obj, i));
					}
				}
			}
			return result;
		}
		long result = 0;		
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				for (Field f : clazz.getDeclaredFields()) {
		            if (!Modifier.isStatic(f.getModifiers())) { //skip statics
		            	if (f.getType().isPrimitive()) {
		            		result += sSizes.getPrimitiveFieldSize(f.getType());
		            	} else {
		            		result += sSizes.getPointerSize();
		            		long offset = UtilUnsafe.UNSAFE.objectFieldOffset(f);
							Object tempObject = UtilUnsafe.UNSAFE.getObject(obj, offset);
							if (tempObject != null)
								iStack.add(tempObject);
		            	}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		result += sSizes.getClassSize();
		return roundUpToNearestEightBytes(result);
	}
	
	public static class MemorySizes {
		private final Map primitiveSizes = new IdentityHashMap() {
			private static final long serialVersionUID = 1L;
			{
				put(boolean.class, 1);
				put(byte.class, 1);
				put(char.class, 2);
				put(short.class, 2);
				put(int.class, 4);
				put(float.class, 4);
				put(double.class, 8);
				put(long.class, 8);
			}
		};

		public int getPrimitiveFieldSize(Class clazz) {
			return ((Integer) primitiveSizes.get(clazz)).intValue();
		}

		public int getPrimitiveArrayElementSize(Class clazz) {
			return getPrimitiveFieldSize(clazz);
		}

		public int getPointerSize() {
			return 4;
		}

		public int getClassSize() {
			return 8;
		}
	}
	
	static class UtilUnsafe {
		public static final sun.misc.Unsafe UNSAFE;
		static {
			Object theUnsafe = null;
			Exception exception = null;
			try {
	            Class<?> uc = Class.forName("sun.misc.Unsafe");
	            Field f = uc.getDeclaredField("theUnsafe");
	            f.setAccessible(true);
	            theUnsafe = f.get(uc);
	        } catch (Exception e) { exception = e; }
	        UNSAFE = (sun.misc.Unsafe) theUnsafe;
	        if (UNSAFE == null) throw new Error("Could not obtain access to sun.misc.Unsafe", exception);
	    }
	    private UtilUnsafe() { }
	}
}