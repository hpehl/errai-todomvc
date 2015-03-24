package org.jboss.errai.demo.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.demo.client.shared.UserComplaint;
import org.jboss.errai.demo.client.shared.UserComplaintEndpoint;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.ui.client.widget.ValueImage;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Model;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * This is the companion Java class of the complaint form as specified by
 * {@link Templated}. It refers to a data field called "app-template" in
 * ComplaintForm.html as its root and gains access to all data fields in the
 * template to add dynamic behavior (e.g. event handlers, data bindings, page
 * transitions).
 * <p>
 * The {@link Page} annotation declares this form as a bookmarkable page that
 * can be transitioned to by other pages of this application. Further the
 * specified role (DefaultPage.class) make this page appear by default when the
 * application is started.
 */
@Templated("ComplaintForm.html#app-template")
public class ComplaintForm extends Composite {

    /**
     * Errai's data binding module will automatically bind the injected instance
     * of {@link UserComplaint} to all fields annotated with {@link Bound}. If not
     * specified otherwise, the bindings occur based on matching field names (e.g.
     * userComplaint.name will automatically be kept in sync with the data-field
     * "name")
     */
    @Inject
    @Model
    private UserComplaint userComplaint;

    @Inject
    @Bound
    @DataField
    private TextBox name;

    @Inject
    @Bound
    @DataField
    private TextBox email;

    @Inject
    @Bound
    @DataField
    private TextArea text;

    @Inject
    @Bound
    @DataField
    private ValueImage image;

    @Inject
    @DataField
    private Button submit;

    /**
     * Errai's JAX-RS module generates a stub class that makes AJAX calls back to
     * the server for each resource method on the {@link UserComplaintEndpoint}
     * interface. The paths and HTTP methods for the AJAX calls are determined
     * automatically based on the JAX-RS annotations ({@code @Path}, {@code @GET},
     * {@code @POST}, and so on) on the resource.
     */
    @Inject
    private Caller<UserComplaintEndpoint> endpoint;

    /**
     * Errai's Navigation module allows to inject page transitions for all
     * {@link Page}s of this application. It will automatically update the
     * fragment ID in the browser's location bar when the transition is executed
     * (the .go() method is invoked). This provides bookmarkable URLs and
     * automatic history support.
     */
    @Inject
    private TransitionTo<ComplaintSubmitted> complaintSubmittedPage;

    /**
     * A {@link TransitionAnchor} provides special support for using anchor tags
     * in HTML templates. When the anchor is clicked it will automatically
     * transition to the corresponding page.
     */
    @Inject
    @DataField
    private TransitionAnchor<Admin> admin;

    /**
     * This method is registered as an event handler for click events on the
     * submit button of the complaint form.
     *
     * @param e the click event.
     */
    @EventHandler("submit")
    private void onSubmit(ClickEvent e) {
        // Execute the REST call to store the complaint on the server
        endpoint.call(new ResponseCallback() {
            @Override
            public void callback(Response response) {
                if (response.getStatusCode() == Response.SC_CREATED) {
                    // Navigate to the "ComplaintSubmitted" page after we received a
                    // response from the server.
                    complaintSubmittedPage.go();
                }
            }
        }).create(userComplaint);
    }
}