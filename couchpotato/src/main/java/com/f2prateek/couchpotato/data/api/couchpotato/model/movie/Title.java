/*
 * Copyright 2014 Prateek Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f2prateek.couchpotato.data.api.couchpotato.model.movie;

import com.google.gson.annotations.SerializedName;

public class Title {
  private static final String DEFAULT = "default";
  private static final String SIMPLE_TITLE = "simple_title";

  @SerializedName(DEFAULT)
  public boolean isDefault;
  @SerializedName(SIMPLE_TITLE)
  public String simpleTitle;
  public long id;
  public String title;
}
