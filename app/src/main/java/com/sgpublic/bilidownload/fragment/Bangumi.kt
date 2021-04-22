package com.sgpublic.bilidownload.fragment

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.activity.Search
import com.sgpublic.bilidownload.activity.Season
import com.sgpublic.bilidownload.base.BaseFragment
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.data.FollowData
import com.sgpublic.bilidownload.databinding.FragmentBangumiBinding
import com.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.module.FollowsModule
import com.sgpublic.bilidownload.ui.BannerItem
import com.sgpublic.bilidownload.ui.SeasonBannerAdapter
import com.sgpublic.bilidownload.widget.ObservableScrollView
import com.sgpublic.bilidownload.widget.ObservableScrollView.ScrollViewListener
import com.zhpan.bannerview.constants.IndicatorGravity
import java.util.*
import kotlin.collections.ArrayList

class Bangumi(private val context: AppCompatActivity): BaseFragment<FragmentBangumiBinding>(context) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        startOnLoadingState(binding.bangumiLoadState)
        getFollowData(1)
    }

    override fun onViewSetup() {
        super.onViewSetup()
        initViewAtTop(binding.bangumiSearchBase)
        initViewAtTop(binding.bangumiBanner)
        binding.bangumiSearch.setOnClickListener {
            Search.startActivity(context)
        }
        binding.bangumiRefresh.setOnRefreshListener {
            Timer().schedule(object : TimerTask(){
                override fun run() {
                    getFollowData(1)
                }
            }, 1000)
        }
    }

    private fun getFollowData(pageIndex: Int) {
        val accessKey = ConfigManager.getString("access_key")
        val mid = ConfigManager.getLong("mid")
        val helper = FollowsModule(context, accessKey)
        helper.getFollows(mid, pageIndex, object : FollowsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    stopOnLoadingState()
                    binding.bangumiLoadState.setImageResource(R.drawable.pic_load_failed)
                    binding.bangumiRefresh.isRefreshing = false
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(followData: ArrayList<FollowData>, has_next: Int) {
                runOnUiThread {
                    stopOnLoadingState()
                    if (followData.isEmpty()) {
                        binding.bangumiLoadState.setImageResource(R.drawable.pic_null)
                        binding.bangumiRefresh.isRefreshing = false
                    } else {
                        binding.bangumiLoadState.visibility = View.INVISIBLE
                        binding.bangumiBase.visibility = View.VISIBLE
                        if (pageIndex == 1) {
                            setupUserData(followData, has_next)
                        } else {
                            setGrid(followData, has_next)
                        }
                    }
                }
            }
        })
    }

    private var isFirstChange = true
    private var bannerInfoList: ArrayList<BannerItem> = ArrayList()

    private fun setupUserData(dataArray: ArrayList<FollowData>, has_next: Int) {
        isFirstChange = true
        listRowSize = 0
        bannerInfoList = ArrayList()
        val nightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        for ((badge, badgeColor1, badgeColorNight, cover, _, isFinish, title, seasonId, newEpCover, _, newEpIndexShow, newEpIsNew) in dataArray) {
            if (isFinish == 0 || newEpIsNew == 1) {
                val badgeColor: Int = if (nightMode) {
                    badgeColorNight
                } else {
                    badgeColor1
                }
                bannerInfoList.add(
                    BannerItem(
                        context,
                        newEpCover,
                        cover,
                        seasonId,
                        title,
                        newEpIndexShow,
                        badge,
                        badgeColor
                    )
                )
                if (bannerInfoList.size > 7) {
                    break
                }
            }
        }
        val bannerItemCount = dataArray.size.coerceAtMost(5)
        while (bannerInfoList.size < bannerItemCount) {
            val (badge, badgeColor1, badgeColorNight, cover, _, isFinish, title, seasonId, newEpCover, _, newEpIndexShow) = dataArray[(Math.random() * dataArray.size).toInt()]
            if (isFinish == 1) {
                var isEquals = false
                for (item_index in bannerInfoList) {
                    if (item_index.seasonId == seasonId) {
                        isEquals = true
                        break
                    }
                }
                if (!isEquals) {
                    val badgeColor: Int = if (nightMode) {
                        badgeColorNight
                    } else {
                        badgeColor1
                    }
                    bannerInfoList.add(
                        BannerItem(
                            context,
                            newEpCover,
                            cover,
                            seasonId,
                            title,
                            newEpIndexShow,
                            badge,
                            badgeColor
                        )
                    )
                }
            }
        }
        runOnUiThread {
            binding.bangumiFollows.removeAllViews()
            binding.bangumiBanner.setIndicatorVisibility(View.VISIBLE)
            binding.bangumiBanner.setIndicatorGravity(IndicatorGravity.CENTER)
            binding.bangumiBanner.setOnPageClickListener { i: Int ->
                val dataInfo: BannerItem = bannerInfoList[i]
                Season.startActivity(
                    context,
                    dataInfo.title,
                    dataInfo.seasonId,
                    dataInfo.seasonCover
                )
            }
            binding.bangumiBanner.setHolderCreator { SeasonBannerAdapter() }
            binding.bangumiBanner.create(bannerInfoList)
            binding.bangumiRefresh.isRefreshing = false
            setGrid(dataArray, has_next)
        }
    }

    private var listRowSize = 0
    private var scrollToEnd = false

    private fun setGrid(data_array: ArrayList<FollowData>, has_next: Int) {
        if (has_next == 0) {
            stopOnLoadingState()
            binding.bangumiFollowEnd.setImageResource(R.drawable.pic_nomore)
        } else {
            startOnLoadingState(binding.bangumiFollowEnd)
        }
        var rowCount = data_array.size / 3
        if (data_array.size % 3 != 0) {
            rowCount += 1
        }
        val listRowSizeOld = listRowSize
        listRowSize += rowCount
        binding.bangumiFollows.rowCount = listRowSize
        binding.bangumiFollows.columnCount = 3
        val viewWidth = (resources.displayMetrics.widthPixels - dip2px(20f)) / 3
        val imageHeight = (viewWidth - dip2px(12f)) / 3 * 4
        val viewHeight = imageHeight + dip2px(38f)
        var dataInfoIndex = 0
        val nightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        for ((badge, badge_color, badge_color_night, cover, _, _, title, season_id) in data_array) {
            val itemBangumiFollow = ItemBangumiFollowBinding.inflate(
                layoutInflater,
                binding.bangumiFollows,
                false
            )
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
            binding.bangumiFollows.addView(itemBangumiFollow.root, params)
            dataInfoIndex += 1
        }
        binding.bangumiBase.setScrollViewListener(object : ScrollViewListener {
            override fun onScrollChanged(
                scrollView: ObservableScrollView?,
                x: Int,
                y: Int,
                oldx: Int,
                oldy: Int
            ) {
                if (binding.bangumiPlaceholder.height > y + binding.bangumiBase.height) {
                    scrollToEnd = false
                } else if (!scrollToEnd) {
                    scrollToEnd = true
                    if (has_next == 1) {
                        getFollowData(listRowSize / 6 + 1)
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        binding.bangumiBanner.stopLoop()
    }

    override fun onResume() {
        super.onResume()
        binding.bangumiBanner.startLoop()
    }

    private var timer: Timer? = null
    private var imageIndex = 0

    private fun startOnLoadingState(imageView: ImageView) {
        imageView.visibility = View.VISIBLE
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                imageIndex =
                    if (imageIndex == R.drawable.pic_search_doing_1) R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                runOnUiThread { imageView.setImageResource(imageIndex) }
            }
        }, 0, 500)
    }

    private fun stopOnLoadingState() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }
}