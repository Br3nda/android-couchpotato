/*
 * Copyright 2014 Prateek Srivastava (@f2prateek)
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

package com.f2prateek.couchpotato.data.api.moviedb;

import com.f2prateek.couchpotato.data.TMDbDatabase;
import com.f2prateek.ln.Ln;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;

@Module(complete = false, library = true)
public class TMDbApiModule {
  private static final String API_URL = "http://api.themoviedb.org/3/";
  private static final String API_KEY = "c820209625cf108a92f8e4192ec26a7f";

  @Provides @Singleton @TMDb Endpoint provideEndpoint() {
    return Endpoints.newFixedEndpoint(API_URL);
  }

  @Provides @Singleton @TMDb Client provideClient(OkHttpClient client) {
    return new OkClient(client);
  }

  @Provides @Singleton @TMDb @ApiKey String provideApiKey() {
    return API_KEY;
  }

  @Provides @Singleton @TMDb RequestInterceptor provideRequestInterceptor(
      @TMDb @ApiKey String apiKey) {
    return new TMDbRequestInterceptor(apiKey);
  }

  @Provides @Singleton @TMDb RestAdapter provideRestAdapter(Endpoint endpoint, Client client,
      @TMDb RequestInterceptor requestInterceptor) {
    return new RestAdapter.Builder() //
        .setClient(client) //
        .setEndpoint(endpoint) //
        .setRequestInterceptor(requestInterceptor) //
        .setLog(new RestAdapter.Log() {
          @Override public void log(String message) {
            Ln.d(message);
          }
        }) //
            //.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL:RestAdapter.LogLevel.NONE)
            // todo: move to drawer
        .build();
  }

  @Provides @Singleton TMDbService provideGalleryService(@TMDb RestAdapter restAdapter) {
    return restAdapter.create(TMDbService.class);
  }

  @Provides @Singleton TMDbDatabase provideTMDbDatabase(TMDbService service) {
    return new TMDbDatabase(service);
  }
}

