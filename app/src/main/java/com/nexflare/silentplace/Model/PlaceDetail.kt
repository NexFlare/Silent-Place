package com.nexflare.silentplace.Model

import android.support.annotation.Keep
import com.google.android.gms.maps.model.LatLng

/**
 * Created by 15103068 on 22-07-2017.
 */
@Keep
data class PlaceDetail(var name:String,var latLng: LatLng){}