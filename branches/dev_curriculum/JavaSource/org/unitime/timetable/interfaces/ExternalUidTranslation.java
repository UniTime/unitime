package org.unitime.timetable.interfaces;

public interface ExternalUidTranslation {
    public static enum Source {
        Staff,   // Staff/DepartmentalInstructor tables
        Student, // Student table
        User,    // Authentication, TimetableManager, etc.
        LDAP     // LDAP lookup
    }

    public String translate(String uid, Source source, Source target);
    
}
