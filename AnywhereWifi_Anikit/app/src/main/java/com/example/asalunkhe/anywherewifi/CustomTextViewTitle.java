/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015  ARRIS Group Inc.                                        *
 *  *  * All Rights Reserved                                                         *
 *  *  * No portions of this material may be reproduced in any form without the      *
 *  *  * written permission of ARRIS.                                                *
 *  *  * *
 *  *  * All information contained in this document is Arris Group, Inc. company     *
 *  *  * private, proprietary, and trade secret.                                     *
 *  *  * *
 *  *  * ******************************************************************************
 *  *  * $Id:                                                                        *
 *  *  * Filename: CustomTextView.java                                                       *
 *  *  * Author  : apaulraj                                                          *
 *  *  * Creation Date: 11/25/2015                                                 *
 *  *  * *
 *  *  * ******************************************************************************
 *  *  * Description:                                                                *
 *  *  * Custom text view with custom fonts applied across the app                          *
 *  *  * *
 *  *  * ******************************************************************************
 *  *  * Revision History:                                                           *
 *  *  * Author                 Date                    Description                  *
 *  *  * apaulraj		        11/25/2015     Custom text view with custom fonts applied across the app*
 *  *  ******************************************************************************
 *
 */

package com.example.asalunkhe.anywherewifi;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextViewTitle extends android.support.v7.widget.AppCompatTextView {

    public CustomTextViewTitle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomTextViewTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextViewTitle(Context context) {
        super(context);
    }


    public void setTypeface(Typeface tf) {
        super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "avenir.otf"));
    }
}