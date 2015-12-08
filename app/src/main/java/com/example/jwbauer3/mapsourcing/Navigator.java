package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class the deals with Navigation. Is called by MyView to do work on multiple nodes.
 */
public class Navigator {
    private BaseNode startNode, endNode;
    private ArrayList<BaseNode> graph;
    private ArrayList<CanvasDrawable> path;


    //todo: make static?
    public Navigator(BaseNode startNode, BaseNode endNode, ArrayList<BaseNode> graph) {
        setStartNode(startNode);
        setEndNode(endNode);
    }

    public Navigator(ArrayList<BaseNode> graph) {
        this.graph = graph;
    }


    //todo: method to set graph?

    public void setStartNode(BaseNode startNode) {
        //get rid of old start node, IFF its a location node.
        if(this.startNode != null && this.startNode instanceof LocationNode){
            graph.remove(this.startNode);
        }
        //if new start node is a location node, add it to the graph
        if(startNode instanceof LocationNode){
            graph.add(startNode);
        }
        //in either case, overwrite what start node refers to
        this.startNode = startNode;

    }

    public void setEndNode(BaseNode endNode) {
        //get rid of old start node, IFF its a location node.
        if(this.endNode != null && this.endNode instanceof LocationNode){
            graph.remove(this.endNode);
        }
        //if new start node is a location node, add it to the graph
        if(endNode instanceof LocationNode){
            graph.add(endNode);
        }
        //in either case, overwrite what end node refers to
        this.endNode = endNode;
    }

    public ArrayList<CanvasDrawable> getPath() {
        return path;
    }

    public void calculatePath() {
        //TreeMap<Node, Integer> guess_score = new TreeMap<Node, Integer>();
        HashMap<CanvasDrawable, CanvasDrawable> cameFrom = new HashMap<>();
        HashMap<BaseNode, Integer> traveled_distance = new HashMap<>();
        HashMap<BaseNode, Integer> estimated_distance = new HashMap<>();

        ArrayList<BaseNode> openNodes = new ArrayList<>();
        openNodes.add(startNode);
        ArrayList<BaseNode> closedNodes = new ArrayList<>();

        //add all nodes into openNodes queue, set the start to zero, everything else to MaxValue
        for (BaseNode node : graph) {
            if (node.equals(startNode)) {
                traveled_distance.put(node, 0);
                estimated_distance.put(node, getHeuristicDistance(node));
            } else {
                traveled_distance.put(node, Integer.MAX_VALUE);
                estimated_distance.put(node, Integer.MAX_VALUE);
            }
        }
        //loop though openNodes

        BaseNode curNode = null, neighborNode;
        while (openNodes.size() != 0) {

            int lowest = Integer.MAX_VALUE;
            for (BaseNode node : openNodes) {
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
                //if we guessed wrong, and neighbor was set to curNode, choose the start BaseNode on this edge.
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

    private Integer getHeuristicDistance(BaseNode node) {
        //using manhattan distance as heuristic;
        return Math.abs(endNode.getxPos() - node.getxPos()) + Math.abs(endNode.getyPos() - node.getyPos());
    }

}
