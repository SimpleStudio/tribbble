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

package me.selinali.tribbble.ui.shot;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Base64;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.selinali.tribbble.BuildConfig;
import me.selinali.tribbble.R;
import me.selinali.tribbble.model.Shot;
import me.selinali.tribbble.ui.common.Bindable;
import me.selinali.tribbble.utils.DateUtils;

import static me.selinali.tribbble.utils.ViewUtils.tintDrawable;

public class ShotCardView extends CardView implements Bindable<Shot> {

  @BindView(R.id.imageview_shot) RatioImageView mShotImageView;
  @BindView(R.id.textview_descript) TextView mDescriptTextView;
  @BindView(R.id.textview_likes_count) TextView mLikesTextView;
  @BindView(R.id.textview_views_count) TextView mViewsTextView;
  @BindView(R.id.adview_card) AdView mAdView;

  private AdRequest mAdRequest;

  public ShotCardView(Context context) {
    super(context);
    inflate(context, R.layout.shot_card_view, this);
    ButterKnife.bind(this);

    tintDrawable(mLikesTextView, 0);
    tintDrawable(mViewsTextView, 0);

    AdRequest.Builder adBuilder = new AdRequest.Builder();
    if (BuildConfig.DEBUG) {
      adBuilder.addTestDevice("2F1959F56393231DB5DDFB6B50F6E32D");
//      adBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    }
    mAdRequest = adBuilder.build();
  }

  @Override public void bind(Shot shot) {
    Glide.with(getContext())
        .load(shot.getImages().getHighResImage())
        .placeholder(R.drawable.grid_item_placeholder)
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .into(mShotImageView);
    mDescriptTextView.setText(Html.fromHtml(new String(Base64.decode(shot.getDescription(), Base64.NO_WRAP))));
    mLikesTextView.setText(String.valueOf(shot.getLikesCount()));
    mViewsTextView.setText(String.valueOf(shot.getViewsCount()));
    mAdView.loadAd(mAdRequest);
  }
}