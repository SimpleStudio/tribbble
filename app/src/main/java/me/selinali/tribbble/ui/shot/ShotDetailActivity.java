package me.selinali.tribbble.ui.shot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.selinali.tribbble.R;
import me.selinali.tribbble.model.Shot;
import uk.co.senab.photoview.PhotoView;

public class ShotDetailActivity extends AppCompatActivity {

    @BindView(R.id.img_shot_detail) PhotoView mShotDetailImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_detail);
        ButterKnife.bind(this);

        Shot shot = Parcels.unwrap(getIntent().getParcelableExtra(ShotActivity.EXTRA_SHOT));

        Glide.with(this)
                .load(shot.getImages().getHighResImage())
                .placeholder(R.drawable.grid_item_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mShotDetailImageView);
    }
}
