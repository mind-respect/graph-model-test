/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.test.module.model.graph;

import guru.bubl.module.model.User;
import guru.bubl.module.model.UserUris;
import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.Triple;
import guru.bubl.module.model.graph.edge.EdgePojo;
import guru.bubl.module.model.graph.identification.Identifier;
import guru.bubl.module.model.graph.identification.IdentificationFactory;
import guru.bubl.module.model.graph.edge.Edge;
import guru.bubl.module.model.graph.edge.EdgeOperator;
import guru.bubl.module.model.test.scenarios.TestScenarios;
import guru.bubl.test.module.utils.ModelTestResources;
import guru.bubl.module.model.graph.vertex.Vertex;
import guru.bubl.module.model.graph.vertex.VertexFactory;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Test;
import guru.bubl.module.model.FriendlyResource;
import guru.bubl.module.model.suggestion.Suggestion;
import guru.bubl.module.model.suggestion.SuggestionPojo;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;


public class VertexOperatorTest extends ModelTestResources {

    @Inject
    VertexFactory vertexFactory;

    @Inject
    IdentificationFactory identificationFactory;

    @Test
    public void can_update_label() {
        EdgeOperator newEdge = edgeFactory.withUri(
                vertexA.addVertexAndRelation().uri()
        );
        VertexOperator vertex = newEdge.destinationVertex();
        vertex.label("Ju-Ji-Tsu");
        assertThat(vertex.label(), is("Ju-Ji-Tsu"));
    }

    @Test
    public void can_update_comment() {
        assertThat(
                vertexA.comment(),
                is("")
        );
        vertexA.comment(
                "Its vertex a !"
        );
        assertThat(
                vertexA.comment(),
                is("Its vertex a !")
        );
    }

    @Test
    public void can_check_if_vertex_has_edge() {
        EdgeOperator edge = vertexA.getEdgeThatLinksToDestinationVertex(
                vertexB
        );
        assertTrue(
                vertexA.hasEdge(
                        edge
                )
        );
        assertTrue(
                vertexB.hasEdge(
                        edge
                )
        );
        edge.remove();
        assertFalse(
                vertexA.hasEdge(
                        edge
                )
        );
        assertFalse(
                vertexB.hasEdge(
                        edge
                )
        );
    }

    @Test
    public void can_add_vertex_and_relation() {
        Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
        Edge edge = vertexA.addVertexAndRelation();
        assertThat(edge, is(not(nullValue())));
        Integer newNumberOfEdgesAndVertices = numberOfEdgesAndVertices();
        assertThat(newNumberOfEdgesAndVertices, is(numberOfEdgesAndVertices + 2));
        assertTrue(vertexA.hasEdge(edge));

        assertThat(edge.sourceVertex().uri(), is(vertexA.uri()));

        Vertex destinationVertex = edge.destinationVertex();
        assertThat(destinationVertex, is(not(nullValue())));
    }

    @Test
    public void can_remove_a_vertex() {
        Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();

        URI vertexBId = vertexB.uri();

        assertTrue(userGraph.haveElementWithId(vertexBId));
        vertexB.remove();
        assertFalse(userGraph.haveElementWithId(vertexBId));

        Integer updatedNumberOfEdgesAndVertices = numberOfEdgesAndVertices();
        assertThat(updatedNumberOfEdgesAndVertices, is(numberOfEdgesAndVertices - 3));
    }

    @Test
    public void removing_a_vertex_decrements_number_of_references_to_its_identification() {
        testThatRemovingGraphElementRemovesTheNumberOfReferencesToItsIdentification(
                vertexA
        );
    }

    @Test
    public void can_get_number_of_connected_edges() {
        assertThat(
                vertexB.getNumberOfConnectedEdges(),
                is(2)
        );
    }

    @Test
    public void removing_a_vertex_decreases_the_number_of_edges_of_its_neighbors() {
        Integer numberOfEdgesForA = vertexA.getNumberOfConnectedEdges();
        Integer numberOfEdgesForC = vertexC.getNumberOfConnectedEdges();
        vertexB.remove();
        assertThat(
                vertexA.getNumberOfConnectedEdges(),
                is(numberOfEdgesForA - 1)
        );
        assertThat(
                vertexC.getNumberOfConnectedEdges(),
                is(numberOfEdgesForC - 1)
        );
    }

    @Test
    public void removing_a_vertex_removes_its_relations() {
        URI edgeBetweenCAndBUri = vertexB.getEdgeThatLinksToDestinationVertex(
                vertexC
        ).uri();
        URI edgeBetweenAAndBUri = vertexB.getEdgeThatLinksToDestinationVertex(
                vertexC
        ).uri();
        assertTrue(
                userGraph.haveElementWithId(
                        edgeBetweenCAndBUri
                )
        );
        assertTrue(
                userGraph.haveElementWithId(
                        edgeBetweenAAndBUri
                )
        );
        vertexB.remove();
        assertFalse(
                userGraph.haveElementWithId(
                        edgeBetweenCAndBUri
                )
        );
        assertFalse(
                userGraph.haveElementWithId(
                        edgeBetweenAAndBUri
                )
        );
    }


    @Test
    public void can_add_suggestions_to_a_vertex() {
        assertTrue(
                vertexA.getSuggestions().isEmpty()
        );
        vertexA.addSuggestions(
                suggestionsToMap(
                        modelTestScenarios.startDateSuggestionFromEventIdentification(user())
                )
        );
        assertFalse(
                vertexA.getSuggestions().isEmpty()
        );
        Suggestion addedSuggestion = vertexA.getSuggestions().values().iterator().next();
        assertThat(
                addedSuggestion.label(),
                is("Start date")
        );
        assertThat(
                addedSuggestion.getType().uri(),
                is(
                        URI.create("http://rdf.freebase.com/rdf/type/datetime")
                )
        );
        assertThat(
                addedSuggestion.getSameAs().uri(),
                is(
                        URI.create("http://rdf.freebase.com/rdf/time/event/start_date")
                )
        );
        assertTrue(
                addedSuggestion.origins().iterator().next().isRelatedToFriendlyResource(
                        new FriendlyResourcePojo(
                                URI.create("http://rdf.freebase.com/rdf/time/event")
                        )
                )
        );
    }

    @Test
    @Ignore("to complete")
    public void can_add_another_origin_to_suggestion() {
        vertexA.addSuggestions(
                suggestionsToMap(
                        modelTestScenarios.nameSuggestionFromPersonIdentification(user())
                )
        );
        vertexA.addSuggestions(
                suggestionsToMap(
                        modelTestScenarios.nameSuggestionFromSymbolIdentification(user())
                )
        );
    }

    @Test
    public void accepting_a_suggestion_adds_a_new_neighbor() {
        Integer numberOfEdges = vertexA.getNumberOfConnectedEdges();
        vertexA.acceptSuggestion(
                modelTestScenarios.nameSuggestionFromPersonIdentification(
                        user()
                )
        );
        Integer newNumberOfEdges = vertexA.getNumberOfConnectedEdges();
        assertThat(
                newNumberOfEdges,
                is(numberOfEdges + 1)
        );
    }

    @Test
    public void edge_from_accepting_suggestion_has_suggestion_label() {
        SuggestionPojo nameSuggestion = modelTestScenarios.nameSuggestionFromPersonIdentification(
                user()
        );
        Edge edge = vertexA.acceptSuggestion(nameSuggestion);
        assertThat(edge.label(), is(nameSuggestion.label()));
    }

    @Test
    public void edge_from_accepting_suggestion_has_suggestion_same_as() {
        SuggestionPojo nameSuggestion = modelTestScenarios.nameSuggestionFromPersonIdentification(
                user()
        );
        Edge edge = vertexA.acceptSuggestion(nameSuggestion);
        Identifier identification = edge.getIdentifications().values().iterator().next();
        assertThat(
                identification.getExternalResourceUri(),
                Is.is(nameSuggestion.getSameAs().uri())
        );
    }

    @Test
    public void new_vertex_from_accepting_suggestion_has_suggestion_same_as_and_type() {
        SuggestionPojo nameSuggestion = modelTestScenarios.nameSuggestionFromPersonIdentification(
                user()
        );
        Vertex newVertex = vertexA.acceptSuggestion(nameSuggestion).destinationVertex();
        assertThat(newVertex.getIdentifications().size(), is(2));
        assertTrue(
                hasTypeWithExternalUri(
                        newVertex,
                        nameSuggestion.getSameAs().uri()
                )
        );
        assertTrue(
                hasTypeWithExternalUri(
                        newVertex,
                        nameSuggestion.getType().uri()
                )
        );
    }

    @Test
    public void vertex_from_suggestion_from_comparison_has_type_label() {
        SuggestionPojo suggestion = testScenarios.suggestionFromComparisonForUserAndTriple(
                anotherUser,
                Triple.fromEdgeSourceAndDestination(
                        vertexA.getEdgeThatLinksToDestinationVertex(vertexB),
                        vertexOfAnotherUser,
                        vertexB
                )
        );
        Vertex newVertex = vertexOfAnotherUser.acceptSuggestion(
                suggestion
        ).destinationVertex();
        assertThat(
                newVertex.label(),
                is("vertex B")
        );
    }

    @Test
    public void vertex_from_suggestion_from_comparison_is_not_identified_to_suggestion_same_as() {
        SuggestionPojo suggestion = testScenarios.suggestionFromComparisonForUserAndTriple(
                anotherUser,
                Triple.fromEdgeSourceAndDestination(
                        vertexA.getEdgeThatLinksToDestinationVertex(vertexB),
                        vertexOfAnotherUser,
                        vertexB
                )
        );
        VertexOperator newVertex = vertexOfAnotherUser.acceptSuggestion(
                suggestion
        ).destinationVertex();
        assertFalse(
                newVertex.getIdentifications().containsKey(
                        suggestion.getSameAs().uri()
                )
        );
    }

    @Test
    public void can_get_empty_list_after_removing_last_same_as() {
        Identifier timBernersLee = vertexA.addMeta(
                modelTestScenarios.timBernersLee()
        ).values().iterator().next();
        assertFalse(vertexA.getIdentifications().isEmpty());
        vertexA.removeIdentification(
                timBernersLee
        );
        assertTrue(vertexA.getIdentifications().isEmpty());
    }

    @Test
    public void deleting_a_vertex_does_not_delete_its_identifications_in_the_graph() {
        assertTrue(
                userGraph.haveElementWithId(
                        vertexB.uri()
                )
        );
        vertexA.addMeta(
                TestScenarios.identificationFromFriendlyResource(
                        vertexB
                )
        );
        vertexA.remove();
        assertTrue(
                userGraph.haveElementWithId(
                        vertexB.uri()
                )
        );
    }

    @Test
    public void can_assign_the_same_identification_to_2_vertices() {
        Identifier timBernersLee = vertexA.addMeta(
                modelTestScenarios.timBernersLee()
        ).values().iterator().next();
        vertexB.addMeta(
                modelTestScenarios.timBernersLee()
        );
        assertTrue(
                vertexA.getIdentifications().containsKey(
                        timBernersLee.getExternalResourceUri()
                )
        );
        assertTrue(
                vertexB.getIdentifications().containsKey(
                        timBernersLee.getExternalResourceUri()
                )
        );
    }

    @Test
    public void can_get_same_as() {
        EdgeOperator newEdge = edgeFactory.withUri(
                vertexA.addVertexAndRelation().uri()
        );
        VertexOperator newVertex = newEdge.destinationVertex();
        newVertex.label("Tim Berners Lee");
        assertTrue(newVertex.getIdentifications().isEmpty());
        newVertex.addMeta(modelTestScenarios.timBernersLee());
        Identifier sameAs = newVertex.getIdentifications().values().iterator().next();
        assertThat(
                sameAs.getExternalResourceUri(),
                is(
                        modelTestScenarios.timBernersLee().getExternalResourceUri()
                )
        );
    }

    @Test
    public void can_add_generic_identification() {
        assertFalse(vertexA.getIdentifications().containsKey(
                modelTestScenarios.extraterrestrial().getExternalResourceUri()
        ));
        Identifier ExtraTerrestrial = vertexA.addMeta(
                modelTestScenarios.extraterrestrial()
        ).values().iterator().next();
        assertTrue(vertexA.getIdentifications().containsKey(
                ExtraTerrestrial.getExternalResourceUri()
        ));
    }

    @Test
    public void can_test_if_vertex_has_destination_vertex() {
        assertFalse(vertexA.hasDestinationVertex(vertexC));
        vertexA.addRelationToVertex(vertexC);
        assertTrue(vertexA.hasDestinationVertex(vertexC));
    }

    @Test
    public void source_vertex_is_not_a_destination_vertex() {
        vertexA.addRelationToVertex(vertexC);
        assertTrue(vertexA.hasDestinationVertex(vertexC));
        assertFalse(vertexC.hasDestinationVertex(vertexA));
    }

    @Test
    public void there_is_a_creation_date() {
        assertThat(
                vertexA.creationDate(),
                is(not(nullValue()))
        );
    }

    @Test
    public void there_is_a_last_modification_date() {
        assertThat(
                vertexA.lastModificationDate(),
                is(not(nullValue()))
        );
    }

    @Test
    public void a_vertex_is_private_by_default() {
        VertexOperator newVertex = vertexFactory.withUri(
                vertexA.addVertexAndRelation().destinationVertex().uri()
        );
        assertFalse(newVertex.isPublic());
    }


    @Test
    public void can_make_private_vertex_with_no_relations() {
        VertexOperator noRelationsVertex = vertexFactory.withUri(
                userGraph.createVertex().uri()
        );
        noRelationsVertex.makePublic();
        assertTrue(noRelationsVertex.isPublic());
        noRelationsVertex.makePrivate();
        assertFalse(noRelationsVertex.isPublic());
    }

    @Test
    public void making_a_vertex_public_makes_all_its_edges_public_where_the_other_end_vertex_is_also_public() {
        vertexA.setShareLevel(ShareLevel.PUBLIC);
        vertexC.setShareLevel(ShareLevel.PRIVATE);
        vertexB.setShareLevel(ShareLevel.PRIVATE);
        assertFalse(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexA
                ).isPublic()
        );
        assertFalse(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexC
                ).isPublic()
        );
        vertexB.makePublic();
        assertTrue(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexA
                ).isPublic()
        );
        assertFalse(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexC
                ).isPublic()
        );
    }

    @Test
    public void making_a_vertex_private_makes_all_its_edges_private() {
        vertexA.makePublic();
        vertexB.makePublic();
        vertexC.makePrivate();
        assertTrue(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexA
                ).isPublic()
        );
        assertFalse(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexC
                ).isPublic()
        );
        vertexB.makePrivate();
        assertFalse(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexA
                ).isPublic()
        );
        assertFalse(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexC
                ).isPublic()
        );
    }


    @Test
    public void can_add_vertex_and_relation_using_edge_and_vertex_uris() {
        UUID vertexId = UUID.randomUUID();
        UUID edgeId = UUID.randomUUID();
        EdgePojo newEdge = vertexA.addVertexAndRelationWithIds(
                vertexId.toString(),
                edgeId.toString()
        );
        UserUris userUris = new UserUris(vertexA.getOwnerUsername());
        URI vertexUri = userUris.vertexUriFromShortId(vertexId.toString());
        assertThat(
                newEdge.destinationVertex().uri(),
                is(vertexUri)
        );
        URI edgeUri = userUris.edgeUriFromShortId(edgeId.toString());
        assertThat(
                newEdge.uri(),
                is(edgeUri)
        );
    }

    @Test
    public void checks_that_uris_dont_already_exist() {
        UUID vertexId = UUID.fromString(
                UserUris.graphElementShortId(vertexB.uri())
        );
        UUID edgeId = UUID.fromString(
                UserUris.graphElementShortId(vertexA.getEdgeThatLinksToDestinationVertex(vertexB).uri())
        );
        EdgePojo newEdge = vertexA.addVertexAndRelationWithIds(
                vertexId.toString(),
                edgeId.toString()
        );
        UserUris userUris = new UserUris(vertexA.getOwnerUsername());
        URI vertexUri = userUris.vertexUriFromShortId(vertexId.toString());
        assertThat(
                newEdge.destinationVertex().uri(),
                is(not(vertexUri))
        );
        URI edgeUri = userUris.edgeUriFromShortId(edgeId.toString());
        assertThat(
                newEdge.uri(),
                is(not(edgeUri))
        );
    }

    @Test
    public void changing_share_level_from_friend_to_public_decrements_neighbors_number_of_friends() {
        vertexB.setShareLevel(ShareLevel.FRIENDS);
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(1)
        );
        vertexB.setShareLevel(ShareLevel.PUBLIC);
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(0)
        );
    }

    @Test
    public void can_check_equality() {
        assertTrue(vertexA.equals(vertexA));
        assertFalse(vertexA.equals(vertexB));
    }

    @Test
    public void can_compare_to_friendly_resource() {
        FriendlyResource vertexAAsFriendlyResource = vertexA;
        assertTrue(vertexA.equals(vertexAAsFriendlyResource));
    }

    @Test
    public void can_get_empty_set_of_included_graph_elements_for_a_vertex_that_have_none() {
        assertTrue(
                vertexA.getIncludedVertices().isEmpty()
        );
        assertTrue(
                vertexA.getIncludedEdges().isEmpty()
        );
    }

    @Test
    public void can_create_vertex_from_graph_elements_set() {
        Vertex newVertex = vertexFactory.createFromGraphElements(
                vertexBAndC(),
                edgeBetweenBAndCInSet()
        );
        Map<URI, ? extends Vertex> includedVertices = newVertex.getIncludedVertices();
        assertTrue(includedVertices.containsKey(vertexB.uri()));
        assertTrue(includedVertices.containsKey(vertexC.uri()));
        assertFalse(includedVertices.containsKey(vertexA.uri()));
        Map<URI, ? extends Edge> includedEdges = newVertex.getIncludedEdges();
        assertTrue(includedEdges.containsKey(
                vertexB.getEdgeThatLinksToDestinationVertex(vertexC).uri()
        ));
    }

    @Test
    public void more_than_one_graph_element_are_required_to_create_vertex_from_graph_elements() {
        Set<Vertex> emptyVertices = new HashSet<>();
        Set<Edge> emptyEdges = new HashSet<>();
        try {
            vertexFactory.createFromGraphElements(
                    emptyVertices,
                    emptyEdges
            );
            fail();
        } catch (Exception exception) {
            //continue
        }
        Set<Vertex> one = new HashSet<>();
        one.add(vertexA);
        try {
            vertexFactory.createFromGraphElements(
                    one,
                    emptyEdges
            );
            fail();
        } catch (Exception exception) {
            //continue
        }
    }

    @Test
    public void removing_a_graph_element_removes_it_from_included_graph_elements_as_well() {
        Vertex newVertex = vertexFactory.createFromGraphElements(
                vertexBAndC(),
                edgeBetweenBAndCInSet()
        );
        assertThat(
                newVertex.getIncludedVertices().size(),
                is(2)
        );
        assertThat(
                newVertex.getIncludedEdges().size(),
                is(1)
        );
        vertexB.getEdgeThatLinksToDestinationVertex(
                vertexC
        ).remove();
        assertThat(
                newVertex.getIncludedVertices().size(),
                is(2)
        );
        assertThat(
                newVertex.getIncludedEdges().size(),
                is(0)
        );
    }

    @Test
    public void can_remove_a_vertex_having_no_relations() {
        VertexOperator vertexWithNoRelations = vertexFactory.createForOwner(
                user().username()
        );
        URI vertexWithNoRelationsUri = vertexWithNoRelations.uri();
        assertTrue(
                userGraph.haveElementWithId(
                        vertexWithNoRelationsUri
                )
        );
        vertexWithNoRelations.remove();
        assertFalse(
                userGraph.haveElementWithId(
                        vertexWithNoRelationsUri
                )
        );
    }

    @Test
    public void removing_a_vertex_removes_its_delete_edges_from_included_graph_elements_as_well() {
        Vertex newVertex = vertexFactory.createFromGraphElements(
                vertexBAndC(),
                edgeBetweenBAndCInSet()
        );
        assertThat(
                newVertex.getIncludedVertices().size(),
                is(2)
        );
        assertThat(
                newVertex.getIncludedEdges().size(),
                is(1)
        );
        vertexB.remove();
        assertThat(
                newVertex.getIncludedVertices().size(),
                is(1)
        );
        assertThat(
                newVertex.getIncludedEdges().size(),
                is(0)
        );
    }

    @Test
    public void removing_vertex_that_has_included_graph_elements_doesnt_remove_its_included_graph_elements() {
        VertexOperator newVertex = vertexFactory.createFromGraphElements(
                vertexBAndC(),
                edgeBetweenBAndCInSet()
        );
        newVertex.remove();
        assertTrue(
                wholeGraph().containsVertex(vertexB)
        );
        assertTrue(
                wholeGraph().containsVertex(vertexC)
        );
    }

    @Test
    public void including_a_vertex_doesnt_add_to_it_any_included_graph_elements() {
        vertexFactory.createFromGraphElements(
                vertexBAndC(),
                edgeBetweenBAndCInSet()
        );
        assertTrue(vertexB.getIncludedVertices().isEmpty());
        assertTrue(vertexB.getIncludedEdges().isEmpty());
    }

    @Test
    public void when_deleting_a_vertex_it_decrements_the_number_of_connected_vertices_of_its_neighbors() {
        vertexC.addVertexAndRelation();
        assertThat(
                vertexC.getNumberOfConnectedEdges(),
                is(2)
        );
        assertThat(
                vertexA.getNumberOfConnectedEdges(),
                is(1)
        );
        vertexB.remove();
        assertThat(
                vertexC.getNumberOfConnectedEdges(),
                is(1)
        );
        assertThat(
                vertexA.getNumberOfConnectedEdges(),
                is(0)
        );
    }

    @Test
    public void adding_a_relation_to_existing_vertices_increments_number_of_connected_edges() {
        int numberOfEdgesForVertexA = vertexA.getNumberOfConnectedEdges();
        int numberOfEdgesForVertexC = vertexC.getNumberOfConnectedEdges();
        vertexC.addRelationToVertex(vertexA);
        assertThat(
                vertexA.getNumberOfConnectedEdges(),
                is(numberOfEdgesForVertexA + 1)
        );
        assertThat(
                vertexC.getNumberOfConnectedEdges(),
                is(numberOfEdgesForVertexC + 1)
        );
    }

    @Test
    public void adding_a_relation_to_existing_vertices_does_not_increment_nb_public_neighbors_if_both_are_private() {
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        vertexC.addRelationToVertex(vertexA);
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
    }

    @Test
    public void adding_a_relation_to_existing_vertices_increments_nb_public_neighbors_to_source_if_destination_is_public() {
        vertexA.makePublic();
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        vertexC.addRelationToVertex(vertexA);
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(1)
        );
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
    }

    @Test
    public void adding_a_relation_to_existing_vertices_increments_nb_public_neighbors_to_destination_if_source_is_public() {
        vertexC.makePublic();
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        vertexC.addRelationToVertex(vertexA);
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(1)
        );
    }

    @Test
    public void adding_a_relation_to_existing_vertices_increments_nb_friend_neighbors_to_source_if_destination_is_friend() {
        vertexA.setShareLevel(ShareLevel.FRIENDS);
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbFriendNeighbors(),
                is(0)
        );
        vertexC.addRelationToVertex(vertexA);
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(1)
        );
        assertThat(
                vertexA.getNbFriendNeighbors(),
                is(0)
        );
    }

    @Test
    public void adding_a_relation_to_existing_vertices_increments_nb_friend_neighbors_to_destination_if_source_is_friend() {
        vertexC.setShareLevel(ShareLevel.FRIENDS);
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbFriendNeighbors(),
                is(0)
        );
        vertexC.addRelationToVertex(vertexA);
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(0)
        );
        assertThat(
                vertexA.getNbFriendNeighbors(),
                is(1)
        );
    }

    @Test
    public void fork_does_not_have_the_same_uri() {
        Vertex vertexAClone = vertexA.forkForUserUsingCache(
                user,
                vertexA
        );
        assertThat(
                vertexAClone.uri(),
                not(vertexA.uri())
        );
    }

    @Test
    public void fork_has_same_label_and_comment() {
        vertexA.comment(
                "vertex A comment"
        );
        VertexOperator vertexAClone = vertexA.forkForUserUsingCache(
                user,
                vertexA
        );
        assertThat(
                vertexAClone.label(),
                is("vertex A")
        );
        assertThat(
                vertexAClone.comment(),
                is("vertex A comment")
        );
    }

    @Test
    public void fork_is_identified_to_original_vertex() {
        VertexOperator vertexAClone = vertexA.forkForUserUsingCache(
                user,
                vertexA
        );
        vertexAClone.getIdentifications().containsKey(
                vertexA.uri()
        );
    }

    @Test
    public void fork_identification_to_original_vertex_has_the_original_vertex_label() {
        VertexOperator vertexAClone = vertexA.forkForUserUsingCache(
                user,
                vertexA
        );
        Identifier identification = identificationFactory.withUri(
                vertexAClone.getIdentifications().get(
                        vertexA.uri()
                ).uri()
        );
        assertThat(
                identification.label(),
                is("vertex A")
        );
    }

    @Test
    public void making_vertex_public_increments_the_number_of_public_neighbor_vertices_set_to_neighbors() {
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        vertexB.makePublic();
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(1)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(1)
        );
    }

    @Test
    public void making_vertex_private_decrements_the_number_of_public_neighbor_vertices_set_to_neighbors() {
        vertexB.makePublic();
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(1)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(1)
        );
        vertexB.makePrivate();
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
    }

    @Test
    public void new_child_vertex_nb_public_neighbors_is_set_to_1_when_parent_is_public() {
        VertexOperator newVertex = vertexFactory.withUri(
                vertexA.addVertexAndRelation().destinationVertex().uri()
        );
        assertThat(
                newVertex.getNbPublicNeighbors(),
                is(0)
        );
        vertexA.makePublic();
        VertexOperator anotherNewVertex = vertexFactory.withUri(
                vertexA.addVertexAndRelation().destinationVertex().uri()
        );
        assertThat(
                anotherNewVertex.getNbPublicNeighbors(),
                is(1)
        );
    }

    @Test
    public void if_removed_vertex_is_public_it_decrements_nb_public_neighbors_to_neighbors() {
        vertexB.makePublic();
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(1)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(1)
        );
        vertexB.remove();
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
    }

    @Test
    public void if_removed_vertex_is_private_it_does_not_decrements_nb_public_neighbors_to_neighbors() {
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
        vertexB.remove();
        assertThat(
                vertexA.getNbPublicNeighbors(),
                is(0)
        );
        assertThat(
                vertexC.getNbPublicNeighbors(),
                is(0)
        );
    }

    @Test
    public void mergeTo_removes_vertex() {
        URI farCenterVertexUri = userGraph.createVertex().uri();
        VertexOperator farCenterVertex = vertexFactory.withUri(
                farCenterVertexUri
        );
        assertTrue(userGraph.haveElementWithId(farCenterVertexUri));
        farCenterVertex.mergeTo(vertexC);
        assertFalse(userGraph.haveElementWithId(farCenterVertexUri));
    }

    @Test
    public void mergeTo_includes_edges() {
        URI farCenterVertexUri = userGraph.createVertex().uri();
        VertexOperator farCenterVertex = vertexFactory.withUri(
                farCenterVertexUri
        );
        Edge edge1 = farCenterVertex.addVertexAndRelation();
        Edge edge2 = farCenterVertex.addVertexAndRelation();
        assertFalse(
                vertexC.hasEdge(edge1)
        );
        assertFalse(
                vertexC.hasEdge(edge2)
        );
        farCenterVertex.mergeTo(vertexC);
        assertTrue(
                vertexC.hasEdge(edge1)
        );
        assertTrue(
                vertexC.hasEdge(edge2)
        );
    }

    @Test
    public void mergeTo_preserves_edges_direction() {
        URI farCenterVertexUri = userGraph.createVertex().uri();
        VertexOperator farCenterVertex = vertexFactory.withUri(
                farCenterVertexUri
        );
        Edge edge1 = farCenterVertex.addVertexAndRelation();
        Vertex edge1OtherVertex = edge1.destinationVertex();
        edgeFactory.withUri(edge1.uri()).inverse();
        farCenterVertex.mergeTo(vertexC);

        EdgeOperator edge1Operator = vertexC.connectedEdges().get(
                edge1.uri()
        );
        assertThat(
                edge1Operator.sourceVertex(),
                is(edge1OtherVertex)
        );
        assertThat(
                edge1Operator.destinationVertex(),
                is(vertexC)
        );
    }

    @Test
    public void can_set_share_level() {
        assertThat(
                vertexA.getShareLevel(),
                is(ShareLevel.PRIVATE)
        );
        vertexA.setShareLevel(
                ShareLevel.FRIENDS
        );
        assertThat(
                vertexA.getShareLevel(),
                is(ShareLevel.FRIENDS)
        );
    }

    @Test
    public void setting_share_level_sets_it_for_surrounding_edges() {
        assertThat(
                vertexB.getEdgeThatLinksToDestinationVertex(vertexC).getShareLevel(),
                is(ShareLevel.PRIVATE)
        );
        vertexB.setShareLevel(
                ShareLevel.FRIENDS
        );
        assertThat(
                vertexB.getEdgeThatLinksToDestinationVertex(vertexC).getShareLevel(),
                is(ShareLevel.PRIVATE)
        );
        vertexC.setShareLevel(
                ShareLevel.FRIENDS
        );
        assertThat(
                vertexB.getEdgeThatLinksToDestinationVertex(vertexC).getShareLevel(),
                is(ShareLevel.FRIENDS)
        );
        vertexB.setShareLevel(ShareLevel.PUBLIC);

        assertThat(
                vertexB.getEdgeThatLinksToDestinationVertex(vertexC).getShareLevel(),
                is(ShareLevel.FRIENDS)
        );
    }

    @Test
    public void setting_share_level_to_friends_increments_number_of_friend_neighbors_to_neighbors() {
        assertThat(
                vertexA.getNbFriendNeighbors(),
                is(0)
        );
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(0)
        );
        vertexB.setShareLevel(ShareLevel.FRIENDS);
        assertThat(
                vertexA.getNbFriendNeighbors(),
                is(1)
        );
        assertThat(
                vertexC.getNbFriendNeighbors(),
                is(1)
        );
    }

    private Set<Edge> edgeBetweenBAndCInSet() {

        Set<Edge> edges = new HashSet<>();
        edges.add(
                vertexB.getEdgeThatLinksToDestinationVertex(
                        vertexC
                )
        );
        return edges;
    }

    private Set<Vertex> vertexBAndC() {
        Set<Vertex> vertexBAndC = new HashSet<>();
        vertexBAndC.add(vertexB);
        vertexBAndC.add(vertexC);
        return vertexBAndC;
    }

    private Boolean hasTypeWithExternalUri(Vertex vertex, URI externalUri) {
        for (Identifier identification : vertex.getIdentifications().values()) {
            if (identification.getExternalResourceUri().equals(externalUri)) {
                return true;
            }
        }
        return false;
    }
}

