package models.jobs;

import models.jobs.annotation.JobDefine;
import org.jsoup.helper.StringUtil;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import util.mq.MQPublisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * 可记录运行历史的Job基类.
 */
public class JobWithHistory<V> extends Job<V> {

    private static ThreadLocal<List<String>> _threadLocalRemark = new ThreadLocal<>();

    public void addRemark(String remark) {
        List<String> remarks = _threadLocalRemark.get();
        if (remark == null) {
            remarks = new ArrayList<>();
            _threadLocalRemark.set(remarks);
        }
        remarks.add(remark);
    }

    public List<String> safeGetRemarks() {
        List<String> remarks = _threadLocalRemark.get();
        return remarks == null ? EMPTY_LIST : remarks;
    }

    public void cleanRemarks() {
        _threadLocalRemark.remove();
    }

    /**
     * doJob中记录运行历史，发到MQ中保存
     * 并把doJob转到doJobWithHistory方法中
     */
    public final void doJob() throws Exception {
        JobsMessage message = generateJobsMessage();

        long beginAt = System.currentTimeMillis();
        try {
            doJobWithHistory();
        } catch (Exception e) {
            addRemark("Exception:" + e.getMessage());
            throw e;
        } finally {
            long endAt = System.currentTimeMillis();
            message.runTimes = endAt - beginAt;
            message.runRemark(StringUtil.join(safeGetRemarks(), ""));
            MQPublisher.publish(JobsMessage.MQ_KEY, message);
            cleanRemarks();
        }

    }

    private JobsMessage generateJobsMessage() {
        JobsMessage message = JobsMessage.forClass(getClass().getName());

        Every every = getClass().getAnnotation(Every.class);
        if (every != null) {
            message.scheduled("@Every(" + every.value() + ")");
        }
        On onAnno = getClass().getAnnotation(On.class);
        if (onAnno != null) {
            message.scheduled("@On(" + onAnno.value() + ")");
        }

        JobDefine jobAnno = getClass().getAnnotation(JobDefine.class);
        if (jobAnno != null) {
            message.title(jobAnno.title());
            message.retainHistoryMinutes(jobAnno.retainHistoryMinutes());
            message.description(jobAnno.description());
        } else {
            message.title(getClass().getSimpleName());
            message.retainHistoryMinutes(30000);
        }
        message.runnedAt(new Date());
        return message;
    }

    public void doJobWithHistory() throws Exception {

    }
}
