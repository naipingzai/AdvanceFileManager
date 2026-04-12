package coil.target

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.transition.TransitionTarget

/**
 * An opinionated [ViewTarget] that simplifies updating the [Drawable] attached to a [View]
 * and supports automatically starting and stopping animated [Drawable]s.
 *
 * If you need custom behaviour that this class doesn't support it's recommended
 * to implement [ViewTarget] directly.
 */
abstract class GenericViewTarget<T : View> : ViewTarget<T>, TransitionTarget, DefaultLifecycleObserver {

    private var isStarted = false

    /**
     * The current [Drawable] attached to [view].
     */
    abstract override var drawable: Drawable?

    override fun onStart(placeholder: Drawable?) = updateDrawable(placeholder)

    override fun onError(error: Drawable?) = updateDrawable(error)

    override fun onSuccess(result: Drawable) = updateDrawable(result)

    // Explicit DefaultLifecycleObserver overrides for lifecycle 2.9.x compatibility.
    override fun onCreate(owner: LifecycleOwner) {}

    override fun onStart(owner: LifecycleOwner) {
        isStarted = true
        updateAnimation()
    }

    override fun onResume(owner: LifecycleOwner) {}
    override fun onPause(owner: LifecycleOwner) {}

    override fun onStop(owner: LifecycleOwner) {
        isStarted = false
        updateAnimation()
    }

    override fun onDestroy(owner: LifecycleOwner) {}

    /** Replace the [ImageView]'s current drawable with [drawable]. */
    protected fun updateDrawable(drawable: Drawable?) {
        (this.drawable as? Animatable)?.stop()
        this.drawable = drawable
        updateAnimation()
    }

    /** Start/stop the current [Drawable]'s animation based on the current lifecycle state. */
    protected fun updateAnimation() {
        val animatable = drawable as? Animatable ?: return
        if (isStarted) animatable.start() else animatable.stop()
    }
}
