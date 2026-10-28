package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

data class ActionResult(
    val success: Boolean,
    val message: String
)

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        _isServiceActive.value = true
        Log.i(TAG, "JarvisAccessibilityService connected and ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Events monitored for window state changes and interactive transitions
    }

    override fun onInterrupt() {
        Log.w(TAG, "JarvisAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instanceRef?.get() == this) {
            instanceRef = null
            _isServiceActive.value = false
        }
        Log.i(TAG, "JarvisAccessibilityService destroyed")
    }

    /**
     * Finds and clicks an element on screen that matches the query text or content description.
     */
    fun clickElement(targetText: String): ActionResult {
        val root = rootInActiveWindow ?: return ActionResult(
            success = false,
            message = "No active screen window found. Target app may not expose accessible elements."
        )

        try {
            val matchingNode = findNodeByTextRecursive(root, targetText.trim().lowercase())
            if (matchingNode == null) {
                return ActionResult(
                    success = false,
                    message = "Could not find element with text \"$targetText\" on current screen."
                )
            }

            // Find clickable target: either the node or its closest clickable parent
            var clickableNode: AccessibilityNodeInfo? = matchingNode
            while (clickableNode != null && !clickableNode.isClickable) {
                val parent = clickableNode.parent
                clickableNode = parent
            }

            val target = clickableNode ?: matchingNode
            val performed = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            return if (performed) {
                ActionResult(success = true, message = "Tapped \"$targetText\" successfully.")
            } else {
                ActionResult(success = false, message = "Element found, but click action could not be performed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing click action", e)
            return ActionResult(success = false, message = "Screen interaction error: ${e.message}")
        }
    }

    /**
     * Scrolls the current screen either down (forward) or up (backward).
     */
    fun scrollScreen(scrollDown: Boolean): ActionResult {
        val root = rootInActiveWindow ?: return ActionResult(
            success = false,
            message = "No active screen window available to scroll."
        )

        try {
            val scrollableNode = findFirstScrollableNode(root)
            if (scrollableNode == null) {
                return ActionResult(
                    success = false,
                    message = "No scrollable container found on this screen."
                )
            }

            val action = if (scrollDown) {
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            } else {
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            }

            val performed = scrollableNode.performAction(action)
            val direction = if (scrollDown) "down" else "up"

            return if (performed) {
                ActionResult(success = true, message = "Scrolled screen $direction.")
            } else {
                ActionResult(
                    success = false,
                    message = "Could not scroll further $direction on this screen."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing scroll", e)
            return ActionResult(success = false, message = "Scroll error: ${e.message}")
        }
    }

    /**
     * Enters text into an accessible input field currently focused or visible on screen.
     */
    fun enterText(textToEnter: String): ActionResult {
        val root = rootInActiveWindow ?: return ActionResult(
            success = false,
            message = "No active screen window found."
        )

        try {
            // Check focused node first, or find first editable node
            val targetNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: findFirstEditableNode(root)

            if (targetNode == null) {
                return ActionResult(
                    success = false,
                    message = "No active or editable text field found on screen."
                )
            }

            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToEnter)
            }
            val performed = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            return if (performed) {
                ActionResult(success = true, message = "Entered \"$textToEnter\" into field.")
            } else {
                ActionResult(success = false, message = "Failed to input text into the selected field.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error entering text", e)
            return ActionResult(success = false, message = "Text input error: ${e.message}")
        }
    }

    /**
     * Navigates back via system global action.
     */
    fun performBack(): ActionResult {
        val performed = performGlobalAction(GLOBAL_ACTION_BACK)
        return if (performed) {
            ActionResult(success = true, message = "Navigated back.")
        } else {
            ActionResult(success = false, message = "Back navigation failed.")
        }
    }

    /**
     * Navigates to home screen via system global action.
     */
    fun performHome(): ActionResult {
        val performed = performGlobalAction(GLOBAL_ACTION_HOME)
        return if (performed) {
            ActionResult(success = true, message = "Navigated to home screen.")
        } else {
            ActionResult(success = false, message = "Home navigation failed.")
        }
    }

    /**
     * Opens system app overview / recents screen.
     */
    fun performRecents(): ActionResult {
        val performed = performGlobalAction(GLOBAL_ACTION_RECENTS)
        return if (performed) {
            ActionResult(success = true, message = "Opened recent apps.")
        } else {
            ActionResult(success = false, message = "Could not open recent apps.")
        }
    }

    /**
     * Opens system notifications shade.
     */
    fun performNotifications(): ActionResult {
        val performed = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        return if (performed) {
            ActionResult(success = true, message = "Opened notifications.")
        } else {
            ActionResult(success = false, message = "Could not open notifications.")
        }
    }

    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, query: String): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString()?.lowercase()
        val contentDesc = node.contentDescription?.toString()?.lowercase()

        if ((nodeText != null && nodeText.contains(query)) ||
            (contentDesc != null && contentDesc.contains(query))) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByTextRecursive(child, query)
            if (found != null) {
                return found
            }
        }
        return null
    }

    private fun findFirstScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFirstScrollableNode(child)
            if (found != null) {
                return found
            }
        }
        return null
    }

    private fun findFirstEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFirstEditableNode(child)
            if (found != null) {
                return found
            }
        }
        return null
    }

    companion object {
        private const val TAG = "JarvisAccessibility"
        private var instanceRef: WeakReference<JarvisAccessibilityService>? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        val instance: JarvisAccessibilityService?
            get() = instanceRef?.get()

        /**
         * Checks whether the user has enabled this Accessibility Service in Android Settings.
         */
        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, JarvisAccessibilityService::class.java)
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledComponent = ComponentName.unflattenFromString(componentNameString)
                if (enabledComponent != null && enabledComponent == expectedComponentName) {
                    return true
                }
            }
            return false
        }
    }
}
