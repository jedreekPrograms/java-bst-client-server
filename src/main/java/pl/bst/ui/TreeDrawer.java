package pl.bst.ui;

import java.util.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import pl.bst.model.TreeNode;

public final class TreeDrawer {
    private record Placement(TreeNode<?> node, int depth) { }
    private final Pane pane;
    public TreeDrawer(Pane pane) { this.pane = pane; }
    public void draw(TreeNode<?> root) {
        pane.getChildren().clear(); pane.setPrefSize(600, 300);
        if (root == null) return;
        Map<TreeNode<?>,Double> x = new IdentityHashMap<>();
        Deque<TreeNode<?>> stack = new ArrayDeque<>(); TreeNode<?> current = root;
        while (current != null || !stack.isEmpty()) {
            while (current != null) { stack.push(current); current = current.left(); }
            current = stack.pop(); x.put(current, 40.0 + x.size() * 75); current = current.right();
        }
        pane.setPrefWidth(Math.max(600, x.size() * 75 + 80));
        Deque<Placement> pending = new ArrayDeque<>(); pending.push(new Placement(root,0));
        int maximumDepth = 0;
        while (!pending.isEmpty()) {
            Placement place = pending.pop(); TreeNode<?> node = place.node();
            maximumDepth = Math.max(maximumDepth, place.depth());
            double nx = x.get(node), ny = 40 + place.depth() * 85;
            if (node.left() != null) { edge(nx,ny,x.get(node.left()),ny+85); pending.push(new Placement(node.left(),place.depth()+1)); }
            if (node.right() != null) { edge(nx,ny,x.get(node.right()),ny+85); pending.push(new Placement(node.right(),place.depth()+1)); }
            Circle circle = new Circle(25,Color.web("#a8d0e6")); circle.setStroke(Color.web("#374785"));
            String label = node.value().toString(); Text text = new Text(label.length()>8 ? label.substring(0,7)+"…" : label);
            StackPane view = new StackPane(circle,text); view.setLayoutX(nx-25); view.setLayoutY(ny-25);
            view.setPrefSize(50,50); Tooltip.install(view,new Tooltip(label)); pane.getChildren().add(view);
        }
        pane.setPrefHeight(Math.max(300, maximumDepth*85+100));
    }
    private void edge(double x,double y,double tx,double ty) {
        Line line = new Line(x,y+25,tx,ty-25); line.setStroke(Color.web("#557a95")); pane.getChildren().add(line);
    }
}
