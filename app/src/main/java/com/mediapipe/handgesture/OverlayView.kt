/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mediapipe.handgesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: GestureRecognizerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var borderPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.hg_color_secondary)
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 8f

        pointPaint.color = ContextCompat.getColor(context!!, R.color.hg_color_primary)
        pointPaint.style = Paint.Style.FILL
        borderPaint.color = ContextCompat.getColor(context!!, R.color.hg_color_secondary)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 8f
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val radius = 16f
        results?.let { gestureRecognizerResult ->
            for (landmark in gestureRecognizerResult.landmarks()) {
                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        gestureRecognizerResult.landmarks()[0][it!!.start()].x() * imageWidth * scaleFactor,
                        gestureRecognizerResult.landmarks()[0][it.start()].y() * imageHeight * scaleFactor,
                        gestureRecognizerResult.landmarks()[0][it.end()].x() * imageWidth * scaleFactor,
                        gestureRecognizerResult.landmarks()[0][it.end()].y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }

                for (normalizedLandmark in landmark) {
                    canvas.drawCircle(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        radius,
                        pointPaint
                    )
                    canvas.drawCircle(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        radius,
                        borderPaint
                    )
                }
            }
        }
    }

    fun setResults(
        gestureRecognizerResult: GestureRecognizerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ) {
        results = gestureRecognizerResult

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }

            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }
}
