<div align = "center">
    <h1>Animal Chess AR Android</h1>
    <p>An AR version Animal Chess Game<p>
    <a href="https://firebase.google.com/" target="_blank"><img src="https://img.shields.io/badge/Firebase-Cloud-orange.svg?longCache=true&style=for-the-badge" alt="Firebase"></a>
    <a href="https://gradle.org/" target="_blank"><img src="https://img.shields.io/badge/Gradle-4.4-green.svg?longCache=true&style=for-the-badge" alt="Gradle"></a>
</div>

## Instructions
Animal Chess AR is an Android AR version Animal Chess Game which created by using Google AR Core.
#### To build this application, following techniques are used:  
- AR Core 1.2.0
- Scenefrom SDK 1.0.0

## Development Steps
- 渲染7*9的棋盘， 然后放置
- Cloud Anchor的实现 （两台手机同时看到棋盘）
- 动物放置（8种类型）
- 动物移动animation定义
- 碰撞定义
- 对动物的操作，用tap加view
- 输赢逻辑定义
- 游戏入口出口
- Use Resonance Audio SDK to do 3D sound

## backend logic design (draft 1)
Use Google Firebase Realtime Db to avoid server development.
- db primary key is 房号（roomId）
- roomId(Int) couldAnchorId, user1Id, user2Id, current board

## game logic design (draft 1)
- User1登入游戏，创建room，生成棋盘， 生成cloudAnchorId
- User1 send roomId, cloudAnchorId to realtime Db
- User2使用roomId拿到cloudAnchorId登入游戏，看到棋盘
- 先后手决定？
- 每个user下棋时，block other user to 操作棋子
- 每个user自己回合结束，2台手机同时重新绘制棋子

## 游戏规则参考
### 棋具
棋盤横7列，纵9行。双方底线上各有1个兽穴（置於第一行的中间）和3个陷阱（分别置於兽穴的前、左及右方），在棋盤中央有2个2×3大小的长方形，称为小河，另外有图案标示棋子的起始位置。
双方各有八只以动物命名的棋子。
### 棋规
#### 目的
己方任一棋子走进对方兽穴或吃光对方所有棋子者胜。
#### 棋子
双方的八只棋子由强至弱为：象、狮、虎、豹、狼、狗、猫、鼠。
棋子可以吃掉同级或较弱的棋子。
例外：鼠可以吃掉象，但象却不可以吃掉鼠。
若棋子走进敌方的陷阱，任一棋子都可把它吃掉。
#### 移动
棋子可以纵横向移动一格，但不可移动到自己的兽穴。
只有鼠才可以进入小河，而且可以直接把对岸的动物吃掉。
在小河的棋子不可吃掉在陆地的棋子，反之亦然。但在水中的棋子可吃掉同样在水中的棋子。
狮子可跳纵横河，虎只可跳横河，而且可以直接把对岸的动物吃掉。可是若小河中有鼠（无论是敌方或是己方），狮、虎便不可跳河。　　
#### 胜负判定:
- 任何一方的兽走入敌方的兽穴就算胜利（自己的兽类不可以走入自己的兽穴）；
- 任何一方的兽被吃光就算失败，对方获胜；
- 任何一方所有活着的兽被对方困住，均不可移动时，就算失败，对方获胜；
- 任何一方连续两次走棋时间用完，就算失败，对方获胜；
- 任何一方中途离开游戏，就算逃跑，对方获胜；
- 在连续100回合内，双方均无动物被吃，就算和棋。
#### 违例处理:
- 为了防止无赖长杀，在连续7步棋内，如果同一动物连续超过3次进入同一棋格，在接下来的第8步棋将禁止该动物进入该棋格（若7步内有进入陷阱，则不受该限制；被追动物不受该限制），该规则简称7-3违例规则；
- 为了防止长杀，在连续17步棋内，如果只操作同一个动物，且该动物的活动范围不超过5个棋格，在接下来的第18步棋将禁止该动物进入上述5个棋格中的任意一个（若17步内有进入陷阱，则不受该限制），该规则简称17-5违例规则。

