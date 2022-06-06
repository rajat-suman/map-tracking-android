# map-tracking-android

Use Following Functions

--------------------------- TO SHOW FRESH PATH -------------------------------


fun Context.showPath(

    srcLat: LatLng,
    
    desLat: LatLng,
    
    mMap: GoogleMap?,
    
    @DrawableRes source: Int,
    
    @DrawableRes destination: Int,
    
    @DrawableRes via: Int,
    
    wayPoints: ArrayList<LatLng> = ArrayList(),
    
    valueIs: (PathModel) -> Unit = { _ -> }
    
)

srcLat -> from Location 
    
desLat -> To Location
    
@DrawableRes source: Int -> Marker Drawable for from location
    
@DrawableRes destination: Int -> Marker Drawable for to location
    
@DrawableRes via: Int -> Marker Drawable for wayopoint location
    
wayPoints: ArrayList<LatLng> = ArrayList() -> List of waypoints
    

----------------------------- TO ANIMATE DRIVER/VEHICLE -------------------------------
  
fun Context.animateDriver(
    
    driverLatitude: Double?,
    
    driverLongitude: Double?,
    
    bearing: Double?,
    
    googleMap: GoogleMap?,
    
    @DrawableRes source: Int,
    
    @DrawableRes destination: Int,
    
    @DrawableRes via: Int,
    
    eta: (String) -> Unit = {}
)
  
driverLatitude: Double? -> Driver's Current Latitude
    
driverLongitude: Double? -> Driver's Current Longitude
    
bearing: Double? -> Driver's Bearing(angle from north)
    
googleMap: GoogleMap?, -> Google Map's Instance
    
@DrawableRes source: Int -> Marker Drawable for from location
    
@DrawableRes destination: Int -> Marker Drawable for to location
    
@DrawableRes via: Int -> Marker Drawable for wayopoint location
    
  

  
----------------------- TO CLEAR MAP ------------------------------------------
    
  use this method instead of map.clear()
    
  
fun GoogleMap.clearMap()
  
 
---------------------- TO NAVIGATE USING GOOGLE MAP APP -----------------------
  
  
private fun Context.navigateToMapWithLatLong(
    
    sourceLatitude: Double,
    
    sourceLongitude: Double,
    
    destinationLatitude: Double,
    
    destinationLongitude: Double
    
)
  
------------------------ TO NAVIGATE USING WAZE MAP ----------------------------

fun Context.navigateToWazeWithLatLong(
    
    sourceLatitude: Double,
    
    sourceLongitude: Double,
    
    destinationLatitude: Double,
    
    destinationLongitude: Double
    
) 



