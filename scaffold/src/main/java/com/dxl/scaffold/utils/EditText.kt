package com.dxl.scaffold.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 *
 * @author duxiaolong
 */
fun EditText?.focusAndShowKeyboardDelayed() {
    this?.postDelayed({
        this.focusAndShowKeyboard()
    }, 200)
}

fun EditText?.hideInputKeyboard() {
    this?.let {
        val systemService = context.getSystemService(Context.INPUT_METHOD_SERVICE)
        if (systemService is InputMethodManager) {
            systemService.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}

fun EditText?.focusAndShowKeyboard() {
    this?.let {
        isFocusable = true
        isFocusableInTouchMode = true
        setSelection(text.length)
        requestFocus()
        val systemService = context.getSystemService(Context.INPUT_METHOD_SERVICE)
        if (systemService is InputMethodManager) {
            systemService.showSoftInput(this, 0)
        }
    }
}