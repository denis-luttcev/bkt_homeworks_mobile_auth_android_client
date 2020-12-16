package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.ktor.util.KtorExperimentalAPI
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.databinding.PostCardLayoutBinding

@KtorExperimentalAPI
class PostAdapter(private val mFiller: LayoutFiller) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = PostCardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostViewHolder(binding.root, binding)
    }

    override fun getItemCount(): Int {
        return sRepository.getListItemCount()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, itemPosition: Int) {
        val post = sRepository.getItemByPosition(itemPosition)
        val postPosition = sRepository.getPostPosition(itemPosition)

        mFiller.initPostCardLayout((holder as PostViewHolder).mBinding, post, postPosition)
        mFiller.initPostView(holder.mBinding, post)
    }
}

class PostViewHolder(itemView: View, val mBinding: PostCardLayoutBinding) : RecyclerView.ViewHolder(itemView)