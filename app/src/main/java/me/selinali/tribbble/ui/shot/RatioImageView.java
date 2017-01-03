package me.selinali.tribbble.ui.shot;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 *
 * 4:3比例的ImageView
 * @author pcqpcq
 * @version 1.0.0
 * @since 2017/1/3 下午2:18
 */

public class RatioImageView extends ImageView {
    public RatioImageView(Context context) {
        super(context);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //   let the default measuring occur, then force the desired aspect ratio
        //   on the view (not the drawable).
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        //force a 4:3 aspect ratio
        int height = Math.round(width * .75f);
        setMeasuredDimension(width, height);
    }
}
