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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mobisage.android.MobiSageAdBanner;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.selinali.tribbble.R;
import me.selinali.tribbble.model.Shot;
import me.selinali.tribbble.ui.common.Bindable;

import static me.selinali.tribbble.utils.ViewUtils.tintDrawable;

public class ShotCardView extends CardView implements Bindable<Shot> {

  @BindView(R.id.imageview_shot) RatioImageView mShotImageView;
  @BindView(R.id.textview_descript) TextView mDescriptTextView;
  @BindView(R.id.textview_likes_count) TextView mLikesTextView;
  @BindView(R.id.textview_views_count) TextView mViewsTextView;
  @BindView(R.id.ad_container) LinearLayout mAdContainer;

  private MobiSageAdBanner mAdView;

  public ShotCardView(Context context) {
    super(context);
    inflate(context, R.layout.shot_card_view, this);
    ButterKnife.bind(this);

    tintDrawable(mLikesTextView, 0);
    tintDrawable(mViewsTextView, 0);

    // 实例化广告(广告尺寸自适应不同分辨率设备), 传入相应类型 slottoken, true 为大尺寸,false 为小尺寸
    mAdView = new MobiSageAdBanner(getContext(), "zczNHqJaQNf9bIqXSjHM8EHr");
    // 设置轮播时间
    mAdView.setAdRefreshInterval(MobiSageAdBanner.AD_REFRESH_60);
    // 设置轮播动画
    mAdView.setAnimeType(MobiSageAdBanner.ANIME_NOANIME);
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

    // 将广告加入容器
    mAdContainer.addView(mAdView);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (mAdView != null) {
      mAdView.destroyAdView();
      mAdView = null;
    }
  }
}