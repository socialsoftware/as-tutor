package pt.ulisboa.tecnico.socialsoftware.auth.subscriptions;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.auth.repository.AuthUserRepository;
import pt.ulisboa.tecnico.socialsoftware.common.events.AddCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.common.events.DeleteAuthUserEvent;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException;

import static pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage.AUTHUSER_BY_USERID_NOT_FOUND;

@Component
public class AuthUserSubscriptions {
    private static Logger logger = LoggerFactory.getLogger(AuthUserSubscriptions.class);

    @Autowired
    private AuthUserRepository authUserRepository;

    @Subscribe
    public void addCourseExecution(AddCourseExecutionEvent event) {
        logger.info("Received addCourseExecution event!\n");
        AuthUser authUser = authUserRepository.findAuthUserById(event.getUserId())
                .orElseThrow(() -> new TutorException(AUTHUSER_BY_USERID_NOT_FOUND, event.getUserId()));

        logger.info(authUser.toString() + "\n");

        authUser.addCourseExecution(event.getCourseExecutionId());
    }

    @Subscribe
    public void deleteAuthUser(DeleteAuthUserEvent event) {
        logger.info("Received deleteAuthUser event!\n");
        AuthUser authUser = authUserRepository.findAuthUserById(event.getUserId())
                // Does not throw exception because when we anonymize users,
                // events are sent even if authUser does not exist
                .orElse(null);

        if (authUser != null) {
            logger.info(authUser + "\n");
            authUser.remove();
            authUserRepository.delete(authUser);
        }
    }
}