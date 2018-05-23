package com.ar.animal.chess.model


data class AnimalTile(var animal: Animal = Animal(), var tile: Tile = Tile())

data class Tile(var posCol: Int = 0, var posRow: Int = 0,
                var tileType: TileType = TileType.TILE_GRASS)

data class Animal(var posCol: Int = 0, var posRow: Int = 0,
                  var state: AnimalState = AnimalState.ALIVE,
                  var animalType: AnimalType = AnimalType.MOUSE)

enum class UserType {
    USER_A,
    USER_B
}

enum class GameState {
    USER_A_TURN,
    USER_B_TURN,
    USER_A_WIN,
    USER_B_WIN,
    NO_WIN_USER
}

enum class AnimalType {
    MOUSE,
    CAT,
    DOG,
    WOLVES,
    LEOPARD,
    TIGER,
    LION,
    ELEPHANT
}

enum class AnimalState {
    ALIVE,
    DEAD
}

enum class EndPointType {
    DEVELOP,
    RELEASE
}

enum class TileType() {
    TILE_GRASS,
    TILE_TRAP,
    TILE_RIVER,
    TILE_BASEMENT
}