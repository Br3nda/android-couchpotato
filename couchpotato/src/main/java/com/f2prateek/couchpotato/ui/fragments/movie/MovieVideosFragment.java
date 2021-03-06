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

package com.f2prateek.couchpotato.ui.fragments.movie;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.f2prateek.couchpotato.ForActivity;
import com.f2prateek.couchpotato.R;
import com.f2prateek.couchpotato.data.api.Movie;
import com.f2prateek.couchpotato.data.api.tmdb.TMDbDatabase;
import com.f2prateek.couchpotato.data.api.tmdb.model.Video;
import com.f2prateek.couchpotato.data.rx.EndlessObserver;
import com.f2prateek.couchpotato.ui.misc.BindableListAdapter;
import com.f2prateek.couchpotato.ui.views.MovieVideoItem;
import com.f2prateek.couchpotato.util.CollectionUtils;
import com.squareup.picasso.Picasso;
import java.util.List;
import javax.inject.Inject;

public class MovieVideosFragment extends MovieInfoGridFragment {
  @Inject TMDbDatabase tmDbDatabase;
  @Inject Picasso picasso;
  @Inject @ForActivity Context activityContext;

  @Override public void onViewCreated(View view, Bundle inState) {
    super.onViewCreated(view, inState);
    ((GridView) getCollectionView()).setColumnWidth(
        getResources().getDimensionPixelOffset(R.dimen.large_grid_item_width));
  }

  @Override protected void fetch(Movie minifiedMovie) {
    subscribe(tmDbDatabase.getVideos(minifiedMovie.id()), new EndlessObserver<List<Video>>() {
          @Override public void onNext(List<Video> videos) {
            if (!CollectionUtils.isNullOrEmpty(videos)) {
              MovieVideoAdapter adapter = new MovieVideoAdapter(activityContext, picasso);
              adapter.replaceWith(videos);
              setAdapter(adapter);
            }
          }
        }
    );
  }

  static class MovieVideoAdapter extends BindableListAdapter<Video> {
    private final Picasso picasso;

    public MovieVideoAdapter(Context context, Picasso picasso) {
      super(context);
      this.picasso = picasso;
    }

    @Override public View newView(LayoutInflater inflater, int position, ViewGroup container) {
      return inflater.inflate(R.layout.movie_video_item, container, false);
    }

    @Override public void bindView(Video item, int position, View view) {
      ((MovieVideoItem) view).bindTo(item, picasso);
    }
  }
}
