/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BasePreferenceLevel;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.util.Constants;




public class PreferenceLevel extends BasePreferenceLevel {
	private static final long serialVersionUID = 1L;

    /** Request attribute name for available preference levels  **/
    public static String PREF_LEVEL_ATTR_NAME = "prefLevelsList";

    /** Level for neutral preference **/
    public static String PREF_LEVEL_NEUTRAL = "4";

    /** Level for required preference **/
    public static String PREF_LEVEL_REQUIRED = "1";
    
    /** preference to color conversion */
    private static Hashtable sPref2color = null;
    /** preference to color conversion (hexadecimal) */
    private static Hashtable sHexPref2color = null;
    /** preference to awt color conversion */
    private static Hashtable sAwtPref2color = null;
    /** preference to background color conversion */
    private static Hashtable sBgPref2color = null;
    
    private static Hashtable sPref2abbv = null;

    public static String sProhibited = Constants.sPreferenceProhibited;
    public static String sRequired = Constants.sPreferenceRequired;
    public static String sStronglyDiscouraged = Constants.sPreferenceStronglyDiscouraged;
    public static String sDiscouraged = Constants.sPreferenceDiscouraged;
    public static String sPreferred = Constants.sPreferencePreferred;
    public static String sStronglyPreferred = Constants.sPreferenceStronglyPreferred;
    public static String sNeutral = Constants.sPreferenceNeutral;
    
    public static final char sCharLevelProhibited = 'P';
    public static final char sCharLevelRequired = 'R';
    public static final char sCharLevelStronglyDiscouraged = '4';
    public static final char sCharLevelDiscouraged = '3';
    public static final char sCharLevelPreferred = '1';
    public static final char sCharLevelStronglyPreferred = '0';
    public static final char sCharLevelNeutral = '2';

    public static int sIntLevelProhibited = Constants.sPreferenceLevelProhibited;
    public static int sIntLevelRequired = Constants.sPreferenceLevelRequired;
    public static int sIntLevelStronglyDiscouraged = Constants.sPreferenceLevelStronglyDiscouraged;
    public static int sIntLevelDiscouraged = Constants.sPreferenceLevelDiscouraged;
    public static int sIntLevelPreferred = Constants.sPreferenceLevelDiscouraged;
    public static int sIntLevelStronglyPreferred = Constants.sPreferenceLevelStronglyPreferred;
    public static int sIntLevelNeutral = Constants.sPreferenceLevelNeutral;

    /** static initialization */
    static {
        sPref2color = new Hashtable();
        sPref2color.put(sRequired,"rgb(60,60,180)");
        sPref2color.put(sStronglyPreferred,"rgb(15,130,30)"); // rgb(20,160,40)
        sPref2color.put(sPreferred,"rgb(50,200,20)"); // rgb(110,200,20), rgb(150,240,40)
        sPref2color.put(sNeutral,"rgb(240,240,240)");
        sPref2color.put(sDiscouraged,"rgb(220,180,20)"); //rgb(240,200,40)
        sPref2color.put(sStronglyDiscouraged,"rgb(240,100,40)");
        sPref2color.put(sProhibited,"rgb(200,30,20)");
        sAwtPref2color = new Hashtable();
        sAwtPref2color.put(sRequired,new Color(60,60,180));
        sAwtPref2color.put(sStronglyPreferred,new Color(15,130,30));
        sAwtPref2color.put(sPreferred,new Color(50,200,20));
        sAwtPref2color.put(sNeutral,new Color(240,240,240));
        sAwtPref2color.put(sDiscouraged,new Color(220,180,20));
        sAwtPref2color.put(sStronglyDiscouraged,new Color(240,100,40));
        sAwtPref2color.put(sProhibited,new Color(200,30,20));
        sBgPref2color = new Hashtable();
        sBgPref2color.put(sRequired,"rgb(80,80,200)");
        sBgPref2color.put(sStronglyPreferred,"rgb(30,160,60)"); //rgb(40,180,60)
        sBgPref2color.put(sPreferred,"rgb(70,230,30)");//rgb(170,240,60)
        sBgPref2color.put(sNeutral,"rgb(240,240,240)");
        sBgPref2color.put(sDiscouraged,"rgb(240,210,60)");
        sBgPref2color.put(sStronglyDiscouraged,"rgb(240,120,60)");
        sBgPref2color.put(sProhibited,"rgb(220,50,40)");
        sHexPref2color = new Hashtable();
        sHexPref2color.put(sRequired,"#3c3cb4");
        sHexPref2color.put(sStronglyPreferred,"#0f821e"); //14a028
        sHexPref2color.put(sPreferred,"#32c814"); //6ec814, 96f028
        sHexPref2color.put(sNeutral,"#0a0a0a");
        sHexPref2color.put(sDiscouraged,"#dcb414"); //f0c828
        sHexPref2color.put(sStronglyDiscouraged,"#f06428");
        sHexPref2color.put(sProhibited,"#c81e14");
        sPref2abbv = new Hashtable();
        sPref2abbv.put(sRequired,"Req");
        sPref2abbv.put(sStronglyPreferred,"StrPref");
        sPref2abbv.put(sPreferred,"Pref"); //96f028
        sPref2abbv.put(sNeutral,"");
        sPref2abbv.put(sDiscouraged,"Disc"); //f0c828
        sPref2abbv.put(sStronglyDiscouraged,"StrDisc");
        sPref2abbv.put(sProhibited,"Proh");
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
	 * @return Returns the sAwtPref2color.
	 */
	public static Hashtable getSAwtPref2color() {
		return sAwtPref2color;
	}
	/**
	 * @return Returns the sPref2color.
	 */
	public static Hashtable getSPref2color() {
		return sPref2color;
	}
	
    /**
	 * Retrieves all preference levels in the database
	 * ordered by column pref_id
	 * @return Vector of PreferenceLevel objects
	 */
    public static synchronized List<PreferenceLevel> getPreferenceLevelList() {
    	return PreferenceLevelDAO.getInstance().findAll(Order.asc("prefId"));
    }

    public static List<PreferenceLevel> getPreferenceLevelListSoftOnly() {
    	List<PreferenceLevel> ret = getPreferenceLevelList();
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
        for (PreferenceLevel p: getPreferenceLevelList())
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
        if (getSPref2color().containsKey(this.getPrefProlog()))
            return (String)getSPref2color().get(this.getPrefProlog());
        else {
            Debug.log("Unknown color for preference "+this.getPrefName()+".");
            return "rgb(200,200,200)";
        }
    }
    
    /** preference to color conversion */
    public Color awtPrefcolor() {
        if (getSAwtPref2color().containsKey(this.getPrefProlog()))
            return (Color)getSAwtPref2color().get(this.getPrefProlog());
        else {
            Debug.log("Unknown color for preference "+this.getPrefName()+".");
            return new Color(200,200,200);
        }
    }

	public static String int2color(int intPref) {
		return prolog2color(int2prolog(intPref));
	}
	
	public static int prolog2int(String prologPref) {
        return Constants.preference2preferenceLevel(prologPref);
	}
	
	public static String prolog2color(String prologPref) {
    	String ret = (String)sHexPref2color.get(prologPref);
    	if (ret==null)
    		ret = (String)sHexPref2color.get(sNeutral);
    	return ret;
	}

	public static String prolog2abbv(String prologPref) {
    	String ret = (String)sPref2abbv.get(prologPref);
    	if (ret==null)
    		ret = (String)sPref2abbv.get(sNeutral);
    	return ret;
	}

	public static String prolog2colorNohex(String prologPref) {
    	String ret = (String)sPref2color.get(prologPref);
    	if (ret==null)
    		ret = (String)sPref2color.get(sNeutral);
    	return ret;
	}

	public static Color prolog2awtColor(String prologPref) {
		Color ret = (Color)sAwtPref2color.get(prologPref);
    	if (ret==null)
    		ret = (Color)sAwtPref2color.get(sNeutral);
    	return ret;
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
    	String ret = (String)sBgPref2color.get(prologPref);
    	if (ret==null)
    		ret = (String)sBgPref2color.get(sNeutral);
    	return ret;
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
			default : return sNeutral;
		}
	}
	
	public boolean isHard() {
		return sProhibited.equals(getPrefProlog()) || sRequired.equals(getPrefProlog());
	}
	
    
}
