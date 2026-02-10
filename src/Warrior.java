class Warrior extends Character {
    public Warrior(String name) {
        super(name, 100, 15);
    }

    @Override
    public void attack() {
        System.out.println(name + " swings a sword for " + attackPower + " damage!");
    }

    @Override
    public void specialMove(Character target) {
        int dmg = attackPower + 10;
        target.health -= dmg;
        System.out.println(name + " uses Power Strike for " + dmg + " damage!");
    }
}