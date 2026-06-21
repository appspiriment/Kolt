# libs/utils — KMP Utility Extensions

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/utils)
[![Kotlin Multiplatform](https://img.shields.io/badge/KMP-commonMain%20%2B%20androidMain-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)

Pure-Kotlin and Android utility extensions. `commonMain` code runs on any KMP target; `androidMain` code relies on Android SDK or `java.time`.

---

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.appspiriment.kolt:utils:0.1.0")
    // Kolt convention plugins add this automatically — opt out with `kolt { enableUtils.set(false) }`
}
```

---

## API Reference

### Flow Utilities (`commonMain`)

```kotlin
// Throttle / debounce a flow
flow.throttleFirst(500L)
flow.debounce(300L)

// Combine multiple flows safely
combineStates(flow1, flow2, flow3) { a, b, c -> /* ... */ }
```

### String Utilities (`commonMain`)

```kotlin
"hello world".capitalizeWords()          // "Hello World"
"  trim me  ".trimToNull()               // null if blank, else trimmed
"abc123".containsDigit()                 // true
"abc".isAlphanumeric()                   // true
"test@email.com".isValidEmail()          // true
"+91 98765 43210".normalizePhone()       // "9876543210"
```

### Number Format Utilities (`commonMain`)

```kotlin
1234567.89.formatCurrency("INR")         // "₹12,34,567.89"
0.75.toPercentString()                   // "75%"
1500.toCompactString()                   // "1.5K"
```

### Validation Utilities (`commonMain`)

```kotlin
ValidationUtils.isValidEmail("a@b.com")  // true
ValidationUtils.isValidPhone("9876543210") // true
ValidationUtils.isValidUrl("https://x.com") // true
ValidationUtils.isValidPan("ABCDE1234F")  // true (Indian PAN)
```

### AsyncState (`commonMain`)

A sealed class for representing async operation states in ViewModels:

```kotlin
sealed class AsyncState<out T> {
    object Idle : AsyncState<Nothing>()
    object Loading : AsyncState<Nothing>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AsyncState<Nothing>()
}

// Usage in ViewModel
val uiState: StateFlow<AsyncState<List<User>>> = flow {
    emit(AsyncState.Loading)
    try {
        emit(AsyncState.Success(repository.getUsers()))
    } catch (e: Exception) {
        emit(AsyncState.Error("Failed to load users", e))
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AsyncState.Idle)
```

### Context Extensions (`androidMain`)

```kotlin
context.showToast("Hello!")
context.copyToClipboard("label", "text")
context.openUrl("https://example.com")
context.isNetworkAvailable()
context.getAppVersionName()             // "1.2.3"
```

### Time Extensions (`androidMain`)

```kotlin
// LocalDate
LocalDate.now().toDisplayString()       // "7 June 2025"
LocalDate.now().toIsoString()           // "2025-06-07"
someDate.daysUntil(otherDate)

// LocalDateTime
LocalDateTime.now().toRelativeString()  // "2 hours ago"
someDateTime.toEpochMillis()

// Millis
1717776000000L.toLocalDate()
1717776000000L.toFormattedTime("hh:mm a") // "06:00 PM"
```

---

## Source Sets

| Source Set | Contents |
|-----------|---------|
| `commonMain` | `FlowUtils`, `ListUtils`, `StringUtils`, `NumberFormatUtils`, `PhoneNumberUtils`, `ValidationUtils`, `AsyncState` |
| `androidMain` | `ContextExtensions`, `FormatUtils`, `LocalDateExtensions`, `LocalDateTimeExtensions`, `ZonedDateTimeExtensions`, `TimingExtensions` |
| `androidUnitTest` | `MillisToMMddHmaTimeTest` |
