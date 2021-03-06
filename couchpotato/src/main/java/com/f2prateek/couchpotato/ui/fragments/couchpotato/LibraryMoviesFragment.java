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

package com.f2prateek.couchpotato.ui.fragments.couchpotato;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.f2prateek.couchpotato.ForActivity;
import com.f2prateek.couchpotato.R;
import com.f2prateek.couchpotato.data.api.Movie;
import com.f2prateek.couchpotato.data.api.couchpotato.CouchPotatoDatabase;
import com.f2prateek.couchpotato.data.api.couchpotato.CouchPotatoEndpoint;
import com.f2prateek.couchpotato.data.rx.EndlessObserver;
import com.f2prateek.couchpotato.ui.activities.CouchPotatoServerSettingsActivity;
import com.f2prateek.couchpotato.ui.fragments.MoviesGridFragment;
import com.f2prateek.couchpotato.ui.misc.Truss;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class LibraryMoviesFragment extends MoviesGridFragment {
  @Inject CouchPotatoDatabase database;
  @Inject CouchPotatoEndpoint endpoint;
  @Inject @ForActivity Context activityContext;
  final List<CouchPotatoDatabase.LibraryMovieStatus> displayedMoviesStatus = new ArrayList<>(2);

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.library, menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_wanted:
        if (item.isChecked()) {
          displayedMoviesStatus.remove(CouchPotatoDatabase.LibraryMovieStatus.WANTED);
          item.setChecked(false);
        } else {
          displayedMoviesStatus.add(CouchPotatoDatabase.LibraryMovieStatus.WANTED);
          item.setChecked(true);
        }
        fetchMovies();
        return true;
      case R.id.menu_snatched:
        if (item.isChecked()) {
          displayedMoviesStatus.remove(CouchPotatoDatabase.LibraryMovieStatus.SNATCHED);
          item.setChecked(false);
        } else {
          displayedMoviesStatus.add(CouchPotatoDatabase.LibraryMovieStatus.SNATCHED);
          item.setChecked(true);
        }
        fetchMovies();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // todo: arrays.asList mutable equivalent?
    displayedMoviesStatus.add(CouchPotatoDatabase.LibraryMovieStatus.WANTED);
    displayedMoviesStatus.add(CouchPotatoDatabase.LibraryMovieStatus.SNATCHED);
    fetchMovies();
  }

  void fetchMovies() {
    showLoadingView();

    if (!endpoint.isSet()) {
      showServerNotSetMessage();
    } else {
      subscribe(database.getMovies(displayedMoviesStatus), new EndlessObserver<List<Movie>>() {
            @Override public void onNext(List<Movie> movies) {
              adapter.replaceWith(movies);
              setAdapter(adapter);
            }

            @Override public void onError(Throwable throwable) {
              super.onError(throwable);
              showInaccessibleServerMessage();
            }
          }
      );
    }
  }

  void showServerNotSetMessage() {
    final Intent intent = new Intent(activityContext, CouchPotatoServerSettingsActivity.class);
    TextView extraText = (TextView) inflate(R.layout.partial_extra_text);
    extraText.setText(new Truss() //
            .append(getString(R.string.error_server_not_set)) //
            .append(" ") // Space between sentences
            .pushSpan(new ForegroundColorSpan(getResources().getColor(R.color.app_color)))
            .append(getString(R.string.error_server_not_set_prompt))
            .build()
    );
    extraText.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startActivity(intent);
      }
    });
    setExtraView(extraText);
  }

  void showInaccessibleServerMessage() {
    final Intent intent = new Intent(activityContext, CouchPotatoServerSettingsActivity.class);
    TextView extraText = (TextView) inflate(R.layout.partial_extra_text);
    extraText.setText(new Truss() //
            .append(getString(R.string.error_inaccessible_server)) //
            .append(" ") // Space between sentences
            .pushSpan(new ForegroundColorSpan(getResources().getColor(R.color.app_color)))
            .append(getString(R.string.error_inaccessible_server_prompt))
            .build()
    );
    extraText.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startActivity(intent);
      }
    });
    setExtraView(extraText);
  }
}
