package de.nopefrogking.android

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.NumberPicker
import android.widget.TextView
import com.badlogic.gdx.Gdx
import de.nopefrogking.ui.GDXNumberPrompt
import de.nopefrogking.ui.NumberPromptListener
import de.tomgrill.gdxdialogs.core.GDXDialogsVars

class GDXNumberPrompt(private val activity: Activity) : GDXNumberPrompt {
    private lateinit var titleView: TextView
    private lateinit var messageView: TextView

    private lateinit var userInput: NumberPicker
    private lateinit var alertDialog: AlertDialog

    override var message: CharSequence = ""
    override var title: CharSequence = ""

    override var value = 0
    override var minValue = 0
    override var maxValue = 0

    override var cancelText: CharSequence = ""
    override var confirmText: CharSequence = ""

    private var listener: NumberPromptListener? = null
    private var inputValue: Int = 0
    private var isBuild = false

    override fun show(): GDXNumberPrompt {
        if (!isBuild) {
            throw RuntimeException(GDXNumberPrompt::class.java.simpleName + " has not been build. Use build() before" +
                    " show().")
        }

        userInput.value = inputValue


        activity.runOnUiThread {
            Gdx.app.debug(GDXDialogsVars.LOG_TAG, GDXNumberPrompt::class.java.simpleName + " now shown.")
            alertDialog.show()
        }

        return this
    }

    override fun build(): GDXNumberPrompt {
        activity.runOnUiThread({
            //
            val alertDialogBuilder = AlertDialog.Builder(activity)
            val li = LayoutInflater.from(activity)
            //

            val promptsView = li.inflate(getResourceId("number_dialog", "layout"), null)

            alertDialogBuilder.setView(promptsView)

            userInput = promptsView.findViewById(getResourceId("gdxDialogsNumberInput", "id")) as NumberPicker
            userInput.maxValue = maxValue
            userInput.minValue = minValue
            userInput.wrapSelectorWheel = false
            userInput.value = value

            titleView = promptsView.findViewById(getResourceId("gdxDialogsEnterTitle", "id")) as TextView
            messageView = promptsView.findViewById(getResourceId("gdxDialogsEnterMessage", "id")) as TextView

            titleView.text = title
            messageView.text = message

            alertDialogBuilder.setCancelable(false)
                .setPositiveButton(confirmText) { _, _ ->
                    listener?.confirm(userInput.value)
                }.setNegativeButton(cancelText) { dialog, _ ->
                    dialog.cancel()
                    listener?.cancel()
                }
            // create alert dialog alertDialog =
            alertDialog = alertDialogBuilder.create()
            isBuild = true
        })

        // Wait till TextPrompt is built.
        while (!isBuild) {
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                //Again, never supposed to run, never hurts to know if it does.
                e.printStackTrace()
            }

        }

        return this
    }

    fun getResourceId(pVariableName: String, pVariableType: String): Int {
        try {
            return activity.resources.getIdentifier(pVariableName, pVariableType, activity.packageName)
        } catch (e: Exception) {
            Gdx.app.error(GDXDialogsVars.LOG_TAG, "Cannot find resouce with name: " + pVariableName +
                    " Did you copy the layouts to /res/layouts and /res/layouts_v14 ?")
            e.printStackTrace()
            return -1
        }

    }

    override fun setNumberPromptListener(listener: NumberPromptListener): GDXNumberPrompt {
        this.listener = listener
        return this
    }

    override fun dismiss(): GDXNumberPrompt {

        if (!isBuild) {
            throw RuntimeException(GDXNumberPrompt::class.java.simpleName + " has not been build. Use build() " +
                    "before dismiss().")
        }

        Gdx.app.debug(GDXDialogsVars.LOG_TAG, de.nopefrogking.android.GDXNumberPrompt::class.java.simpleName + " dismissed.")
        alertDialog.dismiss() //Method is thread-safe.


        return this
    }
}