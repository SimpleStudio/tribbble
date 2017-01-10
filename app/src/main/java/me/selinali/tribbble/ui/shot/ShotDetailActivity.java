package me.selinali.tribbble.ui.shot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
    }
}
