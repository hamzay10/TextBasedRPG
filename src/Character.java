abstract class Character {
    protected String name;
    protected int health;
    protected int attackPower;


    public Character(String name, int health, int attackPower) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
    }

    public abstract void attack();
    public abstract void specialMove(Character target);
}