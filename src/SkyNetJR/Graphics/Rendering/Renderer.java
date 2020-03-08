package SkyNetJR.Graphics.Rendering;

public abstract class Renderer {
    protected int positionX;
    protected int positionY;

    public abstract void Render(int offsetX, int offsetY);

    public abstract void Destroy();

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
}
