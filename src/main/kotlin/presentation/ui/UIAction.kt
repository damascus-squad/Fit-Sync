package org.damascus.presentation.ui

data class UiAction(
    val name: String,
    val action: () -> Unit
)