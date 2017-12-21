package de.nopefrogking.utils

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor

interface DetachableInputProcessor : InputProcessor {
    var detached : Boolean

}

abstract class DetachableInputProcessorAdapter : DetachableInputProcessor, InputAdapter(){
     override var detached = false


    
}
