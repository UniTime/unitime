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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.base.BasePreferenceLevel;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "preference_level")
public class PreferenceLevel extends BasePreferenceLevel {
	private static final long serialVersionUID = 1L;

    /** Request attribute name for available preference levels  **/
    public static String PREF_LEVEL_ATTR_NAME = "prefLevelsList";

    /** Level for neutral preference **/
    public static String PREF_LEVEL_NEUTRAL = "4";

    /** Level for required preference **/
    public static String PREF_LEVEL_REQUIRED = "1";
        
    public static String sProhibited = Constants.sPreferenceProhibited;
    public static String sRequired = Constants.sPreferenceRequired;
    public static String sStronglyDiscouraged = Constants.sPreferenceStronglyDiscouraged;
    public static String sDiscouraged = Constants.sPreferenceDiscouraged;
    public static String sPreferred = Constants.sPreferencePreferred;
    public static String sStronglyPreferred = Constants.sPreferenceStronglyPreferred;
    public static String sNeutral = Constants.sPreferenceNeutral;
    public static String sNotAvailable = "N";
    
    public static final char sCharLevelProhibited = 'P';
    public static final char sCharLevelRequired = 'R';
    public static final char sCharLevelStronglyDiscouraged = '4';
    public static final char sCharLevelDiscouraged = '3';
    public static final char sCharLevelPreferred = '1';
    public static final char sCharLevelStronglyPreferred = '0';
    public static final char sCharLevelNeutral = '2';
    public static final char sCharLevelNotAvailable = 'N';

    public static int sIntLevelProhibited = Constants.sPreferenceLevelProhibited;
    public static int sIntLevelRequired = Constants.sPreferenceLevelRequired;
    public static int sIntLevelStronglyDiscouraged = Constants.sPreferenceLevelStronglyDiscouraged;
    public static int sIntLevelDiscouraged = Constants.sPreferenceLevelDiscouraged;
    public static int sIntLevelPreferred = Constants.sPreferenceLevelPreferred;
    public static int sIntLevelStronglyPreferred = Constants.sPreferenceLevelStronglyPreferred;
    public static int sIntLevelNeutral = Constants.sPreferenceLevelNeutral;
    public static int sIntLevelNotAvailable = Constants.sPreferenceLevelProhibited;
    
    public static enum PrefColor {
    	Required(Constants.sPreferenceRequired, 'R', Constants.sPreferenceLevelRequired,
    			0x3c3cb4, 0x3c3cb4, 0x3c3cb4, 0x7a7aff),
    	StronglyPreferred(Constants.sPreferenceStronglyPreferred, '0', Constants.sPreferenceLevelStronglyPreferred,
        		0x0f821e, 0x195820, 0x0e771c, 0x1ea03c),
    	Preferred(Constants.sPreferencePreferred, '1', Constants.sPreferenceLevelPreferred,
        		0x32c814, 0x1e7000, 0x32c814, 0x46e61e),
    	Neutral(Constants.sPreferenceNeutral, '2', Constants.sPreferenceLevelNeutral,
        		0x0a0a0a, 0x0a0a0a, 0xf0f0f0, 0xf0f0f0),
    	Disouraged(Constants.sPreferenceDiscouraged, '3', Constants.sPreferenceLevelDiscouraged,
        		0xdcb414, 0x755e00, 0xdcb414, 0xdcb43c),
    	StronglyDiscouraged(Constants.sPreferenceStronglyDiscouraged, '4', Constants.sPreferenceLevelStronglyDiscouraged,
        		0xf06428, 0xa0441c, 0xf06428, 0xf0783c),
    	Prohibited(Constants.sPreferenceProhibited, 'P', Constants.sPreferenceLevelProhibited,
        		0xc81e14, 0xbd1c14, 0xc81e14, 0xff4040),
    	NotAvailable("N", 'N', Constants.sPreferenceLevelProhibited,
        		0x696969, 0x616161, 0x969696, 0x9f9f9f),
    	;
        
    	private String iProlog;
    	private char iChar;
    	private int iLevel;
    	private int iBgColor;
    	private int iLightBgColor;
    	private int iColor;
    	private int iAdaColor;
    	
    	PrefColor(String prolog, char prefChar, int level, int color, int adaColor, int bgColor, int lightBgColor) {
    		iProlog = prolog;
    		iChar = prefChar;
    		iLevel = level;
    		iBgColor = bgColor;
    		iColor = color;
    		iAdaColor = adaColor;
    		iLightBgColor = lightBgColor;
    	}
    	
    	public String getProlog() { return iProlog; }
    	public char getChar() { return iChar; }
    	public int getLevel() { return iLevel; }
    	public int getColor() { return iColor; }
    	public int getAdaColor() { return iAdaColor; }
    	public int getBgColor() { return iBgColor; }
    	public int getLightBgColor() { return iLightBgColor; }
    	
    	public static Color toAwtColor(int color) {
    		return new Color(color);
    	}
    	public static int toR(int color) {
    		return (color >> 16) & 0xFF;
    	}
    	public static int toG(int color) {
    		return (color >> 8) & 0xFF;
    	}
    	public static int toB(int color) {
    		return color & 0xFF;
    	}
    	public static String toRGB(int color) {
    		return "rgb(" + toR(color) + "," + toG(color) + "," + toB(color) + ")";
    	}
    	public static String toHex(int color) {
    		String hex = Integer.toString(color, 16);
    		while (hex.length() < 6) hex = "0" + hex;
    		return "#" + hex;
    	}
    	
    	public static PrefColor fromProlog(String prolog) {
    		for (PrefColor pc: PrefColor.values())
    			if (pc.getProlog().equals(prolog)) return pc;
    		return null;
    	}
    	public static PrefColor fromChar(char ch) {
    		for (PrefColor pc: PrefColor.values())
    			if (pc.getChar() == ch) return pc;
    		return null;
    	}
    	public static PrefColor fromProlog(int level) {
    		return fromProlog(Constants.preferenceLevel2preference(level));
    	}
    	public static PrefColor fromProlog(PreferenceLevel preference) {
    		return fromProlog(preference.getPrefProlog());
    	}
    }
    
/*[CONSTRUCTOR MARKER BEGIN]*/
	public PreferenceLevel () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public PreferenceLevel (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/* Values
            1,R,Required
            2,-2,Strongly Preferred
            3,-1,Preferred
            4,0,Neutral
            5,1,Discouraged
            6,2,Strongly Discouraged
            7,P,Prohibited
	 */
	
    /**
	 * Retrieves all preference levels in the database
	 * ordered by column pref_id
	 * @return Vector of PreferenceLevel objects
	 */
	@Transient
	public static synchronized List<PreferenceLevel> getPreferenceLevelList() {
		return getPreferenceLevelList(false);
	}
	
    public static synchronized List<PreferenceLevel> getPreferenceLevelList(boolean includeNotAvailable) {
    	if (includeNotAvailable) {
    		return PreferenceLevelDAO.getInstance().getSession().createQuery(
    				"from PreferenceLevel order by prefId", PreferenceLevel.class)
    				.setCacheable(true).list();
    	} else {
    		return PreferenceLevelDAO.getInstance().getSession().createQuery(
    				"from PreferenceLevel where prefProlog != :na order by prefId", PreferenceLevel.class)
    				.setParameter("na", sNotAvailable).setCacheable(true).list();
    	}
    }

	@Transient
    public static List<PreferenceLevel> getPreferenceLevelListSoftOnly() {
    	List<PreferenceLevel> ret = getPreferenceLevelList(false);
    	for (Iterator<PreferenceLevel> i = ret.iterator(); i.hasNext(); ) {
    		PreferenceLevel level = i.next();
    		if (sRequired.equals(level.getPrefProlog()) || sProhibited.equals(level.getPrefProlog())) i.remove();
    	}
    	return ret;
    }

    /**
     * Override default equals() behavior - compares prefId
     */
    public boolean equals(Object o) {
        if (o==null || !(o instanceof PreferenceLevel)) return false;
        return getPrefId().intValue()==((PreferenceLevel)o).getPrefId().intValue();
    }
    
    /**
     * Override default hashCode() behavior
     */
    public int hashCode() {
        return getPrefId().intValue();
    }
    
    /**
     * Override default toString() behavior
     */
    public String toString() {
        return "PreferenceLevel{id=" + getPrefId().intValue() + 
        						",prolog=" + getPrefProlog() + 
        						",name=" + getPrefName() + "}";
    }

    /**
     * Retrieves PreferenceLevel for given Pref Id (not uniqueid)
     * @param id Preference Id
     * @return PreferenceLevel object
     */
    public static PreferenceLevel getPreferenceLevel(int id) {
        for (PreferenceLevel p: getPreferenceLevelList())
            if (p.getPrefId() == id) return p;
        return null;
    }
    
    /**
     * Retrieves PreferenceLevel for given Prolog Id
     * @param id Prolog Id
     * @return PreferenceLevel object
     */
    public static PreferenceLevel getPreferenceLevel(String prologId) {
        for (PreferenceLevel p: getPreferenceLevelList(true))
            if (p.getPrefProlog().equalsIgnoreCase(prologId)) return p;
        return null;
    }

    /**
     * Combines two preference levels to give an effective preference level (???) 
     * @param another PreferenceLevel object to be combined
     * @return Effective PreferenceLevel object
     */
    public PreferenceLevel combine(PreferenceLevel another) {
        if (getPrefProlog().equals(another.getPrefProlog())) 
            return this;
        
        if (getPrefProlog().equals(sProhibited) || another.getPrefProlog().equals(sProhibited)) 
            return getPreferenceLevel(sProhibited);
        
        if (getPrefProlog().equals(sRequired)) 
            return another;
        
        if (another.getPrefProlog().equals(sRequired)) 
            return this;
        
        return getPreferenceLevel( String.valueOf( 
                Math.max( 
                        Integer.parseInt( getPrefProlog() ),
                        Integer.parseInt( another.getPrefProlog() )
                        )) );
    }  
    
    /** preference to color conversion */
    public String prefcolor() {
    	return PreferenceLevel.prolog2color(getPrefProlog());
    }
    
	@Transient
    public String getDropdownOptionStyle() {
    	return "background-color: " + prefcolor() + ";";
    }
    
    public String prefcolorNeutralBlack() {
    	if (sNeutral.equals(getPrefProlog())) return "black";
    	else return prefcolor();
    }
    
    /** preference to color conversion */
    public Color awtPrefcolor() {
    	return PreferenceLevel.prolog2awtColor(this.getPrefProlog());
    }

	public static String int2color(int intPref) {
		return prolog2color(int2prolog(intPref));
	}
	
	public static char int2char(int intPref) {
		return prolog2char(int2prolog(intPref));
	}
	
	public static int prolog2int(String prologPref) {
        return Constants.preference2preferenceLevel(prologPref);
	}
	
	public static String prolog2color(String prologPref) {
		PrefColor color = PrefColor.fromProlog(prologPref);
		if (color == null) color = PrefColor.fromProlog(sNeutral);
		if (CommonValues.Legacy.eq(UserProperty.HighContrastPreferences.get()))
			return PrefColor.toHex(color.getColor());
		else
			return PrefColor.toHex(color.getAdaColor());
	}
	
	public static String prolog2style(String prologPref) {
		if (CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get())) {
			return "pref-" + PreferenceLevel.prolog2char(prologPref);
		} else {
			return null;
		}
	}

	public static String prolog2abbv(String prologPref) {
		PreferenceLevel pref = getPreferenceLevel(prologPref);
		return (pref == null ? null : pref.getPrefAbbv() == null ? "" : pref.getPrefAbbv());
	}

	public static Color prolog2awtColor(String prologPref) {
		PrefColor color = PrefColor.fromProlog(prologPref);
		if (color == null) color = PrefColor.fromProlog(sNeutral);
		return PrefColor.toAwtColor(color.getBgColor());
	}
	
	public static Color int2awtColor(int intPref, Color neutralPref) {
		String prologPref = int2prolog(intPref);
		if (prologPref == null || prologPref.equals(sNeutral))
			return neutralPref;
		return prolog2awtColor(prologPref);
	}

	public static String int2prolog(int intPref) {
        return Constants.preferenceLevel2preference(intPref);
    }
	
	public static String int2bgColor(int intPref) {
		return prolog2bgColor(int2prolog(intPref));
	}

	public static String prolog2bgColor(String prologPref) {
		PrefColor color = PrefColor.fromProlog(prologPref);
		if (color == null) color = PrefColor.fromProlog(sNeutral);
		return PrefColor.toRGB(color.getBgColor());
    }
	
	public static String int2bgLightColor(int intPref) {
		return prolog2bgLightColor(int2prolog(intPref));
	}

	public static String prolog2bgLightColor(String prologPref) {
		PrefColor color = PrefColor.fromProlog(prologPref);
		if (color == null) color = PrefColor.fromProlog(sNeutral);
		return PrefColor.toRGB(color.getLightBgColor());
    }

	public static String prolog2string(String prologPref) {
		return getPreferenceLevel(prologPref).getPrefName();
	}
	
	public static String int2string(int intPref) {
		return prolog2string(int2prolog(intPref));
    }
	
	public static char prolog2char(String prologPref) {
		if (sRequired.equals(prologPref))
			return sCharLevelRequired;
		if (sStronglyPreferred.equals(prologPref))
			return sCharLevelStronglyPreferred;
		if (sPreferred.equals(prologPref))
			return sCharLevelPreferred;
		if (sDiscouraged.equals(prologPref))
			return sCharLevelDiscouraged;
		if (sStronglyDiscouraged.equals(prologPref))
			return sCharLevelStronglyDiscouraged;
		if (sProhibited.equals(prologPref))
			return sCharLevelProhibited;
		if (sNotAvailable.equals(prologPref))
			return sCharLevelNotAvailable;
		return sCharLevelNeutral;
	}
	
	public static String char2prolog(char charPref) {
		switch (charPref) {
			case sCharLevelProhibited : return sProhibited;
			case sCharLevelStronglyDiscouraged : return sStronglyDiscouraged;
			case sCharLevelDiscouraged : return sDiscouraged;
			case sCharLevelNeutral : return sNeutral;
			case sCharLevelPreferred : return sPreferred;
			case sCharLevelStronglyPreferred : return sStronglyPreferred;
			case sCharLevelRequired : return sRequired;
			case sCharLevelNotAvailable : return sNotAvailable;
			default : return sNeutral;
		}
	}
	
	@Transient
	public boolean isHard() {
		return sProhibited.equals(getPrefProlog()) || sRequired.equals(getPrefProlog());
	}

	@Transient
	public String getAbbreviation() {
		return getPrefAbbv() == null ? "" : getPrefAbbv();
	}
    
	
	public static void main(String[] args) {
		for (PrefColor p: PrefColor.values()) {
			System.out.println(p.name() + "(" + p.getProlog() + ", " + PrefColor.toHex(p.getAdaColor()) + ". " + PrefColor.toRGB(p.getAdaColor()) + ")");
		}
	}
}
