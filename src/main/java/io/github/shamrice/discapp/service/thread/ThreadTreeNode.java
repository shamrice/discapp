package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.Thread;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ThreadTreeNode {

    @NonNull
    private Thread current;

    private List<ThreadTreeNode> subThreads = new ArrayList<>();

    void addSubThread(ThreadTreeNode threadTreeNode) {
        this.subThreads.add(threadTreeNode);
    }

    Date getNewestCreateDateInNodes() {
        Date newestDate = current.getCreateDt();

        for (ThreadTreeNode currentNode : subThreads) {
            newestDate = findNewestDateInSubThreads(currentNode, newestDate);
        }

        return newestDate;
    }

    private Date findNewestDateInSubThreads(ThreadTreeNode currentNode, Date currentMaxDate) {

        if (currentNode.getCurrent().getCreateDt().after(currentMaxDate)) {
            currentMaxDate = currentNode.getCurrent().getCreateDt();
        }

        for (ThreadTreeNode node : currentNode.subThreads) {
            currentMaxDate = findNewestDateInSubThreads(node, currentMaxDate);
        }

        return currentMaxDate;
    }
}
