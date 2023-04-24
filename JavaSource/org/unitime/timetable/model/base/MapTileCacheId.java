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
package org.unitime.timetable.model.base;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;
import org.unitime.timetable.model.MapTileCache;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public class MapTileCacheId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iZ;
	private Integer iX;
	private Integer iY;

	public MapTileCacheId() {}

	public MapTileCacheId(Integer z, Integer x, Integer y) {
		iZ = z;
		iX = x;
		iY = y;
	}

	@Id
	@Column(name="z")
	public Integer getZ() { return iZ; }
	public void setZ(Integer z) { iZ = z; }

	@Id
	@Column(name="x")
	public Integer getX() { return iX; }
	public void setX(Integer x) { iX = x; }

	@Id
	@Column(name="y")
	public Integer getY() { return iY; }
	public void setY(Integer y) { iY = y; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MapTileCache)) return false;
		MapTileCache mapTileCache = (MapTileCache)o;
		if (getZ() == null || mapTileCache.getZ() == null || !getZ().equals(mapTileCache.getZ())) return false;
		if (getX() == null || mapTileCache.getX() == null || !getX().equals(mapTileCache.getX())) return false;
		if (getY() == null || mapTileCache.getY() == null || !getY().equals(mapTileCache.getY())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getZ() == null || getX() == null || getY() == null) return super.hashCode();
		return getZ().hashCode() ^ getX().hashCode() ^ getY().hashCode();
	}

}
