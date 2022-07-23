package course.concurrency.m2_async.executors.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AsyncClassTest {

    @Autowired
    public AsyncClassHelper asyncClassHelper;

    @EventListener(ApplicationReadyEvent.class)
    public void actionAfterStartup() {
        asyncClassHelper.runAsyncTask();
    }

    @Component
    static class AsyncClassHelper {

        @Autowired
        public ApplicationContext context;

        @Async
        public void runAsyncTask() {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) context.getBean("applicationTaskExecutor");
            System.out.println("Perfect place for breakpoint");
        }
    }
}
