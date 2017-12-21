package de.nopefrogking.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle


class RoundedRectangleDrawable(color: Color, blendingEnabled: Boolean = false, var radius: Float = 0.0f): ColorDrawable(color, blendingEnabled) {
    private fun roundedRect(shape: ShapeRenderer, bounds: Rectangle, radius: Float){
        // Central rectangle
        shape.rect(bounds.x + radius, bounds.y + radius, bounds.width - 2*radius, bounds.height - 2*radius);

        // Four side rectangles, in clockwise order
        shape.rect(bounds.x + radius, bounds.y, bounds.width - 2*radius, radius);
        shape.rect(bounds.x + bounds.width - radius, bounds.y + radius, radius, bounds.height - 2*radius);
        shape.rect(bounds.x + radius, bounds.y + bounds.height - radius, bounds.width - 2*radius, radius);
        shape.rect(bounds.x, bounds.y + radius, radius, bounds.height - 2*radius);

        // Four arches, clockwise too
        shape.arc(bounds.x + radius, bounds.y + radius, radius, 180f, 90f);
        shape.arc(bounds.x + bounds.width - radius, bounds.y + radius, radius, 270f, 90f);
        shape.arc(bounds.x + bounds.width - radius, bounds.y + bounds.height - radius, radius, 0f, 90f);
        shape.arc(bounds.x + radius, bounds.y + bounds.height - radius, radius, 90f, 90f);
    }


    override fun fill(shape: ShapeRenderer, bounds: Rectangle) {
        roundedRect(shape, bounds, radius)
    }
}