package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.PreferenceGroup;

public class ExamObjectsComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
        PreferenceGroup pg1 = (PreferenceGroup)o1;
        PreferenceGroup pg2 = (PreferenceGroup)o2;
        return 0;
    }

}
