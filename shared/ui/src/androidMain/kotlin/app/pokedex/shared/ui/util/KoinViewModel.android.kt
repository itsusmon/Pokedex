package app.pokedex.shared.ui.util

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.module.dsl.DefinitionOptions
import org.koin.core.qualifier.Qualifier
import org.koin.androidx.viewmodel.dsl.viewModel as actualViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf as actualViewModelOf

@Composable
actual inline fun <reified T : ViewModel> viewModel(): T {
    return koinViewModel()
}

actual inline fun <reified R : ViewModel> Module.viewModelOf(
    crossinline constructor: () -> R,
    noinline options: DefinitionOptions<R>?,
): KoinDefinition<R> = actualViewModelOf(constructor, options)

actual inline fun <reified R : ViewModel> Module.viewModel(
    qualifier: Qualifier?,
    noinline definition: Definition<R>,
): KoinDefinition<R> = actualViewModel(qualifier, definition)