import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;


public class TriestBase implements DataStreamAlgo {

    /*
     * Constructor.
     * The parameter samsize denotes the size of the sample, i.e., the number of
     * edges that the algorithm can store.
     */
    private int t;
    private int size;
    private int estimate;

    //count the number of triangles in the sample
    private int triCount;

    //adjList stores an adjacency list used to represent the graph (of sample edges)
    private Hashtable<Integer, HashSet<Integer>> adjList;

    //store the edges in the sample
    private ArrayList<Edge> edgeList;

    public TriestBase(int samsize) {
        t=0;
        size = samsize;
        estimate = 0;
        triCount = 0;
        edgeList = new ArrayList<Edge>();
        adjList = new Hashtable<Integer, HashSet<Integer>>();
    }

    public void handleEdge(Edge edge) {
        t++;
        if (t<=size){
            edgeList.add(edge);
            adjAdd(edge.u,edge.v);
            updateTriangles(edge.u,edge.v,true);
        }
        //For t>= sample size - then flip biased coin to determine whether to add edge to sample
        else if (coinFlip()){
            //choose a random edge in the sample to replace
            int len = edgeList.size();
            Random random = new Random();
            int index = random.nextInt(len);
            Edge delete = edgeList.remove(index);

            //decrease TriCount by the number of triangles involving the removed edge
            updateTriangles(delete.u,delete.v,false);
            adjDelete(delete.u,delete.v);

            //add the current edge to sample
            edgeList.add(edge);
            adjAdd(edge.u,edge.v);

            //increase TriCount by the number of triangles involving the current edge
            updateTriangles(edge.u,edge.v,true);

        }

    }

    //to get the estimate- divide triCount by the weight
    public int getEstimate() {
        if(t<size) return triCount;

        double pi = pi(t,size);

        return (int) (triCount/pi);
    }

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

    //remove an edge from the adjacency list
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

    //compute the weight
    private double pi(int t, int m){
        double d1 = (double)m/t;
        double d2 = (double)(m-1)/(t-1);
        double d3 = (double)(m-2)/(t-2);

        return d1*d2*d3;
    }

    /*Find the result of adding/removing an edge u,v
    Find the number of triangles involving edge u,v
    If Boolean Addition = True, then increase TriCount by that number
    If Boolean Addition= False, then decrease TriCount by that number
    */

    private void updateTriangles(int u, int v,Boolean Addition){
        HashSet<Integer> intersection = new HashSet<Integer>();
        HashSet<Integer> u_adj = adjList.get(u);
        HashSet<Integer> v_adj = adjList.get(v);

        intersection.addAll(u_adj);
        intersection.retainAll(v_adj);

        int numTriangles = intersection.size();

        if(!Addition) numTriangles*=-1;

        triCount+=numTriangles;
    }

}
