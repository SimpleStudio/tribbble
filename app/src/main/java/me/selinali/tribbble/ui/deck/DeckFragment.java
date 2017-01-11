/*
 * Copyright 2016 Selina Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.selinali.tribbble.ui.deck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.parceler.Parcels;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.selinali.tribbble.R;
import me.selinali.tribbble.Selinali;
import me.selinali.tribbble.TribbbleApp;
import me.selinali.tribbble.api.Dribble;
import me.selinali.tribbble.data.ArchiveManager;
import me.selinali.tribbble.model.Shot;
import me.selinali.tribbble.ui.MainActivity;
import me.selinali.tribbble.ui.cardstack.CardStackPort;
import me.selinali.tribbble.ui.common.Bindable;
import me.selinali.tribbble.ui.shot.ShotDetailActivity;
import me.selinali.tribbble.utils.ViewUtils;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DeckFragment extends Fragment implements Bindable<List<Shot>> {

  public DeckFragment() {}

  public static Fragment newInstance() {
    return new DeckFragment();
  }

  private static final String TAG = DeckFragment.class.getSimpleName();
  private static final int PRELOAD_THRESHOLD = 5;
  private static final String LAST_SHOTS_PAGE_KEY = "LAST_SHOTS_PAGE";

  @BindView(R.id.card_stack) CardStackPort mCardStack;
  @BindView(R.id.progress_view) View mProgressView;
  @BindView(R.id.conection_error_container) View mErrorContainer;
  @BindView(R.id.conection_empty_container) View mEmptyContainer;

  private SharedPreferences mPreference;
  private Subscription mSubscription;
  private Unbinder mUnbinder;
  private DeckAdapter mAdapter;
  private int mCurrentPage = 0;
  private int mCurrentPosition = 0;
  private int mLastPageSize = 0;
  private boolean mIsRetry = false;

  private DeckListener mDeckListener = new DeckListener() {
    @Override void onCardSwiped(int direction, int swipedIndex) {
      mCurrentPosition++;
      if (mAdapter.getCount() - swipedIndex <= PRELOAD_THRESHOLD) {
        mCurrentPage++;
        loadNext(0);
      }

      if (direction == RIGHT) {
//        Answers.getInstance().logCustom(new CustomEvent("Shot Archived"));
        Shot shot = mAdapter.getItem(swipedIndex);
        ArchiveManager.instance().archive(shot);
        ((MainActivity) getActivity()).notifyShotArchived(shot);
      } else if (direction == LEFT) {
//        Answers.getInstance().logCustom(new CustomEvent("Shot Discarded"));
        ArchiveManager.instance().discard(mAdapter.getItem(swipedIndex));
      }
    }

    @Override public void topCardTapped() {
      Shot shot = mAdapter.getItem(mCurrentPosition);
      Intent intent = new Intent(getContext(), ShotDetailActivity.class);
      intent.putExtra(ShotDetailActivity.EXTRA_SHOT, Parcels.wrap(shot));
      startActivity(intent);
    }

    @Override
    public boolean swipeContinue(View view, int section, float distanceX, float distanceY) {
      // swipe left or right
      ImageView icon = (ImageView) view.findViewById(R.id.view_cover_icon);
      if (section % 2 == 0) {
        // left
        icon.setBackgroundResource(R.drawable.ic_trash_light);
      } else {
        // right
        icon.setBackgroundResource(R.drawable.ic_likes_light);
      }
      // update alpha
      icon.setAlpha(distanceX / getThreshold());
      return super.swipeContinue(view, section, distanceX, distanceY);
    }

    @Override
    public boolean swipeEnd(View view, int section, float distance) {
      if (distance < getThreshold()) {
        view.findViewById(R.id.view_cover_icon).setAlpha(0);
      }
      return super.swipeEnd(view, section, distance);
    }
  };

  private void loadNext(int delay) {
    Selinali.unsubscribe(mSubscription);
    mSubscription = Dribble.instance()
        .getShots(mCurrentPage,
            DeckFragment::shouldShow,
            shots -> (mLastPageSize == 0 && shots.size() == 0) || (mLastPageSize = shots.size()) >= PRELOAD_THRESHOLD,
            page -> mCurrentPage = page
        )
        .delaySubscription(delay, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::bind, this::handleError);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPreference = TribbbleApp.context().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
    mCurrentPage = mPreference.getInt(LAST_SHOTS_PAGE_KEY, 0);

    loadNext(0);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
    View view = inflater.inflate(R.layout.fragment_deck, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    setupPadding();
    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mPreference.edit().putInt(LAST_SHOTS_PAGE_KEY, mCurrentPage).apply();

    mUnbinder.unbind();
    Selinali.unsubscribe(mSubscription);
  }

  @Override public void bind(List<Shot> shots) {
    ViewUtils.fadeView(mCardStack, true, 250);
    if (mAdapter == null) {
      mAdapter = new DeckAdapter(getContext(), shots);
      mCardStack.setListener(mDeckListener);
      mCardStack.setAdapter(mAdapter);
    } else {
      mAdapter.addAll(shots);
    }
    // 空内容处理
    if (shots.isEmpty() && mCardStack.getCurrIndex() == mAdapter.getCount()) {
      handleEmpty();
    }
    // reset retry
    mIsRetry = false;
  }

  @OnClick({R.id.textview_retry, R.id.textview_empty}) public void onRetryClicked() {
    ViewUtils.fadeView(mErrorContainer, false, 150);
    ViewUtils.fadeView(mEmptyContainer, false, 150);
    mProgressView.setVisibility(View.VISIBLE);

    // try next page
    mCurrentPage += 1;
    mIsRetry = true;

    // 刷新
    loadNext(500);
  }

  private void setupPadding() {
    int navigationBarHeight = ViewUtils.getNavigationBarHeight();
    mCardStack.setPadding(ViewUtils.dpToPx(14), ViewUtils.dpToPx(52),
        ViewUtils.dpToPx(14), navigationBarHeight);
    mProgressView.setPadding(0, 0, 0, navigationBarHeight);
    mErrorContainer.setPadding(0, 0, 0, navigationBarHeight);
  }

  private static boolean shouldShow(Shot shot) {
    return !ArchiveManager.instance().isArchived(shot) &&
        !ArchiveManager.instance().isDiscarded(shot);
  }

  private void handleError(Throwable throwable) {
    // 展示错误UI
    Log.d(TAG, "Failed to load shot", throwable);
//    Crashlytics.logException(throwable);
    mProgressView.setVisibility(View.INVISIBLE);
    ViewUtils.fadeView(mCardStack, false, 150);
    ViewUtils.fadeView(mEmptyContainer, false, 150);
    ViewUtils.fadeView(mErrorContainer, true, 250);
  }

  private void handleEmpty() {
    if (mIsRetry) {
      mCurrentPage -= 1;
    }
    // 展示空UI
    Log.d(TAG, "Touch empty");
    mProgressView.setVisibility(View.INVISIBLE);
    ViewUtils.fadeView(mCardStack, false, 150);
    ViewUtils.fadeView(mErrorContainer, false, 150);
    ViewUtils.fadeView(mEmptyContainer, true, 250);
  }
}
