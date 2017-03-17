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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.youmi.android.normal.banner.BannerManager;
import net.youmi.android.normal.banner.BannerViewListener;

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
  @BindView(R.id.ad_banner) LinearLayout mBannerLayout;

  public ShotCardView(Context context) {
    super(context);
    inflate(context, R.layout.shot_card_view, this);
    ButterKnife.bind(this);

    tintDrawable(mLikesTextView, 0);
    tintDrawable(mViewsTextView, 0);

    // 获取广告条
    View bannerView = BannerManager.getInstance(getContext())
            .getBannerView(getContext(), new BannerViewListener() {
              @Override
              public void onRequestSuccess() {

              }

              @Override
              public void onSwitchBanner() {

              }

              @Override
              public void onRequestFailed() {

              }
            });
    // 将广告条加入到布局中
    mBannerLayout.addView(bannerView);
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
  }

  @Override
  protected void onDetachedFromWindow() {
    // 展示广告条窗口的 onDestroy() 回调方法中调用
    BannerManager.getInstance(getContext()).onDestroy();
    super.onDetachedFromWindow();
  }
}