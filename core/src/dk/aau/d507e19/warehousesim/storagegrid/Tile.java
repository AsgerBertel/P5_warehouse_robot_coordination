package dk.aau.d507e19.warehousesim.storagegrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import dk.aau.d507e19.warehousesim.Drawable;
import dk.aau.d507e19.warehousesim.GraphicsManager;
import dk.aau.d507e19.warehousesim.Position;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.Node;
import dk.aau.d507e19.warehousesim.controller.robot.GridCoordinate;

import java.util.Objects;

public class Tile implements Drawable {

    private static final float GRID_LINE_WIDTH = 0.1f;

    private int posX, posY;
    private GridCoordinate gridCoordinate;
    private Color color;
    public static final int TILE_SIZE = 1;

    public static final Color defaultCenterColor = Color.GRAY;
    public static final Color defaultEdgeColor = Color.BLACK;
    public static final Color overlayColor = new Color(28f / 255f, 217f / 255f, 56f / 255f, 0.2f);
    public static final Color overlayColor2 = new Color(28f / 255f, 56f / 255f, 217f / 255f, 0.2f);

    public Tile(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.gridCoordinate = new GridCoordinate(posX, posY);
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        renderCenterAndBorder(shapeRenderer, defaultCenterColor);
    }

    public void renderCenterAndBorder(ShapeRenderer shapeRenderer, Color centerColor) {
        renderFilledCenter(shapeRenderer, centerColor);
        renderBorder(shapeRenderer, defaultEdgeColor);
    }

    private void renderBorder(ShapeRenderer shapeRenderer, Color borderColor){
        shapeRenderer.setColor(borderColor);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.rect(posX, posY, TILE_SIZE, TILE_SIZE);
        shapeRenderer.end();
    }

    private void renderFilledCenter(ShapeRenderer shapeRenderer, Color color){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        shapeRenderer.setColor(color);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(posX, posY, TILE_SIZE, TILE_SIZE);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void renderOverlay(ShapeRenderer shapeRenderer){
        renderOverlay(shapeRenderer, overlayColor);
    }

    public void renderOverlay(ShapeRenderer shapeRenderer, Color color){
        renderFilledCenter(shapeRenderer, color);
    }

    public void renderTreeNode(Node<GridCoordinate> node, ShapeRenderer shapeRenderer, Color color){
        Vector2 start = new Vector2(posX + 0.5f*TILE_SIZE,posY+ 0.5f*TILE_SIZE);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(color);
        if(node.getParent()== null){
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.circle(start.x,start.y,0.3f);
        }else{
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Vector2 end = new Vector2(
                    node.getParent().getData().getX()+0.5f*TILE_SIZE,
                    node.getParent().getData().getY()+0.5f*TILE_SIZE);

            shapeRenderer.line(start,end);
        }
        shapeRenderer.end();
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public boolean collidesWith(Position collider){
        boolean withInXBounds = collider.getX() >= getPosX()
                && collider.getX() <= getPosX() + TILE_SIZE;
        boolean withInYBounds = collider.getY() >= getPosY()
                && collider.getY() <= getPosY() + TILE_SIZE;
        return withInXBounds && withInYBounds;
    }

    public GridCoordinate getGridCoordinate(){
        return gridCoordinate;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "posX=" + posX +
                ", posY=" + posY +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return posX == tile.posX &&
                posY == tile.posY &&
                Objects.equals(gridCoordinate, tile.gridCoordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY, gridCoordinate);
    }
}
