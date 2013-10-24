/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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

import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.Marshaller;
import org.unitime.timetable.solver.jgroups.SolverContainer;

/**
 * Simple memory counter based on http://www.javaspecialists.eu/archive/Issue078.html
 */
public final class MemoryCounter {
	public static final MemorySizes sSizes = new MemorySizes();
	private final Map iVisited = new IdentityHashMap();
	private final Stack iStack = new Stack();

	public synchronized long estimate(Object obj) {
		assert iVisited.isEmpty();
		assert iStack.isEmpty();
		long result = _estimate(obj);
		while (!iStack.isEmpty()) {
			result += _estimate(iStack.pop());
		}
		iVisited.clear();
		return result;
	}

	private boolean skipObject(Object obj) {
		if (obj instanceof String) {
			// this will not cause a memory leak since
			// unused interned Strings will be thrown away
			if (obj == ((String) obj).intern()) {
				return true;
			}
		}
		if (obj instanceof Marshaller || obj instanceof EmbeddedCacheManager || obj instanceof Thread || obj instanceof Log || obj instanceof Logger || obj instanceof SolverContainer) return true;
		return (obj == null) || iVisited.containsKey(obj);
	}

	private long _estimate(Object obj) {
		if (skipObject(obj))
			return 0;
		if (obj instanceof Cache) {
			return _estimate(((Cache)obj).getAdvancedCache().getDataContainer().entrySet().toArray());
		}
		iVisited.put(obj, null);
		long result = 0;
		Class clazz = obj.getClass();
		if (clazz.isArray()) {
			return _estimateArray(obj);
		}
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (!Modifier.isStatic(fields[i].getModifiers())) {
					if (fields[i].getType().isPrimitive()) {
						result += sSizes.getPrimitiveFieldSize(fields[i]
								.getType());
					} else {
						result += sSizes.getPointerSize();
						fields[i].setAccessible(true);
						try {
							Object toBeDone = fields[i].get(obj);
							if (toBeDone != null) {
								iStack.add(toBeDone);
							}
						} catch (IllegalAccessException ex) {
							assert false;
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		result += sSizes.getClassSize();
		return roundUpToNearestEightBytes(result);
	}

	private long roundUpToNearestEightBytes(long result) {
		if ((result % 8) != 0) {
			result += 8 - (result % 8);
		}
		return result;
	}

	protected long _estimateArray(Object obj) {
		long result = 16;
		int length = Array.getLength(obj);
		if (length != 0) {
			Class arrayElementClazz = obj.getClass().getComponentType();
			if (arrayElementClazz.isPrimitive()) {
				result += length
						* sSizes.getPrimitiveArrayElementSize(arrayElementClazz);
			} else {
				for (int i = 0; i < length; i++) {
					result += sSizes.getPointerSize()
							+ _estimate(Array.get(obj, i));
				}
			}
		}
		return result;
	}

	public static class MemorySizes {
		private final Map primitiveSizes = new IdentityHashMap() {
			private static final long serialVersionUID = 1L;

			{
				put(boolean.class, new Integer(1));
				put(byte.class, new Integer(1));
				put(char.class, new Integer(2));
				put(short.class, new Integer(2));
				put(int.class, new Integer(4));
				put(float.class, new Integer(4));
				put(double.class, new Integer(8));
				put(long.class, new Integer(8));
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
}