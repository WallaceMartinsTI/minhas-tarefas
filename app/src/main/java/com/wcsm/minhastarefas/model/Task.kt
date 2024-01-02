package com.wcsm.minhastarefas.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task (
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: String,
    val allowNotification: Int,
    val completed: Int
) : Parcelable