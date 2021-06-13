package com.github.iielse.imageviewer.demo.business

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.github.iielse.imageviewer.demo.core.ITEM_CLICKED
import com.github.iielse.imageviewer.demo.core.viewer.TransitionViewsRef
import com.github.iielse.imageviewer.demo.core.viewer.TransitionViewsRef.KEY_MAIN
import com.github.iielse.imageviewer.demo.data.MyData
import com.github.iielse.imageviewer.demo.databinding.MainActivityBinding
import com.github.iielse.imageviewer.demo.utils.App
import com.github.iielse.imageviewer.demo.utils.statusBarHeight
import com.github.iielse.imageviewer.utils.Config

class MainActivity : AppCompatActivity() {
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }

    private val viewModel by lazy { ViewModelProvider(this).get(TestDataViewModel::class.java) }
    private val adapter by lazy { TestDataAdapter() }

    override fun onDestroy() {
        super.onDestroy()
        binding.orientation.setOnClickListener(null)
        binding.fullScreen.setOnClickListener(null)
        binding.loadAllAtOnce.setOnClickListener(null)
        binding.customTransition.setOnClickListener(null)
        binding.recyclerView.adapter = null
        adapter.setListener(null)
        TransitionViewsRef.releaseTransitionViewRef(KEY_MAIN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.context = this.applicationContext // 随便找位置借个全局context用用.
        Config.TRANSITION_OFFSET_Y = statusBarHeight()
        setContentView(binding.root)
        initialViews()
        viewModel.dataList.observe(this, androidx.lifecycle.Observer(adapter::submitList))
    }

    private fun handleAdapterListener(action: String, item: Any?) {
        when (action) {
            ITEM_CLICKED -> (item as? MyData?)?.let(::showViewer)
        }
    }

    private fun showViewer(item: MyData) {
        if (item.id == 10L) {
            startActivity(Intent(this, TestActivity::class.java))
            return
        }

        ViewerHelper.provideImageViewerBuilder(this, item, KEY_MAIN)
                .show()
    }

    private fun initialViews() {
        binding.orientation.setOnClickListener {
            val orientationH = ViewerHelper.orientationH
            ViewerHelper.orientationH = !orientationH
            binding.orientation.text = if (!orientationH) "Horizontal" else "Vertical"
            Config.VIEWER_ORIENTATION = if (!orientationH) ViewPager2.ORIENTATION_HORIZONTAL else ViewPager2.ORIENTATION_VERTICAL
        }
        binding.fullScreen.setOnClickListener {
            val isFullScreen = ViewerHelper.fullScreen
            ViewerHelper.fullScreen = !isFullScreen
            binding.fullScreen.text = if (!isFullScreen) "FullScreen(on)" else "FullScreen(off)"
            Config.TRANSITION_OFFSET_Y = if (!isFullScreen) 0 else statusBarHeight()
        }
        binding.loadAllAtOnce.setOnClickListener {
            val isLoadAllAtOnce = ViewerHelper.loadAllAtOnce
            ViewerHelper.loadAllAtOnce = !isLoadAllAtOnce
            binding.loadAllAtOnce.text = if (!isLoadAllAtOnce) "LoadAllAtOnce(on)" else "LoadAllAtOnce(off)"
        }
        binding.simplePlayVideo.setOnClickListener {
            val isSimplePlayVideo = ViewerHelper.simplePlayVideo
            ViewerHelper.simplePlayVideo = !isSimplePlayVideo
            binding.simplePlayVideo.text = if (!isSimplePlayVideo) "Video(simple)" else "Video(controlView)"
        }
        binding.customTransition.setOnClickListener {
            CustomTransitionHelper.show(it)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
        adapter.setListener(::handleAdapterListener)
    }
}

