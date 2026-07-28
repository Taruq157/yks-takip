package com.omerfaruk.ykstakip.data.local

data class Reward(
    val title: String,
    val isAchieved: Boolean,
    val level: Int // 1: Acemi, 2: Tecrübeli, 3: Uzman
)
