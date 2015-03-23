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

/** Add index on course permanent id of LASTLIKE_COURSE_DEMAND table */
create index IDX_LL_COURSE_DEMAND_PERMID on LASTLIKE_COURSE_DEMAND(course_perm_id);
--tablespace smas_index;

/** Set course permanent id to NULL whenever set to -1 for all last-like course requests */ 
update LASTLIKE_COURSE_DEMAND set course_perm_id = null where course_perm_id = '-1';

/** Allow course permanent id of a course offering to be NULL */
alter table COURSE_OFFERING modify perm_id VARCHAR2(20) NULL;

/** Set course permanent id to null whenever set to -1 for all course offerings */
update COURSE_OFFERING set perm_id = null where perm_id = '-1';

commit;
