package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.ktor.util.KtorExperimentalAPI
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.databinding.PostCardLayoutBinding
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.datamodel.Post

@KtorExperimentalAPI
class PostAdapter(private val mFiller: LayoutFiller) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class PostViewHolder(
        private val mBinding: PostCardLayoutBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {
        fun bind(post: Post, postPosition: Int) {
            mFiller.initPostCardLayout(mBinding, post, postPosition)
            mFiller.initPostView(mBinding, post)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PostCardLayoutBinding.inflate(layoutInflater, parent, false)

        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return sRepository.getListItemCount()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, itemPosition: Int) {
        val post = sRepository.getItemByPosition(itemPosition)
        val postPosition = sRepository.getPostPosition(itemPosition)

        (holder as PostViewHolder).bind(post, postPosition)
    }
}
