package pipe.controllers.arcCreator;

import pipe.controllers.PetriNetController;
import pipe.controllers.PipeApplicationController;
import pipe.models.PetriNet;
import pipe.models.component.*;
import pipe.views.PipeApplicationView;

import java.util.HashMap;
import java.util.List;

public class InhibitorCreator implements ArcActionCreator {

    private final PipeApplicationController controller;

    private final PipeApplicationView applicationView;

    public InhibitorCreator(PipeApplicationController controller, PipeApplicationView applicationView) {

        this.controller = controller;
        this.applicationView = applicationView;
    }

    @Override
    public <S extends Connectable, T extends Connectable> void create(S source, T target) {
        createArc(source, target);
    }

    private <S extends Connectable, T extends Connectable> Arc<Place, Transition> createArc(S source, T target) {
        PetriNetController petriNetController = controller.getActivePetriNetController();
        if (source.getClass().equals(Place.class) && target.getClass().equals(Transition.class)) {
            Place place = (Place) source;
            Transition transition = (Transition) target;
            Arc<Place, Transition> arc = new Arc<Place, Transition>(place, transition, new HashMap<Token, String>(),
                    petriNetController.getInhibitorStrategy());
            PetriNet petriNet = petriNetController.getPetriNet();
            petriNet.addArc(arc);
            return arc;
        }
        return null;
    }

    @Override
    public <S extends Connectable, T extends Connectable> void create(S source, T target, List<ArcPoint> arcPoints) {
        Arc<Place, Transition> arc = createArc(source, target);
        if (arc != null) {
            arc.addIntermediatePoints(arcPoints);
        }
    }

    @Override
    public <S extends Connectable, T extends Connectable> boolean canCreate(S source, T target) {
        return source.getClass().equals(Place.class) && target.getClass().equals(Transition.class);
    }
}