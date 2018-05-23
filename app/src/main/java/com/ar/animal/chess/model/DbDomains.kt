package com.ar.animal.chess.model


data class ChessDbModel(var roomId: Int = 0,
                        var config: ConfigDbModel = ConfigDbModel(),
                        var animalTileList: List<AnimalTileDbModel> = listOf())

data class ConfigDbModel(var cloudAnchorId: String = "", var currentRound: Int = 0,
                         var gameState: Int = GameState.USER_A_TURN.ordinal,
                         var timestamp: String = "")

data class AnimalTileDbModel(var tileDbModel: TileDbModel = TileDbModel(),
                             var animalDbModel: AnimalDbModel = AnimalDbModel())

data class TileDbModel(var positionX: Int = 0, var positionY: Int = 0,
                       var tileType: Int = TileType.TILE_GRASS.ordinal)

data class AnimalDbModel(var state: Int = AnimalState.ALIVE.ordinal,
                         var animalType: Int = AnimalType.MOUSE.ordinal)