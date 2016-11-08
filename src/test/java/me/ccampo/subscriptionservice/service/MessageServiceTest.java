package me.ccampo.subscriptionservice.service;

import com.google.common.collect.ImmutableList;
import me.ccampo.subscriptionservice.model.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author Chris Campo
 */
public class MessageServiceTest {

    private MessageService service;

    private Queue<Message> queue;

    @Mock
    private SubscriptionService subscriptionService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        queue = new LinkedList<>();
        service = new MessageService(queue, subscriptionService);
    }

    @Test
    public void createMessage() {
        final String type = "t1";
        final String content = "a";
        final Message result = service.createMessage(type, content);
        assertThat(result.id).isNotNull();
        assertThat(result.type).isEqualTo(type);
        assertThat(result.content).isEqualTo(content);
        verify(subscriptionService).sendMessageToSupportingSubscriptions(result);
    }

    @Test
    public void getMessages() {
        final Message msg1 = new Message("t1", "a");
        final Message msg2 = new Message("t2", "b");
        queue.add(msg1);
        queue.add(msg2);
        final ImmutableList<Message> result = service.getMessages();
        assertThat(result).containsExactlyElementsOf(ImmutableList.copyOf(queue.toArray(new Message[0])));
    }
}
