package com.sd.lib.vlevel.callback;

import android.view.View;

import com.sd.lib.vlevel.FVisibleLevelManager;
import com.sd.lib.vlevel.callback.item.FLevelItemCallback;

import java.lang.ref.WeakReference;

public class ViewItemVisibleCallbackAdapter extends TargetItemVisibleCallbackAdapter
{
    public ViewItemVisibleCallbackAdapter(String levelName, String levelItemName, View view)
    {
        super(levelName, levelItemName, new ViewLevelItemCallback(view));
        /**
         * 由于{@link FVisibleLevelManager.Level}内部采用的是弱引用来保存回调对象，所以这边设置一个内部类，让View持有当前对象，以免当前对象被回收
         */
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener()
        {
            @Override
            public void onViewAttachedToWindow(View v)
            {
            }

            @Override
            public void onViewDetachedFromWindow(View v)
            {
            }
        });
    }

    private static final class ViewLevelItemCallback implements FLevelItemCallback
    {
        private final WeakReference<FLevelItemCallback> mCallback;

        public ViewLevelItemCallback(View view)
        {
            if (!(view instanceof FLevelItemCallback))
                throw new IllegalArgumentException("view should be instance of " + FLevelItemCallback.class.getName());

            final FLevelItemCallback callback = (FLevelItemCallback) view;
            mCallback = new WeakReference<>(callback);
        }

        @Override
        public void onLevelItemVisibleChanged(boolean visible)
        {
            final FLevelItemCallback callback = mCallback.get();
            if (callback != null)
                callback.onLevelItemVisibleChanged(visible);
        }
    }
}
