package com.example.notekeeper.ui.adapters

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.notekeeper.R
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.databinding.NoteRecyclerLayoutBinding
import com.example.notekeeper.utils.Constants
import java.util.*
import javax.inject.Inject


class NotesAdapter @Inject constructor(): RecyclerView.Adapter<NotesAdapter.CustomViewHolder>()
{
    private lateinit var binding: NoteRecyclerLayoutBinding
    private var dataList = emptyList<NoteEntity>()

    lateinit var context: Context

    private lateinit var popupMenu: PopupMenu

    lateinit var adapterState: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder
    {
        binding = NoteRecyclerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        if(adapterState == Constants.ADAPTER_STATE_FAVORITE)
            binding.recyclerLayoutMenu.visibility = View.GONE
        else if(adapterState == Constants.ADAPTER_STATE_NOTES)
            binding.recyclerLayoutMenu.visibility = View.VISIBLE
        return CustomViewHolder()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int)
    {
        holder.bindItems(dataList[position])
        holder.setIsRecyclable(false)

        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 700
        holder.itemView.startAnimation(anim)
    }


    override fun getItemCount() = dataList.size

    inner class CustomViewHolder: RecyclerView.ViewHolder(binding.root)
    {
        fun bindItems(entity: NoteEntity)
        {
            binding.apply {

                //put database information in recycler items to show
                if(entity.title.isEmpty())
                {
                    recyclerLayoutTitle.setTextColor(0xFF959595.toInt())
                    recyclerLayoutTitle.text = "No Title"
                }
                else
                {
                    recyclerLayoutTitle.setTextColor(0xFF000000.toInt())
                    recyclerLayoutTitle.text = entity.title
                }

                if(entity.desc.isEmpty())
                {
                    recyclerLayoutDesc.setTextColor(0xFF959595.toInt())
                    recyclerLayoutDesc.text = "No Description"
                }
                else
                {
                    recyclerLayoutDesc.setTextColor(0xFF000000.toInt())
                    recyclerLayoutDesc.text = entity.desc
                }

                val date = Date(entity.date)
                recyclerLayoutDate.text = DateFormat.format("yyyy/MM/dd\nhh:mm:ss a", date).toString()


                //set priority colors
                when(entity.priority)
                {
                    2 -> {
                        priorityView1.setBackgroundColor(Color.RED)
                        if(adapterState == Constants.ADAPTER_STATE_FAVORITE)
                            priorityView2.setBackgroundColor(Color.RED)
                        else if(adapterState == Constants.ADAPTER_STATE_NOTES)
                            priorityView2.setBackgroundColor(0xFFFFFFFF.toInt())
                    }
                    1 -> {
                        priorityView1.setBackgroundColor(-22000)
                        if(adapterState == Constants.ADAPTER_STATE_FAVORITE)
                            priorityView2.setBackgroundColor(-22000)
                        else if(adapterState == Constants.ADAPTER_STATE_NOTES)
                            priorityView2.setBackgroundColor(0xFFFFFFFF.toInt())
                    }
                    0 -> {
                        priorityView1.setBackgroundColor(0xFFFFEB3B.toInt())
                        if(adapterState == Constants.ADAPTER_STATE_FAVORITE)
                            priorityView2.setBackgroundColor(0xFFFFEB3B.toInt())
                        else if(adapterState == Constants.ADAPTER_STATE_NOTES)
                            priorityView2.setBackgroundColor(0xFFFFFFFF.toInt())
                    }
                }

//                0xFFFF4F4F.toInt()

                //on click for opening each item or closing it
                cardView.setOnClickListener {
                    onItemClickListener?.let {
                        it(entity, Constants.ON_CLICK_GOTO_DETAIL)
                    }
                }

                //recycler menu item click listener
                recyclerLayoutMenu.setOnClickListener {
                    initPopUpMenu(it.findViewById(R.id.recycler_layout_menu))
                    menuOnClickListener(entity)
                }

                //long click on recycler items to open the recycler menu
                cardView.setOnLongClickListener {
                    initPopUpMenu(it.findViewById(R.id.recycler_layout_menu))
                    menuOnClickListener(entity)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    //on item select handling
    private var onItemClickListener: ((NoteEntity, String) -> Unit)? = null
    fun setOnItemCLickListener(listener: (NoteEntity, String) -> Unit){
        onItemClickListener = listener
    }


    fun setData(newListData: List<NoteEntity>)
    {
        val notesDiffUtils = NotesDiffUtils(dataList, newListData)
        val diffUtils = DiffUtil.calculateDiff(notesDiffUtils)
        dataList = newListData
        diffUtils.dispatchUpdatesTo(this)
    }

    class NotesDiffUtils(private val oldItem: List<NoteEntity>, private val newItem: List<NoteEntity>) : DiffUtil.Callback(){
        override fun getOldListSize() = oldItem.size

        override fun getNewListSize() = newItem.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
        {
            return oldItem[oldItemPosition] === newItem[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
        {
            return oldItem[oldItemPosition] === newItem[newItemPosition]
        }
    }

    private fun initPopUpMenu(view: View)
    {
        popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.recycler_items_menu, popupMenu.menu)
        popupMenu.show()
    }

    private fun menuOnClickListener(entity: NoteEntity)
    {
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuItem.itemId)
            {
                R.id.menu_detail_delete -> {
                    onItemClickListener?.let {
                        it(entity, Constants.ON_CLICK_DELETE)
                    }
                    return@setOnMenuItemClickListener true
                }

                R.id.menu_detail_share -> {
                    onItemClickListener?.let {
                        it(entity, Constants.ON_CLICK_SHARE)
                    }
                    return@setOnMenuItemClickListener true
                }

                R.id.menu_detail_edit -> {
                    onItemClickListener?.let {
                        it(entity, Constants.ON_CLICK_EDIT)
                    }
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
        }
    }
}