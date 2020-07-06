package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import io.ktor.features.UnsupportedMediaTypeException
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.new_post_layout.*
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.datamodel.AdsPost
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.datamodel.Post
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.datamodel.Repost
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.AuthorizationException
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.LockedException
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.SchemaAPI.*

private const val GALLERY_REQUEST = 100
private const val CAMERA_REQUEST = 101

@KtorExperimentalAPI
class MainActivity : AppCompatActivity() {
    private val mFiller = LayoutFiller(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAuthentication(onSuccess = ::init, onFailure = ::finish)
    }

    private fun checkAuthentication(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        fun extractToken(future: AccountManagerFuture<Bundle>) {
            try {
                future.result.getString(AccountManager.KEY_AUTHTOKEN)?.also { token: String ->
                    sMyToken = token
                    onSuccess()
                }
            } catch (e: Exception) {
                onFailure()
            }
        }

        val accountManager = AccountManager.get(this)
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)

        if (accounts.isEmpty()) {
            addNewAccount(accountManager, ::extractToken)
        } else {
            receiveAuthToken(accounts[0], accountManager, ::extractToken)
        }
    }

    private fun addNewAccount(
        accountManager: AccountManager,
        callback: (future: AccountManagerFuture<Bundle>) -> Unit
    ) {
        accountManager.addAccount(
            ACCOUNT_TYPE,
            TOKEN_TYPE,
            null,
            null,
            this,
            callback,
            null
        )
    }

    private fun receiveAuthToken(
        account: Account,
        accountManager: AccountManager,
        callback: (future: AccountManagerFuture<Bundle>) -> Unit
    ) {
        accountManager.getAuthToken(
            account,
            TOKEN_TYPE,
            null,
            this,
            callback,
            null
        )
    }

    private fun init() {
        sNetworkService.getMe { user ->
            if (null != user) {
                sMyself = user
                mFiller.initViews()

                if (sRepository.isEmpty()) {
                    loadInitData()
                } else {
                    refreshData()
                }
            } else {
                handleAuthorizationException()
            }
        }
    }

    private fun loadInitData() {
        swipeContainer.isRefreshing = true

        sNetworkService.fetchData(::handlePostsAndAds)
    }

    private fun handlePostsAndAds(posts: List<Post>?, ads: List<AdsPost>?) {
        if (isPresent(posts, ads)) {
            sRepository.addPosts(posts!!)
            sRepository.addAds(ads!!)

            mFiller.notifyDataSetChanged()

            swipeContainer.isRefreshing = false

        } else {
            handleAuthorizationException()
        }
    }

    private fun isPresent(posts: List<Post>?, ads: List<AdsPost>?) = posts != null && ads != null

    private fun handleAuthorizationException() {
        makeToast(this, R.string.authorization_error_message)
        swipeContainer.isRefreshing = false

        resetToken()
    }

    private fun resetToken() {
        val accountManager = AccountManager.get(this)
        accountManager.invalidateAuthToken(TOKEN_TYPE, sMyToken)
        sMyToken = ""
        sMyself = null
        checkAuthentication(onSuccess = ::init, onFailure = ::finish)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GALLERY_REQUEST -> {
                if (isImageSelected(resultCode, data)) {
                    val imageUri = data!!.data!!

                    handleGalleryImage(imageUri)
                }
            }
            CAMERA_REQUEST -> {
                //TODO: implement get camera image
                handleCameraImage()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleGalleryImage(imageUri: Uri) {
        newPreviewIv.setImageURI(imageUri)
        newPreviewIv.clearColorFilter()

        newGalleryBtn.visibility = View.GONE
        newCameraBtn.visibility = View.GONE

        sendMedia(imageUri)
    }

    private fun handleCameraImage() {
        //TODO: implement camera image handle
    }

    private fun isImageSelected(resultCode: Int, data: Intent?) =
        resultCode == RESULT_OK && data != null

    fun getGalleryContent() {
        startActivityForResult(Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }, GALLERY_REQUEST)
    }

    fun getCameraContent() {
        //TODO: implement make new image by camera
    }

    fun refreshData() {
        swipeContainer.isRefreshing = true

        sNetworkService.appendPosts(sRepository.getPostsCount(), ::handlePosts)
        sNetworkService.appendAds(sRepository.getAdsCount(), ::handleAds)
    }

    private fun handlePosts(posts: List<Post>?) {
        if (posts != null) {
            sRepository.addPosts(posts)

            mFiller.notifyDataSetChanged()

            swipeContainer.isRefreshing = false

        } else {
            handleAuthorizationException()
        }
    }

    private fun handleAds(ads: List<AdsPost>?) {
        if (ads != null) {
            sRepository.addAds(ads)

        } else {
            handleAuthorizationException()
        }
    }

    fun sendPost(post: Post) {
        sNetworkService.savePost(post, ::updatePostsInAdapter)
    }

    private fun updatePostsInAdapter(successfully: Boolean) {
        if (successfully) {
            refreshData()
        } else {
            handleAuthorizationException()
        }
    }

    fun requestMedia(url: String, imageView: ImageView) {
        sNetworkService.loadMedia(url) {image ->
            if (image != null) {
                imageView.setImageBitmap(image)
            } else {
                handleAuthorizationException()
            }
        }
    }

    private fun sendMedia(mediaUri: Uri) {
        sNetworkService.saveMedia(mediaUri, this, ::handleMedia)
    }

    private fun handleMedia(url: String?, cause: Throwable?) {
        if (url != null) {
            newPreviewIv.tag = url

        } else {
            when (cause) {
                is AuthorizationException -> handleAuthorizationException()

                is UnsupportedMediaTypeException -> {
                    makeToast(this, R.string.unsupported_media_error_message)
                    mFiller.prepareNewImagePostBody()
                }

                else -> {
                    makeToast(this, R.string.load_error_message)
                    mFiller.prepareNewImagePostBody()
                }
            }
        }
    }

    fun updatePostData(post: Post, position: Int) {
        sNetworkService.updatePost(post) { successfully ->
            if (successfully) {
                handleUpdatedPost(position)
            } else {
                handleAuthorizationException()
            }
        }
    }

    private fun handleUpdatedPost(position: Int) {
        mFiller.notifyDataSetChanged()

        postListing.smoothScrollToPosition(position)
        mFiller.prepareNewTextPostBody()
    }

    fun sendSocial(post: Post, action: SocialAction, mode: Mode, checkBox: CheckBox?, countView: TextView?) {
        sNetworkService.updateSocial(post.id, action, mode) { cause ->
            handleSocial(cause, post, action, checkBox, countView)
        }
    }

    private fun handleSocial(cause: Throwable?,
                             post: Post,
                             action: SocialAction,
                             checkBox: CheckBox?,
                             countView: TextView?) {

        val isSelected = checkBox?.isChecked ?: false
        val count: Int

        when (cause) {
            is AuthorizationException -> handleAuthorizationException()
            is LockedException -> makeToast(this, R.string.locked_error_message)

            null -> {
                when (action) {

                    SocialAction.LIKE -> {
                        if (isSelected) {
                            post.like()
                        } else {
                            post.dislike()
                        }
                        count = post.likes
                        mFiller.updateSocialCountView(count, isSelected, countView!!)
                    }

                    SocialAction.COMMENT -> {
                        if (isSelected) {
                            //TODO: implement handle comment text
                            post.makeComment()
                        } else {
                            //TODO: implement delete comment text
                            post.removeComment()
                        }
                        count = post.comments
                        mFiller.updateSocialCountView(count, isSelected, countView!!)
                    }

                    SocialAction.SHARE -> {
                        if (isSelected) {
                            post.share(this)
                            mFiller.prepareNewRepostBody(
                                Repost(author = sMyself!!.username, source = post.id),
                                checkBox,
                                countView
                            )
                        } else {
                            //TODO: implement find and delete repost
                            post.removeShare()
                        }
                        count = post.shares
                        mFiller.updateSocialCountView(count, isSelected, countView!!)
                    }

                    SocialAction.HIDE -> {
                        sRepository.hidePost(post)
                        mFiller.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun requestDelete(post: Post) {
        sNetworkService.deletePost(post.id) { successfully ->
            if (successfully) {
                //TODO: handle case of delete post which is source of repost
                sRepository.deletePost(post)
                mFiller.notifyDataSetChanged()
            } else {
                handleAuthorizationException()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        cancelRequests()
    }

    private fun cancelRequests() {
        sNetworkService.cancellation()
    }
}