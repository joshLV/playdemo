package models.jobs;

import models.jobs.annotation.JobDefine;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.modules.redis.Redis;
import util.mq.MQPublisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.Collections.EMPTY_LIST;

/**
 * 可记录运行历史的Job基类.
 *
 * 同时，通过Redis，保证同一时间(5秒内)只有一个Jobs能运行.
 */
public class JobWithHistory<V> extends Job<V> {

    private static ThreadLocal<List<String>> _threadLocalRemark = new ThreadLocal<>();

    private final static String REDIS_KEY_LAST_BEGIN_RUN_AT = "jobs.runed-at.";

    public void addRemark(String remark) {
        List<String> remarks = _threadLocalRemark.get();
        if (remarks == null) {
            remarks = new ArrayList<>();
            _threadLocalRemark.set(remarks);
        }
        if (StringUtils.isNotBlank(remark)) {
            remarks.add(remark);
        }
    }

    public List<String> safeGetRemarks() {
        List<String> remarks = _threadLocalRemark.get();
        return remarks == null ? EMPTY_LIST : remarks;
    }

    public void cleanRemarks() {
        _threadLocalRemark.remove();
    }

    /**
     * 如果另一个Job实例在指定毫秒中运行过，此job不再执行.
     * 默认10000毫秒，即10秒
     * @return 毫秒.
     */
    public long getDisableWhenOtherJobsRunInMillis() {
        return 10000l;
    }

    public String getLastBeginRunAtRedisKey() {
        return REDIS_KEY_LAST_BEGIN_RUN_AT + getClass().getName();
    }

    /**
     * 清除最后运行记录，单元测试前调用一下，以避免不行第二次执行job的问题.
     */
    public static void cleanLastBeginRunAtForTest() {
        Set<String> keys = Redis.keys(REDIS_KEY_LAST_BEGIN_RUN_AT + "*");
        for (String key : keys) {
            Redis.del(new String[]{key});
        }
    }

    /**
     * doJob中记录运行历史，发到MQ中保存
     * 并把doJob转到doJobWithHistory方法中
     */
    public final void doJob() throws Exception {
        JobsMessage message = generateJobsMessage();

        long beginAt = System.currentTimeMillis();

        // 为避免多个jvm之间重复 10秒内再次运行的job不被执行
        if (Redis.exists(getLastBeginRunAtRedisKey())) {
            long lastBeginAt = Long.parseLong(Redis.get(getLastBeginRunAtRedisKey()));
            if (Math.abs(lastBeginAt - beginAt) < getDisableWhenOtherJobsRunInMillis()) {
                // 在指定时间（10秒）内被运行过
                Logger.info("Job:" + message + "被另一进程在" + (new Date(lastBeginAt)) + "运行过，不再运行.");
                return;
            }
        }
        Redis.set(getLastBeginRunAtRedisKey(), String.valueOf(beginAt));

        try {
            doJobWithHistory();
            message.runStatus = "SUCCESS";
        } catch (Exception e) {
            message.runStatus = "FAIL";
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
