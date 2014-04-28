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

package com.f2prateek.couchpotato.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import butterknife.InjectView;
import com.f2prateek.couchpotato.Events;
import com.f2prateek.couchpotato.R;
import com.f2prateek.couchpotato.ui.views.MovieGridAdapter;
import com.f2prateek.couchpotato.ui.widget.BetterViewAnimator;
import com.squareup.picasso.Picasso;
import javax.inject.Inject;

public class MoviesGridFragment extends BaseFragment {
  @InjectView(R.id.root) BetterViewAnimator root;
  @InjectView(R.id.grid) AbsListView grid;

  @Inject Picasso picasso;

  protected MovieGridAdapter adapter;

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_movies_grid, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    adapter = new MovieGridAdapter(activityContext, picasso);
    grid.setAdapter(adapter);
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);

        int width = view.getWidth();
        int height = view.getHeight();

        bus.post(new Events.OnMovieClickedEvent(adapter.getItem(position), height, width,
            screenLocation[0], screenLocation[1]));
      }
    });
  }
}
