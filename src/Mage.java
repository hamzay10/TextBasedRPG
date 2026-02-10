class Mage extends Character {
    public Mage(String name) {
        super(name, 80, 20);
    }

    @Override
    public void attack() {
        System.out.println(name + " casts a spell for " + attackPower + " damage!");
    }

    @Override
    public void specialMove(Character target) {
        int dmg = attackPower + 5;
        target.health -= dmg;
        System.out.println(name + " uses Fireball for " + dmg + " damage!");
    }
}