package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class the deals with Navigation. Is called by MyView to do work on multiple nodes.
 */
public class Navigator {
    private Node startNode, endNode;
    private ArrayList<Node> graph;
    private ArrayList<CanvasDrawable> path;


    //todo: make static?
    public Navigator(Node startNode, Node endNode, ArrayList<Node> graph) {
        setStartNode(startNode);
        setEndNode(endNode);
    }

    public Navigator(ArrayList<Node> graph) {
        this.graph = graph;
    }


    //todo: method to set graph?

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public ArrayList<CanvasDrawable> getPath() {
        return path;
    }

    public void calculatePath() {
        //TreeMap<Node, Integer> guess_score = new TreeMap<Node, Integer>();
        HashMap<CanvasDrawable, CanvasDrawable> cameFrom = new HashMap<>();
        HashMap<Node, Integer> traveled_distance = new HashMap<>();
        HashMap<Node, Integer> estimated_distance = new HashMap<>();

        ArrayList<Node> openNodes = new ArrayList<>();
        openNodes.add(startNode);
        ArrayList<Node> closedNodes = new ArrayList<>();

        //add all nodes into openNodes queue, set the start to zero, everything else to MaxValue
        for (Node node : graph) {
            if (node.equals(startNode)) {
                traveled_distance.put(node, 0);
                estimated_distance.put(node, getHeuristicDistance(node));
            } else {
                traveled_distance.put(node, Integer.MAX_VALUE);
                estimated_distance.put(node, Integer.MAX_VALUE);
            }
        }
        //loop though openNodes

        Node curNode = null, neighborNode;
        while (openNodes.size() != 0) {

            int lowest = Integer.MAX_VALUE;
            for (Node node : openNodes) {
                if (estimated_distance.get(node) < lowest) {
                    lowest = estimated_distance.get(node);
                    curNode = node;
                }
            }

            if (endNode.equals(curNode)) {
                path = new ArrayList<>();
                CanvasDrawable curDrawable = endNode;
                while (curDrawable != startNode) {
                    path.add(curDrawable);
                    curDrawable = cameFrom.get(curDrawable);
                }
                path.add(startNode);
                return;
            }
            openNodes.remove(curNode);
            closedNodes.add(curNode);

            for (Edge edge : curNode.getEdges()) {
                //edge are bidirectional, guess that neighbor is the end node on this edge
                neighborNode = edge.getEnd();
                //if we guessed wrong, and neighbor was set to curNode, choose the start Node on this edge.
                if (curNode.equals(neighborNode)) {
                    neighborNode = edge.getStart();
                }

                if (closedNodes.contains(neighborNode)) {
                    continue;
                }
                //neighborNode is still valid
                //tentDistance is how far to get to curNode + how far between cur and neighbor
                int tentativeDistance = traveled_distance.get(curNode) + edge.getWeight();
                if (!openNodes.contains(neighborNode)) {
                    openNodes.add(neighborNode);
                } else if (tentativeDistance >= traveled_distance.get(neighborNode)) {
                    //not a better path
                    continue;
                }
                //is a better path,

                //update actual
                traveled_distance.remove(neighborNode);
                traveled_distance.put(neighborNode, tentativeDistance);
                //updated estimate
                estimated_distance.remove(neighborNode);
                estimated_distance.put(neighborNode, tentativeDistance + getHeuristicDistance(neighborNode));
                //update cameFrom. The edge to curNode, AND, the neighbor to Edge.
                cameFrom.remove(neighborNode);
                cameFrom.put(edge, curNode);
                cameFrom.put(neighborNode, edge);
            }


        }
        //failure to find a path.
        path = null;

    }

    private Integer getHeuristicDistance(Node node) {
        //using manhattan distance as heuristic;
        return Math.abs(endNode.getxPos() - node.getxPos()) + Math.abs(endNode.getyPos() - node.getyPos());
    }

}
