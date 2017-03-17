package me.selinali.tribbble.ui.shot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.youmi.android.normal.spot.SpotListener;
import net.youmi.android.normal.spot.SpotManager;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.selinali.tribbble.R;
import me.selinali.tribbble.model.Shot;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ShotDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SHOT = "EXTRA_SHOT";

    @BindView(R.id.img_shot_detail) PhotoView mShotDetailImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_detail);
        ButterKnife.bind(this);

        Shot shot = Parcels.unwrap(getIntent().getParcelableExtra(ShotDetailActivity.EXTRA_SHOT));

        Glide.with(this)
                .load(shot.getImages().getHighResImage())
                .placeholder(R.drawable.grid_item_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mShotDetailImageView);

        mShotDetailImageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                onBackPressed();
            }
        });

        SpotManager.getInstance(this).showSlideableSpot(this,
                new SpotListener() {
                    @Override
                    public void onShowSuccess() {
                        
                    }

                    @Override
                    public void onShowFailed(int i) {

                    }

                    @Override
                    public void onSpotClosed() {

                    }

                    @Override
                    public void onSpotClicked(boolean b) {

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 插屏广告
        SpotManager.getInstance(this).onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 插屏广告
        SpotManager.getInstance(this).onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 插屏广告
        SpotManager.getInstance(this).onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 如果有需要，可以点击后退关闭插播广告。
        if (SpotManager.getInstance(this).isSpotShowing()) {
            SpotManager.getInstance(this).hideSpot();
        } else {
            super.onBackPressed();
        }
    }
}
