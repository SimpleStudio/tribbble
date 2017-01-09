package me.selinali.tribbble.ui.cardstack;

import android.view.View;

/**
 * copy from {@link com.wenchao.cardstack.DefaultStackEventListener}
 * just rename class, nothing changed
 */
public class DefaultStackEventListenerPort implements CardStackPort.CardEventListener {

    private float mThreshold;

    public DefaultStackEventListenerPort(int i) {
        mThreshold = i;
    }

    @Override
    public boolean swipeEnd(View cardView, int section, float distance) {
        return distance > mThreshold;
    }

    @Override
    public boolean swipeStart(int section, float distance) {

        return false;
    }

    @Override
    public boolean swipeContinue(View cardView, int section, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void discarded(int mIndex,int direction) {

    }

    @Override
    public void topCardTapped() {

    }

    protected float getThreshold() {
        return mThreshold;
    }


}
