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
package org.unitime.timetable.model;

import java.util.Date;

import org.hibernate.Transaction;
import org.unitime.timetable.model.base.BaseMapTileCache;
import org.unitime.timetable.model.dao.MapTileCacheDAO;

public class MapTileCache extends BaseMapTileCache {
	private static final long serialVersionUID = 1L;

	public MapTileCache() {
		super();
	}
	
	public MapTileCache(int zoom, int x, int y) {
		super();
		setX(x); setY(y); setZ(zoom);
	}
	
	public boolean isTooOld() {
		return System.currentTimeMillis() - getTimeStamp().getTime() > 604800000l;
	}
	
	public static byte[] get(int zoom, int x, int y) {
		MapTileCache tile = MapTileCacheDAO.getInstance().get(new MapTileCache(zoom, x, y));
		return tile == null || tile.isTooOld() ? null : tile.getData();
	}
	
	public static void put(int zoom, int x, int y, byte[] data) {
		org.hibernate.Session hibSession = MapTileCacheDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			MapTileCache tile =  MapTileCacheDAO.getInstance().get(new MapTileCache(zoom, x, y), hibSession);
			if (tile == null) {
				tile = new MapTileCache();
				tile.setX(x); tile.setY(y); tile.setZ(zoom);
			}
			tile.setData(data);
			tile.setTimeStamp(new Date());
			hibSession.saveOrUpdate(tile);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		}
	}
}
