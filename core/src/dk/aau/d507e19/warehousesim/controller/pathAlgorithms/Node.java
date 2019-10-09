package dk.aau.d507e19.warehousesim.controller.pathAlgorithms;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
    private T data;
    private Node<T> parent;
    private List<Node<T>> children = new ArrayList<>();

    public Node(T data, Node<T> parent) {
        this.data = data;
        this.parent = parent;
    }

    public void setParent(Node<T> parent) {
        if(this.getParent()!=null){
            this.getParent().removeChild(this);
        }
        this.parent = parent;
    }

    public void addChild(Node<T> child){
        children.add(child);
        child.setParent(this);
    }

    public Node<T> getParent() {
        if(parent==null){
            return null;
        }
        return parent;
    }

    public void removeChild(Node<T> child){
        if(children.contains(child)){
            children.remove(child);
        }
    }
    public List<Node<T>> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void printTree(Node<T> node){
        System.out.println(node.getData().toString());
        for(Node<T> n : node.getChildren()){
            printTree(n);
        }
    }
}
