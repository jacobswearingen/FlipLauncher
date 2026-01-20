// KeyEventHandler.kt
package com.jacobswearingen.fliplauncher

interface KeyEventHandler {
    fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean
}