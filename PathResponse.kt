package com.dynamicProductCustomer.utils

import com.google.gson.annotations.SerializedName

data class PathResponse(
    @SerializedName("geocoded_waypoints")
    var geocodedWaypoints: List<GeocodedWaypoint?>? = listOf(),
    @SerializedName("routes")
    var routes: List<Route?>? = listOf(),
    @SerializedName("status")
    var status: String? = "" // OK
) {
    data class GeocodedWaypoint(
        @SerializedName("geocoder_status")
        var geocoderStatus: String? = "", // OK
        @SerializedName("place_id")
        var placeId: String? = "", // ChIJMfP0YL3vDzkRpBsalxX8XSM
        @SerializedName("types")
        var types: List<String?>? = listOf()
    )

    data class Route(
        @SerializedName("bounds")
        var bounds: Bounds? = Bounds(),
        @SerializedName("copyrights")
        var copyrights: String? = "", // Map data ©2019 Google
        @SerializedName("legs")
        var legs: List<Leg?>? = listOf(),
        @SerializedName("overview_polyline")
        var overviewPolyline: OverviewPolyline? = OverviewPolyline(),
        @SerializedName("summary")
        var summary: String? = "", // Mohali Bypass
        @SerializedName("warnings")
        var warnings: List<Any?>? = listOf(),
        @SerializedName("waypoint_order")
        var waypointOrder: List<Any?>? = listOf()
    ) {
        data class Bounds(
            @SerializedName("northeast")
            var northeast: Northeast? = Northeast(),
            @SerializedName("southwest")
            var southwest: Southwest? = Southwest()
        ) {
            data class Southwest(
                @SerializedName("lat")
                var lat: Double? = 0.0, // 30.70776889999999
                @SerializedName("lng")
                var lng: Double? = 0.0 // 76.7090077
            )

            data class Northeast(
                @SerializedName("lat")
                var lat: Double? = 0.0, // 30.7128458
                @SerializedName("lng")
                var lng: Double? = 0.0 // 76.7197955
            )
        }

        data class OverviewPolyline(
            @SerializedName("points")
            var points: String? = "" // irmzDifesM|JaI~E_EwCyFIQNKbCoBbCoBbF_Et@m@HgAaDwFsCiF~BiBt@o@kAgB{AlAiBgD
        )

        data class Leg(
            @SerializedName("distance")
            var distance: Distance? = Distance(),
            @SerializedName("duration")
            var duration: Duration? = Duration(),
            @SerializedName("end_address")
            var endAddress: String? = "", // 1622, Phase 3B-2, Sector 60, Sahibzada Ajit Singh Nagar, Punjab 160059, India
            @SerializedName("end_location")
            var endLocation: EndLocation? = EndLocation(),
            @SerializedName("start_address")
            var startAddress: String? = "", // Unnamed Road, Phase 8, Industrial Area, Sector 73, Sahibzada Ajit Singh Nagar, Punjab 140308, India
            @SerializedName("start_location")
            var startLocation: StartLocation? = StartLocation(),
            @SerializedName("steps")
            var steps: List<Step?>? = listOf(),
            @SerializedName("traffic_speed_entry")
            var trafficSpeedEntry: List<Any?>? = listOf(),
            @SerializedName("via_waypoint")
            var viaWaypoint: List<viaWay?>? = listOf()
        ) {
            data class viaWay(
                var location: LocationIs? = null,
                var step_index: Int? = null,
                var step_interpolation: Double? = null
            )

            data class LocationIs(
                var lat: Double? = null,
                var lng: Double? = null
            )

            data class Duration(
                @SerializedName("text")
                var text: String? = "", // 5 mins
                @SerializedName("value")
                var value: Double? = 0.0 // 328
            )

            data class StartLocation(
                @SerializedName("lat")
                var lat: Double? = 0.0, // 30.7128458
                @SerializedName("lng")
                var lng: Double? = 0.0 // 76.7090077
            )

            data class Step(
                @SerializedName("distance")
                var distance: Distance? = Distance(),
                @SerializedName("duration")
                var duration: Duration? = Duration(),
                @SerializedName("end_location")
                var endLocation: EndLocation? = EndLocation(),
                @SerializedName("html_instructions")
                var htmlInstructions: String? = "", // Turn <b>right</b> at Skill service centre<div style="font-size:0.9em">Destination will be on the right</div>
                @SerializedName("maneuver")
                var maneuver: String? = "", // turn-right
                @SerializedName("polyline")
                var polyline: Polyline? = Polyline(),
                @SerializedName("start_location")
                var startLocation: StartLocation? = StartLocation(),
                @SerializedName("travel_mode")
                var travelMode: String? = "" // DRIVING
            ) {
                data class Duration(
                    @SerializedName("text")
                    var text: String? = "", // 1 min
                    @SerializedName("value")
                    var value: Int? = 0 // 38
                )

                data class StartLocation(
                    @SerializedName("lat")
                    var lat: Double? = 0.0, // 30.7092494
                    @SerializedName("lng")
                    var lng: Double? = 0.0 // 76.7189616
                )

                data class Distance(
                    @SerializedName("text")
                    var text: String? = "", // 0.1 km
                    @SerializedName("value")
                    var value: Int? = 0 // 99
                )

                data class Polyline(
                    @SerializedName("points")
                    var points: String? = "" // y{lzDodgsMiBgD
                )

                data class EndLocation(
                    @SerializedName("lat")
                    var lat: Double? = 0.0, // 30.70978139999999
                    @SerializedName("lng")
                    var lng: Double? = 0.0 // 76.7197955
                )
            }

            data class EndLocation(
                @SerializedName("lat")
                var lat: Double? = 0.0, // 30.70978139999999
                @SerializedName("lng")
                var lng: Double? = 0.0 // 76.7197955
            )

            data class Distance(
                @SerializedName("text")
                var text: String? = "", // 1.6 km
                @SerializedName("value")
                var value: Int? = 0 // 1634
            )
        }
    }
}
