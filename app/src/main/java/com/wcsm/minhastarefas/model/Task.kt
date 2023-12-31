package com.wcsm.minhastarefas.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task (
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: String,
    val dueDate: String,
    val allowNotification: Boolean,
    val completed: Boolean
) : Parcelable