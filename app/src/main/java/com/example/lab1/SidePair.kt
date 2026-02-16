package com.example.lab1

import android.os.Parcel
import android.os.Parcelable

data class SidePair(val width: Double, val length: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(width)
        parcel.writeDouble(length)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SidePair> {
        override fun createFromParcel(parcel: Parcel): SidePair = SidePair(parcel)
        override fun newArray(size: Int): Array<SidePair?> = arrayOfNulls(size)
    }
}