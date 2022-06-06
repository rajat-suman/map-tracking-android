package com.dynamicProductCustomer.utils

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.beust.klaxon.*
import com.dynamicProductCustomer.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.math.abs
import kotlin.math.sign


var polyLine: List<LatLng>? = null
private var srcMarker: Marker? = null
private var driverMarker: Marker? = null
private var destMarker: Marker? = null
private var polyline: Polyline? = null

/**Show path on Map*/
/**Show path on Map*/
fun Context.showPath(
    srcLat: LatLng,
    desLat: LatLng,
    mMap: GoogleMap?,
    @DrawableRes source: Int,
    @DrawableRes destination: Int,
    @DrawableRes via: Int,
    wayPoints: ArrayList<LatLng> = ArrayList(),
    valueIs: (PathModel) -> Unit = { _ -> }
) {
    try {
        val pathModel = PathModel()
        val latLongB = LatLngBounds.Builder()

        val options = PolylineOptions()
        options.color(ContextCompat.getColor(this, R.color.colorBlack))

        val url = getDirectionsUrl(srcLat, desLat, wayPoints)
        CoroutineScope(Dispatchers.IO).launch {
            val result = URL(url).readText()
            val pathResponse: PathResponse = Gson().fromJson(result, PathResponse::class.java)
            CoroutineScope(Dispatchers.Main).launch {
                mMap?.clear()
                val parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                val routes = json.array<JsonObject>("routes")
                if (routes != null && routes.size > 0) {
                    pathModel.durationText = pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.text ?: ""
                    pathModel.durationMin = (pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.value ?: 0.0) / 60
                    pathModel.srcDesDistance = (pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.value?.toDouble() ?: 0.0) / 1000
                    pathModel.srcDesDistanceInKm = pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.text ?: ""
                    val points = routes["legs"]["steps"][0] as JsonArray<JsonObject>
                    val polyPoints = points.flatMap { decodePoly(it.obj("polyline")?.string("points") ?: "") }
                    latLongB.include(srcLat)
                    for (point in polyPoints) {
                        options.add(point)
                        latLongB.include(point)
                    }
                    polyLine = polyPoints
                    val bounds = latLongB.build()
                    polyline = mMap?.addPolyline(options)
                    if (mMap != null && srcLat.latitude != 0.0 && srcLat.longitude != 0.0) {
                        mMap.addPolyline(options)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                    if (mMap != null) {

                        for (data in pathResponse.routes?.get(0)?.legs?.get(0)?.viaWaypoint
                            ?: ArrayList()) {
                            data?.location?.let { loc ->
                                mMap.addMarker(
                                    MarkerOptions().position(
                                        LatLng(loc.lat ?: 0.0, loc.lng ?: 0.0)
                                    ).icon(
                                        vectorToBitmap(via)
                                    ).anchor(0.5f, 1f)
                                )
                            }

                        }

                        pathModel.srcMarker = mMap.addMarker(
                            MarkerOptions().position(
                                srcLat
                            ).apply {
                                icon(vectorToBitmap(source))
                                anchor(0.5f, 1f)
                            }
                        )

                        pathModel.desMarker = mMap.addMarker(
                            MarkerOptions().apply {
                                position(desLat)
                                icon(vectorToBitmap(destination))
                                anchor(0.5f, 1f)
                            }
                        )

                        srcMarker = pathModel.srcMarker
                        destMarker = pathModel.desMarker
                    }

                    valueIs(pathModel)
                }

            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


/**Find Eta between two lat longs*/
fun Context.findEta(srcLat: LatLng, desLat: LatLng, valueIs: (PathModel) -> Unit) {

    try {
        val pathModel = PathModel()

        val url = getDirectionsUrl(srcLat, desLat)
        CoroutineScope(Dispatchers.IO).launch {
            val result = URL(url).readText()

            val pathResponse: PathResponse = Gson().fromJson(result, PathResponse::class.java)
            CoroutineScope(Dispatchers.Main).launch {
                if (pathResponse.routes != null && !pathResponse.routes.isNullOrEmpty()) {
                    pathModel.durationText =
                        pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.text ?: ""
                    pathModel.durationMin =
                        (pathResponse.routes?.get(0)?.legs?.get(0)?.duration?.value ?: 0.0) / 60
                    pathModel.srcDesDistance =
                        (pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.value?.toDouble()
                            ?: 0.0) / 1000
                    pathModel.srcDesDistanceInKm =
                        pathResponse.routes?.get(0)?.legs?.get(0)?.distance?.text ?: ""
                    valueIs(pathModel)
                }
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**Get url*/
private fun Context.getDirectionsUrl(
    src: LatLng, des: LatLng, markerPoints: ArrayList<LatLng> = ArrayList()
): String {
    var url = "https://maps.googleapis.com/maps/api/directions/"
    val params = "&mode=driving&key=" + getString(R.string.GOOGLE_KEY)
    val output = "json"
    var parameters = ""

    if (markerPoints.isNotEmpty()) {
        var waypoints = ""
        for (i in 0 until markerPoints.size)//loop starts from 2 because 0 and 1 are already printed
        {
            waypoints =
                waypoints + "via:" + markerPoints[i].latitude + "," + markerPoints[i].longitude + "|"
        }
        waypoints = "&waypoints=optimize:true|$waypoints"
        parameters =
            "origin=${src.latitude.toString() + "," + src.longitude}$waypoints" + "&destination=${des.latitude.toString() + "," + des.longitude}"
        url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&$params"

    } else {
        parameters =
            "origin=${src.latitude.toString() + "," + src.longitude}" + "&destination=${des.latitude.toString() + "," + des.longitude}&$params"
        url += "$output?$parameters"

    }
    Log.e("PathUtil", "getDirectionsUrl: $url")
    return url
}

/**Decode poly line*/
private fun decodePoly(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(
            lat.toDouble() / 1E5,
            lng.toDouble() / 1E5
        )
        poly.add(p)
    }
    return poly
}

/**VECTOR IMAGE TO BITMAP*/
fun Context.vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor? {
    ResourcesCompat.getDrawable(resources, id, null)?.let { vectorDrawable ->
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    } ?: return null
}

/**Animate Driver*/

var oldDriverLat:Double?=null
var oldDriverLng:Double?=null
fun Context.animateDriver(
    driverLatitude: Double?,
    driverLongitude: Double?,
    bearing: Double?,
    googleMap: GoogleMap?,
    @DrawableRes source: Int,
    @DrawableRes destination: Int,
    @DrawableRes via: Int,
    eta: (String) -> Unit = {}
) {
    if(oldDriverLat== driverLatitude && oldDriverLng == driverLongitude){
        return
    }
    oldDriverLat= driverLatitude
    oldDriverLng= driverLongitude

    if (polyLine != null) {
        val exceededTolerance = !PolyUtil.isLocationOnPath(
            LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0),
            polyLine,
            false,
            50.0
        )
        if (exceededTolerance) {
            showPath(
                srcLat = LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0),
                desLat = destMarker?.position ?: LatLng(0.0, 0.0),
                mMap = googleMap,
                source = source,
                destination = destination,
                via = via
            ) {}
        } else
            animateCar(
                LatLng(driverLatitude ?: 0.0, driverLongitude ?: 0.0),
                (bearing ?: 0.0).toFloat(),
                googleMap
            ) {
                eta(it)
            }
    }

}

private interface LatLngInterpolator {
    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
    class LinearFixed : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            var lngDelta = b.longitude - a.longitude
            if (abs(lngDelta) > 180) {
                lngDelta -= sign(lngDelta) * 360
            }
            val lng = lngDelta * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }
}

private fun Context.animateCar(endPosition: LatLng, bearing: Float, googleMap: GoogleMap?, eta: (String) -> Unit) {

    try {
        srcMarker?.let {
            val startPosition = it.position
            /*mMap!!.moveCamera(
               CameraUpdateFactory.newLatLngZoom(destination, 16f)
           )*/
//            destMarker?.let { dest ->
//                findEta(endPosition, dest.position) { pathModel ->
//                    eta(pathModel.durationText ?: "")
////                    destMarker?.remove()
//                    driverMarker = googleMap?.addMarker(
//                        MarkerOptions().apply {
//                            position(dest.position)
//                            icon(vectorToBitmap(R.drawable.ic_driver_riding))
//                            anchor(0.5f, 1f)
//                        }
//                    )
//                }
//
//            }


            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator.duration = 2000 // duration 5 seconds
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    driverMarker?.position = newPosition
                    driverMarker?.rotation = bearing

                    try {
                        val nearestPoint = findNearestPoint(newPosition, polyLine ?: emptyList())
                        //update polyline according to points
                        if ((polyline != null) && (polyLine != null) && (polyLine?.size ?: 0) > 1 && (polyline?.points?.size
                                ?: 0) > 1 && (polyLine?.indexOf(
                                nearestPoint
                            ) ?: 0) >= 0 && (polyLine?.indexOf(nearestPoint) ?: 0) <= (polyLine?.size ?: 0)
                        ) {
                            polyline?.points =
                                polyLine?.subList(
                                    polyLine?.indexOf(nearestPoint) ?: 0,
                                    polyLine?.size ?: 0
                                ) ?: emptyList()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } catch (ex: Exception) {
                }
            }
            valueAnimator.addListener(object : AnimatorListenerAdapter() {
            })
            valueAnimator.start()
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun findNearestPoint(test: LatLng, target: List<LatLng>): LatLng {
    var distance = -1.0
    var minimumDistancePoint = test
    for (i in target.indices) {
        val point = target[i]
        var segmentPoint = i + 1
        if (segmentPoint >= target.size) {
            segmentPoint = 0
        }
        val currentDistance = PolyUtil.distanceToLine(test, point, target[segmentPoint])
        if (distance == -1.0 || currentDistance < distance) {
            distance = currentDistance
            minimumDistancePoint = findNearestPoint(test, point, target[segmentPoint])
        }
    }
    return minimumDistancePoint
}

private fun Context.bitmapDescriptorFromVector(): BitmapDescriptor? {
    return ContextCompat.getDrawable(this, R.drawable.iv_taxi_bike)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

fun findNearestPoint(p: LatLng, start: LatLng, end: LatLng): LatLng {
    if (start == end) {
        return start
    }
    val s0lat = Math.toRadians(p.latitude)
    val s0lng = Math.toRadians(p.longitude)
    val s1lat = Math.toRadians(start.latitude)
    val s1lng = Math.toRadians(start.longitude)
    val s2lat = Math.toRadians(end.latitude)
    val s2lng = Math.toRadians(end.longitude)
    val s2s1lat = s2lat - s1lat
    val s2s1lng = s2lng - s1lng
    val u =
        ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng) / (s2s1lat * s2s1lat + s2s1lng * s2s1lng)
    if (u <= 0) {
        return start
    }
    return if (u >= 1) {
        end
    } else LatLng(
        start.latitude + u * (end.latitude - start.latitude),
        start.longitude + u * (end.longitude - start.longitude)
    )
}

/**Navigate to Map*/

/*
fun Context.navigateToMap(
    source: LatLng,
    destination: LatLng
) {
    try {
        when {
            AppController.globalSettings?.navigateOn?.contains("GOOGLE") == true &&
                    AppController.globalSettings?.navigateOn?.contains("WAZE") == true -> {
                showMapDialogWithLatLong(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude
                )
            }
            AppController.globalSettings?.navigateOn?.contains("GOOGLE") == true -> {
                navigateToMapWithLatLong(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude
                )
            }
            AppController.globalSettings?.navigateOn?.contains("WAZE") == true -> {
                navigateToWazeWithLatLong(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude
                )
            }
        }
        */
/* val intent = Intent(
             Intent.ACTION_VIEW,
             Uri.parse("http://maps.google.com/maps?saddr=${source.latitude},${source.longitude}&daddr=${destination.latitude},${destination.longitude}")
         )
         startActivity(intent)*//*

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
*/

/*fun Context.showMapDialogWithLatLong(
    sourceLatitude: Double,
    sourceLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double
) {
    try {
        (this as MapActivity).supportFragmentManager.let {

            CommonBottomSheet(R.layout.map_type) { view, dialog ->
                val layoutView = MapTypeBinding.bind(view)

                layoutView.btnGoogleMap.setOnClickListener {
                    navigateToMapWithLatLong(
                        sourceLatitude,
                        sourceLongitude,
                        destinationLatitude,
                        destinationLongitude
                    )
                    dialog.dismiss()
                }

                layoutView.btnWazeMap.setOnClickListener {
                    navigateToWazeWithLatLong(
                        sourceLatitude,
                        sourceLongitude,
                        destinationLatitude,
                        destinationLongitude
                    )
                    dialog.dismiss()
                }

            }.show(it, "")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}*/

private fun Context.navigateToMapWithLatLong(
    sourceLatitude: Double,
    sourceLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double
) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?saddr=$sourceLatitude,$sourceLongitude&daddr=$destinationLatitude,$destinationLongitude")
        )
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.navigateToWazeWithLatLong(
    sourceLatitude: Double,
    sourceLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double
) {
    try {
        val mapRequest =
            "https://waze.com/ul?q=$destinationLatitude,$destinationLongitude&navigate=yes&zoom=17"

        val gmmIntentUri = Uri.parse(mapRequest)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.waze")
        startActivity(mapIntent)
    } catch (ex: ActivityNotFoundException) {
        // If Waze is not installed, open it in Google Play:
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"))
        startActivity(intent)
    }


}

data class PathModel(
    var srcMarker: Marker? = null,
    var desMarker: Marker? = null,
    var durationText: String? = null,
    var durationMin: Double? = null,
    var srcDesDistance: Double? = null,
    var srcDesDistanceInKm: String? = null,
)

/*private fun getMarkerBitmapFromView(eta: String): BitmapDescriptor? {
    val customMarkerView: View =
        (HomeActivity.context.get()
            ?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.view_custom_marker,
            null
        )

    customMarkerView.findViewById<TextView>(R.id.tvTime).text =
        Html.fromHtml(eta.replace(" ", "<br>"))
    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    customMarkerView.layout(
        0,
        0,
        customMarkerView.measuredWidth,
        customMarkerView.measuredHeight
    )
    customMarkerView.buildDrawingCache()
    val returnedBitmap = Bitmap.createBitmap(
        customMarkerView.measuredWidth, customMarkerView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(returnedBitmap)
    canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
    val drawable: Drawable = customMarkerView.background
    drawable.draw(canvas)
    customMarkerView.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(returnedBitmap)
}*/

fun GoogleMap.clearMap() {
    polyLine = null
    polyline = null
    srcMarker?.remove()
    destMarker?.remove()
    srcMarker = null
    destMarker = null
    oldDriverLat= null
    oldDriverLng= null
    this.clear()
}

