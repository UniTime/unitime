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

update solver_info_def set implementation=replace(implementation,'edu.purdue.smas','org.unitime');
update solver_parameter_def set default_value=replace(default_value,'edu.purdue.smas','org.unitime') where default_value like 'edu.purdue.smas%';
update change_log set obj_type=replace(obj_type,'edu.purdue.smas','org.unitime');

commit;
