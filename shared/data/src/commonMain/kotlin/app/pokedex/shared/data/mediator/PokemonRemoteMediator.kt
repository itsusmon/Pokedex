package app.pokedex.shared.data.mediator

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import app.pokedex.shared.common.either.fold
import app.pokedex.shared.data.mapper.PokemonResponseMapper
import app.pokedex.shared.database.PokemonEntity
import app.pokedex.shared.database.dao.PokemonDao
import app.pokedex.shared.network.api.PokedexApi

internal class PokemonRemoteMediator(
    private val pokemonDao: PokemonDao,
    private val pokedexApi: PokedexApi,
    private val pokemonResponseMapper: PokemonResponseMapper,
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                if (lastItem == null) 0 else lastItem.page + 1
            }
        }
        return pokedexApi.fetchPokemons(
            page = page,
            pageSize = state.config.pageSize
        ).fold(
            onSuccess = { pokemonsResponse ->
                val pokemonEntities = pokemonsResponse.results.map { pokemonResponse ->
                    pokemonResponseMapper.toEntity(response = pokemonResponse, page = page)
                }
                if (loadType == LoadType.REFRESH) {
                    pokemonDao.clearAndInsertPokemon(pokemonEntities)
                } else {
                    pokemonDao.upsertPokemon(pokemonEntities)
                }
                MediatorResult.Success(endOfPaginationReached = pokemonsResponse.next == null)
            },
            onFailure = MediatorResult::Error
        )
    }
}