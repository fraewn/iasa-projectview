package com.iasa.projectview.model.dto

import java.util.*

class IASAApiResponse<T>(
    val data: T?
) {
    val timestamp by lazy { Date().time }
}
