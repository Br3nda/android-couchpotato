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

package com.f2prateek.couchpotato.ui;

import android.animation.TimeInterpolator;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import butterknife.InjectView;
import com.f2prateek.couchpotato.R;
import com.f2prateek.couchpotato.data.TMDbDatabase;
import com.f2prateek.couchpotato.data.api.tmdb.model.Backdrop;
import com.f2prateek.couchpotato.data.api.tmdb.model.Credits;
import com.f2prateek.couchpotato.data.api.tmdb.model.Images;
import com.f2prateek.couchpotato.data.api.tmdb.model.MinifiedMovie;
import com.f2prateek.couchpotato.data.api.tmdb.model.Movie;
import com.f2prateek.couchpotato.data.rx.EndlessObserver;
import com.f2prateek.couchpotato.ui.colorizer.ColorScheme;
import com.f2prateek.couchpotato.ui.misc.AlphaForegroundColorSpan;
import com.f2prateek.couchpotato.ui.widget.KenBurnsView;
import com.f2prateek.couchpotato.ui.widget.NotifyingScrollView;
import com.f2prateek.dart.InjectExtra;
import com.f2prateek.ln.Ln;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MovieActivity extends BaseActivity
    implements NotifyingScrollView.OnScrollChangedListener {

  private static final String ARGS_MOVIE = "movie";
  private static final String ARGS_ORIENTATION = "orientation";
  private static final String ARGS_THUMBNAIL_LEFT = "thumbnail_left";
  private static final String ARGS_THUMBNAIL_TOP = "thumbnail_top";
  private static final String ARGS_THUMBNAIL_WIDTH = "thumbnail_width";
  private static final String ARGS_THUMBNAIL_HEIGHT = "thumbnail_height";

  private static final int ANIMATION_DURATION = 500; // 500ms
  private static final int HALF_ANIMATION_DURATION = ANIMATION_DURATION / 2;

  private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
  private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();

  @InjectExtra(ARGS_MOVIE) MinifiedMovie minifiedMovie;
  @InjectExtra(ARGS_THUMBNAIL_LEFT) int thumbnailLeft;
  @InjectExtra(ARGS_THUMBNAIL_TOP) int thumbnailTop;
  @InjectExtra(ARGS_THUMBNAIL_WIDTH) int thumbnailWidth;
  @InjectExtra(ARGS_THUMBNAIL_HEIGHT) int thumbnailHeight;
  @InjectExtra(ARGS_ORIENTATION) int originalOrientation;

  @InjectView(android.R.id.home) ImageView actionBarIconView;
  @InjectView(R.id.movie_header) View movieHeader;
  @InjectView(R.id.movie_header_backdrop) KenBurnsView movieBackdrop;
  @InjectView(R.id.movie_header_poster) ImageView moviePoster;
  @InjectView(R.id.movie_scroll_container) NotifyingScrollView scrollView;
  @InjectView(R.id.movie_primary) FrameLayout moviePrimary;
  @InjectView(R.id.movie_primary_accent) FrameLayout moviePrimaryAccent;
  @InjectView(R.id.movie_secondary) FrameLayout movieSecondary;
  @InjectView(R.id.movie_secondary_accent) FrameLayout movieSecondaryAccent;
  @InjectView(R.id.movie_tertiary_accent) FrameLayout movieTertiaryAccent;

  int mLeftDelta;
  int mTopDelta;
  float mWidthScale;
  float mHeightScale;

  @Inject TMDbDatabase tmDbDatabase;
  @Inject Picasso picasso;

  private int actionBarTitleColor;
  private int actionBarHeight;
  private int headerHeight;
  private int minHeaderTranslation;
  private AccelerateDecelerateInterpolator smoothInterpolator;
  private RectF sourceRect = new RectF();
  private RectF targetRect = new RectF();
  private AlphaForegroundColorSpan alphaForegroundColorSpan;
  private SpannableString spannableString;
  private TypedValue typedValue = new TypedValue();

  public static Intent createIntent(Context context, MinifiedMovie movie, int left, int top,
      int width, int height, int orientation) {
    Intent intent = new Intent(context, MovieActivity.class);
    intent.putExtra(ARGS_MOVIE, movie);
    intent.putExtra(ARGS_THUMBNAIL_LEFT, left);
    intent.putExtra(ARGS_THUMBNAIL_TOP, top);
    intent.putExtra(ARGS_THUMBNAIL_WIDTH, width);
    intent.putExtra(ARGS_THUMBNAIL_HEIGHT, height);
    intent.putExtra(ARGS_ORIENTATION, orientation);
    return intent;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    spannableString = new SpannableString(minifiedMovie.getTitle());
    picasso.load(minifiedMovie.getPosterPath()).fit().centerCrop().into(moviePoster);

    tmDbDatabase.getMovie(minifiedMovie.getId(), new EndlessObserver<Movie>() {
      @Override public void onNext(Movie movie) {
        Ln.d(movie);
      }
    });

    tmDbDatabase.getMovieImages(minifiedMovie.getId(), new EndlessObserver<Images>() {
      @Override public void onNext(Images images) {
        List<String> backdrops = new ArrayList<>();
        for (Backdrop backdrop : images.getBackdrops()) {
          backdrops.add(backdrop.getFilePath());
        }
        movieBackdrop.update(backdrops);
      }
    });

    tmDbDatabase.getSimilarMovies(minifiedMovie.getId(),
        new EndlessObserver<List<MinifiedMovie>>() {
          @Override public void onNext(List<MinifiedMovie> similarMovies) {
            Ln.d(similarMovies);
          }
        }
    );

    tmDbDatabase.getMovieCredits(minifiedMovie.getId(), new EndlessObserver<Credits>() {
      @Override public void onNext(Credits credits) {
        Ln.d(credits);
      }
    });

    // Only run the animation if we're coming from the parent activity, not if
    // we're recreated automatically by the window manager (e.g., device rotation)
    if (savedInstanceState == null) {
      ViewTreeObserver observer = moviePoster.getViewTreeObserver();
      observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

        @Override
        public boolean onPreDraw() {
          moviePoster.getViewTreeObserver().removeOnPreDrawListener(this);

          // Figure out where the thumbnail and full size versions are, relative
          // to the screen and each other
          int[] screenLocation = new int[2];
          moviePoster.getLocationOnScreen(screenLocation);
          mLeftDelta = thumbnailLeft - screenLocation[0];
          mTopDelta = thumbnailTop - screenLocation[1];

          // Scale factors to make the large version the same size as the thumbnail
          mWidthScale = (float) thumbnailWidth / moviePoster.getWidth();
          mHeightScale = (float) thumbnailHeight / moviePoster.getHeight();

          runEnterAnimation();

          return true;
        }
      });
    } else {
      init();
    }
  }

  /**
   * The enter animation scales the picture in from its previous thumbnail
   * size/location, colorizing it in parallel. In parallel, the background of the
   * activity is fading in. When the pictue is in place, the text description
   * drops down.
   */
  public void runEnterAnimation() {
    // Set starting values for properties we're going to animate. These
    // values scale and position the full size version down to the thumbnail
    // size/location, from which we'll animate it back up
    moviePoster.setPivotX(0);
    moviePoster.setPivotY(0);
    moviePoster.setScaleX(mWidthScale);
    moviePoster.setScaleY(mHeightScale);
    moviePoster.setTranslationX(mLeftDelta);
    moviePoster.setTranslationY(mTopDelta);

    // We'll fade the content in later
    scrollView.setAlpha(0);
    movieBackdrop.load(picasso, minifiedMovie.getBackdropPath());
    movieBackdrop.setAlpha(0);

    // Animate scale and translation to go from thumbnail to full size
    moviePoster.animate().setDuration(ANIMATION_DURATION).
        scaleX(1).scaleY(1).
        translationX(0).translationY(0).
        setInterpolator(sDecelerator).
        withEndAction(new Runnable() {
          public void run() {
            init();
            // Animate the content in after the image animation is done
            scrollView.animate().setDuration(HALF_ANIMATION_DURATION).alpha(1).
                setInterpolator(sDecelerator);
            movieBackdrop.animate().setDuration(HALF_ANIMATION_DURATION).alpha(1).
                setInterpolator(sDecelerator);
          }
        });
  }

  /**
   * The exit animation is basically a reverse of the enter animation, except that if
   * the orientation has changed we simply scale the picture back into the center of
   * the screen.
   *
   * @param endAction This action gets run after the animation completes (this is
   * when we actually switch activities)
   */
  public void runExitAnimation(final Runnable endAction) {
    // No need to set initial values for the reverse animation; the image is at the
    // starting size/location that we want to start from. Just animate to the
    // thumbnail size/location that we retrieved earlier

    // Caveat: configuration change invalidates thumbnail positions; just animate
    // the scale around the center. Also, fade it out since it won't match up with
    // whatever's actually in the center
    final boolean fadeOut;
    if (getResources().getConfiguration().orientation != originalOrientation) {
      moviePoster.setPivotX(moviePoster.getWidth() / 2);
      moviePoster.setPivotY(moviePoster.getHeight() / 2);
      mLeftDelta = 0;
      mTopDelta = 0;
      fadeOut = true;
    } else {
      fadeOut = false;
    }

    // First, slide/fade content out of the way
    movieBackdrop.animate()
        .alpha(0)
        .setDuration(HALF_ANIMATION_DURATION)
        .setInterpolator(sAccelerator);
    setTitleAlpha(0);
    scrollView.animate().alpha(0).
        setDuration(HALF_ANIMATION_DURATION).setInterpolator(sAccelerator).
        withEndAction(new Runnable() {
          public void run() {
            // Animate image back to thumbnail size/location
            moviePoster.animate().setDuration(HALF_ANIMATION_DURATION).
                scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta).
                withEndAction(endAction);
            if (fadeOut) {
              moviePoster.animate().alpha(0);
            }
          }
        });
  }

  /**
   * Overriding this method allows us to run our exit animation first, then exiting
   * the activity when it is complete.
   */
  @Override
  public void onBackPressed() {
    runExitAnimation(new Runnable() {
      public void run() {
        // *Now* go ahead and exit the activity
        finish();
      }
    });
  }

  @Override
  public void finish() {
    super.finish();

    // override transitions to skip the standard window animations
    overridePendingTransition(0, 0);
  }

  private void updateColorScheme() {
    Observable.from(minifiedMovie.getPosterPath())
        .map(new Func1<String, Bitmap>() {
          @Override public Bitmap call(String url) {
            try {
              return picasso.load(url).get();
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        })
        .map(new Func1<Bitmap, ColorScheme>() {
          @Override public ColorScheme call(Bitmap bitmap) {
            return ColorScheme.fromBitmap(bitmap);
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new EndlessObserver<ColorScheme>() {
          @Override public void onNext(ColorScheme colorScheme) {
            animateBackgroundColor(moviePrimary, colorScheme.getPrimaryText());
            animateBackgroundColor(moviePrimaryAccent, colorScheme.getPrimaryAccent());
            animateBackgroundColor(movieSecondary, colorScheme.getSecondaryText());
            animateBackgroundColor(movieSecondaryAccent, colorScheme.getSecondaryAccent());
            animateBackgroundColor(movieTertiaryAccent, colorScheme.getTertiaryAccent());
          }
        });
  }

  private void animateBackgroundColor(View view, int endColor) {
    ColorDrawable layers[] = new ColorDrawable[2];
    layers[0] = new ColorDrawable(getResources().getColor(R.color.background));
    layers[1] = new ColorDrawable(endColor);

    TransitionDrawable drawable = new TransitionDrawable(layers);
    view.setBackground(drawable);
    drawable.startTransition(ANIMATION_DURATION);
  }

  @Override protected void inflateLayout(ViewGroup container) {
    getLayoutInflater().inflate(R.layout.activity_movie, container);
  }

  private void init() {
    smoothInterpolator = new AccelerateDecelerateInterpolator();
    headerHeight = getResources().getDimensionPixelSize(R.dimen.movie_header_height);
    minHeaderTranslation = -headerHeight + getActionBarHeight();
    actionBarTitleColor = getResources().getColor(android.R.color.white);
    alphaForegroundColorSpan = new AlphaForegroundColorSpan(actionBarTitleColor);

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    setTitleAlpha(0);

    scrollView.setOnScrollChangedListener(this);
    updateColorScheme();
  }

  public int getActionBarHeight() {
    if (actionBarHeight != 0) return actionBarHeight;

    getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
    actionBarHeight =
        TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());

    return actionBarHeight;
  }

  /**
   * Set the alpha value for the action bar title.
   */
  private void setTitleAlpha(float alpha) {
    alphaForegroundColorSpan.setAlpha(alpha);
    spannableString.setSpan(alphaForegroundColorSpan, 0, spannableString.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    getActionBar().setTitle(spannableString);
  }

  /**
   * Standard math clamp method https://en.wikipedia.org/wiki/Clamping_(graphics)
   *
   * @param value value to clamp
   * @param max maximum to return
   * @param min minimum to return
   * @return min if value < min, max if value > max, else value
   */
  public static float clamp(float value, float max, float min) {
    return Math.max(Math.min(value, min), max);
  }

  /**
   * Interpolate between two views.
   * This will translate source to somewhere between source and target depending on the
   * interpolation
   * value.
   * Used to translate the logo to the action bar icon.
   *
   * @param interpolation 'progress' of the interpolation
   */
  private void interpolate(View source, View target, float interpolation) {
    getOnScreenRect(sourceRect, source);
    getOnScreenRect(targetRect, target);

    float scaleX = 1.0F + interpolation * (targetRect.width() / sourceRect.width() - 1.0F);
    float scaleY = 1.0F + interpolation * (targetRect.height() / sourceRect.height() - 1.0F);
    float translationX = interpolation * (targetRect.left - sourceRect.left);
    float translationY = interpolation * (targetRect.top - sourceRect.top);

    source.setTranslationX(translationX);
    source.setTranslationY(translationY);
    source.setScaleX(scaleX);
    source.setScaleY(scaleY);
  }

  /**
   * Get the position of the view into the given RectF.
   */
  private RectF getOnScreenRect(RectF rect, View view) {
    rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    return rect;
  }

  @Override public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
    int scrollY = who.getScrollY();
    movieHeader.setTranslationY(Math.max(-scrollY, minHeaderTranslation));
    float ratio = clamp(movieHeader.getTranslationY() / minHeaderTranslation, 0.0f, 1.0f);
    interpolate(moviePoster, actionBarIconView, smoothInterpolator.getInterpolation(ratio));
    float alpha = clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F);
    setTitleAlpha(alpha);
  }
}
