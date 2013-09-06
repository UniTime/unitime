/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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

spool schema.log

drop user timetable cascade;

create user timetable identified by unitime;

grant dba to timetable;

prompt
prompt Creating table DATE_PATTERN
prompt ===========================
prompt
create table timetable.DATE_PATTERN
(
  uniqueid   NUMBER(20),
  name       VARCHAR2(50),
  pattern    VARCHAR2(366),
  offset     NUMBER(10),
  type       NUMBER(10),
  visible    NUMBER(1),
  session_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DATE_PATTERN
  add constraint PK_DATE_PATTERN primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DATE_PATTERN
  add constraint NN_DATE_PATTERN_OFFSET
  check ("OFFSET" IS NOT NULL);
alter table timetable.DATE_PATTERN
  add constraint NN_DATE_PATTERN_PATTERN
  check ("PATTERN" IS NOT NULL);
alter table timetable.DATE_PATTERN
  add constraint NN_DATE_PATTERN_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.DATE_PATTERN
  add constraint NN_DATE_PATTERN_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_DATE_PATTERN_SESSION on timetable.DATE_PATTERN (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table DEPT_STATUS_TYPE
prompt ===============================
prompt
create table timetable.DEPT_STATUS_TYPE
(
  uniqueid  NUMBER(20),
  reference VARCHAR2(20),
  label     VARCHAR2(60),
  status    NUMBER(10),
  apply     NUMBER(10),
  ord       NUMBER(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPT_STATUS_TYPE
  add constraint PK_DEPT_STATUS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPT_STATUS_TYPE
  add constraint NN_DEPT_STATUS_TYPE_APPLY
  check ("APPLY" IS NOT NULL);
alter table timetable.DEPT_STATUS_TYPE
  add constraint NN_DEPT_STATUS_TYPE_LABEL
  check ("LABEL" IS NOT NULL);
alter table timetable.DEPT_STATUS_TYPE
  add constraint NN_DEPT_STATUS_TYPE_ORD
  check ("ORD" IS NOT NULL);
alter table timetable.DEPT_STATUS_TYPE
  add constraint NN_DEPT_STATUS_TYPE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.DEPT_STATUS_TYPE
  add constraint NN_DEPT_STATUS_TYPE_STATUS
  check ("STATUS" IS NOT NULL);
alter table timetable.DEPT_STATUS_TYPE
  add constraint NN_DEPT_STATUS_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table SESSIONS
prompt =======================
prompt
create table timetable.SESSIONS
(
  academic_initiative     VARCHAR2(20),
  session_begin_date_time DATE,
  classes_end_date_time   DATE,
  session_end_date_time   DATE,
  uniqueid                NUMBER(20) not null,
  holidays                VARCHAR2(366),
  def_datepatt_id         NUMBER(20),
  status_type             NUMBER(20),
  last_modified_time      TIMESTAMP(6),
  academic_year           VARCHAR2(4),
  academic_term           VARCHAR2(20),
  exam_begin_date         DATE,
  event_begin_date        DATE,
  event_end_date          DATE
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SESSIONS
  add constraint PK_SESSIONS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SESSIONS
  add constraint FK_SESSIONS_STATUS_TYPE foreign key (STATUS_TYPE)
  references timetable.DEPT_STATUS_TYPE (UNIQUEID) on delete cascade;
alter table timetable.SESSIONS
  add constraint FK_SESSION_DATEPATT foreign key (DEF_DATEPATT_ID)
  references timetable.DATE_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_ACADEMIC_INITIATIV
  check ("ACADEMIC_INITIATIVE" IS NOT NULL);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_ACADEMIC_TERM
  check ("ACADEMIC_TERM" IS NOT NULL);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_ACADEMIC_YEAR
  check ("ACADEMIC_YEAR" IS NOT NULL);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_CLASSESENDDATETIME
  check ("CLASSES_END_DATE_TIME" IS NOT NULL);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_EVENT_BEGIN_DATE
  check (event_begin_date is not null);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_EVENT_END_DATE
  check (event_end_date is not null);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_EXAM_BEGIN_DATE
  check (exam_begin_date is not null);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_SESSIONENDDATETIME
  check ("SESSION_END_DATE_TIME" IS NOT NULL);
alter table timetable.SESSIONS
  add constraint NN_SESSIONS_SESSION_BEGI_DT_TM
  check ("SESSION_BEGIN_DATE_TIME" IS NOT NULL);
alter table timetable.DATE_PATTERN
  add constraint FK_DATE_PATTERN_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
create index timetable.IDX_SESSIONS_DATE_PATTERN on timetable.SESSIONS (DEF_DATEPATT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SESSIONS_STATUS_TYPE on timetable.SESSIONS (STATUS_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ACADEMIC_AREA
prompt ============================
prompt
create table timetable.ACADEMIC_AREA
(
  uniqueid                   NUMBER(20),
  session_id                 NUMBER(20),
  academic_area_abbreviation VARCHAR2(10),
  short_title                VARCHAR2(50),
  long_title                 VARCHAR2(100),
  external_uid               VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACADEMIC_AREA
  add constraint PK_ACADEMIC_AREA primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACADEMIC_AREA
  add constraint UK_ACADEMIC_AREA unique (SESSION_ID, ACADEMIC_AREA_ABBREVIATION)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACADEMIC_AREA
  add constraint FK_ACADEMIC_AREA_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.ACADEMIC_AREA
  add constraint NN_ACADEMIC_AREA_ACAD_AREA_ABB
  check ("ACADEMIC_AREA_ABBREVIATION" IS NOT NULL);
alter table timetable.ACADEMIC_AREA
  add constraint NN_ACADEMIC_AREA_LONG_TITLE
  check ("LONG_TITLE" IS NOT NULL);
alter table timetable.ACADEMIC_AREA
  add constraint NN_ACADEMIC_AREA_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.ACADEMIC_AREA
  add constraint NN_ACADEMIC_AREA_SHORT_TITLE
  check ("SHORT_TITLE" IS NOT NULL);
alter table timetable.ACADEMIC_AREA
  add constraint NN_ACADEMIC_AREA_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ACADEMIC_AREA_ABBV on timetable.ACADEMIC_AREA (ACADEMIC_AREA_ABBREVIATION, SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ACADEMIC_CLASSIFICATION
prompt ======================================
prompt
create table timetable.ACADEMIC_CLASSIFICATION
(
  uniqueid     NUMBER(20),
  session_id   NUMBER(20),
  code         VARCHAR2(10),
  name         VARCHAR2(50),
  external_uid VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACADEMIC_CLASSIFICATION
  add constraint PK_ACAD_CLASS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACADEMIC_CLASSIFICATION
  add constraint FK_ACAD_CLASS_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.ACADEMIC_CLASSIFICATION
  add constraint NN_ACAD_CLASS_CODE
  check ("CODE" IS NOT NULL);
alter table timetable.ACADEMIC_CLASSIFICATION
  add constraint NN_ACAD_CLASS_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.ACADEMIC_CLASSIFICATION
  add constraint NN_ACAD_CLASS_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.ACADEMIC_CLASSIFICATION
  add constraint NN_ACAD_CLASS_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ACADEMIC_CLASF_CODE on timetable.ACADEMIC_CLASSIFICATION (CODE, SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table RESERVATION_TYPE
prompt ===============================
prompt
create table timetable.RESERVATION_TYPE
(
  uniqueid  NUMBER(20),
  reference VARCHAR2(20),
  label     VARCHAR2(60)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.RESERVATION_TYPE
  add constraint PK_RESERVATION_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.RESERVATION_TYPE
  add constraint UK_RESERVATION_TYPE_LABEL unique (LABEL)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.RESERVATION_TYPE
  add constraint UK_RESERVATION_TYPE_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.RESERVATION_TYPE
  add constraint NN_RESERVATION_TYPE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.RESERVATION_TYPE
  add constraint NN_RESERVATION_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table ACAD_AREA_RESERVATION
prompt ====================================
prompt
create table timetable.ACAD_AREA_RESERVATION
(
  uniqueid             NUMBER(20),
  owner                NUMBER(20),
  reservation_type     NUMBER(20),
  acad_classification  NUMBER(20),
  acad_area            NUMBER(20),
  priority             NUMBER(5),
  reserved             NUMBER(10),
  prior_enrollment     NUMBER(10),
  projected_enrollment NUMBER(10),
  owner_class_id       VARCHAR2(1),
  requested            NUMBER(10),
  last_modified_time   TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACAD_AREA_RESERVATION
  add constraint PK_ACAD_AREA_RESV primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ACAD_AREA_RESERVATION
  add constraint FK_ACAD_AREA_RESV_ACAD_AREA foreign key (ACAD_AREA)
  references timetable.ACADEMIC_AREA (UNIQUEID) on delete cascade;
alter table timetable.ACAD_AREA_RESERVATION
  add constraint FK_ACAD_AREA_RESV_ACAD_CLASS foreign key (ACAD_CLASSIFICATION)
  references timetable.ACADEMIC_CLASSIFICATION (UNIQUEID) on delete cascade;
alter table timetable.ACAD_AREA_RESERVATION
  add constraint FK_ACAD_AREA_RESV_TYPE foreign key (RESERVATION_TYPE)
  references timetable.RESERVATION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_ACAD_AREA
  check ("ACAD_AREA" IS NOT NULL);
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_OWNER
  check ("OWNER" IS NOT NULL);
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_OWNER_CLS_ID
  check ("OWNER_CLASS_ID" IS NOT NULL);
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_PRIORITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_RESERVED
  check ("RESERVED" IS NOT NULL);
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_RESERV_TYPE
  check ("RESERVATION_TYPE" IS NOT NULL);
alter table timetable.ACAD_AREA_RESERVATION
  add constraint NN_ACAD_AREA_RESV_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ACAD_AREA_RESV_ACAD_AREA on timetable.ACAD_AREA_RESERVATION (ACAD_AREA)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ACAD_AREA_RESV_ACAD_CLASS on timetable.ACAD_AREA_RESERVATION (ACAD_CLASSIFICATION)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ACAD_AREA_RESV_OWNER on timetable.ACAD_AREA_RESERVATION (OWNER)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ACAD_AREA_RESV_OWNER_CLS on timetable.ACAD_AREA_RESERVATION (OWNER_CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ACAD_AREA_RESV_TYPE on timetable.ACAD_AREA_RESERVATION (RESERVATION_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table APPLICATION_CONFIG
prompt =================================
prompt
create table timetable.APPLICATION_CONFIG
(
  name        VARCHAR2(1000),
  value       VARCHAR2(4000),
  description VARCHAR2(100)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.APPLICATION_CONFIG
  add constraint PK_APPLICATION_CONFIG primary key (NAME)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.APPLICATION_CONFIG
  add constraint NN_APPLICATION_CONFIG_KEY
  check ("NAME" IS NOT NULL);

prompt
prompt Creating table OFFR_CONSENT_TYPE
prompt ================================
prompt
create table timetable.OFFR_CONSENT_TYPE
(
  uniqueid  NUMBER(20),
  reference VARCHAR2(20),
  label     VARCHAR2(60)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_CONSENT_TYPE
  add constraint PK_OFFR_CONSENT_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_CONSENT_TYPE
  add constraint UK_OFFR_CONSENT_TYPE_LABEL unique (LABEL)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_CONSENT_TYPE
  add constraint UK_OFFR_CONSENT_TYPE_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_CONSENT_TYPE
  add constraint NN_OFFR_CONSENT_TYPE_REF
  check ("REFERENCE" IS NOT NULL);
alter table timetable.OFFR_CONSENT_TYPE
  add constraint NN_OFFR_CONSENT_TYPE_UNIQUE_ID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table INSTRUCTIONAL_OFFERING
prompt =====================================
prompt
create table timetable.INSTRUCTIONAL_OFFERING
(
  uniqueid               NUMBER(20),
  session_id             NUMBER(20),
  instr_offering_perm_id NUMBER(10),
  not_offered            NUMBER(1),
  limit                  NUMBER(4),
  consent_type           NUMBER(20),
  designator_required    NUMBER(1),
  last_modified_time     TIMESTAMP(6),
  uid_rolled_fwd_from    NUMBER(20),
  external_uid           VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint PK_INSTR_OFFR primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint FK_INSTR_OFFR_CONSENT_TYPE foreign key (CONSENT_TYPE)
  references timetable.OFFR_CONSENT_TYPE (UNIQUEID) on delete cascade;
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint NN_INSTR_OFFR_DESIGNATOR_REQD
  check ("DESIGNATOR_REQUIRED" IS NOT NULL);
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint NN_INSTR_OFFR_NOT_OFFERED
  check ("NOT_OFFERED" IS NOT NULL);
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint NN_INSTR_OFFR_PERM_ID
  check ("INSTR_OFFERING_PERM_ID" IS NOT NULL);
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint NN_INSTR_OFFR_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.INSTRUCTIONAL_OFFERING
  add constraint NN_INSTR_OFFR_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_INSTR_OFFR_CONSENT on timetable.INSTRUCTIONAL_OFFERING (CONSENT_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table INSTR_OFFERING_CONFIG
prompt ====================================
prompt
create table timetable.INSTR_OFFERING_CONFIG
(
  uniqueid             NUMBER(20),
  config_limit         NUMBER(10),
  instr_offr_id        NUMBER(20),
  unlimited_enrollment NUMBER(1),
  name                 VARCHAR2(10),
  last_modified_time   TIMESTAMP(6),
  uid_rolled_fwd_from  NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint PK_INSTR_OFFR_CFG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint UK_INSTR_OFFR_CFG_NAME unique (UNIQUEID, NAME)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint FK_INSTR_OFFR_CFG_INSTR_OFFR foreign key (INSTR_OFFR_ID)
  references timetable.INSTRUCTIONAL_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint NN_INSTR_OFFR_CFG_INST_OFFR_ID
  check ("INSTR_OFFR_ID" IS NOT NULL);
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint NN_INSTR_OFFR_CFG_LIMIT
  check ("CONFIG_LIMIT" IS NOT NULL);
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint NN_INSTR_OFFR_CFG_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.INSTR_OFFERING_CONFIG
  add constraint NN_INSTR_OFFR_CFG_UNLIM_ENRL
  check ("UNLIMITED_ENROLLMENT" IS NOT NULL);
create index timetable.IDX_INSTR_OFFR_CFG_INSTR_OFFR on timetable.INSTR_OFFERING_CONFIG (INSTR_OFFR_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ITYPE_DESC
prompt =========================
prompt
create table timetable.ITYPE_DESC
(
  itype       NUMBER(2),
  abbv        VARCHAR2(7),
  description VARCHAR2(50),
  sis_ref     VARCHAR2(20),
  basic       NUMBER(1),
  parent      NUMBER(2),
  organized   NUMBER(1)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ITYPE_DESC
  add constraint PK_ITYPE_DESC primary key (ITYPE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ITYPE_DESC
  add constraint NN_ITYPE_DESC_ITYPE
  check ("ITYPE" IS NOT NULL);
alter table timetable.ITYPE_DESC
  add constraint NN_ITYPE_DESC_ORGANIZED
  check (organized is not null);

prompt
prompt Creating table SCHEDULING_SUBPART
prompt =================================
prompt
create table timetable.SCHEDULING_SUBPART
(
  uniqueid              NUMBER(20),
  min_per_wk            NUMBER(4),
  parent                NUMBER(20),
  config_id             NUMBER(20),
  itype                 NUMBER(2),
  date_pattern_id       NUMBER(20),
  auto_time_spread      NUMBER(1) default 1,
  subpart_suffix        VARCHAR2(5),
  student_allow_overlap NUMBER(1) default (0),
  last_modified_time    TIMESTAMP(6),
  uid_rolled_fwd_from   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SCHEDULING_SUBPART
  add constraint PK_SCHED_SUBPART primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SCHEDULING_SUBPART
  add constraint FK_SCHED_SUBPART_CONFIG foreign key (CONFIG_ID)
  references timetable.INSTR_OFFERING_CONFIG (UNIQUEID) on delete cascade;
alter table timetable.SCHEDULING_SUBPART
  add constraint FK_SCHED_SUBPART_DATE_PATTERN foreign key (DATE_PATTERN_ID)
  references timetable.DATE_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.SCHEDULING_SUBPART
  add constraint FK_SCHED_SUBPART_ITYPE foreign key (ITYPE)
  references timetable.ITYPE_DESC (ITYPE) on delete cascade;
alter table timetable.SCHEDULING_SUBPART
  add constraint FK_SCHED_SUBPART_PARENT foreign key (PARENT)
  references timetable.SCHEDULING_SUBPART (UNIQUEID) on delete cascade;
alter table timetable.SCHEDULING_SUBPART
  add constraint NN_SCHED_SUBPART_AUTO_TIME_SPR
  check ("AUTO_TIME_SPREAD" IS NOT NULL);
alter table timetable.SCHEDULING_SUBPART
  add constraint NN_SCHED_SUBPART_CONFIG_ID
  check ("CONFIG_ID" IS NOT NULL);
alter table timetable.SCHEDULING_SUBPART
  add constraint NN_SCHED_SUBPART_ITYPE
  check ("ITYPE" IS NOT NULL);
alter table timetable.SCHEDULING_SUBPART
  add constraint NN_SCHED_SUBPART_MIN_PER_WK
  check ("MIN_PER_WK" IS NOT NULL);
alter table timetable.SCHEDULING_SUBPART
  add constraint NN_SCHED_SUBPART_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.SCHEDULING_SUBPART
  add constraint NN_SUBPART_STAL_OVERLAP
  check ("STUDENT_ALLOW_OVERLAP" IS NOT NULL);
create index timetable.IDX_SCHED_SUBPART_CONFIG on timetable.SCHEDULING_SUBPART (CONFIG_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SCHED_SUBPART_DATE_PATTERN on timetable.SCHEDULING_SUBPART (DATE_PATTERN_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SCHED_SUBPART_ITYPE on timetable.SCHEDULING_SUBPART (ITYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SCHED_SUBPART_PARENT on timetable.SCHEDULING_SUBPART (PARENT)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table CLASS_
prompt =====================
prompt
create table timetable.CLASS_
(
  uniqueid              NUMBER(20),
  subpart_id            NUMBER(20),
  expected_capacity     NUMBER(4),
  nbr_rooms             NUMBER(4),
  parent_class_id       NUMBER(20),
  owner_id              NUMBER(20),
  room_capacity         NUMBER(4),
  notes                 VARCHAR2(1000),
  date_pattern_id       NUMBER(20),
  managing_dept         NUMBER(20),
  display_instructor    NUMBER(1),
  sched_print_note      VARCHAR2(2000),
  class_suffix          VARCHAR2(10),
  display_in_sched_book NUMBER(1) default 1,
  max_expected_capacity NUMBER(4),
  room_ratio            FLOAT default 1.0,
  section_number        NUMBER(5),
  last_modified_time    TIMESTAMP(6),
  uid_rolled_fwd_from   NUMBER(20),
  external_uid          VARCHAR2(40),
  enrollment            NUMBER(4)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CLASS_
  add constraint PK_CLASS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CLASS_
  add constraint FK_CLASS_DATEPATT foreign key (DATE_PATTERN_ID)
  references timetable.DATE_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.CLASS_
  add constraint FK_CLASS_PARENT foreign key (PARENT_CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.CLASS_
  add constraint FK_CLASS_SCHEDULING_SUBPART foreign key (SUBPART_ID)
  references timetable.SCHEDULING_SUBPART (UNIQUEID) on delete cascade;
alter table timetable.CLASS_
  add constraint NN_CLASS_DISPLAY_INSTRUCTOR
  check ("DISPLAY_INSTRUCTOR" IS NOT NULL);
alter table timetable.CLASS_
  add constraint NN_CLASS_DISPLAY_IN_SCHED_BOOK
  check ("DISPLAY_IN_SCHED_BOOK" IS NOT NULL);
alter table timetable.CLASS_
  add constraint NN_CLASS_EXPECTED_CAPACITY
  check ("EXPECTED_CAPACITY" IS NOT NULL);
alter table timetable.CLASS_
  add constraint NN_CLASS_MAX_EXPECTED_CAPACITY
  check ("MAX_EXPECTED_CAPACITY" IS NOT NULL);
alter table timetable.CLASS_
  add constraint NN_CLASS_ROOM_RATIO
  check ("ROOM_RATIO" IS NOT NULL);
alter table timetable.CLASS_
  add constraint NN_CLASS_SUBPART_ID
  check ("SUBPART_ID" IS NOT NULL);
alter table timetable.CLASS_
  add constraint NN_CLASS_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_CLASS_DATEPATT on timetable.CLASS_ (DATE_PATTERN_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CLASS_MANAGING_DEPT on timetable.CLASS_ (MANAGING_DEPT)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CLASS_PARENT on timetable.CLASS_ (PARENT_CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CLASS_SUBPART_ID on timetable.CLASS_ (SUBPART_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SOLVER_GROUP
prompt ===========================
prompt
create table timetable.SOLVER_GROUP
(
  uniqueid   NUMBER(20),
  name       VARCHAR2(50),
  abbv       VARCHAR2(50),
  session_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_GROUP
  add constraint PK_SOLVER_GROUP primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_GROUP
  add constraint FK_SOLVER_GROUP_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_GROUP
  add constraint NN_SOLVER_GROUP_ABBV
  check ("ABBV" IS NOT NULL);
alter table timetable.SOLVER_GROUP
  add constraint NN_SOLVER_GROUP_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.SOLVER_GROUP
  add constraint NN_SOLVER_GROUP_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.SOLVER_GROUP
  add constraint NN_SOLVER_GROUP_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_SOLVER_GROUP_SESSION on timetable.SOLVER_GROUP (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SOLUTION
prompt =======================
prompt
create table timetable.SOLUTION
(
  uniqueid           NUMBER(20),
  created            DATE,
  valid              NUMBER(1),
  commited           NUMBER(1),
  commit_date        DATE,
  note               VARCHAR2(1000),
  creator            VARCHAR2(250),
  owner_id           NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLUTION
  add constraint PK_SOLUTION primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLUTION
  add constraint FK_SOLUTION_OWNER foreign key (OWNER_ID)
  references timetable.SOLVER_GROUP (UNIQUEID) on delete cascade;
alter table timetable.SOLUTION
  add constraint NN_SOLUTION_COMMITED
  check ("COMMITED" IS NOT NULL);
alter table timetable.SOLUTION
  add constraint NN_SOLUTION_CREATED
  check ("CREATED" IS NOT NULL);
alter table timetable.SOLUTION
  add constraint NN_SOLUTION_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.SOLUTION
  add constraint NN_SOLUTION_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.SOLUTION
  add constraint NN_SOLUTION_VALID
  check ("VALID" IS NOT NULL);
create index timetable.IDX_SOLUTION_OWNER on timetable.SOLUTION (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TIME_PATTERN
prompt ===========================
prompt
create table timetable.TIME_PATTERN
(
  uniqueid   NUMBER(20),
  name       VARCHAR2(50),
  mins_pmt   NUMBER(10),
  slots_pmt  NUMBER(10),
  nr_mtgs    NUMBER(10),
  visible    NUMBER(1),
  type       NUMBER(10),
  break_time NUMBER(3),
  session_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN
  add constraint PK_TIME_PATTERN primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN
  add constraint FK_TIME_PATTERN_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.TIME_PATTERN
  add constraint NN_TIME_PATTERN_SESSION
  check (SESSION_ID IS NOT NULL);
alter table timetable.TIME_PATTERN
  add constraint NN_TIME_PATTERN_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_TIME_PATTERN_SESSION on timetable.TIME_PATTERN (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ASSIGNMENT
prompt =========================
prompt
create table timetable.ASSIGNMENT
(
  uniqueid           NUMBER(20),
  days               NUMBER(10),
  slot               NUMBER(10),
  time_pattern_id    NUMBER(20),
  solution_id        NUMBER(20),
  class_id           NUMBER(20),
  class_name         VARCHAR2(100),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ASSIGNMENT
  add constraint PK_ASSIGNMENT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ASSIGNMENT
  add constraint FK_ASSIGNMENT_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.ASSIGNMENT
  add constraint FK_ASSIGNMENT_SOLUTION foreign key (SOLUTION_ID)
  references timetable.SOLUTION (UNIQUEID) on delete cascade;
alter table timetable.ASSIGNMENT
  add constraint FK_ASSIGNMENT_TIME_PATTERN foreign key (TIME_PATTERN_ID)
  references timetable.TIME_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.ASSIGNMENT
  add constraint NN_ASSIGNMENT_CLASS_ID
  check ("CLASS_ID" IS NOT NULL);
alter table timetable.ASSIGNMENT
  add constraint NN_ASSIGNMENT_CLASS_NAME
  check ("CLASS_NAME" IS NOT NULL);
alter table timetable.ASSIGNMENT
  add constraint NN_ASSIGNMENT_SOLUTION_ID
  check ("SOLUTION_ID" IS NOT NULL);
alter table timetable.ASSIGNMENT
  add constraint NN_ASSIGNMENT_TIME_PATTERN_ID
  check ("TIME_PATTERN_ID" IS NOT NULL);
alter table timetable.ASSIGNMENT
  add constraint NN_ASSIGNMENT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ASSIGNMENT_CLASS on timetable.ASSIGNMENT (CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ASSIGNMENT_SOLUTION_INDEX on timetable.ASSIGNMENT (SOLUTION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ASSIGNMENT_TIME_PATTERN on timetable.ASSIGNMENT (TIME_PATTERN_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table POSITION_TYPE
prompt ============================
prompt
create table timetable.POSITION_TYPE
(
  uniqueid   NUMBER(20),
  reference  VARCHAR2(20),
  label      VARCHAR2(60),
  sort_order NUMBER(4)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POSITION_TYPE
  add constraint PK_POSITION_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POSITION_TYPE
  add constraint UK_POSITION_TYPE_LABEL unique (LABEL)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POSITION_TYPE
  add constraint UK_POSITION_TYPE_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POSITION_TYPE
  add constraint NN_POSITION_TYPE_LABEL
  check ("LABEL" IS NOT NULL);
alter table timetable.POSITION_TYPE
  add constraint NN_POSITION_TYPE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.POSITION_TYPE
  add constraint NN_POSITION_TYPE_SORT_ORDER
  check (sort_order is not null);
alter table timetable.POSITION_TYPE
  add constraint NN_POSITION_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table DEPARTMENT
prompt =========================
prompt
create table timetable.DEPARTMENT
(
  uniqueid           NUMBER(20),
  session_id         NUMBER(20),
  abbreviation       VARCHAR2(20),
  name               VARCHAR2(100),
  dept_code          VARCHAR2(50),
  external_uid       VARCHAR2(40),
  rs_color           VARCHAR2(6),
  external_manager   NUMBER(1),
  external_mgr_label VARCHAR2(30),
  external_mgr_abbv  VARCHAR2(10),
  solver_group_id    NUMBER(20),
  status_type        NUMBER(20),
  dist_priority      NUMBER(10) default (0),
  allow_req_time     NUMBER(1) default (0),
  allow_req_room     NUMBER(1) default (0),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPARTMENT
  add constraint PK_DEPARTMENT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPARTMENT
  add constraint UK_DEPARTMENT_DEPT_CODE unique (SESSION_ID, DEPT_CODE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPARTMENT
  add constraint FK_DEPARTMENT_SOLVER_GROUP foreign key (SOLVER_GROUP_ID)
  references timetable.SOLVER_GROUP (UNIQUEID) on delete cascade;
alter table timetable.DEPARTMENT
  add constraint FK_DEPARTMENT_STATUS_TYPE foreign key (STATUS_TYPE)
  references timetable.DEPT_STATUS_TYPE (UNIQUEID) on delete cascade;
alter table timetable.DEPARTMENT
  add constraint NN_DEPARTMENT_DEPT_CODE
  check ("DEPT_CODE" IS NOT NULL);
alter table timetable.DEPARTMENT
  add constraint NN_DEPARTMENT_DIST_PRIORITY
  check ("DIST_PRIORITY" IS NOT NULL);
alter table timetable.DEPARTMENT
  add constraint NN_DEPARTMENT_EXTERNAL_MANAGER
  check ("EXTERNAL_MANAGER" IS NOT NULL);
alter table timetable.DEPARTMENT
  add constraint NN_DEPARTMENT_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.DEPARTMENT
  add constraint NN_DEPARTMENT_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.DEPARTMENT
  add constraint NN_DEPARTMENT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_DEPARTMENT_SOLVER_GRP on timetable.DEPARTMENT (SOLVER_GROUP_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_DEPARTMENT_STATUS_TYPE on timetable.DEPARTMENT (STATUS_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table DEPARTMENTAL_INSTRUCTOR
prompt ======================================
prompt
create table timetable.DEPARTMENTAL_INSTRUCTOR
(
  uniqueid            NUMBER(20),
  external_uid        VARCHAR2(40),
  career_acct         VARCHAR2(20),
  lname               VARCHAR2(100),
  fname               VARCHAR2(100),
  mname               VARCHAR2(100),
  pos_code_type       NUMBER(20),
  note                VARCHAR2(20),
  department_uniqueid NUMBER(20),
  ignore_too_far      NUMBER(1) default 0,
  last_modified_time  TIMESTAMP(6),
  email               VARCHAR2(200)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPARTMENTAL_INSTRUCTOR
  add constraint PK_DEPT_INSTR primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPARTMENTAL_INSTRUCTOR
  add constraint FK_DEPT_INSTR_DEPT foreign key (DEPARTMENT_UNIQUEID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.DEPARTMENTAL_INSTRUCTOR
  add constraint FK_DEPT_INSTR_POS_CODE_TYPE foreign key (POS_CODE_TYPE)
  references timetable.POSITION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.DEPARTMENTAL_INSTRUCTOR
  add constraint NN_DEPT_INSTR_DEPT_UNIQUEID
  check ("DEPARTMENT_UNIQUEID" IS NOT NULL);
alter table timetable.DEPARTMENTAL_INSTRUCTOR
  add constraint NN_DEPT_INSTR_LNAME
  check ("LNAME" IS NOT NULL);
alter table timetable.DEPARTMENTAL_INSTRUCTOR
  add constraint NN_DEPT_INSTR_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_DEPT_INSTR_DEPT on timetable.DEPARTMENTAL_INSTRUCTOR (DEPARTMENT_UNIQUEID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_DEPT_INSTR_POSITION_TYPE on timetable.DEPARTMENTAL_INSTRUCTOR (POS_CODE_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ASSIGNED_INSTRUCTORS
prompt ===================================
prompt
create table timetable.ASSIGNED_INSTRUCTORS
(
  assignment_id      NUMBER(20),
  instructor_id      NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ASSIGNED_INSTRUCTORS
  add constraint PK_ASSIGNED_INSTRUCTORS primary key (ASSIGNMENT_ID, INSTRUCTOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ASSIGNED_INSTRUCTORS
  add constraint FK_ASSIGNED_INSTRS_ASSIGNMENT foreign key (ASSIGNMENT_ID)
  references timetable.ASSIGNMENT (UNIQUEID) on delete cascade;
alter table timetable.ASSIGNED_INSTRUCTORS
  add constraint FK_ASSIGNED_INSTRS_INSTRUCTOR foreign key (INSTRUCTOR_ID)
  references timetable.DEPARTMENTAL_INSTRUCTOR (UNIQUEID) on delete cascade;
alter table timetable.ASSIGNED_INSTRUCTORS
  add constraint NN_ASSIGNED_INSTRS_ASSGN_ID
  check ("ASSIGNMENT_ID" IS NOT NULL);
alter table timetable.ASSIGNED_INSTRUCTORS
  add constraint NN_ASSIGNED_INSTRS_INSTR_ID
  check ("INSTRUCTOR_ID" IS NOT NULL);
create index timetable.IDX_ASSIGNED_INSTRUCTORS on timetable.ASSIGNED_INSTRUCTORS (ASSIGNMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ASSIGNED_ROOMS
prompt =============================
prompt
create table timetable.ASSIGNED_ROOMS
(
  assignment_id      NUMBER(20),
  room_id            NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ASSIGNED_ROOMS
  add constraint PK_ASSIGNED_ROOMS primary key (ASSIGNMENT_ID, ROOM_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ASSIGNED_ROOMS
  add constraint FK_ASSIGNED_ROOMS_ASSIGNMENT foreign key (ASSIGNMENT_ID)
  references timetable.ASSIGNMENT (UNIQUEID) on delete cascade;
alter table timetable.ASSIGNED_ROOMS
  add constraint NN_ASSIGNED_ROOMS_ASSIGN_ID
  check ("ASSIGNMENT_ID" IS NOT NULL);
alter table timetable.ASSIGNED_ROOMS
  add constraint NN_ASSIGNED_ROOMS_ROOM_ID
  check ("ROOM_ID" IS NOT NULL);
create index timetable.IDX_ASSIGNED_ROOMS on timetable.ASSIGNED_ROOMS (ASSIGNMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table BUILDING
prompt =======================
prompt
create table timetable.BUILDING
(
  uniqueid     NUMBER(20),
  session_id   NUMBER(20),
  abbreviation VARCHAR2(10),
  name         VARCHAR2(100),
  coordinate_x FLOAT,
  coordinate_y FLOAT,
  external_uid VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.BUILDING
  add constraint PK_BUILDING primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.BUILDING
  add constraint UK_BUILDING unique (SESSION_ID, ABBREVIATION)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.BUILDING
  add constraint FK_BUILDING_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.BUILDING
  add constraint NN_BUILDING_ABBREVIATION
  check ("ABBREVIATION" IS NOT NULL);
alter table timetable.BUILDING
  add constraint NN_BUILDING_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.BUILDING
  add constraint NN_BUILDING_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.BUILDING
  add constraint NN_BUILDING_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table PREFERENCE_LEVEL
prompt ===============================
prompt
create table timetable.PREFERENCE_LEVEL
(
  pref_id     NUMBER(2),
  pref_prolog VARCHAR2(2),
  pref_name   VARCHAR2(20),
  uniqueid    NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.PREFERENCE_LEVEL
  add constraint PK_PREFERENCE_LEVEL primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.PREFERENCE_LEVEL
  add constraint UK_PREFERENCE_LEVEL_PREF_ID unique (PREF_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.PREFERENCE_LEVEL
  add constraint NN_PREFERENCE_LEVEL_PREF_ID
  check ("PREF_ID" IS NOT NULL);
alter table timetable.PREFERENCE_LEVEL
  add constraint NN_PREFERENCE_LEVEL_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table BUILDING_PREF
prompt ============================
prompt
create table timetable.BUILDING_PREF
(
  uniqueid           NUMBER(20),
  owner_id           NUMBER(20),
  pref_level_id      NUMBER(20),
  bldg_id            NUMBER(20),
  distance_from      NUMBER(5),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.BUILDING_PREF
  add constraint PK_BUILDING_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.BUILDING_PREF
  add constraint FK_BUILDING_PREF_BLDG foreign key (BLDG_ID)
  references timetable.BUILDING (UNIQUEID) on delete cascade;
alter table timetable.BUILDING_PREF
  add constraint FK_BUILDING_PREF_LEVEL foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.BUILDING_PREF
  add constraint NN_BUILDING_PREF_BLDG_ID
  check ("BLDG_ID" IS NOT NULL);
alter table timetable.BUILDING_PREF
  add constraint NN_BUILDING_PREF_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.BUILDING_PREF
  add constraint NN_BUILDING_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_BUILDING_PREF_BLDG on timetable.BUILDING_PREF (BLDG_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_BUILDING_PREF_LEVEL on timetable.BUILDING_PREF (PREF_LEVEL_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_BUILDING_PREF_OWNER on timetable.BUILDING_PREF (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SUBJECT_AREA
prompt ===========================
prompt
create table timetable.SUBJECT_AREA
(
  uniqueid                  NUMBER(20),
  session_id                NUMBER(20),
  subject_area_abbreviation VARCHAR2(10),
  short_title               VARCHAR2(50),
  long_title                VARCHAR2(100),
  schedule_book_only        VARCHAR2(1),
  pseudo_subject_area       VARCHAR2(1),
  department_uniqueid       NUMBER(20),
  external_uid              VARCHAR2(40),
  last_modified_time        TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SUBJECT_AREA
  add constraint PK_SUBJECT_AREA primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SUBJECT_AREA
  add constraint UK_SUBJECT_AREA unique (SESSION_ID, SUBJECT_AREA_ABBREVIATION)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SUBJECT_AREA
  add constraint FK_SUBJECT_AREA_DEPT foreign key (DEPARTMENT_UNIQUEID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_DEPARTMENT_UID
  check ("DEPARTMENT_UNIQUEID" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_LONG_TITLE
  check ("LONG_TITLE" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_PSEUDO_SUBAREA
  check ("PSEUDO_SUBJECT_AREA" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_SCHE_BOOK_ONLY
  check ("SCHEDULE_BOOK_ONLY" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_SHORT_TITLE
  check ("SHORT_TITLE" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_SUBJ_AREA_ABBR
  check ("SUBJECT_AREA_ABBREVIATION" IS NOT NULL);
alter table timetable.SUBJECT_AREA
  add constraint NN_SUBJECT_AREA_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_SUBJECT_AREA_DEPT on timetable.SUBJECT_AREA (DEPARTMENT_UNIQUEID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TIMETABLE_MANAGER
prompt ================================
prompt
create table timetable.TIMETABLE_MANAGER
(
  uniqueid           NUMBER(20),
  external_uid       VARCHAR2(40),
  first_name         VARCHAR2(100),
  middle_name        VARCHAR2(100),
  last_name          VARCHAR2(100),
  email_address      VARCHAR2(200),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIMETABLE_MANAGER
  add constraint PK_TIMETABLE_MANAGER primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIMETABLE_MANAGER
  add constraint UK_TIMETABLE_MANAGER_PUID unique (EXTERNAL_UID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIMETABLE_MANAGER
  add constraint NN_TIMETABLE_MANAGER_FIRST_NAM
  check ("FIRST_NAME" IS NOT NULL);
alter table timetable.TIMETABLE_MANAGER
  add constraint NN_TIMETABLE_MANAGER_LAST_NAME
  check ("LAST_NAME" IS NOT NULL);
alter table timetable.TIMETABLE_MANAGER
  add constraint NN_TIMETABLE_MANAGER_PUID
  check ("EXTERNAL_UID" IS NOT NULL);
alter table timetable.TIMETABLE_MANAGER
  add constraint NN_TIMETABLE_MANAGER_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table CHANGE_LOG
prompt =========================
prompt
create table timetable.CHANGE_LOG
(
  uniqueid      NUMBER(20),
  session_id    NUMBER(20),
  manager_id    NUMBER(20),
  time_stamp    TIMESTAMP(9),
  obj_type      VARCHAR2(255),
  obj_uid       NUMBER(20),
  obj_title     VARCHAR2(255),
  subj_area_id  NUMBER(20),
  department_id NUMBER(20),
  source        VARCHAR2(50),
  operation     VARCHAR2(50),
  detail        BLOB
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CHANGE_LOG
  add constraint PK_CHANGE_LOG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CHANGE_LOG
  add constraint FK_CHANGE_LOG_DEPARTMENT foreign key (DEPARTMENT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.CHANGE_LOG
  add constraint FK_CHANGE_LOG_MANAGER foreign key (MANAGER_ID)
  references timetable.TIMETABLE_MANAGER (UNIQUEID) on delete cascade;
alter table timetable.CHANGE_LOG
  add constraint FK_CHANGE_LOG_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.CHANGE_LOG
  add constraint FK_CHANGE_LOG_SUBJAREA foreign key (SUBJ_AREA_ID)
  references timetable.SUBJECT_AREA (UNIQUEID) on delete cascade;
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_MANAGER
  check ("MANAGER_ID" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_OBJTITLE
  check ("OBJ_TITLE" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_OBJTYPE
  check ("OBJ_TYPE" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_OBJUID
  check ("OBJ_UID" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_OP
  check ("OPERATION" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_SRC
  check ("SOURCE" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_TS
  check ("TIME_STAMP" IS NOT NULL);
alter table timetable.CHANGE_LOG
  add constraint NN_CHANGE_LOG_UID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_CHANGE_LOG_DEPARTMENT on timetable.CHANGE_LOG (DEPARTMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CHANGE_LOG_OBJECT on timetable.CHANGE_LOG (OBJ_TYPE, OBJ_UID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CHANGE_LOG_SESSIONMGR on timetable.CHANGE_LOG (SESSION_ID, MANAGER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CHANGE_LOG_SUBJAREA on timetable.CHANGE_LOG (SUBJ_AREA_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table CLASS_INSTRUCTOR
prompt ===============================
prompt
create table timetable.CLASS_INSTRUCTOR
(
  uniqueid           NUMBER(20),
  class_id           NUMBER(20),
  instructor_id      NUMBER(20),
  percent_share      NUMBER(3),
  is_lead            NUMBER(1),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CLASS_INSTRUCTOR
  add constraint PK_CLASS_INSTRUCTOR_UNIQUEID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CLASS_INSTRUCTOR
  add constraint FK_CLASS_INSTRUCTOR_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.CLASS_INSTRUCTOR
  add constraint FK_CLASS_INSTRUCTOR_INSTR foreign key (INSTRUCTOR_ID)
  references timetable.DEPARTMENTAL_INSTRUCTOR (UNIQUEID) on delete cascade;
alter table timetable.CLASS_INSTRUCTOR
  add constraint NN_CLASS_INSTRUCTOR_CLASS_ID
  check ("CLASS_ID" IS NOT NULL);
alter table timetable.CLASS_INSTRUCTOR
  add constraint NN_CLASS_INSTRUCTOR_INSTR_ID
  check ("INSTRUCTOR_ID" IS NOT NULL);
alter table timetable.CLASS_INSTRUCTOR
  add constraint NN_CLASS_INSTRUCTOR_IS_LEAD
  check ("IS_LEAD" IS NOT NULL);
alter table timetable.CLASS_INSTRUCTOR
  add constraint NN_CLASS_INSTRUCTOR_PERC_SHARE
  check ("PERCENT_SHARE" IS NOT NULL);
alter table timetable.CLASS_INSTRUCTOR
  add constraint NN_CLASS_INSTRUCTOR_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_CLASS_INSTRUCTOR_CLASS on timetable.CLASS_INSTRUCTOR (CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CLASS_INSTRUCTOR_INSTR on timetable.CLASS_INSTRUCTOR (INSTRUCTOR_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_STATUS_TYPE
prompt ==================================
prompt
create table timetable.STUDENT_STATUS_TYPE
(
  uniqueid     NUMBER(20),
  abbreviation VARCHAR2(20),
  name         VARCHAR2(50)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_STATUS_TYPE
  add constraint PK_STUDENT_STATUS_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_STATUS_TYPE
  add constraint NN_STUDENT_STATUS_TYPE_ABBV
  check ("ABBREVIATION" IS NOT NULL);
alter table timetable.STUDENT_STATUS_TYPE
  add constraint NN_STUDENT_STATUS_TYPE_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.STUDENT_STATUS_TYPE
  add constraint NN_STUDENT_STATUS_TYPE_UID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table STUDENT
prompt ======================
prompt
create table timetable.STUDENT
(
  uniqueid            NUMBER(20),
  external_uid        VARCHAR2(40),
  first_name          VARCHAR2(100),
  middle_name         VARCHAR2(100),
  last_name           VARCHAR2(100),
  email               VARCHAR2(200),
  free_time_cat       NUMBER(10) default (0),
  schedule_preference NUMBER(10) default (0),
  status_type_id      NUMBER(20),
  status_change_date  DATE,
  session_id          NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT
  add constraint PK_STUDENT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT
  add constraint FK_STUDENT_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.STUDENT
  add constraint FK_STUDENT_STATUS_STUDENT foreign key (STATUS_TYPE_ID)
  references timetable.STUDENT_STATUS_TYPE (UNIQUEID) on delete cascade;
alter table timetable.STUDENT
  add constraint NN_STUDENT_FNAME
  check ("FIRST_NAME" IS NOT NULL);
alter table timetable.STUDENT
  add constraint NN_STUDENT_FT_CAT
  check ("FREE_TIME_CAT" IS NOT NULL);
alter table timetable.STUDENT
  add constraint NN_STUDENT_LNAME
  check ("LAST_NAME" IS NOT NULL);
alter table timetable.STUDENT
  add constraint NN_STUDENT_SCH_PREF
  check ("SCHEDULE_PREFERENCE" IS NOT NULL);
alter table timetable.STUDENT
  add constraint NN_STUDENT_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.STUDENT
  add constraint NN_STUDENT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STUDENT_SESSION on timetable.STUDENT (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table FREE_TIME
prompt ========================
prompt
create table timetable.FREE_TIME
(
  uniqueid   NUMBER(20),
  name       VARCHAR2(50),
  day_code   NUMBER(10),
  start_slot NUMBER(10),
  length     NUMBER(10),
  category   NUMBER(10),
  session_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.FREE_TIME
  add constraint PK_FREE_TIME primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.FREE_TIME
  add constraint FK_FREE_TIME_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_CATEGORY
  check ("CATEGORY" IS NOT NULL);
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_DAY_CODE
  check ("DAY_CODE" IS NOT NULL);
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_LENGTH
  check ("LENGTH" IS NOT NULL);
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_START_SLOT
  check ("START_SLOT" IS NOT NULL);
alter table timetable.FREE_TIME
  add constraint NN_FREE_TIME_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table COURSE_DEMAND
prompt ============================
prompt
create table timetable.COURSE_DEMAND
(
  uniqueid       NUMBER(20),
  student_id     NUMBER(20),
  priority       NUMBER(10),
  waitlist       NUMBER(1),
  is_alternative NUMBER(1),
  timestamp      DATE,
  free_time_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_DEMAND
  add constraint PK_COURSE_DEMAND primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_DEMAND
  add constraint FK_COURSE_DEMAND_FREE_TIME foreign key (FREE_TIME_ID)
  references timetable.FREE_TIME (UNIQUEID) on delete cascade;
alter table timetable.COURSE_DEMAND
  add constraint FK_COURSE_DEMAND_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.COURSE_DEMAND
  add constraint NN_COURSE_DEMAND_IS_ALT
  check ("IS_ALTERNATIVE" IS NOT NULL);
alter table timetable.COURSE_DEMAND
  add constraint NN_COURSE_DEMAND_PRIORITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.COURSE_DEMAND
  add constraint NN_COURSE_DEMAND_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.COURSE_DEMAND
  add constraint NN_COURSE_DEMAND_TIMESTAMP
  check ("TIMESTAMP" IS NOT NULL);
alter table timetable.COURSE_DEMAND
  add constraint NN_COURSE_DEMAND_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.COURSE_DEMAND
  add constraint NN_COURSE_DEMAND_WAITLIST
  check ("WAITLIST" IS NOT NULL);
create index timetable.IDX_COURSE_DEMAND_FREE_TIME on timetable.COURSE_DEMAND (FREE_TIME_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_DEMAND_STUDENT on timetable.COURSE_DEMAND (STUDENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_OFFERING
prompt ==============================
prompt
create table timetable.COURSE_OFFERING
(
  uniqueid             NUMBER(20),
  course_nbr           VARCHAR2(10),
  is_control           NUMBER(1),
  perm_id              VARCHAR2(20),
  proj_demand          NUMBER(10),
  instr_offr_id        NUMBER(20),
  subject_area_id      NUMBER(20),
  title                VARCHAR2(90),
  schedule_book_note   VARCHAR2(1000),
  demand_offering_id   NUMBER(20),
  demand_offering_type NUMBER(20),
  nbr_expected_stdents NUMBER(10) default (0),
  external_uid         VARCHAR2(40),
  last_modified_time   TIMESTAMP(6),
  uid_rolled_fwd_from  NUMBER(20),
  lastlike_demand      NUMBER(10) default 0,
  enrollment           NUMBER(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_OFFERING
  add constraint PK_COURSE_OFFERING primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_OFFERING
  add constraint UK_COURSE_OFFERING_SUBJ_CRS unique (COURSE_NBR, SUBJECT_AREA_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_OFFERING
  add constraint FK_COURSE_OFFERING_DEMAND_OFFR foreign key (DEMAND_OFFERING_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.COURSE_OFFERING
  add constraint FK_COURSE_OFFERING_INSTR_OFFR foreign key (INSTR_OFFR_ID)
  references timetable.INSTRUCTIONAL_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.COURSE_OFFERING
  add constraint FK_COURSE_OFFERING_SUBJ_AREA foreign key (SUBJECT_AREA_ID)
  references timetable.SUBJECT_AREA (UNIQUEID) on delete cascade;
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_COURSE_NBR
  check ("COURSE_NBR" IS NOT NULL);
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_EXPST
  check ("NBR_EXPECTED_STDENTS" IS NOT NULL);
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_INSTROFFRID
  check ("INSTR_OFFR_ID" IS NOT NULL);
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_IS_CONTROL
  check ("IS_CONTROL" IS NOT NULL);
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_LL_DEMAND
  check ("LASTLIKE_DEMAND" IS NOT NULL);
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_SUBJAREA_ID
  check ("SUBJECT_AREA_ID" IS NOT NULL);
alter table timetable.COURSE_OFFERING
  add constraint NN_COURSE_OFFERING_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_COURSE_OFFERING_CONTROL on timetable.COURSE_OFFERING (IS_CONTROL)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_OFFERING_DEMD_OFFR on timetable.COURSE_OFFERING (DEMAND_OFFERING_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_OFFERING_INSTR_OFFR on timetable.COURSE_OFFERING (INSTR_OFFR_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_REQUEST
prompt =============================
prompt
create table timetable.COURSE_REQUEST
(
  uniqueid           NUMBER(20),
  course_demand_id   NUMBER(20),
  course_offering_id NUMBER(20),
  ord                NUMBER(10),
  allow_overlap      NUMBER(1),
  credit             NUMBER(10) default (0)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_REQUEST
  add constraint PK_COURSE_REQUEST primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_REQUEST
  add constraint FK_COURSE_REQUEST_DEMAND foreign key (COURSE_DEMAND_ID)
  references timetable.COURSE_DEMAND (UNIQUEID) on delete cascade;
alter table timetable.COURSE_REQUEST
  add constraint FK_COURSE_REQUEST_OFFERING foreign key (COURSE_OFFERING_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.COURSE_REQUEST
  add constraint NN_COURSE_REQUEST_CREDIT
  check ("CREDIT" IS NOT NULL);
alter table timetable.COURSE_REQUEST
  add constraint NN_COURSE_REQUEST_DEMAND
  check ("COURSE_DEMAND_ID" IS NOT NULL);
alter table timetable.COURSE_REQUEST
  add constraint NN_COURSE_REQUEST_OFFERING
  check ("COURSE_OFFERING_ID" IS NOT NULL);
alter table timetable.COURSE_REQUEST
  add constraint NN_COURSE_REQUEST_ORDER
  check ("ORD" IS NOT NULL);
alter table timetable.COURSE_REQUEST
  add constraint NN_COURSE_REQUEST_OVERLAP
  check ("ALLOW_OVERLAP" IS NOT NULL);
alter table timetable.COURSE_REQUEST
  add constraint NN_COURSE_REQUEST_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_COURSE_REQUEST_DEMAND on timetable.COURSE_REQUEST (COURSE_DEMAND_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_REQUEST_OFFERING on timetable.COURSE_REQUEST (COURSE_OFFERING_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table CLASS_WAITLIST
prompt =============================
prompt
create table timetable.CLASS_WAITLIST
(
  uniqueid          NUMBER(20),
  student_id        NUMBER(20),
  course_request_id NUMBER(20),
  class_id          NUMBER(20),
  type              NUMBER(10) default (0),
  timestamp         DATE
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CLASS_WAITLIST
  add constraint PK_CLASS_WAITLIST primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CLASS_WAITLIST
  add constraint FK_CLASS_WAITLIST_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.CLASS_WAITLIST
  add constraint FK_CLASS_WAITLIST_REQUEST foreign key (COURSE_REQUEST_ID)
  references timetable.COURSE_REQUEST (UNIQUEID) on delete cascade;
alter table timetable.CLASS_WAITLIST
  add constraint FK_CLASS_WAITLIST_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.CLASS_WAITLIST
  add constraint NN_CLASS_WAITLIST_CLASS
  check ("CLASS_ID" IS NOT NULL);
alter table timetable.CLASS_WAITLIST
  add constraint NN_CLASS_WAITLIST_COURSE
  check ("COURSE_REQUEST_ID" IS NOT NULL);
alter table timetable.CLASS_WAITLIST
  add constraint NN_CLASS_WAITLIST_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.CLASS_WAITLIST
  add constraint NN_CLASS_WAITLIST_TIMESTMP
  check ("TIMESTAMP" IS NOT NULL);
alter table timetable.CLASS_WAITLIST
  add constraint NN_CLASS_WAITLIST_TYPE
  check ("TYPE" IS NOT NULL);
alter table timetable.CLASS_WAITLIST
  add constraint NN_CLASS_WAITLIST_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_CLASS_WAITLIST_CLASS on timetable.CLASS_WAITLIST (CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CLASS_WAITLIST_REQ on timetable.CLASS_WAITLIST (COURSE_REQUEST_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CLASS_WAITLIST_STUDENT on timetable.CLASS_WAITLIST (STUDENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SOLVER_INFO_DEF
prompt ==============================
prompt
create table timetable.SOLVER_INFO_DEF
(
  uniqueid       NUMBER(20),
  name           VARCHAR2(100),
  description    VARCHAR2(1000),
  implementation VARCHAR2(250)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_INFO_DEF
  add constraint PK_SOLVER_INFO_DEF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_INFO_DEF
  add constraint NN_SOLVER_INFO_DEF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table SOLVER_INFO
prompt ==========================
prompt
create table timetable.SOLVER_INFO
(
  uniqueid           NUMBER(20),
  type               NUMBER(10),
  value              BLOB,
  opt                VARCHAR2(250),
  solver_info_def_id NUMBER(20),
  solution_id        NUMBER(20),
  assignment_id      NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 256K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_INFO
  add constraint PK_SOLVER_INFO primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_INFO
  add constraint FK_SOLVER_INFO_ASSIGNMENT foreign key (ASSIGNMENT_ID)
  references timetable.ASSIGNMENT (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_INFO
  add constraint FK_SOLVER_INFO_DEF foreign key (SOLVER_INFO_DEF_ID)
  references timetable.SOLVER_INFO_DEF (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_INFO
  add constraint FK_SOLVER_INFO_SOLUTION foreign key (SOLUTION_ID)
  references timetable.SOLUTION (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_INFO
  add constraint NN_SOLVER_INFO_TYPE
  check ("TYPE" IS NOT NULL);
alter table timetable.SOLVER_INFO
  add constraint NN_SOLVER_INFO_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.SOLVER_INFO
  add constraint NN_SOLVER_INFO_VALUE
  check ("VALUE" IS NOT NULL);
create index timetable.IDX_SOLVER_INFO on timetable.SOLVER_INFO (ASSIGNMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SOLVER_INFO_SOLUTION on timetable.SOLVER_INFO (SOLUTION_ID, SOLVER_INFO_DEF_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table CONSTRAINT_INFO
prompt ==============================
prompt
create table timetable.CONSTRAINT_INFO
(
  assignment_id  NUMBER(20),
  solver_info_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CONSTRAINT_INFO
  add constraint UK_CONSTRAINT_INFO_SOLV_ASSGN primary key (SOLVER_INFO_ID, ASSIGNMENT_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CONSTRAINT_INFO
  add constraint FK_CONSTRAINT_INFO_ASSIGNMENT foreign key (ASSIGNMENT_ID)
  references timetable.ASSIGNMENT (UNIQUEID) on delete cascade;
alter table timetable.CONSTRAINT_INFO
  add constraint FK_CONSTRAINT_INFO_SOLVER foreign key (SOLVER_INFO_ID)
  references timetable.SOLVER_INFO (UNIQUEID) on delete cascade;
alter table timetable.CONSTRAINT_INFO
  add constraint NN_CONSTRAINT_INFO_ASSIGN_ID
  check ("ASSIGNMENT_ID" IS NOT NULL);
alter table timetable.CONSTRAINT_INFO
  add constraint NN_CONSTRAINT_INFO_SOL_INFO_ID
  check ("SOLVER_INFO_ID" IS NOT NULL);
create index timetable.IDX_CONSTRAINT_INFO on timetable.CONSTRAINT_INFO (ASSIGNMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_CATALOG
prompt =============================
prompt
create table timetable.COURSE_CATALOG
(
  uniqueid            NUMBER(20) not null,
  session_id          NUMBER(20),
  external_uid        VARCHAR2(40),
  subject             VARCHAR2(10),
  course_nbr          VARCHAR2(10),
  title               VARCHAR2(100),
  perm_id             VARCHAR2(20),
  approval_type       VARCHAR2(20),
  designator_req      NUMBER(1),
  prev_subject        VARCHAR2(10),
  prev_crs_nbr        VARCHAR2(10),
  credit_type         VARCHAR2(20),
  credit_unit_type    VARCHAR2(20),
  credit_format       VARCHAR2(20),
  fixed_min_credit    FLOAT,
  max_credit          FLOAT,
  frac_credit_allowed NUMBER(1)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CATALOG
  add constraint PK_COURSE_CATALOG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_CRED
  check ("FIXED_MIN_CREDIT" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_CRED_FMT
  check ("CREDIT_FORMAT" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_CRED_TYPE
  check ("CREDIT_TYPE" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_CRED_UNIT
  check ("CREDIT_UNIT_TYPE" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_CRS_NBR
  check ("COURSE_NBR" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_DES_REQ
  check ("DESIGNATOR_REQ" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_SUBJ
  check ("SUBJECT" IS NOT NULL);
alter table timetable.COURSE_CATALOG
  add constraint NN_CRS_CATLOG_TITLE
  check ("TITLE" IS NOT NULL);
create index timetable.IDX_COURSE_CATALOG on timetable.COURSE_CATALOG (SESSION_ID, SUBJECT, COURSE_NBR)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_CREDIT_TYPE
prompt =================================
prompt
create table timetable.COURSE_CREDIT_TYPE
(
  uniqueid                NUMBER(20),
  reference               VARCHAR2(20),
  label                   VARCHAR2(60),
  abbreviation            VARCHAR2(10),
  legacy_crse_master_code VARCHAR2(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_TYPE
  add constraint PK_COURSE_CREDIT_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_TYPE
  add constraint UK_COURSE_CREDIT_TYPE_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_TYPE
  add constraint NN_COURSE_CREDIT_TYPE_REF
  check ("REFERENCE" IS NOT NULL);
alter table timetable.COURSE_CREDIT_TYPE
  add constraint NN_COURSE_CREDIT_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table COURSE_CREDIT_UNIT_CONFIG
prompt ========================================
prompt
create table timetable.COURSE_CREDIT_UNIT_CONFIG
(
  uniqueid                       NUMBER(20),
  credit_format                  VARCHAR2(20),
  owner_id                       NUMBER(20),
  credit_type                    NUMBER(20),
  credit_unit_type               NUMBER(20),
  defines_credit_at_course_level NUMBER(1),
  fixed_units                    FLOAT,
  min_units                      FLOAT,
  max_units                      FLOAT,
  fractional_incr_allowed        NUMBER(1),
  instr_offr_id                  NUMBER(20),
  last_modified_time             TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint PK_CRS_CRDT_UNIT_CFG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint FK_CRS_CRDT_UNIT_CFG_CRDT_TYPE foreign key (CREDIT_TYPE)
  references timetable.COURSE_CREDIT_TYPE (UNIQUEID) on delete cascade;
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint FK_CRS_CRDT_UNIT_CFG_IO_OWN foreign key (INSTR_OFFR_ID)
  references timetable.INSTRUCTIONAL_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint FK_CRS_CRDT_UNIT_CFG_OWNER foreign key (OWNER_ID)
  references timetable.SCHEDULING_SUBPART (UNIQUEID) on delete cascade;
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint NN_CRS_CRDT_UNIT_CFG_CRDT_FMT
  check ("CREDIT_FORMAT" IS NOT NULL);
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint NN_CRS_CRDT_UNIT_CFG_CRDT_TYPE
  check ("CREDIT_TYPE" IS NOT NULL);
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint NN_CRS_CRDT_UNIT_CFG_DEF_LEVEL
  check ("DEFINES_CREDIT_AT_COURSE_LEVEL" IS NOT NULL);
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint NN_CRS_CRDT_UNIT_CFG_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.COURSE_CREDIT_UNIT_CONFIG
  add constraint NN_CRS_CRDT_UNIT_CFG_UNIT_TYPE
  check ("CREDIT_UNIT_TYPE" IS NOT NULL);
create index timetable.IDX_CRS_CRDT_UNIT_CFG_CRD_TYPE on timetable.COURSE_CREDIT_UNIT_CONFIG (CREDIT_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CRS_CRDT_UNIT_CFG_IO_OWN on timetable.COURSE_CREDIT_UNIT_CONFIG (INSTR_OFFR_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_CRS_CRDT_UNIT_CFG_OWNER on timetable.COURSE_CREDIT_UNIT_CONFIG (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_CREDIT_UNIT_TYPE
prompt ======================================
prompt
create table timetable.COURSE_CREDIT_UNIT_TYPE
(
  uniqueid     NUMBER(20),
  reference    VARCHAR2(20),
  label        VARCHAR2(60),
  abbreviation VARCHAR2(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_UNIT_TYPE
  add constraint PK_CRS_CRDT_UNIT_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_UNIT_TYPE
  add constraint UK_CRS_CRDT_UNIT_TYPE_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_CREDIT_UNIT_TYPE
  add constraint NN_CRS_CRDT_UNIT_TYPE_REF
  check ("REFERENCE" IS NOT NULL);
alter table timetable.COURSE_CREDIT_UNIT_TYPE
  add constraint NN_CRS_CRDT_UNIT_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table COURSE_REQUEST_OPTION
prompt ====================================
prompt
create table timetable.COURSE_REQUEST_OPTION
(
  uniqueid          NUMBER(20),
  course_request_id NUMBER(20),
  option_type       NUMBER(10),
  value             BLOB
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_REQUEST_OPTION
  add constraint PK_COURSE_REQUEST_OPTION primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_REQUEST_OPTION
  add constraint FK_COURSE_REQUEST_OPTIONS_REQ foreign key (COURSE_REQUEST_ID)
  references timetable.COURSE_REQUEST (UNIQUEID) on delete cascade;
alter table timetable.COURSE_REQUEST_OPTION
  add constraint NN_COURSE_REQUEST_OPT_REQ
  check ("COURSE_REQUEST_ID" IS NOT NULL);
alter table timetable.COURSE_REQUEST_OPTION
  add constraint NN_COURSE_REQUEST_OPT_TYPE
  check ("OPTION_TYPE" IS NOT NULL);
alter table timetable.COURSE_REQUEST_OPTION
  add constraint NN_COURSE_REQUEST_OPT_UID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.COURSE_REQUEST_OPTION
  add constraint NN_COURSE_REQUEST_OPT_VALUE
  check ("VALUE" IS NOT NULL);
create index timetable.IDX_COURSE_REQUEST_OPTION_REQ on timetable.COURSE_REQUEST_OPTION (COURSE_REQUEST_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_RESERVATION
prompt =================================
prompt
create table timetable.COURSE_RESERVATION
(
  uniqueid             NUMBER(20),
  owner                NUMBER(20),
  reservation_type     NUMBER(20),
  course_offering      NUMBER(20),
  priority             NUMBER(5),
  reserved             NUMBER(10),
  prior_enrollment     NUMBER(10),
  projected_enrollment NUMBER(10),
  owner_class_id       VARCHAR2(1),
  requested            NUMBER(10),
  last_modified_time   TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_RESERVATION
  add constraint PK_COURSE_RESV primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_RESERVATION
  add constraint FK_COURSE_RESERV_TYPE foreign key (RESERVATION_TYPE)
  references timetable.RESERVATION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.COURSE_RESERVATION
  add constraint FK_COURSE_RESV_CRS_OFFR foreign key (COURSE_OFFERING)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_CRS_OFFR
  check ("COURSE_OFFERING" IS NOT NULL);
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_OWNER
  check ("OWNER" IS NOT NULL);
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_OWNER_CLASS_ID
  check ("OWNER_CLASS_ID" IS NOT NULL);
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_PRIORITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_RESERVED
  check ("RESERVED" IS NOT NULL);
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_RESERV_TYPE
  check ("RESERVATION_TYPE" IS NOT NULL);
alter table timetable.COURSE_RESERVATION
  add constraint NN_COURSE_RESV_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_COURSE_RESV_CRS_OFFR on timetable.COURSE_RESERVATION (COURSE_OFFERING)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_RESV_OWNER on timetable.COURSE_RESERVATION (OWNER)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_RESV_OWNER_CLS on timetable.COURSE_RESERVATION (OWNER_CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_COURSE_RESV_TYPE on timetable.COURSE_RESERVATION (RESERVATION_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table COURSE_SUBPART_CREDIT
prompt ====================================
prompt
create table timetable.COURSE_SUBPART_CREDIT
(
  uniqueid            NUMBER(20) not null,
  course_catalog_id   NUMBER(20),
  subpart_id          VARCHAR2(10),
  credit_type         VARCHAR2(20),
  credit_unit_type    VARCHAR2(20),
  credit_format       VARCHAR2(20),
  fixed_min_credit    FLOAT,
  max_credit          FLOAT,
  frac_credit_allowed NUMBER(1)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint PK_COURSE_SUBPART_CREDIT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint FK_SUBPART_CRED_CRS foreign key (COURSE_CATALOG_ID)
  references timetable.COURSE_CATALOG (UNIQUEID) on delete cascade;
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint NN_CRS_SUBPART_CRED
  check ("FIXED_MIN_CREDIT" IS NOT NULL);
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint NN_CRS_SUBPART_CRED_CRS_ID
  check ("COURSE_CATALOG_ID" IS NOT NULL);
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint NN_CRS_SUBPART_CRED_FMT
  check ("CREDIT_FORMAT" IS NOT NULL);
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint NN_CRS_SUBPART_CRED_SUB_ID
  check ("SUBPART_ID" IS NOT NULL);
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint NN_CRS_SUBPART_CRED_TYPE
  check ("CREDIT_TYPE" IS NOT NULL);
alter table timetable.COURSE_SUBPART_CREDIT
  add constraint NN_CRS_SUBPART_CRED_UNIT
  check ("CREDIT_UNIT_TYPE" IS NOT NULL);

prompt
prompt Creating table CRSE_CREDIT_FORMAT
prompt =================================
prompt
create table timetable.CRSE_CREDIT_FORMAT
(
  uniqueid     NUMBER(20),
  reference    VARCHAR2(20),
  label        VARCHAR2(60),
  abbreviation VARCHAR2(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CRSE_CREDIT_FORMAT
  add constraint PK_CRSE_CREDIT_FORMAT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CRSE_CREDIT_FORMAT
  add constraint UK_CRSE_CREDIT_FORMAT_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CRSE_CREDIT_FORMAT
  add constraint NN_CRSE_CREDIT_FORMAT_REF
  check ("REFERENCE" IS NOT NULL);
alter table timetable.CRSE_CREDIT_FORMAT
  add constraint NN_CRSE_CREDIT_FORMAT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table CURRICULUM
prompt =========================
prompt
create table timetable.CURRICULUM
(
  uniqueid     NUMBER(20),
  abbv         VARCHAR2(20),
  name         VARCHAR2(60),
  acad_area_id NUMBER(20),
  dept_id      NUMBER(20)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM
  add constraint PK_CURRICULUM primary key (UNIQUEID);
alter table timetable.CURRICULUM
  add constraint FK_CURRICULUM_ACAD_AREA foreign key (ACAD_AREA_ID)
  references timetable.ACADEMIC_AREA (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM
  add constraint FK_CURRICULUM_DEPT foreign key (DEPT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM
  add constraint NN_CURRICULUM_ABBV
  check ("ABBV" IS NOT NULL);
alter table timetable.CURRICULUM
  add constraint NN_CURRICULUM_ACAD_AREA
  check (acad_area_id is not null);
alter table timetable.CURRICULUM
  add constraint NN_CURRICULUM_DEPT
  check ("DEPT_ID" IS NOT NULL);
alter table timetable.CURRICULUM
  add constraint NN_CURRICULUM_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.CURRICULUM
  add constraint NN_CURRICULUM_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table CURRICULUM_CLASF
prompt ===============================
prompt
create table timetable.CURRICULUM_CLASF
(
  uniqueid      NUMBER(20),
  curriculum_id NUMBER(20),
  name          VARCHAR2(20),
  acad_clasf_id NUMBER(20),
  nr_students   NUMBER(10),
  ord           NUMBER(10),
  students      CLOB
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_CLASF
  add constraint PK_CURRICULUM_CLASF primary key (UNIQUEID);
alter table timetable.CURRICULUM_CLASF
  add constraint FK_CURRICULUM_CLASF_ACAD_CLASF foreign key (ACAD_CLASF_ID)
  references timetable.ACADEMIC_CLASSIFICATION (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_CLASF
  add constraint FK_CURRICULUM_CLASF_CURRICULUM foreign key (CURRICULUM_ID)
  references timetable.CURRICULUM (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_CLASF
  add constraint NN_CURRICULUM_CLASF_ACAD_CLASF
  check (acad_clasf_id is not null);
alter table timetable.CURRICULUM_CLASF
  add constraint NN_CURRICULUM_CLASF_CUR_ID
  check ("CURRICULUM_ID" IS NOT NULL);
alter table timetable.CURRICULUM_CLASF
  add constraint NN_CURRICULUM_CLASF_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.CURRICULUM_CLASF
  add constraint NN_CURRICULUM_CLASF_NRSTUDENTS
  check ("NR_STUDENTS" IS NOT NULL);
alter table timetable.CURRICULUM_CLASF
  add constraint NN_CURRICULUM_CLASF_ORD
  check ("ORD" IS NOT NULL);
alter table timetable.CURRICULUM_CLASF
  add constraint NN_CURRICULUM_CLASF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table CURRICULUM_COURSE
prompt ================================
prompt
create table timetable.CURRICULUM_COURSE
(
  uniqueid     NUMBER(20),
  course_id    NUMBER(20),
  cur_clasf_id NUMBER(20),
  pr_share     FLOAT,
  ord          NUMBER(10)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_COURSE
  add constraint PK_CURRICULUM_COURSE primary key (UNIQUEID);
alter table timetable.CURRICULUM_COURSE
  add constraint FK_CURRICULUM_COURSE_CLASF foreign key (CUR_CLASF_ID)
  references timetable.CURRICULUM_CLASF (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_COURSE
  add constraint FK_CURRICULUM_COURSE_COURSE foreign key (COURSE_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_COURSE
  add constraint NN_CURRICULUM_COURSE_COURSE_ID
  check ("COURSE_ID" IS NOT NULL);
alter table timetable.CURRICULUM_COURSE
  add constraint NN_CURRICULUM_COURSE_ORD
  check ("ORD" IS NOT NULL);
alter table timetable.CURRICULUM_COURSE
  add constraint NN_CURRICULUM_COURSE_PRSH
  check ("PR_SHARE" IS NOT NULL);
alter table timetable.CURRICULUM_COURSE
  add constraint NN_CURRICULUM_COURSE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.CURRICULUM_COURSE
  add constraint NN_CURRICULUM_CUR_CLASF_ID
  check ("CUR_CLASF_ID" IS NOT NULL);

prompt
prompt Creating table CURRICULUM_GROUP
prompt ===============================
prompt
create table timetable.CURRICULUM_GROUP
(
  uniqueid      NUMBER(20),
  name          VARCHAR2(20),
  color         VARCHAR2(20),
  type          NUMBER(10),
  curriculum_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_GROUP
  add constraint PK_CURRICULUM_GROUP primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_GROUP
  add constraint FK_CURRICULUM_GROUP_CURRICULUM foreign key (CURRICULUM_ID)
  references timetable.CURRICULUM (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_GROUP
  add constraint NN_CURRICULUM_GROUP_CURRICULUM
  check ("CURRICULUM_ID" IS NOT NULL);
alter table timetable.CURRICULUM_GROUP
  add constraint NN_CURRICULUM_GROUP_ID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.CURRICULUM_GROUP
  add constraint NN_CURRICULUM_GROUP_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.CURRICULUM_GROUP
  add constraint NN_CURRICULUM_GROUP_TYPE
  check ("TYPE" IS NOT NULL);

prompt
prompt Creating table CURRICULUM_COURSE_GROUP
prompt ======================================
prompt
create table timetable.CURRICULUM_COURSE_GROUP
(
  group_id      NUMBER(20),
  cur_course_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_COURSE_GROUP
  add constraint PK_CURRICULUM_COURSE_GROUPS primary key (GROUP_ID, CUR_COURSE_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_COURSE_GROUP
  add constraint FK_CUR_COURSE_GROUP_COURSE foreign key (CUR_COURSE_ID)
  references timetable.CURRICULUM_COURSE (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_COURSE_GROUP
  add constraint FK_CUR_COURSE_GROUP_GROUP foreign key (GROUP_ID)
  references timetable.CURRICULUM_GROUP (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_COURSE_GROUP
  add constraint NN_CURRICULUM_COURSE_ID
  check ("GROUP_ID" IS NOT NULL);
alter table timetable.CURRICULUM_COURSE_GROUP
  add constraint NN_CUR_COURSE_GROUPS_COURSE
  check ("CUR_COURSE_ID" IS NOT NULL);

prompt
prompt Creating table POS_MAJOR
prompt ========================
prompt
create table timetable.POS_MAJOR
(
  uniqueid     NUMBER(20),
  code         VARCHAR2(10),
  name         VARCHAR2(50),
  external_uid VARCHAR2(20),
  session_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_MAJOR
  add constraint PK_POS_MAJOR primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_MAJOR
  add constraint FK_POS_MAJOR_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.POS_MAJOR
  add constraint NN_POS_MAJOR_CODE
  check ("CODE" IS NOT NULL);
alter table timetable.POS_MAJOR
  add constraint NN_POS_MAJOR_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.POS_MAJOR
  add constraint NN_POS_MAJOR_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.POS_MAJOR
  add constraint NN_POS_MAJOR_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_POS_MAJOR_CODE on timetable.POS_MAJOR (CODE, SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table CURRICULUM_MAJOR
prompt ===============================
prompt
create table timetable.CURRICULUM_MAJOR
(
  curriculum_id NUMBER(20),
  major_id      NUMBER(20)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_MAJOR
  add constraint PK_CURRICULUM_MAJOR primary key (CURRICULUM_ID, MAJOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_MAJOR
  add constraint FK_CURRICULUM_MAJOR_CURRICULUM foreign key (CURRICULUM_ID)
  references timetable.CURRICULUM (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_MAJOR
  add constraint FK_CURRICULUM_MAJOR_MAJOR foreign key (MAJOR_ID)
  references timetable.POS_MAJOR (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_MAJOR
  add constraint NN_CURRICULUM_MAJOR_CUR_ID
  check ("CURRICULUM_ID" IS NOT NULL);
alter table timetable.CURRICULUM_MAJOR
  add constraint NN_CURRICULUM_MAJOR_MAJ_ID
  check ("MAJOR_ID" IS NOT NULL);

prompt
prompt Creating table CURRICULUM_RULE
prompt ==============================
prompt
create table timetable.CURRICULUM_RULE
(
  uniqueid      NUMBER(20),
  acad_area_id  NUMBER(20),
  major_id      NUMBER(20),
  acad_clasf_id NUMBER(20),
  projection    FLOAT
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_RULE
  add constraint PK_CURRICULUM_RULE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.CURRICULUM_RULE
  add constraint FK_CUR_RULE_ACAD_AREA foreign key (ACAD_AREA_ID)
  references timetable.ACADEMIC_AREA (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_RULE
  add constraint FK_CUR_RULE_ACAD_CLASF foreign key (ACAD_CLASF_ID)
  references timetable.ACADEMIC_CLASSIFICATION (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_RULE
  add constraint FK_CUR_RULE_MAJOR foreign key (MAJOR_ID)
  references timetable.POS_MAJOR (UNIQUEID) on delete cascade;
alter table timetable.CURRICULUM_RULE
  add constraint NN_CUR_RULE_ACAD_AREA
  check ("ACAD_AREA_ID" IS NOT NULL);
alter table timetable.CURRICULUM_RULE
  add constraint NN_CUR_RULE_ACAD_CLASF
  check ("ACAD_CLASF_ID" IS NOT NULL);
alter table timetable.CURRICULUM_RULE
  add constraint NN_CUR_RULE_ID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.CURRICULUM_RULE
  add constraint NN_CUR_RULE_PROJ
  check ("PROJECTION" IS NOT NULL);
create index timetable.IDX_CUR_RULE_AREADEPT on timetable.CURRICULUM_RULE (ACAD_AREA_ID, ACAD_CLASF_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table DATE_PATTERN_DEPT
prompt ================================
prompt
create table timetable.DATE_PATTERN_DEPT
(
  dept_id    NUMBER(20),
  pattern_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DATE_PATTERN_DEPT
  add constraint PK_DATE_PATTERN_DEPT primary key (DEPT_ID, PATTERN_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DATE_PATTERN_DEPT
  add constraint FK_DATE_PATTERN_DEPT_DATE foreign key (PATTERN_ID)
  references timetable.DATE_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.DATE_PATTERN_DEPT
  add constraint FK_DATE_PATTERN_DEPT_DEPT foreign key (DEPT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.DATE_PATTERN_DEPT
  add constraint NN_DATE_PATTERN_DEPT_DEPT_ID
  check ("DEPT_ID" IS NOT NULL);
alter table timetable.DATE_PATTERN_DEPT
  add constraint NN_DATE_PATTERN_DEPT_PATT_ID
  check ("PATTERN_ID" IS NOT NULL);

prompt
prompt Creating table DEMAND_OFFR_TYPE
prompt ===============================
prompt
create table timetable.DEMAND_OFFR_TYPE
(
  uniqueid  NUMBER(20),
  reference VARCHAR2(20),
  label     VARCHAR2(60)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEMAND_OFFR_TYPE
  add constraint PK_DEMAND_OFFR_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEMAND_OFFR_TYPE
  add constraint UK_DEMAND_OFFR_TYPE_LABEL unique (LABEL)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEMAND_OFFR_TYPE
  add constraint UK_DEMAND_OFFR_TYPE_REF unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEMAND_OFFR_TYPE
  add constraint NN_DEMAND_OFFR_TYPE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.DEMAND_OFFR_TYPE
  add constraint NN_DEMAND_OFFR_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table DEPT_TO_TT_MGR
prompt =============================
prompt
create table timetable.DEPT_TO_TT_MGR
(
  timetable_mgr_id NUMBER(20),
  department_id    NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPT_TO_TT_MGR
  add constraint PK_DEPT_TO_TT_MGR_UID primary key (TIMETABLE_MGR_ID, DEPARTMENT_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DEPT_TO_TT_MGR
  add constraint FK_DEPT_TO_TT_MGR_DEPT foreign key (DEPARTMENT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.DEPT_TO_TT_MGR
  add constraint FK_DEPT_TO_TT_MGR_MGR foreign key (TIMETABLE_MGR_ID)
  references timetable.TIMETABLE_MANAGER (UNIQUEID) on delete cascade;
alter table timetable.DEPT_TO_TT_MGR
  add constraint NN_DEPT_TO_TT_MGR_DEPT_ID
  check ("DEPARTMENT_ID" IS NOT NULL);
alter table timetable.DEPT_TO_TT_MGR
  add constraint NN_DEPT_TO_TT_MGR_TMTBL_MGR_ID
  check ("TIMETABLE_MGR_ID" IS NOT NULL);

prompt
prompt Creating table DESIGNATOR
prompt =========================
prompt
create table timetable.DESIGNATOR
(
  uniqueid           NUMBER(20),
  subject_area_id    NUMBER(20),
  instructor_id      NUMBER(20),
  code               VARCHAR2(3),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DESIGNATOR
  add constraint PK_DESIGNATOR primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DESIGNATOR
  add constraint UK_DESIGNATOR_CODE unique (SUBJECT_AREA_ID, INSTRUCTOR_ID, CODE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DESIGNATOR
  add constraint FK_DESIGNATOR_INSTRUCTOR foreign key (INSTRUCTOR_ID)
  references timetable.DEPARTMENTAL_INSTRUCTOR (UNIQUEID) on delete cascade;
alter table timetable.DESIGNATOR
  add constraint FK_DESIGNATOR_SUBJ_AREA foreign key (SUBJECT_AREA_ID)
  references timetable.SUBJECT_AREA (UNIQUEID) on delete cascade;
alter table timetable.DESIGNATOR
  add constraint NN_DESIGNATOR_CODE
  check ("CODE" IS NOT NULL);
alter table timetable.DESIGNATOR
  add constraint NN_DESIGNATOR_INSTRUCTOR_ID
  check ("INSTRUCTOR_ID" IS NOT NULL);
alter table timetable.DESIGNATOR
  add constraint NN_DESIGNATOR_SUBJECT_AREA_ID
  check ("SUBJECT_AREA_ID" IS NOT NULL);
alter table timetable.DESIGNATOR
  add constraint NN_DESIGNATOR_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table DISTRIBUTION_TYPE
prompt ================================
prompt
create table timetable.DISTRIBUTION_TYPE
(
  uniqueid            NUMBER(20),
  reference           VARCHAR2(20),
  label               VARCHAR2(60),
  sequencing_required VARCHAR2(1) default '0',
  req_id              NUMBER(6),
  allowed_pref        VARCHAR2(10),
  description         VARCHAR2(2048),
  abbreviation        VARCHAR2(20),
  instructor_pref     NUMBER(1) default (0),
  exam_pref           NUMBER(1) default 0
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_TYPE
  add constraint PK_DISTRIBUTION_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_TYPE
  add constraint UK_DISTRIBUTION_TYPE_REQ_ID unique (REQ_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_TYPE
  add constraint NN_DISTRIBUTION_TYPE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.DISTRIBUTION_TYPE
  add constraint NN_DISTRIBUTION_TYPE_REQ_ID
  check ("REQ_ID" IS NOT NULL);
alter table timetable.DISTRIBUTION_TYPE
  add constraint NN_DISTRIBUTION_TYPE_SEQ_REQD
  check ("SEQUENCING_REQUIRED" IS NOT NULL);
alter table timetable.DISTRIBUTION_TYPE
  add constraint NN_DISTRIBUTION_TYPE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table DISTRIBUTION_PREF
prompt ================================
prompt
create table timetable.DISTRIBUTION_PREF
(
  uniqueid            NUMBER(20),
  owner_id            NUMBER(20),
  pref_level_id       NUMBER(20),
  dist_type_id        NUMBER(20),
  grouping            NUMBER(10),
  last_modified_time  TIMESTAMP(6),
  uid_rolled_fwd_from NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_PREF
  add constraint PK_DISTRIBUTION_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_PREF
  add constraint FK_DISTRIBUTION_PREF_DIST_TYPE foreign key (DIST_TYPE_ID)
  references timetable.DISTRIBUTION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.DISTRIBUTION_PREF
  add constraint FK_DISTRIBUTION_PREF_LEVEL foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.DISTRIBUTION_PREF
  add constraint NN_DISTRIBUTION_PREF_DIST_TYPE
  check ("DIST_TYPE_ID" IS NOT NULL);
alter table timetable.DISTRIBUTION_PREF
  add constraint NN_DISTRIBUTION_PREF_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.DISTRIBUTION_PREF
  add constraint NN_DISTRIBUTION_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_DISTRIBUTION_PREF_LEVEL on timetable.DISTRIBUTION_PREF (PREF_LEVEL_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_DISTRIBUTION_PREF_OWNER on timetable.DISTRIBUTION_PREF (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_DISTRIBUTION_PREF_TYPE on timetable.DISTRIBUTION_PREF (DIST_TYPE_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table DISTRIBUTION_OBJECT
prompt ==================================
prompt
create table timetable.DISTRIBUTION_OBJECT
(
  uniqueid           NUMBER(20),
  dist_pref_id       NUMBER(20),
  sequence_number    NUMBER(3),
  pref_group_id      NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_OBJECT
  add constraint PK_DISTRIBUTION_OBJECT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DISTRIBUTION_OBJECT
  add constraint FK_DISTRIBUTION_OBJECT_PREF foreign key (DIST_PREF_ID)
  references timetable.DISTRIBUTION_PREF (UNIQUEID) on delete cascade;
alter table timetable.DISTRIBUTION_OBJECT
  add constraint NN_DISTRIBUTION_OBJECT_PREFID
  check ("DIST_PREF_ID" IS NOT NULL);
alter table timetable.DISTRIBUTION_OBJECT
  add constraint NN_DISTRIBUTION_OBJECT_PRGRPID
  check ("PREF_GROUP_ID" IS NOT NULL);
alter table timetable.DISTRIBUTION_OBJECT
  add constraint NN_DISTRIBUTION_OBJ_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_DISTRIBUTION_OBJECT_PG on timetable.DISTRIBUTION_OBJECT (PREF_GROUP_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_DISTRIBUTION_OBJECT_PREF on timetable.DISTRIBUTION_OBJECT (DIST_PREF_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table DIST_TYPE_DEPT
prompt =============================
prompt
create table timetable.DIST_TYPE_DEPT
(
  dist_type_id NUMBER(19),
  dept_id      NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DIST_TYPE_DEPT
  add constraint PK_DIST_TYPE_DEPT primary key (DIST_TYPE_ID, DEPT_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.DIST_TYPE_DEPT
  add constraint FK_DIST_TYPE_DEPT_DEPT foreign key (DEPT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.DIST_TYPE_DEPT
  add constraint FK_DIST_TYPE_DEPT_TYPE foreign key (DIST_TYPE_ID)
  references timetable.DISTRIBUTION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.DIST_TYPE_DEPT
  add constraint NN_DIST_TYPE_DEPT_DEPT_ID
  check ("DEPT_ID" IS NOT NULL);
alter table timetable.DIST_TYPE_DEPT
  add constraint NN_DIST_TYPE_DEPT_DIST_TYPE_ID
  check ("DIST_TYPE_ID" IS NOT NULL);

prompt
prompt Creating table EVENT_CONTACT
prompt ============================
prompt
create table timetable.EVENT_CONTACT
(
  uniqueid    NUMBER(20),
  external_id VARCHAR2(40),
  email       VARCHAR2(200),
  phone       VARCHAR2(25),
  firstname   VARCHAR2(100),
  middlename  VARCHAR2(100),
  lastname    VARCHAR2(100)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT_CONTACT
  add constraint PK_EVENT_CONTACT_UNIQUEID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT_CONTACT
  add constraint NN_EVENT_CONTACT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table EXAM_PERIOD
prompt ==========================
prompt
create table timetable.EXAM_PERIOD
(
  uniqueid           NUMBER(20),
  session_id         NUMBER(20),
  date_ofs           NUMBER(10),
  start_slot         NUMBER(10),
  length             NUMBER(10),
  pref_level_id      NUMBER(20),
  exam_type          NUMBER(10) default 0,
  event_start_offset NUMBER(10) default 0 not null,
  event_stop_offset  NUMBER(10) default 0 not null
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_PERIOD
  add constraint PK_EXAM_PERIOD primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_PERIOD
  add constraint FK_EXAM_PERIOD_PREF foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.EXAM_PERIOD
  add constraint FK_EXAM_PERIOD_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.EXAM_PERIOD
  add constraint NN_EXAM_PERIOD_DATE_OFS
  check ("DATE_OFS" IS NOT NULL);
alter table timetable.EXAM_PERIOD
  add constraint NN_EXAM_PERIOD_LENGTH
  check ("LENGTH" IS NOT NULL);
alter table timetable.EXAM_PERIOD
  add constraint NN_EXAM_PERIOD_PREF
  check ("PREF_LEVEL_ID" IS NOT NULL);
alter table timetable.EXAM_PERIOD
  add constraint NN_EXAM_PERIOD_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.EXAM_PERIOD
  add constraint NN_EXAM_PERIOD_START_SLOT
  check ("START_SLOT" IS NOT NULL);
alter table timetable.EXAM_PERIOD
  add constraint NN_EXAM_PERIOD_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table EXAM
prompt ===================
prompt
create table timetable.EXAM
(
  uniqueid            NUMBER(20),
  session_id          NUMBER(20),
  name                VARCHAR2(100),
  note                VARCHAR2(1000),
  length              NUMBER(10),
  max_nbr_rooms       NUMBER(10) default 1,
  seating_type        NUMBER(10),
  assigned_period     NUMBER(20),
  assigned_pref       VARCHAR2(100),
  exam_type           NUMBER(10) default 0,
  avg_period          NUMBER(10),
  uid_rolled_fwd_from NUMBER(20),
  exam_size           NUMBER(10),
  print_offset        NUMBER(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM
  add constraint PK_EXAM primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM
  add constraint FK_EXAM_PERIOD foreign key (ASSIGNED_PERIOD)
  references timetable.EXAM_PERIOD (UNIQUEID) on delete cascade;
alter table timetable.EXAM
  add constraint FK_EXAM_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.EXAM
  add constraint NN_EXAM_LENGTH
  check ("LENGTH" IS NOT NULL);
alter table timetable.EXAM
  add constraint NN_EXAM_NBR_ROOMS
  check ("MAX_NBR_ROOMS" IS NOT NULL);
alter table timetable.EXAM
  add constraint NN_EXAM_SEATING
  check ("SEATING_TYPE" IS NOT NULL);
alter table timetable.EXAM
  add constraint NN_EXAM_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.EXAM
  add constraint NN_EXAM_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table SPONSORING_ORGANIZATION
prompt ======================================
prompt
create table timetable.SPONSORING_ORGANIZATION
(
  uniqueid NUMBER(20),
  name     VARCHAR2(100),
  email    VARCHAR2(200)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SPONSORING_ORGANIZATION
  add constraint PK_SPONSORING_ORGANIZATION primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SPONSORING_ORGANIZATION
  add constraint NN_SPONSOR_ORG_ID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.SPONSORING_ORGANIZATION
  add constraint NN_SPONSOR_ORG_NAME
  check ("NAME" IS NOT NULL);

prompt
prompt Creating table EVENT
prompt ====================
prompt
create table timetable.EVENT
(
  uniqueid        NUMBER(20) not null,
  event_name      VARCHAR2(100),
  min_capacity    NUMBER(10),
  max_capacity    NUMBER(10),
  sponsoring_org  NUMBER(20),
  main_contact_id NUMBER(20),
  class_id        NUMBER(20),
  exam_id         NUMBER(20),
  event_type      NUMBER(10),
  req_attd        NUMBER(1),
  email           VARCHAR2(1000),
  sponsor_org_id  NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT
  add constraint PK_EVENT_UNIQUEID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT
  add constraint FK_EVENT_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.EVENT
  add constraint FK_EVENT_EXAM foreign key (EXAM_ID)
  references timetable.EXAM (UNIQUEID) on delete cascade;
alter table timetable.EVENT
  add constraint FK_EVENT_MAIN_CONTACT foreign key (MAIN_CONTACT_ID)
  references timetable.EVENT_CONTACT (UNIQUEID) on delete set null;
alter table timetable.EVENT
  add constraint FK_EVENT_SPONSOR_ORG foreign key (SPONSOR_ORG_ID)
  references timetable.SPONSORING_ORGANIZATION (UNIQUEID) on delete set null;
alter table timetable.EVENT
  add constraint NN_EVENT_TYPE
  check (event_type is not null);
create index timetable.IDX_EVENT_CLASS on timetable.EVENT (CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_EVENT_EXAM on timetable.EVENT (EXAM_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table EVENT_JOIN_EVENT_CONTACT
prompt =======================================
prompt
create table timetable.EVENT_JOIN_EVENT_CONTACT
(
  event_id         NUMBER(20),
  event_contact_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT_JOIN_EVENT_CONTACT
  add constraint FK_EVENT_CONTACT_JOIN foreign key (EVENT_CONTACT_ID)
  references timetable.EVENT_CONTACT (UNIQUEID) on delete cascade;
alter table timetable.EVENT_JOIN_EVENT_CONTACT
  add constraint FK_EVENT_ID_JOIN foreign key (EVENT_ID)
  references timetable.EVENT (UNIQUEID) on delete cascade;
alter table timetable.EVENT_JOIN_EVENT_CONTACT
  add constraint NN_EVENT_JOIN_EVENT_CONTACT_ID
  check ("EVENT_CONTACT_ID" IS NOT NULL);
alter table timetable.EVENT_JOIN_EVENT_CONTACT
  add constraint NN_EVENT_JOIN_EVENT_ID
  check ("EVENT_ID" IS NOT NULL);

prompt
prompt Creating table EVENT_NOTE
prompt =========================
prompt
create table timetable.EVENT_NOTE
(
  uniqueid   NUMBER(20),
  event_id   NUMBER(20),
  text_note  VARCHAR2(1000),
  time_stamp DATE,
  note_type  NUMBER(10) default 0,
  uname      VARCHAR2(100),
  meetings   VARCHAR2(2000)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT_NOTE
  add constraint PK_EVENT_NOTE_UNIQUEID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EVENT_NOTE
  add constraint FK_EVENT_NOTE_EVENT foreign key (EVENT_ID)
  references timetable.EVENT (UNIQUEID) on delete cascade;
alter table timetable.EVENT_NOTE
  add constraint NN_EVENT_NOTE_EVENT_UNIQUEID
  check ("EVENT_ID" IS NOT NULL);
alter table timetable.EVENT_NOTE
  add constraint NN_EVENT_NOTE_TS
  check (time_stamp is not null);
alter table timetable.EVENT_NOTE
  add constraint NN_EVENT_NOTE_TYPE
  check ("NOTE_TYPE" IS NOT NULL);
alter table timetable.EVENT_NOTE
  add constraint NN_EVENT_NOTE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table EXACT_TIME_MINS
prompt ==============================
prompt
create table timetable.EXACT_TIME_MINS
(
  uniqueid   NUMBER(20),
  mins_min   NUMBER(4),
  mins_max   NUMBER(4),
  nr_slots   NUMBER(4),
  break_time NUMBER(4)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXACT_TIME_MINS
  add constraint PK_EXACT_TIME_MINS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXACT_TIME_MINS
  add constraint NN_EXACT_TIME_MINS_BREAK
  check ("BREAK_TIME" IS NOT NULL);
alter table timetable.EXACT_TIME_MINS
  add constraint NN_EXACT_TIME_MINS_MAX
  check ("MINS_MAX" IS NOT NULL);
alter table timetable.EXACT_TIME_MINS
  add constraint NN_EXACT_TIME_MINS_MIN
  check ("MINS_MIN" IS NOT NULL);
alter table timetable.EXACT_TIME_MINS
  add constraint NN_EXACT_TIME_MINS_SLOTS
  check ("NR_SLOTS" IS NOT NULL);
alter table timetable.EXACT_TIME_MINS
  add constraint NN_EXACT_TIME_MINS_UID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_EXACT_TIME_MINS on timetable.EXACT_TIME_MINS (MINS_MIN, MINS_MAX)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table EXAM_INSTRUCTOR
prompt ==============================
prompt
create table timetable.EXAM_INSTRUCTOR
(
  exam_id       NUMBER(20),
  instructor_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_INSTRUCTOR
  add constraint PK_EXAM_INSTRUCTOR primary key (EXAM_ID, INSTRUCTOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_INSTRUCTOR
  add constraint FK_EXAM_INSTRUCTOR_EXAM foreign key (EXAM_ID)
  references timetable.EXAM (UNIQUEID) on delete cascade;
alter table timetable.EXAM_INSTRUCTOR
  add constraint FK_EXAM_INSTRUCTOR_INSTRUCTOR foreign key (INSTRUCTOR_ID)
  references timetable.DEPARTMENTAL_INSTRUCTOR (UNIQUEID) on delete cascade;
alter table timetable.EXAM_INSTRUCTOR
  add constraint NN_EXAM_INSTRUCTOR_EXAM
  check ("EXAM_ID" IS NOT NULL);
alter table timetable.EXAM_INSTRUCTOR
  add constraint NN_EXAM_INSTRUCTOR_INSTRUCTOR
  check ("INSTRUCTOR_ID" IS NOT NULL);

prompt
prompt Creating table EXAM_LOCATION_PREF
prompt =================================
prompt
create table timetable.EXAM_LOCATION_PREF
(
  uniqueid      NUMBER(20),
  location_id   NUMBER(20),
  pref_level_id NUMBER(20),
  period_id     NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_LOCATION_PREF
  add constraint PK_EXAM_LOCATION_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_LOCATION_PREF
  add constraint FK_EXAM_LOCATION_PREF_PERIOD foreign key (PERIOD_ID)
  references timetable.EXAM_PERIOD (UNIQUEID) on delete cascade;
alter table timetable.EXAM_LOCATION_PREF
  add constraint FK_EXAM_LOCATION_PREF_PREF foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.EXAM_LOCATION_PREF
  add constraint NN_EXAM_LOCATION_PREF_OWNER
  check ("LOCATION_ID" IS NOT NULL);
alter table timetable.EXAM_LOCATION_PREF
  add constraint NN_EXAM_LOCATION_PREF_PERIOD
  check ("PERIOD_ID" IS NOT NULL);
alter table timetable.EXAM_LOCATION_PREF
  add constraint NN_EXAM_LOCATION_PREF_PREF
  check ("PREF_LEVEL_ID" IS NOT NULL);
alter table timetable.EXAM_LOCATION_PREF
  add constraint NN_EXAM_LOCATION_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_EXAM_LOCATION_PREF on timetable.EXAM_LOCATION_PREF (LOCATION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table EXAM_OWNER
prompt =========================
prompt
create table timetable.EXAM_OWNER
(
  uniqueid   NUMBER(20),
  exam_id    NUMBER(20),
  owner_id   NUMBER(20),
  owner_type NUMBER(10),
  course_id  NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_OWNER
  add constraint PK_EXAM_OWNER primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_OWNER
  add constraint FK_EXAM_OWNER_COURSE foreign key (COURSE_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.EXAM_OWNER
  add constraint FK_EXAM_OWNER_EXAM foreign key (EXAM_ID)
  references timetable.EXAM (UNIQUEID) on delete cascade;
alter table timetable.EXAM_OWNER
  add constraint NN_EXAM_OWNER_COURSE
  check (course_id is not null);
alter table timetable.EXAM_OWNER
  add constraint NN_EXAM_OWNER_EXAM_ID
  check ("EXAM_ID" IS NOT NULL);
alter table timetable.EXAM_OWNER
  add constraint NN_EXAM_OWNER_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.EXAM_OWNER
  add constraint NN_EXAM_OWNER_OWNER_TYPE
  check ("OWNER_TYPE" IS NOT NULL);
alter table timetable.EXAM_OWNER
  add constraint NN_EXAM_OWNER_UNIQUE_ID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_EXAM_OWNER_COURSE on timetable.EXAM_OWNER (COURSE_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_EXAM_OWNER_EXAM on timetable.EXAM_OWNER (EXAM_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_EXAM_OWNER_OWNER on timetable.EXAM_OWNER (OWNER_ID, OWNER_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table EXAM_PERIOD_PREF
prompt ===============================
prompt
create table timetable.EXAM_PERIOD_PREF
(
  uniqueid      NUMBER(20),
  owner_id      NUMBER(20),
  pref_level_id NUMBER(20),
  period_id     NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_PERIOD_PREF
  add constraint PK_EXAM_PERIOD_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_PERIOD_PREF
  add constraint FK_EXAM_PERIOD_PREF_PERIOD foreign key (PERIOD_ID)
  references timetable.EXAM_PERIOD (UNIQUEID) on delete cascade;
alter table timetable.EXAM_PERIOD_PREF
  add constraint FK_EXAM_PERIOD_PREF_PREF foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.EXAM_PERIOD_PREF
  add constraint NN_EXAM_PERIOD_PREF_OWNER
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.EXAM_PERIOD_PREF
  add constraint NN_EXAM_PERIOD_PREF_PERIOD
  check ("PERIOD_ID" IS NOT NULL);
alter table timetable.EXAM_PERIOD_PREF
  add constraint NN_EXAM_PERIOD_PREF_PREF
  check ("PREF_LEVEL_ID" IS NOT NULL);
alter table timetable.EXAM_PERIOD_PREF
  add constraint NN_EXAM_PERIOD_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table EXAM_ROOM_ASSIGNMENT
prompt ===================================
prompt
create table timetable.EXAM_ROOM_ASSIGNMENT
(
  exam_id     NUMBER(20),
  location_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_ROOM_ASSIGNMENT
  add constraint PK_EXAM_ROOM_ASSIGNMENT primary key (EXAM_ID, LOCATION_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXAM_ROOM_ASSIGNMENT
  add constraint FK_EXAM_ROOM_EXAM foreign key (EXAM_ID)
  references timetable.EXAM (UNIQUEID) on delete cascade;
alter table timetable.EXAM_ROOM_ASSIGNMENT
  add constraint NN_EXAM_ROOM_EXAM_ID
  check ("EXAM_ID" IS NOT NULL);
alter table timetable.EXAM_ROOM_ASSIGNMENT
  add constraint NN_EXAM_ROOM_LOCATION_ID
  check ("LOCATION_ID" IS NOT NULL);

prompt
prompt Creating table EXTERNAL_BUILDING
prompt ================================
prompt
create table timetable.EXTERNAL_BUILDING
(
  uniqueid     NUMBER(20) not null,
  session_id   NUMBER(20),
  external_uid VARCHAR2(40),
  abbreviation VARCHAR2(10),
  coordinate_x FLOAT,
  coordinate_y FLOAT,
  display_name VARCHAR2(100)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_BUILDING
  add constraint PK_EXTERNAL_BLDG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_BUILDING
  add constraint NN_EXTERNAL_BLDG_ABBV
  check ("ABBREVIATION" IS NOT NULL);
alter table timetable.EXTERNAL_BUILDING
  add constraint NN_EXTERNAL_BLDG_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
create index timetable.IDX_EXTERNAL_BUILDING on timetable.EXTERNAL_BUILDING (SESSION_ID, ABBREVIATION)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_TYPE
prompt ========================
prompt
create table timetable.ROOM_TYPE
(
  uniqueid  NUMBER(20),
  reference VARCHAR2(20),
  label     VARCHAR2(60),
  ord       NUMBER(10),
  is_room   NUMBER(1) default 1
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_TYPE
  add constraint PK_ROOM_TYPE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_TYPE
  add constraint NN_ROOM_TYPE_LABEL
  check ("LABEL" IS NOT NULL);
alter table timetable.ROOM_TYPE
  add constraint NN_ROOM_TYPE_ORD
  check ("ORD" IS NOT NULL);
alter table timetable.ROOM_TYPE
  add constraint NN_ROOM_TYPE_REF
  check ("REFERENCE" IS NOT NULL);
alter table timetable.ROOM_TYPE
  add constraint NN_ROOM_TYPE_ROOM
  check ("IS_ROOM" IS NOT NULL);
alter table timetable.ROOM_TYPE
  add constraint NN_ROOM_TYPE_UID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table EXTERNAL_ROOM
prompt ============================
prompt
create table timetable.EXTERNAL_ROOM
(
  uniqueid         NUMBER(20) not null,
  external_bldg_id NUMBER(20),
  external_uid     VARCHAR2(40),
  room_number      VARCHAR2(10),
  coordinate_x     FLOAT,
  coordinate_y     FLOAT,
  capacity         NUMBER(10),
  classification   VARCHAR2(20),
  instructional    NUMBER(1),
  display_name     VARCHAR2(100),
  exam_capacity    NUMBER(10),
  room_type        NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_ROOM
  add constraint PK_EXTERNAL_ROOM primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_ROOM
  add constraint FK_EXTERNAL_ROOM_TYPE foreign key (ROOM_TYPE)
  references timetable.ROOM_TYPE (UNIQUEID) on delete cascade;
alter table timetable.EXTERNAL_ROOM
  add constraint FK_EXT_ROOM_BUILDING foreign key (EXTERNAL_BLDG_ID)
  references timetable.EXTERNAL_BUILDING (UNIQUEID) on delete cascade;
alter table timetable.EXTERNAL_ROOM
  add constraint NN_EXTERNAL_RM_BLDG_ID
  check ("EXTERNAL_BLDG_ID" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM
  add constraint NN_EXTERNAL_RM_CAPACITY
  check ("CAPACITY" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM
  add constraint NN_EXTERNAL_RM_CLASS
  check ("CLASSIFICATION" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM
  add constraint NN_EXTERNAL_RM_INSTR
  check ("INSTRUCTIONAL" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM
  add constraint NN_EXTERNAL_RM_NBR
  check ("ROOM_NUMBER" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM
  add constraint NN_EXTERNAL_ROOM_TYPE
  check (room_type is not null);
create index timetable.IDX_EXTERNAL_ROOM on timetable.EXTERNAL_ROOM (EXTERNAL_BLDG_ID, ROOM_NUMBER)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table EXTERNAL_ROOM_DEPARTMENT
prompt =======================================
prompt
create table timetable.EXTERNAL_ROOM_DEPARTMENT
(
  uniqueid         NUMBER(20) not null,
  external_room_id NUMBER(20),
  department_code  VARCHAR2(50),
  percent          NUMBER(10),
  assignment_type  VARCHAR2(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_ROOM_DEPARTMENT
  add constraint PK_EXTERNAL_ROOM_DEPT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_ROOM_DEPARTMENT
  add constraint FK_EXT_DEPT_ROOM foreign key (EXTERNAL_ROOM_ID)
  references timetable.EXTERNAL_ROOM (UNIQUEID) on delete cascade;
alter table timetable.EXTERNAL_ROOM_DEPARTMENT
  add constraint NN_EXTERNAL_RM_DEPT_ASSGN
  check ("ASSIGNMENT_TYPE" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM_DEPARTMENT
  add constraint NN_EXTERNAL_RM_DEPT_CODE
  check ("DEPARTMENT_CODE" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM_DEPARTMENT
  add constraint NN_EXTERNAL_RM_DEPT_PCNT
  check ("PERCENT" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM_DEPARTMENT
  add constraint NN_EXTERNAL_RM_DEPT_ROOM_ID
  check ("EXTERNAL_ROOM_ID" IS NOT NULL);

prompt
prompt Creating table EXTERNAL_ROOM_FEATURE
prompt ====================================
prompt
create table timetable.EXTERNAL_ROOM_FEATURE
(
  uniqueid         NUMBER(20) not null,
  external_room_id NUMBER(20),
  name             VARCHAR2(20),
  value            VARCHAR2(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_ROOM_FEATURE
  add constraint PK_EXTERNAL_ROOM_FEATURE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.EXTERNAL_ROOM_FEATURE
  add constraint FK_EXT_FTR_ROOM foreign key (EXTERNAL_ROOM_ID)
  references timetable.EXTERNAL_ROOM (UNIQUEID) on delete cascade;
alter table timetable.EXTERNAL_ROOM_FEATURE
  add constraint NN_EXTERNAL_RM_FTR_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM_FEATURE
  add constraint NN_EXTERNAL_RM_FTR_ROOM_ID
  check ("EXTERNAL_ROOM_ID" IS NOT NULL);
alter table timetable.EXTERNAL_ROOM_FEATURE
  add constraint NN_EXTERNAL_RM_FTR_VALUE
  check ("VALUE" IS NOT NULL);

prompt
prompt Creating table HISTORY
prompt ======================
prompt
create table timetable.HISTORY
(
  uniqueid   NUMBER(20),
  subclass   VARCHAR2(10),
  old_value  VARCHAR2(20),
  new_value  VARCHAR2(20),
  old_number VARCHAR2(20),
  new_number VARCHAR2(20),
  session_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.HISTORY
  add constraint PK_HISTORY primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.HISTORY
  add constraint FK_HISTORY_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.HISTORY
  add constraint NN_HISTORY_NEW_VALUE
  check ("NEW_VALUE" IS NOT NULL);
alter table timetable.HISTORY
  add constraint NN_HISTORY_OLD_VALUE
  check ("OLD_VALUE" IS NOT NULL);
alter table timetable.HISTORY
  add constraint NN_HISTORY_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.HISTORY
  add constraint NN_HISTORY_SUBCLASS
  check ("SUBCLASS" IS NOT NULL);
alter table timetable.HISTORY
  add constraint NN_HISTORY_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_HISTORY_SESSION on timetable.HISTORY (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table HT_PREFERENCE
prompt ============================
prompt
create global temporary table timetable.HT_PREFERENCE
(
  uniqueid NUMBER(19) not null
)
on commit delete rows;

prompt
prompt Creating table INDIVIDUAL_RESERVATION
prompt =====================================
prompt
create table timetable.INDIVIDUAL_RESERVATION
(
  uniqueid           NUMBER(20),
  owner              NUMBER(20),
  reservation_type   NUMBER(20),
  priority           NUMBER(5),
  external_uid       VARCHAR2(40),
  over_limit         NUMBER(1),
  expiration_date    DATE,
  owner_class_id     VARCHAR2(1),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint PK_INDIVIDUAL_RESV primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint FK_INDIVIDUAL_RESV_TYPE foreign key (RESERVATION_TYPE)
  references timetable.RESERVATION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_EXP_DATE
  check ("EXPIRATION_DATE" IS NOT NULL);
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_OVERLIMIT
  check ("OVER_LIMIT" IS NOT NULL);
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_OWNER
  check ("OWNER" IS NOT NULL);
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_OWNER_CLSID
  check ("OWNER_CLASS_ID" IS NOT NULL);
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_PRIORITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_RESERV_TYPE
  check ("RESERVATION_TYPE" IS NOT NULL);
alter table timetable.INDIVIDUAL_RESERVATION
  add constraint NN_INDIVIDUAL_RESV_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_INDIVIDUAL_RESV_OWNER on timetable.INDIVIDUAL_RESERVATION (OWNER)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_INDIVIDUAL_RESV_OWNER_CLS on timetable.INDIVIDUAL_RESERVATION (OWNER_CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_INDIVIDUAL_RESV_TYPE on timetable.INDIVIDUAL_RESERVATION (RESERVATION_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table JENRL
prompt ====================
prompt
create table timetable.JENRL
(
  uniqueid    NUMBER(20),
  jenrl       FLOAT,
  solution_id NUMBER(20),
  class1_id   NUMBER(20),
  class2_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.JENRL
  add constraint PK_JENRL primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.JENRL
  add constraint FK_JENRL_CLASS1 foreign key (CLASS1_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.JENRL
  add constraint FK_JENRL_CLASS2 foreign key (CLASS2_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.JENRL
  add constraint FK_JENRL_SOLUTION foreign key (SOLUTION_ID)
  references timetable.SOLUTION (UNIQUEID) on delete cascade;
alter table timetable.JENRL
  add constraint NN_JENRL_CLASS1_ID
  check ("CLASS1_ID" IS NOT NULL);
alter table timetable.JENRL
  add constraint NN_JENRL_CLASS2_ID
  check ("CLASS2_ID" IS NOT NULL);
alter table timetable.JENRL
  add constraint NN_JENRL_JENRL
  check ("JENRL" IS NOT NULL);
alter table timetable.JENRL
  add constraint NN_JENRL_SOLUTION_ID
  check ("SOLUTION_ID" IS NOT NULL);
alter table timetable.JENRL
  add constraint NN_JENRL_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_JENRL on timetable.JENRL (SOLUTION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_JENRL_CLASS1 on timetable.JENRL (CLASS1_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_JENRL_CLASS2 on timetable.JENRL (CLASS2_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table LASTLIKE_COURSE_DEMAND
prompt =====================================
prompt
create table timetable.LASTLIKE_COURSE_DEMAND
(
  uniqueid        NUMBER(20),
  student_id      NUMBER(20),
  subject_area_id NUMBER(20),
  course_nbr      VARCHAR2(10),
  priority        NUMBER(10) default (0),
  course_perm_id  VARCHAR2(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint PK_LASTLIKE_COURSE_DEMAND primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint FK_LL_COURSE_DEMAND_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint FK_LL_COURSE_DEMAND_SUBJAREA foreign key (SUBJECT_AREA_ID)
  references timetable.SUBJECT_AREA (UNIQUEID) on delete cascade;
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint NN_LL_COURSE_DEMAND_AREA
  check ("SUBJECT_AREA_ID" IS NOT NULL);
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint NN_LL_COURSE_DEMAND_COURSENBR
  check ("COURSE_NBR" IS NOT NULL);
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint NN_LL_COURSE_DEMAND_PRIRITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint NN_LL_COURSE_DEMAND_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.LASTLIKE_COURSE_DEMAND
  add constraint NN_LL_COURSE_DEMAND_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_LL_COURSE_DEMAND_COURSE on timetable.LASTLIKE_COURSE_DEMAND (SUBJECT_AREA_ID, COURSE_NBR)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_LL_COURSE_DEMAND_PERMID on timetable.LASTLIKE_COURSE_DEMAND (COURSE_PERM_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_LL_COURSE_DEMAND_STUDENT on timetable.LASTLIKE_COURSE_DEMAND (STUDENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SETTINGS
prompt =======================
prompt
create table timetable.SETTINGS
(
  uniqueid       NUMBER(20),
  name           VARCHAR2(30),
  default_value  VARCHAR2(100),
  allowed_values VARCHAR2(500),
  description    VARCHAR2(100)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SETTINGS
  add constraint PK_SETTINGS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SETTINGS
  add constraint NN_SETTINGS_ALLOWED_VALUES
  check ("ALLOWED_VALUES" IS NOT NULL);
alter table timetable.SETTINGS
  add constraint NN_SETTINGS_DEFAULT_VALUE
  check ("DEFAULT_VALUE" IS NOT NULL);
alter table timetable.SETTINGS
  add constraint NN_SETTINGS_DESCRIPTION
  check ("DESCRIPTION" IS NOT NULL);
alter table timetable.SETTINGS
  add constraint NN_SETTINGS_KEY
  check ("NAME" IS NOT NULL);
alter table timetable.SETTINGS
  add constraint NN_SETTINGS_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table MANAGER_SETTINGS
prompt ===============================
prompt
create table timetable.MANAGER_SETTINGS
(
  uniqueid      NUMBER(20),
  key_id        NUMBER(20),
  value         VARCHAR2(100),
  user_uniqueid NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.MANAGER_SETTINGS
  add constraint PK_MANAGER_SETTINGS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.MANAGER_SETTINGS
  add constraint FK_MANAGER_SETTINGS_KEY foreign key (KEY_ID)
  references timetable.SETTINGS (UNIQUEID) on delete cascade;
alter table timetable.MANAGER_SETTINGS
  add constraint FK_MANAGER_SETTINGS_USER foreign key (USER_UNIQUEID)
  references timetable.TIMETABLE_MANAGER (UNIQUEID) on delete cascade;
alter table timetable.MANAGER_SETTINGS
  add constraint NN_MANAGER_SETTINGS_KEY_ID
  check ("KEY_ID" IS NOT NULL);
alter table timetable.MANAGER_SETTINGS
  add constraint NN_MANAGER_SETTINGS_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
alter table timetable.MANAGER_SETTINGS
  add constraint NN_MANAGER_SETTINGS_USER_ID
  check ("USER_UNIQUEID" IS NOT NULL);
alter table timetable.MANAGER_SETTINGS
  add constraint NN_MANAGER_SETTINGS_VALUE
  check ("VALUE" IS NOT NULL);
create index timetable.IDX_MANAGER_SETTINGS_KEY on timetable.MANAGER_SETTINGS (KEY_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_MANAGER_SETTINGS_MANAGER on timetable.MANAGER_SETTINGS (USER_UNIQUEID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table MEETING
prompt ======================
prompt
create table timetable.MEETING
(
  uniqueid           NUMBER(20),
  event_id           NUMBER(20),
  meeting_date       DATE,
  start_period       NUMBER(10),
  start_offset       NUMBER(10),
  stop_period        NUMBER(10),
  stop_offset        NUMBER(10),
  location_perm_id   NUMBER(20),
  class_can_override NUMBER(1),
  approved_date      DATE
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 448K
    minextents 1
    maxextents unlimited
  );
alter table timetable.MEETING
  add constraint PK_MEETING_UNIQUEID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 192K
    minextents 1
    maxextents unlimited
  );
alter table timetable.MEETING
  add constraint FK_MEETING_EVENT foreign key (EVENT_ID)
  references timetable.EVENT (UNIQUEID) on delete cascade;
alter table timetable.MEETING
  add constraint NN_MEETING_DATE
  check ("MEETING_DATE" IS NOT NULL);
alter table timetable.MEETING
  add constraint NN_MEETING_EVENT_ID
  check ("EVENT_ID" IS NOT NULL);
alter table timetable.MEETING
  add constraint NN_MEETING_OVERRIDE
  check ("CLASS_CAN_OVERRIDE" IS NOT NULL);
alter table timetable.MEETING
  add constraint NN_MEETING_START_PERIOD
  check ("START_PERIOD" IS NOT NULL);
alter table timetable.MEETING
  add constraint NN_MEETING_STOP_PERIOD
  check ("STOP_PERIOD" IS NOT NULL);
alter table timetable.MEETING
  add constraint NN_MEETING_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table NON_UNIVERSITY_LOCATION
prompt ======================================
prompt
create table timetable.NON_UNIVERSITY_LOCATION
(
  uniqueid          NUMBER(20),
  session_id        NUMBER(20),
  name              VARCHAR2(20),
  capacity          NUMBER(10),
  coordinate_x      FLOAT,
  coordinate_y      FLOAT,
  ignore_too_far    NUMBER(1),
  manager_ids       VARCHAR2(200),
  pattern           VARCHAR2(350),
  ignore_room_check NUMBER(1) default (0),
  display_name      VARCHAR2(100),
  exam_capacity     NUMBER(10) default 0,
  permanent_id      NUMBER(20) not null,
  exam_type         NUMBER(10) default 0,
  room_type         NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint PK_NON_UNIV_LOC primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint FK_LOCATION_TYPE foreign key (ROOM_TYPE)
  references timetable.ROOM_TYPE (UNIQUEID) on delete cascade;
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint FK_NON_UNIV_LOC_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_LOCATION_TYPE
  check (room_type is not null);
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_NON_UNIV_LOC_CAPACITY
  check ("CAPACITY" IS NOT NULL);
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_NON_UNIV_LOC_IGN_TM_CHECK
  check ("IGNORE_ROOM_CHECK" IS NOT NULL);
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_NON_UNIV_LOC_IGN_TOO_FAR
  check ("IGNORE_TOO_FAR" IS NOT NULL);
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_NON_UNIV_LOC_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_NON_UNIV_LOC_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.NON_UNIVERSITY_LOCATION
  add constraint NN_NON_UNIV_LOC_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_LOCATION_PERMID on timetable.NON_UNIVERSITY_LOCATION (PERMANENT_ID, SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_NON_UNIV_LOC_SESSION on timetable.NON_UNIVERSITY_LOCATION (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table OFFR_GROUP
prompt =========================
prompt
create table timetable.OFFR_GROUP
(
  uniqueid      NUMBER(20),
  session_id    NUMBER(20),
  name          VARCHAR2(20),
  description   VARCHAR2(200),
  department_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_GROUP
  add constraint PK_OFFR_GROUP_UID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_GROUP
  add constraint FK_OFFR_GROUP_DEPT foreign key (DEPARTMENT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.OFFR_GROUP
  add constraint FK_OFFR_GROUP_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.OFFR_GROUP
  add constraint NN_OFFR_GROUP_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.OFFR_GROUP
  add constraint NN_OFFR_GROUP_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.OFFR_GROUP
  add constraint NN_OFFR_GROUP_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_OFFR_GROUP_DEPT on timetable.OFFR_GROUP (DEPARTMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_OFFR_GROUP_SESSION on timetable.OFFR_GROUP (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table OFFR_GROUP_OFFERING
prompt ==================================
prompt
create table timetable.OFFR_GROUP_OFFERING
(
  offr_group_id     NUMBER(20),
  instr_offering_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_GROUP_OFFERING
  add constraint PK_OFFR_GROUP_OFFERING primary key (OFFR_GROUP_ID, INSTR_OFFERING_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.OFFR_GROUP_OFFERING
  add constraint FK_OFFR_GROUP_INSTR_OFFR foreign key (INSTR_OFFERING_ID)
  references timetable.INSTRUCTIONAL_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.OFFR_GROUP_OFFERING
  add constraint FK_OFFR_GROUP_OFFR_OFFR_GRP foreign key (OFFR_GROUP_ID)
  references timetable.OFFR_GROUP (UNIQUEID) on delete cascade;
alter table timetable.OFFR_GROUP_OFFERING
  add constraint NN_OFFR_GROUP_OFFERING_GRP_ID
  check ("OFFR_GROUP_ID" IS NOT NULL);
alter table timetable.OFFR_GROUP_OFFERING
  add constraint NN_OFFR_GROUP_OFFERING_OFFR_ID
  check ("INSTR_OFFERING_ID" IS NOT NULL);

prompt
prompt Creating table POSITION_CODE_TO_TYPE
prompt ====================================
prompt
create table timetable.POSITION_CODE_TO_TYPE
(
  position_code CHAR(5),
  pos_code_type NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POSITION_CODE_TO_TYPE
  add constraint PK_POS_CODE_TO_TYPE primary key (POSITION_CODE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POSITION_CODE_TO_TYPE
  add constraint FK_POS_CODE_TO_TYPE_CODE_TYPE foreign key (POS_CODE_TYPE)
  references timetable.POSITION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.POSITION_CODE_TO_TYPE
  add constraint NN_POS_CODE_TO_TYPE_CODE_TYPE
  check ("POS_CODE_TYPE" IS NOT NULL);
alter table timetable.POSITION_CODE_TO_TYPE
  add constraint NN_POS_CODE_TO_TYPE_POS_CODE
  check ("POSITION_CODE" IS NOT NULL);
create index timetable.IDX_POS_CODE_TO_TYPE_TYPE on timetable.POSITION_CODE_TO_TYPE (POS_CODE_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table POS_ACAD_AREA_MAJOR
prompt ==================================
prompt
create table timetable.POS_ACAD_AREA_MAJOR
(
  academic_area_id NUMBER(20),
  major_id         NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_ACAD_AREA_MAJOR
  add constraint PK_POS_ACAD_AREA_MAJOR primary key (ACADEMIC_AREA_ID, MAJOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_ACAD_AREA_MAJOR
  add constraint FK_POS_ACAD_AREA_MAJOR_AREA foreign key (ACADEMIC_AREA_ID)
  references timetable.ACADEMIC_AREA (UNIQUEID) on delete cascade;
alter table timetable.POS_ACAD_AREA_MAJOR
  add constraint FK_POS_ACAD_AREA_MAJOR_MAJOR foreign key (MAJOR_ID)
  references timetable.POS_MAJOR (UNIQUEID) on delete cascade;
alter table timetable.POS_ACAD_AREA_MAJOR
  add constraint NN_POS_ACAD_AREA_MAJOR_AREA
  check ("ACADEMIC_AREA_ID" IS NOT NULL);
alter table timetable.POS_ACAD_AREA_MAJOR
  add constraint NN_POS_ACAD_AREA_MAJOR_MAJOR
  check ("MAJOR_ID" IS NOT NULL);

prompt
prompt Creating table POS_MINOR
prompt ========================
prompt
create table timetable.POS_MINOR
(
  uniqueid     NUMBER(20),
  code         VARCHAR2(10),
  name         VARCHAR2(50),
  external_uid VARCHAR2(40),
  session_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_MINOR
  add constraint PK_POS_MINOR primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_MINOR
  add constraint FK_POS_MINOR_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.POS_MINOR
  add constraint NN_POS_MINOR_CODE
  check ("CODE" IS NOT NULL);
alter table timetable.POS_MINOR
  add constraint NN_POS_MINOR_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.POS_MINOR
  add constraint NN_POS_MINOR_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.POS_MINOR
  add constraint NN_POS_MINOR_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table POS_ACAD_AREA_MINOR
prompt ==================================
prompt
create table timetable.POS_ACAD_AREA_MINOR
(
  academic_area_id NUMBER(20),
  minor_id         NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_ACAD_AREA_MINOR
  add constraint PK_POS_ACAD_AREA_MINOR primary key (ACADEMIC_AREA_ID, MINOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_ACAD_AREA_MINOR
  add constraint FK_POS_ACAD_AREA_MINOR_AREA foreign key (ACADEMIC_AREA_ID)
  references timetable.ACADEMIC_AREA (UNIQUEID) on delete cascade;
alter table timetable.POS_ACAD_AREA_MINOR
  add constraint FK_POS_ACAD_AREA_MINOR_MINOR foreign key (MINOR_ID)
  references timetable.POS_MINOR (UNIQUEID) on delete cascade;
alter table timetable.POS_ACAD_AREA_MINOR
  add constraint NN_POS_ACAD_AREA_MINOR_AREA
  check ("ACADEMIC_AREA_ID" IS NOT NULL);
alter table timetable.POS_ACAD_AREA_MINOR
  add constraint NN_POS_ACAD_AREA_MINOR_MINOR
  check ("MINOR_ID" IS NOT NULL);

prompt
prompt Creating table POS_RESERVATION
prompt ==============================
prompt
create table timetable.POS_RESERVATION
(
  uniqueid             NUMBER(20),
  owner                NUMBER(20),
  reservation_type     NUMBER(20),
  acad_classification  NUMBER(20),
  pos_major            NUMBER(20),
  priority             NUMBER(5),
  reserved             NUMBER(10),
  prior_enrollment     NUMBER(10),
  projected_enrollment NUMBER(10),
  owner_class_id       VARCHAR2(1),
  requested            NUMBER(10),
  last_modified_time   TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_RESERVATION
  add constraint PK_POS_RESV primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.POS_RESERVATION
  add constraint FK_POS_RESV_ACAD_CLASS foreign key (ACAD_CLASSIFICATION)
  references timetable.ACADEMIC_CLASSIFICATION (UNIQUEID) on delete cascade;
alter table timetable.POS_RESERVATION
  add constraint FK_POS_RESV_MAJOR foreign key (POS_MAJOR)
  references timetable.POS_MAJOR (UNIQUEID) on delete cascade;
alter table timetable.POS_RESERVATION
  add constraint FK_POS_RESV_TYPE foreign key (RESERVATION_TYPE)
  references timetable.RESERVATION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESC_RESERVED
  check ("RESERVED" IS NOT NULL);
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESV_OWNER
  check ("OWNER" IS NOT NULL);
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESV_OWNER_CLS_ID
  check ("OWNER_CLASS_ID" IS NOT NULL);
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESV_POS_MAJOR
  check ("POS_MAJOR" IS NOT NULL);
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESV_PRIORITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESV_RESERV_TYPE
  check ("RESERVATION_TYPE" IS NOT NULL);
alter table timetable.POS_RESERVATION
  add constraint NN_POS_RESV_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_POS_RESV_ACAD_CLASS on timetable.POS_RESERVATION (ACAD_CLASSIFICATION)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_POS_RESV_MAJOR on timetable.POS_RESERVATION (POS_MAJOR)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_POS_RESV_OWNER on timetable.POS_RESERVATION (OWNER)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_POS_RESV_OWNER_CLS on timetable.POS_RESERVATION (OWNER_CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_POS_RESV_TYPE on timetable.POS_RESERVATION (RESERVATION_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table RELATED_COURSE_INFO
prompt ==================================
prompt
create table timetable.RELATED_COURSE_INFO
(
  uniqueid   NUMBER(20),
  event_id   NUMBER(20),
  owner_id   NUMBER(20),
  owner_type NUMBER(10),
  course_id  NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.RELATED_COURSE_INFO
  add constraint PK_RELATED_CRS_INFO primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.RELATED_COURSE_INFO
  add constraint FK_EVENT_OWNER_COURSE foreign key (COURSE_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.RELATED_COURSE_INFO
  add constraint FK_EVENT_OWNER_EVENT foreign key (EVENT_ID)
  references timetable.EVENT (UNIQUEID) on delete cascade;
alter table timetable.RELATED_COURSE_INFO
  add constraint NN_REL_CRS_INFO_COURSE_ID
  check ("COURSE_ID" IS NOT NULL);
alter table timetable.RELATED_COURSE_INFO
  add constraint NN_REL_CRS_INFO_EVENT_ID
  check ("EVENT_ID" IS NOT NULL);
alter table timetable.RELATED_COURSE_INFO
  add constraint NN_REL_CRS_INFO_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.RELATED_COURSE_INFO
  add constraint NN_REL_CRS_INFO_OWNER_TYPE
  check ("OWNER_TYPE" IS NOT NULL);
alter table timetable.RELATED_COURSE_INFO
  add constraint NN_REL_CRS_INFO_UNIQUE_ID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_EVENT_OWNER_EVENT on timetable.RELATED_COURSE_INFO (EVENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_EVENT_OWNER_OWNER on timetable.RELATED_COURSE_INFO (OWNER_ID, OWNER_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROLES
prompt ====================
prompt
create table timetable.ROLES
(
  role_id   NUMBER(20),
  reference VARCHAR2(20),
  abbv      VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROLES
  add constraint PK_ROLES primary key (ROLE_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROLES
  add constraint UK_ROLES_ABBV unique (ABBV)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROLES
  add constraint UK_ROLES_REFERENCE unique (REFERENCE)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROLES
  add constraint NN_ROLES_ABBV
  check ("ABBV" IS NOT NULL);
alter table timetable.ROLES
  add constraint NN_ROLES_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.ROLES
  add constraint NN_ROLES_ROLE_ID
  check ("ROLE_ID" IS NOT NULL);

prompt
prompt Creating table ROOM
prompt ===================
prompt
create table timetable.ROOM
(
  uniqueid          NUMBER(20),
  external_uid      VARCHAR2(40),
  session_id        NUMBER(20),
  building_id       NUMBER(20),
  room_number       VARCHAR2(10),
  capacity          NUMBER(10),
  coordinate_x      FLOAT,
  coordinate_y      FLOAT,
  ignore_too_far    NUMBER(1),
  manager_ids       VARCHAR2(200),
  pattern           VARCHAR2(350),
  ignore_room_check NUMBER(1) default (0),
  classification    VARCHAR2(20),
  display_name      VARCHAR2(100),
  exam_capacity     NUMBER(10) default 0,
  permanent_id      NUMBER(20) not null,
  exam_type         NUMBER(10) default 0,
  room_type         NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM
  add constraint PK_ROOM primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM
  add constraint UK_ROOM unique (SESSION_ID, BUILDING_ID, ROOM_NUMBER)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM
  add constraint FK_ROOM_BUILDING foreign key (BUILDING_ID)
  references timetable.BUILDING (UNIQUEID) on delete cascade;
alter table timetable.ROOM
  add constraint FK_ROOM_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.ROOM
  add constraint FK_ROOM_TYPE foreign key (ROOM_TYPE)
  references timetable.ROOM_TYPE (UNIQUEID) on delete cascade;
alter table timetable.ROOM
  add constraint NN_ROOM_BUILDING_ID
  check ("BUILDING_ID" IS NOT NULL);
alter table timetable.ROOM
  add constraint NN_ROOM_CAPACITY
  check ("CAPACITY" IS NOT NULL);
alter table timetable.ROOM
  add constraint NN_ROOM_CONSTRAINT
  check ("IGNORE_ROOM_CHECK" IS NOT NULL);
alter table timetable.ROOM
  add constraint NN_ROOM_IGNORE_TOO_FAR
  check ("IGNORE_TOO_FAR" IS NOT NULL);
alter table timetable.ROOM
  add constraint NN_ROOM_ROOM_NUMBER
  check ("ROOM_NUMBER" IS NOT NULL);
alter table timetable.ROOM
  add constraint NN_ROOM_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.ROOM
  add constraint NN_ROOM_TYPE
  check (room_type is not null);
alter table timetable.ROOM
  add constraint NN_ROOM_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_BUILDING on timetable.ROOM (BUILDING_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_PERMID on timetable.ROOM (PERMANENT_ID, SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_DEPT
prompt ========================
prompt
create table timetable.ROOM_DEPT
(
  uniqueid      NUMBER(20),
  room_id       NUMBER(20),
  department_id NUMBER(20),
  is_control    NUMBER(1) default 0
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_DEPT
  add constraint PK_ROOM_DEPT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_DEPT
  add constraint FK_ROOM_DEPT_DEPT foreign key (DEPARTMENT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.ROOM_DEPT
  add constraint NN_ROOM_DEPT_CONTROL
  check ("IS_CONTROL" IS NOT NULL);
alter table timetable.ROOM_DEPT
  add constraint NN_ROOM_DEPT_DEPARTMENT_ID
  check ("DEPARTMENT_ID" IS NOT NULL);
alter table timetable.ROOM_DEPT
  add constraint NN_ROOM_DEPT_ROOM_ID
  check ("ROOM_ID" IS NOT NULL);
alter table timetable.ROOM_DEPT
  add constraint NN_ROOM_DEPT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_DEPT_DEPT on timetable.ROOM_DEPT (DEPARTMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_DEPT_ROOM on timetable.ROOM_DEPT (ROOM_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_FEATURE
prompt ===========================
prompt
create table timetable.ROOM_FEATURE
(
  uniqueid      NUMBER(20),
  discriminator VARCHAR2(10),
  label         VARCHAR2(20),
  sis_reference VARCHAR2(20),
  sis_value     VARCHAR2(20),
  department_id NUMBER(20),
  abbv          VARCHAR2(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_FEATURE
  add constraint PK_ROOM_FEATURE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_FEATURE
  add constraint FK_ROOM_FEATURE_DEPT foreign key (DEPARTMENT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.ROOM_FEATURE
  add constraint NN_ROOM_FEATURE_DISCRIMINATOR
  check ("DISCRIMINATOR" IS NOT NULL);
alter table timetable.ROOM_FEATURE
  add constraint NN_ROOM_FEATURE_LABEL
  check ("LABEL" IS NOT NULL);
alter table timetable.ROOM_FEATURE
  add constraint NN_ROOM_FEATURE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_FEATURE_DEPT on timetable.ROOM_FEATURE (DEPARTMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_FEATURE_PREF
prompt ================================
prompt
create table timetable.ROOM_FEATURE_PREF
(
  uniqueid           NUMBER(20),
  owner_id           NUMBER(20),
  pref_level_id      NUMBER(20),
  room_feature_id    NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_FEATURE_PREF
  add constraint PK_ROOM_FEAT_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_FEATURE_PREF
  add constraint FK_ROOM_FEAT_PREF_LEVEL foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.ROOM_FEATURE_PREF
  add constraint FK_ROOM_FEAT_PREF_ROOM_FEAT foreign key (ROOM_FEATURE_ID)
  references timetable.ROOM_FEATURE (UNIQUEID) on delete cascade;
alter table timetable.ROOM_FEATURE_PREF
  add constraint NN_ROOM_FEAT_PREF_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.ROOM_FEATURE_PREF
  add constraint NN_ROOM_FEAT_PREF_ROOM_FEAT_ID
  check ("ROOM_FEATURE_ID" IS NOT NULL);
alter table timetable.ROOM_FEATURE_PREF
  add constraint NN_ROOM_FEAT_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_FEAT_PREF_LEVEL on timetable.ROOM_FEATURE_PREF (PREF_LEVEL_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_FEAT_PREF_OWNER on timetable.ROOM_FEATURE_PREF (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_FEAT_PREF_ROOM_FEAT on timetable.ROOM_FEATURE_PREF (ROOM_FEATURE_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_GROUP
prompt =========================
prompt
create table timetable.ROOM_GROUP
(
  uniqueid      NUMBER(20),
  session_id    NUMBER(20),
  name          VARCHAR2(20),
  description   VARCHAR2(200),
  global        NUMBER(1),
  default_group NUMBER(1),
  department_id NUMBER(20),
  abbv          VARCHAR2(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_GROUP
  add constraint PK_ROOM_GROUP_UID primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_GROUP
  add constraint FK_ROOM_GROUP_DEPT foreign key (DEPARTMENT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.ROOM_GROUP
  add constraint FK_ROOM_GROUP_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.ROOM_GROUP
  add constraint NN_ROOM_GROUP_DEFAULT_GROUP
  check ("DEFAULT_GROUP" IS NOT NULL);
alter table timetable.ROOM_GROUP
  add constraint NN_ROOM_GROUP_GLOBAL
  check ("GLOBAL" IS NOT NULL);
alter table timetable.ROOM_GROUP
  add constraint NN_ROOM_GROUP_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.ROOM_GROUP
  add constraint NN_ROOM_GROUP_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.ROOM_GROUP
  add constraint NN_ROOM_GROUP_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_GROUP_DEPT on timetable.ROOM_GROUP (DEPARTMENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_GROUP_SESSION on timetable.ROOM_GROUP (SESSION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_GROUP_PREF
prompt ==============================
prompt
create table timetable.ROOM_GROUP_PREF
(
  uniqueid           NUMBER(20),
  owner_id           NUMBER(20),
  pref_level_id      NUMBER(20),
  room_group_id      NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_GROUP_PREF
  add constraint PK_ROOM_GROUP_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_GROUP_PREF
  add constraint FK_ROOM_GROUP_PREF_LEVEL foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.ROOM_GROUP_PREF
  add constraint FK_ROOM_GROUP_PREF_ROOM_GRP foreign key (ROOM_GROUP_ID)
  references timetable.ROOM_GROUP (UNIQUEID) on delete cascade;
alter table timetable.ROOM_GROUP_PREF
  add constraint NN_ROOM_GROUP_PREF_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.ROOM_GROUP_PREF
  add constraint NN_ROOM_GROUP_PREF_ROOM_GRP_ID
  check ("ROOM_GROUP_ID" IS NOT NULL);
alter table timetable.ROOM_GROUP_PREF
  add constraint NN_ROOM_GROUP_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_GROUP_PREF_LEVEL on timetable.ROOM_GROUP_PREF (PREF_LEVEL_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_GROUP_PREF_OWNER on timetable.ROOM_GROUP_PREF (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_GROUP_PREF_ROOM_GRP on timetable.ROOM_GROUP_PREF (ROOM_GROUP_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_GROUP_ROOM
prompt ==============================
prompt
create table timetable.ROOM_GROUP_ROOM
(
  room_group_id NUMBER(20),
  room_id       NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_GROUP_ROOM
  add constraint PK_ROOM_GROUP_ROOM primary key (ROOM_GROUP_ID, ROOM_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_GROUP_ROOM
  add constraint FK_ROOM_GROUP_ROOM_ROOM_GRP foreign key (ROOM_GROUP_ID)
  references timetable.ROOM_GROUP (UNIQUEID) on delete cascade;
alter table timetable.ROOM_GROUP_ROOM
  add constraint NN_ROOM_GROUP_ROOM_ROOM_GRP_ID
  check ("ROOM_GROUP_ID" IS NOT NULL);
alter table timetable.ROOM_GROUP_ROOM
  add constraint NN_ROOM_GROUP_ROOM_ROOM_ID
  check ("ROOM_ID" IS NOT NULL);

prompt
prompt Creating table ROOM_JOIN_ROOM_FEATURE
prompt =====================================
prompt
create table timetable.ROOM_JOIN_ROOM_FEATURE
(
  room_id    NUMBER(20),
  feature_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_JOIN_ROOM_FEATURE
  add constraint UK_ROOM_JOIN_ROOM_FEAT_RM_FEAT unique (ROOM_ID, FEATURE_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_JOIN_ROOM_FEATURE
  add constraint FK_ROOM_JOIN_ROOM_FEAT_RM_FEAT foreign key (FEATURE_ID)
  references timetable.ROOM_FEATURE (UNIQUEID) on delete cascade;
alter table timetable.ROOM_JOIN_ROOM_FEATURE
  add constraint NN_ROOM_JOIN_ROOM_FEAT_FEAT_ID
  check ("FEATURE_ID" IS NOT NULL);
alter table timetable.ROOM_JOIN_ROOM_FEATURE
  add constraint NN_ROOM_JOIN_ROOM_FEAT_ROOM_ID
  check ("ROOM_ID" IS NOT NULL);

prompt
prompt Creating table ROOM_PREF
prompt ========================
prompt
create table timetable.ROOM_PREF
(
  uniqueid           NUMBER(20),
  owner_id           NUMBER(20),
  pref_level_id      NUMBER(20),
  room_id            NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_PREF
  add constraint PK_ROOM_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_PREF
  add constraint FK_ROOM_PREF_LEVEL foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.ROOM_PREF
  add constraint NN_ROOM_PREF_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.ROOM_PREF
  add constraint NN_ROOM_PREF_ROOM_ID
  check ("ROOM_ID" IS NOT NULL);
alter table timetable.ROOM_PREF
  add constraint NN_ROOM_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_ROOM_PREF_LEVEL on timetable.ROOM_PREF (PREF_LEVEL_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_ROOM_PREF_OWNER on timetable.ROOM_PREF (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table ROOM_TYPE_OPTION
prompt ===============================
prompt
create table timetable.ROOM_TYPE_OPTION
(
  room_type  NUMBER(20),
  session_id NUMBER(20),
  status     NUMBER(10),
  message    VARCHAR2(200)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_TYPE_OPTION
  add constraint PK_ROOM_TYPE_OPTION primary key (ROOM_TYPE, SESSION_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.ROOM_TYPE_OPTION
  add constraint FK_RTYPE_OPTION_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.ROOM_TYPE_OPTION
  add constraint FK_RTYPE_OPTION_TYPE foreign key (ROOM_TYPE)
  references timetable.ROOM_TYPE (UNIQUEID) on delete cascade;
alter table timetable.ROOM_TYPE_OPTION
  add constraint NN_RTYPE_OPT_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.ROOM_TYPE_OPTION
  add constraint NN_RTYPE_OPT_STATUS
  check ("STATUS" IS NOT NULL);
alter table timetable.ROOM_TYPE_OPTION
  add constraint NN_RTYPE_OPT_TYPE
  check ("ROOM_TYPE" IS NOT NULL);

prompt
prompt Creating table SECTIONING_INFO
prompt ==============================
prompt
create table timetable.SECTIONING_INFO
(
  uniqueid          NUMBER(20),
  class_id          NUMBER(20),
  nbr_exp_students  FLOAT,
  nbr_hold_students FLOAT
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SECTIONING_INFO
  add constraint PK_SECTIONING_INFO primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SECTIONING_INFO
  add constraint FK_SECTIONING_INFO_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.SECTIONING_INFO
  add constraint NN_SECTIONING_INFO_CLASS
  check ("CLASS_ID" IS NOT NULL);
alter table timetable.SECTIONING_INFO
  add constraint NN_SECTIONING_INFO_EXP
  check ("NBR_EXP_STUDENTS" IS NOT NULL);
alter table timetable.SECTIONING_INFO
  add constraint NN_SECTIONING_INFO_HOLD
  check ("NBR_HOLD_STUDENTS" IS NOT NULL);
alter table timetable.SECTIONING_INFO
  add constraint NN_SECTIONING_INFO_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table SECTIONING_QUEUE
prompt ===============================
prompt
create table timetable.SECTIONING_QUEUE
(
  uniqueid   NUMBER(20),
  session_id NUMBER(20),
  type       NUMBER(10),
  time_stamp TIMESTAMP(6),
  message    CLOB
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SECTIONING_QUEUE
  add constraint PK_SECT_QUEUE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SECTIONING_QUEUE
  add constraint NN_SECT_QUEUE_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.SECTIONING_QUEUE
  add constraint NN_SECT_QUEUE_TS
  check ("TIME_STAMP" IS NOT NULL);
alter table timetable.SECTIONING_QUEUE
  add constraint NN_SECT_QUEUE_TYPE
  check ("TYPE" IS NOT NULL);
alter table timetable.SECTIONING_QUEUE
  add constraint NN_SECT_QUEUE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_SECT_QUEUE_SESSION_TS on timetable.SECTIONING_QUEUE (SESSION_ID, TIME_STAMP)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SOLVER_GR_TO_TT_MGR
prompt ==================================
prompt
create table timetable.SOLVER_GR_TO_TT_MGR
(
  solver_group_id  NUMBER(20),
  timetable_mgr_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_GR_TO_TT_MGR
  add constraint PK_SOLVER_GR_TO_TT_MGR primary key (SOLVER_GROUP_ID, TIMETABLE_MGR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_GR_TO_TT_MGR
  add constraint FK_SOLVER_GR_TO_TT_MGR_SOLVGRP foreign key (SOLVER_GROUP_ID)
  references timetable.SOLVER_GROUP (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_GR_TO_TT_MGR
  add constraint FK_SOLVER_GR_TO_TT_MGR_TT_MGR foreign key (TIMETABLE_MGR_ID)
  references timetable.TIMETABLE_MANAGER (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_GR_TO_TT_MGR
  add constraint NN_SOLVER_GR_TO_TT_MGR_SOLVGRP
  check ("SOLVER_GROUP_ID" IS NOT NULL);
alter table timetable.SOLVER_GR_TO_TT_MGR
  add constraint NN_SOLVER_GR_TO_TT_MGR_TT_MGR
  check ("TIMETABLE_MGR_ID" IS NOT NULL);

prompt
prompt Creating table SOLVER_PARAMETER_GROUP
prompt =====================================
prompt
create table timetable.SOLVER_PARAMETER_GROUP
(
  uniqueid    NUMBER(20),
  name        VARCHAR2(100),
  description VARCHAR2(1000),
  condition   VARCHAR2(250),
  ord         NUMBER(10),
  param_type  NUMBER(10) default 0
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PARAMETER_GROUP
  add constraint PK_SOLVER_PARAM_GROUP primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PARAMETER_GROUP
  add constraint NN_SOLVER_PARAM_GROUP_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table SOLVER_PARAMETER_DEF
prompt ===================================
prompt
create table timetable.SOLVER_PARAMETER_DEF
(
  uniqueid              NUMBER(20),
  name                  VARCHAR2(100),
  default_value         VARCHAR2(2048),
  description           VARCHAR2(1000),
  type                  VARCHAR2(250),
  ord                   NUMBER(10),
  visible               NUMBER(1),
  solver_param_group_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PARAMETER_DEF
  add constraint PK_SOLV_PARAM_DEF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PARAMETER_DEF
  add constraint FK_SOLV_PARAM_DEF_SOLV_PAR_GRP foreign key (SOLVER_PARAM_GROUP_ID)
  references timetable.SOLVER_PARAMETER_GROUP (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_PARAMETER_DEF
  add constraint NN_SOLV_PARAM_DEF_SOLV_PAR_GRP
  check ("SOLVER_PARAM_GROUP_ID" IS NOT NULL);
alter table timetable.SOLVER_PARAMETER_DEF
  add constraint NN_SOLV_PARAM_DEF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_SOLV_PARAM_DEF_GR on timetable.SOLVER_PARAMETER_DEF (SOLVER_PARAM_GROUP_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table SOLVER_PREDEF_SETTING
prompt ====================================
prompt
create table timetable.SOLVER_PREDEF_SETTING
(
  uniqueid    NUMBER(20),
  name        VARCHAR2(100),
  description VARCHAR2(1000),
  appearance  NUMBER(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PREDEF_SETTING
  add constraint PK_SOLV_PREDEF_SETTG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PREDEF_SETTING
  add constraint NN_SOLV_PREDEF_SETTG_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table SOLVER_PARAMETER
prompt ===============================
prompt
create table timetable.SOLVER_PARAMETER
(
  uniqueid                 NUMBER(20),
  value                    VARCHAR2(2048),
  solver_param_def_id      NUMBER(20),
  solution_id              NUMBER(20),
  solver_predef_setting_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.SOLVER_PARAMETER
  add constraint FK_SOLVER_PARAM_DEF foreign key (SOLVER_PARAM_DEF_ID)
  references timetable.SOLVER_PARAMETER_DEF (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_PARAMETER
  add constraint FK_SOLVER_PARAM_PREDEF_STG foreign key (SOLVER_PREDEF_SETTING_ID)
  references timetable.SOLVER_PREDEF_SETTING (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_PARAMETER
  add constraint FK_SOLVER_PARAM_SOLUTION foreign key (SOLUTION_ID)
  references timetable.SOLUTION (UNIQUEID) on delete cascade;
alter table timetable.SOLVER_PARAMETER
  add constraint NN_SOLVER_PARAM_SOLV_PARAM_DEF
  check ("SOLVER_PARAM_DEF_ID" IS NOT NULL);
alter table timetable.SOLVER_PARAMETER
  add constraint NN_SOLVER_PARAM_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_SOLVER_PARAM_DEF on timetable.SOLVER_PARAMETER (SOLVER_PARAM_DEF_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SOLVER_PARAM_PREDEF on timetable.SOLVER_PARAMETER (SOLVER_PREDEF_SETTING_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_SOLVER_PARAM_SOLUTION on timetable.SOLVER_PARAMETER (SOLUTION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STAFF
prompt ====================
prompt
create table timetable.STAFF
(
  uniqueid     NUMBER(20),
  external_uid VARCHAR2(40),
  fname        VARCHAR2(100),
  mname        VARCHAR2(100),
  lname        VARCHAR2(100),
  pos_code     VARCHAR2(20),
  dept         VARCHAR2(50),
  email        VARCHAR2(200)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STAFF
  add constraint PK_STAFF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STAFF
  add constraint NN_STAFF_LNAME
  check ("LNAME" IS NOT NULL);
alter table timetable.STAFF
  add constraint NN_STAFF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table STANDARD_EVENT_NOTE
prompt ==================================
prompt
create table timetable.STANDARD_EVENT_NOTE
(
  uniqueid  NUMBER(20),
  reference VARCHAR2(20),
  note      VARCHAR2(1000)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STANDARD_EVENT_NOTE
  add constraint PK_STANDARD_EVENT_NOTE primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STANDARD_EVENT_NOTE
  add constraint NN_STD_EVENT_NOTE_NOTE
  check ("NOTE" IS NOT NULL);
alter table timetable.STANDARD_EVENT_NOTE
  add constraint NN_STD_EVENT_NOTE_REFERENCE
  check ("REFERENCE" IS NOT NULL);
alter table timetable.STANDARD_EVENT_NOTE
  add constraint NN_STD_EVENT_NOTE_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table STUDENT_ACAD_AREA
prompt ================================
prompt
create table timetable.STUDENT_ACAD_AREA
(
  uniqueid      NUMBER(20),
  student_id    NUMBER(20),
  acad_clasf_id NUMBER(20),
  acad_area_id  NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ACAD_AREA
  add constraint PK_STUDENT_ACAD_AREA primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ACAD_AREA
  add constraint FK_STUDENT_ACAD_AREA_AREA foreign key (ACAD_AREA_ID)
  references timetable.ACADEMIC_AREA (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ACAD_AREA
  add constraint FK_STUDENT_ACAD_AREA_CLASF foreign key (ACAD_CLASF_ID)
  references timetable.ACADEMIC_CLASSIFICATION (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ACAD_AREA
  add constraint FK_STUDENT_ACAD_AREA_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ACAD_AREA
  add constraint NN_ACAD_AREA_AREA
  check ("ACAD_AREA_ID" IS NOT NULL);
alter table timetable.STUDENT_ACAD_AREA
  add constraint NN_ACAD_AREA_CLASF
  check ("ACAD_CLASF_ID" IS NOT NULL);
alter table timetable.STUDENT_ACAD_AREA
  add constraint NN_ACAD_AREA_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.STUDENT_ACAD_AREA
  add constraint NN_ACAD_AREA_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STUDENT_ACAD_AREA on timetable.STUDENT_ACAD_AREA (STUDENT_ID, ACAD_AREA_ID, ACAD_CLASF_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create unique index timetable.UK_STUDENT_ACAD_AREA on timetable.STUDENT_ACAD_AREA (STUDENT_ID, ACAD_CLASF_ID, ACAD_AREA_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_ACCOMODATION
prompt ===================================
prompt
create table timetable.STUDENT_ACCOMODATION
(
  uniqueid     NUMBER(20),
  name         VARCHAR2(50),
  abbreviation VARCHAR2(20),
  external_uid VARCHAR2(40),
  session_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ACCOMODATION
  add constraint PK_STUDENT_ACCOMODATION primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ACCOMODATION
  add constraint FK_STUDENT_ACCOM_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ACCOMODATION
  add constraint NN_STUDENT_ACCOM_ABBV
  check ("ABBREVIATION" IS NOT NULL);
alter table timetable.STUDENT_ACCOMODATION
  add constraint NN_STUDENT_ACCOM_NAME
  check ("NAME" IS NOT NULL);
alter table timetable.STUDENT_ACCOMODATION
  add constraint NN_STUDENT_ACCOM_SESSION
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.STUDENT_ACCOMODATION
  add constraint NN_STUDENT_ACCOM_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table STUDENT_CLASS_ENRL
prompt =================================
prompt
create table timetable.STUDENT_CLASS_ENRL
(
  uniqueid           NUMBER(20),
  student_id         NUMBER(20),
  course_request_id  NUMBER(20),
  class_id           NUMBER(20),
  timestamp          DATE,
  course_offering_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_CLASS_ENRL
  add constraint PK_STUDENT_CLASS_ENRL primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_CLASS_ENRL
  add constraint FK_STUDENT_CLASS_ENRL_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_CLASS_ENRL
  add constraint FK_STUDENT_CLASS_ENRL_COURSE foreign key (COURSE_OFFERING_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_CLASS_ENRL
  add constraint FK_STUDENT_CLASS_ENRL_REQUEST foreign key (COURSE_REQUEST_ID)
  references timetable.COURSE_REQUEST (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_CLASS_ENRL
  add constraint FK_STUDENT_CLASS_ENRL_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_CLASS_ENRL
  add constraint NN_STUDENT_CLASS_ENRL_CLASS
  check ("CLASS_ID" IS NOT NULL);
alter table timetable.STUDENT_CLASS_ENRL
  add constraint NN_STUDENT_CLASS_ENRL_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.STUDENT_CLASS_ENRL
  add constraint NN_STUDENT_CLASS_ENRL_TIMESTMP
  check ("TIMESTAMP" IS NOT NULL);
alter table timetable.STUDENT_CLASS_ENRL
  add constraint NN_STUDENT_CLASS_ENRL_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STUDENT_CLASS_ENRL_CLASS on timetable.STUDENT_CLASS_ENRL (CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STUDENT_CLASS_ENRL_COURSE on timetable.STUDENT_CLASS_ENRL (COURSE_OFFERING_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STUDENT_CLASS_ENRL_REQ on timetable.STUDENT_CLASS_ENRL (COURSE_REQUEST_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STUDENT_CLASS_ENRL_STUDENT on timetable.STUDENT_CLASS_ENRL (STUDENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_ENRL
prompt ===========================
prompt
create table timetable.STUDENT_ENRL
(
  uniqueid           NUMBER(20),
  student_id         NUMBER(20),
  solution_id        NUMBER(20),
  class_id           NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ENRL
  add constraint PK_STUDENT_ENRL primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ENRL
  add constraint FK_STUDENT_ENRL_CLASS foreign key (CLASS_ID)
  references timetable.CLASS_ (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ENRL
  add constraint FK_STUDENT_ENRL_SOLUTION foreign key (SOLUTION_ID)
  references timetable.SOLUTION (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ENRL
  add constraint NN_STUDENT_ENRL_CLASS_ID
  check ("CLASS_ID" IS NOT NULL);
alter table timetable.STUDENT_ENRL
  add constraint NN_STUDENT_ENRL_SOLUTION_ID
  check ("SOLUTION_ID" IS NOT NULL);
alter table timetable.STUDENT_ENRL
  add constraint NN_STUDENT_ENRL_STUDENT_ID
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.STUDENT_ENRL
  add constraint NN_STUDENT_ENRL_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STUDENT_ENRL on timetable.STUDENT_ENRL (SOLUTION_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STUDENT_ENRL_ASSIGNMENT on timetable.STUDENT_ENRL (SOLUTION_ID, CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STUDENT_ENRL_CLASS on timetable.STUDENT_ENRL (CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_ENRL_MSG
prompt ===============================
prompt
create table timetable.STUDENT_ENRL_MSG
(
  uniqueid         NUMBER(20),
  message          VARCHAR2(255),
  msg_level        NUMBER(10) default (0),
  type             NUMBER(10) default (0),
  timestamp        DATE,
  course_demand_id NUMBER(20),
  ord              NUMBER(10)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ENRL_MSG
  add constraint PK_STUDENT_ENRL_MSG primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_ENRL_MSG
  add constraint FK_STUDENT_ENRL_MSG_DEMAND foreign key (COURSE_DEMAND_ID)
  references timetable.COURSE_DEMAND (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_DEMAND
  check ("COURSE_DEMAND_ID" IS NOT NULL);
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_LEV
  check ("MSG_LEVEL" IS NOT NULL);
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_MSG
  check ("MESSAGE" IS NOT NULL);
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_ORDER
  check ("ORD" IS NOT NULL);
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_TS
  check ("TIMESTAMP" IS NOT NULL);
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_TYPE
  check ("TYPE" IS NOT NULL);
alter table timetable.STUDENT_ENRL_MSG
  add constraint NN_STUDENT_ENRL_MSG_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STUDENT_ENRL_MSG_DEM on timetable.STUDENT_ENRL_MSG (COURSE_DEMAND_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_GROUP
prompt ============================
prompt
create table timetable.STUDENT_GROUP
(
  uniqueid           NUMBER(20),
  session_id         NUMBER(20),
  group_abbreviation VARCHAR2(30),
  group_name         VARCHAR2(90),
  external_uid       VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_GROUP
  add constraint PK_STUDENT_GROUP primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_GROUP
  add constraint UK_STUDENT_GROUP_SESSION_SIS unique (SESSION_ID, GROUP_ABBREVIATION)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_GROUP
  add constraint FK_STUDENT_GROUP_SESSION foreign key (SESSION_ID)
  references timetable.SESSIONS (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_GROUP
  add constraint NN_STUDENT_GROUP_GROUP_ABBREVI
  check ("GROUP_ABBREVIATION" IS NOT NULL);
alter table timetable.STUDENT_GROUP
  add constraint NN_STUDENT_GROUP_GROUP_NAME
  check ("GROUP_NAME" IS NOT NULL);
alter table timetable.STUDENT_GROUP
  add constraint NN_STUDENT_GROUP_SESSION_ID
  check ("SESSION_ID" IS NOT NULL);
alter table timetable.STUDENT_GROUP
  add constraint NN_STUDENT_GROUP_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table STUDENT_GROUP_RESERVATION
prompt ========================================
prompt
create table timetable.STUDENT_GROUP_RESERVATION
(
  uniqueid             NUMBER(20),
  owner                NUMBER(20),
  reservation_type     NUMBER(20),
  student_group        NUMBER(20),
  priority             NUMBER(5),
  reserved             NUMBER(10),
  prior_enrollment     NUMBER(10),
  projected_enrollment NUMBER(10),
  owner_class_id       VARCHAR2(1),
  requested            NUMBER(10),
  last_modified_time   TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint PK_STU_GRP_RESV primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint FK_STU_GRP_RESV_RESERV_TYPE foreign key (RESERVATION_TYPE)
  references timetable.RESERVATION_TYPE (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint FK_STU_GRP_RESV_STU_GRP foreign key (STUDENT_GROUP)
  references timetable.STUDENT_GROUP (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_OWNER
  check ("OWNER" IS NOT NULL);
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_OWNER_CLS_ID
  check ("OWNER_CLASS_ID" IS NOT NULL);
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_PRIORITY
  check ("PRIORITY" IS NOT NULL);
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_RESERVED
  check ("RESERVED" IS NOT NULL);
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_RESERV_TYPE
  check ("RESERVATION_TYPE" IS NOT NULL);
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_STU_GRP
  check ("STUDENT_GROUP" IS NOT NULL);
alter table timetable.STUDENT_GROUP_RESERVATION
  add constraint NN_STU_GRP_RESV_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STU_GRP_RESV_OWNER on timetable.STUDENT_GROUP_RESERVATION (OWNER)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STU_GRP_RESV_OWNER_CLS on timetable.STUDENT_GROUP_RESERVATION (OWNER_CLASS_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STU_GRP_RESV_STUDENT_GROUP on timetable.STUDENT_GROUP_RESERVATION (STUDENT_GROUP)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_STU_GRP_RESV_TYPE on timetable.STUDENT_GROUP_RESERVATION (RESERVATION_TYPE)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_MAJOR
prompt ============================
prompt
create table timetable.STUDENT_MAJOR
(
  student_id NUMBER(20),
  major_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_MAJOR
  add constraint PK_STUDENT_MAJOR primary key (STUDENT_ID, MAJOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_MAJOR
  add constraint FK_STUDENT_MAJOR_MAJOR foreign key (MAJOR_ID)
  references timetable.POS_MAJOR (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_MAJOR
  add constraint FK_STUDENT_MAJOR_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_MAJOR
  add constraint NN_STUDENT_MAJOR_MAJOR
  check ("MAJOR_ID" IS NOT NULL);
alter table timetable.STUDENT_MAJOR
  add constraint NN_STUDENT_MAJOR_STUDENT
  check ("STUDENT_ID" IS NOT NULL);

prompt
prompt Creating table STUDENT_MINOR
prompt ============================
prompt
create table timetable.STUDENT_MINOR
(
  student_id NUMBER(20),
  minor_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_MINOR
  add constraint PK_STUDENT_MINOR primary key (STUDENT_ID, MINOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_MINOR
  add constraint FK_STUDENT_MINOR_MINOR foreign key (MINOR_ID)
  references timetable.POS_MINOR (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_MINOR
  add constraint FK_STUDENT_MINOR_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_MINOR
  add constraint NN_STUDENT_MINOR_MINOR
  check ("MINOR_ID" IS NOT NULL);
alter table timetable.STUDENT_MINOR
  add constraint NN_STUDENT_MINOR_STUDENT
  check ("STUDENT_ID" IS NOT NULL);

prompt
prompt Creating table STUDENT_SECT_HIST
prompt ================================
prompt
create table timetable.STUDENT_SECT_HIST
(
  uniqueid   NUMBER(20),
  student_id NUMBER(20),
  data       BLOB,
  type       NUMBER(10),
  timestamp  DATE
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_SECT_HIST
  add constraint PK_STUDENT_SECT_HIST primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_SECT_HIST
  add constraint FK_STUDENT_SECT_HIST_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_SECT_HIST
  add constraint NN_STUDENT_SECTH_DATA
  check ("DATA" IS NOT NULL);
alter table timetable.STUDENT_SECT_HIST
  add constraint NN_STUDENT_SECTH_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.STUDENT_SECT_HIST
  add constraint NN_STUDENT_SECTH_TS
  check ("TIMESTAMP" IS NOT NULL);
alter table timetable.STUDENT_SECT_HIST
  add constraint NN_STUDENT_SECTH_TYPE
  check ("TYPE" IS NOT NULL);
alter table timetable.STUDENT_SECT_HIST
  add constraint NN_STUDENT_SECTH_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_STUDENT_SECT_HIST_STUDENT on timetable.STUDENT_SECT_HIST (STUDENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table STUDENT_TO_ACOMODATION
prompt =====================================
prompt
create table timetable.STUDENT_TO_ACOMODATION
(
  student_id      NUMBER(20),
  accomodation_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_TO_ACOMODATION
  add constraint PK_STUDENT_TO_ACOMODATION primary key (STUDENT_ID, ACCOMODATION_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_TO_ACOMODATION
  add constraint FK_STUDENT_ACOMODATION_ACCOM foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_TO_ACOMODATION
  add constraint FK_STUDENT_ACOMODATION_STUDENT foreign key (ACCOMODATION_ID)
  references timetable.STUDENT_ACCOMODATION (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_TO_ACOMODATION
  add constraint NN_STUDENT_TO_ACOMOD_ACOMOD
  check ("ACCOMODATION_ID" IS NOT NULL);
alter table timetable.STUDENT_TO_ACOMODATION
  add constraint NN_STUDENT_TO_ACOMOD_STUDENT
  check ("STUDENT_ID" IS NOT NULL);

prompt
prompt Creating table STUDENT_TO_GROUP
prompt ===============================
prompt
create table timetable.STUDENT_TO_GROUP
(
  student_id NUMBER(20),
  group_id   NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_TO_GROUP
  add constraint PK_STUDENT_TO_GROUP primary key (STUDENT_ID, GROUP_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.STUDENT_TO_GROUP
  add constraint FK_STUDENT_GROUP_GROUP foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_TO_GROUP
  add constraint FK_STUDENT_GROUP_STUDENT foreign key (GROUP_ID)
  references timetable.STUDENT_GROUP (UNIQUEID) on delete cascade;
alter table timetable.STUDENT_TO_GROUP
  add constraint NN_STUDENT_TO_GROUP_GROUP
  check ("GROUP_ID" IS NOT NULL);
alter table timetable.STUDENT_TO_GROUP
  add constraint NN_STUDENT_TO_GROUP_STUDENT
  check ("STUDENT_ID" IS NOT NULL);

prompt
prompt Creating table TIME_PATTERN_DAYS
prompt ================================
prompt
create table timetable.TIME_PATTERN_DAYS
(
  uniqueid        NUMBER(20),
  day_code        NUMBER(10),
  time_pattern_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN_DAYS
  add constraint PK_TIME_PATTERN_DAYS primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN_DAYS
  add constraint FK_TIME_PATTERN_DAYS_TIME_PATT foreign key (TIME_PATTERN_ID)
  references timetable.TIME_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.TIME_PATTERN_DAYS
  add constraint NN_TIME_PATTERN_DAYS_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_TIME_PATTERN_DAYS on timetable.TIME_PATTERN_DAYS (TIME_PATTERN_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TIME_PATTERN_DEPT
prompt ================================
prompt
create table timetable.TIME_PATTERN_DEPT
(
  dept_id    NUMBER(20),
  pattern_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN_DEPT
  add constraint PK_TIME_PATTERN_DEPT primary key (DEPT_ID, PATTERN_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN_DEPT
  add constraint FK_TIME_PATTERN_DEPT_DEPT foreign key (DEPT_ID)
  references timetable.DEPARTMENT (UNIQUEID) on delete cascade;
alter table timetable.TIME_PATTERN_DEPT
  add constraint FK_TIME_PATTERN_DEPT_PATTERN foreign key (PATTERN_ID)
  references timetable.TIME_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.TIME_PATTERN_DEPT
  add constraint NN_TIME_PATTERN_DEPT_DEPT_ID
  check ("DEPT_ID" IS NOT NULL);
alter table timetable.TIME_PATTERN_DEPT
  add constraint NN_TIME_PATTERN_DEPT_PATT_ID
  check ("PATTERN_ID" IS NOT NULL);

prompt
prompt Creating table TIME_PATTERN_TIME
prompt ================================
prompt
create table timetable.TIME_PATTERN_TIME
(
  uniqueid        NUMBER(20),
  start_slot      NUMBER(10),
  time_pattern_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN_TIME
  add constraint PK_TIME_PATTERN_TIME primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PATTERN_TIME
  add constraint FK_TIME_PATTERN_TIME foreign key (TIME_PATTERN_ID)
  references timetable.TIME_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.TIME_PATTERN_TIME
  add constraint NN_TIME_PATTERN_TIME_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_TIME_PATTERN_TIME on timetable.TIME_PATTERN_TIME (TIME_PATTERN_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TIME_PREF
prompt ========================
prompt
create table timetable.TIME_PREF
(
  uniqueid           NUMBER(20),
  owner_id           NUMBER(20),
  pref_level_id      NUMBER(20),
  preference         VARCHAR2(2048),
  time_pattern_id    NUMBER(20),
  last_modified_time TIMESTAMP(6)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PREF
  add constraint PK_TIME_PREF primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TIME_PREF
  add constraint FK_TIME_PREF_PREF_LEVEL foreign key (PREF_LEVEL_ID)
  references timetable.PREFERENCE_LEVEL (UNIQUEID) on delete cascade;
alter table timetable.TIME_PREF
  add constraint FK_TIME_PREF_TIME_PTRN foreign key (TIME_PATTERN_ID)
  references timetable.TIME_PATTERN (UNIQUEID) on delete cascade;
alter table timetable.TIME_PREF
  add constraint NN_TIME_PREF_OWNER_ID
  check ("OWNER_ID" IS NOT NULL);
alter table timetable.TIME_PREF
  add constraint NN_TIME_PREF_PREF_LEVEL_ID
  check ("PREF_LEVEL_ID" IS NOT NULL);
alter table timetable.TIME_PREF
  add constraint NN_TIME_PREF_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_TIME_PREF_OWNER on timetable.TIME_PREF (OWNER_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_TIME_PREF_PREF_LEVEL on timetable.TIME_PREF (PREF_LEVEL_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_TIME_PREF_TIME_PTRN on timetable.TIME_PREF (TIME_PATTERN_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TMTBL_MGR_TO_ROLES
prompt =================================
prompt
create table timetable.TMTBL_MGR_TO_ROLES
(
  manager_id     NUMBER(20),
  role_id        NUMBER(20),
  uniqueid       NUMBER(20),
  is_primary     NUMBER(1),
  receive_emails NUMBER(1) default 1
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint PK_TMTBL_MGR_TO_ROLES primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint UK_TMTBL_MGR_TO_ROLES_MGR_ROLE unique (MANAGER_ID, ROLE_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint FK_TMTBL_MGR_TO_ROLES_MANAGER foreign key (MANAGER_ID)
  references timetable.TIMETABLE_MANAGER (UNIQUEID) on delete cascade;
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint FK_TMTBL_MGR_TO_ROLES_ROLE foreign key (ROLE_ID)
  references timetable.ROLES (ROLE_ID) on delete cascade;
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint NN_TMTBL_MGR_TO_ROLES_MGR_ID
  check ("MANAGER_ID" IS NOT NULL);
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint NN_TMTBL_MGR_TO_ROLES_PRIMARY
  check ("IS_PRIMARY" IS NOT NULL);
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint NN_TMTBL_MGR_TO_ROLES_ROLE_ID
  check ("ROLE_ID" IS NOT NULL);
alter table timetable.TMTBL_MGR_TO_ROLES
  add constraint NN_TMTBL_MGR_TO_ROLES_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table USERS
prompt ====================
prompt
create table timetable.USERS
(
  username     VARCHAR2(15) not null,
  password     VARCHAR2(25),
  external_uid VARCHAR2(40)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.USERS
  add constraint PK_USERS primary key (USERNAME)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.USERS
  add constraint NN_USERS_PASSWD
  check (password is not null);

prompt
prompt Creating table USER_DATA
prompt ========================
prompt
create table timetable.USER_DATA
(
  external_uid VARCHAR2(12),
  name         VARCHAR2(100),
  value        VARCHAR2(2048)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.USER_DATA
  add constraint PK_USER_DATA primary key (EXTERNAL_UID, NAME)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.USER_DATA
  add constraint NN_USER_DATA_KEY
  check ("NAME" IS NOT NULL);
alter table timetable.USER_DATA
  add constraint NN_USER_DATA_PUID
  check ("EXTERNAL_UID" IS NOT NULL);
alter table timetable.USER_DATA
  add constraint NN_USER_DATA_VALUE
  check ("VALUE" IS NOT NULL);

prompt
prompt Creating table WAITLIST
prompt =======================
prompt
create table timetable.WAITLIST
(
  uniqueid           NUMBER(20),
  student_id         NUMBER(20),
  course_offering_id NUMBER(20),
  type               NUMBER(10) default (0),
  timestamp          DATE
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.WAITLIST
  add constraint PK_WAITLIST primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.WAITLIST
  add constraint FK_WAITLIST_COURSE_OFFERING foreign key (COURSE_OFFERING_ID)
  references timetable.COURSE_OFFERING (UNIQUEID) on delete cascade;
alter table timetable.WAITLIST
  add constraint FK_WAITLIST_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.WAITLIST
  add constraint NN_WAITLIST_OFFERING
  check ("COURSE_OFFERING_ID" IS NOT NULL);
alter table timetable.WAITLIST
  add constraint NN_WAITLIST_STUDENT
  check ("STUDENT_ID" IS NOT NULL);
alter table timetable.WAITLIST
  add constraint NN_WAITLIST_TS
  check ("TIMESTAMP" IS NOT NULL);
alter table timetable.WAITLIST
  add constraint NN_WAITLIST_TYPE
  check ("TYPE" IS NOT NULL);
alter table timetable.WAITLIST
  add constraint NN_WAITLIST_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);
create index timetable.IDX_WAITLIST_OFFERING on timetable.WAITLIST (COURSE_OFFERING_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
create index timetable.IDX_WAITLIST_STUDENT on timetable.WAITLIST (STUDENT_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table XCONFLICT
prompt ========================
prompt
create table timetable.XCONFLICT
(
  uniqueid      NUMBER(20),
  conflict_type NUMBER(10),
  distance      FLOAT
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT
  add constraint PK_XCONFLICT primary key (UNIQUEID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT
  add constraint NN_XCONFLICT_TYPE
  check ("CONFLICT_TYPE" IS NOT NULL);
alter table timetable.XCONFLICT
  add constraint NN_XCONFLICT_UNIQUEID
  check ("UNIQUEID" IS NOT NULL);

prompt
prompt Creating table XCONFLICT_EXAM
prompt =============================
prompt
create table timetable.XCONFLICT_EXAM
(
  conflict_id NUMBER(20),
  exam_id     NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT_EXAM
  add constraint PK_XCONFLICT_EXAM primary key (CONFLICT_ID, EXAM_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT_EXAM
  add constraint FK_XCONFLICT_EX_CONF foreign key (CONFLICT_ID)
  references timetable.XCONFLICT (UNIQUEID) on delete cascade;
alter table timetable.XCONFLICT_EXAM
  add constraint FK_XCONFLICT_EX_EXAM foreign key (EXAM_ID)
  references timetable.EXAM (UNIQUEID) on delete cascade;
alter table timetable.XCONFLICT_EXAM
  add constraint NN_XCONFLICT_EX_CONF
  check ("CONFLICT_ID" IS NOT NULL);
alter table timetable.XCONFLICT_EXAM
  add constraint NN_XCONFLICT_EX_EXAM
  check ("EXAM_ID" IS NOT NULL);
create index timetable.IDX_XCONFLICT_EXAM on timetable.XCONFLICT_EXAM (EXAM_ID)
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table XCONFLICT_INSTRUCTOR
prompt ===================================
prompt
create table timetable.XCONFLICT_INSTRUCTOR
(
  conflict_id   NUMBER(20),
  instructor_id NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT_INSTRUCTOR
  add constraint PK_XCONFLICT_INSTRUCTOR primary key (CONFLICT_ID, INSTRUCTOR_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT_INSTRUCTOR
  add constraint FK_XCONFLICT_IN_CONF foreign key (CONFLICT_ID)
  references timetable.XCONFLICT (UNIQUEID) on delete cascade;
alter table timetable.XCONFLICT_INSTRUCTOR
  add constraint FK_XCONFLICT_IN_INSTRUCTOR foreign key (INSTRUCTOR_ID)
  references timetable.DEPARTMENTAL_INSTRUCTOR (UNIQUEID) on delete cascade;
alter table timetable.XCONFLICT_INSTRUCTOR
  add constraint NN_XCONFLICT_IN_CONF
  check ("CONFLICT_ID" IS NOT NULL);
alter table timetable.XCONFLICT_INSTRUCTOR
  add constraint NN_XCONFLICT_IN_STUDENT
  check ("INSTRUCTOR_ID" IS NOT NULL);

prompt
prompt Creating table XCONFLICT_STUDENT
prompt ================================
prompt
create table timetable.XCONFLICT_STUDENT
(
  conflict_id NUMBER(20),
  student_id  NUMBER(20)
)
tablespace USERS
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT_STUDENT
  add constraint PK_XCONFLICT_STUDENT primary key (CONFLICT_ID, STUDENT_ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
alter table timetable.XCONFLICT_STUDENT
  add constraint FK_XCONFLICT_ST_CONF foreign key (CONFLICT_ID)
  references timetable.XCONFLICT (UNIQUEID) on delete cascade;
alter table timetable.XCONFLICT_STUDENT
  add constraint FK_XCONFLICT_ST_STUDENT foreign key (STUDENT_ID)
  references timetable.STUDENT (UNIQUEID) on delete cascade;
alter table timetable.XCONFLICT_STUDENT
  add constraint NN_XCONFLICT_ST_CONF
  check ("CONFLICT_ID" IS NOT NULL);
alter table timetable.XCONFLICT_STUDENT
  add constraint NN_XCONFLICT_ST_STUDENT
  check ("STUDENT_ID" IS NOT NULL);

prompt
prompt Creating sequence ACADEMIC_AREA_SEQ
prompt ===================================
prompt
create sequence timetable.ACADEMIC_AREA_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 161
increment by 1
cache 20;

prompt
prompt Creating sequence ACAD_CLASS_SEQ
prompt ================================
prompt
create sequence timetable.ACAD_CLASS_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 81
increment by 1
cache 20;

prompt
prompt Creating sequence ASSIGNMENT_SEQ
prompt ================================
prompt
create sequence timetable.ASSIGNMENT_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 142617
increment by 1
cache 20;

prompt
prompt Creating sequence BUILDING_SEQ
prompt ==============================
prompt
create sequence timetable.BUILDING_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 1802
increment by 1
cache 20;

prompt
prompt Creating sequence CLASS_INSTRUCTOR_SEQ
prompt ======================================
prompt
create sequence timetable.CLASS_INSTRUCTOR_SEQ
minvalue 1
maxvalue 9999999999
start with 38148
increment by 1
cache 20;

prompt
prompt Creating sequence CRS_CREDIT_UNIG_CFG_SEQ
prompt =========================================
prompt
create sequence timetable.CRS_CREDIT_UNIG_CFG_SEQ
minvalue 1
maxvalue 99999999999999999999
start with 27641
increment by 1
cache 20;

prompt
prompt Creating sequence CRS_OFFR_DEMAND_SEQ
prompt =====================================
prompt
create sequence timetable.CRS_OFFR_DEMAND_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 5992578
increment by 1
cache 20;

prompt
prompt Creating sequence CRS_OFFR_SEQ
prompt ==============================
prompt
create sequence timetable.CRS_OFFR_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 135793
increment by 1
cache 20;

prompt
prompt Creating sequence DATE_PATTERN_SEQ
prompt ==================================
prompt
create sequence timetable.DATE_PATTERN_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 893
increment by 1
cache 20;

prompt
prompt Creating sequence DESIGNATOR_SEQ
prompt ================================
prompt
create sequence timetable.DESIGNATOR_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 3827
increment by 1
cache 20;

prompt
prompt Creating sequence DIST_OBJ_SEQ
prompt ==============================
prompt
create sequence timetable.DIST_OBJ_SEQ
minvalue 1
maxvalue 9999999999
start with 5772
increment by 1
cache 20;

prompt
prompt Creating sequence HISTORY_SEQ
prompt =============================
prompt
create sequence timetable.HISTORY_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 41
increment by 1
cache 20;

prompt
prompt Creating sequence INSTRUCTOR_SEQ
prompt ================================
prompt
create sequence timetable.INSTRUCTOR_SEQ
minvalue 1
maxvalue 9999999999
start with 86446
increment by 1
cache 20;

prompt
prompt Creating sequence INSTR_OFFR_CONFIG_SEQ
prompt =======================================
prompt
create sequence timetable.INSTR_OFFR_CONFIG_SEQ
minvalue 1
maxvalue 9999999999
start with 37026
increment by 1
cache 20;

prompt
prompt Creating sequence INSTR_OFFR_PERMID_SEQ
prompt =======================================
prompt
create sequence timetable.INSTR_OFFR_PERMID_SEQ
minvalue 1
maxvalue 9999999999
start with 166820
increment by 1
cache 20;

prompt
prompt Creating sequence INSTR_OFFR_SEQ
prompt ================================
prompt
create sequence timetable.INSTR_OFFR_SEQ
minvalue 1
maxvalue 99999999999999999999
start with 132391
increment by 1
cache 20;

prompt
prompt Creating sequence JENRL_SEQ
prompt ===========================
prompt
create sequence timetable.JENRL_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence LOC_PERM_ID_SEQ
prompt =================================
prompt
create sequence timetable.LOC_PERM_ID_SEQ
minvalue 1
maxvalue 99999999999999999999
start with 41
increment by 1
cache 20;

prompt
prompt Creating sequence POS_MAJOR_SEQ
prompt ===============================
prompt
create sequence timetable.POS_MAJOR_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 1221
increment by 1
cache 20;

prompt
prompt Creating sequence PREF_GROUP_SEQ
prompt ================================
prompt
create sequence timetable.PREF_GROUP_SEQ
minvalue 1
maxvalue 99999999999999999999
start with 239279
increment by 1
cache 20;

prompt
prompt Creating sequence PREF_LEVEL_SEQ
prompt ================================
prompt
create sequence timetable.PREF_LEVEL_SEQ
minvalue 1
maxvalue 9999999999
start with 8
increment by 1
cache 20;

prompt
prompt Creating sequence PREF_SEQ
prompt ==========================
prompt
create sequence timetable.PREF_SEQ
minvalue 1
maxvalue 99999999999999999999
start with 123520
increment by 1
cache 20;

prompt
prompt Creating sequence QX__$TRIG_NUM
prompt ===============================
prompt
create sequence timetable.QX__$TRIG_NUM
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
nocache;

prompt
prompt Creating sequence REF_TABLE_SEQ
prompt ===============================
prompt
create sequence timetable.REF_TABLE_SEQ
minvalue 1
maxvalue 1000000
start with 465
increment by 1
cache 20;

prompt
prompt Creating sequence RESERVATION_SEQ
prompt =================================
prompt
create sequence timetable.RESERVATION_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 29643
increment by 1
cache 20;

prompt
prompt Creating sequence ROLE_SEQ
prompt ==========================
prompt
create sequence timetable.ROLE_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 121
increment by 1
cache 20;

prompt
prompt Creating sequence ROOM_DEPT_SEQ
prompt ===============================
prompt
create sequence timetable.ROOM_DEPT_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 4141
increment by 1
cache 20;

prompt
prompt Creating sequence ROOM_FEATURE_SEQ
prompt ==================================
prompt
create sequence timetable.ROOM_FEATURE_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 568
increment by 1
cache 20;

prompt
prompt Creating sequence ROOM_GROUP_SEQ
prompt ================================
prompt
create sequence timetable.ROOM_GROUP_SEQ
minvalue 1
maxvalue 9999999999
start with 6231
increment by 1
nocache;

prompt
prompt Creating sequence ROOM_SEQ
prompt ==========================
prompt
create sequence timetable.ROOM_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 8035
increment by 1
cache 20;

prompt
prompt Creating sequence ROOM_SHARING_GROUP_SEQ
prompt ========================================
prompt
create sequence timetable.ROOM_SHARING_GROUP_SEQ
minvalue 1
maxvalue 9999999999
start with 12667
increment by 1
cache 20;

prompt
prompt Creating sequence SETTINGS_SEQ
prompt ==============================
prompt
create sequence timetable.SETTINGS_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 228
increment by 1
cache 20;

prompt
prompt Creating sequence SOLUTION_SEQ
prompt ==============================
prompt
create sequence timetable.SOLUTION_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 846
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_GROUP_SEQ
prompt ==================================
prompt
create sequence timetable.SOLVER_GROUP_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 385
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_INFO_DEF_SEQ
prompt =====================================
prompt
create sequence timetable.SOLVER_INFO_DEF_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_INFO_SEQ
prompt =================================
prompt
create sequence timetable.SOLVER_INFO_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 275219
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_PARAMETER_DEF_SEQ
prompt ==========================================
prompt
create sequence timetable.SOLVER_PARAMETER_DEF_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 381
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_PARAMETER_GROUP_SEQ
prompt ============================================
prompt
create sequence timetable.SOLVER_PARAMETER_GROUP_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 121
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_PARAMETER_SEQ
prompt ======================================
prompt
create sequence timetable.SOLVER_PARAMETER_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 95376
increment by 1
cache 20;

prompt
prompt Creating sequence SOLVER_PREDEF_SETTING_SEQ
prompt ===========================================
prompt
create sequence timetable.SOLVER_PREDEF_SETTING_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 141
increment by 1
cache 20;

prompt
prompt Creating sequence STAFF_SEQ
prompt ===========================
prompt
create sequence timetable.STAFF_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 3463061
increment by 1
cache 20;

prompt
prompt Creating sequence STUDENT_CONFLICT_SEQ
prompt ======================================
prompt
create sequence timetable.STUDENT_CONFLICT_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence STUDENT_ENRL_SEQ
prompt ==================================
prompt
create sequence timetable.STUDENT_ENRL_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 7619278
increment by 1
cache 20;

prompt
prompt Creating sequence STUDENT_GROUP_SEQ
prompt ===================================
prompt
create sequence timetable.STUDENT_GROUP_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence STUDENT_INFO_SEQ
prompt ==================================
prompt
create sequence timetable.STUDENT_INFO_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 74861
increment by 1
cache 20;

prompt
prompt Creating sequence SUBJECT_AREA_SEQ
prompt ==================================
prompt
create sequence timetable.SUBJECT_AREA_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 839
increment by 1
cache 20;

prompt
prompt Creating sequence TIMETABLE_GLOBAL_INFO_SEQ
prompt ===========================================
prompt
create sequence timetable.TIMETABLE_GLOBAL_INFO_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence TIMETABLE_INFO_SEQ
prompt ====================================
prompt
create sequence timetable.TIMETABLE_INFO_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence TIMETABLE_MGR_SEQ
prompt ===================================
prompt
create sequence timetable.TIMETABLE_MGR_SEQ
minvalue 1
maxvalue 99999999999999999999
start with 550
increment by 1
cache 20;

prompt
prompt Creating sequence TIMETABLE_SEQ
prompt ===============================
prompt
create sequence timetable.TIMETABLE_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence TIMETABLE_SOLVER_PARAMETER_SEQ
prompt ================================================
prompt
create sequence timetable.TIMETABLE_SOLVER_PARAMETER_SEQ
minvalue 1
maxvalue 9999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating sequence TIME_PATTERN_DAYS_SEQ
prompt =======================================
prompt
create sequence timetable.TIME_PATTERN_DAYS_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 4918
increment by 1
cache 20;

prompt
prompt Creating sequence TIME_PATTERN_SEQ
prompt ==================================
prompt
create sequence timetable.TIME_PATTERN_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 1609
increment by 1
cache 20;

prompt
prompt Creating sequence TIME_PATTERN_TIMES_SEQ
prompt ========================================
prompt
create sequence timetable.TIME_PATTERN_TIMES_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 9481
increment by 1
cache 20;

prompt
prompt Creating sequence TMTBL_MGR_TO_ROLES_SEQ
prompt ========================================
prompt
create sequence timetable.TMTBL_MGR_TO_ROLES_SEQ
minvalue 1
maxvalue 9999999999
start with 590
increment by 1
cache 20;

prompt
prompt Creating sequence USER_SETTINGS_SEQ
prompt ===================================
prompt
create sequence timetable.USER_SETTINGS_SEQ
minvalue 1
maxvalue 999999999999999999999999999
start with 944
increment by 1
cache 20;

commit;

spool off
