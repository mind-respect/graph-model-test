package org.triple_brain.module.model.graph;

import org.junit.Ignore;
import org.junit.Test;
import org.triple_brain.module.model.ExternalFriendlyResource;
import org.triple_brain.module.model.ModelTestScenarios;
import org.triple_brain.module.model.graph.exceptions.InvalidDepthOfSubVerticesException;
import org.triple_brain.module.model.graph.exceptions.NonExistingResourceException;
import org.triple_brain.module.model.suggestion.PersistedSuggestion;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

/*
* Copyright Mozilla Public License 1.1
*/
public class UserGraphTest extends AdaptableGraphComponentTest {
    public static final int DEPTH_OF_SUB_VERTICES_COVERING_ALL_GRAPH_VERTICES = 10;
    @Test
    public void can_get_graph_with_default_center_vertex() {
        SubGraph graph = userGraph.graphWithDefaultVertexAndDepth(
                DEPTH_OF_SUB_VERTICES_COVERING_ALL_GRAPH_VERTICES
        );
        assertThat(graph, is(not(nullValue())));
        assertThat(graph.numberOfVertices(), is(3));
        assertThat(graph.numberOfEdges(), is(2));
        assertTrue(graph.containsVertex(vertexA));
    }

    @Test
    public void can_get_graph_with_custom_center_vertex() {
        SubGraph graph = userGraph.graphWithDepthAndCenterVertexId(
                DEPTH_OF_SUB_VERTICES_COVERING_ALL_GRAPH_VERTICES,
                vertexB.id());
        assertThat(graph, is(not(nullValue())));
        Vertex centerVertex = graph.vertexWithIdentifier(vertexB.id());
        assertThat(graph.numberOfEdges(), is(2));
        assertThat(graph.numberOfVertices(), is(3));
        assertThat(centerVertex.label(), is("vertex B"));
    }

    @Test
    public void can_get_circular_graph_with_default_center_vertex() {
        vertexC.addRelationToVertex(vertexA);
        SubGraph graph = userGraph.graphWithDefaultVertexAndDepth(DEPTH_OF_SUB_VERTICES_COVERING_ALL_GRAPH_VERTICES);
        assertThat(graph, is(not(nullValue())));
        assertThat(graph.numberOfEdgesAndVertices(), is(numberOfEdgesAndVertices()));
        Vertex centerVertex = graph.vertexWithIdentifier(vertexA.id());
        assertThat(centerVertex.label(), is("vertex A"));
    }

    @Test
    public void can_get_a_limited_graph_with_default_center_vertex() throws Exception {
        SubGraph subGraph = userGraph.graphWithDefaultVertexAndDepth(2);
        assertThat(subGraph.numberOfEdges(), is(2));
        assertThat(subGraph.numberOfVertices(), is(3));
        assertTrue(subGraph.containsVertex(vertexA));

        subGraph = userGraph.graphWithDefaultVertexAndDepth(1);
        assertThat(subGraph.numberOfEdges(), is(1));
        assertThat(subGraph.numberOfVertices(), is(2));
        assertFalse(subGraph.containsVertex(vertexC));
        assertTrue(subGraph.containsVertex(vertexA));
    }

    @Test

    public void can_get_a_limited_graph_with_a_custom_center_vertex() {
        SubGraph subGraph = userGraph.graphWithDepthAndCenterVertexId(
                1,
                vertexC.id()
        );
        assertThat(subGraph.numberOfVertices(), is(2));
        assertThat(subGraph.numberOfEdges(), is(1));
        assertFalse(subGraph.containsVertex(vertexA));
        assertTrue(subGraph.containsVertex(vertexC));
    }

    @Test

    public void can_get_sub_graph_of_destination_vertex_of_center_vertex() {
        Edge newEdge = vertexC.addVertexAndRelation();
        SubGraph subGraph = userGraph.graphWithDepthAndCenterVertexId(
                2, vertexB.id()
        );
        assertThat(subGraph.numberOfEdges(), is(3));
        assertThat(subGraph.numberOfVertices(), is(4));

        assertTrue(subGraph.containsVertex(vertexA));
        assertTrue(subGraph.containsVertex(vertexB));
        assertTrue(subGraph.containsVertex(vertexC));
        assertTrue(subGraph.containsVertex(newEdge.destinationVertex()));
    }

    @Test
    public void can_get_sub_graph_of_source_vertex_of_center_vertex() {
        SubGraph subGraph;
        Edge newEdge = vertexA.addVertexAndRelation();
        subGraph = userGraph.graphWithDepthAndCenterVertexId(
                2, vertexB.id()
        );
        assertThat(subGraph.numberOfVertices(), is(4));
        assertThat(subGraph.numberOfEdges(), is(3));

        assertTrue(subGraph.containsVertex(newEdge.destinationVertex()));
        assertTrue(subGraph.containsVertex(vertexA));
        assertTrue(subGraph.containsVertex(vertexB));
        assertTrue(subGraph.containsVertex(vertexC));
    }

    @Test
    public void can_get_sub_graph_of_source_vertex_of_center_vertex_having_also_a_circular_relation() {
        vertexC.addRelationToVertex(vertexA);
        Edge edgeGoingOutOfC = vertexC.addVertexAndRelation();

        SubGraph subGraph = userGraph.graphWithDepthAndCenterVertexId(
                2,
                vertexA.id()
        );
        assertTrue(subGraph.containsVertex(edgeGoingOutOfC.destinationVertex()));
    }

    @Test
    public void with_a_depth_of_sub_vertices_of_zero_only_central_vertex_is_returned() {
        SubGraph subGraph = userGraph.graphWithDefaultVertexAndDepth(0);
        assertThat(subGraph.numberOfVertices(), is(1));
        assertThat(subGraph.numberOfEdges(), is(0));
        assertTrue(subGraph.containsVertex(vertexA));

        subGraph = userGraph.graphWithDepthAndCenterVertexId(
                0, vertexB.id()
        );
        assertThat(subGraph.numberOfVertices(), is(1));
        assertThat(subGraph.numberOfEdges(), is(0));
        assertTrue(subGraph.containsVertex(vertexB));
    }

    @Test
    public void an_exception_is_thrown_when_getting_graph_with_default_center_vertex_with_negative_depth() {
        try {
            userGraph.graphWithDefaultVertexAndDepth(-1);
            fail();
        } catch (InvalidDepthOfSubVerticesException e) {
            assertThat(e.getMessage(), is("Invalid depth of sub vertices. Depth was:-1 and center vertex id was:" + vertexA.id()));
        }
    }

    @Test
    public void an_exception_is_thrown_when_getting_graph_with_custom_center_vertex_with_negative_depth() {
        try {
            userGraph.graphWithDepthAndCenterVertexId(-1, vertexB.id());
            fail();
        } catch (InvalidDepthOfSubVerticesException e) {
            assertThat(e.getMessage(), is("Invalid depth of sub vertices. Depth was:-1 and center vertex id was:" + vertexB.id()));
        }
    }

    @Test
    public void an_exception_is_thrown_when_getting_graph_with_non_existing_center_vertex() {
        Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
        try {
            userGraph.graphWithDepthAndCenterVertexId(1, "invalid_URI");
            fail();
        } catch (NonExistingResourceException e) {
            assertThat(e.getMessage(), is("Resource with URI: invalid_URI not found"));
        }
        assertThat(numberOfEdgesAndVertices(), is(numberOfEdgesAndVertices));
    }

    @Test
    public void frontier_vertices_with_hidden_vertices_have_a_list_of_their_hidden_properties_name() {
        Edge newEdge = vertexB.addVertexAndRelation();
        newEdge.label("new edge");
        SubGraph subGraph = userGraph.graphWithDepthAndCenterVertexId(
                1,
                vertexA.id()
        );
        List<String> vertexBConnectedEdgesLabel = subGraph.vertexWithIdentifier(vertexB.id())
                .hiddenConnectedEdgesLabel();
        assertFalse(vertexBConnectedEdgesLabel.isEmpty());
        assertThat(vertexBConnectedEdgesLabel.size(), is(2));
        assertTrue(vertexBConnectedEdgesLabel.contains("between vertex B and vertex C"));
        assertTrue(vertexBConnectedEdgesLabel.contains("new edge"));
    }

    @Test
    public void connected_frontier_vertices_have_their_edge() {
        Edge newEdge = vertexC.addRelationToVertex(vertexA);
        newEdge.label("edge between frontier vertices");
        SubGraph subGraph = userGraph.graphWithDepthAndCenterVertexId(
                1,
                vertexB.id()
        );
        List<String> vertexCConnectedEdgesLabel = subGraph.vertexWithIdentifier(
                vertexC.id()
        ).hiddenConnectedEdgesLabel();
        assertThat(vertexCConnectedEdgesLabel.size(), is(0));
        Vertex vertexCFromSubGraph = subGraph.vertexWithIdentifier(
                vertexC.id()
        );
        assertThat(vertexCFromSubGraph.connectedEdges().size(), is(2));
    }

    @Test
    @Ignore("to implement later, not a priority")
    public void can_get_rdf_xml_representation_of_graph() {
        assertThat(userGraph.toRdfXml(), is(not(nullValue())));
    }

    @Test
    public void vertex_additional_type_label_is_in_sub_graph(){
        assertTrue(
                vertexA.getAdditionalTypes().isEmpty()
        );
        vertexA.addType(
                ModelTestScenarios.personType()
        );
        SubGraph subGraph = wholeGraph();
        vertexA = subGraph.vertexWithIdentifier(vertexA.id());
        ExternalFriendlyResource additionalType = vertexA.getAdditionalTypes().iterator().next();
        assertThat(additionalType.label(), is("Person"));
    }

    @Test
    public void vertex_suggestions_have_their_properties_sub_graph(){
        vertexA.addSuggestions(
                ModelTestScenarios.startDateSuggestion()
        );
        SubGraph subGraph = wholeGraph();
        vertexA = subGraph.vertexWithIdentifier(vertexA.id());
        PersistedSuggestion suggestion = vertexA.suggestions().iterator().next();
        assertThat(suggestion.get().label(), is("Start date"));
    }

    @Test
    public void distance_from_center_vertex_is_set_for_each_vertex_in_sub_graph(){
       assertThat(
               vertexInWholeGraph(vertexA).minDistanceFromCenterVertex(),
               is(0)
       );
        assertThat(
                vertexInWholeGraph(vertexB).minDistanceFromCenterVertex(),
                is(1)
        );
        assertThat(
                vertexInWholeGraph(vertexC).minDistanceFromCenterVertex(),
                is(2)
        );
    }

    @Test
    public void the_minimum_distance_from_center_vertex_is_returned(){
        assertThat(
                vertexInWholeGraph(vertexC).minDistanceFromCenterVertex(),
                is(2)
        );
        vertexC().addRelationToVertex(vertexA());
        assertThat(
                vertexInWholeGraph(vertexC).minDistanceFromCenterVertex(),
                is(1)
        );
    }
}
