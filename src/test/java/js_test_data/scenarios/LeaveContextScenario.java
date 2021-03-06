package js_test_data.scenarios;

import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.GraphFactory;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.subgraph.SubGraphJson;
import guru.bubl.module.model.graph.subgraph.UserGraph;
import guru.bubl.module.model.graph.vertex.VertexFactory;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import js_test_data.JsTestScenario;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Inject;

public class LeaveContextScenario implements JsTestScenario {

    /*
     *
     tech choice {
       -> choice a{
            -> choice a1
            -> choice a2
       }
       -> choice b
       -> choice c
    }
    * */

    @Inject
    protected GraphFactory graphFactory;

    @Inject
    protected VertexFactory vertexFactory;

    User user = User.withEmailAndUsername("a", "b");

    private VertexOperator
            techChoice,
            choiceA,
            choiceB,
            choiceC,
            choiceA1,
            choiceA2;

    @Override
    public Object build() {
        UserGraph userGraph = graphFactory.loadForUser(user);
        create();
        try {
            return new JSONObject().put(
                    "techChoiceCenter",
                    SubGraphJson.toJson(
                            userGraph.aroundForkUriInShareLevels(
                                    techChoice.uri(),
                                    ShareLevel.allShareLevelsInt
                            )
                    )
            ).put(
                    "choiceACenter",
                    SubGraphJson.toJson(
                            userGraph.aroundForkUriInShareLevels(
                                    choiceA.uri(),
                                    ShareLevel.allShareLevelsInt
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void create() {
        techChoice = vertexFactory.createForOwner(user.username());
        techChoice.label("tech choice");

        choiceA = vertexFactory.createForOwner(user.username());
        choiceA.label("choice a");
        techChoice.addRelationToFork(choiceA.uri(), ShareLevel.PRIVATE, ShareLevel.PRIVATE);

        choiceB = vertexFactory.createForOwner(user.username());
        choiceB.label("choice b");
        techChoice.addRelationToFork(choiceB.uri(), ShareLevel.PRIVATE, ShareLevel.PRIVATE);

        choiceC = vertexFactory.createForOwner(user.username());
        choiceC.label("choice c");
        techChoice.addRelationToFork(choiceC.uri(), ShareLevel.PRIVATE, ShareLevel.PRIVATE);

        choiceA1 = vertexFactory.createForOwner(user.username());
        choiceA1.label("choice a1");
        choiceA.addRelationToFork(choiceA1.uri(), ShareLevel.PRIVATE, ShareLevel.PRIVATE);

        choiceA2 = vertexFactory.createForOwner(user.username());
        choiceA2.label("choice a2");
        choiceA.addRelationToFork(choiceA2.uri(), ShareLevel.PRIVATE, ShareLevel.PRIVATE);
    }


}
