package com.latifur.mapbox

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    var mapView: MapView? = null
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        val btnMyLocation: FloatingActionButton = findViewById(R.id.btnMyLocation)
        val btnDrawLine: ExtendedFloatingActionButton = findViewById(R.id.btnDrawLine)

        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS){
            animateCameraDelayed()
        }

        btnMyLocation.setOnClickListener {
            val intent = Intent(applicationContext, MyLocation::class.java)
            startActivity(intent)
            locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
            locationPermissionHelper.checkPermissions {
                mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS) {
                    // Disable scroll gesture, since we are updating the camera position based on the indicator location.
                    mapView?.gestures?.scrollEnabled = false
                    mapView?.gestures?.addOnMapClickListener { point ->
                        mapView?.location
                            ?.isLocatedAt(point) { isPuckLocatedAtPoint ->
                                if (isPuckLocatedAtPoint) {
                                    Toast.makeText(this,
                                        "Clicked on location puck",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        true
                    }
                    mapView?.gestures?.addOnMapLongClickListener { point ->
                        mapView?.location
                            ?.isLocatedAt(point) { isPuckLocatedAtPoint ->
                                if (isPuckLocatedAtPoint) {
                                    Toast.makeText(this,
                                        "Long-clicked on location puck",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        true
                    }
                }
            }
        }
        btnDrawLine.setOnClickListener {
            val intent = Intent(applicationContext, DrawGeoJsonLineActivity::class.java)
            startActivity(intent)
        }

    }

    //Custom animation
    private fun animateCameraDelayed() {
        mapView?.camera?.apply {

            val bearing = createBearingAnimator(CameraAnimatorOptions.cameraAnimatorOptions(-45.0)) {
                duration = 4000
                interpolator = AccelerateDecelerateInterpolator()
            }
            val zoom = createZoomAnimator(
                CameraAnimatorOptions.cameraAnimatorOptions(12.0) {
                    startValue(30.0)
                }
            ) {
                duration = 4000
                interpolator = AccelerateDecelerateInterpolator()
            }
            val pitch = createPitchAnimator(
                CameraAnimatorOptions.cameraAnimatorOptions(44.0) {
                    startValue(0.0)
                }
            ) {
                duration = 4000
                interpolator = AccelerateDecelerateInterpolator()
            }
            playAnimatorsSequentially(zoom, pitch, bearing)
        }
    }

}