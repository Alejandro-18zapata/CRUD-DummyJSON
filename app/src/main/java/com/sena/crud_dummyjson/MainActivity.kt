package com.sena.crud_dummyjson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sena.crud_dummyjson.ui.screen.ProductListScreen
import com.sena.crud_dummyjson.ui.theme.CRUDDummyJSONTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CRUDDummyJSONTheme {
                ProductListScreen()
            }
        }
    }
}
