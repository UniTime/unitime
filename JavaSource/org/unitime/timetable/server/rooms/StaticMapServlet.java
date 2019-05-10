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
package org.unitime.timetable.server.rooms;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;

/**
 * @author Tomas Muller
 */
public class StaticMapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static int sTileSize = 256;
	private Map<Integer,Map<Integer,Map<Integer,byte[]>>> iCache = new HashMap<Integer,Map<Integer,Map<Integer,byte[]>>>();
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}
	
	protected synchronized byte[] get(int zoom, int x, int y) {
		Map<Integer,Map<Integer,byte[]>> xyCache = iCache.get(zoom);
		if (xyCache == null) return null;
		Map<Integer,byte[]> yCache = xyCache.get(x);
		if (yCache == null) return null;
		return yCache.get(y);
	}
	
	protected synchronized void put(int zoom, int x, int y, byte[] cache) {
		Map<Integer,Map<Integer,byte[]>> xyCache = iCache.get(zoom);
		if (xyCache == null) {
			xyCache = new HashMap<Integer,Map<Integer,byte[]>>();
			iCache.put(zoom, xyCache);
		}
		Map<Integer,byte[]> yCache = xyCache.get(x);
		if (yCache == null) {
			yCache = new HashMap<Integer,byte[]>();
			xyCache.put(x, yCache);
		}
		yCache.put(y, cache);
	}
	
	protected BufferedImage fetchTile(int zoom, int x, int y) throws MalformedURLException, IOException {
		byte[] cached = get(zoom, x, y);
		if (cached == null) {
			String tileURL =ApplicationProperty.RoomUseLeafletMapTiles.value().replace("{s}", String.valueOf((char)('a' + ToolBox.random(3)))).replace("{z}", String.valueOf(zoom)).replace("{x}", String.valueOf(x)).replace("{y}", String.valueOf(y));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = null;
			try {
				in = new URL(tileURL).openStream();
				byte[] byteChunk = new byte[4096];
				int n;
				while ((n = in.read(byteChunk)) > 0)
					out.write(byteChunk, 0, n);
			} finally {
				in.close();
			}
			cached = out.toByteArray();
			put(zoom, x, y, cached);	
		}
		return ImageIO.read(new ByteArrayInputStream(cached)); 
	}
	
	protected double lonToTile(double lon, int zoom) {
		return ((lon + 180.0) / 360.0) * Math.pow(2.0, zoom);
	}
	
	protected double latToTile(double lat, int zoom) {
		return (1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2.0 * Math.pow(2.0, zoom);
	}
	
	protected BufferedImage createBaseMap(int width, int height, double lat, double lon, int zoom) throws MalformedURLException, IOException {
		double centerX = lonToTile(lon, zoom);
		double centerY = latToTile(lat, zoom);
		
		int startX = (int)Math.floor(centerX - (width / 2.0) / sTileSize);
		int startY = (int)Math.floor(centerY - (height / 2.0) / sTileSize);
		int endX = (int)Math.ceil(centerX + (width / 2.0) / sTileSize);
		int endY = (int)Math.ceil(centerY + (height / 2.0) / sTileSize);
		
		double offsetX = -Math.floor((centerX - Math.floor(centerX)) * sTileSize);
		double offsetY = -Math.floor((centerY - Math.floor(centerY)) * sTileSize);
        offsetX += Math.floor(width / 2.0);
        offsetY += Math.floor(height / 2.0);
        offsetX += Math.floor(startX - Math.floor(centerX)) * sTileSize;
        offsetY += Math.floor(startY - Math.floor(centerY)) * sTileSize;
		
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = result.getGraphics();

        for (int x = startX; x <= endX; x++)
        	for (int y = startY; y <= endY; y++) {
        		BufferedImage tile = fetchTile(zoom, x, y);
        		int destX = (x - startX) * sTileSize + (int)offsetX;
        		int destY = (y - startY) * sTileSize + (int)offsetY;
        		g.drawImage(tile, destX, destY, null);
        	}
        
		BufferedImage shadow = ImageIO.read(StaticMapServlet.class.getClassLoader().getResourceAsStream("org/unitime/timetable/server/resources/marker-shadow.png"));
        g.drawImage(shadow, width / 2 - 12, height / 2 - 41, null);

        BufferedImage marker = ImageIO.read(StaticMapServlet.class.getClassLoader().getResourceAsStream("org/unitime/timetable/server/resources/marker.png"));
        g.drawImage(marker, width / 2 - 12, height / 2 - 41, null);
        
		return result;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String center = request.getParameter("center");
		int zoom = Integer.parseInt(request.getParameter("zoom"));
		String size = request.getParameter("size");
		double lat = Double.parseDouble(center.split(",")[0]);
		double lon = Double.parseDouble(center.split(",")[1]);
		int width = Integer.parseInt(size.split("[,x]")[0]);
		int height = Integer.parseInt(size.split("[,x]")[1]);
		BufferedImage image = createBaseMap(width, height, lat, lon, zoom);
		response.setContentType("image/png");
		response.setDateHeader("Date", System.currentTimeMillis());
		response.setDateHeader("Expires", System.currentTimeMillis() + 604800000l);
		response.setHeader("Cache-control", "public, max-age=604800");
		ImageIO.write(image, "PNG", response.getOutputStream());
	}

}
