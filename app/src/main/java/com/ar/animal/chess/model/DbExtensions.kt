package com.ar.animal.chess.model

fun AnimalDbModel.mapDbModelToDomain(): Animal {
    val animal = Animal(positionX, positionY)
    when (state) {
        AnimalState.ALIVE.ordinal -> animal.state = AnimalState.ALIVE
        AnimalState.DEAD.ordinal -> animal.state = AnimalState.DEAD
    }

    when (animalDrawType) {
        AnimalDrawType.TYPE_A.ordinal -> animal.animalDrawType = AnimalDrawType.TYPE_A
        AnimalDrawType.TYPE_B.ordinal -> animal.animalDrawType = AnimalDrawType.TYPE_B
    }

    when (animalType) {
        AnimalType.RAT.ordinal -> animal.animalType = AnimalType.RAT
        AnimalType.CAT.ordinal -> animal.animalType = AnimalType.CAT
        AnimalType.DOG.ordinal -> animal.animalType = AnimalType.DOG
        AnimalType.WOLF.ordinal -> animal.animalType = AnimalType.WOLF
        AnimalType.LEOPARD.ordinal -> animal.animalType = AnimalType.LEOPARD
        AnimalType.TIGER.ordinal -> animal.animalType = AnimalType.TIGER
        AnimalType.LION.ordinal -> animal.animalType = AnimalType.LION
        AnimalType.ELEPHANT.ordinal -> animal.animalType = AnimalType.ELEPHANT
    }

    return animal
}