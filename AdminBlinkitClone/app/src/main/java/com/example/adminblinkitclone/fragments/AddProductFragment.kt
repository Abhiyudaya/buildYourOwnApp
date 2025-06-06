package com.example.adminblinkitclone.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.Constants
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.activity.AdminMainActivity
import com.example.adminblinkitclone.adapter.AdapterSelectedImage
import com.example.adminblinkitclone.databinding.FragmentAddProductBinding
import com.example.adminblinkitclone.model.Product
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {
    private val viewModel : AdminViewModel by viewModels()

    private lateinit var binding: FragmentAddProductBinding

    private val imageUris : ArrayList<Uri> = arrayListOf()
    val selectedImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()){ listOfUri ->
        val fiveImages = listOfUri.take(5)
        imageUris.clear()
        imageUris.addAll(fiveImages)

        binding.rvProductImages.adapter = AdapterSelectedImage(imageUris)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAutoCompleteTextViews()

        onImageSelectClicked()

        onAddButtonClicked()

        return binding.root
    }

    private fun onAddButtonClicked() {
        binding.btnAddProduct.setOnClickListener {
            Utils.showDialog(requireContext(),"Uploading images...")
            val productTitle = binding.etProductTitle.text.toString()
            val productQuantity = binding.etProductQuantity.text.toString()
            val productUnit = binding.etProductUnit.text.toString()
            val productPrice = binding.etProductPrice.text.toString()
            val productStock = binding.etProductStock.text.toString()
            val productCategory = binding.etProductCategory.text.toString()
            val productType = binding.etProductType.text.toString()

            if (productTitle.isEmpty() || productQuantity.isEmpty() || productUnit.isEmpty() ||
                productPrice.isEmpty() || productStock.isEmpty() || productCategory.isEmpty() || productType.isEmpty()){
                Utils.hideDialog()
                Utils.showToast(requireContext(),"Empty fields are not allowed")
            }

            else if(imageUris.isEmpty()){
                Utils.apply {
                    hideDialog()
                    showToast(requireContext(),"Please upload some images")
                }
            }

            else{
                val product = Product(
                    productTitle = productTitle,
                    productQuantity = productQuantity.toInt(),
                    productUnit = productUnit,
                    productPrice = productPrice.toInt(),
                    productStock = productStock.toInt(),
                    productCategory = productCategory,
                    productType = productType,
                    itemCount = 0,
                    adminUid = Utils.getCurrentUserId(),
                    productRandomId = Utils.getRandomId()
                )
                saveImage(product)
            }
        }
    }

    private fun saveImage(product: Product) {
        viewModel.saveImageInDB(imageUris)
        lifecycleScope.launch {
            viewModel.isImageUploaded.collect{
                if (it){
                    Utils.apply {
                        hideDialog()
                        showToast(requireContext(),"Image Saved")
                    }
                    getUrls(product)
                }
            }
        }
    }

    private fun getUrls(product: Product) {
        Utils.showDialog(requireContext(),"Publishing Product...")
        lifecycleScope.launch {
            viewModel.downloadedUrls.collect{
                val urls = it
                product.productImageUris = urls
                saveProduct(product)
            }
        }
    }

    private fun saveProduct(product: Product) {
        viewModel.saveProduct(product)
        lifecycleScope.launch {
            viewModel.isProductSaved.collect{
                if (it){
                    Utils.hideDialog()
                    startActivity(Intent(requireContext(),AdminMainActivity::class.java))
                    Utils.showToast(requireContext(),"Your product is live")
                }
            }
        }
    }

    private fun onImageSelectClicked() {
        binding.btnSelectImage.setOnClickListener {
            selectedImage.launch("image/*")
        }
    }

    private fun setAutoCompleteTextViews(){
        val units = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allUnitsOfProducts)
        val category = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allProductCategory)
        val productType = ArrayAdapter(requireContext(),R.layout.show_list,Constants.allProductType)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColorValue = ContextCompat.getColor(requireContext(), R.color.Yellow)
            statusBarColor = statusBarColorValue
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}