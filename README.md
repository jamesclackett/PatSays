# Pat Says 
---

Pat Says in an online-based 2-4 player card game build for Android devices. It is based off of a game known by many other names (Triple Jeep Jump for example). 
However it has now become known to myself and friends as 'Pat Says'. This is a nod to a friend who, in his wisdom has become the de-facto referee of our games.

The rules of the game are fairly simple. The first person to empty their hand and the table of all cards, wins. Cards may be placed on others so long as they are 'higher', and special cards
change the folow of the game or allow special moves to be played. Cards are distributed evenly among players. Of those cards, 3 are unknown and placed face-down, 3 are selected
by the player and placed face-up. The remainder are kept in their hand.

Players must first empty their hand of cards, then the 3 face-up cards (in any order), and finally the 3 face-down cards (also in any order).

**Please note that PatSays is currently in its final stages of development and not finalised.**

---
#### Rules:

* Played cards are placed on the table on top of previously played cards.
* The played cards can sometimes be 'burned' and removed from the game entirely (see below)
* Suit/Color do not matter
* Multiple cards may be played at the same time (e.g three 7's)
* If the player is unable to make a play, then they must pick up all the 'played' cards

---

#### Card Values (In Ascending Order):

* 4
* 5
* 6
* 7 (reverses order for one turn only. i.e. the next card played must be lower than 7)
* 8 (skip next player's turn)
* 9
* Jack
* Queen
* King
* Ace

Special Cards (can be played at any time, all are equal value)
* 2 (resets played cards to '0')
* 3 (invisible, played but game value remains that of card underneath)
* 10 (burn all played cards and go again)

---
## Design:

Pat Says relies on Googles Realtime Firebase Database to facilitate its online multiplayer, user authentication, and social features. PatSays allows between 2-4 players 
per game and contains a 'friends' system similar to that found in many online multiplayer games. Users can search for others and add them as a friend or invite them to 
games. It is also possible view all of the users recently played with. A list of all friends can be viewed, and from here they can also be imvited to a game.

Games are initiated when a host player creates a pre-game lobby. This triggers a new directory in the realtime DB specifically for that game. All invited users are added 
to this live game directory. Firebase Realtime DB offers a great API for event-driven database listeners. This allows all players to be immediately notified of changes 
in their current game.

---

## User Interface:



