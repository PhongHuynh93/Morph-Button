package com.umbrella.morphbutton

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.umbrella.morphbutton.databinding.ActivityMainBinding
import com.umbrella.morphbutton.util.dp
import com.umbrella.morphbutton.util.getColorX
import com.umbrella.morphbutton.util.getDrawableX
import com.umbrella.morphbutton.util.sp
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenStarted {
            binding.btn1.apply {
                iconDrawable.setTint(getColorX(R.color.green))
                while (true) {
                    delay(1000)
                    setUIState(MorphButton.UIState.Loading)
                    delay(1500)
                    setUIState(MorphButton.UIState.Button)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            binding.btn2.apply {
                fromBgColor = getColorX(R.color.purple_700)
                toBgColor = getColorX(R.color.green)
                fromTextColor = getColorX(R.color.white)
                toTextColor = getColorX(R.color.black)
                text = "Login"
                textSize = 24 * sp()
                setPadding((32 * dp()).toInt(), (16 * dp()).toInt(), (32 * dp()).toInt(), (16 * dp()).toInt())
                iconDrawable = getDrawableX(R.drawable.ic_sync).apply {
                    setTint(getColorX(R.color.white))
                }
                while (true) {
                    delay(1000)
                    setUIState(MorphButton.UIState.Loading)
                    delay(1500)
                    setUIState(MorphButton.UIState.Button)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            binding.btn3.apply {
                fromBgColor = getColorX(R.color.light_yellow)
                toBgColor = getColorX(R.color.red)
                fromTextColor = getColorX(R.color.red)
                toTextColor = getColorX(R.color.white)
                text = "Pokemon"
                textSize = 48 * sp()
                setPadding((40 * dp()).toInt(), (24 * dp()).toInt(), (40 * dp()).toInt(), (24 * dp()).toInt())
                iconDrawable = getDrawableX(R.drawable.ic_baseline_catching_pokemon)
                while (true) {
                    delay(1000)
                    setUIState(MorphButton.UIState.Loading)
                    delay(1500)
                    setUIState(MorphButton.UIState.Button)
                }
            }
        }
    }
}