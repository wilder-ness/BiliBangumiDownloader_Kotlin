package com.sgpublic.bilidownload.fragment

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.activity.Season
import com.sgpublic.bilidownload.base.BaseFragment
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.data.FollowData
import com.sgpublic.bilidownload.databinding.FragmentFollowsBinding
import com.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.module.FollowsModule
import com.sgpublic.bilidownload.widget.ObservableScrollView
import java.util.*

class Follows(private val context: AppCompatActivity, @StringRes private val title: Int, private val status: Int) : BaseFragment<FragmentFollowsBinding>(context) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        binding.followsBase.visibility = View.INVISIBLE
        startOnLoadingState(binding.followsLoadState)
        getFollowData()
    }

    override fun onViewSetup() {
        binding.followsRefresh.setOnRefreshListener {
            listRowSize = 0
            Timer().schedule(object : TimerTask(){
                override fun run() {
                    getFollowData()
                }
            }, 500)
        }
    }

    override fun getTitle(): CharSequence = context.getString(title)

    private fun getFollowData(pageIndex: Int = 1) {
        if (pageIndex == 1){
            listRowSize = 0
        }
        val accessKey = ConfigManager.getString("access_key")
        val mid = ConfigManager.getLong("mid")
        val helper = FollowsModule(context, accessKey)
        helper.getFollows(mid, pageIndex, status, object : FollowsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    stopOnLoadingState()
                    binding.followsLoadState.setImageResource(R.drawable.pic_load_failed)
                    binding.followsRefresh.isRefreshing = false
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(followData: ArrayList<FollowData>, hasNext: Int) {
                runOnUiThread {
                    stopOnLoadingState()
                    try {
                        if (followData.isEmpty()) {
                            binding.followsLoadState.setImageResource(R.drawable.pic_null)
                            binding.followsRefresh.isRefreshing = false
                        } else {
                            binding.followsLoadState.visibility = View.INVISIBLE
                            binding.followsBase.visibility = View.VISIBLE
                            if (pageIndex == 1) {
                                binding.followsGrid.removeAllViews()
                                if (binding.followsRefresh.isRefreshing){
                                    binding.followsRefresh.isRefreshing = false
                                }
                            }
                            setGrid(followData, hasNext)
                        }
                    } catch (ignore: NullPointerException) { }
                }
            }
        })
    }

    private var listRowSize = 0
    private var scrollToEnd = false
    private fun setGrid(data_array: ArrayList<FollowData>, hasNext: Int) {
        if (hasNext == 0) {
            stopOnLoadingState()
            binding.followsEnd.setImageResource(R.drawable.pic_nomore)
        } else {
            startOnLoadingState(binding.followsEnd)
        }
        var rowCount = data_array.size / 3
        if (data_array.size % 3 != 0) {
            rowCount += 1
        }
        val listRowSizeOld = listRowSize
        listRowSize += rowCount
        binding.followsGrid.rowCount = listRowSize
        binding.followsGrid.columnCount = 3
        val viewWidth = (resources.displayMetrics.widthPixels - dip2px(20f)) / 3
        val imageHeight = (viewWidth - dip2px(12f)) / 3 * 4
        val viewHeight = imageHeight + dip2px(38f)
        var dataInfoIndex = 0
        val nightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        for ((badge, badge_color, badge_color_night, cover, _, _, title, season_id) in data_array) {
            val itemBangumiFollow = ItemBangumiFollowBinding.inflate(layoutInflater, binding.followsGrid, false)
            itemBangumiFollow.followContent.text = title
            if (badge == "") {
                itemBangumiFollow.itemFollowBadgesBackground.visibility = View.GONE
            } else {
                itemBangumiFollow.itemFollowBadgesBackground.visibility = View.VISIBLE
                if (nightMode) {
                    itemBangumiFollow.itemFollowBadgesBackground.setCardBackgroundColor(
                            badge_color_night
                    )
                } else {
                    itemBangumiFollow.itemFollowBadgesBackground.setCardBackgroundColor(badge_color)
                }
                itemBangumiFollow.itemFollowBadges.text = badge
            }

            val requestOptions = RequestOptions()
                    .placeholder(R.drawable.pic_doing_v)
                    .error(R.drawable.pic_load_failed)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(context)
                    .load(cover)
                    .apply(requestOptions)
                    .addListener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                                e: GlideException?, model: Any, target: Target<Drawable?>,
                                isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                                resource: Drawable?, model: Any, target: Target<Drawable?>,
                                dataSource: DataSource, isFirstResource: Boolean
                        ): Boolean {
                            itemBangumiFollow.followImagePlaceholder.animate().alpha(0f)
                                    .setDuration(400)
                                    .setListener(null)
                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    runOnUiThread {
                                        itemBangumiFollow.followImagePlaceholder.visibility = View.GONE
                                        itemBangumiFollow.followImage.visibility = View.VISIBLE
                                        itemBangumiFollow.followImage.animate().alpha(1f)
                                                .setDuration(400).setListener(null)
                                    }
                                }
                            }, 400)
                            return false
                        }
                    }) //.transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemBangumiFollow.followImage)
            itemBangumiFollow.followImage.layoutParams.height = imageHeight
            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(dataInfoIndex / 3 + listRowSizeOld)
            params.columnSpec = GridLayout.spec(dataInfoIndex % 3)
            params.width = viewWidth
            params.height = viewHeight
            itemBangumiFollow.root.setOnClickListener {
                Season.startActivity(context, title, season_id, cover)
            }
            binding.followsGrid.addView(itemBangumiFollow.root, params)
            dataInfoIndex += 1
        }
        binding.followsBase.setScrollViewListener(object : ObservableScrollView.ScrollViewListener {
            override fun onScrollChanged(scrollView: ObservableScrollView?, x: Int, y: Int, oldx: Int, oldy: Int) {
                if (binding.followsPlaceholder.height > y + binding.followsBase.height) {
                    scrollToEnd = false
                } else if (!scrollToEnd) {
                    scrollToEnd = true
                    if (hasNext == 1) {
                        getFollowData(listRowSize / 6 + 1)
                    }
                }
            }
        })
    }

    private var timer: MutableMap<ImageView, Timer?> = mutableMapOf()
    private var imageIndex = 0
    private fun startOnLoadingState(image: ImageView) {
        image.visibility = View.VISIBLE
        if (timer[image] == null){
            timer[image] = Timer()
        }
        timer[image]?.schedule(object : TimerTask() {
            override fun run() {
                imageIndex = if (imageIndex == R.drawable.pic_search_doing_1) R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                runOnUiThread { image.setImageResource(imageIndex) }
            }
        }, 0, 500)
    }

    private fun stopOnLoadingState() {
        for ((key, timer) in timer){
            if (timer != null){
                timer.cancel()
                this.timer[key] = null
            }
        }
    }
}