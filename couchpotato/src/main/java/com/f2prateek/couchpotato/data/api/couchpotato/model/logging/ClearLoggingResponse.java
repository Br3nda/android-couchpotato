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

package com.f2prateek.couchpotato.data.api.couchpotato.model.logging;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

/** Response for /logging.clear */
public class ClearLoggingResponse implements Parcelable {
  private static final String FIELD_SUCCESS = "success";

  @SerializedName(FIELD_SUCCESS)
  private boolean success;

  public boolean isSuccess() {
    return success;
  }

  public ClearLoggingResponse(Parcel in) {
    success = in.readInt() == 1;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Parcelable.Creator<ClearLoggingResponse> CREATOR =
      new Parcelable.Creator<ClearLoggingResponse>() {
        public ClearLoggingResponse createFromParcel(Parcel in) {
          return new ClearLoggingResponse(in);
        }

        public ClearLoggingResponse[] newArray(int size) {
          return new ClearLoggingResponse[size];
        }
      };

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(success ? 1 : 0);
  }
}