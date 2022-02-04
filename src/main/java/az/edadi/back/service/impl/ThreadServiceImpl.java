package az.edadi.back.service.impl;

import az.edadi.back.entity.User;
import az.edadi.back.entity.message.Thread;
import az.edadi.back.entity.message.UserThread;
import az.edadi.back.exception.model.UserNotFoundException;
import az.edadi.back.model.request.ThreadRequestModel;
import az.edadi.back.model.response.ThreadResponseModel;
import az.edadi.back.repository.ThreadRepository;
import az.edadi.back.repository.UserRepository;
import az.edadi.back.repository.UserThreadRepository;
import az.edadi.back.service.ThreadService;
import az.edadi.back.utility.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ThreadServiceImpl implements ThreadService {
    private final ThreadRepository threadRepository;
    private final UserThreadRepository userThreadRepository;
    private final UserRepository userRepository;

    public ThreadServiceImpl(ThreadRepository threadRepository,
                             UserThreadRepository userThreadRepository, UserRepository userRepository) {
        this.threadRepository = threadRepository;
        this.userThreadRepository = userThreadRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ThreadResponseModel createThread(ThreadRequestModel threadRequestModel) {
        User targetUser=userRepository.findByUsername(threadRequestModel.getUsername()).orElseThrow(UserNotFoundException::new);
        Optional<List<UserThread>> userThreadList = userThreadRepository.getThreadsBWithUserIds(targetUser.getId(), AuthUtil.getCurrentUserId());

         if(userThreadList.isPresent()&&userThreadList.get().size()>0)
            return new ThreadResponseModel(userThreadList.get().get(0).getThread());

        Thread thread = threadRepository.save(new Thread());
        UserThread userThread = new UserThread();
        UserThread userThread2 = new UserThread();

        userThread.setUser(new User(AuthUtil.getCurrentUserId()));
        userThread2.setUser(targetUser);

        userThread.setThread(thread);
        userThread2.setThread(thread);

        userThreadRepository.save(userThread);
        userThread2= userThreadRepository.save(userThread2);
        thread.setUserThread(Arrays.asList(userThread2,userThread));

        return new ThreadResponseModel(threadRepository.findById(thread.getId()).get());

    }

    @Override
    public List<ThreadResponseModel> getThreads(int page) {
        Pageable pageable  = PageRequest.of(page, 15);
        List<UserThread> threadList = userThreadRepository.findByUserId(AuthUtil.getCurrentUserId(),pageable);
        return threadList.stream()
                .map(userThread -> new ThreadResponseModel(userThread.getThread()))
                .collect(Collectors.toList());
    }
}
