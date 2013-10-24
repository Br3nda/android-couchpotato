/*
 * Copyright 2013 Prateek Srivastava (@f2prateek)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f2prateek.couchpotato.model.movie;

import java.util.List;

public class Release {
  public List<File> files;
  public int quality_id;
  public int status_id;
  public long last_edit;
  public String identifier;
  public long id;

  @Override public String toString() {
    return "Release{" +
        "files=" + files +
        ", quality_id=" + quality_id +
        ", status_id=" + status_id +
        ", last_edit=" + last_edit +
        ", identifier='" + identifier + '\'' +
        ", id=" + id +
        '}';
  }
}
