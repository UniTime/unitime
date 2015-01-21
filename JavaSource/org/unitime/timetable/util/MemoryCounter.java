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