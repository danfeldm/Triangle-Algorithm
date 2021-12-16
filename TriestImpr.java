import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

public class TriestImpr implements DataStreamAlgo {
    /*
     * Constructor.
     * The parameter samsize denotes the size of the sample, i.e., the number of
     * edges that the algorithm can store.
     */
    private int t;
    private int size;
    private double estimate;

    //adjList stores an adjacency list used to represent the graph (of sample edges)
    private Hashtable<Integer, HashSet<Integer>> adjList;

    //store the edges in the sample
    private ArrayList<Edge> edgeList;


    public TriestImpr(int samsize) {
        t=0;
        size = samsize;
        estimate = 0;
        edgeList = new ArrayList<Edge>();
        adjList = new Hashtable<Integer, HashSet<Integer>>();
    }

    public void handleEdge(Edge edge) {
        t++;

        //count the number triangles created by adding the new edge to the graph
        int newTriangles = updateTriangles(edge.u, edge.v);

        //increase the triangle estimate
        double ratio = (double)(t-1)*(t-2)/(size*(size-1));
        double increment = ratio*newTriangles;
        estimate += increment;


       /* if t<= size - then there will be always be space to add the new edge to the sample
       * if t>size then flip a biased to coin to determine whether to add the edge to sample
       * --> then in that case we must remove an edge from the sample to make space for the new edge
       */
        Boolean flip = coinFlip();
        if(t>size && flip){
            int len = edgeList.size();
            Random random = new Random();
            int index = random.nextInt(len);
            Edge delete = edgeList.remove(index);
            adjDelete(delete.u,delete.v);
        }
        if(t<=size || flip) {
            adjAdd(edge.u, edge.v);
            edgeList.add(edge);
        }
    }

    public int getEstimate() { return (int)estimate; }

    private Boolean coinFlip(){
        double bias = (double)size/t;
        return Math.random() < bias;
    }

    //add an edge to the adjacency list
    private void adjAdd(int u, int v){
        HashSet<Integer> u_adj= new HashSet<Integer>();
        HashSet<Integer> v_adj= new HashSet<Integer>();
        u_adj.add(v);
        v_adj.add(u);
        if(adjList.containsKey(u)){
            HashSet<Integer> h = adjList.get(u);
            u_adj.addAll(h);
        }
        if(adjList.containsKey(v)){
            HashSet<Integer> h = adjList.get(v);
            v_adj.addAll(h);
        }
        adjList.put(u,u_adj);
        adjList.put(v,v_adj);
    }

    //delete an edge from the adjacency list
    private void adjDelete(int u, int v){
        HashSet<Integer> u_adj = adjList.get(u);
        HashSet<Integer> v_adj = adjList.get(v);

        u_adj.remove(v);
        v_adj.remove(u);

        if (u_adj.isEmpty()){
            adjList.remove(u);
        }
        else{
            adjList.put(u,u_adj);
        }

        if (v_adj.isEmpty()){
            adjList.remove(v);
        }
        else{
            adjList.put(v,v_adj);
        }
    }

    //returns the number of triangles involving the edge u,v
    private int updateTriangles(int u, int v){
        if (!adjList.containsKey(u) || !adjList.containsKey(v)) return 0;

        HashSet<Integer> intersection = new HashSet<Integer>();
        HashSet<Integer> u_adj = adjList.get(u);
        HashSet<Integer> v_adj = adjList.get(v);

        intersection.addAll(u_adj);
        intersection.retainAll(v_adj);

        int numTriangles = intersection.size();
        return numTriangles;
    }

}
