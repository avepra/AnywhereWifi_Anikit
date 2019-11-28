package com.example.asalunkhe.anywherewifi;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by apaulraj on 11/24/2017.
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context) {
        super(context);
    }


    public void setTypeface(Typeface tf) {
        super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "avenir_regular.otf"));
    }
}
