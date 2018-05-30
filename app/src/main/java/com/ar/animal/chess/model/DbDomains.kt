package com.ar.animal.chess.model


data class ChessDbModel(var roomId: Int = 0,
                        var config: ConfigDbModel = ConfigDbModel(),
                        var gameInfo: GameInfoDbModel = GameInfoDbModel())

data class GameInfoDbModel(var currentRound: Int = 0,
                           var gameState: Int = GameState.USER_A_TURN.ordinal,
                           var timestamp: String = "",
                           var animalInfoList: List<AnimalDbModel> = listOf())

data class ConfigDbModel(var cloudAnchorConfig: CloudAnchorDbModel = CloudAnchorDbModel(),
                         var userConfirmStart: UserConfirmStartDbModel = UserConfirmStartDbModel(),
                         var userA: UserDbModel = UserDbModel(), var userB: UserDbModel = UserDbModel())

data class UserConfirmStartDbModel(var isUserAConfirm: Boolean = false,
                                   var isUserBConfirm: Boolean = false)

data class CloudAnchorDbModel(var roomId: Int = 0, var cloudAnchorId: String = "", var timestamp: String = "")

data class UserDbModel(var userId: String = "", var userType: Int = UserType.USER_A.ordinal,
                       var userImageUrl: String = "", var userName: String = "")

data class TileDbModel(var positionX: Int = 0, var positionY: Int = 0,
                       var tileType: Int = TileType.TILE_GRASS.ordinal)

data class AnimalDbModel(var positionX: Int = 0, var positionY: Int = 0,
                         var state: Int = AnimalState.ALIVE.ordinal,
                         var animalType: Int = AnimalType.RAT.ordinal,
                         var animalDrawType: Int = AnimalDrawType.TYPE_A.ordinal)