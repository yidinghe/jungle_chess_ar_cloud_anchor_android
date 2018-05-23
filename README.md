<div align = "center">
    <h1>Jungle Chess AR Android</h1>
    <p>An AR version Jungle Chess Game<p>
    <a href="https://firebase.google.com/" target="_blank"><img src="https://img.shields.io/badge/Firebase-Cloud-orange.svg?longCache=true&style=for-the-badge" alt="Firebase"></a>
    <a href="https://gradle.org/" target="_blank"><img src="https://img.shields.io/badge/Gradle-4.4-green.svg?longCache=true&style=for-the-badge" alt="Gradle"></a>
</div>

## Instructions
Jungle Chess AR is an Android AR version Jungle Chess Game which created by using Google AR Core and CloudAnchor.
#### To build this application, following techniques are used:  
- AR Core 1.2.0
- Scenefrom SDK 1.0.1

## Game Board Example
[Jungle Board Game wiki documentation](https://en.wikipedia.org/wiki/Jungle_(board_game))
![Game_Board_Demo](Resources/example_board.png)
## Rules
### Objective
The goal of the game is either to move a piece onto a special square, the den, on the opponent's side of the board, or capture all of the opponent's pieces.
### Board
The Jungle gameboard consists of seven columns and nine rows of squares. Pieces move on the square spaces as in international chess, not on the lines as in xiangqi. Pictures of eight animals and their names appear on each side of the board to indicate initial placement of the game pieces. After initial setup, these animal spaces have no special meaning in gameplay.
There are several special squares and areas of the Jungle board: The den (Chinese: 獸穴: literally: "lair") is located in the center of the first row or rank of the board, and is labeled as such in Chinese. Traps (Chinese: 陷阱; literally: "snare") are located to each side and in front of the den, and are also labeled in Chinese. Two water areas or rivers (Chinese: 河川; literally: "river") are located in the center of the Jungle board. Each comprises six squares in a 2×3 rectangle, and labeled with the Chinese characters for "river". There are single columns or files of ordinary land squares on the edges of the board, and down the middle between the rivers.
### Pieces
Each side has eight pieces representing different animals, each with a different rank. Higher ranking pieces can capture all pieces of identical or weaker ranking. However, there is one exception: The rat may capture the elephant, while the elephant may or may not capture the rat (depending on the variant). The animal ranking, from strongest to weakest, is:
- Elephant
- Lion
- Tiger
- Leopard
- Wolf
- Dog
- Cat
- Rat

Pieces are placed onto the corresponding pictures of the animals which are invariably shown on the Jungle board. All pieces move one space orthogonally.
### Movement
Players alternate moves with White moving first. During their turn, a player must move. Each piece moves one square horizontally or vertically (not diagonally). A piece may not move to its own den.
There are special rules related to the water squares:
- The rat is the only animal that is allowed to go onto a water square. However, some players prefer that the dog is also allowed to go onto a water square, which makes it easier to capture the opponent's rat in the water.
- The rat may not capture the elephant or another rat on land directly from a water square. If the dog is also allowed to go onto a water square, the dog may not capture the rat on land directly from a water square.
- Similarly, a rat on land may not attack a rat in the water.
- The rat may attack the opponent rat in the water if both pieces are in the water. If the dog is also allowed to go onto a water square, the dog may also attack the opponent's rat when both pieces are in the water.
- The lion and tiger pieces may jump over a river by moving horizontally or vertically. They move from a square on one edge of the river to the next non-water square on the other side. Such a move is not allowed if there is a rat (whether friendly or enemy) on any of the intervening water squares. If the dog is allowed on water, a dog (friendly or enemy) on any intervening water square also prevents the lion and the tiger from jumping over the river. The lion and tiger are allowed to capture enemy pieces by such jumping moves.
### Capturing
Animals capture the opponent pieces by "eating" them. A piece can capture any enemy piece which has the same or lower rank, with the following exceptions:
- The rat may kill (capture) the elephant.
- The player may capture any enemy piece in one of the player's trap squares regardless of rank.

## Development Steps
- render 7*9 Game Board，then place it
- Integrate with Cloud Anchor and Firebase Realtime Db so that two phones can have a same AR Game Board
- Render 8 types of Animals and place them
- Define Animal movement and capturing animation
- Define how to control Animals by using tap and views
- Define Game Win/Lose logic
- Implement All the UIs including launch page and setting page
- Integrate with Google SignIn to get user info
- Use Resonance Audio SDK to do 3D sound

## backend logic design (draft 2)
Use Google Firebase Realtime Db to avoid server development.
- db primary key is roomId
- roomId(Int) couldAnchorId, user1Id, user2Id, current board
- data class ChessDbModel(var roomId: Int = 0,
                         var config: ConfigDbModel = ConfigDbModel(),
                         var animalList: List<AnimalDbModel> = listOf())
- data class ConfigDbModel(var cloudAnchorId: String = "", var currentRound: Int = 0,
                           var gameState: Int = GameState.USER_A_TURN.ordinal,
                           var timestamp: String = "")
- data class AnimalDbModel(var positionX: Int = 0, var positionY: Int = 0,
                           var animalType: Int = AnimalType.MOUSE.ordinal)

## game logic design (draft 1)
- UserA login game，create room，generate roomId，host CloudAnchor and generate cloudAnchorId
- UserA send roomId, cloudAnchorId to realtime Db
- UserB login game, pair with roomId, see the game board
- Define turns, by using gameState and currentRound
- When UserA plays game，block UserB tap event for the Game Board, When UserB plays game，block UserA tap event for the Game Board, 
- Every 5s? pull db to update view and rerender Game Board
- Every user turn finish, pull db to update view and rerender Game Board