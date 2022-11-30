package com.example.hobbyclubs.general
//
//import android.net.Uri
//import android.util.Log
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.example.hobbyclubs.api.*
//
//class ImageViewModel : ViewModel() {
//    val clubLogoUris = MutableLiveData<List<Pair<String, Uri?>>>(listOf())
//    val eventBannerUris = MutableLiveData<List<Pair<String, Uri?>>>(listOf())
//    val newsBannerUris = MutableLiveData<List<Pair<String, Uri?>>>(listOf())
//    val clubBannerUris = MutableLiveData<List<Pair<String, Uri?>>>(listOf())
//    val clubMemberProfilePicUris = MutableLiveData<List<Pair<String, Uri?>>>(listOf())
//
//    fun getEventUris(listOfEvents: List<Event>) {
//        val tempList = mutableListOf<Pair<String, Uri?>>()
//
//        listOfEvents.forEach { event ->
//            FirebaseHelper.getAllFiles("${CollectionName.events}/${event.id}")
//                .addOnSuccessListener { res ->
//                    val items = res.items
//                    if (items.isEmpty()) {
//                        tempList.add(Pair(event.id, null))
//                        if (tempList.size == listOfEvents.size) {
//                            eventBannerUris.value = tempList.toList()
//                        }
//                        return@addOnSuccessListener
//                    }
//                    val bannerRef = items.find { it.name == "0.jpg" } ?: items.first()
//                    bannerRef
//                        .downloadUrl
//                        .addOnSuccessListener {
//                            tempList.add(Pair(event.id, it))
//                            if (tempList.size == listOfEvents.size) {
//                                eventBannerUris.value = tempList.toList()
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
//
//    fun getClubUris(listOfClubs: List<Club>) {
//        val tempListLogo = mutableListOf<Pair<String, Uri?>>()
//        val tempListBanner = mutableListOf<Pair<String, Uri?>>()
//
//        listOfClubs.forEach { club ->
//            FirebaseHelper.getAllFiles("${CollectionName.clubs}/${club.ref}")
//                .addOnSuccessListener { res ->
//                    val items = res.items
//                    val logoRef = items.find { it.name == "logo" }
//                    val bannerRef = items.find { it.name == "banner" }
//                    if (logoRef == null) {
//                        tempListLogo.add(Pair(club.ref, null))
//                        if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
//                            clubBannerUris.value = tempListBanner.toList()
//                            clubLogoUris.value = tempListLogo.toList()
//                        }
//                        return@addOnSuccessListener
//                    } else {
//                        logoRef.downloadUrl.addOnSuccessListener {
//                            tempListLogo.add(Pair(club.ref, it))
//                            if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
//                                clubBannerUris.value = tempListBanner.toList()
//                                clubLogoUris.value = tempListLogo.toList()
//                            }
//                        }
//                    }
//                    if (bannerRef == null) {
//                        tempListBanner.add(Pair(club.ref, null))
//                        if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
//                            clubBannerUris.value = tempListBanner.toList()
//                            clubLogoUris.value = tempListLogo.toList()
//                        }
//                        return@addOnSuccessListener
//                    } else {
//                        bannerRef.downloadUrl.addOnSuccessListener {
//                            tempListBanner.add(Pair(club.ref, it))
//                            if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
//                                clubBannerUris.value = tempListBanner.toList()
//                                clubLogoUris.value = tempListLogo.toList()
//                            }
//                        }
//                    }
//                }
//                .addOnFailureListener {
//                    Log.e("getAllFiles", "ClubTile: ", it)
//                }
//        }
//    }
//
//    fun getUserProfileUrisFromRequest(listOfRequests: List<Request>) {
//        val tempListProfile = mutableListOf<Pair<String, Uri?>>()
//
//        listOfRequests.forEach { request ->
//            FirebaseHelper.getFile("${CollectionName.users}/${request.userId}")
//                .downloadUrl
//                .addOnSuccessListener {
//                    tempListProfile.add(Pair(request.userId, it))
//                    if (tempListProfile.size == listOfRequests.size) {
//                        clubMemberProfilePicUris.value = tempListProfile.toList()
//                    }
//                }
//                .addOnFailureListener {
//                    Log.e("getPicUri", "getUserProfileUris: ", it)
//                }
//        }
//    }
//    fun getUserProfileUris(listOfUsers: List<User>) {
//        val tempListProfile = mutableListOf<Pair<String, Uri?>>()
//
//        listOfUsers.forEach { user ->
//            FirebaseHelper.getFile("${CollectionName.users}/${user.uid}")
//                .downloadUrl
//                .addOnSuccessListener {
//                    tempListProfile.add(Pair(user.uid, it))
//                    if (tempListProfile.size == listOfUsers.size) {
//                        clubMemberProfilePicUris.value = tempListProfile.toList()
//                    }
//                }
//                .addOnFailureListener {
//                    Log.e("getPicUri", "getUserProfileUris: ", it)
//                }
//        }
//    }
//    fun getNewsUris(listOfNews: List<News>, isSmallTile: Boolean) {
//        val tempListProfile = mutableListOf<Pair<String, Uri?>>()
//
//        listOfNews.forEach { news ->
//            val path = if (isSmallTile) "${CollectionName.clubs}/${news.clubId}/logo" else "${CollectionName.news}/${news.id}/newsImage.jpg"
//            Log.d("getPicUri", "getNewsUris: $path")
//            FirebaseHelper.getFile(path)
//                .downloadUrl
//                .addOnSuccessListener {
//                    tempListProfile.add(Pair(news.clubId, it))
//                    if (tempListProfile.size == listOfNews.size) {
//                        newsBannerUris.value = tempListProfile.toList()
//                    }
//                }
//                .addOnFailureListener {
//                    Log.e("getPicUri", "getNewsBannerUris: ", it)
//                }
//        }
//    }
//}