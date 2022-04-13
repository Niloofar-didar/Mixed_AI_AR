package com.arcore.MixedAIAR;

import android.app.Activity
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.CHOICE_MODE_SINGLE
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

//lateinit var consumer : BitmapCollector
class CustomAdapter(private val mList: MutableList<ItemsViewModel>, val streamSource: DynamicBitmapSource/*BitmapSource*/, val activity: Activity) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
     // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ai_settings_card_view_design, parent, false)

        return ViewHolder(view)

    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]
        var outputText : SpannableStringBuilder? = null

        holder.modelListView.choiceMode = CHOICE_MODE_SINGLE
        holder.deviceListView.choiceMode = CHOICE_MODE_SINGLE

        val modelAdapter = ArrayAdapter(
            holder.modelListView.context,
            R.layout.ai_settings_listview_row,
            R.id.listview_row_text,
            itemsViewModel.models
        )
        val deviceAdapter = ArrayAdapter(
            holder.modelListView.context,
            R.layout.ai_settings_listview_row,
            R.id.listview_row_text,
            itemsViewModel.devices
        )

        holder.modelListView.adapter = modelAdapter
        holder.deviceListView.adapter = deviceAdapter

        holder.numberPicker.minValue = 1
        holder.numberPicker.maxValue = 10
        holder.numberPicker.wrapSelectorWheel = true

        holder.modelListView.setItemChecked(0, true)
        holder.deviceListView.setItemChecked(0, true)

        initializeActiveModel(holder, itemsViewModel)
        itemsViewModel.consumer = BitmapCollector(streamSource, itemsViewModel.classifier, /*holder.textView,*/ activity)

        holder.numberPicker.setOnValueChangedListener {
                picker, oldVal, newVal -> updateActiveModel(holder, itemsViewModel) }
        holder.modelListView.setOnItemClickListener { parent, view, pos, id ->
            updateActiveModel(holder, itemsViewModel)
        }
        holder.deviceListView.setOnItemClickListener { parent, view, pos, id ->
            updateActiveModel(holder, itemsViewModel)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var modelListView: ListView = itemView.findViewById(R.id.model)
        val deviceListView: ListView = itemView.findViewById(R.id.device)
        val numberPicker: NumberPicker= itemView.findViewById(R.id.np)
        val textView: TextView = itemView.findViewById(R.id.TextView_AiOutput)
    }


    fun initializeActiveModel(holder: CustomAdapter.ViewHolder, itemsView: ItemsViewModel) {
        itemsView.currentModel = 0
        itemsView.currentDevice = 0
        itemsView.currentNumThreads = 1
        itemsView.classifier=ImageClassifierFloatMobileNet(activity)
    }
    fun updateActiveModel(holder: CustomAdapter.ViewHolder, itemsView : ItemsViewModel) {
        itemsView.consumer?.pauseCollect()

        // Get UI information before delegating to background
        val modelIndex: Int = holder.modelListView.checkedItemPosition
        val deviceIndex: Int = holder.deviceListView.checkedItemPosition
        val numThreads: Int = holder.numberPicker.value

        // Do not update if there is no change
        if (modelIndex == itemsView.currentModel
            && deviceIndex == itemsView.currentDevice
            && numThreads == itemsView.currentNumThreads) {
            return
        }
        itemsView.currentModel = modelIndex
        itemsView.currentDevice = deviceIndex
        itemsView.currentNumThreads = numThreads


        // Disable classifier while updating
        if (itemsView.classifier != null) {
            itemsView.classifier?.close()
            itemsView.classifier = null
        }

        // Lookup names of parameters.
        val model: String = itemsView.models[itemsView.currentModel]
        val device: String = itemsView.devices[itemsView.currentDevice]

        Log.i("Custom Adapter",
            "Changing model to $model device $device"
        )

        // Try to load model.
        try {
            when(model) {
                itemsView.models[0]->itemsView.classifier=ImageClassifierFloatMobileNet(activity)
                itemsView.models[1]->itemsView.classifier=ImageClassifierQuantizedMobileNet(activity)
            }
        } catch (e: IOException) {
            Log.d(
                "Custom Adapter",
                "Failed to load",
                e
            )
            itemsView.classifier = null
        }

        when(device) {
            itemsView.devices[0]-> itemsView.classifier?.useCPU()
            itemsView.devices[1]-> itemsView.classifier?.useGpu()
            itemsView.devices[2]-> itemsView.classifier?.useNNAPI()
        }
        itemsView.setCollector(BitmapCollector(streamSource, itemsView.classifier,/* holder.textView,*/ activity))
        itemsView.consumer?.startCollect()
    }


}
