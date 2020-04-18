package com.iasa.projectview.util

import java.util.*

fun Date.addSeconds(amount: Int): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.add(Calendar.SECOND, amount)
    return cal.time
}
