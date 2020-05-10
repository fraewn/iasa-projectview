package com.iasa.projectview.model.dto

import java.util.*

data class IASAErrorResponse(
    val message: String
) {
    val timestamp by lazy { Date().time }
}
