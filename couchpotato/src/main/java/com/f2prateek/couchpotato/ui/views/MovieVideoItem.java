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

package com.f2prateek.couchpotato.ui.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.f2prateek.couchpotato.R;
import com.f2prateek.couchpotato.data.api.tmdb.model.Video;
import com.squareup.picasso.Picasso;

public class MovieVideoItem extends FrameLayout {
  @InjectView(R.id.video_thumbnail) ImageView image;
  @InjectView(R.id.video_title) TextView title;

  private Video video;

  public MovieVideoItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.inject(this);
  }

  public void bindTo(Video video, Picasso picasso) {
    this.video = video;
    picasso.load(video.getThumbnail()).fit().centerCrop().error(R.drawable.ic_launcher).into(image);
    title.setText(video.getName());
  }

  @OnClick(R.id.video_thumbnail) public void onImageClicked() {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(video.getUrl()));
    getContext().startActivity(i);
  }
}
