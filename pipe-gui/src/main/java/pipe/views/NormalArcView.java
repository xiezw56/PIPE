package pipe.views;

import pipe.controllers.PetriNetController;
import pipe.gui.Constants;
import pipe.gui.PetriNetTab;
import pipe.gui.model.PipeApplicationModel;
import pipe.handlers.ArcHandler;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Token;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;


public class NormalArcView<S extends Connectable, T extends Connectable> extends ArcView<S,T>  {
    ArcHead arcHead = new NormalHead();
    private final static String type = "normal";
    private final Collection<NameLabel> weightLabel = new LinkedList<NameLabel>();
    // bidirectional arc?
    private boolean joined = false;
    // Whether or not exists an inverse arc
    private NormalArcView<S, T> _inverse = null;
    private Boolean tagged = false;

    public NormalArcView(Arc<S, T> model,
                         PetriNetController controller, Container parent, ArcHandler<? extends Connectable, ? extends Connectable> handler, PipeApplicationModel applicationModel) {
        super(model, controller, parent, handler, applicationModel);
        setTagged(model.isTagged());
        addConnectableListener();
        for (NameLabel label : weightLabel) {
            getParent().add(label);
        }
    }

    private void addConnectableListener() {
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String name = propertyChangeEvent.getPropertyName();
                if (name.equals(Arc.WEIGHT_CHANGE_MESSAGE)) {
                    updateWeights();
                }
            }
        };
        model.addPropertyChangeListener(listener);
    }

    @Override
    public void delete() {
        super.delete();
        for (NameLabel label : weightLabel) {
            removeLabelFromParentContainer(label);
        }
    }

    private void removeLabelFromParentContainer(NameLabel label) {
        tab.remove(label);
    }

    private void updateWeights() {
        removeCurrentWeights();
        createWeightLabels();
        setWeightLabelPosition();

        if (tab != null) {
            addWeightLabelsToContainer(tab);
        }
    }

    protected void setWeightLabelPosition() {
        int originalX = (int) (arcPath.midPoint.x);
        int originalY = (int) (arcPath.midPoint.y) - 10;
        int x = originalX;
        int y = originalY;
        int yCount = 0;

        for (NameLabel label : weightLabel) {
            if (yCount >= 4) {
                y = originalY;
                x += 17;
                yCount = 0;
            }
            label.setPosition(x + label.getWidth() / 2 - 4, y);
            y += 10;
            yCount++;
        }
    }

    private void removeCurrentWeights() {
        for (NameLabel name : weightLabel) {
            removeLabelFromParentContainer(name);
        }
        weightLabel.clear();
    }

    private void createWeightLabels() {
        Map<String, String> weights = model.getTokenWeights();
        for (Map.Entry<String, String> entry : weights.entrySet()) {
            String weight = entry.getValue();
            String tokenId = entry.getKey();
            NameLabel label = new NameLabel();
            label.setText(weight);
            try {
                Token token = petriNetController.getToken(tokenId);
                label.setColor(token.getColor());
            } catch (PetriNetComponentNotFoundException e) {
                e.printStackTrace();
                label.setColor(Color.BLACK);
            } label.updateSize();
            weightLabel.add(label);
        }
    }

    private void addWeightLabelsToContainer(Container container) {
        for (NameLabel label : weightLabel) {
            container.add(label);
        }
    }

    @Override
    public void addToPetriNetTab(PetriNetTab tab) {
        super.addToPetriNetTab(tab);
        updateWeights();
    }

    @Override public String getType() {
        return type;
    }

    /**
     * Accessor function to check whether or not the Arc is tagged
     */
    @Override public boolean isTagged() {
        return tagged;
    }

    /**
     * Accessor function to set whether or not the Arc is tagged
     *
     * @param flag
     */
    public void setTagged(boolean flag) {
        /** Set the timed transition attribute (for GSPNs) */

        tagged = flag;

        // If it becomes tagged we must remove any existing weight....
        // ...and thus we can reuse the weightLabel to display that it's
        // tagged!!!
        // Because remember that a tagged arc must have a weight of 1...
        /*
         * if (tagged) { //weight = 1; weightLabel.setText("TAG");
		 * setWeightLabelPosition(); weightLabel.updateSize(); } else {
		 * weightLabel.setText((weight > 1)?Integer.toString(weight) : ""); }
		 */
        repaint();
    }

    void updateWeightLabel() {

        setWeightLabelPosition();

    }

    private void updateArc(boolean isJoined) {
        inView = true;
        _inverse.inView = !isJoined;

        if (isJoined) {
            _inverse.removeFromView();
//            TransitionView transitionView = this.getTransition();
//            transitionView.removeFromArc(_inverse);
//            transitionView.removeArcCompareObject(_inverse);
//            transitionView.updateConnected();
            joined = isJoined;
        }
        updateWeightLabel();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform reset = g2.getTransform();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.translate(getComponentDrawOffset() + ZOOM_GROW - arcPath.getBounds().getX(),
                getComponentDrawOffset() + ZOOM_GROW - arcPath.getBounds().getY());


        if (isSelected() && !_ignoreSelection) {
            g2.setPaint(Constants.SELECTION_LINE_COLOUR);
        } else {
            g2.setPaint(Constants.ELEMENT_LINE_COLOUR);
        }

        if (joined) {
            g2.translate(arcPath.getPoint(0).getX(), arcPath.getPoint(0).getY());
            g2.rotate(arcPath.getStartAngle() + Math.PI);
            g2.setTransform(reset);
        }

        g2.setStroke(new BasicStroke(1f));
        g2.draw(arcPath);

        g2.translate(arcPath.getPoint(arcPath.getEndIndex()).getX(), arcPath.getPoint(arcPath.getEndIndex()).getY());

        g2.rotate(model.getEndAngle());
        g2.setColor(java.awt.Color.WHITE);


        if (isSelected() && !_ignoreSelection) {
            g2.setPaint(Constants.SELECTION_LINE_COLOUR);
        } else {
            g2.setPaint(Constants.ELEMENT_LINE_COLOUR);
        }

        arcHead.draw(g2);

        g2.transform(reset);
    }
}