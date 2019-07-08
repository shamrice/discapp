package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Thread;

import java.util.ArrayList;
import java.util.List;

//todo : find a better place for this
public class ThreadTreeNode {

    private Thread current;
    private List<ThreadTreeNode> subThreads = new ArrayList<>();

    public ThreadTreeNode(Thread current) {
        this.current = current;
    }


    public Thread getCurrent() {
        return current;
    }


    public List<ThreadTreeNode> getSubThreads() {
        return subThreads;
    }

    public void setSubThreads(List<ThreadTreeNode> subThreads) {
        this.subThreads = subThreads;
    }

    public void addSubThread(ThreadTreeNode threadTreeNode) {
        this.subThreads.add(threadTreeNode);
    }
}
