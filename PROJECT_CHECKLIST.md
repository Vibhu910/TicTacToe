# Project Verification Checklist

Use this checklist to verify all project requirements are met during demonstration.

## 🎮 Game Rules

- [ ] Getting 3 in a row means the player who made it LOSES
- [ ] Winner is the one who forces opponent to make 3 in a row
- [ ] Draw occurs when board fills without any 3 in a row

## 📱 Required Screens

### 1. Game Screen (Play vs Computer)
- [ ] 3x3 interactive grid displayed
- [ ] Symbols appear when tapping empty cells
- [ ] Cannot tap occupied cells
- [ ] Player always plays as X
- [ ] AI plays as O
- [ ] Turn alternates properly
- [ ] Win/draw message displays correctly
- [ ] Reset button works
- [ ] Back to menu button works

### 2. Settings Screen
- [ ] Three difficulty options visible (Easy, Medium, Hard)
- [ ] Can select one difficulty at a time (mutually exclusive)
- [ ] Selection persists when returning to menu
- [ ] Back button works

### 3. Past Games Screen
- [ ] Shows list of past games
- [ ] Each game displays:
  - [ ] Date and time
  - [ ] Winner (X, O, or Draw)
  - [ ] Difficulty mode
- [ ] List is scrollable
- [ ] Back button works

## 🤖 AI Functionality

### Easy Mode
- [ ] AI makes completely random moves
- [ ] Human can win easily

### Medium Mode
- [ ] AI makes mix of random and optimal moves
- [ ] Human can win sometimes

### Hard Mode
- [ ] AI makes optimal moves only
- [ ] "AI is thinking..." message appears
- [ ] Very difficult to beat (should draw if both play well)
- [ ] AI only loses if human plays perfectly

### Difficulty Switching
- [ ] Can change difficulty mid-game
- [ ] Board does NOT reset when changing difficulty
- [ ] Next AI move reflects new difficulty

## 💾 Data Persistence

- [ ] Play a game
- [ ] Check that it appears in Game History
- [ ] Close app completely (force stop)
- [ ] Reopen app
- [ ] History still shows previous games
- [ ] Difficulty setting is remembered

## 👥 Human vs Human (Same Device)

- [ ] Select "Play vs Human" from menu
- [ ] Select "Play on Same Device"
- [ ] Two players can alternate turns
- [ ] Player X goes first
- [ ] Cannot tap occupied cells
- [ ] Win/draw detection works
- [ ] Reset button works
- [ ] Game appears in history

## 📡 Peer-to-Peer (Different Devices)

### Setup (Do this first)
- [ ] Two Android devices available
- [ ] Bluetooth enabled on both
- [ ] Devices paired via Android settings

### Connection
- [ ] Open app on both devices
- [ ] Both select "Play vs Human" → "Play on Different Devices"
- [ ] Device 1: Tap "Wait for Connection (Host)"
- [ ] Device 2: Select Device 1 from list
- [ ] Connection message appears on both

### Who Goes First
- [ ] "Who Goes First?" dialog appears on both devices
- [ ] Two options visible: "ME" and "OPPONENT"
- [ ] One player selects an option
- [ ] Both devices show who goes first
- [ ] Correct player symbols assigned (first player = X)

### Gameplay
- [ ] First player can make move
- [ ] Move appears on BOTH devices
- [ ] Second player cannot tap until move received
- [ ] Second player can now make move
- [ ] Move appears on BOTH devices
- [ ] Continue until game ends
- [ ] Win/draw detected on BOTH devices
- [ ] Same winner shown on BOTH devices

### Reset
- [ ] Either player taps "Reset Game"
- [ ] Board clears on BOTH devices
- [ ] "Who Goes First?" dialog appears again
- [ ] Can play another round

### History
- [ ] Game appears in history on Device 1
- [ ] Game appears in history on Device 2
- [ ] Both show same result

### Connection Loss
- [ ] Move devices out of Bluetooth range
- [ ] Connection lost message appears
- [ ] App doesn't crash

## 🎨 UI/UX Verification

### Visual Design
- [ ] X symbols are blue
- [ ] O symbols are red
- [ ] Grid is clearly visible
- [ ] Text is readable
- [ ] Buttons are properly sized
- [ ] Consistent styling throughout

### Navigation
- [ ] Can navigate from main menu to all screens
- [ ] Back buttons work from all screens
- [ ] No way to get stuck

### Responsiveness
- [ ] Taps are registered immediately
- [ ] No lag during gameplay
- [ ] AI responds within reasonable time
- [ ] Smooth transitions between screens

## 🔍 Edge Cases

### Game Logic
- [ ] Test all 8 winning positions (3 rows, 3 cols, 2 diagonals)
- [ ] Test draw scenario (board full, no winner)
- [ ] Test Misere rule (3 in row = lose)

### AI Behavior
- [ ] AI doesn't take forever on Hard mode
- [ ] AI makes move even when about to lose
- [ ] Easy mode is actually random (not optimal)

### Database
- [ ] Many games don't cause issues (test 20+ games)
- [ ] Very old games still appear correctly
- [ ] Scrolling through history is smooth

### Bluetooth
- [ ] Works with different device models
- [ ] Handles connection interruption
- [ ] Works after reconnection
- [ ] Doesn't crash if Bluetooth is disabled

## 📋 Grading Rubric Items

### 1. Gameplay Functionality
- [ ] 1.1: Appropriate symbols on tap ✓
- [ ] 1.2: Cannot tap occupied spaces ✓

### 2. Difficulty Settings
- [ ] 2.1: Change difficulty without reset ✓
- [ ] 2.2: Mutually exclusive modes ✓

### 3. Difficulty Gameplay
- [ ] 3.1: Hard mode behavior ✓
- [ ] 3.2: Easy mode random ✓
- [ ] 3.3: Medium mode 50/50 ✓

### 4. Display Past Games
- [ ] 4.1: Date/time ✓
- [ ] 4.2: Winner/draw ✓
- [ ] 4.3: Difficulty mode ✓
- [ ] 4.4: Scrollable ✓
- [ ] 4.5: Persists ✓

### 5. P2P Gameplay
- [ ] Symbols on both devices ✓
- [ ] Turn enforcement ✓
- [ ] Win/loss/draw detection ✓
- [ ] State synchronization ✓
- [ ] Reset synchronization ✓

### 6. Who Goes First
- [ ] Two options ✓
- [ ] Sends message ✓

### 7. P2P History
- [ ] Games in history on both devices ✓

## 🔧 Technical Verification

### Code Quality
- [ ] No compiler errors
- [ ] No lint warnings
- [ ] Code is well-organized
- [ ] Comments explain complex logic

### Algorithm
- [ ] Minimax implemented (check MinimaxAI.kt)
- [ ] Alpha-beta pruning present
- [ ] No third-party AI libraries used
- [ ] Works for Misere variant

### Database
- [ ] Room database used
- [ ] Proper DAO pattern
- [ ] Entity has all required fields

### Bluetooth
- [ ] Direct device-to-device (no server)
- [ ] JSON message format
- [ ] Proper permission handling

### Android Best Practices
- [ ] Kotlin used throughout
- [ ] Modern architecture (Compose)
- [ ] Async operations don't block UI
- [ ] Permissions requested properly

## 🎯 Demonstration Script

### Quick Demo (5 minutes)
1. **Show Main Menu** (10 sec)
2. **Play vs Computer** (60 sec)
   - Make a few moves
   - Show "AI is thinking" on Hard mode
   - Show difficulty change without reset
3. **Show Settings** (20 sec)
   - Toggle between difficulties
4. **Show Game History** (20 sec)
   - Scroll through games
   - Point out date, winner, difficulty
5. **Show P2P Setup** (30 sec)
   - Connect two devices
   - Show "Who Goes First" dialog
6. **Play P2P Game** (60 sec)
   - Make moves on both devices
   - Show synchronization
7. **Show P2P Reset** (20 sec)
   - Reset and show it works on both
8. **Verify History on Both** (20 sec)
   - Show game appears on both devices

### Detailed Demo (15 minutes)
Follow the full checklist above, demonstrating each feature.

## 📝 Notes for Demonstration

### Key Points to Mention
1. **Misere Rules**: Emphasize inverted objective
2. **Minimax Algorithm**: Explain it's implemented from scratch
3. **Alpha-Beta Pruning**: Mention optimization technique
4. **Direct P2P**: No server, pure device-to-device
5. **Persistent Storage**: Room database for history
6. **Modern Stack**: Compose, Kotlin, Material Design 3

### Common Questions
**Q**: How does Minimax work for Misere?
**A**: Evaluation function is inverted - making 3 in a row is BAD (-10), forcing opponent to make 3 in a row is GOOD (+10).

**Q**: Can you really beat Hard mode?
**A**: Only if you play optimally. Most games end in draw.

**Q**: Does it work on emulators?
**A**: Yes, but Bluetooth is limited. Physical devices recommended for P2P.

**Q**: What happens if connection drops?
**A**: Connection lost message appears. Can return to menu and reconnect.

**Q**: How is data synchronized?
**A**: JSON messages over Bluetooth containing complete game state.

## ✅ Pre-Submission Checklist

- [ ] All code compiles without errors
- [ ] No lint warnings
- [ ] README.md is complete
- [ ] All activities work
- [ ] Tested on physical device(s)
- [ ] Tested P2P between two devices
- [ ] Database persistence verified
- [ ] All settings work
- [ ] AI difficulties work as specified
- [ ] Code is clean and commented
- [ ] No unused files in project

## 🎓 Final Check

**Before submitting, verify:**
- [ ] Project meets ALL specification requirements
- [ ] Can demonstrate all features
- [ ] Know how to explain algorithm
- [ ] Understand the code you wrote
- [ ] Ready to answer questions

---

**Project Status**: READY FOR SUBMISSION ✅

All items checked = Ready to demonstrate and submit!


