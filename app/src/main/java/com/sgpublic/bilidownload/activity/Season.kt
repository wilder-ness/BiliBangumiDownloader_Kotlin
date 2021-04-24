package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.data.Episode.InfoData
import com.sgpublic.bilidownload.data.SeasonData
import com.sgpublic.bilidownload.data.SeriesData
import com.sgpublic.bilidownload.databinding.*
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.module.SeasonModule
import com.sgpublic.bilidownload.ui.BlurHelper
import com.sgpublic.bilidownload.ui.SeasonPagerAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class Season: BaseActivity<ActivitySeasonBinding>() {
    private val isVip: Int = ConfigManager.getInt("vip_state")
    private lateinit var qualityAccessInt: IntArray
    private lateinit var qualityAccessString: ArrayList<String>

    private lateinit var episodeData: ArrayList<InfoData>
    private lateinit var seasonData: SeasonData
    private lateinit var seasonInfo: SeriesData
    private lateinit var tabTitles: ArrayList<String>

    private lateinit var pagerPlaceholderBinding: PagerPlaceholderBinding
    private lateinit var pagerInfoBinding: PagerInfoBinding
    private lateinit var pagerDownloadBinding: PagerDownloadBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        seasonInfo = SeriesData()
        seasonInfo.season_id = intent.getLongExtra("season_id", 0)
        seasonInfo.cover = intent.getStringExtra("cover_url").toString()
        seasonInfo.title = intent.getStringExtra("title").toString()
        binding.seasonCollapsingToolbar.title = seasonInfo.title

        val requestOptions = RequestOptions()
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

        Glide.with(this)
                .load(seasonInfo.cover)
                .apply(requestOptions)
                .apply(RequestOptions.bitmapTransform(BlurHelper()))
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                setAnimateState(true, 400, binding.seasonCoverBackground)
                            }
                        }, 400)
                        return false
                    }
                })
                .into(binding.seasonCoverBackground)

        Glide.with(this)
                .load(seasonInfo.cover)
                .apply(requestOptions.placeholder(R.drawable.pic_doing_v))
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        setAnimateState(false, 400, binding.seasonCoverPlaceholder) {
                            setAnimateState(true, 400, binding.seasonCover)
                        }
                        return false
                    }
                })
                .into(binding.seasonCover)

        val module = SeasonModule(this@Season, ConfigManager.getString("access_key"))
        module.getInfoBySid(seasonInfo.season_id, object : SeasonModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    stopOnLoadingState()
                    binding.seasonLoading.setImageResource(R.drawable.pic_load_failed)
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(episodeData: ArrayList<InfoData>, seasonData: SeasonData) {
                this@Season.episodeData = episodeData
                this@Season.seasonInfo = seasonData.base_info
                this@Season.seasonData = seasonData
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            onSetupSeasonInfo()
                        }
                    }
                }, 500)
            }
        })
    }

    override fun onViewSetup() {
        pagerPlaceholderBinding = PagerPlaceholderBinding.inflate(
            layoutInflater,
            binding.seasonViewpager,
            false
        )
        pagerInfoBinding = PagerInfoBinding.inflate(layoutInflater, binding.seasonViewpager, false)
        pagerDownloadBinding = PagerDownloadBinding.inflate(
            layoutInflater,
            binding.seasonViewpager,
            false
        )
        tabTitles = arrayListOf(
            getString(R.string.title_season_info),
            getString(R.string.title_season_download)
        )

        startOnLoadingState(binding.seasonLoading)
        setSupportActionBar(binding.seasonToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        val viewList = ArrayList<View>()
        viewList.add(pagerPlaceholderBinding.root)
        viewList.add(pagerPlaceholderBinding.root)
        binding.seasonViewpager.adapter = SeasonPagerAdapter(viewList, tabTitles)

        binding.seasonTab.run {
            for (title in tabTitles){
                addTab(newTab().setText(title))
            }
            setupWithViewPager(binding.seasonViewpager)
        }
    }

    private fun onSetupSeasonInfo() {
        this.qualityAccessInt = IntArray(seasonData.qualities.size)
        this.qualityAccessString = ArrayList()
        for (qdIndex in seasonData.qualities.indices) {
            if (isVip == 0 && qdIndex == 0) {
                continue
            }
            val (quality, description) = seasonData.qualities[qdIndex]
            qualityAccessString.add(description)
            qualityAccessInt[qdIndex] = quality
        }
        runOnUiThread {
            setAnimateState(false, 300, binding.seasonLoading, ::stopOnLoadingState)
            setAnimateState(false, 300, binding.seasonViewpager){
                val viewList = ArrayList<View>()
                viewList.add(pagerInfoBinding.root)
                viewList.add(pagerDownloadBinding.root)
                runOnUiThread {
                    binding.seasonViewpager.adapter = SeasonPagerAdapter(viewList, tabTitles)
                    try {
                        if (episodeData.size > 0) {
                            pagerDownloadBinding.seasonEpisodeList.visibility = View.VISIBLE
                            pagerDownloadBinding.seasonNoEpisode.visibility = View.GONE
                        } else {
                            pagerDownloadBinding.seasonEpisodeList.visibility = View.GONE
                            pagerDownloadBinding.seasonNoEpisode.visibility = View.VISIBLE
                        }
                    onSeasonInfoLoad()
                    onEpisodeLoad()
                    } catch (ignore: NullPointerException) { }
                }
                setAnimateState(true, 500, binding.seasonViewpager)
            }
        }
    }

    private fun onSeasonInfoLoad() {
        if (seasonData.rating == 0.0) {
            pagerInfoBinding.seasonRatingString.visibility = View.INVISIBLE
            pagerInfoBinding.seasonRatingNull.visibility = View.VISIBLE
            pagerInfoBinding.seasonRatingStar.progress = 0
        } else {
            pagerInfoBinding.seasonRatingNull.visibility = View.INVISIBLE
            pagerInfoBinding.seasonRatingString.visibility = View.VISIBLE
            pagerInfoBinding.seasonRatingString.text = seasonData.rating.toString()
            pagerInfoBinding.seasonRatingStar.progress = seasonData.rating.roundToInt()
        }
        pagerInfoBinding.seasonStuff.setOnClickListener {
            pagerInfoBinding.seasonStuff.maxLines =
                if (seasonData.staff_lines == pagerInfoBinding.seasonStuff.maxLines) 3 else seasonData.staff_lines
        }
        pagerInfoBinding.seasonActors.setOnClickListener {
            pagerInfoBinding.seasonActors.maxLines =
                if (seasonData.actors_lines == pagerInfoBinding.seasonActors.maxLines) 3 else seasonData.actors_lines
        }
        pagerInfoBinding.seasonContent.text = seasonData.description
        if (seasonData.alias != "") {
            pagerInfoBinding.seasonAliasBase.visibility = View.VISIBLE
            pagerInfoBinding.seasonAlias.text = seasonData.alias
        }
        if (seasonData.styles != "") {
            pagerInfoBinding.seasonStylesBase.visibility = View.VISIBLE
            pagerInfoBinding.seasonStyles.text = seasonData.styles
        }
        if (seasonData.styles != "" || seasonData.alias != "") {
            pagerInfoBinding.seasonAliasStylesBase.visibility = View.VISIBLE
        }
        if (seasonData.actors != "") {
            pagerInfoBinding.seasonActorsBase.visibility = View.VISIBLE
            pagerInfoBinding.seasonActors.text = seasonData.actors
        }
        if (seasonData.staff != "") {
            pagerInfoBinding.seasonStuffBase.visibility = View.VISIBLE
            pagerInfoBinding.seasonStuff.text = seasonData.staff
        }
        if (seasonData.evaluate != "") {
            pagerInfoBinding.seasonEvaluateBase.visibility = View.VISIBLE
            pagerInfoBinding.seasonEvaluate.text = seasonData.evaluate
        }
        if (seasonData.series.size == 0) {
            return
        }
        pagerInfoBinding.seasonSeriesBase.visibility = View.VISIBLE
        var rowCount = seasonData.series.size / 3
        if (seasonData.series.size % 3 != 0) {
            rowCount += 1
        }
        pagerInfoBinding.seasonSeries.rowCount = rowCount
        pagerInfoBinding.seasonSeries.columnCount = 3

        val viewWidth = (resources.displayMetrics.widthPixels - dip2px(56f)) / 3
        val imageHeight = (viewWidth - dip2px(12f)) / 3 * 4
        val viewHeight = imageHeight + dip2px(38f)

        var dataInfoIndex = 0

        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        for ((_, _, badge, badge_color, badge_color_night, cover, title, season_id) in seasonData.series) {
            val itemBangumeFollow = ItemBangumiFollowBinding.inflate(
                layoutInflater,
                pagerInfoBinding.seasonSeries,
                false
            )
            itemBangumeFollow.followContent.text = title
            if (badge == "") {
                itemBangumeFollow.itemFollowBadgesBackground.visibility = View.GONE
            } else {
                itemBangumeFollow.itemFollowBadgesBackground.visibility = View.VISIBLE
                if (nightMode) {
                    itemBangumeFollow.itemFollowBadgesBackground.setCardBackgroundColor(
                        badge_color_night
                    )
                } else {
                    itemBangumeFollow.itemFollowBadgesBackground.setCardBackgroundColor(
                        badge_color
                    )
                }
                itemBangumeFollow.itemFollowBadges.text = badge
            }
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_v)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(this@Season)
                .load(cover)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        setAnimateState(false, 400, itemBangumeFollow.followImagePlaceholder) {
                            setAnimateState(true, 400, itemBangumeFollow.followImage)
                        }
                        return false
                    }
                })
                .into(itemBangumeFollow.followImage)
            itemBangumeFollow.followImage.layoutParams.height = imageHeight
            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(dataInfoIndex / 3)
            params.columnSpec = GridLayout.spec(dataInfoIndex % 3)
            params.width = viewWidth
            params.height = viewHeight
            itemBangumeFollow.root.setOnClickListener {
                startActivity(this@Season, title, season_id, cover)
            }
            pagerInfoBinding.seasonSeries.addView(itemBangumeFollow.root, params)
            dataInfoIndex += 1
        }
    }

    private fun onEpisodeLoad() {
        if (episodeData.size <= 0) {
            return
        }
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this@Season, android.R.layout.simple_spinner_dropdown_item, qualityAccessString)
        pagerDownloadBinding.seasonQuality.adapter = arrayAdapter
        var qualitySet = qualityAccessInt.indexOf(ConfigManager.getInt("quality", 80))
        if (qualitySet < 0) {
            qualitySet = 0
        }
        pagerDownloadBinding.seasonQuality.setSelection(qualitySet)
        val viewWidth = (resources.displayMetrics.widthPixels - dip2px(36f)) / 2
        val imageHeight = (viewWidth - dip2px(12f)) / 8 * 5
//        val viewHeight = imageHeight + dip2px(60f)
        var rowCount = episodeData.size / 2
        if (episodeData.size % 2 != 0) {
            rowCount += 1
        }
        pagerDownloadBinding.seasonGrid.columnCount = 2
        pagerDownloadBinding.seasonGrid.rowCount = rowCount
        val nightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        for (episode_index in episodeData.indices) {
            val episodeDataIndex = episodeData[episode_index]
            val itemSeasonEpisode = ItemSeasonEpisodeBinding.inflate(layoutInflater, pagerDownloadBinding.seasonGrid, false)
            if (episodeDataIndex.title == ""){
                itemSeasonEpisode.episodeTitle.visibility = View.GONE
            } else {
                itemSeasonEpisode.episodeTitle.text = episodeDataIndex.title
            }
            itemSeasonEpisode.episodePublicTime.text = String.format(
                getString(R.string.text_episode_public_time),
                episodeDataIndex.pub_real_time
            )
            var indexTitle = episodeDataIndex.index
            try {
                indexTitle.toFloat()
                indexTitle = String.format(
                    getString(R.string.text_episode_index), indexTitle
                )
            } catch (ignore: NumberFormatException) {
            }
            itemSeasonEpisode.episodeIndexTitle.text = indexTitle
            if (episodeDataIndex.badge == "") {
                itemSeasonEpisode.episodeVipBackground.visibility = View.GONE
            } else {
                itemSeasonEpisode.episodeVipBackground.visibility = View.VISIBLE
                if (nightMode) {
                    itemSeasonEpisode.episodeVipBackground.setCardBackgroundColor(episodeDataIndex.badge_color_night)
                } else {
                    itemSeasonEpisode.episodeVipBackground.setCardBackgroundColor(episodeDataIndex.badge_color)
                }
                itemSeasonEpisode.episodeVip.text = episodeDataIndex.badge
            }
            itemSeasonEpisode.root.setOnClickListener {
                if (episodeDataIndex.payment == 13 && isVip == 0) {
                    onToast(R.string.text_episode_vip_needed)
                } else if (ConfigManager.checkClient() == null) {
                    onToast(R.string.text_episode_no_app_installed)
                } else {
                    onToast("开发中")
                }
            }
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_h)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(this@Season)
                .load(episodeDataIndex.cover)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        setAnimateState(true, 400, itemSeasonEpisode.episodeImage)
                        return false
                    }
                })
                .into(itemSeasonEpisode.episodeImage)
            itemSeasonEpisode.episodeImage.layoutParams.height = imageHeight
            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(episode_index / 2)
            params.columnSpec = GridLayout.spec(episode_index % 2)
            params.width = viewWidth
//            params.height = viewHeight
            pagerDownloadBinding.seasonGrid.addView(itemSeasonEpisode.root, params)
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context, title: String?, sid: Long, cover_url: String?) {
            val intent = Intent(context, Season::class.java)
            intent.putExtra("season_id", sid)
            intent.putExtra("cover_url", cover_url)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }
    }
}