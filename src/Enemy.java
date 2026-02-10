class Enemy extends Character {
    public Enemy(String name, int health, int attackPower) {
        super(name, health, attackPower);
    }

    @Override
    public void attack() {
        System.out.println(name + " attacks for " + attackPower + " damage!");
    }

    @Override
    public void specialMove(Character target) {
        // kept simple
        target.health -= attackPower;
        System.out.println(name + " attacks for " + attackPower + " damage!");
    }
}