package com.example.camera

import android.annotation.SuppressLint

data class Product(

val id: Int = 0,
var img: Int = 0,
val price: Double = 0.0,
val rating: Double = 0.0,
val shortdesc: String = "",
val title: String = ""
)



    /*fun Product(id: Int, title: String?, shortdesc: String?, rating: Double, price: Double, image: Int) {
        this.id = id
        this.title = title
        this.shortdesc = shortdesc
        this.rating = rating
        this.price = price
        this.image = image
    }*/

    /* fun getId(): Int {
        return id
    }

    fun getTitle(): String? {
        return title
    }

    fun getShortdesc(): String? {
        return shortdesc
    }

    fun getRating(): Double {
        return rating
    }

    fun getPrice(): Double {
        return price
    }

    fun getImage(): Int {
        return image
    }

}*/