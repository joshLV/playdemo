package models.jobs;

import models.mq.QueueIDMessage;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于发送到MQ保存Jobs运行历史的Message对象.
 */
public class JobsMessage extends QueueIDMessage implements Serializable {

    private static final long serialVersionUID = 7063901063912330652L;

    public final static String MQ_KEY = "foundation.jobs.history";

    public String className;

    public String title;

    public String description;

    public String scheduledInfo;

    /**
     * 运行时间.
     */
    public Date runnedAt;

    /**
     * 运行备注.
     */
    public String runRemark;

    public String runStatus;

    /**
     * 运行时长，毫秒.
     */
    public long runTimes;

    /**
     * 历史保留的分钟数
     */
    public int retainHistoryMinutes;

    public static JobsMessage forClass(String className) {
        JobsMessage jobsMessage = new JobsMessage();
        jobsMessage.className = className;
        jobsMessage.retainHistoryMinutes = 6600;
        return jobsMessage;
    }

    public JobsMessage title(String _title) {
        this.title = _title;
        return this;
    }

    public JobsMessage description(String _desc) {
        this.description = _desc;
        return this;
    }

    public JobsMessage runnedAt(Date value) {
        this.runnedAt = value;
        return this;
    }

    public JobsMessage runRemark(String value) {
        this.runRemark = value;
        return this;
    }


    public JobsMessage runStatus(String value) {
        this.runStatus = value;
        return this;
    }

    public JobsMessage scheduled(String _scheduledInfo) {
        this.scheduledInfo = _scheduledInfo;
        return this;
    }

    public JobsMessage retainHistoryMinutes(int minutes) {
        this.retainHistoryMinutes = minutes;
        return this;
    }

    public JobsDefine toJobsDefine() {
        JobsDefine define = new JobsDefine();
        define.className = this.className;
        define.title = this.title;
        define.description = this.description;
        define.scheduledInfo = this.scheduledInfo;
        return define;
    }

    @Override
    public String getId() {
        return MQ_KEY + className;
    }
}
