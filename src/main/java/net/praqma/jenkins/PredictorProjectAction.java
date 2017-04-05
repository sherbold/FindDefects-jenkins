/*
 * The MIT License
 *
 * Copyright 2013 Praqma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins;

import hudson.model.Action;
import hudson.model.ProminentProjectAction;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.Collection;
import java.util.Random;

/**
 * Class implementing actionable and ProminentProjectAction.
 *
 * Prominent project actions means that the plugin will have a footprint on the jobs main page.
 * It also allows us to have a menu item.
 *
 * Opposite the build action(or just {@link Action}), the project action is NOT persisted.
 *
 * @author Praqma
 */
public class PredictorProjectAction implements ProminentProjectAction {

    public final AbstractProject<?,?> project;
    public int correct;
    public int incorrect;
    
    /**
     * If this method returns null, no icon will be used and the link will not be visible
     * @return the path to the icon we want to use for our project action. 
     */
    @Override
    public String getIconFileName() {
        return "/plugin/bugs-predictor/images/64x64/staricon.png";
    }

    /**
     * This method is used to create the text for the link to 'Project Action' link on the Job's
     * front page.
     *
     * @return the text to be displayed on the project action link page
     */
    @Override
    public String getDisplayName() {
        return "Defect Predictor statistics ";
    }

    @Override
    public String getUrlName() {
        return "predictortatistics";
    }

    public PredictorProjectAction(AbstractProject<?,?> project) {
        this.project = project;
    }

    /**
     *
     * @return the last build action associated with this project.
     */
    public Collection<PredictorBuildAction> getLastBuildActions() {
        AbstractBuild<?, ?> b = project.getLastCompletedBuild();
        return b == null ? null : b.getActions(PredictorBuildAction.class);
    }

   
    //--------------------------------------------------
    int loc;
    int cbo;
    int wmc;
   
   
    
    Random r = new Random(1000);
	
    public int getLoc()
    {
    	loc = r.nextInt(500);
    	return loc;
    }
    public int getCbo()
    {
    	cbo = r.nextInt(5);
    	return cbo;
    }
    public int getWmc()
    {
    	wmc = r.nextInt(500);
    	return wmc;
    }
    
  
    
    //-----------------------------------------------------
    
    
}
