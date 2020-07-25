package com.example.gmapsample.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gmapsample.R
import com.example.gmapsample.model.ProfileImage
import com.example.gmapsample.ui.component.ListItemDecoration
import com.example.gmapsample.ui.component.RecyclerItemClickListener
import com.example.gmapsample.ui.component.SelectCell


class SelectPictureFragment : Activity() {
    private lateinit var listView: RecyclerView
    private lateinit var adapter: SelectAdapter
    private val picturesList = ArrayList<ProfileImage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_select_picture)

        addProfilePictures()

        listView = findViewById(R.id.listView)
        adapter = SelectAdapter(this)
        listView.adapter = adapter
        listView.layoutManager =
            GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        listView.addItemDecoration(ListItemDecoration(12, 3))
        listView.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                listView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val selectCell = view as SelectCell
                        finishSelf(
                            resources.getIdentifier(
                                selectCell.textView.text.toString(), "drawable",
                                packageName
                            )
                        )
                    }

                    override fun OnItemLongClick(view: View, position: Int) {

                    }

                })
        )

    }

    private fun addProfilePictures() {
        picturesList.add(
            ProfileImage(
                R.drawable.ic_mouse,
                this.resources.getResourceEntryName(R.drawable.ic_mouse)
            )
        )
        picturesList.add(
            ProfileImage(
                R.drawable.ic_sick,
                this.resources.getResourceEntryName(R.drawable.ic_sick)
            )
        )
        picturesList.add(
            ProfileImage(
                R.drawable.ic_time,
                this.resources.getResourceEntryName(R.drawable.ic_time)
            )
        )

        picturesList.add(
            ProfileImage(
                R.drawable.ic_sick,
                this.resources.getResourceEntryName(R.drawable.ic_sick)
            )
        )
        picturesList.add(
            ProfileImage(
                R.drawable.ic_time,
                this.resources.getResourceEntryName(R.drawable.ic_time)
            )
        )
        picturesList.add(
            ProfileImage(
                R.drawable.ic_mouse,
                this.resources.getResourceEntryName(R.drawable.ic_mouse)
            )
        )
    }


    private inner class SelectAdapter : RecyclerView.Adapter<IViewHolder> {

        private var parentActivity: Activity

        constructor(parentActivity: Activity) : super() {
            this.parentActivity = parentActivity
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IViewHolder {
            val view = SelectCell(parentActivity)
            return IViewHolder(view)
        }

        override fun getItemCount(): Int {
            return picturesList.size
        }

        override fun onBindViewHolder(holder: IViewHolder, position: Int) {
            val profileImage: ProfileImage = picturesList[position]
            holder.imageView.setImageDrawable(parentActivity.getDrawable(profileImage.imageId))
            holder.textView.text = profileImage.imageName
        }

    }

    private fun finishSelf(pictureId: Int) {
        val intent = Intent()
        if (pictureId != 0) {
            intent.putExtra("picture_id", pictureId)
            setResult(RESULT_OK, intent)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishSelf(0)
    }

    private class IViewHolder : RecyclerView.ViewHolder {
        var imageView: ImageView
        var textView: TextView

        constructor(view: View) : super(view) {
            val selectCell = view as SelectCell
            imageView = selectCell.imageView
            textView = selectCell.textView

        }
    }
}