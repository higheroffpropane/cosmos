package com.dskochilov.cosmos.core.common.coroutines

import javax.inject.Qualifier

/**
 * Hilt qualifier for injecting specific coroutine dispatchers, so production
 * code never references [kotlinx.coroutines.Dispatchers] directly and tests
 * can substitute a test dispatcher.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: CosmosDispatchers)

enum class CosmosDispatchers {
    IO,
    DEFAULT
}
