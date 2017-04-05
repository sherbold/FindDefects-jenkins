
package net.praqma.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.IOException;

/**
 *
 * @author Mads
 */
public class GuessingBuildWrapper extends BuildWrapper {

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return new Environment() {

            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                listener.getLogger().println("Tearing down in build wrapper");
                return super.tearDown(build, listener); //To change body of generated methods, choose Tools | Templates.
            }            
        };
    }
    
    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        
		public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Defects Preditctor Plugin Environment";
        }


        @Override
        public boolean isApplicable(AbstractProject<?, ?> arg0) {
            return arg0 instanceof FreeStyleProject;
        }
        
    }
    
}
