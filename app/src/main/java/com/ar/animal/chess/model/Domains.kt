package com.ar.animal.chess.model

data class ChessDbModel(var roomId: Int = 0,
                        var config: ConfigDbModel = ConfigDbModel(),
                        var animalList: List<AnimalDbModel> = listOf())

data class ConfigDbModel(var cloudAnchorId: String = "", var currentRound: Int = 0,
                         var gameState: Int = GameState.USER_A_TURN.ordinal,
                         var timestamp: String = "")

data class AnimalDbModel(var positionX: Int = 0, var positionY: Int = 0, var stats: AnimalStats = AnimalStats.ALIVE,
                         var animalType: Int = AnimalType.MOUSE.ordinal)

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

enum class AnimalStats{
    ALIVE,
    DEAD
}

enum class EndPointType {
    DEVELOP,
    RELEASE
}