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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wenchao.cardstack.CardStack;

import org.parceler.Parcels;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.selinali.tribbble.R;
import me.selinali.tribbble.Selinali;
import me.selinali.tribbble.api.Dribble;
import me.selinali.tribbble.data.ArchiveManager;
import me.selinali.tribbble.model.Shot;
import me.selinali.tribbble.ui.MainActivity;
import me.selinali.tribbble.ui.common.Bindable;
import me.selinali.tribbble.ui.shot.ShotActivity;
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
  private static final int PRELOAD_THRESHOLD = 1;

  @BindView(R.id.card_stack) CardStack mCardStack;
  @BindView(R.id.progress_view) View mProgressView;
  @BindView(R.id.conection_error_container) View mErrorContainer;
  @BindView(R.id.conection_empty_container) View mEmptyContainer;

  private Subscription mSubscription;
  private Unbinder mUnbinder;
  private DeckAdapter mAdapter;
  private int mCurrentPage = 1;
  private int mCurrentPosition = 0;

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
      intent.putExtra(ShotActivity.EXTRA_SHOT, Parcels.wrap(shot));

      ActivityOptionsCompat options =
              ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                      mCardStack.findViewById(R.id.imageview_shot), getString(R.string.transition_shot_image));
      ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }
  };

  private void loadNext(int delay) {
    Selinali.unsubscribe(mSubscription);
    mSubscription = Dribble.instance()
        .getShots(mCurrentPage,
            DeckFragment::shouldShow,
            shots -> shots.size() >= PRELOAD_THRESHOLD || shots.size() == 0,
            page -> mCurrentPage = page
        )
        .delaySubscription(delay, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::bind, this::handleError);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    if (shots.isEmpty() && mProgressView.isShown()) {
      handleEmpty();
    }
  }

  @OnClick({R.id.textview_retry, R.id.textview_empty}) public void onRetryClicked() {
    mProgressView.setVisibility(View.VISIBLE);
    ViewUtils.fadeView(mErrorContainer, false, 250);
    ViewUtils.fadeView(mEmptyContainer, false, 250);
    loadNext(500);
  }

  private void setupPadding() {
    int navigationBarHeight = ViewUtils.getNavigationBarHeight();
    mCardStack.setPadding(ViewUtils.dpToPx(14), ViewUtils.dpToPx(52),
        ViewUtils.dpToPx(14), navigationBarHeight + ViewUtils.dpToPx(80));
    mProgressView.setPadding(0, 0, 0, navigationBarHeight + ViewUtils.dpToPx(80));
    mErrorContainer.setPadding(0, 0, 0, navigationBarHeight);
  }

  private static boolean shouldShow(Shot shot) {
    return !ArchiveManager.instance().isArchived(shot) &&
        !ArchiveManager.instance().isDiscarded(shot);
  }

  private void handleError(Throwable throwable) {
    Log.d(TAG, "Failed to load shot", throwable);
//    Crashlytics.logException(throwable);
    mProgressView.setVisibility(View.INVISIBLE);
    ViewUtils.fadeView(mCardStack, false, 250);
    ViewUtils.fadeView(mEmptyContainer, false, 250);
    ViewUtils.fadeView(mErrorContainer, true, 250);
  }

  private void handleEmpty() {
    Log.d(TAG, "Touch empty");
    mProgressView.setVisibility(View.INVISIBLE);
    ViewUtils.fadeView(mCardStack, false, 250);
    ViewUtils.fadeView(mErrorContainer, false, 250);
    ViewUtils.fadeView(mEmptyContainer, true, 250);
  }
}
