



package com.example.camera

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import java.lang.String
import kotlin.Int



 class ProductAdapter( val mCtx: Context, productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder?>() {
    private val productList: List<Product>

    init {
        this.productList = productList
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(mCtx)
        val view = inflater.inflate(R.layout.list_layout, null)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull productViewHolder: ProductViewHolder, i: Int) {
        val product: Product = productList[i]
        productViewHolder.textViewTitle.setText(product.getTitle())
        productViewHolder.textViewDesc.setText(product.getShortdesc())
        productViewHolder.textViewRating.setText(String.valueOf(product.getRating()))
        productViewHolder.textViewPrice.setText(String.valueOf(product.getPrice()))
        //productViewHolder.imageView.setImageDrawable(mCtx.resources.getDrawable(product.getImage()))
    }



    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ProductViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textViewTitle: TextView
        var textViewDesc: TextView
        var textViewRating: TextView
        var textViewPrice: TextView

        init {
            imageView = itemView.findViewById(R.id.imageView)
            textViewTitle = itemView.findViewById(R.id.textViewTitle)
            textViewDesc = itemView.findViewById(R.id.textViewShortDesc)
            textViewRating = itemView.findViewById(R.id.textViewRating)
            textViewPrice = itemView.findViewById(R.id.textViewPrice)
        }
    }
}