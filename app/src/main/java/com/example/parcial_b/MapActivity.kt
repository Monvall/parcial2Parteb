package com.example.parcial_b
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.fragment.app.FragmentActivity

class MapActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Configurar el botón de regreso
        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            // Termina la actividad y regresa a la MainActivity
            finish()
        }

        // Inicializar el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Añadir marcador en una ubicación predeterminada
        val myLocation = LatLng(-34.0, 151.0) // Cambia la ubicación según lo que necesites
        mMap.addMarker(MarkerOptions().position(myLocation).title("Marcador en mi ubicación"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
    }
}
