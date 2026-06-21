package io.github.appspiriment.kolt.utils.state

/**
 * Represents the lifecycle of an asynchronous operation with four mutually exclusive states.
 *
 * Typical usage in a ViewModel state class:
 * ```kotlin
 * data class HomeState(
 *     val articles: AsyncState<List<Article>> = AsyncState.Idle,
 *     val profile:  AsyncState<User>          = AsyncState.Loading,
 * )
 * ```
 *
 * Collecting in the UI:
 * ```kotlin
 * when (val s = state.articles) {
 *     AsyncState.Idle    -> { }
 *     AsyncState.Loading -> CircularProgressIndicator()
 *     is AsyncState.Success -> ArticleList(s.data)
 *     is AsyncState.Error   -> ErrorMessage(s.message)
 * }
 * ```
 */
sealed class AsyncState<out T> {

    /** No operation has been started yet. The initial value for most async fields. */
    data object Idle : AsyncState<Nothing>()

    /** An operation is in progress. */
    data object Loading : AsyncState<Nothing>()

    /** The operation completed successfully. */
    data class Success<out T>(val data: T) : AsyncState<T>()

    /**
     * The operation failed.
     * @param throwable The original exception, if available.
     * @param message   A display-ready error message. If null, use [throwable].message.
     */
    data class Error(
        val throwable: Throwable? = null,
        val message: String? = throwable?.message,
    ) : AsyncState<Nothing>()

    // ── State checks ─────────────────────────────────────────────────────────

    val isIdle: Boolean get() = this is Idle
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    /** The contained data if this is [Success], otherwise null. */
    val dataOrNull: T? get() = (this as? Success)?.data

    /** The throwable if this is [Error], otherwise null. */
    val throwableOrNull: Throwable? get() = (this as? Error)?.throwable

    /** The error message if this is [Error], otherwise null. */
    val errorMessage: String? get() = (this as? Error)?.message

    // ── Chaining callbacks ────────────────────────────────────────────────────

    /** Invokes [block] with the data when this is [Success]. Returns this for chaining. */
    inline fun onSuccess(block: (T) -> Unit): AsyncState<T> {
        if (this is Success) block(data)
        return this
    }

    /** Invokes [block] when this is [Error]. Returns this for chaining. */
    inline fun onError(block: (throwable: Throwable?, message: String?) -> Unit): AsyncState<T> {
        if (this is Error) block(throwable, message)
        return this
    }

    /** Invokes [block] when this is [Loading]. Returns this for chaining. */
    inline fun onLoading(block: () -> Unit): AsyncState<T> {
        if (this is Loading) block()
        return this
    }

    /** Invokes [block] when this is [Idle]. Returns this for chaining. */
    inline fun onIdle(block: () -> Unit): AsyncState<T> {
        if (this is Idle) block()
        return this
    }

    // ── Transformation ────────────────────────────────────────────────────────

    /**
     * Transforms [Success] data with [transform], leaving all other states unchanged.
     * ```kotlin
     * val names: AsyncState<List<String>> = users.map { it.map(User::name) }
     * ```
     */
    fun <R> map(transform: (T) -> R): AsyncState<R> = when (this) {
        is Success -> Success(transform(data))
        is Loading -> Loading
        is Idle -> Idle
        is Error -> Error(throwable, message)
    }

    /**
     * Returns [Success.data] if this is [Success], otherwise [default].
     */
    fun getOrDefault(default: @UnsafeVariance T): T = if (this is Success) data else default

    /**
     * Returns [Success.data] if this is [Success], otherwise the result of [provider].
     */
    inline fun getOrElse(provider: () -> @UnsafeVariance T): T =
        if (this is Success) data else provider()
}

// ── Top-level factory helpers ─────────────────────────────────────────────────

/**
 * Wraps a suspending [block] in [AsyncState], returning [AsyncState.Success] on success
 * or [AsyncState.Error] on any exception.
 *
 * Designed for use inside a ViewModel:
 * ```kotlin
 * updateUiState { it.copy(articles = AsyncState.Loading) }
 * val result = asyncState { repository.getArticles() }
 * updateUiState { it.copy(articles = result) }
 * ```
 */
suspend fun <T> asyncState(block: suspend () -> T): AsyncState<T> = try {
    AsyncState.Success(block())
} catch (e: kotlinx.coroutines.CancellationException) {
    throw e
} catch (e: Throwable) {
    AsyncState.Error(e)
}
