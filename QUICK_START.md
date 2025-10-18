# Quick Start Guide

## 🚀 Running the Application

### Prerequisites
1. **Android Studio** (Hedgehog 2023.1.1 or later)
2. **Android Device or Emulator** (API 24+)
3. **For P2P Testing**: Two physical Android devices with Bluetooth

### Installation Steps

1. **Open Project**
   ```
   - Launch Android Studio
   - File → Open
   - Navigate to: /Users/vibhubhardwaj/Downloads/TicTac/tic-tac-toe
   - Click "Open"
   ```

2. **Sync Gradle**
   ```
   - Wait for Gradle sync to complete automatically
   - Or click: File → Sync Project with Gradle Files
   ```

3. **Run on Device/Emulator**
   ```
   - Connect Android device via USB (enable USB debugging)
   - Or start an emulator
   - Click the green "Run" button (▶️)
   - Select your device
   - Wait for app to install and launch
   ```

## 🎮 How to Use Each Feature

### Play vs Computer (AI)

1. From main menu, tap **"Play vs Computer"**
2. You are X (blue), tap any empty cell to make your move
3. AI plays as O (red) and responds automatically
4. **To change difficulty**: Tap "Change Difficulty" button (works mid-game!)
5. **To reset**: Tap "Reset Game"
6. **Remember**: Getting 3 in a row means you LOSE!

**Testing Different Difficulties:**
- **Easy**: You should win easily (AI makes random moves)
- **Medium**: You can win sometimes (AI is inconsistent)
- **Hard**: Very difficult to win (AI plays optimally)

### Play vs Human (Same Device)

1. From main menu, tap **"Play vs Human"**
2. Tap **"Play on Same Device"**
3. Players alternate turns on the same device
4. X plays first (blue), then O (red)
5. **To reset**: Tap "Reset Game"

### Play vs Human (Different Devices - Bluetooth)

**Setup (Do Once):**
1. Enable Bluetooth on both devices
2. Pair devices via Android Bluetooth settings
3. Keep devices within 10 meters

**To Play:**

**On Device 1 (Host):**
1. Open app → "Play vs Human" → "Play on Different Devices"
2. Tap **"Wait for Connection (Host)"**
3. Wait for Device 2 to connect

**On Device 2 (Client):**
1. Open app → "Play vs Human" → "Play on Different Devices"
2. Select Device 1 from the list of paired devices
3. Wait for connection

**Once Connected:**
1. Both devices show "Who Goes First?" dialog
2. One player (either device) selects **"ME"** or **"OPPONENT"**
3. First player gets X, second gets O
4. Take turns making moves
5. Moves appear on both devices automatically!
6. Game ends when someone wins or draws
7. **To play again**: Tap "Reset Game" on either device

### View Game History

1. From main menu, tap **"Game History"**
2. Scroll to see all past games
3. Each entry shows:
   - Date and time played
   - Winner (X, O, or Draw)
   - Difficulty mode (for AI games)
   - Game mode (Computer or Human)

**To verify persistence:**
1. Play a game
2. Force stop the app
3. Reopen → Check history → Your game is still there!

### Change Settings

1. From main menu, tap **"Settings"**
2. Select a difficulty:
   - **Easy**: AI random moves
   - **Medium**: AI 50% random, 50% optimal
   - **Hard**: AI always optimal
3. Read game rules explanation
4. Tap "Back to Menu"
5. Your selection is saved!

## 🧪 Testing Scenarios

### Test 1: Basic Gameplay
```
1. Play vs Computer (Hard)
2. Make moves until game ends
3. Verify winner is announced
4. Check Game History - your game should appear
```

### Test 2: Difficulty Switching
```
1. Start game on Easy mode
2. Make 1-2 moves
3. Change to Hard mode (board should NOT reset)
4. Continue playing - AI should be smarter now
```

### Test 3: Data Persistence
```
1. Play 3 games vs Computer
2. Check Game History - see all 3 games
3. Force stop app (swipe away from recents)
4. Reopen app
5. Check Game History - all 3 games still there!
```

### Test 4: Peer-to-Peer
```
1. Pair two devices via Bluetooth
2. Device 1: Wait for connection
3. Device 2: Connect to Device 1
4. Select who goes first
5. Make moves on each device
6. Verify moves appear on both devices
7. Complete game
8. Check history on BOTH devices
```

### Test 5: Misere Rules
```
1. Play vs Computer (Easy for easier testing)
2. Intentionally try to make 3 in a row
3. When you succeed - YOU LOSE (as expected!)
4. Try to force AI to make 3 in a row
5. When AI does - YOU WIN!
```

## 🔍 Troubleshooting

### App won't build
```
Solution:
- File → Invalidate Caches → Invalidate and Restart
- Build → Clean Project
- Build → Rebuild Project
```

### Bluetooth not connecting
```
Solution:
- Ensure Bluetooth is enabled on both devices
- Check devices are paired in Android settings
- Make sure app has Bluetooth permissions
- Try unpairing and re-pairing devices
- Keep devices close together (< 10 meters)
```

### "AI is thinking" takes too long
```
This is normal for Hard mode on first move.
Usually takes < 1 second, max ~3 seconds.
If longer, the algorithm is working correctly!
```

### Game history is empty
```
Solution:
- Play at least one complete game
- Make sure game reaches win/draw state
- History updates automatically
```

### Can't tap cells during AI's turn
```
This is expected behavior!
Wait for AI to make its move.
"AI is thinking..." message should appear.
```

## 📝 Quick Feature Reference

| Feature | Location | What to Test |
|---------|----------|--------------|
| AI Gameplay | Play vs Computer | Make moves, change difficulty |
| Local Multiplayer | Play vs Human → Same Device | Two players alternate |
| P2P Multiplayer | Play vs Human → Different Devices | Connect, play, sync |
| History | Game History | View past games, scroll list |
| Settings | Settings | Change difficulty, view rules |
| Misere Rules | Any game | Get 3 in row = LOSE |
| Persistence | Close & reopen app | History still there |

## 🎯 Key Things to Demonstrate

1. **Misere Rules Work**: Show that getting 3 in a row = lose
2. **AI Difficulty Levels**: Show Easy (random) vs Hard (optimal)
3. **Mid-game Difficulty Change**: Change without resetting board
4. **History Persistence**: Close app, reopen, data still there
5. **P2P Sync**: Moves appear on both devices in real-time
6. **Who Goes First**: Show selection dialog and symbol assignment

## 🏆 Success Criteria

✅ **App is working correctly if:**
- Can play complete games vs AI
- Can change difficulty mid-game
- History shows past games with all details
- Can play vs human on same device
- Can connect two devices via Bluetooth
- Moves sync across connected devices
- History persists after app closure
- Misere rules are enforced (3 in row = lose)

## 📞 Need Help?

Check these files:
- `README.md` - Detailed documentation
- `IMPLEMENTATION_SUMMARY.md` - Technical details
- `PROJECT_CHECKLIST.md` - Complete verification list
- Code comments - Explanations in source files

## 🎓 Project Structure Quick Reference

```
Key Files:
├── MainActivity.kt              ← Main menu
├── PlayComputerActivity.kt      ← AI gameplay
├── PlayHumanActivity.kt         ← PvP gameplay
├── HistoryActivity.kt           ← Game history
├── SettingsActivity.kt          ← Settings
├── GameLogic.kt                 ← Game rules
├── MinimaxAI.kt                 ← AI algorithm
├── BluetoothManager.kt          ← P2P connectivity
└── database/                    ← Data storage
    ├── GameHistory.kt
    ├── GameHistoryDao.kt
    └── AppDatabase.kt
```

---

**Ready to use!** Follow the steps above to start playing. 🎮


