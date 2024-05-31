//package io.github.alexzhirkevich.compottie.internal.shapes.util
//
//import androidx.compose.ui.geometry.Offset
//import kotlin.math.sqrt
//
///*
// * Copyright 2023 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//
//
///**
// * The magnitude of the Point, which is the distance of this point from (0, 0).
// *
// * If you need this value to compare it to another [Point]'s distance,
// * consider using [getDistanceSquared] instead, since it is cheaper to compute.
// */
//internal fun Offset.getDistance() = sqrt(x * x + y * y)
//
///**
// * The square of the magnitude (which is the distance of this point from (0, 0)) of the Point.
// *
// * This is cheaper than computing the [getDistance] itself.
// */
//internal fun Offset.getDistanceSquared() = x * x + y * y
//
//internal fun Offset.dotProduct(other: Offset) = x * other.x + y * other.y
//
//internal fun Offset.dotProduct(otherX: Float, otherY: Float) = x * otherX + y * otherY
//
///**
// * Compute the Z coordinate of the cross product of two vectors, to check if the second vector is
// * going clockwise ( > 0 ) or counterclockwise (< 0) compared with the first one.
// * It could also be 0, if the vectors are co-linear.
// */
//internal fun Offset.clockwise(other: Offset) = x * other.y - y * other.x > 0
//
///**
// * Returns unit vector representing the direction to this point from (0, 0)
// */
//internal fun Offset.getDirection() = run {
//    val d = this.getDistance()
//    require(d > 0f) { "Can't get the direction of a 0-length vector" }
//    this / d
//}
//
///**
// * Linearly interpolate between two Points.
// *
// * The [fraction] argument represents position on the timeline, with 0.0 meaning
// * that the interpolation has not started, returning [start] (or something
// * equivalent to [start]), 1.0 meaning that the interpolation has finished,
// * returning [stop] (or something equivalent to [stop]), and values in between
// * meaning that the interpolation is at the relevant point on the timeline
// * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
// * 1.0, so negative values and values greater than 1.0 are valid (and can
// * easily be generated by curves).
// *
// * Values for [fraction] are usually obtained from an [Animation<Float>], such as
// * an `AnimationController`.
// */
//internal fun interpolate(start: Offset, stop: Offset, fraction: Float): Offset {
//    return Offset(
//        interpolate(start.x, stop.x, fraction),
//        interpolate(start.y, stop.y, fraction)
//    )
//}
//
//internal fun Offset.transformed(f: PointTransformer): Offset {
//    val result = f.transform(x, y)
//    return Offset(result.first, result.second)
//}