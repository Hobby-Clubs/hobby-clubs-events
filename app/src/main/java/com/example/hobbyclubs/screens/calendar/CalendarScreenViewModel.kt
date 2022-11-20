package com.example.hobbyclubs.screens.calendar

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.toDate
import org.joda.time.DateTimeComparator
import java.time.LocalDate

class CalendarScreenViewModel() : ViewModel() {
    val firebase = FirebaseHelper
    val dateTimeComparator = DateTimeComparator.getDateOnlyInstance()

    val allEvents = MutableLiveData<List<Event>>()
    val filteredEvents = MutableLiveData<List<Event>>()
    val selection = MutableLiveData<List<LocalDate>>()
    val listOfUri = MutableLiveData<List<Pair<String, Uri?>>>()

    init {
        getEvents()
    }

    fun onSelectionChanged(selection: List<LocalDate>) {
        if(selection.isNotEmpty()) {
            this.selection.value = selection
//            filteredEvents.value = allEvents.value?.filter { event ->
//                (dateTimeComparator.compare(event.date.toDate(), selection.first().toDate())) == 0
//                // event.date.toDate() == selection.first().toDate()
//            }
        } else {
            this.selection.value = null
//            filteredEvents.value = emptyList()
        }
    }

    fun getEvents() {
        firebase.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("calendarViewModel", "getEvents: ", error)
                    return@addSnapshotListener
                }
                val fetchedEvents = data.toObjects(Event::class.java)
                val eventsByDate = fetchedEvents.sortedByDescending { event -> event.date }
                allEvents.value = eventsByDate
//                getUris(eventsByDate)
            }
    }

//    fun getUris(listOfEvents: List<Event>) {
//        val tempList = mutableListOf<Pair<String, Uri?>>()
//
//        listOfEvents.forEach { event ->
//            FirebaseHelper.getAllFiles("${CollectionName.events}/${event.id}")
//                .addOnSuccessListener { res ->
//                    val items = res.items
//                    if (items.isEmpty()) {
//                        tempList.add(Pair(event.id, null))
//                        if (tempList.size == listOfEvents.size) {
//                            listOfUri.value = tempList.toList()
//                        }
//                        return@addOnSuccessListener
//                    }
//                    val bannerRef = items.find { it.name == "0.jpg" } ?: items.first()
//                    bannerRef
//                        .downloadUrl
//                        .addOnSuccessListener {
//                            tempList.add(Pair(event.id, it))
//                            if (tempList.size == listOfEvents.size) {
//                                listOfUri.value = tempList.toList()
//                            }
//                        }
//                        .addOnFailureListener {
//                            Log.e("getPicUri", "EventTile: ", it)
//                        }
//                }
//                .addOnFailureListener {
//                    Log.e("getAllFiles", "EventTile: ", it)
//                }
//        }
//    }
}
