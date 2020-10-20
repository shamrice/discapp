package io.github.shamrice.discapp.service.rss;

import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import io.github.shamrice.discapp.service.configuration.enums.RssBehavior;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RssService {

    @Autowired
    private ThreadRepository threadRepository;

    public List<Thread> getLatestThreadsForRssFeed(long appId, RssBehavior rssBehavior) {

        Pageable limit = PageRequest.of(0, 30);

        log.info("Getting latest 30 threads for RSS feed with type: " + rssBehavior.name());

        if (rssBehavior == RssBehavior.ALL || rssBehavior == RssBehavior.ALL_PREVIEW) {
            return threadRepository.findByApplicationIdAndDeletedAndIsApprovedOrderByCreateDtDesc(appId, false, true, limit);
        } else {
            return threadRepository.findByApplicationIdAndParentIdAndDeletedAndIsApprovedOrderByCreateDtDesc(appId, 0L,false, true, limit);
        }
    }
}
