package com.woong.woong_android.seller_market

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.woong.woong_android.applicationcontroller.ApplicationController
import com.woong.woong_android.network.NetworkService
import com.woong.woong_android.seller_market.adapter.SmPagerAdapter
import com.woong.woong_android.seller_market.get.GetMarketInfoResponse
import com.woong.woong_android.home.product.SellerIdx
import com.woong.woong_android.seller_market.get.GetBookmarkFlagResponse
import com.woong.woong_android.seller_market.post.PostBookmarkResponse
import com.woong.woong_android.seller_market.product.SellerMarketProductDetail
import kotlinx.android.synthetic.main.activity_sellermarket.*
import kotlinx.android.synthetic.main.fragment_product_home.*
import kotlinx.android.synthetic.main.title_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.TextView
import android.view.ViewGroup
import com.woong.woong_android.R
import com.woong.woong_android.woong_marketinfo

import com.woong.woong_android.chat
import com.woong.woong_android.notice.get.GetChatRoomIdResponse
import com.woong.woong_android.*
import com.woong.woong_android.notice.message.chat.NoticeChatActivity



class SellerMarketActivity : AppCompatActivity() {
    lateinit var networkService: NetworkService
    lateinit var requestManager: RequestManager
    var bookmarkFlag: Int = 0


    //val tabLayout = tab_top_sellermarket

    fun replaceFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.frame_sellermarket, fragment)
        transaction.addToBackStack(null)    // 이전 상태를 백스택에 추가하여 사용자가 백버튼을 눌렀을때에 대한 호환성 추가
        transaction.commit()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sellermarket)
        requestManager = Glide.with(this)
//        var tv:TextView = LayoutInflater.from(this).inflate(R.id.tab_top_sellermarket, null)
////        textView tv=(TextView)LayoutInflater.from(this).inflate(R.layout.custom_tab,null)
//        textView.setTypeface()
        val market_id = woong_marketinfo.market_id
        ib_message_sellermarket.setOnClickListener {
            woong_marketinfo.market_id = market_id
            networkService = ApplicationController.instance.networkService

            val getChatRoomId = networkService.getChatRoomId(woong_usertoken.user_token,woong_marketinfo.market_id)
            getChatRoomId.enqueue(object:Callback<GetChatRoomIdResponse>{
                override fun onFailure(call: Call<GetChatRoomIdResponse>?, t: Throwable?) {

                }

                override fun onResponse(call: Call<GetChatRoomIdResponse>?, response: Response<GetChatRoomIdResponse>?) {

                    if(response!!.isSuccessful){
                        chat.chat_room_num = response.body().data.chatting_room_id
                    }
                }

            })
            chat.new_room_flag = 1
            val intent = Intent(this@SellerMarketActivity, NoticeChatActivity::class.java)
            startActivity(intent)
        }

        networkService = ApplicationController.instance.networkService


        val myProductPagerAdapter = SmPagerAdapter(supportFragmentManager) // 프래그먼트안에 뷰페이저 쓸경우 childFragmentManager써주세욤
        val viewPager = viewpager_sellermarket
        val tabLayout = tab_top_sellermarket

        viewPager.adapter = myProductPagerAdapter
        viewPager.currentItem = SellerIdx.id
        tabLayout.setTabTextColors(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"))
        tabLayout.setupWithViewPager(viewPager)
//        setCustomFont()
        if (SellerIdx.id == 1) {
            val bundle = Bundle()
            bundle.putInt("market_id", woong_marketinfo.market_id)
            bundle.putInt("item_id", woong_marketinfo.item_id)
            val smpd = SellerMarketProductDetail()
            smpd.arguments = bundle
            replaceFragment(smpd)
            SellerIdx.id = 0
        }

        getMarketInfo()

        setSupportActionBar(toolbar)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.title_layout)
        supportActionBar?.setShowHideAnimationEnabled(false)

        val listener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset <= -1 * dpToPx(126.5f, applicationContext)) {
                toolbar.visibility = View.VISIBLE
                tab_top_sellermarket.setBackgroundColor(Color.WHITE)
                tab_top_sellermarket.setSelectedTabIndicatorColor(Color.parseColor("#529C77"))
                tab_top_sellermarket.setTabTextColors(Color.parseColor("#ADADAD"), Color.parseColor("#529C77"))
            } else {
                toolbar.visibility = View.INVISIBLE
                tab_top_sellermarket.setBackgroundColor(Color.parseColor("#529C77"))
                tab_top_sellermarket.setSelectedTabIndicatorColor(Color.WHITE)
                tab_top_sellermarket.setTabTextColors(Color.WHITE, Color.WHITE)
            }
        }
        appbar_sellermarket.addOnOffsetChangedListener(listener)

        ib_bookmark_sellermarket.setOnClickListener {
            var postBookmark: Call<PostBookmarkResponse>

            if (bookmarkFlag == 1) { // 즐찾 중이라면
                postBookmark = networkService.delBookmark(woong_usertoken.user_token, woong_marketinfo.market_id)
                postBookmark.enqueue(object : Callback<PostBookmarkResponse> {
                    override fun onFailure(call: Call<PostBookmarkResponse>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<PostBookmarkResponse>?, response: Response<PostBookmarkResponse>?) {
                        ib_bookmark_sellermarket.setImageResource(R.drawable.seller_market_intro_favorite)
                        bookmarkFlag = 0
                    }
                })
            } else {
                postBookmark = networkService.postBookmark(woong_usertoken.user_token, woong_marketinfo.market_id)
                postBookmark.enqueue(object : Callback<PostBookmarkResponse> {
                    override fun onFailure(call: Call<PostBookmarkResponse>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<PostBookmarkResponse>?, response: Response<PostBookmarkResponse>?) {
                        ib_bookmark_sellermarket.setImageResource(R.drawable.seller_market_intro_f_like_o)
                        bookmarkFlag = 1
                    }
                })
            }
        }
        ib_cart_title.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            frgIntent.flag=1
            frgIntent.idx=1

            startActivity(intent)
        }
    }

    fun dpToPx(dp: Float, context: Context): Float {
        return (dp * context.resources.displayMetrics.density)
    }

    fun getAcbar(): ActionBar? {
        return supportActionBar
    }

    fun getBookmarkFlag() {
        var getBookmarkFlag = networkService.getBookmarkFlag(woong_usertoken.user_token, woong_marketinfo.market_id)
        getBookmarkFlag.enqueue(object : Callback<GetBookmarkFlagResponse> {
            override fun onFailure(call: Call<GetBookmarkFlagResponse>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<GetBookmarkFlagResponse>?, response: Response<GetBookmarkFlagResponse>?) {
                if (response!!.isSuccessful) {
                    if (response.body().message == "1") {  // 즐찾 중이라면 찬 별 표시
                        ib_bookmark_sellermarket.setImageResource(R.drawable.seller_market_intro_f_like_o)
                        bookmarkFlag = 1
                    } else {
                        ib_bookmark_sellermarket.setImageResource(R.drawable.seller_market_intro_favorite)
                        bookmarkFlag = 0
                    }
                }
            }
        })
    }

    fun getMarketInfo() {
        //유저토큰(header)과 마켓아이디(path)
        var user_token = woong_usertoken.user_token
        var market_id = woong_marketinfo.market_id

        getBookmarkFlag()

        var getMarketInfo = networkService.getMarketDetail(user_token, market_id)
        getMarketInfo.enqueue(object : Callback<GetMarketInfoResponse> {
            override fun onFailure(call: Call<GetMarketInfoResponse>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<GetMarketInfoResponse>?, response: Response<GetMarketInfoResponse>?) {
                if (response!!.isSuccessful) {
                    tv_name_sellermarket.text = response.body().data.market_name
                    var free_flag = response.body().data.delivery
                    if (free_flag == 1) { //유료
                        tv_tag2_sellermarket.text = "#유료배송"
                    } else {
                        tv_tag2_sellermarket.text = "#무료배송"
                    }
                    tv_distance_sellermarket.text = response.body().data.youandi

                    tv_storename_title.text = response.body().data.market_name


                    chat.chat_store_name = tv_storename_title.text.toString()

                    requestManager.load(response.body().data.farmer_image_key).into(iv_profile_sellermarket)
                }
            }
        })

    }
//
//    fun setCustomFont() {
//        val vg = tabLayout.getChildAt(0)
//
//        val vg = tabLayout.getChildAt(0) as ViewGroup
//        val tabsCount = vg.childCount
//
//        for (j in 0 until tabsCount) {
//            val vgTab = vg.getChildAt(j) as ViewGroup
//
//            val tabChildsCount = vgTab.childCount
//
//            for (i in 0 until tabChildsCount) {
//                val tabViewChild = vgTab.getChildAt(i)
//                if (tabViewChild is TextView) {
//                    //Put your font in assests folder
//                    //assign name of the font here (Must be case sensitive)
//                    tabViewChild.typeface = Typeface.createFromAsset(assets, "nanumsquare_bold.ttf")
//                }
//            }
//        }
//    }
}
