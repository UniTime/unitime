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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class RoomInterface implements IsSerializable {

	public static class RoomSharingDisplayMode implements IsSerializable {
		private String iName;
		private int iFirstDay = 0, iLastDay = 4, iFirstSlot = 90, iLastSlot = 210, iStep = 6;
		
		public RoomSharingDisplayMode() {}
		
		public RoomSharingDisplayMode(String m) {
			String[] model = m.split("\\|");
			iName = model[0];
			iFirstDay = Integer.parseInt(model[1]);
			iLastDay = Integer.parseInt(model[2]);
			iFirstSlot = Integer.parseInt(model[3]);
			iLastSlot = Integer.parseInt(model[4]);
			iStep = Integer.parseInt(model[5]);
		}
		
		public int getFirstDay() { return iFirstDay; }
		public void setFirstDay(int firstDay) { iFirstDay = firstDay; }
		
		public int getLastDay() { return iLastDay; }
		public void setLastDay(int lastDay) { iLastDay = lastDay; }
		
		public int getFirstSlot() { return iFirstSlot; }
		public void setFirstSlot(int firstSlot) { iFirstSlot = firstSlot; }

		public int getLastSlot() { return iLastSlot; }
		public void setLastSlot(int lastSlot) { iLastSlot = lastSlot; }
		
		public int getStep() { return iStep; }
		public void setStep(int step) { iStep = step; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }

		public String toString() { 
			return getFirstDay() + "|" + getLastDay() + "|" + getFirstSlot() + "|" + getLastSlot() + "|" + getStep();
		}
		
		public String toHex() {
			return Integer.toHexString(getStep() * 4064256 + getFirstDay() * 580608 + getLastDay() * 82944 + getFirstSlot() * 288 + (getLastSlot() % 288));
		}
		
		public static RoomSharingDisplayMode fromHex(String hexMode) {
			int mode = Integer.parseInt(hexMode, 16);
			RoomSharingDisplayMode m = new RoomSharingDisplayMode();
			m.setStep(mode / 4064256);
			m.setFirstDay((mode % 4064256) / 580608);
			m.setLastDay((mode % 580608) / 82944);
			m.setFirstSlot((mode % 82944) / 288);
			m.setLastSlot(mode % 288 == 0 ? 288 : mode % 288);
			return m;
		}
	}
	
	public static class RoomSharingOption implements IsSerializable {
		private String iCode, iName;
		private String iColor;
		private Long iId;
		private boolean iEditable;
		private Long iPreferenceId = null;
		
		public RoomSharingOption() {}
		public RoomSharingOption(Long id, String color, String code, String name, boolean editable, Long preferenceId) {
			iId = id; iColor = color; iCode = code; iName = name; iEditable = editable; iPreferenceId = preferenceId;
		}
		public RoomSharingOption(Long id, String color, String code, String name, boolean editable) {
			this(id, color, code, name, editable, null);
		}
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditable() { return iEditable; }
		
		public boolean hasPreference() { return iPreferenceId != null;}
		public void setPreference(Long preferenceId) { iPreferenceId = preferenceId; }
		public Long getPreference() { return iPreferenceId; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof RoomSharingOption)) return false;
			return getId().equals(((RoomSharingOption)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	}
	
	public static class RoomSharingModel implements IsSerializable, GwtRpcResponse {
		private Long iId;
		private String iName;
		private Long iDefaultOption;
		private boolean iDefaultHorizontal;
		private List<RoomSharingDisplayMode> iModes;
		private List<RoomSharingOption> iOptions;
		private List<RoomSharingOption> iOtherOptions;
		private Map<Integer, Map<Integer, Long>> iModel;
		private Map<Integer, Map<Integer, Boolean>> iEditable;
		private int iDefaultMode = 0;
		private boolean iDefaultEditable = true;
		private String iNote = null;
		private boolean iNoteEditable = false;
		private List<PreferenceInterface> iPreferences = null;
		private Long iDefaultPreference = null;
		
		public RoomSharingModel() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id;}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public void addMode(RoomSharingDisplayMode mode) {
			if (iModes == null) iModes = new ArrayList<RoomSharingDisplayMode>();
			iModes.add(mode);
		}
		
		public List<RoomSharingDisplayMode> getModes() {
			return iModes;
		}
		
		public void setDefaultOption(RoomSharingOption option) {
			iDefaultOption = (option == null ? null : option.getId());
		}

		public RoomSharingOption getDefaultOption() {
			return getOption(iDefaultOption);
		}
		
		public RoomSharingOption getOption(Long id) {
			if (id == null) id = iDefaultOption;
			if (iOptions == null || id == null) return null;
			for (RoomSharingOption option: iOptions)
				if (option.getId().equals(id)) return option;
			return (!id.equals(iDefaultOption) ? getOption(iDefaultOption) : null);
		}
		
		public void addOption(RoomSharingOption option) {
			if (iOptions == null) iOptions = new ArrayList<RoomSharingOption>();
			iOptions.add(option);
		}
		
		public void addOther(RoomSharingOption option) {
			if (iOtherOptions == null) iOtherOptions = new ArrayList<RoomSharingOption>();
			iOtherOptions.add(option);
		}
		
		public List<RoomSharingOption> getAdditionalOptions() {
			List<RoomSharingOption> other = new ArrayList<RoomSharingOption>();
			if (iOtherOptions == null || iOtherOptions.isEmpty()) return other;
			for (RoomSharingOption option: iOtherOptions)
				if (!iOptions.contains(option)) other.add(option);
			return other;
		}

		public List<RoomSharingOption> getOptions() { return iOptions; }
		
		public List<RoomSharingOption> getRemovableOptions() {
			List<RoomSharingOption> options = new ArrayList<RoomSharingOption>();
			if (iOptions == null) return options;
			for (RoomSharingOption option: iOptions)
				if (option.isEditable() && option.getId() >= 0) options.add(option);
			return options;
		}
		
		public boolean isEditable() {
			if (iOptions == null) return false;
			for (RoomSharingOption option: iOptions)
				if (option.isEditable()) {
					if (iDefaultEditable) return true;
					if (iEditable == null || iEditable.isEmpty()) return false;
					for (Map<Integer, Boolean> slot2ed: iEditable.values())
						for (Boolean ed: slot2ed.values())
							if (ed) return true;
				}
			return false;
		}

		public RoomSharingOption getOption(int day, int slot) {
			if (iModel == null) return getOption(iDefaultOption);
			Map<Integer, Long> slot2id = iModel.get(day);
			return getOption(slot2id == null ? null : slot2id.get(slot)); 
		}
		
		public void setOption(int day, int slot, Long optionId) {
			if (iModel == null) iModel = new HashMap<Integer, Map<Integer,Long>>();
			Map<Integer, Long> slot2id = iModel.get(day);
			if (slot2id == null) {
				slot2id = new HashMap<Integer, Long>();
				iModel.put(day, slot2id);
			}
			if (optionId == null)
				slot2id.remove(slot);
			else
				slot2id.put(slot, optionId);
		}

		public void setOption(int day, int slot, int step, RoomSharingOption option) {
			for (int i = 0; i < step; i++)
				setOption(day, slot + i, option == null ? null : option.getId());
		}
		
		public boolean isEditable(int day, int slot) {
			if (iEditable == null) return iDefaultEditable;
			Map<Integer, Boolean> slot2ed = iEditable.get(day);
			if (slot2ed == null) return iDefaultEditable;
			Boolean ed = slot2ed.get(slot);
			return (ed == null ? iDefaultEditable : ed);
		}
		
		public boolean isEditable(int day, int slot, int step) {
			for (int i = 0; i < step; i++)
				if (!isEditable(day, slot + i)) return false;
			return true;
		}

		public void setEditable(int day, int slot, boolean editable) {
			if (iEditable == null) iEditable = new HashMap<Integer, Map<Integer,Boolean>>();
			Map<Integer, Boolean> slot2ed = iEditable.get(day);
			if (slot2ed == null) {
				slot2ed = new HashMap<Integer, Boolean>();
				iEditable.put(day, slot2ed);
			}
			slot2ed.put(slot, editable);
		}
		
		public void setDefaultEditable(boolean editable) { iDefaultEditable = editable; }
		
		public boolean isDefaultHorizontal() { return iDefaultHorizontal; }
		public void setDefaultHorizontal(boolean horizontal) { iDefaultHorizontal = horizontal; }
		
		public int getDefaultMode() { return iDefaultMode; }
		public void setDefaultMode(int mode) { iDefaultMode = mode; }
		
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		
		public boolean isNoteEditable() { return iNoteEditable; }
		public void setNoteEditable(boolean noteEditable) { iNoteEditable = noteEditable; }
		
		public void setDefaultPreference(PreferenceInterface preference) {
			iDefaultPreference = (preference == null ? null : preference.getId());
		}

		public PreferenceInterface getDefaultPreference() {
			return getPreference(iDefaultPreference);
		}
		
		public PreferenceInterface getPreference(Long id) {
			if (id == null) id = iDefaultPreference;
			if (iPreferences == null || id == null) return null;
			for (PreferenceInterface preference: iPreferences)
				if (preference.getId().equals(id)) return preference;
			return (!id.equals(iDefaultPreference) ? getPreference(iDefaultPreference) : null);
		}
		
		public void addPreference(PreferenceInterface preference) {
			if (iPreferences == null) iPreferences = new ArrayList<PreferenceInterface>();
			iPreferences.add(preference);
		}
		
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
	}
	
	public static class RoomSharingRequest implements GwtRpcRequest<RoomSharingModel> {
		public static enum Operation implements IsSerializable {
			LOAD,
			SAVE
		}
		private Operation iOperation;
		private Long iLocationId;
		private RoomSharingModel iModel;
		private boolean iEventAvailability = false;
		private boolean iIncludeRoomPreferences = false;
		
		public RoomSharingRequest() {}
		
		public Long getLocationId() { return iLocationId; }
		public void setLocationId(Long locationId) { iLocationId = locationId; }
		
		public RoomSharingModel getModel() { return iModel; }
		public void setModel(RoomSharingModel model) { iModel = model; }

		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public String toString() { return getOperation().name() + "[" + getLocationId() + "]"; }
		
		public boolean isEventAvailability() { return iEventAvailability; }
		public void setEventAvailability(boolean availability) { iEventAvailability = availability; }
		
		public boolean isIncludeRoomPreferences() { return iIncludeRoomPreferences; }
		public void setIncludeRoomPreferences(boolean includeRoomPreferences) { iIncludeRoomPreferences = includeRoomPreferences; }
		
		public static RoomSharingRequest load(Long locationId, boolean eventAvailability, boolean includeRoomPreferences) {
			RoomSharingRequest request = new RoomSharingRequest();
			request.setOperation(Operation.LOAD);
			request.setLocationId(locationId);
			request.setEventAvailability(eventAvailability);
			request.setIncludeRoomPreferences(includeRoomPreferences);
			return request;
		}
		
		public static RoomSharingRequest load(Long locationId, boolean eventAvailability) {
			return load(locationId, eventAvailability, false);
		}
		
		public static RoomSharingRequest save(Long locationId, RoomSharingModel model, boolean eventAvailability, boolean includeRoomPreferences) {
			RoomSharingRequest request = new RoomSharingRequest();
			request.setOperation(Operation.SAVE);
			request.setLocationId(locationId);
			request.setModel(model);
			request.setEventAvailability(eventAvailability);
			request.setIncludeRoomPreferences(includeRoomPreferences);
			return request;
		}
		
		public static RoomSharingRequest save(Long locationId, RoomSharingModel model, boolean eventAvailability) {
			return save(locationId, model, eventAvailability, false);
		}
	}
	
	public static class RoomHintResponse implements GwtRpcResponse {
		private Long iId = null;
		private String iLabel = null;
		private String iDisplayName = null;
		private String iRoomTypeLabel = null;
		private String iMiniMapUrl = null;
		private Integer iCapacity = null, iExamCapacity = null, iBreakTime = null;
		private String iExamType = null;
		private String iArea = null;
		private String iGroups = null;
		private String iEventStatus = null;
		private String iEventDepartment = null;
		private String iNote = null;
		private boolean iIgnoreRoomCheck = false;
		
		private Map<String, String> iFeatures = null;
		private List<RoomPictureInterface> iPictures = null;
		
		public RoomHintResponse() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String getDisplayName() { return iDisplayName; }
		public void setDisplayName(String displayName) { iDisplayName = displayName; }
		public boolean hasDisplayName() { return iDisplayName != null && !iDisplayName.isEmpty(); }
		
		public String getRoomTypeLabel() { return iRoomTypeLabel; }
		public void setRoomTypeLabel(String roomTypeLabel) { iRoomTypeLabel = roomTypeLabel; }
		public boolean hasRoomTypeLabel() { return iRoomTypeLabel != null && !iRoomTypeLabel.isEmpty(); }
		
		public String getMiniMapUrl() { return iMiniMapUrl; }
		public void setMiniMapUrl(String miniMapUrl) { iMiniMapUrl = miniMapUrl; }
		public boolean hasMiniMapUrl() { return iMiniMapUrl != null && !iMiniMapUrl.isEmpty(); }

		public Integer getCapacity() { return iCapacity; }
		public void setCapacity(Integer capacity) { iCapacity = capacity; }
		public boolean hasCapacity() { return iCapacity != null && iCapacity != 0; }

		public Integer getExamCapacity() { return iExamCapacity; }
		public void setExamCapacity(Integer examCapacity) { iExamCapacity = examCapacity; }
		public boolean hasExamCapacity() { return iExamCapacity != null && iExamCapacity != 0; }
		
		public String getExamType() { return iExamType; }
		public void setExamType(String examType) { iExamType = examType; }
		public boolean hasExamType() { return iExamType != null && !iExamType.isEmpty(); }

		public String getArea() { return iArea; }
		public void setArea(String area) { iArea = area; }
		public boolean hasArea() { return iArea != null && !iArea.isEmpty(); }
		
		public boolean hasFeatures() { return iFeatures != null && !iFeatures.isEmpty(); }
		public void addFeature(String type, String name) {
			if (iFeatures == null) iFeatures = new HashMap<String, String>();
			String featuresThisType = iFeatures.get(type);
    		if (featuresThisType == null) {
    			featuresThisType = "";
    		} else {
    			featuresThisType += ", ";
    		}
    		featuresThisType += name;
    		iFeatures.put(type, featuresThisType);
		}
		public Set<String> getFeatureNames() { return new TreeSet<String>(iFeatures.keySet()); }
		public String getFeatures(String name) { return iFeatures.get(name); }
		
		public String getGroups() { return iGroups; }
		public void setGroups(String groups) { iGroups = groups; }
		public boolean hasGroups() { return iGroups != null && !iGroups.isEmpty(); }

		public String getEventStatus() { return iEventStatus; }
		public void setEventStatus(String eventStatus) { iEventStatus = eventStatus; }
		public boolean hasEventStatus() { return iEventStatus != null && !iEventStatus.isEmpty(); }

		public String getEventDepartment() { return iEventDepartment; }
		public void setEventDepartment(String eventDepartment) { iEventDepartment = eventDepartment; }
		public boolean hasEventDepartment() { return iEventDepartment != null && !iEventDepartment.isEmpty(); }

		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }

		public Integer getBreakTime() { return iBreakTime; }
		public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }
		public boolean hasBreakTime() { return iBreakTime != null && iBreakTime != 0; }
		
		public boolean isIgnoreRoomCheck() { return iIgnoreRoomCheck; }
		public void setIgnoreRoomCheck(boolean ignoreRoomCheck) { iIgnoreRoomCheck = ignoreRoomCheck; }
		
		public boolean hasPictures() { return iPictures != null && !iPictures.isEmpty(); }
		public void addPicture(RoomPictureInterface picture) {
			if (iPictures == null)
				iPictures = new ArrayList<RoomPictureInterface>();
			iPictures.add(picture);
		}
		public List<RoomPictureInterface> getPictures() { return iPictures; }
	}
	
	public static class RoomHintRequest implements GwtRpcRequest<RoomHintResponse> {
		private Long iLocationId;
		
		public Long getLocationId() { return iLocationId; }
		public void setLocationId(Long locationId) { iLocationId = locationId; }
		
		public String toString() { return "" + getLocationId(); }
		
		public static RoomHintRequest load(Long locationId) {
			RoomHintRequest request = new RoomHintRequest();
			request.setLocationId(locationId);
			return request;
		}
	}

	public static class RoomPictureInterface implements IsSerializable {
		private Long iUniqueId;
		private String iName;
		private String iType;
		
		public RoomPictureInterface() {}
		
		public RoomPictureInterface(Long uniqueId, String name, String type) {
			setUniqueId(uniqueId);
			setName(name);
			setType(type);
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
	}
	
	public static class RoomPictureRequest implements GwtRpcRequest<RoomPictureResponse> {
		public static enum Operation implements IsSerializable {
			LOAD,
			SAVE,
			UPLOAD,
		}
		public static enum Apply implements IsSerializable {
			THIS_SESSION_ONLY,
			ALL_FUTURE_SESSIONS,
			ALL_SESSIONS,
		}
		private Operation iOperation;
		private Apply iApply;
		private Long iLocationId;
		private List<RoomPictureInterface> iPictures;
		
		public RoomPictureRequest() {}
		
		public Long getLocationId() { return iLocationId; }
		public void setLocationId(Long locationId) { iLocationId = locationId; }
		
		public List<RoomPictureInterface> getPictures() { return iPictures; }
		public void setPictures(List<RoomPictureInterface> pictures) { iPictures = pictures; }

		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public Apply getApply() { return iApply; }
		public void setApply(Apply apply) { iApply = apply; }
		
		public String toString() { return getOperation().name() + "[" + getLocationId() + "]"; }
		
		public static RoomPictureRequest load(Long locationId) {
			RoomPictureRequest request = new RoomPictureRequest();
			request.setOperation(Operation.LOAD);
			request.setLocationId(locationId);
			return request;
		}
		
		public static RoomPictureRequest save(Long locationId, Apply apply, List<RoomPictureInterface> pictures) {
			RoomPictureRequest request = new RoomPictureRequest();
			request.setOperation(Operation.SAVE);
			request.setLocationId(locationId);
			request.setPictures(pictures);
			request.setApply(apply);
			return request;
		}
		
		public static RoomPictureRequest upload(Long locationId) {
			RoomPictureRequest request = new RoomPictureRequest();
			request.setOperation(Operation.UPLOAD);
			request.setLocationId(locationId);
			return request;
		}
	}
	
	public static class RoomPictureResponse implements GwtRpcResponse {
		private String iName;
		private List<RoomPictureInterface> iPictures;
		private RoomPictureRequest.Apply iApply;
		
		public RoomPictureResponse() {}
		
		public boolean hasPictures() { return iPictures != null && !iPictures.isEmpty(); }
		public void addPicture(RoomPictureInterface picture) {
			if (iPictures == null)
				iPictures = new ArrayList<RoomPictureInterface>();
			iPictures.add(picture);
		}
		public List<RoomPictureInterface> getPictures() { return iPictures; }
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public RoomPictureRequest.Apply getApply() { return iApply; }
		public void setApply(RoomPictureRequest.Apply apply) { iApply = apply; }
	}
	
	public static class RoomTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		private boolean iRoom = false;
		
		public RoomTypeInterface() {}
		
		public RoomTypeInterface(Long id, String label, boolean room) {
			iId = id; iLabel = label; iRoom = room;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public boolean isRoom() { return iRoom; }
		public void setRoom(boolean room) { iRoom = room; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof RoomTypeInterface)) return false;
			return getId().equals(((RoomTypeInterface)object).getId());
		}
	}
	
	public static class FeatureTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iAbbv;
		private String iLabel;
		private boolean iEvents = false;
		
		public FeatureTypeInterface() {}
		
		public FeatureTypeInterface(Long id, String abbv, String label, boolean events) {
			iId = id; iAbbv = abbv; iLabel = label; iEvents = events;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }

		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public boolean isEvents() { return iEvents; }
		public void setEvents(boolean events) { iEvents = events; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof FeatureTypeInterface)) return false;
			return getId().equals(((FeatureTypeInterface)object).getId());
		}
	}
	
	public static class RoomPropertyInterface implements GwtRpcResponse {
		private Long iId;
		private String iAbbv;
		private String iLabel;
		private String iColor;
		private String iTitle;
		
		public RoomPropertyInterface() {}
		
		public RoomPropertyInterface(Long id, String label) {
			iId = id; iLabel = label;
		}
		
		public RoomPropertyInterface(Long id, String abbv, String label) {
			iId = id; iAbbv = abbv; iLabel = label;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof RoomPropertyInterface)) return false;
			return getId().equals(((RoomPropertyInterface)object).getId());
		}
	}
	
	public static class BuildingInterface implements GwtRpcResponse {
		private Long iId;
		private String iAbbreviation;
		private String iName;
		private Double iX, iY;
		
		public BuildingInterface() {}
		
		public BuildingInterface(Long id, String abbreviation, String name) {
			iId = id; iAbbreviation = abbreviation; iName = name;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbreviation; }
		public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public boolean hasCoordinates() { return iX != null && iY != null; }
		public Double getX() { return iX; }
		public void setX(Double x) { iX = x; }
		
		public Double getY() { return iY; }
		public void setY(Double y) { iY = y; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof BuildingInterface)) return false;
			return getId().equals(((BuildingInterface)object).getId());
		}
	}
	
	public static class ExamTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iReference;
		private String iLabel;
		private boolean iFinal = false;
		
		public ExamTypeInterface() {}
		
		public ExamTypeInterface(Long id, String reference, String label, boolean finalExams) {
			iId = id; iReference = reference; iLabel = label; iFinal = finalExams;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }

		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public boolean isFinal() { return iFinal; }
		public void setFinal(boolean finalExams) { iFinal = finalExams; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof ExamTypeInterface)) return false;
			return getId().equals(((ExamTypeInterface)object).getId());
		}
	}
	
	public static class DepartmentInterface extends RoomPropertyInterface {
		private String iCode;
		private boolean iExternal = false, iEvent = false;
		private String iExternalAbbv, iExternalLabel;
		private PreferenceInterface iPreference;
		
		public DepartmentInterface() {
			super();
		}
		
		public String getDeptCode() { return iCode; }
		public void setDeptCode(String code) { iCode = code; }
		public String getAbbreviationOrCode() { return getAbbreviation() == null || getAbbreviation().isEmpty() ? getDeptCode() : getAbbreviation(); }
		public String getExtAbbreviationOrCode() { return getExtAbbreviation() == null || getExtAbbreviation().isEmpty() ? getDeptCode() : getExtAbbreviation(); }
		
		public boolean isExternal() { return iExternal; }
		public void setExternal(boolean external) { iExternal = external; }
		
		public boolean isEvent() { return iEvent; }
		public void setEvent(boolean event) { iEvent = event; }

		public String getExtAbbreviation() { return iExternalAbbv; }
		public void setExtAbbreviation(String abbv) { iExternalAbbv = abbv; }
		public String getExtAbbreviationWhenExist() { return getExtAbbreviation() == null || getExtAbbreviation().isEmpty() ? getAbbreviationOrCode() : getExtAbbreviation(); }

		public String getExtLabel() { return iExternalLabel; }
		public void setExtLabel(String label) { iExternalLabel = label; }
		public String getExtLabelWhenExist() { return getExtLabel() == null || getExtLabel().isEmpty() ? getLabel() : getExtLabel(); }
	
		public PreferenceInterface getPreference() { return iPreference; }
		public void setPreference(PreferenceInterface preference) { iPreference = preference; }
	}
	
	public static class FeatureInterface extends RoomPropertyInterface {
		private DepartmentInterface iDepartment = null;
		private FeatureTypeInterface iType;
		
		public FeatureInterface() {
			super();
		}
		
		public FeatureInterface(Long id, String abbv, String label) {
			super(id, abbv, label);
		}
		
		public boolean isDepartmental() { return iDepartment != null; }
		public DepartmentInterface getDepartment() { return iDepartment; }
		public void setDepartment(DepartmentInterface department) { iDepartment = department; }
		
		public boolean hasType() { return iType != null; }
		public FeatureTypeInterface getType() { return iType; }
		public void setType(FeatureTypeInterface type) { iType = type; }
	}
	
	public static class GroupInterface extends RoomPropertyInterface {
		private DepartmentInterface iDepartment = null;

		public GroupInterface() {
			super();
		}
		
		public GroupInterface(Long id, String abbv, String label) {
			super(id, abbv, label);
		}
		
		public boolean isDepartmental() { return iDepartment != null; }
		public DepartmentInterface getDepartment() { return iDepartment; }
		public void setDepartment(DepartmentInterface department) { iDepartment = department; }
	}
	
	public static class RoomDetailInterface extends FilterRpcResponse.Entity {
		private static final long serialVersionUID = 1L;
		
		private String iExternalId;
		private BuildingInterface iBuilding;
		private RoomTypeInterface iRoomType;
		private Integer iCapacity, iExamCapacity;
		private Double iX, iY, iArea;
		private DepartmentInterface iControlDepartment, iEventDepartment;
		private List<DepartmentInterface> iDepartments = new ArrayList<DepartmentInterface>();
		private List<GroupInterface> iGroups = new ArrayList<GroupInterface>();
		private List<FeatureInterface> iFeatures = new ArrayList<FeatureInterface>();
		private List<ExamTypeInterface> iExamTypes = new ArrayList<ExamTypeInterface>();
		private boolean iIgnoreTooFar = false, iIgnoreRoomCheck = false;
		private String iPeriodPreference = null, iAvailability = null, iEventAvailability = null;
		private String iRoomSharingNote = null, iEventNote = null, iDefaultEventNote = null;
		private Integer iEventStatus = null, iBreakTime = null, iDefaultEventStatus = null, iDefaultBreakTime = null;
		private String iPrefix = null;
		private boolean iCanShowDetail = false, iCanSeeAvailability = false, iCanSeePeriodPreferences = false, iCanSeeEventAvailability = false;
		private boolean iCanChange = false, iCanChangeAvailability = false, iCanChangeControll = false, iCanChangeExternalId = false, iCanChangeType = false, iCanChangeCapacity = false, iCanChangeExamStatus = false,
				iCanChangeRoomProperties = false, iCanChangeEventProperties = false, iCanChangePicture = false, iCanChangePreferences = false,
				iCanChangeGroups = false, iCanChangeFeatures = false, iCanChangeEventAvailability = false;
		private boolean iCanDelete = false;
		private String iMiniMapUrl = null, iMapUrl = null;
		private List<RoomPictureInterface> iPictures = new ArrayList<RoomPictureInterface>();
		private String iLastChange = null;
		private RoomSharingModel iRoomSharingModel = null, iEventAvailabilityModel = null;
		private Map<Long, PeriodPreferenceModel> iPeriodPreferenceModels = null;
		
		public RoomDetailInterface() {}
		
		public RoomDetailInterface(Long uniqueId, String displayName, String label, String... properties) {
			super(uniqueId, displayName, label, properties);
		}
		
		public RoomDetailInterface(FilterRpcResponse.Entity entity) {
			setUniqueId(entity.getUniqueId());
			setAbbreviation(entity.getAbbreviation());
			setName(entity.getName());
			String roomType = entity.getProperty("type", null);
			if (roomType != null)
				setRoomType(new RoomTypeInterface(-1l, roomType, true));
			String capacity = entity.getProperty("capacity", null);
			if (capacity != null)
				setCapacity(Integer.valueOf(capacity));
		}
		
		public String getExternalId() { return iExternalId; }
		public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		
		public String getLabel() {
			if (iBuilding != null)
				return iBuilding.getAbbreviation() + " " + getName();
			else
				return getName();
		}
		
		public String getDisplayName() { return getAbbreviation(); }
		public boolean hasDisplayName() { return getAbbreviation() != null && !getAbbreviation().isEmpty(); }
		public void setDisplayName(String name) { setAbbreviation(name); }
		
		public RoomTypeInterface getRoomType() { return iRoomType; }
		public void setRoomType(RoomTypeInterface roomType) { iRoomType = roomType; }
		
		public Integer getCapacity() { return iCapacity; }
		public void setCapacity(Integer capacity) { iCapacity = capacity ; }
		
		public Integer getExamCapacity() { return iExamCapacity; }
		public void setExamCapacity(Integer capacity) { iExamCapacity = capacity ; }
		
		public List<GroupInterface> getGroups() { return iGroups; }
		public void addGroup(GroupInterface group) { iGroups.add(group); }
		public void removeGroup(GroupInterface group) { iGroups.remove(group); }
		public boolean hasGroups() { return iGroups != null && !iGroups.isEmpty(); }
		public List<GroupInterface> getGlobalGroups() {
			List<GroupInterface> groups = new ArrayList<GroupInterface>();
			if (iGroups != null)
				for (GroupInterface group: iGroups)
					if (group.getDepartment() == null) groups.add(group);
			return groups;
		}
		public List<GroupInterface> getDepartmentalGroups(Long departmentId) {
			List<GroupInterface> groups = new ArrayList<GroupInterface>();
			if (iGroups != null)
				for (GroupInterface group: iGroups)
					if (group.getDepartment() != null && (departmentId == null || departmentId.equals(group.getDepartment().getId()))) groups.add(group);
			return groups;
		}
		public boolean hasGroup(Long groupId) {
			if (iGroups == null) return false;
			for (GroupInterface group: iGroups)
				if (groupId.equals(group.getId())) return true;
			return false;
		}
		
		public boolean hasControlDepartment() { return iControlDepartment != null; }
		public DepartmentInterface getControlDepartment() { return iControlDepartment; }
		public void setControlDepartment(DepartmentInterface controlDepartment) { iControlDepartment = controlDepartment; }

		public boolean hasEventDepartment() { return iEventDepartment != null; }
		public DepartmentInterface getEventDepartment() { return iEventDepartment; }
		public void setEventDepartment(DepartmentInterface eventDepartment) { iEventDepartment = eventDepartment; }
		
		public List<FeatureInterface> getFeatures() { return iFeatures; }
		public void addFeature(FeatureInterface feature) { iFeatures.add(feature); }
		public void removeFeature(FeatureInterface feature) { iFeatures.remove(feature); }
		public boolean hasFeatures() { return iFeatures != null && !iFeatures.isEmpty(); }
		public List<FeatureInterface> getFeatures(FeatureTypeInterface type) {
			List<FeatureInterface> ret = new ArrayList<FeatureInterface>();
			for (FeatureInterface f: iFeatures) {
				if ((type == null && f.getType() == null) || (type != null && type.equals(f.getType())))
					ret.add(f);
			}
			return ret;
		}
		public List<FeatureInterface> getFeatures(Long typeId) {
			List<FeatureInterface> ret = new ArrayList<FeatureInterface>();
			for (FeatureInterface f: iFeatures) {
				if ((typeId == null && f.getType() == null) || (typeId != null && f.getType() != null && typeId.equals(f.getType().getId())))
					ret.add(f);
			}
			return ret;
		}
		public boolean hasFeature(Long featureId) {
			if (iFeatures == null) return false;
			for (FeatureInterface feature: iFeatures)
				if (featureId.equals(feature.getId())) return true;
			return false;
		}
		
		public List<DepartmentInterface> getDepartments() { return iDepartments; }
		public void addDepartment(DepartmentInterface department) { iDepartments.add(department); }
		public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
		
		public BuildingInterface getBuilding() { return iBuilding; }
		public void setBuilding(BuildingInterface building) { iBuilding = building; }
		
		public boolean hasCoordinates() { return iX != null && iY != null; }
		public Double getX() { return iX; }
		public void setX(Double x) { iX = x; }
		
		public Double getY() { return iY; }
		public void setY(Double y) { iY = y; }
		
		public Double getArea() { return iArea; }
		public void setArea(Double area) { iArea = area; }
		
		public boolean hasPreference(String deptCode) {
			for (DepartmentInterface d: getDepartments()) {
				if (!d.getDeptCode().equals(deptCode) && d.getPreference() != null) return true;
			}
			return false;
		}
		
		public boolean hasPreference() {
			for (DepartmentInterface d: getDepartments())
				if (d.getPreference() != null) return true;
			return false;
		}
		
		public boolean isIgnoreRoomCheck() { return iIgnoreRoomCheck; }
		public void setIgnoreRoomCheck(boolean ignoreRoomCheck) { iIgnoreRoomCheck = ignoreRoomCheck; }
		
		public boolean isIgnoreTooFar() { return iIgnoreTooFar; }
		public void setIgnoreTooFar(boolean ignoreTooFar) { iIgnoreTooFar = ignoreTooFar; }
		
		public boolean hasExamTypes() { return iExamTypes != null && !iExamTypes.isEmpty(); }
		public List<ExamTypeInterface> getExamTypes() { return iExamTypes; }
		public void addExamRype(ExamTypeInterface type) { iExamTypes.add(type); }
		
		public String getPeriodPreference() { return iPeriodPreference; }
		public void setPeriodPreference(String pref) { iPeriodPreference = pref; }
		
		public String getAvailability() { return iAvailability; }
		public void setAvailability(String availability) { iAvailability = availability; }
		
		public String getEventAvailability() { return iEventAvailability; }
		public void setEventAvailability(String availability) { iEventAvailability = availability; }
		
		public String getRoomSharingNote() { return iRoomSharingNote; }
		public void setRoomSharingNote(String note) { iRoomSharingNote = note; }
		
		public String getEventNote() { return iEventNote; }
		public void setEventNote(String note) { iEventNote = note; }
		public boolean hasEventNote() { return iEventNote != null && !iEventNote.isEmpty(); }
		
		public Integer getEventStatus() { return iEventStatus; }
		public void setEventStatus(Integer eventStatus) { iEventStatus = eventStatus; }
		
		public Integer getBreakTime() { return iBreakTime; }
		public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }
		
		public String getDefaultEventNote() { return iDefaultEventNote; }
		public void setDefaultEventNote(String note) { iDefaultEventNote = note; }
		public boolean hasDefaultEventNote() { return iDefaultEventNote != null && !iDefaultEventNote.isEmpty(); }
		
		public Integer getDefaultEventStatus() { return iDefaultEventStatus; }
		public void setDefaultEventStatus(Integer eventStatus) { iDefaultEventStatus = eventStatus; }
		
		public Integer getDefaultBreakTime() { return iDefaultBreakTime; }
		public void setDefaultBreakTime(Integer breakTime) { iDefaultBreakTime = breakTime; }
		
		public String getPrefix() { return iPrefix; }
		public void setPrefix(String prefix) { iPrefix = prefix; }
		
		public boolean isCanShowDetail() { return iCanShowDetail; }
		public void setCanShowDetail(boolean canShowDetail) { iCanShowDetail = canShowDetail; }

		public boolean isCanSeeAvailability() { return iCanSeeAvailability; }
		public void setCanSeeAvailability(boolean canSeeAvailability) { iCanSeeAvailability = canSeeAvailability; }

		public boolean isCanSeePeriodPreferences() { return iCanSeePeriodPreferences; }
		public void setCanSeePeriodPreferences(boolean canSeePeriodPreferences) { iCanSeePeriodPreferences = canSeePeriodPreferences; }

		public boolean isCanSeeEventAvailability() { return iCanSeeEventAvailability; }
		public void setCanSeeEventAvailability(boolean canSeeEventAvailability) { iCanSeeEventAvailability = canSeeEventAvailability; }

		public boolean isCanChange() { return iCanChange; }
		public void setCanChange(boolean canChange) { iCanChange = canChange; }

		public boolean isCanChangeAvailability() { return iCanChangeAvailability; }
		public void setCanChangeAvailability(boolean canChangeAvailability) { iCanChangeAvailability = canChangeAvailability; }

		public boolean isCanChangeControll() { return iCanChangeControll; }
		public void setCanChangeControll(boolean canChangeControll) { iCanChangeControll = canChangeControll; }

		public boolean isCanChangeExternalId() { return iCanChangeExternalId; }
		public void setCanChangeExternalId(boolean canChangeExternalId) { iCanChangeExternalId = canChangeExternalId; }

		public boolean isCanChangeType() { return iCanChangeType; }
		public void setCanChangeType(boolean canChangeType) { iCanChangeType = canChangeType; }

		public boolean isCanChangeCapacity() { return iCanChangeCapacity; }
		public void setCanChangeCapacity(boolean canChangeCapacity) { iCanChangeCapacity = canChangeCapacity; }

		public boolean isCanChangeExamStatus() { return iCanChangeExamStatus; }
		public void setCanChangeExamStatus(boolean canChangeExamStatus) { iCanChangeExamStatus = canChangeExamStatus; }

		public boolean isCanChangeRoomProperties() { return iCanChangeRoomProperties; }
		public void setCanChangeRoomProperties(boolean canChangeRoomProperties) { iCanChangeRoomProperties = canChangeRoomProperties; }

		public boolean isCanChangeEventProperties() { return iCanChangeEventProperties; }
		public void setCanChangeEventProperties(boolean canChangeEventProperties) { iCanChangeEventProperties = canChangeEventProperties; }

		public boolean isCanChangePicture() { return iCanChangePicture; }
		public void setCanChangePicture(boolean canChangePicture) { iCanChangePicture = canChangePicture; }

		public boolean isCanChangePreferences() { return iCanChangePreferences; }
		public void setCanChangePreferences(boolean canChangePreferences) { iCanChangePreferences = canChangePreferences; }

		public boolean isCanChangeGroups() { return iCanChangeGroups; }
		public void setCanChangeGroups(boolean canChangeGroups) { iCanChangeGroups = canChangeGroups; }

		public boolean isCanChangeFeatures() { return iCanChangeFeatures; }
		public void setCanChangeFeatures(boolean canChangeFeatures) { iCanChangeFeatures = canChangeFeatures; }

		public boolean isCanChangeEventAvailability() { return iCanChangeEventAvailability; }
		public void setCanChangeEventAvailability(boolean canChangeEventAvailability) { iCanChangeEventAvailability = canChangeEventAvailability; }

		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		
		public String getMiniMapUrl() { return iMiniMapUrl; }
		public void setMiniMapUrl(String miniMapUrl) { iMiniMapUrl = miniMapUrl; }
		public boolean hasMiniMapUrl() { return iMiniMapUrl != null && !iMiniMapUrl.isEmpty(); }
		
		public String getMapUrl() { return iMapUrl; }
		public void setMapUrl(String MapUrl) { iMapUrl = MapUrl; }
		public boolean hasMapUrl() { return iMapUrl != null && !iMapUrl.isEmpty(); }
		
		public boolean hasPictures() { return iPictures != null && !iPictures.isEmpty(); }
		public void addPicture(RoomPictureInterface picture) {
			iPictures.add(picture);
		}
		public List<RoomPictureInterface> getPictures() { return iPictures; }
		
		public String getLastChange() { return iLastChange; }
		public boolean hasLastChange() { return iLastChange != null && !iLastChange.isEmpty(); }
		public void setLastChange(String lastChange) { iLastChange = lastChange; }
		
		public boolean hasRoomSharingModel() { return iRoomSharingModel != null; }
		public void setRoomSharingModel(RoomSharingModel model) { iRoomSharingModel = model; }
		public RoomSharingModel getRoomSharingModel() { return iRoomSharingModel; }
		
		public boolean hasEventAvailabilityModel() { return iEventAvailabilityModel != null; }
		public void setEventAvailabilityModel(RoomSharingModel model) { iEventAvailabilityModel = model; }
		public RoomSharingModel getEventAvailabilityModel() { return iEventAvailabilityModel; }
		
		public boolean hasPeriodPreferenceModel(Long examTypeId) {
			return iPeriodPreferenceModels != null && iPeriodPreferenceModels.containsKey(examTypeId);
		}
		public PeriodPreferenceModel getPeriodPreferenceModel(Long examTypeId) {
			return (iPeriodPreferenceModels == null ? null : iPeriodPreferenceModels.get(examTypeId));
		}
		public void setPeriodPreferenceModel(PeriodPreferenceModel model) {
			if (model == null || model.getExamType() == null) return;
			if (iPeriodPreferenceModels == null) iPeriodPreferenceModels = new HashMap<Long, PeriodPreferenceModel>();
			iPeriodPreferenceModels.put(model.getExamType().getId(), model);
		}

	}
	
	public static class RoomDetailsRequest implements GwtRpcRequest<GwtRpcResponseList<RoomDetailInterface>> {
		private List<Long> iLocationIds = new ArrayList<Long>();
		private String iDepartment = null;
		
		public RoomDetailsRequest() {}
		
		public void setDepartment(String department) { iDepartment = department; }
		public String getDepartment() { return iDepartment; }
		
		public boolean hasLocationIds() { return iLocationIds != null && !iLocationIds.isEmpty(); }
		public List<Long> getLocationIds() { return iLocationIds; }
		public void addLocationId(Long locationId) { iLocationIds.add(locationId); }
		
		public int size() { return iLocationIds == null ? 0 : iLocationIds.size(); }
		
		public String toString() { return "" + getLocationIds(); }
		
		public static RoomDetailsRequest load(Long... locationId) {
			RoomDetailsRequest request = new RoomDetailsRequest();
			for (Long id: locationId)
				request.addLocationId(id);
			return request;
		}
	}
	
	public static class PreferenceInterface implements IsSerializable {
		private String iCode, iName;
		private String iColor;
		private Long iId;
		private boolean iEditable;
		
		public PreferenceInterface() {}
		public PreferenceInterface(Long id, String color, String code, String name, boolean editable) {
			iId = id; iColor = color; iCode = code; iName = name; iEditable = editable;
		}
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditable() { return iEditable; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof PreferenceInterface)) return false;
			return getId().equals(((PreferenceInterface)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	}
	
	public static class PeriodInterface implements IsSerializable {
		private Long iId;
		private int iDay, iStart, iLength;
		
		public PeriodInterface() {}
		
		public PeriodInterface(Long id, int day, int start, int length) {
			iId = id; iDay = day; iStart = start; iLength = length;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public int getDay() { return iDay; }
		public void setDay(int day) { iDay = day; }
		
		public int getStartSlot() { return iStart; }
		public void setStartSlot(int start) { iStart = start; }
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		
	}
	
	public static class PeriodPreferenceModel implements IsSerializable, GwtRpcResponse {
		private ExamTypeInterface iExamType;
		private Long iLocationId;
		private Long iDefaultPreference;
		private Date iFirstDate;
		private TreeSet<Integer> iStarts = new TreeSet<Integer>();
		private TreeSet<Integer> iDays = new TreeSet<Integer>();
		private List<PreferenceInterface> iPreferences = new ArrayList<PreferenceInterface>();
		private List<PeriodInterface> iPeriods = new ArrayList<PeriodInterface>();
		private Map<Integer, Map<Integer, Long>> iModel;
		
		public PeriodPreferenceModel() {}
		
		public ExamTypeInterface getExamType() { return iExamType; }
		public void setExamType(ExamTypeInterface examType) { iExamType = examType; }
		
		public void setLocationId(Long id) { iLocationId = id; }
		public Long getLocationId() { return iLocationId; }

		public Date getFirstDate() { return iFirstDate; }
		public void setFirstDate(Date date) { iFirstDate = date; }
		
		public TreeSet<Integer> getDays() { return iDays; }
		public TreeSet<Integer> getSlots() { return iStarts; }
		public int getLength(int slot) {
			for (PeriodInterface p: iPeriods)
				if (p.getStartSlot() == slot) return p.getLength();
			return 12;
		}
		
		public void setDefaultPreference(PreferenceInterface preference) {
			iDefaultPreference = (preference == null ? null : preference.getId());
		}

		public PreferenceInterface getDefaultPreference() {
			return getPreference(iDefaultPreference);
		}
		
		public PreferenceInterface getPreference(Long id) {
			if (id == null) id = iDefaultPreference;
			if (iPreferences == null || id == null) return null;
			for (PreferenceInterface preference: iPreferences)
				if (preference.getId().equals(id)) return preference;
			return (!id.equals(iDefaultPreference) ? getPreference(iDefaultPreference) : null);
		}
		
		public void addPreference(PreferenceInterface preference) {
			if (iPreferences == null) iPreferences = new ArrayList<PreferenceInterface>();
			iPreferences.add(preference);
		}
		
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
		
		public void addPeriod(PeriodInterface period) {
			iPeriods.add(period);
			iDays.add(period.getDay());
			iStarts.add(period.getStartSlot());
		}
		
		public List<PeriodInterface> getPeriods() { return iPeriods; }
		public PeriodInterface getPeriod(int day, int slot) {
			for (PeriodInterface p: iPeriods)
				if (p.getDay() == day && p.getStartSlot() == slot)
					return p;
			return null;
		}
		
		public PreferenceInterface getPreference(int day, int slot) {
			if (iModel == null) return getPreference(iDefaultPreference);
			Map<Integer, Long> slot2id = iModel.get(day);
			return getPreference(slot2id == null ? null : slot2id.get(slot)); 
		}
		
		public void setPreference(int day, int slot, Long optionId) {
			if (iModel == null) iModel = new HashMap<Integer, Map<Integer,Long>>();
			Map<Integer, Long> slot2id = iModel.get(day);
			if (slot2id == null) {
				slot2id = new HashMap<Integer, Long>();
				iModel.put(day, slot2id);
			}
			if (optionId == null)
				slot2id.remove(slot);
			else
				slot2id.put(slot, optionId);
		}
		
		public void setPreference(int day, int slot, PreferenceInterface preference) {
			setPreference(day, slot, preference == null ? null : preference.getId());
		}
	}
	
	public static class PeriodPreferenceRequest implements GwtRpcRequest<PeriodPreferenceModel> {
		public static enum Operation implements IsSerializable {
			LOAD,
			SAVE
		}
		private Operation iOperation;
		private Long iLocationId;
		private PeriodPreferenceModel iModel;
		private Long iExamTypeId = null;
		
		public PeriodPreferenceRequest() {}
		
		public Long getLocationId() { return iLocationId; }
		public void setLocationId(Long locationId) { iLocationId = locationId; }
		
		public PeriodPreferenceModel getModel() { return iModel; }
		public void setModel(PeriodPreferenceModel model) { iModel = model; }

		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public String toString() { return getOperation().name() + "[" + getLocationId() + "," + getExamTypeId() + "]"; }
		
		public Long getExamTypeId() { return iExamTypeId; }
		public void setExamTypeId(Long examTypeId) { iExamTypeId = examTypeId; }
		
		public static PeriodPreferenceRequest load(Long locationId, Long examTypeId) {
			PeriodPreferenceRequest request = new PeriodPreferenceRequest();
			request.setOperation(Operation.LOAD);
			request.setLocationId(locationId);
			request.setExamTypeId(examTypeId);
			return request;
		}
		
		public static PeriodPreferenceRequest save(Long locationId, Long examTypeId, PeriodPreferenceModel model) {
			PeriodPreferenceRequest request = new PeriodPreferenceRequest();
			request.setOperation(Operation.SAVE);
			request.setLocationId(locationId);
			request.setModel(model);
			request.setExamTypeId(examTypeId);
			return request;
		}
	}
	
	public static class RoomPropertiesRequest implements GwtRpcRequest<RoomPropertiesInterface> {
		
		public RoomPropertiesRequest() {}
		
		@Override
		public String toString() { return ""; }
	}
	
	public static class RoomPropertiesInterface implements GwtRpcResponse {
		private Long iSessionId = null;
		private String iSessionName = null;
		private boolean iCanExportPdf = false, iCanExportCsv = false;
		private boolean iCanEditDepartments = false;
		private boolean iCanEditRoomExams = false;
		private boolean iCanAddRoom = false, iCanAddNonUniversity = false;
		private List<RoomTypeInterface> iRoomTypes = new ArrayList<RoomTypeInterface>();
		private List<BuildingInterface> iBuildings = new ArrayList<BuildingInterface>();
		private List<FeatureTypeInterface> iFeatureTypes = new ArrayList<FeatureTypeInterface>();
		private List<DepartmentInterface> iDepartments = new ArrayList<DepartmentInterface>();
		private List<ExamTypeInterface> iExamTypes = new ArrayList<ExamTypeInterface>();
		private List<GroupInterface> iGroups = new ArrayList<GroupInterface>();
		private List<FeatureInterface> iFeatures = new ArrayList<FeatureInterface>();
		private List<PreferenceInterface> iPreferences = new ArrayList<PreferenceInterface>();
		private boolean iCanSeeCourses = false, iCanSeeExams = false, iCanSeeEvents = false;
		private boolean iGridAsText = false, iHorizontal = false, iGoogleMap = false;
		private List<RoomSharingDisplayMode> iModes;
		private String iEllipsoid = null;
		private boolean iCanChangeAvailability = false, iCanChangeControll = false, iCanChangeExternalId = false, iCanChangeExamStatus = false,
				iCanChangeEventProperties = false, iCanChangePicture = false, iCanChangePreferences = false,
				iCanChangeGroups = false, iCanChangeFeatures = false, iCanChangeEventAvailability = false;
		
		public RoomPropertiesInterface() {}
		
		public Long getAcademicSessionId() { return iSessionId; }
		public void setAcademicSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public String getAcademicSessionName() { return iSessionName; }
		public void setAcademicSessionName(String name) { iSessionName = name; }

		public boolean isCanEditDepartments() { return iCanEditDepartments; }
		public void setCanEditDepartments(boolean canEditDepartments) { iCanEditDepartments = canEditDepartments; }

		public boolean isCanExportPdf() { return iCanExportPdf; }
		public void setCanExportPdf(boolean canExportPdf) { iCanExportPdf = canExportPdf; }
		
		public boolean isCanExportCsv() { return iCanExportCsv; }
		public void setCanExportCsv(boolean canExportCsv) { iCanExportCsv = canExportCsv; }

		public boolean isCanEditRoomExams() { return iCanEditRoomExams; }
		public void setCanEditRoomExams(boolean canEditRoomExams) { iCanEditRoomExams = canEditRoomExams; }

		public boolean isCanAddRoom() { return iCanAddRoom; }
		public void setCanAddRoom(boolean canAddRoom) { iCanAddRoom = canAddRoom; }
		
		public boolean isCanAddNonUniversity() { return iCanAddNonUniversity; }
		public void setCanAddNonUniversity(boolean canAddNonUniv) { iCanAddNonUniversity = canAddNonUniv; }
		
		public void addRoomType(RoomTypeInterface roomType) { iRoomTypes.add(roomType); }
		public List<RoomTypeInterface> getRoomTypes() { return iRoomTypes; }
		public RoomTypeInterface getRoomType(Long id) {
			for (RoomTypeInterface type: iRoomTypes)
				if (id.equals(type.getId())) return type;
			return null;
		}

		public void addBuilding(BuildingInterface building) { iBuildings.add(building); }
		public List<BuildingInterface> getBuildings() { return iBuildings; }
		public BuildingInterface getBuilding(Long id) {
			for (BuildingInterface building: iBuildings)
				if (id.equals(building.getId()))
					return building;
			return null;
		}
		
		public void addFeatureType(FeatureTypeInterface featureType) { iFeatureTypes.add(featureType); }
		public List<FeatureTypeInterface> getFeatureTypes() { return iFeatureTypes; }
		public FeatureTypeInterface getFeatureType(Long id) {
			for (FeatureTypeInterface type: iFeatureTypes)
				if (id.equals(type.getId()))
					return type;
			return null;
		}
		
		public void addDepartment(DepartmentInterface department) { iDepartments.add(department); }
		public List<DepartmentInterface> getDepartments() { return iDepartments; }
		public DepartmentInterface getDepartment(Long id) {
			for (DepartmentInterface department: iDepartments)
				if (id.equals(department.getId()))
					return department;
			return null;
		}
		
		public void addExamType(ExamTypeInterface examType) { iExamTypes.add(examType); }
		public List<ExamTypeInterface> getExamTypes() { return iExamTypes; }
		
		public boolean isCanSeeCourses() { return iCanSeeCourses; }
		public void setCanSeeCourses(boolean canSeeCourses) { iCanSeeCourses = canSeeCourses; }

		public boolean isCanSeeExams() { return iCanSeeExams; }
		public void setCanSeeExams(boolean canSeeExams) { iCanSeeExams = canSeeExams; }

		public boolean isCanSeeEvents() { return iCanSeeEvents; }
		public void setCanSeeEvents(boolean canSeeEvents) { iCanSeeEvents = canSeeEvents; }
		
		public boolean isGridAsText() { return iGridAsText; }
		public void setGridAsText(boolean gridAsText) { iGridAsText = gridAsText; }
		
		public boolean isHorizontal() { return iHorizontal; }
		public void setHorizontal(boolean horizontal) { iHorizontal = horizontal; }
		
		public void addGroup(GroupInterface group) { iGroups.add(group); }
		public List<GroupInterface> getGroups() { return iGroups; }
		
		public void addFeature(FeatureInterface feature) { iFeatures.add(feature); }
		public List<FeatureInterface> getFeatures() { return iFeatures; }
		
		public void addPreference(PreferenceInterface preference) { iPreferences.add(preference); }
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
		
		public void addMode(RoomSharingDisplayMode mode) {
			if (iModes == null) iModes = new ArrayList<RoomSharingDisplayMode>();
			iModes.add(mode);
		}
		
		public List<RoomSharingDisplayMode> getModes() {
			return iModes;
		}
		
		public boolean hasModes() { return iModes != null && !iModes.isEmpty(); }
		
		public String getEllipsoid() { return iEllipsoid; }
		public boolean hasEllipsoid() { return iEllipsoid != null & !iEllipsoid.isEmpty(); }
		public void setEllipsoid(String ellipsoid) { iEllipsoid = ellipsoid; }
		
		public void setGoogleMap(boolean map) { iGoogleMap = map; }
		public boolean isGoogleMap() { return iGoogleMap; }
		
		public boolean isCanChangeAvailability() { return iCanChangeAvailability; }
		public void setCanChangeAvailability(boolean canChangeAvailability) { iCanChangeAvailability = canChangeAvailability; }

		public boolean isCanChangeControll() { return iCanChangeControll; }
		public void setCanChangeControll(boolean canChangeControll) { iCanChangeControll = canChangeControll; }

		public boolean isCanChangeExternalId() { return iCanChangeExternalId; }
		public void setCanChangeExternalId(boolean canChangeExternalId) { iCanChangeExternalId = canChangeExternalId; }

		public boolean isCanChangeExamStatus() { return iCanChangeExamStatus; }
		public void setCanChangeExamStatus(boolean canChangeExamStatus) { iCanChangeExamStatus = canChangeExamStatus; }

		public boolean isCanChangeEventProperties() { return iCanChangeEventProperties; }
		public void setCanChangeEventProperties(boolean canChangeEventProperties) { iCanChangeEventProperties = canChangeEventProperties; }

		public boolean isCanChangePicture() { return iCanChangePicture; }
		public void setCanChangePicture(boolean canChangePicture) { iCanChangePicture = canChangePicture; }

		public boolean isCanChangePreferences() { return iCanChangePreferences; }
		public void setCanChangePreferences(boolean canChangePreferences) { iCanChangePreferences = canChangePreferences; }

		public boolean isCanChangeGroups() { return iCanChangeGroups; }
		public void setCanChangeGroups(boolean canChangeGroups) { iCanChangeGroups = canChangeGroups; }

		public boolean isCanChangeFeatures() { return iCanChangeFeatures; }
		public void setCanChangeFeatures(boolean canChangeFeatures) { iCanChangeFeatures = canChangeFeatures; }

		public boolean isCanChangeEventAvailability() { return iCanChangeEventAvailability; }
		public void setCanChangeEventAvailability(boolean canChangeEventAvailability) { iCanChangeEventAvailability = canChangeEventAvailability; }
	}
	
	public static enum RoomsColumn {
		NAME,
		EXTERNAL_ID,
		TYPE,
		CAPACITY,
		EXAM_CAPACITY,
		AREA,
		COORDINATES,
		DISTANCE_CHECK,
		ROOM_CHECK,
		MAP,
		PICTURES,
		PREFERENCE,
		AVAILABILITY,
		DEPARTMENTS,
		CONTROL_DEPT,
		EXAM_TYPES,
		PERIOD_PREF,
		EVENT_DEPARTMENT,
		EVENT_STATUS,
		EVENT_AVAILABILITY,
		EVENT_MESSAGE,
		BREAK_TIME,
		GROUPS,
		FEATURES,
		;

		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}
	
	public static enum RoomsPageMode implements IsSerializable {
		COURSES("department:Managed", RoomsColumn.NAME, RoomsColumn.TYPE, RoomsColumn.CAPACITY, RoomsColumn.AREA, RoomsColumn.AVAILABILITY, RoomsColumn.DEPARTMENTS, RoomsColumn.FEATURES, RoomsColumn.GROUPS),
		EXAMS("department:Managed", RoomsColumn.NAME, RoomsColumn.TYPE, RoomsColumn.CAPACITY, RoomsColumn.EXAM_CAPACITY, RoomsColumn.AREA, RoomsColumn.EXAM_TYPES, RoomsColumn.PERIOD_PREF, RoomsColumn.FEATURES, RoomsColumn.GROUPS),
		EVENTS("flag:Events department:Managed", RoomsColumn.NAME, RoomsColumn.TYPE, RoomsColumn.CAPACITY, RoomsColumn.AREA, RoomsColumn.EVENT_DEPARTMENT, RoomsColumn.EVENT_AVAILABILITY,
				RoomsColumn.EVENT_STATUS, RoomsColumn.EVENT_MESSAGE, RoomsColumn.BREAK_TIME, RoomsColumn.FEATURES, RoomsColumn.GROUPS), 
		;
		private String iQuery;
		private int iColumns;
		RoomsPageMode(String query, RoomsColumn... column) {
			iQuery = query;
			iColumns = 0;
			for (RoomsColumn f: column) iColumns += f.flag();
		}
		
		public String getQuery() { return iQuery; }
		public int getColumns() { return iColumns; }
	}
	
	public static class RoomFilterRpcRequest extends org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest {
		private static final long serialVersionUID = 1L;
		
		public RoomFilterRpcRequest() {
			super();
		}
	}
}
