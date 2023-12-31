package com.example.camera

import android.annotation.SuppressLint

class Product {

    private var id: Int = 0
    private var title: String? = ""
    private  var shortdesc: String? = ""
    private var rating = 0.0
    private  var price:kotlin.Double = 0.0
    private var image:String  = ""

    fun Product(id: Int, title: String?, shortdesc: String?, rating: Double, price: Double, image: String) {
        this.id = id
        this.title = title
         this.shortdesc = shortdesc
        this.rating = rating
        this.price = price
        this.image = image
    }

    fun getId(): Int {
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

    fun getImage(): String {
        return image
    }

}