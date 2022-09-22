package com.example.maskinfokotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var storeAdapter: StoreAdapter

//    private val requestPermission =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
//            if (map[Manifest.permission.ACCESS_FINE_LOCATION]!!
//                && map[Manifest.permission.ACCESS_COARSE_LOCATION]!!
//            ) {
//                viewModel.fetchStoreInfo()
//                //performAction()
//            } else {
//                toast("권한이 거부되었습니다.")
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            adapter = storeAdapter
        }

        viewModel.apply {
            itemLiveData.observe(this@MainActivity, Observer {
                storeAdapter.updateItems(it)
            })

            loadingLiveData.observe(this@MainActivity, Observer { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            })
        }

        performAction()
    }

    private fun performAction() {
        // 권한 설정
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 0)

            return
        }

        // 위치 정보 설정
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            alert("위치 서비스 설정이 꺼져 있어 현재 위치를 확인할 수 없습니다. 설정을 변경하시겠습니까?", "위치 정보 설정") {
                yesButton {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                noButton {}
            }.show()
            return
        }

        viewModel.fetchStoreInfo()
    }

    // 권한 설정
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty()) {
                    var isAllGranted = true
                    for (grant in grantResults) {
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        }
                    }

                    if (isAllGranted) {
                        performAction()
                    } else {
                        // Permission Denied
                        // If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]
                        alert("권한을 거부할 경우 본 서비스를 이용하실 수 없습니다.\n\n[설정] > [권한]에서 권한을 켜주세요.") {
                            okButton {
                                // 애플리케이션 정보
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                            cancelButton {
                                toast("권한이 거부되었습니다.")
                            }
                        }.show()
                    }
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                performAction()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}