import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class GameManager {

    private int wins = 0;


    private Queue<Character> turnQueue = new LinkedList<>();


    private char[][] arena = {
            {'-', '-', '-'},
            {'-', '-', '-'},
            {'-', '-', '-'}
    };


    // Multiple players support
    private ArrayList<Character> players = new ArrayList<>();
    private int currentPlayerIndex = -1;


    private Character getCurrentPlayer() {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) return null;
        return players.get(currentPlayerIndex);
    }


    public void displayArena() {
        for (int i = 0; i < arena.length; i++) {
            for (int j = 0; j < arena[i].length; j++) {
                System.out.print(arena[i][j] + " ");
            }
            System.out.println();
        }
    }


    public void sortEnemiesByHealth(ArrayList<Enemy> enemies) {
        enemies.sort(Comparator.comparingInt(e -> e.health));
    }


    public Enemy searchEnemyByName(ArrayList<Enemy> enemies, String targetName) {
        for (Enemy e : enemies) {
            if (e.name.equalsIgnoreCase(targetName)) {
                return e;
            }
        }
        return null;
    }


    // Save to a stable folder so restarts don't "lose" files
    public void savePlayer(Character player) {
        try {
            new File("saves").mkdirs();
            FileWriter writer = new FileWriter(getSaveFileName(player.name));
            writer.write(player.getClass().getSimpleName() + "\n");
            writer.write(player.name + "\n");
            writer.write(player.health + "\n");
            writer.write(player.attackPower + "\n");
            writer.close();
            System.out.println("Game saved successfully (" + getSaveFileName(player.name) + ").");
        } catch (IOException e) {
            System.out.println("Error saving game.");
        }
    }


    // Load a player from a file based on a name (uses the same filename rules as saving)
    public Character loadPlayerByName(String playerName) {
        try {
            Scanner fileScanner = new Scanner(new File(getSaveFileName(playerName)));


            String type = fileScanner.nextLine();
            String name = fileScanner.nextLine();
            int health = Integer.parseInt(fileScanner.nextLine());
            int attackPower = Integer.parseInt(fileScanner.nextLine());


            fileScanner.close();


            Character loaded;
            if (type.equals("Mage")) {
                loaded = new Mage(name);
            } else {
                loaded = new Warrior(name);
            }


            loaded.health = health;
            loaded.attackPower = attackPower;


            if (loaded.health <= 0) {
                loaded.health = 0;
                System.out.println("Warning: This character was defeated and has 0 HP.");
            }


            return loaded;


        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    private String getSaveFileName(String playerName) {
        String safe = playerName.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
        if (safe.isEmpty()) safe = "Player";
        return "saves/save_" + safe + ".txt";
    }


    // NEW FIX: on restart, automatically load any saves into the players list
    private void loadAllSavedPlayers() {
        new File("saves").mkdirs();
        File folder = new File("saves");
        File[] files = folder.listFiles();


        if (files == null) return;


        for (File f : files) {
            String fname = f.getName();
            if (!fname.startsWith("save_") || !fname.endsWith(".txt")) continue;


            // Reconstruct the "playerName key" used by loadPlayerByName:
            // save_<SAFE>.txt  -> SAFE (we can pass SAFE back in)
            String key = fname.substring("save_".length(), fname.length() - ".txt".length());


            Character loaded = loadPlayerByName(key);
            if (loaded != null) {
                // Avoid duplicates in the list by exact name match
                boolean exists = false;
                for (Character p : players) {
                    if (p.name.equalsIgnoreCase(loaded.name)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    players.add(loaded);
                }
            }
        }
    }


    public void createPlayer(Scanner scanner) {
        System.out.println("\n=== CREATE CHARACTER ===");
        System.out.print("Name your character: ");
        String firstName = scanner.nextLine().trim();
        if (firstName.isEmpty()) firstName = "Player";


        System.out.print("""
               1) Warrior (HP 100, ATK 15)
               2) Mage (HP 80, ATK 20)
               Choose class:\s""");
        String choice = scanner.nextLine().trim();


        Character newPlayer;
        if (choice.equals("2")) {
            newPlayer = new Mage(firstName + " The Mage");
        } else {
            newPlayer = new Warrior(firstName + " The Warrior");
        }


        players.add(newPlayer);
        currentPlayerIndex = players.size() - 1;


        System.out.println("Your new character: " + newPlayer.name +
                " (HP: " + newPlayer.health + ", ATK: " + newPlayer.attackPower + ")");
        System.out.println("Current player set to: " + newPlayer.name);
    }


    public void listPlayers() {
        if (players.isEmpty()) {
            System.out.println("No players created yet.");
            return;
        }
        System.out.println("\n=== PLAYERS ===");
        for (int i = 0; i < players.size(); i++) {
            Character p = players.get(i);
            String marker = (i == currentPlayerIndex) ? " <== current" : "";
            System.out.println((i + 1) + ") " + p.name + " (HP: " + p.health + ", ATK: " + p.attackPower + ")" + marker);
        }
    }


    // CHANGED (the fix): if no players exist, auto-load saved players first
    public void choosePlayer(Scanner scanner) {
        if (players.isEmpty()) {
            loadAllSavedPlayers();
        }


        if (players.isEmpty()) {
            System.out.println("No players created yet.");
            return;
        }


        listPlayers();
        System.out.print("Choose player number: ");
        String input = scanner.nextLine().trim();


        try {
            int pick = Integer.parseInt(input);
            if (pick < 1 || pick > players.size()) {
                System.out.println("Invalid player number.");
                return;
            }
            currentPlayerIndex = pick - 1;
            System.out.println("Current player set to: " + getCurrentPlayer().name);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }


    public void countdown(int n) {
        if (n <= 0) {
            System.out.println("FIGHT!");
            return;
        }
        System.out.println(n + "...");
        countdown(n - 1);
    }


    private int randInt(int min, int max) {
        return (int)(Math.random() * (max - min + 1)) + min;
    }


    public void startBattle(Scanner scanner) {
        System.out.println("\n=== BATTLE ===");


        Character player = getCurrentPlayer();


        if (player == null) {
            System.out.println("You must create a player first.");
            return;
        }


        if (player.health <= 0) {
            System.out.println("You are defeated. Choose/create a new player to battle again.");
            return;
        }


        ArrayList<Enemy> enemies = new ArrayList<>();


        int bonusHP = wins * 5;
        int bonusATK = wins * 2;


        enemies.add(new Enemy("Goblin", 40 + bonusHP, randInt(10, 20) + bonusATK));
        enemies.add(new Enemy("Orc", 60 + bonusHP, randInt(12, 20) + bonusATK));


        sortEnemiesByHealth(enemies);


        Enemy enemy = enemies.get(randInt(0, enemies.size() - 1));


        Enemy confirm = searchEnemyByName(enemies, enemy.name);
        if (confirm == null) {
            System.out.println("Error: enemy selection failed.");
            return;
        }


        System.out.println("A wild " + enemy.name + " appears! (HP: " + enemy.health + ", ATK: " + enemy.attackPower + ")");


        System.out.println("\n=== ARENA ===");
        displayArena();


        System.out.println("\n=== BATTLE START ===");
        countdown(3);


        while (player.health > 0 && enemy.health > 0) {
            System.out.println("\nYour HP: " + player.health + " | Enemy HP: " + enemy.health);
            System.out.println("1) Attack");
            System.out.println("2) Special");
            System.out.println("3) Run");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();


            if (choice.equals("3")) {
                System.out.println("You ran away!");
                return;
            }


            if (!choice.equals("1") && !choice.equals("2")) {
                System.out.println("Invalid choice.");
                continue;
            }


            turnQueue.clear();
            turnQueue.add(player);
            turnQueue.add(enemy);


            while (!turnQueue.isEmpty() && player.health > 0 && enemy.health > 0) {
                Character current = turnQueue.poll();


                if (current == player) {
                    if (choice.equals("1")) {
                        enemy.health -= player.attackPower;
                        System.out.println("You hit the " + enemy.name + " for " + player.attackPower + " damage!");
                    } else {
                        player.specialMove(enemy);
                    }


                    if (enemy.health <= 0) {
                        System.out.println("You defeated the " + enemy.name + "!");
                        wins++;


                        int heal = 10;
                        player.health += heal;
                        System.out.println("You recover " + heal + " HP after the battle. Current HP: " + player.health);
                        return;
                    }
                } else {
                    int enemyDamage = randInt(1, enemy.attackPower);
                    player.health -= enemyDamage;
                    System.out.println(enemy.name + " hits you for " + enemyDamage + " damage!");


                    if (player.health <= 0) {
                        System.out.println("You died!");
                        return;
                    }
                }
            }
        }
    }


    public void run() {
        Scanner scanner = new Scanner(System.in);


        while (true) {
            Character current = getCurrentPlayer();


            System.out.println("\n=== MAIN MENU ===");
            if (current != null) {
                System.out.println("Current Player: " + current.name + " (HP: " + current.health + ", ATK: " + current.attackPower + ")");
            } else {
                System.out.println("Current Player: (none)");
            }


            System.out.print("""
                   1) Create player
                   2) Choose player
                   3) Start battle
                   4) Save current player
                   5) Quit
                   Choose:\s""");


            String choice = scanner.nextLine().trim();


            switch (choice) {
                case "1":
                    createPlayer(scanner);
                    break;
                case "2":
                    choosePlayer(scanner); // now auto-loads saves if needed
                    break;
                case "3":
                    startBattle(scanner);
                    break;
                case "4": {
                    Character p = getCurrentPlayer();
                    if (p == null) {
                        System.out.println("No player to save.");
                    } else {
                        savePlayer(p);
                    }
                    break;
                }
                case "5":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }
}