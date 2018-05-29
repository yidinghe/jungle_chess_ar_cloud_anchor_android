package com.ar.animal.chess.model


data class AnimalTile(var animal: Animal = Animal(), var tile: Tile = Tile())

data class Tile(var posCol: Int = 0, var posRow: Int = 0,
                var tileType: TileType = TileType.TILE_GRASS)

data class Animal(var posCol: Int = 0, var posRow: Int = 0,
                  var state: AnimalState = AnimalState.ALIVE,
                  var animalType: AnimalType = AnimalType.RAT,
                  var animalDrawType: AnimalDrawType = AnimalDrawType.TYPE_A)

data class ChessUserInfo(var uid: String = "", var displayName: String = "",
                         var photoUrl: String = "", var userType: UserType = UserType.USER_A)

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
    RAT,
    CAT,
    DOG,
    WOLF,
    LEOPARD,
    TIGER,
    LION,
    ELEPHANT
}

enum class AnimalDrawType {
    TYPE_A,
    TYPE_B
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