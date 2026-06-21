// Root build file — declare plugins apply false so submodules can apply them.
// Do NOT put dependencies or android {} here.

plugins {
    // Android + Appspiriment convention plugins
    alias(koltlibs.plugins.kolt.application)       apply false
    alias(koltlibs.plugins.kolt.library)           apply false
    alias(koltlibs.plugins.kolt.library.compose)   apply false
    alias(koltlibs.plugins.kolt.library.hilt)      apply false
    alias(koltlibs.plugins.kolt.library.hilt.compose) apply false
    alias(koltlibs.plugins.kolt.data)              apply false
}
