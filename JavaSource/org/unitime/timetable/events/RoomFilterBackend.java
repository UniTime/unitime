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
package org.unitime.timetable.events;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.hibernate.type.StringType;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomFilterRpcRequest.class)
public class RoomFilterBackend extends FilterBoxBackend<RoomFilterRpcRequest> {
	private DistanceMetric iMetrics;
	private static double EPSILON = 0.000001;
	private static DecimalFormat sCDF = new DecimalFormat("0.000000");
	private static DecimalFormat sNF = new DecimalFormat("0000");
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	private static enum Size {
		eq, lt, gt, le, ge
	};

	@Override
	public void load(RoomFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		Set<String> departments = request.getOptions("department");
		
		Set<Long> userDepts = null;
		if (request.hasOption("user")) {
			userDepts = new HashSet<Long>(
					TimetableManagerDAO.getInstance().getSession().createQuery(
							"select d.uniqueId from TimetableManager m inner join m.departments d where " +
							"m.externalUniqueId = :user and d.session.uniqueId = :sessionId")
							.setLong("sessionId", request.getSessionId())
							.setString("user", request.getOption("user"))
							.setCacheable(true).list()
					);
		}
		
		fixRoomFeatureTypes(request);
		
		Map<Long, Entity> types = new HashMap<Long, Entity>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), null, -1, null, "type", context)) {
			Entity type = types.get(location.getRoomType().getUniqueId());
			if (type == null) {
				type = new Entity(location.getRoomType().getUniqueId(), location.getRoomType().getReference(), location.getRoomType().getLabel(), "order", sNF.format(location.getRoomType().getOrd()), "translated-value", location.getRoomType().getLabel());
				types.put(type.getUniqueId(), type);
			}
			type.incCount();
		}
		response.add("type", new TreeSet<Entity>(types.values()));
		
		Map<String, Map<Long, Entity>> featuresByType = new HashMap<String, Map<Long, Entity>>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), null, -1, null, null, context)) {
			for (RoomFeature rf: location.getFeatures()) {
				if (rf instanceof GlobalRoomFeature || (rf instanceof DepartmentRoomFeature && departments != null && departments.contains(((DepartmentRoomFeature)rf).getDepartment().getDeptCode()))) {
					if (showRoomFeature(rf.getFeatureType())) {
						String type = (rf.getFeatureType() == null ? "feature" : rf.getFeatureType().getReference());
						Map<Long, Entity> features = featuresByType.get(type);
						if (features == null) {
							features = new HashMap<Long, Entity>();
							featuresByType.put(type, features);
						}
						Entity feature = features.get(rf.getUniqueId());
						if (feature == null) {
							feature = new Entity(rf.getUniqueId(), rf.getAbbv(), rf.getLabel(), "translated-value", rf.getLabel());
							features.put(feature.getUniqueId(), feature);
						}
						feature.incCount();
					}
				}
			}
		}
		for (String type: new TreeSet<String>(featuresByType.keySet())) {
			response.add(type.replace(' ', '_'), new TreeSet<Entity>(featuresByType.get(type).values()));
		}
		
		Map<Long, Entity> groups = new HashMap<Long, Entity>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), null, -1, null, "group", context)) {
			for (RoomGroup rg: location.getRoomGroups()) {
				if (rg.isGlobal() || (departments != null && departments.contains(rg.getDepartment().getDeptCode()))) {
					Entity group = groups.get(rg.getUniqueId());
					if (group == null) {
						group = new Entity(rg.getUniqueId(), rg.getAbbv(), rg.getName(), "translated-value", rg.getName());
						groups.put(group.getUniqueId(), group);
					}
					group.incCount();
				}
			}
		}
		response.add("group", new TreeSet<Entity>(groups.values()));
		
		Map<Long, Entity> buildings = new HashMap<Long, Entity>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), null, -1, null, "building", context)) {
			if (location instanceof Room) {
				Room room = (Room)location;
				Entity building = buildings.get(room.getBuilding().getUniqueId());
				if (building == null) {
					building = new Entity(room.getBuilding().getUniqueId(), room.getBuilding().getAbbreviation(), room.getBuilding().getAbbrName());
					buildings.put(building.getUniqueId(), building);
				}
				building.incCount();
			}
		}
		response.add("building", new TreeSet<Entity>(buildings.values()));

		Entity managed = new Entity(0l, "Managed", MESSAGES.labelDepartmentManagedRooms(), "translated-value", MESSAGES.attrDepartmentManagedRooms());
		Map<Long, Entity> exams = null;
		if (context.hasPermission(request.getSessionId(), Right.Examinations) || context.hasPermission(request.getSessionId(), Right.ExaminationSchedule)) {
			exams = new HashMap<Long, Entity>();
			for (ExamType type: ExamType.findAllApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable)) {
				Entity e = new Entity(-type.getUniqueId(), type.getReference(), MESSAGES.examinationRooms(type.getLabel()));
				exams.put(type.getUniqueId(), e);
			}
		}
		Map<Long, Entity> depts = new HashMap<Long, Entity>();
		boolean eventRooms = (request.hasOptions("flag") && (request.getOptions("flag").contains("event") || request.getOptions("flag").contains("Event")));
		boolean allRooms = (request.hasOptions("flag") && (request.getOptions("flag").contains("all") || request.getOptions("flag").contains("All")));
		boolean deptIndep = context.hasPermission(Right.DepartmentIndependent); 
		for (Location location: locations(request.getSessionId(), request.getOptions(), null, -1, null, "department", context)) {
			Department evtDept = (location.getEventDepartment() != null && location.getEventDepartment().isAllowEvents() ? location.getEventDepartment() : null);
			boolean isManaged = false;
			if (eventRooms) {
				Entity department = depts.get(location.getEventDepartment().getUniqueId());
				if (department == null) {
					department = new Entity(location.getEventDepartment().getUniqueId(), location.getEventDepartment().getDeptCode(), location.getEventDepartment().getDeptCode() + " - " + location.getEventDepartment().getName());
					depts.put(department.getUniqueId(), department);
				}
				department.incCount();
				if (deptIndep || (userDepts != null && userDepts.contains(location.getEventDepartment().getUniqueId()))) isManaged = true;
			} else {
				for (RoomDept rd: location.getRoomDepts()) {
					if (!deptIndep && !allRooms && (userDepts == null || !(userDepts.contains(rd.getDepartment().getUniqueId())))) continue;
					if (evtDept != null && rd.getDepartment().equals(evtDept)) evtDept = null;
					Entity department = depts.get(rd.getDepartment().getUniqueId());
					if (department == null) {
						department = new Entity(rd.getDepartment().getUniqueId(), rd.getDepartment().getDeptCode(),
								rd.getDepartment().getDeptCode() + " - " + rd.getDepartment().getName() + (rd.getDepartment().isExternalManager() ? " (" + rd.getDepartment().getExternalMgrLabel() + ")" : ""));
						depts.put(department.getUniqueId(), department);
					}
					department.incCount();
					if (deptIndep || (userDepts != null && userDepts.contains(rd.getDepartment().getUniqueId()))) isManaged = true;
				}
				if (evtDept != null && allRooms) {
					Entity department = depts.get(evtDept.getUniqueId());
					if (department == null) {
						department = new Entity(evtDept.getUniqueId(), evtDept.getDeptCode(),
								evtDept.getDeptCode() + " - " + evtDept.getName() + (evtDept.isExternalManager() ? " (" + evtDept.getExternalMgrLabel() + ")" : ""));
						depts.put(department.getUniqueId(), department);
					}
					if (deptIndep || (userDepts != null && userDepts.contains(evtDept.getUniqueId()))) isManaged = true;
					department.incCount();					
				}
			}
			if (exams != null && !exams.isEmpty()) {
				for (ExamType type: location.getExamTypes()) {
					Entity e = exams.get(type.getUniqueId());
					if (e != null) e.incCount();
				}
			}
			if (isManaged)
				managed.incCount();
		}
		if (managed.getCount() > 0)
			response.add("department",managed);
		if (exams != null && !exams.isEmpty())
			for (Entity e: new TreeSet<Entity>(exams.values()))
				if (e.getCount() > 0)
					response.add("department", e);
		response.add("department", new TreeSet<Entity>(depts.values()));
	}
	
	protected void fixRoomFeatureTypes(RoomFilterRpcRequest request) {
		for (RoomFeatureType type: RoomFeatureTypeDAO.getInstance().findAll())
			if (showRoomFeature(type) && request.hasOptions(type.getReference().replace(' ', '_')))
				for (String option: request.getOptions(type.getReference().replace(' ', '_')))
					request.addOption("feature", option);
	}
	
	public List<Location> locations(Long sessionId, RoomFilterRpcRequest filter, int limit, Map<Long, Double> room2distance, EventContext context) {
		fixRoomFeatureTypes(filter);
		return locations(sessionId, filter.getOptions(), new Query(filter.getText()), limit, room2distance, null, context);
	}
	
	protected List<Location> locations(Long sessionId, Map<String, Set<String>> options, Query query, int limit, Map<Long, Double> room2distance, String ignoreCommand, EventContext context) {
		org.hibernate.Session hibSession = RoomDAO.getInstance().getSession();

		RoomQuery rq = getQuery(sessionId, options, context);
		org.hibernate.Query q = rq.select().exclude(ignoreCommand).query(hibSession);
		List<Location> locations = q.setCacheable(true).list();
		
		Set<String> building = (options == null || "building".equals(ignoreCommand) ? null : options.get("building"));
		Set<String> size = (options == null || "size".equals(ignoreCommand) ? null : options.get("size"));
		Set<String> flag = (options == null || "flag".equals(ignoreCommand) ? null : options.get("flag"));
		boolean nearby = (flag != null && (flag.contains("nearby") || flag.contains("Nearby")));

		Set<String> featureTypes = new HashSet<String>();
		for (RoomFeatureType ft: RoomFeatureTypeDAO.getInstance().findAll())
			if (showRoomFeature(ft)) featureTypes.add(ft.getReference().toLowerCase().replace(' ', '_'));
		
		List<Location> ret = new ArrayList<Location>();
		for (Location location: locations) {
			if (query != null && !query.match(new LocationMatcher(location, featureTypes))) continue;
			if (nearby && building != null && !building.isEmpty() && (!(location instanceof Room) || !building.contains(((Room)location).getBuilding().getAbbreviation()))) continue;
			ret.add(location);
		}
		
		final Map<Long, Double> distances = (room2distance == null ? new Hashtable<Long, Double>() : room2distance);
		if (nearby && building != null && !building.isEmpty() && (limit <= 0 || ret.size() < limit)) {
			double allowedDistance = ApplicationProperty.EventNearByDistance.doubleValue();
			Set<Coordinates> coord = new HashSet<Coordinates>();
			for (Location location: ret)
				coord.add(new Coordinates(location));

			if (coord.isEmpty()) {
				for (Building b: (List<Building>)hibSession.createQuery("select b from Building b where" +
						" b.session.uniqueId = :sessionId and b.abbreviation in :building")
						.setLong("sessionId", sessionId)
						.setParameterList("building", building, new StringType())
						.list()) {
					coord.add(new Coordinates(-b.getUniqueId(), b.getCoordinateX(), b.getCoordinateY()));
				}
			}
			
			if (!coord.isEmpty()) {
				for (Location location: locations) {
					if (building != null && !building.isEmpty() && (location instanceof Room) && building.contains(((Room)location).getBuilding().getAbbreviation())) continue;
					if (query != null && !query.match(new LocationMatcher(location, featureTypes))) continue;
					Coordinates c = new Coordinates(location);
					Double distance = null;
					for (Coordinates x: coord) {
						double d = c.distance(x);
						if (distance == null || distance > d) distance = d;
					}
					if (distance != null && distance <= allowedDistance) {
						ret.add(location);
						if (distances != null) distances.put(location.getUniqueId(), distance);
					}
				}
			}
		}
		
		final boolean sortBySize = (size != null && !size.isEmpty());
		Collections.sort(ret, new Comparator<Location>() {
			@Override
			public int compare(Location l1, Location l2) {
				Double d1 = distances.get(l1.getUniqueId());
				Double d2 = distances.get(l2.getUniqueId());
				if (d1 == null && d2 != null) return -1;
				if (d1 != null && d2 == null) return 1;
				if (d1 != null) {
					int cmp = new Long(Math.round(d1)).compareTo(Math.round(d2));
					if (cmp != 0) return cmp;
				}
				if (sortBySize) {
					int cmp = new Integer(l1.getCapacity() != null ? l1.getCapacity() : Integer.MAX_VALUE).compareTo(l2.getCapacity() != null ? l2.getCapacity() : Integer.MAX_VALUE);
					if (cmp != 0) return cmp;
				}
				return l1.getLabel().compareTo(l2.getLabel());
			}
		});
		
		return (limit <= 0 || ret.size() < limit ? ret : ret.subList(0, limit));
	}
	
	private String suggestionQuery(String query) {
		if (query == null || query.isEmpty()) return query;
		if (!query.contains(":") && !query.contains("\""))
			return "starts:\"" + query + "\"";
		return query;
	}
	
	@Override
	public void suggestions(RoomFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		fixRoomFeatureTypes(request);

		Map<Long, Double> distances = new HashMap<Long, Double>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), new Query(suggestionQuery(request.getText())), 20, distances, null, context)) {
			String hint = location.getRoomTypeLabel() + ", " + location.getCapacity() + " seats";
			Double dist = distances.get(location.getUniqueId());
			if (dist != null) hint += ", " + Math.round(dist) + " m";
			response.addSuggestion(location.getLabel(), location.getLabel(), "(" + hint + ")");
		}
	}
	
	@Override
	public void enumarate(RoomFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		fixRoomFeatureTypes(request);

		Map<Long, Double> distances = new HashMap<Long, Double>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), new Query(request.getText()), -1, distances, null, context)) {
			Double dist = distances.get(location.getUniqueId());
			response.addResult(new Entity(
					location.getUniqueId(),
					location.getDisplayName(),
					location.getLabel(),
					"permId", location.getPermanentId().toString(),
					"type", location.getRoomType().getLabel(),
					"capacity", location.getCapacity().toString(),
					"distance", String.valueOf(dist == null ? 0l : Math.round(dist)),
					"overbook", context.hasPermission(location, Right.EventLocationOverbook) ? "1" : "0",
					"breakTime", String.valueOf(location.getEffectiveBreakTime()),
					"message", location.getEventMessage(),
					"ignoreRoomCheck", location.isIgnoreRoomCheck() ? "1" : "0"
					));
		}
	}
	
	public DistanceMetric getDistanceMetric() {
		if (iMetrics == null) {
			DataProperties config = new DataProperties();
			config.setProperty("Distances.Ellipsoid", ApplicationProperty.DistanceEllipsoid.value());
			config.setProperty("Distances.Speed", ApplicationProperty.EventDistanceSpeed.value());
			iMetrics = new DistanceMetric(new DataProperties(config));
			TravelTime.populateTravelTimes(iMetrics);
		}
		return iMetrics;
	}
	
	public class Coordinates {
		Long iId;
		Double iX, iY;
		public Coordinates(Long id, Double x, Double y) { iId = id; iX = x; iY = y; }
		public Coordinates(Location location) { iId = location.getUniqueId(); iX = location.getCoordinateX(); iY = location.getCoordinateY(); }
		
		public Long id() { return iId; }
		public Double x() { return iX; }
		public Double y() { return iY; }
		public boolean hasCoordinates() { return iX != null && iY != null; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Coordinates)) return false;
			Coordinates c = (Coordinates)o;
			if (!hasCoordinates()) return !c.hasCoordinates();
			else if (!c.hasCoordinates()) return false;
			return Math.abs(c.x() - x()) < EPSILON && Math.abs(c.y() - y()) < EPSILON;
		}
		
		public double distance(Coordinates c) {
			return (hasCoordinates() && c.hasCoordinates() ? getDistanceMetric().getDistanceInMeters(id(), x(), y(), c.id(), c.x(), c.y()) : Double.POSITIVE_INFINITY); 
		}
		
		public double distance(Location location) {
			return distance(new Coordinates(location));
		}
		
		public String toString() {
			return (hasCoordinates() ? sCDF.format(iX) + "," + sCDF.format(iY) : "");
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
	}
	
	public class LocationMatcher implements TermMatcher {
		private Location iLocation;
		private Set<String> iFeatureTypes = null;
		
		LocationMatcher(Location location, Set<String> featureTypes) {
			iLocation = location;
			iFeatureTypes = featureTypes;
		}
		
		public Location getLocation() { return iLocation; }

		@Override
		public boolean match(String attr, String term) {
			if (attr == null || attr.isEmpty()) {
				return term.isEmpty() || has(getLocation().getLabel(), term) || has(getLocation().getDisplayName(), term);
			} else if ("feature".equals(attr) || (iFeatureTypes != null && iFeatureTypes.contains(attr.toLowerCase()))) {
				for (RoomFeature rf: getLocation().getFeatures())
					if (rf instanceof GlobalRoomFeature && (eq(rf.getAbbv(), term) || has(rf.getLabel(), term))) return true;
				return false;
			} else if ("group".equals(attr)) {
				for (RoomGroup rg: getLocation().getRoomGroups())
					if (rg.isGlobal() && (eq(rg.getAbbv(), term) || has(rg.getName(), term))) return true;
				return false;
			} else if ("type".equals(attr)) {
				return eq(getLocation().getRoomType().getReference(), term) || has(getLocation().getRoomType().getLabel(), term);
			} else if ("room".equals(attr)) {
				return has(getLocation().getLabel(), term) || has(getLocation().getDisplayName(), term);
			} else if ("starts".equals(attr)) {
				return getLocation().getLabel().toLowerCase().startsWith(term.toLowerCase()) || (getLocation() instanceof Room && ((Room)getLocation()).getRoomNumber().toLowerCase().startsWith(term.toLowerCase()));
			} else if ("contains".equals(attr)) {
				return getLocation().getLabel().toLowerCase().contains(term.toLowerCase()) || (getLocation() instanceof Room && ((Room)getLocation()).getRoomNumber().toLowerCase().contains(term.toLowerCase()));
			} else if ("building".equals(attr) || "bldg".equals(attr)) {
				if (getLocation() instanceof Room) {
					Building building = ((Room)getLocation()).getBuilding();
					return eq(building.getAbbreviation(), term) || has(building.getName(), term);
				}
				return false;
			} else if ("size".equals(attr)) {
				int min = 0, max = Integer.MAX_VALUE;
				Size prefix = Size.eq;
				String number = term;
				if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
				else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
				else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
				else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
				else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
				try {
					int a = Integer.parseInt(number);
					switch (prefix) {
						case eq: min = max = a; break; // = a
						case le: max = a; break; // <= a
						case ge: min = a; break; // >= a
						case lt: max = a - 1; break; // < a
						case gt: min = a + 1; break; // > a
					}
				} catch (NumberFormatException e) {}
				if (term.contains("..")) {
					try {
						String a = term.substring(0, term.indexOf('.'));
						String b = term.substring(term.indexOf("..") + 2);
						min = Integer.parseInt(a); max = Integer.parseInt(b);
					} catch (NumberFormatException e) {}
				}
				return min <= getLocation().getCapacity() && getLocation().getCapacity() <= max;
			} else if ("flag".equals(attr) && "event".equalsIgnoreCase(term)) {
				return getLocation().getEventDepartment() != null && getLocation().getEventDepartment().isAllowEvents() && getLocation().getEffectiveEventStatus() != RoomTypeOption.Status.NoEventManagement;
			} else if ("department".equals(attr) || "dept".equals(attr) || "event".equals(attr) || "control".equals(attr)) {
				if ("event".equalsIgnoreCase(term))
					return getLocation().getEventDepartment() != null && getLocation().getEventDepartment().isAllowEvents() && getLocation().getEffectiveEventStatus() != RoomTypeOption.Status.NoEventManagement;
				else if ("managed".equals(term))
					return false; // not supported
				else {
					if (!"control".equals(attr))
						if (getLocation().getEventDepartment() != null && (eq(getLocation().getEventDepartment().getDeptCode(), term) || eq(getLocation().getEventDepartment().getAbbreviation(), term) || has(getLocation().getEventDepartment().getName(), term)))
							return true;
					if (!"event".equals(attr)) {
						for (RoomDept rd: getLocation().getRoomDepts()) {
							if ("control".equals(attr) && !rd.isControl()) continue;
							if (eq(rd.getDepartment().getDeptCode(), term) || eq(rd.getDepartment().getAbbreviation(), term) || has(rd.getDepartment().getName(), term)
									|| (rd.getDepartment().isExternalManager() && (eq(rd.getDepartment().getExternalMgrAbbv(), term) || has(rd.getDepartment().getExternalMgrLabel(), term))))
								return true;
						}
						if (!"control".equals(attr))
							for (ExamType t: getLocation().getExamTypes()) {
								if (eq(t.getReference(), term) || has(t.getLabel(), term)) return true;
							}
					}
					return false;
				}
			} else {
				return true;
			}
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			if (eq(name, term)) return true;
			for (String t: name.split(" |,"))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}

		
	}
	
	public static String toCommand(String label) {
		String ret = "";
		for (String word: label.toLowerCase().split(" ")) {
			if (ret.isEmpty() || word.length() <= 1)
				ret += word;
			else
				ret += word.substring(0,1).toUpperCase() + word.substring(1);
		}
		return ret;
	}
	
	protected boolean checkEventStatus() { return true; }
	
	protected boolean showRoomFeature(RoomFeatureType type) {
		return type == null || type.isShowInEventManagement(); 
	}
	
	public RoomQuery getQuery(Long sessionId, Map<String, Set<String>> options, EventContext context) {
		RoomQuery query = new RoomQuery(sessionId);
		
		Set<String> types = (options == null ? null : options.get("type"));
		if (types != null && !types.isEmpty()) {
			String type = "";
			int id = 0;
			for (String s: types) {
				type += (type.isEmpty() ? "" : ",") + ":Xt" + id;
				query.addParameter("type", "Xt" + id, s);
				id++;
			}
			query.addWhere("type", "l.roomType.label in (" + type + ") or l.roomType.reference in (" + type + ")");
		}
		
		Set<String> departments = (options == null ? null : options.get("department"));
		String department = (departments == null || departments.isEmpty() ? null : departments.iterator().next());
		Set<String> features = (options == null ? null : options.get("feature"));
		if (features != null && !features.isEmpty()) {
			String from = "";
			String where = "";
			int id = 0;
			for (String s: features) {
				if (department == null) {
					from += (from.isEmpty() ? "" : " ") + "inner join l.features f" + id;
					where += (where.isEmpty() ? "" : " and ") + " (f" + id + ".label = :Xf" + id + " or f" + id + ".abbv = :Xf" + id + ") and f" + id + ".class = GlobalRoomFeature";
				} else {
					from += (from.isEmpty() ? "" : " ") + "inner join l.features f" + id + " left outer join f" + id + ".department fd" + id;
					where += (where.isEmpty() ? "" : " and ") + " (f" + id + ".label = :Xf" + id + " or f" + id + ".abbv = :Xf" + id + ") and (f" + id + ".class = GlobalRoomFeature or fd" + id +".deptCode = :Xfd)";
				}
				query.addParameter("feature", "Xf" + id, s);
				id++;
			}
			if (department != null)
				query.addParameter("feature", "Xfd", department);
			query.addFrom("feature", from);
			query.addWhere("feature", where);
		}
		
		Set<String> groups = (options == null ? null : options.get("group"));
		if (groups != null && !groups.isEmpty()) {
			String group = "";
			int id = 0;
			for (String s: groups) {
				group += (group.isEmpty() ? "" : ", ") + ":Xg" + id;
				query.addParameter("group", "Xg" + id, s);
				id++;
			}
			query.addFrom("group", "inner join l.roomGroups g left outer join g.department gd");
			if (department == null)
				query.addWhere("group", "(g.name in (" + group + ") or g.abbv in (" + group + ")) and g.global = true");
			else {
				query.addWhere("group", "(g.name in (" + group + ") or g.abbv in (" + group + ")) and (g.global = true or gd.deptCode = :Xgd)");
				query.addParameter("group", "Xgd", department);
			}
		}
		
		Set<String> size = (options == null ? null : options.get("size"));
		if (size != null && !size.isEmpty()) {
			String term = size.iterator().next();
			int min = 0, max = Integer.MAX_VALUE;
			Size prefix = Size.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
			try {
				int a = Integer.parseInt(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf('.'));
					String b = term.substring(term.indexOf("..") + 2);
					min = Integer.parseInt(a); max = Integer.parseInt(b);
				} catch (NumberFormatException e) {}
			}
			if (min > 0) {
				if (max < Integer.MAX_VALUE) {
					query.addWhere("size", "l.capacity >= :Xmin and l.capacity <= :Xmax");
					query.addParameter("size", "Xmin", min);
					query.addParameter("size", "Xmax", max);
				} else {
					query.addWhere("size", "l.capacity >= :Xmin");
					query.addParameter("size", "Xmin", min);
				}
			} else if (max < Integer.MAX_VALUE) {
				query.addWhere("size", "l.capacity <= :Xmax");
				query.addParameter("size", "Xmax", max);
			}
		}
		
		Set<String> flags = (options == null ? null : options.get("flag"));
		boolean nearby = (flags != null && (flags.contains("nearby") || flags.contains("Nearby")));
		
		Set<String> buildings = (options == null ? null : options.get("building"));
		if (buildings != null && !buildings.isEmpty() && !nearby) {
			String building = "";
			int id = 0;
			for (String s: buildings) {
				building += (building.isEmpty() ? "" : ", ") + ":Xb" + id;
				query.addParameter("building", "Xb" + id, s);
				id++;
			}
			query.addWhere("building", "l.building.abbreviation in (" + building + ")");
		}
		
		boolean eventRooms = (flags != null && (flags.contains("event") || flags.contains("Event")));
		boolean allRooms = (flags != null && (flags.contains("all") || flags.contains("All")));
		
		if (department != null) {
			Set<String> users = (options == null ? null : options.get("user"));
			String user = (users == null || users.isEmpty() ? null : users.iterator().next());
			if ("Managed".equalsIgnoreCase(department) && user != null) {
				if (context.hasPermission(Right.DepartmentIndependent)) {
					if (eventRooms)
						query.addWhere("department", "l.eventDepartment is not null");
					else if (!allRooms)
						query.addFrom("department", "inner join l.roomDepts rd");
				} else {
					if (eventRooms) {
						query.addFrom("department", "inner join l.eventDepartment.timetableManagers m");
						query.addWhere("department", "m.externalUniqueId = :Xu");
					} else if (allRooms) {
						query.addFrom("department", "left outer join l.eventDepartment.timetableManagers m1 left outer join l.roomDepts rd left outer join rd.department.timetableManagers m2");
						query.addWhere("department", "m1.externalUniqueId = :Xu or m2.externalUniqueId = :Xu");
					} else {
						query.addFrom("department", "inner join l.roomDepts rd inner join rd.department.timetableManagers m");
						query.addWhere("department", "m.externalUniqueId = :Xu");
					}
					query.addParameter("department", "Xu", user);
				}
			} else {
				if (eventRooms) {
					query.addFrom("department", "left outer join l.examTypes x");
					query.addWhere("department", "l.eventDepartment.deptCode = :Xd or x.reference = :Xd");
				} else if (allRooms) {
					query.addFrom("department", "left outer join l.examTypes x left outer join l.roomDepts rd left outer join rd.department rdd left outer join l.eventDepartment ed");
					query.addWhere("department", "rdd.deptCode = :Xd or ed.deptCode = :Xd or x.reference = :Xd");
				} else {
					query.addFrom("department", "left outer join l.examTypes x left outer join l.roomDepts rd");
					query.addWhere("department", "rd.department.deptCode = :Xd or x.reference = :Xd");
				}
				query.addParameter("department", "Xd", department);
			}
		}
		
		if (eventRooms) {
			if (checkEventStatus()) {
				query.addFrom("flag", "RoomTypeOption o");
				query.addWhere("flag", "l.eventDepartment.allowEvents = true and ((l.eventStatus is null and o.status != 0 and o.roomType = l.roomType and o.department = l.eventDepartment) or l.eventStatus != 0)");
			} else
				query.addWhere("flag", "l.eventDepartment is not null");
		}
		
		Set<String> ids = (options == null ? null : options.get("id"));
		if (ids != null && !ids.isEmpty()) {
			String list = "";
			int id = 0;
			for (String s: ids) {
				list += (list.isEmpty() ? "" : ", ") + ":Xi" + id;
				query.addParameter("id", "Xi" + id, s);
				id++;
			}
			query.addWhere("id", "l.uniqueId in (" + list + ")");
		}
		
		return query;
	}
	
	public static class RoomQuery {
		private Long iSessionId;
		private Map<String, String> iFrom = new HashMap<String, String>();
		private Map<String, String> iWhere = new HashMap<String, String>();
		private Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public RoomQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public void addFrom(String option, String from) { iFrom.put(option, from); }
		public void addWhere(String option, String where) { iWhere.put(option, where); }

		protected void addParameter(String option, String name, Object value) {
			Map<String, Object> params = iParams.get(option);
			if (params == null) { params = new HashMap<String, Object>(); iParams.put(option, params); }
			params.put(name, value);
		}
		
		public String getFrom(Collection<String> excludeOption) {
			String from = "";
			for (Map.Entry<String, String> entry: iFrom.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				from += (entry.getValue().startsWith("inner join") || entry.getValue().startsWith("left outer join") || entry.getValue().startsWith("left join fetch") ? " " : ", ") + entry.getValue();
			}
			return from;
		}
		
		public String getWhere(Collection<String> excludeOption) {
			String where = "";
			for (Map.Entry<String, String> entry: iWhere.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				where += " and (" + entry.getValue() + ")";
			}
			return where;
		}
		
		public org.hibernate.Query setParams(org.hibernate.Query query, Collection<String> excludeOption) {
			for (Map.Entry<String, Map<String, Object>> entry: iParams.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				for (Map.Entry<String, Object> param: entry.getValue().entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setInteger(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setLong(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setString(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setBoolean(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setDate(param.getKey(), (Date)param.getValue());
					} else {
						query.setString(param.getKey(), param.getValue().toString());
					}
				}
			}
			return query;
		}
		
		public RoomInstance select(String select) {
			return new RoomInstance(select);
		}
		
		public RoomInstance select() {
			return select(null);
		}
		
		public class RoomInstance {
			private String iSelect = null, iFrom = null, iWhere = null, iOrderBy = null, iGroupBy = null, iType = "Location";
			private Integer iLimit = null;
			private Set<String> iExclude = new HashSet<String>();
			private Map<String, Object> iParams = new HashMap<String, Object>();
			
			private RoomInstance(String select) {
				iSelect = select;
			}
			
			public RoomInstance from(String from) { iFrom = from; return this; }
			public RoomInstance where(String where) { 
				if (iWhere == null)
					iWhere = "(" + where + ")";
				else
					iWhere += " and (" + where + ")";
				return this;
			}
			public RoomInstance type(String type) { iType = type; return this; }
			public RoomInstance order(String orderBy) { iOrderBy = orderBy; return this; }
			public RoomInstance group(String groupBy) { iGroupBy = groupBy; return this; }
			public RoomInstance exclude(String excludeOption) { 
				if (excludeOption != null && !excludeOption.isEmpty()) iExclude.add(excludeOption);
				return this;
			}
			public RoomInstance set(String param, Object value) { iParams.put(param, value); return this; }
			public RoomInstance limit(Integer limit) { iLimit = (limit == null || limit <= 0 ? null : limit); return this; }
			
			public String query() {
				return
					"select " + (iSelect == null ? "distinct l" : iSelect) +
					" from " + iType + " l " + 
					(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					" where l.session.uniqueId = :sessionId " +
					getWhere(iExclude) + (iWhere == null ? "" : " and (" + iWhere + ")") +
					(iGroupBy == null ? "" : " group by " + iGroupBy) +
					(iOrderBy == null ? "" : " order by " + iOrderBy);
			}
			
			public org.hibernate.Query query(org.hibernate.Session hibSession) {
				// System.out.println("Q: " + query());
				org.hibernate.Query query = setParams(hibSession.createQuery(query()), iExclude).setLong("sessionId", iSessionId).setCacheable(true);
				for (Map.Entry<String, Object> param: iParams.entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setInteger(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setLong(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setString(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setBoolean(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setDate(param.getKey(), (Date)param.getValue());
					} else {
						query.setString(param.getKey(), param.getValue().toString());
					}
				}
				if (iLimit != null)
					query.setMaxResults(iLimit);
				return query;
			}
		}
	}
}
