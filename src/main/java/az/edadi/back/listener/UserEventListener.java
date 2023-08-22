package az.edadi.back.listener;

import az.edadi.back.constants.event.UserEvent;
import az.edadi.back.repository.UserEventsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserEventListener {

    private final UserEventsRepository userEventsRepository;

    @EventListener
    public void onApplicationEvent(UserEvent event) {
        userEventsRepository.saveEvent(event);
    }
}