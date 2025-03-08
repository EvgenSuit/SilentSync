package com.suit.utility.ui

sealed class CustomResult {
    data object None: CustomResult()
    data object InProgress: CustomResult()
    data object Success: CustomResult()
    data object Error: CustomResult()

    fun isInProgress() = this is InProgress
    fun isSuccess() = this is Success
    fun isError() = this is Error
}