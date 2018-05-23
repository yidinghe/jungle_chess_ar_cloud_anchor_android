package com.ar.animal.chess.model


data class ChessDbModel(var roomId: Int = 0,
                        var config: ConfigDbModel = ConfigDbModel(),
                        var tileList: List<TileDbModel> = listOf(),
                        var animalAList: List<AnimalDbModel> = listOf(),
                        var animalBList: List<AnimalDbModel> = listOf())

data class ConfigDbModel(var cloudAnchorId: String = "", var currentRound: Int = 0,
                         var gameState: Int = GameState.USER_A_TURN.ordinal,
                         var timestamp: String = "", var userList: List<UserDbModel> = listOf())

data class UserDbModel(var userId: String = "", var userType: Int = UserType.USER_A.ordinal,
                       var userImageUrl: String = "", var userName: String = "")

data class TileDbModel(var positionX: Int = 0, var positionY: Int = 0,
                       var tileType: Int = TileType.TILE_GRASS.ordinal)

data class AnimalDbModel(var positionX: Int = 0, var positionY: Int = 0,
                         var state: Int = AnimalState.ALIVE.ordinal,
                         var animalType: Int = AnimalType.RAT.ordinal)