package io.github.appspiriment.kolt.utils.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn


/*********************************************************
 * Class   :  ExtensionUtils
 * Author  :  Arun Nair
 * Created :  16/09/2022
 *******************************************************
 * Purpose :
 *******************************************************
 * Rework Details:
 * 1) {Author} :  {Date} : {Details}
 *********************************************************/


fun <T> List<T>.combineBy(predicate: (T) -> Boolean, selector: (T) -> Int): Int {
    return filter { predicate.invoke(it) }.sumOf { selector.invoke(it) }
}

fun <T> List<T>.combineBy(predicate: (T) -> Boolean, selector: (T) -> Long): Long {
    return filter { predicate.invoke(it) }.sumOf { selector.invoke(it) }
}

fun <T> List<T>.takeIfNotEmpty(): List<T>? {
    return this.takeIf { it.isNotEmpty() }
}

fun List<String?>.filterNotNullOrBlank(): List<String> {
    return this.filterNotNull().filterNot { it.isBlank() }
}

fun List<String>.filterNotBlank(): List<String> {
    return this.filterNot { it.isBlank() }
}
fun List<String>.filterNotBlankAnd(predicate: (String) -> Boolean): List<String> {
    return this.filter { it.isNotBlank() && predicate(it)}
}

fun <T> List<T>.plusIf(element: List<T>?, predicate: (List<T>) -> Boolean): List<T> {
    return if (element.isNullOrEmpty() || predicate(this).not()) return this else
        this.plus(element)
}
