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

package com.f2prateek.couchpotato.data.api.couchpotato;

import com.f2prateek.couchpotato.data.api.Movie;
import com.f2prateek.couchpotato.data.api.couchpotato.model.ApiKeyResponse;
import com.f2prateek.couchpotato.data.api.couchpotato.model.movie.AddMovieResponse;
import com.f2prateek.couchpotato.data.api.couchpotato.model.movie.CouchPotatoMovie;
import com.f2prateek.couchpotato.data.api.couchpotato.model.movie.MoviesResponse;
import com.f2prateek.couchpotato.data.api.couchpotato.model.profile.Profile;
import com.f2prateek.couchpotato.data.api.couchpotato.model.profile.ProfilesResponse;
import com.f2prateek.couchpotato.util.Strings;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.f2prateek.couchpotato.data.Util.md5;

public class CouchPotatoDatabase {
  private final CouchPotatoService couchPotatoService;

  public CouchPotatoDatabase(CouchPotatoService couchPotatoService) {
    this.couchPotatoService = couchPotatoService;
  }

  /**
   * Wrap the given observable to be subscribe on the I/O thread, and observe on the main thread.
   */
  private static <T> Observable<T> network(Observable<T> observable) {
    return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  public Subscription getApiKey(final String username, final String password,
      final Observer<ApiKeyResponse> observer) {
    return network(couchPotatoService.getApiKey(md5(password), md5(username))
        .timeout(10, TimeUnit.SECONDS)).subscribe(observer);
  }

  public enum LibraryMovieStatus {
    WANTED("active"),
    SNATCHED("done");

    String string;

    LibraryMovieStatus(String string) {
      this.string = string;
    }

    @Override public String toString() {
      return string;
    }
  }

  public Subscription getMovies(final List<LibraryMovieStatus> statuses,
      final Observer<List<Movie>> observer) {
    String status = Strings.join(',', statuses);
    return network(couchPotatoService.getMovies(status)
        .timeout(30, TimeUnit.SECONDS)
        .flatMap(new Func1<MoviesResponse, Observable<CouchPotatoMovie>>() {
          @Override public Observable<CouchPotatoMovie> call(MoviesResponse moviesResponse) {
            return Observable.from(moviesResponse.getMovies());
          }
        })
        .map(new Func1<CouchPotatoMovie, Movie>() {
          @Override public Movie call(CouchPotatoMovie couchPotatoMovie) {
            return Movie.create(couchPotatoMovie);
          }
        })
        .toList()) //
        .subscribe(observer);
  }

  public Subscription getProfiles(final Observer<List<Profile>> observer) {
    return network(couchPotatoService.getProfiles() //
        .map(new Func1<ProfilesResponse, List<Profile>>() { //
          @Override public List<Profile> call(ProfilesResponse response) {
            return response.getProfiles();
          }
        }) //
        .cache())//
        .subscribe(observer);
  }

  public Subscription addMovie(int profileId, String imdbId, final Observer<Boolean> observer) {
    return network(
        couchPotatoService.addMovie(profileId, imdbId).map(new Func1<AddMovieResponse, Boolean>() {
          @Override public Boolean call(AddMovieResponse addMovieResponse) {
            return addMovieResponse.success;
          }
        })) //
        .subscribe(observer);
  }
}