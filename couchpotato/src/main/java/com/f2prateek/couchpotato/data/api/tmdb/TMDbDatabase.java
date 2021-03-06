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

package com.f2prateek.couchpotato.data.api.tmdb;

import com.f2prateek.couchpotato.data.api.Movie;
import com.f2prateek.couchpotato.data.api.tmdb.model.Configuration;
import com.f2prateek.couchpotato.data.api.tmdb.model.Images;
import com.f2prateek.couchpotato.data.api.tmdb.model.MinifiedMovie;
import com.f2prateek.couchpotato.data.api.tmdb.model.MovieCollectionResponse;
import com.f2prateek.couchpotato.data.api.tmdb.model.MovieCreditsResponse;
import com.f2prateek.couchpotato.data.api.tmdb.model.MovieReview;
import com.f2prateek.couchpotato.data.api.tmdb.model.MovieReviewsResponse;
import com.f2prateek.couchpotato.data.api.tmdb.model.MovieVideosResponse;
import com.f2prateek.couchpotato.data.api.tmdb.model.TMDbMovie;
import com.f2prateek.couchpotato.data.api.tmdb.model.Video;
import java.util.List;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class TMDbDatabase {
  private final TMDbService tmDbService;
  private Observable<Configuration> configurationObservable;

  public TMDbDatabase(TMDbService tmDbService) {
    this.tmDbService = tmDbService;
  }

  // Fetch the configuration and cache it future use.
  public Observable<Configuration> getConfiguration() {
    if (configurationObservable == null) {
      configurationObservable = tmDbService.configuration().subscribeOn(Schedulers.io()).cache();
    }
    return configurationObservable;
  }

  private Observable<List<Movie>> transformMovieResponse(final Configuration configuration,
      Observable<MovieCollectionResponse> observable) {
    return observable.map(new Func1<MovieCollectionResponse, List<MinifiedMovie>>() {
      @Override public List<MinifiedMovie> call(MovieCollectionResponse response) {
        return response.getResults();
      }
    }).flatMap(new Func1<List<MinifiedMovie>, Observable<MinifiedMovie>>() {
      @Override public Observable<MinifiedMovie> call(List<MinifiedMovie> movies) {
        return Observable.from(movies);
      }
    }).filter(new Func1<MinifiedMovie, Boolean>() {
      @Override public Boolean call(MinifiedMovie movie) {
        // TODO: control in preferences
        return !movie.isAdult();
      }
    }).map(new Func1<MinifiedMovie, MinifiedMovie>() {
      @Override public MinifiedMovie call(MinifiedMovie movie) {
        movie.setConfiguration(configuration);
        return movie;
      }
    }).map(new Func1<MinifiedMovie, Movie>() {
      @Override public Movie call(MinifiedMovie movie) {
        return Movie.create(movie);
      }
    }).toList().subscribeOn(Schedulers.io());
  }

  public Observable<List<Movie>> getPopularMovies(final int page) {
    return getConfiguration().flatMap(new Func1<Configuration, Observable<List<Movie>>>() {
      @Override public Observable<List<Movie>> call(Configuration configuration) {
        return transformMovieResponse(configuration, tmDbService.popular(page));
      }
    });
  }

  public Observable<List<Movie>> getTopRatedMovies(final int page) {
    return getConfiguration().flatMap(new Func1<Configuration, Observable<List<Movie>>>() {
      @Override public Observable<List<Movie>> call(Configuration configuration) {
        return transformMovieResponse(configuration, tmDbService.topRated(page));
      }
    });
  }

  public Observable<List<Movie>> getUpcomingMovies(final int page) {
    return getConfiguration().flatMap(new Func1<Configuration, Observable<List<Movie>>>() {
      @Override public Observable<List<Movie>> call(Configuration configuration) {
        return transformMovieResponse(configuration, tmDbService.upcoming(page));
      }
    });
  }

  public Observable<List<Movie>> getNowPlayingMovies(final int page) {
    return getConfiguration().flatMap(new Func1<Configuration, Observable<List<Movie>>>() {
      @Override public Observable<List<Movie>> call(Configuration configuration) {
        return transformMovieResponse(configuration, tmDbService.nowPlaying(page));
      }
    });
  }

  public Observable<TMDbMovie> getMovie(final long id) {
    return Observable.combineLatest(getConfiguration(), tmDbService.movie(id),
        new Func2<Configuration, TMDbMovie, TMDbMovie>() {
          @Override public TMDbMovie call(Configuration configuration, TMDbMovie tmDbMovie) {
            tmDbMovie.setConfiguration(configuration);
            return tmDbMovie;
          }
        }
    ).subscribeOn(Schedulers.io());
  }

  public Observable<List<Movie>> discoverMovies(final int page) {
    return getConfiguration().flatMap(new Func1<Configuration, Observable<List<Movie>>>() {
      @Override public Observable<List<Movie>> call(Configuration configuration) {
        return transformMovieResponse(configuration, tmDbService.discover(page));
      }
    });
  }

  public Observable<Images> getMovieImages(final long id) {
    return Observable.combineLatest(getConfiguration(), tmDbService.movieImages(id),
        new Func2<Configuration, Images, Images>() {
          @Override public Images call(Configuration configuration, Images images) {
            images.setConfiguration(configuration);
            return images;
          }
        }
    ).subscribeOn(Schedulers.io());
  }

  public Observable<MovieCreditsResponse> getMovieCredits(final long id) {
    return Observable.combineLatest(getConfiguration(), tmDbService.movieCredits(id),
        new Func2<Configuration, MovieCreditsResponse, MovieCreditsResponse>() {
          @Override public MovieCreditsResponse call(Configuration configuration,
              MovieCreditsResponse movieCreditsResponse) {
            movieCreditsResponse.setConfiguration(configuration);
            return movieCreditsResponse;
          }
        }
    ).subscribeOn(Schedulers.io());
  }

  public Observable<List<Video>> getVideos(final long id) {
    return tmDbService.movieVideos(id).map(new Func1<MovieVideosResponse, List<Video>>() {
      @Override public List<Video> call(MovieVideosResponse movieVideosResponse) {
        return movieVideosResponse.getResults();
      }
    }).subscribeOn(Schedulers.io());
  }

  public Observable<List<Movie>> getSimilarMovies(final long id) {
    return getConfiguration().flatMap(new Func1<Configuration, Observable<List<Movie>>>() {
      @Override public Observable<List<Movie>> call(Configuration configuration) {
        return transformMovieResponse(configuration, tmDbService.movieSimilar(id));
      }
    });
  }

  public Observable<List<MovieReview>> getMovieReviews(final long id) {
    return Observable.combineLatest(getConfiguration(), tmDbService.movieReviews(id),
        new Func2<Configuration, MovieReviewsResponse, MovieReviewsResponse>() {
          @Override public MovieReviewsResponse call(Configuration configuration,
              MovieReviewsResponse movieReviewsResponse) {
            return movieReviewsResponse;
          }
        }
    ).map(new Func1<MovieReviewsResponse, List<MovieReview>>() {
      @Override public List<MovieReview> call(MovieReviewsResponse movieReviewsResponse) {
        return movieReviewsResponse.getResults();
      }
    }).subscribeOn(Schedulers.io());
  }
}
