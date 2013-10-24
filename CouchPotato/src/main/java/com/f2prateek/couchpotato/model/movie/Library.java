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

public class Library {
  public Info info;
  public String plot;
  public int status_id;
  public String tagline;
  public List<Title> titles;
  public int year;
  public String identifier;
  public List<File> files;

  @Override public String toString() {
    return "Library{" +
        "info=" + info +
        ", plot='" + plot + '\'' +
        ", status_id=" + status_id +
        ", tagline='" + tagline + '\'' +
        ", titles=" + titles +
        ", year=" + year +
        ", identifier='" + identifier + '\'' +
        ", files=" + files +
        '}';
  }
}
