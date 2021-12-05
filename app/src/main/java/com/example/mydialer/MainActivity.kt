package com.example.mydialer

import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var contactList = arrayListOf<Contact>()
    private var contactsJson = arrayListOf<Contact>()
    private val adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        val recycleView: RecyclerView = findViewById(R.id.rView)
        val data = "https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download"

        Thread {
            val connection = URL(data).openConnection() as HttpURLConnection
            val jsonData = connection.inputStream.bufferedReader().readText()

            contactsJson = Gson().fromJson(jsonData, Array<Contact>::class.java).toList() as ArrayList<Contact>
            contactList = contactsJson.clone() as ArrayList<Contact>

            runOnUiThread {
                recycleView.layoutManager = LinearLayoutManager(this)
                recycleView.adapter = adapter
                adapter.submitList(contactList)
            }

            val searchBox: EditText = findViewById(R.id.et_search)
            searchBox.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {

                }
                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                    search(searchBox.text.toString())
                }
            })

        }.start()
    }

    fun search(request: String) {
        contactList.clear()
        if(request.isNotBlank()){
            for(contact in contactsJson){
                if ((contact.name.contains(request)) or (contact.phone.contains(request)) or (contact.type.contains(request))){
                    contactList.add(contact)
                }
            }
        }
        else {
            contactList = contactsJson.clone() as ArrayList<Contact>
        }
        adapter.submitList(contactList)
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_tb_menu, menu)
        return true
    }
}

data class Contact(
    val name: String,
    val phone: String,
    val type: String
)

class ContactItemDiffCallback : DiffUtil.ItemCallback<Contact>(){
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val textview1: TextView = view.findViewById(R.id.nameBox)
    private val textview2: TextView = view.findViewById(R.id.phoneBox)
    private val textview3: TextView = view.findViewById(R.id.descBox)

    fun bindTo(contact: Contact){
        textview1.text = contact.name
        textview2.text = contact.phone
        textview3.text = contact.type
    }
}

class Adapter : ListAdapter<Contact, ViewHolder>(ContactItemDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.rview_item, parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(currentList[position])
    }
}


